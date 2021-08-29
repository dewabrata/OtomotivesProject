package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.MultipartRequest;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NumberFormatUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SAVE_OR_UPDATE_DISCOUNT_LOYALTY;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.ConstUtils.DATA;

public class AturDiscountLoyalty_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_loyalty);
        initToolbar();
        initComponent();
        loadData();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Voucher Discount");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        find(R.id.et_disc, EditText.class).addTextChangedListener(new NumberFormatUtils().percentTextWatcher(find(R.id.et_disc, EditText.class)));
        find(R.id.tv_tgl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerDialog();
            }
        });
        find(R.id.btn_kategori_disc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initKategoriDisc();
            }
        });
        initAutoCompleteNopol();
        initTextListenerPonsel();
    }

    private void initTextListenerPonsel() {
        find(R.id.et_no_ponsel, NikitaAutoComplete.class).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !find(R.id.et_no_ponsel, NikitaAutoComplete.class).getText().toString().contains("+62 ")) {
                    find(R.id.et_no_ponsel, NikitaAutoComplete.class).setText("+62 ");
                }
            }
        });

        find(R.id.et_no_ponsel, NikitaAutoComplete.class).addTextChangedListener(new TextWatcher() {
            int prevLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLength = s.length();
                Log.d("length__", "beforeTextChanged: " + prevLength);
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                find(R.id.et_no_ponsel, NikitaAutoComplete.class).removeTextChangedListener(this);
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting == 0) return;
                if (counting < 4 && !find(R.id.et_no_ponsel, NikitaAutoComplete.class).getText().toString().contains("+62 ")) {
                    find(R.id.et_no_ponsel, NikitaAutoComplete.class).setTag(null);
                    find(R.id.et_no_ponsel, NikitaAutoComplete.class).setText("+62 ");
                    Selection.setSelection(find(R.id.et_no_ponsel, NikitaAutoComplete.class).getText(), find(R.id.et_no_ponsel, NikitaAutoComplete.class).getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.et_no_ponsel, NikitaAutoComplete.class).requestFocus();
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }

                find(R.id.et_no_ponsel, NikitaAutoComplete.class).addTextChangedListener(this);
            }
        });
    }

    private void initAutoCompleteNopol() {
        find(R.id.et_nopol, NikitaAutoComplete.class).setThreshold(3);
        find(R.id.et_nopol, NikitaAutoComplete.class).setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = bookTitle.replaceAll(" ", "").toUpperCase();
                args.put("action", "SUGGESTION");
                args.put("nopol", nopol);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_NOMOR_POLISI), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                findView(convertView, R.id.title, TextView.class).setText(formatNopol(getItem(position).get("NO_POLISI").asString()));
                return convertView;
            }
        });

        find(R.id.et_nopol, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_nopol));
        find(R.id.et_nopol, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                find(R.id.et_nopol, NikitaAutoComplete.class).setText(formatNopol(n.get("NO_POLISI").asString()));
            }
        });

    }


    @SuppressLint("SetTextI18n")
    private void loadData() {
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (builderKategori.toString().isEmpty()) {
                    showWarning("KATEGORI DISCOUNT HARUS DI PILIH!");
                } else if (NumberFormatUtils.formatOnlyNumber(find(R.id.et_no_ponsel, NikitaAutoComplete.class).getText().toString()).equals("0")) {
                    find(R.id.et_no_ponsel, NikitaAutoComplete.class).setError("HARUS DI ISI");
                    viewFocus(find(R.id.et_no_ponsel, NikitaAutoComplete.class));
                } else if (find(R.id.et_nopol, NikitaAutoComplete.class).getText().toString().isEmpty()) {
                    find(R.id.et_nopol, NikitaAutoComplete.class).setError("HARUS DI ISI");
                    viewFocus(find(R.id.et_nopol, NikitaAutoComplete.class));
                } else if (find(R.id.tv_tgl, TextView.class).getText().toString().isEmpty()) {
                    find(R.id.tv_tgl, TextView.class).setError("HARUS DI ISI");
                    viewFocus(find(R.id.tv_tgl, NikitaAutoComplete.class));
                } else if (find(R.id.et_disc, EditText.class).getText().toString().isEmpty() || find(R.id.et_disc, EditText.class).getText().toString().equals("0,00")) {
                    find(R.id.et_disc, EditText.class).setError("HARUS DI ISI");
                    viewFocus(find(R.id.et_disc, EditText.class));
                } else {
                    saveData();
                }
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson response;

            @Override
            public void run() {
                MultipartRequest formBody = new MultipartRequest(getActivity());

                formBody.addString("CID", getSetting("CID"));
                formBody.addString("nopol", find(R.id.et_nopol, NikitaAutoComplete.class).getText().toString().replaceAll(" ", ""));
                formBody.addString("noPonsel", NumberFormatUtils.formatOnlyNumber(find(R.id.et_no_ponsel, NikitaAutoComplete.class).getText().toString()));
                formBody.addString("expiredDate", DateFormatUtils.formatDateToDatabase(find(R.id.tv_tgl, TextView.class).getText().toString()));
                formBody.addString("discPercent", find(R.id.et_disc, EditText.class).getText().toString());
                formBody.addString("userID", getSetting("user"));
                formBody.addString("isSendMessage", find(R.id.cb_kirim_pesan, CheckBox.class).isChecked() ? "Y" : "N");
                formBody.addString("kategoriDisc", builderKategori.toString());

                response = Nson.readJson(formBody.execute(AppApplication.getBaseUrlV4(SAVE_OR_UPDATE_DISCOUNT_LOYALTY)));
            }

            @Override
            public void runUI() {
                if (response.get("status").asBoolean()) {
                    AppApplication.getMessageTrigger();
                    showSuccess("Sukses Menambahkan Aktivitas Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Data!");
                }
            }
        });
    }

    public void getDatePickerDialog() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                assert date != null;
                find(R.id.tv_tgl, TextView.class).setText(sdf.format(date));
            }
        }, year, month, day);

        datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private final Nson kategoriSelectedList = Nson.newArray();
    boolean[] isSelectedKategoriArr = null;
    boolean selectAllKategori = true;
    StringBuilder builderKategori = new StringBuilder();

    private void initKategoriDisc() {
        final List<String> kategoriList = Arrays.asList("ALL", "PART", "LAYANAN");
        final List<String> selectedList = new ArrayList<>();
        selectedList.addAll(kategoriSelectedList.asArray());

        final String[] itemArray = kategoriList.toArray(new String[]{});
        isSelectedKategoriArr = new boolean[itemArray.length];
        if (selectedList.size() > 0) {
            for (int j = 0; j < itemArray.length; j++) {
                for (int i = 0; i < selectedList.size(); i++) {
                    if (itemArray[j].equals(selectedList.get(i))) {
                        isSelectedKategoriArr[j] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Kategori Discount")
                .setMultiChoiceItems(itemArray, isSelectedKategoriArr, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        kategoriSelectedList.asArray().clear();
                        kategoriSelectedList.asArray().addAll(selectedList);
                        if (selectedList.size() > 0) {
                            builderKategori = new StringBuilder();
                            for (int i = 0; i < selectedList.size(); i++) {
                                for (int j = 0; j < kategoriList.size(); j++) {
                                    if (selectedList.get(i).equals(kategoriList.get(j)) && !selectedList.get(i).equals("ALL")) {
                                        builderKategori.append(selectedList.get(i)).append(", ");
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
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
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
                    if (selectAllKategori) {
                        for (int i = 1; i < itemArray.length; i++) { // we start with first element after "Select all" choice
                            if (isChecked && !listView.isItemChecked(i)
                                    || !isChecked && listView.isItemChecked(i)) {
                                listView.performItemClick(listView, i, 0);
                            }
                        }
                    }
                } else {
                    if (!isChecked && listView.isItemChecked(0)) {
                        selectAllKategori = false;
                        listView.performItemClick(listView, 0, 0);
                        selectAllKategori = true;
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
                    isSelectedKategoriArr[position] = isChecked;
                } catch (Exception e) {
                    showError(selectedList.toString());
                }

            }
        });
    }

    private void initRvMerkKendaraan() {
        RecyclerView rv = findViewById(R.id.rv_kategori_disc);
        rv.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager
                (
                        1,
                        StaggeredGridLayoutManager.HORIZONTAL
                );
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(new NikitaRecyclerAdapter(kategoriSelectedList, R.layout.item_sort_by) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.img_check_selected).setVisibility(View.GONE);
                if (!kategoriSelectedList.get(position).asString().equals("ALL")) {
                    viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setText(kategoriSelectedList.get(position).asString());
                } else {
                    viewHolder.find(R.id.tv_tittle_sort_by, TextView.class).setVisibility(View.GONE);
                }
            }
        });
        Objects.requireNonNull(rv.getAdapter()).notifyDataSetChanged();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
