package com.rkrzmail.oto.modules.sparepart.lokasi_part;

import android.content.Intent;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_non_lokasi_part, container, false);
        rvNonAlokasi = v.findViewById(R.id.recyclerView_nonAlokasi);
        initComponent("");
        return v;
    }

    public void initComponent(String cari) {
        getNonTeralokasikan(cari);
        rvNonAlokasi.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNonAlokasi.setHasFixedSize(true);

        rvNonAlokasi.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_non_lokasi_part) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_merk_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_stock_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("STOCK").asString());
                viewHolder.find(R.id.tv_pending_nonLokasiPart, TextView.class).setText(nListArray.get(position).get("PENDING").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {
                        Intent i = new Intent(getActivity(), AturLokasiPart_Activity.class);
                        i.putExtra("NON_ALOKASI", nListArray.get(position).toJson());
                        startActivityForResult(i, LokasiPart_Activity.REQUEST_ATUR);
                    }
                })
        );
    }

    public void getNonTeralokasikan(final String cari){
        ((LokasiPart_Activity) getActivity()).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", cari);
                args.put("flag", "NON_TERALOKASI");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlokasipart"), args));
            }
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvNonAlokasi.getAdapter().notifyDataSetChanged();
                } else {
                    ((LokasiPart_Activity) getActivity()).showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getNonTeralokasikan("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK && requestCode == LokasiPart_Activity.REQUEST_ATUR){
            getNonTeralokasikan("");
        }
    }
}
