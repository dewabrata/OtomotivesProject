package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.PartHome_MainTab_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.srv.SearchListener;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_SPAREPART;
import static com.rkrzmail.utils.ConstUtils.ALL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class PartBengkel_PartHome_Fragment extends Fragment implements SearchListener.ISearch, SearchListener.ISearchAutoComplete {

    private RecyclerView rvPart;
    private AppActivity activity;
    private View fragmentView;
    private SearchView.SearchAutoComplete searchAutoComplete = null;

    private final Nson partList = Nson.newArray();
    private SearchListener.IFragmentListener iFragmentListener = null;
    private String searchQuery = null;
    private String searchTag = null;

    public PartBengkel_PartHome_Fragment() {
    }

    public static PartBengkel_PartHome_Fragment newInstance(String query, String tag) {
        PartBengkel_PartHome_Fragment fragment = new PartBengkel_PartHome_Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(PartHome_MainTab_Activity.SEARCH_PART, query);
        bundle.putString(PartHome_MainTab_Activity.SEARCH_TAG, tag);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = ((PartHome_MainTab_Activity) getActivity());
        fragmentView = inflater.inflate(R.layout.activity_list_basic, container, false);
        SwipeRefreshLayout swipeRefreshLayout = fragmentView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setEnabled(false);
        initHideToolbar(fragmentView);
        initRecylerviewPart(fragmentView);

        return fragmentView;
    }

    private void initHideToolbar(View view) {
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iFragmentListener = (SearchListener.IFragmentListener) context;
        iFragmentListener.addiSearch(PartBengkel_PartHome_Fragment.this, PartBengkel_PartHome_Fragment.this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (iFragmentListener != null) {
            iFragmentListener.removeISearch(PartBengkel_PartHome_Fragment.this, PartBengkel_PartHome_Fragment.this);
        }
    }

    @Override
    public void onTextQuery(String text) {
        viewALLPart(text);
    }

    @Override
    public void attachAdapter(SearchView.SearchAutoComplete searchAutoComplete) {
        this.searchAutoComplete = searchAutoComplete;
        this.searchAutoComplete.setTag("BENGKEL");
        searchAutoComplete.setAdapter(autoCompleteAdapter());
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(i)));
                String object = n.get("NAMA_PART").asString();

                activity.find(R.id.search_src_text, SearchView.SearchAutoComplete.class).setText(object);
                activity.find(R.id.search_src_text, SearchView.SearchAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(i)));
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getArguments() != null) {
            searchQuery = (String) getArguments().get(PartHome_MainTab_Activity.SEARCH_PART);
            searchTag = (String) getArguments().get(PartHome_MainTab_Activity.SEARCH_TAG);
        }

        if (isVisibleToUser) {
            if(activity != null){
                activity.showError(searchTag);
            }
            if(searchTag != null){
                if(searchTag.equals("BENGKEL")){
                    attachAdapter(searchAutoComplete);
                    if (searchQuery != null) {
                        onTextQuery(searchQuery);
                    } else {
                        if(activity != null){
                            activity.showError(searchAutoComplete.getTag().toString());
                            viewALLPart("");
                        }
                    }
                }else{
                    attachAdapter(null);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
            if (searchQuery != null) {
                onTextQuery(searchQuery);
            } else {
                viewALLPart("");
            }
        }
    }

    private void initRecylerviewPart(View view) {
        rvPart = view.findViewById(R.id.recyclerView);
        rvPart.setHasFixedSize(true);
        rvPart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPart.setAdapter(new NikitaRecyclerAdapter(partList, R.layout.item_daftar_cari_part) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(partList.get(position).get("MERK").asString());
                viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(partList.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(partList.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(partList.get(position).get("STOCK_RUANG_PART").asString());

                if (partList.get(position).get("HARGA_JUAL").asString().equals("FLEXIBLE")) {
                    viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText("");
                    viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText(RP + NumberFormatUtils.formatRp(partList.get(position).get("HPP").asString()));
                } else {
                    viewHolder.find(R.id.tv_cari_harga_part, TextView.class).setText(RP + NumberFormatUtils.formatRp(partList.get(position).get("HARGA_JUAL").asString()));
                    viewHolder.find(R.id.tv_cari_hpp, TextView.class).setText("");
                }
                if (!partList.get(position).get("LOKASI").asString().equals("*")) {
                    viewHolder.find(R.id.tv_cari_pending, TextView.class).setText(partList.get(position).get("LOKASI").asString());
                } else {
                    viewHolder.find(R.id.tv_cari_pending, TextView.class).setText("");
                }

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {

            }
        }));
    }

    public void viewALLPart(final String cari) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("search", cari);
                args.put("lokasi", ALL);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
            }

            @Override
            public void runUI() {
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

    private NsonAutoCompleteAdapter autoCompleteAdapter() {
        return new NsonAutoCompleteAdapter(getActivity()) {
            Nson result;
            boolean isNoPart;
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("spec", "Bengkel");
                args.put("lokasi", "ALL");
                args.put("search", bookTitle);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SPAREPART), args));
                result = result.get("data");
                isNoPart = bookTitle.contains(result.get(0).get("NO_PART").asString());
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                String search;
                if(isNoPart){
                    search =  getItem(position).get("NO_PART").asString();
                }else{
                    if (!getItem(position).containsKey("NAMA_LAIN")) {
                        search = getItem(position).get("NAMA_PART").asString();
                    } else {
                        search = getItem(position).get("NAMA_PART").asString() + " ( " + getItem(position).get("NO_PART").asString() + " ) ";
                    }
                }


                activity.findView(convertView, R.id.title, TextView.class).setText(search);
                return convertView;
            }
        };
    }
}
