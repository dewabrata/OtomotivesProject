package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.MapPicker_Dialog;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.GET_LOGO_BENGKEL;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO;

public class Usaha_Profile_Fragment extends Fragment implements OnMapReadyCallback, MapPicker_Dialog.GetLocation {


    private EditText etNamaBengkel, etAlamat, etBadanUsaha, etKotaKab, etNoTelp, etNib, etNpwp, etKodePos, etNoTelpMessage,
            etMaxAntrianExpress, etMaxAntrianStandart, etGoogleBisnis;
    private Spinner spAfiliasi, spMngKeuangan, spInspeksiMekanik;
    private MultiSelectionSpinner spJenisKendaraan;
    private CheckBox cbPkp;
    private AlertDialog alertDialog;
    private Button btnLogo, btnTampakDepan;
    private View fragmentView;

    private Bitmap bitmapLogo = null, bitmapTampakDepan = null;
    private String fotoLogoBase64 = "", fotoTampakDepanBase64 = "";
    private String latitude = "", longitude = "";
    private final String logoName = "logo.png";
    private final String tampakDepanName = "tampakDepan.png";
    List<String> loadPrincipalList = new ArrayList<>();

    private Nson dataPrincipalList = Nson.newArray();
    private final Nson saveDataprincipal = Nson.newArray();
    private final Nson saveDataMerk = Nson.newArray();
    private final Nson saveDataBidangUsaha = Nson.newArray();
    private Nson dataMerkKendaraan = Nson.newArray();
    private Nson dataBidangUsaha = Nson.newArray();
    private final List<String> merkListName = new ArrayList<>();
    private final List<String> bidangUsahaListName = new ArrayList<>();
    private final List<String> principalListName = new ArrayList<>();

    private Nson getData;

    private AppActivity activity;
    private File logoTempFile = null, tampakDepanTempFile = null;

    private boolean isLoadBitmapLogo = false, isLoadBitmapDepan = false;
    private boolean isLogo = false, isTampakDepan = false;

    private final List<String> afiliasiList = Arrays.asList(
            "--PILIH--",
            "JARINGAN",
            "INDIVIDUAL"
    );

    private GoogleMap map;


    public Usaha_Profile_Fragment() {
    }

    public Usaha_Profile_Fragment newIntasnce(Nson data) {
        this.getData = data;
        Bundle args = new Bundle();
        args.putString("DATA", data.toJson());
        Usaha_Profile_Fragment usahaProfileFragment = new Usaha_Profile_Fragment();
        usahaProfileFragment.setArguments(args);
        return usahaProfileFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       fragmentView = inflater.inflate(R.layout.fragment_tab_usaha_bengkel, container, false);
        activity = ((ProfileBengkel_Activity) getActivity());
        initComponent();
        initData();
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {
        }
    }

    private void initComponent() {
        etNamaBengkel = fragmentView.findViewById(R.id.et_namaBengkel_usaha);
        etAlamat = fragmentView.findViewById(R.id.et_alamat_usaha);
        etKodePos = fragmentView.findViewById(R.id.et_kodepos_usaha);
        etBadanUsaha = fragmentView.findViewById(R.id.et_namaUsaha_usaha);
        etKotaKab = fragmentView.findViewById(R.id.et_kotaKab_usaha);
        etNoTelp = fragmentView.findViewById(R.id.et_no_telp);
        etNoTelpMessage = fragmentView.findViewById(R.id.et_no_telp_message);
        etNib = fragmentView.findViewById(R.id.et_nib_usaha);
        etNpwp = fragmentView.findViewById(R.id.et_npwp_usaha);
        spAfiliasi = fragmentView.findViewById(R.id.sp_afiliasi_usaha);
        spJenisKendaraan = fragmentView.findViewById(R.id.sp_jenisKendaraan_usaha);
        cbPkp = fragmentView.findViewById(R.id.cb_pkp_usaha);
        etMaxAntrianExpress = fragmentView.findViewById(R.id.et_maxAntrianExpress);
        etMaxAntrianStandart = fragmentView.findViewById(R.id.et_maxAntrianStandart);
        ImageView imgAntrianExpress = fragmentView.findViewById(R.id.ic_AntrianExpress_usaha);
        ImageView imgAntrianStandart = fragmentView.findViewById(R.id.ic_AntrianStandart_usaha);
        btnLogo = fragmentView.findViewById(R.id.btn_logo_depan);
        btnTampakDepan = fragmentView.findViewById(R.id.btn_tampak_depan);
        Button btnSimpan = fragmentView.findViewById(R.id.btn_simpan_usaha);
        Button btnLokasi = fragmentView.findViewById(R.id.btn_lokasi_tambahan);
        etGoogleBisnis = fragmentView.findViewById(R.id.et_google_bisnis);
        spMngKeuangan  = fragmentView.findViewById(R.id.sp_management_keuangan);
        spInspeksiMekanik = fragmentView.findViewById(R.id.sp_inspeksi_mekanik);

        etNoTelpMessage.addTextChangedListener(textWatcherPonsel);
        final MapPicker_Dialog mapPicker_dialog = new MapPicker_Dialog();
        mapPicker_dialog.getBengkelLocation(this);
        btnLokasi.setEnabled(false);
        btnLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapPicker_dialog.show(getFragmentManager(), null);
            }
        });

        imgAntrianExpress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTimeHourDialog(etMaxAntrianExpress);
            }
        });

        imgAntrianStandart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTimeHourDialog(etMaxAntrianStandart);
            }
        });

        btnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLogo.getText().toString().equals("PREVIEW LOGO")) {
                    showDialogPreviewFoto(bitmapLogo, "Logo Bengkel", new String[]{}, true);
                } else {
                    isLogo = true;
                    isTampakDepan = false;
                    getImagePickerByGalerryOrCamera();
                }
            }
        });

        btnTampakDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnTampakDepan.getText().toString().equals("PREVIEW TAMPAK DEPAN")) {
                    showDialogPreviewFoto(bitmapTampakDepan, "Tampak Depan Bengkel", new String[]{}, true);
                } else {
                    isLogo = false;
                    isTampakDepan = true;
                    getImagePickerByGalerryOrCamera();
                }
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etKodePos.getText().toString().isEmpty()) {
                    activity.errorFocus(etKodePos, "KODE POS HARUS DI PILIH");
                } else if (spAfiliasi.getSelectedItem().toString().equals("--PILIH--")) {
                    activity.setErrorSpinner(spAfiliasi, "AFILIASI HARUS DI PILIH");
                }else if(etMaxAntrianExpress.getText().toString().isEmpty()){
                    activity.errorFocus(etMaxAntrianExpress, "WAKTU MAX ANTRIAN HARUS DI ISI");
                }else if(etMaxAntrianStandart.getText().toString().isEmpty()){
                    activity.errorFocus(etMaxAntrianStandart, "WAKTU MAX ANTRIAN HARUS DI ISI");
                } else {
                    saveData();
                }
            }
        });
    }

    TextWatcher textWatcherPonsel = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            TextInputLayout tlPonsel = fragmentView.findViewById(R.id.tl_nohp);
            int counting = (s == null) ? 0 : s.toString().length();
            if (counting == 0) {
                tlPonsel.setErrorEnabled(false);
            } else if (counting < 4) {
                etNoTelpMessage.setText("+62 ");
                Selection.setSelection(etNoTelpMessage.getText(), etNoTelpMessage.getText().length());
            } else if (counting < 12) {
                tlPonsel.setError("No. Hp Min. 6 Karakter");
                etNoTelpMessage.requestFocus();
            } else {
                tlPonsel.setErrorEnabled(false);
            }
        }
    };

    private Bitmap decodeBase64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void initData() {
        if (activity != null) {
            ((ProfileBengkel_Activity) Objects.requireNonNull(getActivity())).getDataUsaha(new ProfileBengkel_Activity.UsahaData() {
                @SuppressLint("SetTextI18n")
                @Override
                public void getData(Nson nson) {

                    etKodePos.setText(nson.get("KODE_POS").asString());
                    cbPkp.setChecked(nson.get("PKP").asString().equals("Y"));
                    etNpwp.setText(nson.get("NPWP").asString());
                    etNib.setText(nson.get("NIB").asString());
                    etBadanUsaha.setText(nson.get("NAMA_USAHA").asString());
                    etNamaBengkel.setText(nson.get("NAMA_BENGKEL").asString());
                    etAlamat.setText(nson.get("ALAMAT").asString());
                    etKotaKab.setText(nson.get("KOTA_KABUPATEN").asString());
                    etNoTelp.setText(nson.get("NO_TELP").asString());
                    etNoTelpMessage.setText(nson.get("HP_MESSAGE").asString());
                    etMaxAntrianExpress.setText(nson.get("MAX_ANTRIAN_EXPRESS_MENIT").asString());
                    etMaxAntrianStandart.setText(nson.get("MAX_ANTRIAN_STANDART_MENIT").asString());
                    etGoogleBisnis.setText(nson.get("GOOGLE_BISNIS").asString());

                    String isPembayaran = nson.get("PEMBAYARAN_ACTIVE").asString().equals("Y") ? "YA" : "TIDAK";

                    activity.setSpinnerOffline(Arrays.asList(activity.getResources().getStringArray(R.array.ya_tidak)), spMngKeuangan, isPembayaran);
                    setJenisKendaraan(Collections.singletonList(UtilityAndroid.getSetting(getContext(), "JENIS_KENDARAAN", "")));
                    setSpMerkKendaraan(nson.get("MERK_BENGKEL"));
                    setSpBidangUsaha(nson.get("BIDANG_USAHA_BENGKEL"));
                    setSpInspeksiMekanik(nson.get("INSPEKSI_MEKANIK").asString().equals("Y") ? "YA" : "TIDAK");
                    activity.setSpinnerOffline(afiliasiList, spAfiliasi, nson.get("AFILIASI").asString());

                    if (nson.get("PRINCIPAL_BENGKEL").asArray().size() > 0) {
                        Nson loadPrincipal = nson.get("PRINCIPAL_BENGKEL");
                        if (loadPrincipal.size() > 0) {
                            loadPrincipalList.clear();
                            for (int i = 0; i < loadPrincipal.size(); i++) {
                                loadPrincipalList.add(loadPrincipal.get(i).get("NAMA_PRINCIPAL").asString());
                            }
                        }
                    }

                    setSpNamaPrincipal(loadPrincipalList);
                }
            });
        }

        getImageBase64();
    }

    private void saveData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String latLong = latitude + ", " + longitude;
                String afliasi = spAfiliasi.getSelectedItem().toString();
                if (afliasi.contains("--PILIH--")) {
                    afliasi = afliasi
                            .replace("--PILIH--", "")
                            .replace("--PILIH--,", "");
                }
               
                args.put("action", "update");
                args.put("kategori", "USAHA");
                args.put("kodePos", etKodePos.getText().toString());
                args.put("namaUsaha", etBadanUsaha.getText().toString());
                args.put("nib", etNib.getText().toString());
                args.put("npwp", etNpwp.getText().toString());
                args.put("pkp", cbPkp.isChecked() ? "Y" : "N");
                args.put("afliasi", afliasi);
                args.put("namaPrincipial", "");
                args.put("noTelp", etNoTelp.getText().toString());
                args.put("hpMessage", etNoTelpMessage.getText().toString());
                args.put("fotoLogo", fotoLogoBase64);
                args.put("fotoTampakDepan", fotoTampakDepanBase64);
                args.put("antrianExpres", etMaxAntrianExpress.getText().toString());
                args.put("antrianStandart", etMaxAntrianStandart.getText().toString());
                args.put("petaLokasi", latLong);
                args.put("principalList", saveDataprincipal.toJson());
                args.put("googleBisnis", etGoogleBisnis.getText().toString());
                args.put("merkList", saveDataMerk.toJson());
                args.put("bidangUsahaList", saveDataBidangUsaha.toJson());
                args.put("pembayaranAktif", spMngKeuangan.getSelectedItem().toString().equals("YA") ? "Y" : "N");
                args.put("inspeksiMekanik", spInspeksiMekanik.getSelectedItem().toString().equals("YA") ? "Y" : "N");
                args.put("namaBengkel", etNamaBengkel.getText().toString());
                args.put("alamat", etAlamat.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PROFILE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    activity.setSetting("MAX_ANTRIAN_EXPRESS_MENIT", etMaxAntrianExpress.getText().toString());
                    activity.setSetting("MAX_ANTRIAN_STANDART_MENIT", etMaxAntrianStandart.getText().toString());
                    if (saveDataMerk.size() > 0)
                        activity.setSetting("MERK_KENDARAAN_ARRAY", saveDataMerk.toJson());
                    if (saveDataBidangUsaha.size() > 0)
                        activity.setSetting("BIDANG_USAHA_ARRAY", saveDataBidangUsaha.toJson());

                    activity.showSuccess("BEHASIL MENYIMPAN DATA");
                    ((ProfileBengkel_Activity) activity).getData();
                    initData();
                } else {
                    activity.showError(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private String formatAntrianTime(int jam, int menit) {
        try {
            return String.format("%02d:%02d:%02d", 0, jam, menit);
        } catch (Exception e) {
            return "";
        }
    }

    private void setJenisKendaraan(List<String> string) {
        spJenisKendaraan.setEnabled(false);
        spJenisKendaraan.setItems(string, true);
    }

    private final Nson merkSelectedList = Nson.newArray();
    boolean[] isSelectedMerkArr = null;
    boolean selectAllMerk = true;

    private void setSpMerkKendaraan(final Nson loadMerk) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("CID", "CID");
                args.put("flag", "Merk");
                args.put("jenisKendaraan", activity.getSetting("JENIS_KENDARAAN"));
                if (dataMerkKendaraan.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JENIS_KENDARAAN), args));
                    dataMerkKendaraan = result.get("data");
                }
                if(dataMerkKendaraan.size() > 0){
                    for (int i = 0; i < dataMerkKendaraan.size(); i++) {
                        merkListName.add(dataMerkKendaraan.get(i).get("MERK").asString());
                    }
                }
                merkSelectedList.asArray().clear();
                if(loadMerk.size() > 0){
                    for (int i = 0; i < loadMerk.size(); i++) {
                        merkSelectedList.add(loadMerk.get(i).get("MERK").asString());
                    }
                }
            }

            @Override
            public void runUI() {
                activity.findView(fragmentView, R.id.btn_merk_kendaraan, Button.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogMerkKendaraan();
                    }
                });
                initRvMerkKendaraan();
            }
        });
    }

    private void showDialogMerkKendaraan() {
        final List<String> selectedList = new ArrayList<>();
        selectedList.addAll(merkSelectedList.asArray());

        final String[] itemArray = merkListName.toArray(new String[]{});
        isSelectedMerkArr = new boolean[itemArray.length];
        if(selectedList.size() > 0){
            for (int j = 0; j < itemArray.length; j++) {
                for (int i = 0; i < selectedList.size(); i++) {
                    if(itemArray[j].equals(selectedList.get(i))){
                        isSelectedMerkArr[j] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Merk Kendaraan")
                .setMultiChoiceItems(itemArray, isSelectedMerkArr, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        merkSelectedList.asArray().clear();
                        merkSelectedList.asArray().addAll(selectedList);
                        if (selectedList.size() > 0) {
                            saveDataMerk.asArray().clear();
                            for (int i = 0; i < selectedList.size(); i++) {
                                for (int j = 0; j < dataMerkKendaraan.size(); j++) {
                                    if (selectedList.get(i).equals(dataMerkKendaraan.get(j).get("MERK").asString())) {
                                        saveDataMerk.add(Nson.newObject()
                                                .set("MERK_ID", dataMerkKendaraan.get(j).get("ID").asString())
                                                .set("MERK", dataMerkKendaraan.get(j).get("MERK").asString())
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                        initRvMerkKendaraan();
                    }
                })
                .setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedList.clear();
                    }
                });

        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();

        final ListView listView = alertDialog.getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isChecked = listView.isItemChecked(position);
                String itemSelected = parent.getItemAtPosition(position).toString();

                if (itemSelected.equals("ALL")) {
                    if (selectAllMerk) {
                        for (int i = 1; i < itemArray.length; i++) { // we start with first element after "Select all" choice
                            if (isChecked && !listView.isItemChecked(i)
                                    || !isChecked && listView.isItemChecked(i)) {
                                listView.performItemClick(listView, i, 0);
                            }
                        }
                    }
                } else {
                    if (!isChecked && listView.isItemChecked(0)) {
                        selectAllMerk = false;
                        listView.performItemClick(listView, 0, 0);
                        selectAllMerk = true;
                    }
                }

                try {
                    if (isChecked) {
                        selectedList.add(itemSelected);
                    } else {
                        if (selectedList.size() > 0) {
                            for (int i = 0; i < selectedList.size(); i++) {
                                if (selectedList.get(i).equals(itemSelected)) {
                                    selectedList.remove(i);
                                }
                            }
                        }
                    }
                    isSelectedMerkArr[position] = isChecked;
                } catch (Exception e) {
                    activity.showError(selectedList.toString());
                }

            }
        });
    }

    private void initRvMerkKendaraan(){
        RecyclerView rvMerk = setRecylerViewHorizontal(R.id.rv_merk_kendaraan, 2);
        if (rvMerk != null) {
            rvMerk.setAdapter(new NikitaRecyclerAdapter(merkSelectedList, R.layout.item_sort_by) {
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    viewHolder.find(R.id.img_check_selected).setVisibility(View.GONE);
                    if(!merkSelectedList.get(position).asString().equals("ALL")){
                        viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(merkSelectedList.get(position).asString());
                    }else{
                        viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setVisibility(View.GONE);
                    }
                }
            });
            Objects.requireNonNull(rvMerk.getAdapter()).notifyDataSetChanged();
        }

    }

    private void initRvBidangUsaha(){
        RecyclerView rvBidangUsaha = setRecylerViewHorizontal(R.id.rv_bidang_usaha, 2);
        if (rvBidangUsaha != null) {
            rvBidangUsaha.setAdapter(new NikitaRecyclerAdapter(bidangUsahaSelectedList, R.layout.item_sort_by) {
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    viewHolder.find(R.id.img_check_selected).setVisibility(View.GONE);
                    if(!bidangUsahaSelectedList.get(position).asString().equals("ALL")){
                        viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(bidangUsahaSelectedList.get(position).asString());
                    }else{
                        viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setVisibility(View.GONE);
                    }
                }
            });
            Objects.requireNonNull(rvBidangUsaha.getAdapter()).notifyDataSetChanged();
        }

    }

    private final Nson bidangUsahaSelectedList = Nson.newArray();
    boolean[] isSelectedBidangUsahaArr = null;

    private void setSpBidangUsaha(final Nson loadBidangUsaha) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "bidangUsaha");
                if (dataBidangUsaha.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
                    dataBidangUsaha = result.get("data");
                }

                if (dataBidangUsaha.size() > 0) {
                    for (int i = 0; i < dataBidangUsaha.size(); i++) {
                        if(dataBidangUsaha.get(i).get("JENIS_KENDARAAN").asString()
                                .equals(activity.getSetting("JENIS_KENDARAAN"))){
                            bidangUsahaListName.add(dataBidangUsaha.get(i).get("BIDANG_USAHA").asString());
                        }
                    }
                }
                bidangUsahaSelectedList.asArray().clear();
                if(loadBidangUsaha.size() > 0){
                    for (int i = 0; i < loadBidangUsaha.size(); i++) {
                        bidangUsahaSelectedList.add(loadBidangUsaha.get(i).get("KATEGORI").asString());
                    }
                }
            }

            @Override
            public void runUI() {
                activity.findView(fragmentView, R.id.btn_bidang_usaha, Button.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogBidangUsaha();
                    }
                });
                initRvBidangUsaha();
            }
        });
    }

    private void showDialogBidangUsaha(){
        final List<String> selectedList = new ArrayList<>();
        selectedList.addAll(bidangUsahaSelectedList.asArray());

        final String[] itemArray = bidangUsahaListName.toArray(new String[]{});
        isSelectedBidangUsahaArr = new boolean[itemArray.length];
        if(selectedList.size() > 0){
            for (int j = 0; j < itemArray.length; j++) {
                for (int i = 0; i < selectedList.size(); i++) {
                    if(itemArray[j].equals(selectedList.get(i))){
                        isSelectedBidangUsahaArr[j] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bidang Usaha")
                .setMultiChoiceItems(itemArray, isSelectedBidangUsahaArr, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        bidangUsahaSelectedList.asArray().clear();
                        bidangUsahaSelectedList.asArray().addAll(selectedList);
                        if (selectedList.size() > 0) {
                            saveDataBidangUsaha.asArray().clear();
                            for (int i = 0; i < selectedList.size(); i++) {
                                for (int j = 0; j < dataBidangUsaha.size(); j++) {
                                    if (selectedList.get(i).equals(dataBidangUsaha.get(j).get("BIDANG_USAHA").asString())) {
                                        saveDataBidangUsaha.add(Nson.newObject()
                                                .set("BIDANG_USAHA_ID", dataBidangUsaha.get(j).get("ID").asString())
                                                .set("BIDANG_USAHA", dataBidangUsaha.get(j).get("BIDANG_USAHA").asString())
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                        initRvBidangUsaha();
                    }
                })
                .setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedList.clear();
                    }
                });

        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();

        final ListView listView = alertDialog.getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isChecked = listView.isItemChecked(position);
                String itemSelected = parent.getItemAtPosition(position).toString();

                if (!isChecked && listView.isItemChecked(0)) {
                    listView.performItemClick(listView, 0, 0);
                }

                try {
                    if (isChecked) {
                        selectedList.add(itemSelected);
                    } else {
                        if (selectedList.size() > 0) {
                            for (int i = 0; i < selectedList.size(); i++) {
                                if (selectedList.get(i).equals(itemSelected)) {
                                    selectedList.remove(i);
                                }
                            }
                        }
                    }
                    isSelectedBidangUsahaArr[position] = isChecked;
                } catch (Exception e) {
                    activity.showError(e.getMessage());
                }

            }
        });
    }

    private final Nson principalSelectedList = Nson.newArray();
    boolean[] isSelectedPrincipalArr = null;

    private void setSpNamaPrincipal(final List<String> selectionList) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Principal");
                if (dataPrincipalList.size() == 0) {
                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("databengkel"), args));
                    dataPrincipalList = result.get("data");
                }
                principalListName.clear();
                if(dataPrincipalList.size() > 0){
                    for (int i = 0; i < dataPrincipalList.size(); i++) {
                        principalListName.add(dataPrincipalList.get(i).get("NAMA").asString());
                    }
                }
                principalSelectedList.asArray().clear();
                if(selectionList.size() > 0){
                    principalSelectedList.asArray().addAll(selectionList);
                }
            }

            @Override
            public void runUI() {
                activity.findView(fragmentView, R.id.btn_principal, Button.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogPrincipal();
                    }
                });
                initRvPrincipal();
            }
        });
    }

    private void showDialogPrincipal(){
        final List<String> selectedList = new ArrayList<>();
        selectedList.addAll(principalSelectedList.asArray());

        final String[] itemArray = principalListName.toArray(new String[]{});
        isSelectedPrincipalArr = new boolean[itemArray.length];
        if(selectedList.size() > 0){
            for (int j = 0; j < itemArray.length; j++) {
                for (int i = 0; i < selectedList.size(); i++) {
                    if(itemArray[j].equals(selectedList.get(i))){
                        isSelectedPrincipalArr[j] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bidang Usaha")
                .setMultiChoiceItems(itemArray, isSelectedPrincipalArr, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        principalSelectedList.asArray().clear();
                        principalSelectedList.asArray().addAll(selectedList);
                        if (selectedList.size() > 0) {
                            saveDataprincipal.asArray().clear();
                            for (int i = 0; i < selectedList.size(); i++) {
                                for (int j = 0; j < dataPrincipalList.size(); j++) {
                                    if (selectedList.get(i).equals(dataPrincipalList.get(j).get("NAMA").asString())) {
                                        saveDataprincipal.add(Nson.newObject()
                                                .set("PRINCIPAL_ID", dataBidangUsaha.get(j).get("ID").asString())
                                                .set("NAMA_PRINCIPAL", dataBidangUsaha.get(j).get("NAMA").asString())
                                        );
                                        break;
                                    }
                                }
                            }
                        }

                        initRvPrincipal();
                    }
                })
                .setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedList.clear();
                    }
                });

        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();

        final ListView listView = alertDialog.getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isChecked = listView.isItemChecked(position);
                String itemSelected = parent.getItemAtPosition(position).toString();

                if (!isChecked && listView.isItemChecked(0)) {
                    listView.performItemClick(listView, 0, 0);
                }

                try {
                    if (isChecked) {
                        selectedList.add(itemSelected);
                    } else {
                        if (selectedList.size() > 0) {
                            for (int i = 0; i < selectedList.size(); i++) {
                                if (selectedList.get(i).equals(itemSelected)) {
                                    selectedList.remove(i);
                                }
                            }
                        }
                    }
                    isSelectedPrincipalArr[position] = isChecked;
                } catch (Exception e) {
                    activity.showError(e.getMessage());
                }

            }
        });
    }

    private void initRvPrincipal(){
        RecyclerView rvPrincipal = setRecylerViewHorizontal(R.id.rv_principal, 2);
        if (rvPrincipal != null) {
            rvPrincipal.setAdapter(new NikitaRecyclerAdapter(principalSelectedList, R.layout.item_sort_by) {
                @Override
                public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                    super.onBindViewHolder(viewHolder, position);
                    viewHolder.find(R.id.img_check_selected).setVisibility(View.GONE);
                    viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(principalSelectedList.get(position).asString());
                }
            });
            Objects.requireNonNull(rvPrincipal.getAdapter()).notifyDataSetChanged();
        }

    }

    private RecyclerView setRecylerViewHorizontal(int viewId, int spanCount) {
        if (viewId == 0) return null;
        RecyclerView recyclerView = fragmentView.findViewById(viewId);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager
                (
                        spanCount,
                        StaggeredGridLayoutManager.HORIZONTAL
                );
        recyclerView.setLayoutManager(layoutManager);
        return recyclerView;
    }

    private void setSpInspeksiMekanik(final String selection){
        List<String> yaTidak = Arrays.asList("YA", "TIDAK");
        activity.setSpinnerOffline(yaTidak, spInspeksiMekanik, selection);
    }

    private void getImageBase64() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(activity.getApplicationContext(), "CID", "").trim();
                //logo
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_LOGO_BENGKEL), args));
                result = result.get("data");

                String base64String = result.get("LOGO_IMAGE").asString();
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                bitmapLogo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                base64String = result.get("TAMPAK_DEPAN_IMAGE").asString();
                decodedString = Base64.decode(base64String, Base64.DEFAULT);
                bitmapTampakDepan = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            @Override
            public void runUI() {
                if (bitmapLogo != null)
                    btnLogo.setText("PREVIEW LOGO");
                if (bitmapTampakDepan != null)
                    btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
            }
        });

    }

    private void getImageFromAlbum(final int REQUEST) {
        try {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, REQUEST);
        } catch (Exception exp) {
            Log.i("Error", exp.toString());
        }
    }

    public void getImagePickerByGalerryOrCamera() {
        final List<Intent> intents = new ArrayList<>();
        intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

        Intent result = Intent.createChooser(intents.remove(0), null);
        result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
        startActivityForResult(result, REQUEST_FOTO);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOTO) {
            Bundle extras = null;
            Uri imageUri = null;
            if (data != null) {
                extras = data.getExtras();
                if (extras == null)
                    imageUri = data.getData();
            }
            if (imageUri != null) {
                try {
                    if (isLogo) {
                        bitmapLogo = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                    } else {
                        if (isTampakDepan) {
                            bitmapTampakDepan = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (isLogo) {
                    bitmapLogo = (Bitmap) (extras != null ? extras.get("data") : null);
                } else {
                    if (isTampakDepan) {
                        bitmapTampakDepan = (Bitmap) (extras != null ? extras.get("data") : null);
                    }
                }
            }

            fotoLogoBase64 = activity.bitmapToBase64(bitmapLogo);
            fotoTampakDepanBase64 = activity.bitmapToBase64(bitmapTampakDepan);

            if (bitmapLogo != null)
                btnLogo.setText("PREVIEW LOGO");
            if (bitmapTampakDepan != null)
                btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
        }
    }

    @SuppressLint("SetTextI18n")
    private void showDialogPreviewFoto(final Bitmap bitmap, String toolbarTittle, final String[] base64, final boolean isPreview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
        builder.setView(dialogView);

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
        Button btnCancel = dialogView.findViewById(R.id.btn_alert_cancel);
        Button btnSimpan = dialogView.findViewById(R.id.btn_alert_save);

        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(toolbarTittle);

        if (bitmap != null)
            img.setImageBitmap(bitmap);

        if (isPreview) {
            btnCancel.setText("Tutup");
            btnSimpan.setText("Foto Ulang");
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreview) {
                    alertDialog.dismiss();
                } else {
                    if (bitmap == bitmapLogo) {
                        if (!isLoadBitmapLogo) {
                            bitmapLogo = null;
                        }
                    } else if (bitmap == bitmapTampakDepan) {
                        if (!isLoadBitmapDepan) {
                            bitmapTampakDepan = null;
                        }
                    }
                    alertDialog.dismiss();
                }

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreview) {
                    if (bitmap == bitmapLogo) {
                        isLogo = true;
                        isTampakDepan = false;
                        getImagePickerByGalerryOrCamera();
                    } else if (bitmap == bitmapTampakDepan) {
                        isLogo = false;
                        isTampakDepan = true;
                        getImagePickerByGalerryOrCamera();
                    }
                } else {
                    if (bitmap == bitmapLogo) {
                        btnLogo.setText("PREVIEW LOGO");
                    } else if (bitmap == bitmapTampakDepan) {
                        btnTampakDepan.setText("PREVIEW TAMPAK DEPAN");
                    }
                    if (bitmap != null) {
                        base64[0] = activity.bitmapToBase64(bitmap);
                    }
                    activity.showInfo("SUKSES MENYIMPAN FOTO");
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog = builder.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        cursor.close();
        return cursor.getString(idx);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void getLatLong(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
