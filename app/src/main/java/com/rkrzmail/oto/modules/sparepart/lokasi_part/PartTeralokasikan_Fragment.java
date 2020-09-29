package com.rkrzmail.oto.modules.sparepart.lokasi_part;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.oto.modules.layanan.AturLayanan_Activity;
import com.rkrzmail.oto.modules.sparepart.stock_opname.StockOpname_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.CARI_PART_TERALOKASIKAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_ATUR_LOKASI;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_OPNAME;

public class PartTeralokasikan_Fragment extends Fragment {

    private  RecyclerView rvLokasi_part;
    private Nson nListArray = Nson.newArray();

    public PartTeralokasikan_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_part_teralokasikan_, container, false);
        rvLokasi_part = (RecyclerView) v.findViewById(R.id.recyclerView_teralokasikan);
        initComponent("");
        return v;
    }

    public void initComponent(String cari) {
        getTeralokasikan(cari);
        rvLokasi_part.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLokasi_part.setHasFixedSize(true);
        rvLokasi_part.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_lokasi_part) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_noFolder, TextView.class).setText(nListArray.get(position).get("KODE").asString());
                viewHolder.find(R.id.tv_lokasiPart, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_namaPart, TextView.class).setText( nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_nomor_part, TextView.class).setText(nListArray.get(position).get("NOMOR_PART_NOMOR").asString());
                viewHolder.find(R.id.tv_merk, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_stock, TextView.class).setText(nListArray.get(position).get("STOCK").asString());
                viewHolder.find(R.id.tv_pending, TextView.class).setText(nListArray.get(position).get("PENDING").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                Intent i = new Intent(getActivity(), AturLokasiPart_Activity.class);
                i.putExtra(CARI_PART_TERALOKASIKAN, nListArray.get(position).toJson());
                startActivityForResult(i, REQUEST_ATUR_LOKASI);
            }
        }));
    }

    public void getTeralokasikan(final String cari){
        ((LokasiPart_Activity) getActivity()).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", cari);
                args.put("flag", "TERALOKASI");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlokasipart"), args));
            }
            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvLokasi_part.getAdapter()).notifyDataSetChanged();
                }else{
                    ((LokasiPart_Activity) Objects.requireNonNull(getActivity())).showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getTeralokasikan("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK && requestCode == LokasiPart_Activity.REQUEST_ATUR){
            getTeralokasikan("");
        }else if(resultCode == getActivity().RESULT_OK && requestCode == REQUEST_ATUR_LOKASI){
            getTeralokasikan("");
        }
    }
}
