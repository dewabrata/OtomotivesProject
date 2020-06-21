package com.rkrzmail.oto.modules.sparepart.lokasi_part;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PartNonLokasi_Fragment extends Fragment {

    private Nson nListArray = Nson.newArray();
    private RecyclerView rvNonAlokasi;

    public PartNonLokasi_Fragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_non_lokasi_part, container, false);
        initComponent(v);
        return v;
    }

    private void initComponent(View v) {
        rvNonAlokasi = v.findViewById(R.id.recyclerView_nonAlokasi);

        rvNonAlokasi.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNonAlokasi.setHasFixedSize(true);

        rvNonAlokasi.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_lokasi_part) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, final int position) {

                viewHolder.find(R.id.tv_namaPart_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("NO_FOLDER").asString());
                viewHolder.find(R.id.tv_noPart_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_merk_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());

            }
        });

        catchData();
    }

    private void catchData() {
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
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(nListArray.get("data").asArray());
                    rvNonAlokasi.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Gagal Memuat Data");
                }
            }
        });

    }

    private void showError(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

}
