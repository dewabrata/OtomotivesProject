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
                args.put("PENEMPATAN", "");
                nListArray = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlokasipart"), args));

            }

            @Override
            public void runUI() {
                if (nListArray.get("status").asString().equalsIgnoreCase("OK")) {
                    //Log.d("HITUNG", nListArray.get("status").asString());
                    int jumlah = 0;
                    ArrayList<String> counting = new ArrayList<>();
                    for (int i = 0; i < nListArray.get("data").size(); i++) {
                        nListArray.toJson();

                        if (nListArray.get("data").get("PENEMPATAN").get("PALET") == null &&
                                nListArray.get("data").get("PENEMPATAN").get("RAK") == null ){

                            counting.add(nListArray.get("data").get(i).get("PENEMPATAN").asString());
                            jumlah = counting.size();
                            count.setText(String.valueOf(jumlah) + "PART BELUM DI ALOKASIKAN");
                            Log.d("HITUNG", String.valueOf(jumlah));
                            Log.d("HITUNG", nListArray.get("data").get(i).get("PENEMPATAN").asString());
                        }else{
                            Log.d("HITUNG", nListArray.get("data").get(i).get("PENEMPATAN").asString());
                        }
                        // Log.d("HITUNG", nListArray.get("data").get(i).get("PENEMPATAN").asString());
                    }
                }


            }
        });

        alokasi_part = v.findViewById(R.id.btn_alokasi_part);
        alokasi_part.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AturLokasiPart_Activity.class));
            }
        });
        return v;
    }
}
