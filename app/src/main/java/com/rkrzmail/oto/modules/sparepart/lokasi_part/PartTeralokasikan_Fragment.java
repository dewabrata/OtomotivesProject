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
import com.rkrzmail.oto.modules.sparepart.stock_opname.StockOpname_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class PartTeralokasikan_Fragment extends Fragment {

    public static RecyclerView rvLokasi_part;
    private Nson nListArray = Nson.newArray();
    private static final int REQUEST_STOCK_OPNAME = 1212;
    private static final int REQUEST_BARCODE = 13;
    private View v;

    public PartTeralokasikan_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_part_teralokasikan_, container, false);
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

                String nama = nListArray.get(position).get("NAMA_MASTER").asString() + " / " + nListArray.get(position).get("NAMA_LAIN_MASTER").asString();

                viewHolder.find(R.id.tv_noFolder, TextView.class).setText(nListArray.get(position).get("NO_FOLDER").asString());
                viewHolder.find(R.id.tv_lokasiPart, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_namaPart, TextView.class).setText(nama);
                viewHolder.find(R.id.tv_nomor_part, TextView.class).setText(nListArray.get(position).get("NOMOR_PART_NOMOR").asString());
                viewHolder.find(R.id.tv_merk, TextView.class).setText(nListArray.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_stock, TextView.class).setText(nListArray.get(position).get("STOCK").asString());

                viewHolder.find(R.id.img_optionMenu, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.find(R.id.img_optionMenu, ImageButton.class));
                        popup.inflate(R.menu.menu_lokasi_part);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.action_stockOpname:
//                                        Stock opname : membuka form stock opname
                                        Intent i = new Intent(getActivity(), StockOpname_Activity.class);
                                        i.putExtra("NO_PART_ID", nListArray.get(position).toJson());
                                        startActivityForResult(i, REQUEST_STOCK_OPNAME);
                                        break;
                                    case R.id.action_qrCode:
                                        Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                                        startActivityForResult(intent, REQUEST_BARCODE);
                                        break;
                                    case R.id.action_hapusDaftar:
//                                        Hapus daftar : menghapus part dari lokasi penempatan
                                        break;
                                }
                                return true;
                            }
                        });
                        popup.show();
                    }
                });
            }
        });
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
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvLokasi_part.getAdapter().notifyDataSetChanged();
                }else{
                    ((LokasiPart_Activity) getActivity()).showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK && requestCode == LokasiPart_Activity.REQUEST_ATUR){
            getTeralokasikan("");
        }
    }
}
