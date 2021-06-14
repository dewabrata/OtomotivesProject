package com.rkrzmail.oto.modules.Fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.AturLokasiPart_Activity;
import com.rkrzmail.oto.modules.sparepart.LokasiPart_MainTab_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_LOKASI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_TERALOKASIKAN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_ATUR_LOKASI;

public class PartTeralokasikan_Fragment extends Fragment {

    private RecyclerView rvLokasi_part;
    private NikitaAutoComplete etSearch;
    private ImageView imgSubmit;
    private Nson nListArray = Nson.newArray();
    private SwipeRefreshLayout swipeRefreshLayout;
    private View fragmentView;

    public PartTeralokasikan_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_part_teralokasikan_, container, false);
        rvLokasi_part = (RecyclerView) fragmentView.findViewById(R.id.recyclerView_teralokasikan);
        etSearch = fragmentView.findViewById(R.id.et_search);
        imgSubmit = fragmentView.findViewById(R.id.img_submit_search);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);

        initComponent();
        getTeralokasikan("");
        initAutoCompleteSearch();
        return fragmentView;
    }

    public void initComponent() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTeralokasikan("");
            }
        });

        rvLokasi_part.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLokasi_part.setHasFixedSize(true);
        rvLokasi_part.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_lokasi_part) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_noFolder, TextView.class).setText(nListArray.get(position).get("KODE").asString());
                viewHolder.find(R.id.tv_lokasiPart, TextView.class).setText(nListArray.get(position).get("LOKASI").asString());
                viewHolder.find(R.id.tv_namaPart, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
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


    public void getTeralokasikan(final String cari) {
        ((LokasiPart_MainTab_Activity) getActivity()).newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", cari);
                args.put("flag", "TERALOKASI");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LOKASI_PART), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    Objects.requireNonNull(rvLokasi_part.getAdapter()).notifyDataSetChanged();
                } else {
                    ((LokasiPart_MainTab_Activity) Objects.requireNonNull(getActivity())).showError(result.get("message").asString());
                }
            }
        });
    }

    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }

    @Override
    public void onResume() {
        super.onResume();
        getTeralokasikan("");
    }

    private void initAutoCompleteSearch() {
        final boolean[] isNoPart = new boolean[1];
        etSearch.setThreshold(0);
        etSearch.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "TERALOKASI");
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
                } else {
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
                    object = data.get("NOMOR_PART_NOMOR").asString();
                } else {
                    object = data.get("NAMA_PART").asString();
                }
                etSearch.setText("");
                getTeralokasikan(object);
            }
        });

        imgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etSearch.getText().toString().isEmpty()) {
                    getTeralokasikan(etSearch.getText().toString());
                } else {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == LokasiPart_MainTab_Activity.REQUEST_ATUR) {
            getTeralokasikan("");
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_ATUR_LOKASI) {
            getTeralokasikan("");
        }
    }
}
