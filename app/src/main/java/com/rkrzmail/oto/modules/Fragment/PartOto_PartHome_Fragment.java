package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.PartHome_MainTab_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;
import static com.rkrzmail.utils.ConstUtils.RP;

public class PartOto_PartHome_Fragment extends Fragment {

    private RecyclerView rvPart;
    private AppActivity activity;
    private View fragmentView;
    SwipeRefreshLayout swipeRefreshLayout;
    private NikitaAutoComplete etSearch;
    private ImageView imgSubmit;

    private final Nson partList = Nson.newArray();

    public PartOto_PartHome_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = ((PartHome_MainTab_Activity) getActivity());
        fragmentView = inflater.inflate(R.layout.layout_recylerview_with_search_box, container, false);
        swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);
        etSearch = fragmentView.findViewById(R.id.et_search);
        imgSubmit = fragmentView.findViewById(R.id.img_submit_search);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewALLPart("");
            }
        });

        initRecylerviewPart();
        initAutoCompleteSearch();

        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
           viewALLPart("");
        }
    }

    private void initRecylerviewPart() {
        rvPart = fragmentView.findViewById(R.id.recyclerView);
        rvPart.setHasFixedSize(true);
        rvPart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_cari_part_oto) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(partList.get(position).get("MERK").asString());
                        viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(partList.get(position).get("NAMA_PART").asString());
                        viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(partList.get(position).get("NO_PART").asString());
                        viewHolder.find(R.id.tv_cari_het, TextView.class).setText(RP + NumberFormatUtils.formatRp(partList.get(position).get("HET").asString()));
                    }
                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

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

    public void viewALLPart(final String cari) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                swipeProgress(true);
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("search", cari);
                args.put("isPartHome", "Y");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
            }

            @Override
            public void runUI() {
                swipeProgress(false);
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    partList.asArray().clear();
                    partList.asArray().addAll(result.asArray());
                    rvPart.getAdapter().notifyDataSetChanged();
                } else {
                    activity.showError("Gagal Mencari Part");
                }
            }
        });
    }

    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }

    private void initAutoCompleteSearch() {
        final boolean[] isNoPart = new boolean[1];
        etSearch.setThreshold(0);
        etSearch.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {

            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Sparepart");
                args.put("search", bookTitle);
                args.put("jenis", "partOto");
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SUGGESTION), args));
                result = result.get("data");
                isNoPart[0] = result.get(0).get("NO_PART").asString().toLowerCase().contains(bookTitle.toLowerCase());
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                String search;
                if (isNoPart[0]) {
                    search = getItem(position).get("NO_PART").asString();
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
                    object = data.get("NO_PART").asString();
                } else {
                    object = data.get("NAMA_PART").asString();
                }
                etSearch.setText("");
                viewALLPart(object);
            }
        });

        imgSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etSearch.getText().toString().isEmpty()) {
                    viewALLPart(etSearch.getText().toString());
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

}
