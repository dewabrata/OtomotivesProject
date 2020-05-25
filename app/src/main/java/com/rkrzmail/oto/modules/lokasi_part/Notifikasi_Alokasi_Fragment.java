package com.rkrzmail.oto.modules.lokasi_part;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.ArrayList;
import java.util.Map;

public class Notifikasi_Alokasi_Fragment extends Fragment {

    private TextView count;
    private Button alokasi_part;

    public Notifikasi_Alokasi_Fragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.notifikasi_alokasi_part, container, false);

        count = v.findViewById(R.id.tv_count_alokasi_part);

        MessageMsg.newTask(getActivity(), new Messagebox.DoubleRunnable() {
            Nson nListArray;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                nListArray = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlokasipart"), args));

            }

            @Override
            public void runUI() {
                if (nListArray.get("status").asString().equalsIgnoreCase("OK")) {

                    int jumlah = 0;
                    StringBuilder strData = new StringBuilder();
                    for (int i = 0; i < nListArray.get("data").size(); i++) {
                        strData.append(nListArray.get("data").get(i).get("PENEMPATAN"));
                    }
                    Log.d("Notifikasi", String.valueOf(strData.length()));
                    jumlah = strData.length();
                    count.setText(jumlah + " " + "PART BELUM DI ALOKASIKAN");

                    }

            }
        });


        alokasi_part = v.findViewById(R.id.btn_alokasi_part);
        alokasi_part.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CariPart_Activity.class));
            }
        });
        return v;
    }
}
