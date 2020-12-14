package com.rkrzmail.oto.modules.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.util.List;

public class TabUsaha_Fragment extends Fragment {

    private EditText etNamaBengkel, etAlamat, etBadanUsaha, etKotaKab, etNoponsel, etNib, etNpwp;
    private Spinner spAfiliasi, spJaringan, spJumlahUser;
    private Button btnSimpan;
    private CheckBox cbPkp;
    private MultiSelectionSpinner spJenisKendaraan, spBidangUsaha, spMerkKendaraan, spAktivitasUsaha;
    private LinearLayout lyJaringan;

    public TabUsaha_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_usaha_bengkel, container, false);
        initComponent(v);
        return v;
    }

    private void initComponent(View v) {

        etNamaBengkel = v.findViewById(R.id.et_namaBengkel_usaha);
        etAlamat = v.findViewById(R.id.et_alamat_usaha);
        etBadanUsaha = v.findViewById(R.id.et_namaUsaha_usaha);
        etKotaKab = v.findViewById(R.id.et_kotaKab_usaha);
        etNoponsel = v.findViewById(R.id.et_noPhone_usaha);
        etNib = v.findViewById(R.id.et_nib_usaha);
        etNpwp = v.findViewById(R.id.et_npwp_usaha);
        spAfiliasi = v.findViewById(R.id.sp_afiliasi_usaha);
        spJaringan = v.findViewById(R.id.sp_namaJaringan_usaha);
        spJenisKendaraan = v.findViewById(R.id.sp_jenisKendaraan_usaha);
        spBidangUsaha = v.findViewById(R.id.sp_bidangUsaha_usaha);
        spMerkKendaraan = v.findViewById(R.id.sp_merkKendaraan_usaha);
        spJumlahUser = v.findViewById(R.id.sp_jumlahUser_usaha);
        btnSimpan = v.findViewById(R.id.btn_simpan_usaha);
        cbPkp = v.findViewById(R.id.cb_pkp_usaha);
        lyJaringan = v.findViewById(R.id.ly_jaringan_usaha);
        spAktivitasUsaha = v.findViewById(R.id.sp_aktivitasUsaha_usaha);

        validation();

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void saveData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }
            @Override
            public void runUI() {

            }
        });
    }

    private void validation() {
        ((ProfileBengkel_Activity) getActivity()).spinnerNoDefaultOffline(spJumlahUser, getResources().getStringArray(R.array.max_jumlah_user));
        ((ProfileBengkel_Activity) getActivity()).spinnerNoDefaultOffline(spAfiliasi, getResources().getStringArray(R.array.afiliasi_usaha));
        ((ProfileBengkel_Activity) getActivity()).setMultiSelectionSpinnerFromApi(
                spJenisKendaraan, "nama", "BENGKEL", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                    @Override
                    public void selectedIndices(List<Integer> indices) {

                    }

                    @Override
                    public void selectedStrings(List<String> strings) {

                    }
                }, "TYPE", "");
        String[] items = getResources().getStringArray(R.array.aktivitas_usaha);
        spAktivitasUsaha.setItems(items);
        spAktivitasUsaha.setSelection(new int[]{});

        spAfiliasi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("INDIVIDUAL")) {
                    Tools.setViewAndChildrenEnabled(lyJaringan, false);
                } else {
                    Tools.setViewAndChildrenEnabled(lyJaringan, true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
