package com.rkrzmail.oto.modules.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.AturLokasiPart_Activity;
import com.rkrzmail.oto.modules.Adapter.LokasiPart_MainTab_Activity_OLD;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_LOKASI_PART;

public class PartNonLokasi_Fragment extends Fragment {

    private Nson nListArray = Nson.newArray();
    private RecyclerView rvNonAlokasi;
    private ImageView imgSubmit;
    private NikitaAutoComplete etSearch;
    private View fragmentView;
    private SwipeRefreshLayout swipeRefreshLayout;

    public PartNonLokasi_Fragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_non_lokasi_part, container, false);
        rvNonAlokasi = fragmentView.findViewById(R.id.recyclerView_nonAlokasi);
        etSearch = fragmentView.findViewById(R.id.et_search);
        imgSubmit = fragmentView.findViewById(R.id.img_submit_search);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        initComponent();
        getNonTeralokasikan("");
        initAutoCompleteSearch();
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNonTeralokasikan("");
    }

    public void initComponent() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNonTeralokasikan("");
            }
        });

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
                        startActivityForResult(i, LokasiPart_MainTab_Activity_OLD.REQUEST_ATUR);
                    }
                })
        );
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }


    public void getNonTeralokasikan(final String cari){
        ((LokasiPart_MainTab_Activity_OLD) getActivity()).newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", cari);
                args.put("flag", "NON_TERALOKASI");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LOKASI_PART), args));
            }
            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvNonAlokasi.getAdapter().notifyDataSetChanged();
                } else {
                    ((LokasiPart_MainTab_Activity_OLD) getActivity()).showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }

    private void initAutoCompleteSearch(){
        final boolean[] isNoPart = new boolean[1];
        etSearch.setThreshold(3);
        etSearch.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "NON_TERALOKASI");
                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LOKASI_PART), args));
                result = result.get("data");
                isNoPart[0] = result.get(0).get("NOMOR_PART_NOMOR").asString().toLowerCase().contains(bookTitle.toLowerCase());
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                String search;
                if (isNoPart[0]) {
                    search = getItem(position).get("NOMOR_PART_NOMOR").asString();
                }else{
                    search = getItem(position).get("NAMA_PART").asString();
                }
                findView(convertView, R.id.title, TextView.class).setText(search);
                return convertView;
            }
        });

        etSearch.setLoadingIndicator((android.widget.ProgressBar) fragmentView.findViewById(R.id.pb_search));
        etSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Nson data = Nson.readJson(String.valueOf(parent.getItemAtPosition(position)));
                String object;
                if (isNoPart[0]) {
                    object = data.get(position).get("NOMOR_PART_NOMOR").asString();
                }else{
                    object =  data.get(position).get("NAMA_PART").asString();
                }
                etSearch.setText("");
                getNonTeralokasikan(object);
            }
        });

        imgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etSearch.getText().toString().isEmpty()){
                    getNonTeralokasikan(etSearch.getText().toString());
                }else{
                    imgSubmit.setVisibility(View.GONE);
                    etSearch.setError("Pencarian Harus di Isi");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            etSearch.setError(null);
                            imgSubmit.setVisibility(View.VISIBLE);
                        }
                    }, 2000);
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
        if(resultCode == getActivity().RESULT_OK && requestCode == LokasiPart_MainTab_Activity_OLD.REQUEST_ATUR){
            getNonTeralokasikan("");
        }
    }
}
