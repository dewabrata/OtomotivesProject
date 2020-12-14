package com.rkrzmail.oto.modules.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabTambahan_Fragment extends Fragment {

    private Spinner spFasilitas, spBooking;
    private EditText etHomeKm, etEmergencyKm, etJemputKm, etHomeRadius, etEmergencyRadius, etJemputRadius, etKapasitas, etMaxHari, etBiaya;
    private Button btnSimpan;
    private LinearLayout lyLayanan, lyTambahan, lyEntryMax, lyEntryKm;
    private MultiSelectionSpinner spLayanan;

    public TabTambahan_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_tambahan_bengkel, container, false);
        initComponent(v);
        return v;
    }

    private void initComponent(View v) {
        spFasilitas = v.findViewById(R.id.sp_fasilitas_tambahan);
        spBooking = v.findViewById(R.id.sp_booking_tambahan);
        spLayanan = v.findViewById(R.id.sp_afiliasi_tambahan);
        etHomeKm = v.findViewById(R.id.et_homeKm_tambahan);
        etEmergencyKm = v.findViewById(R.id.et_emgKm_tambahan);
        etJemputKm = v.findViewById(R.id.et_jemputKm_tambahan);
        etEmergencyRadius = v.findViewById(R.id.et_emgRadius_tambahan);
        etHomeRadius = v.findViewById(R.id.et_homeRadius_tambahan);
        etJemputRadius = v.findViewById(R.id.et_jemputRadius_tambahan);
        etKapasitas = v.findViewById(R.id.et_kapasitas_tambahan);
        etMaxHari = v.findViewById(R.id.et_maxHari_tambahan);
        etBiaya = v.findViewById(R.id.et_biayaSimpan_tambahan);
        btnSimpan = v.findViewById(R.id.btn_simpan_tambahan);
        lyLayanan = v.findViewById(R.id.ly_layanan_tambahan);
        lyEntryKm = v.findViewById(R.id.ly_entryKm_tambahan);
        lyEntryMax = v.findViewById(R.id.ly_entryMax_tambahan);
        lyTambahan = v.findViewById(R.id.ly_tambahan);

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
        String[] items = getResources().getStringArray(R.array.layanan_tambahan);
        spLayanan.setItems(items);
        spLayanan.setSelection(new int[]{});
        spLayanan.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {
            }

            @Override
            public void selectedStrings(List<String> strings) {
                for (View view : lyTambahan.getTouchables()) {
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        editText.setEnabled(false);
                        if (strings.contains("HOME")) {
                            etHomeKm.setEnabled(true);
                            etHomeRadius.setEnabled(true);
                        } else if (strings.contains("ANTAR - JEMPUT")) {
                            etJemputKm.setEnabled(true);
                            etJemputRadius.setEnabled(true);
                        } else if (strings.contains("EMERGENCY")) {
                            etEmergencyKm.setEnabled(true);
                            etEmergencyRadius.setEnabled(true);
                        } else {
                            editText.setEnabled(false);

                        }
                    }
                }
            }
        });

        spBooking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("TIDAK")) {
                    Tools.setViewAndChildrenEnabled(lyTambahan, false);
                } else {
                    Tools.setViewAndChildrenEnabled(lyTambahan, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}
