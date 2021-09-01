package com.rkrzmail.oto.modules;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputContentInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.RegistrasiBengkel_Activity;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;
import com.rkrzmail.srv.ZanyEditText;
import com.rkrzmail.utils.APIUrls;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_LOGIN;

public class Otp_Activity extends AppActivity {

    private char[] otp;
    private String one, two, three, four, five, six;
    private boolean isRegist = false;
    private Nson bengkelList = Nson.newArray();

    private int count2 = 0, count3 = 0, count4 = 0, count5 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setContentView(R.layout.activity_otp);

        isRegist = getIntent().hasExtra("REGISTRASI");

        find(R.id.et1, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                count2 = 0;
                one = s.toString();
                if (s.length() == 1) {
                    find(R.id.et2, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et2, ZanyEditText.class).addTextChangedListener(new TextWatcher() {
            private int lastLength;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastLength = s.length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                two = s.toString();
                if (s.length() == 1) {
                    find(R.id.et3, EditText.class).requestFocus();
                }

            }
        });

        find(R.id.et3, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                count2 = 0;
                three = s.toString();
                if (s.length() == 1) {
                    find(R.id.et4, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et4, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                four = s.toString();
                if (s.length() == 1) {
                    find(R.id.et5, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et5, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                five = s.toString();
                if (s.length() == 1) {
                    find(R.id.et6, EditText.class).requestFocus();
                }
            }
        });

        find(R.id.et6, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    find(R.id.et6, EditText.class).clearFocus();
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(((EditText) findViewById(R.id.et1)).getText().toString());
                stringBuilder.append(((EditText) findViewById(R.id.et2)).getText().toString());
                stringBuilder.append(((EditText) findViewById(R.id.et3)).getText().toString());
                stringBuilder.append(((EditText) findViewById(R.id.et4)).getText().toString());
                stringBuilder.append(((EditText) findViewById(R.id.et5)).getText().toString());
                stringBuilder.append(((EditText) findViewById(R.id.et6)).getText().toString());
                String dummy = stringBuilder.toString();

                if (dummy.length() == 6) {
                    if (isRegist) {
                        checkOtpRegistasi(dummy);
                    } else {
                        login(dummy);
                    }
                }
            }
        });

        String tittle = "<font color=#F44336><u>Kirim Ulang OTP</u></font>";
        find(R.id.tv_reqOtp, TextView.class).setText(Html.fromHtml(tittle));
        find(R.id.tv_reqOtp, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOtp();
            }
        });
    }

    private void requestOtp() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", isRegist ? "requestOtp" : "Request");
                args.put("user", getIntentStringExtra("user"));
                args.put("nohp", getIntentStringExtra("user"));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(isRegist ? APIUrls.SET_REGISTRASI : "login"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Request OTP, Silahkan Login");

                } else {
                    showError("Gagal Request OTP");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getActivity(), LoginActivity.class));
        finish();
    }

    private void login(final String otp) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            String sResult;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Login");
                args.put("user", getIntentStringExtra("user"));
                args.put("password", otp);

                sResult = (InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_LOGIN), args));
            }

            @Override
            public void runUI() {
                Nson nson = Nson.readNson(sResult);//test1
                if (nson.get("status").asString().equalsIgnoreCase("OK")) {
                    if (nson.get("data").get(0).get("status").asString().equalsIgnoreCase("error")) {
                        showError("User tidak di temukan / password salah");
                        return;
                    }

                    nson = nson.get("data").get(0);
                    if (nson.get("TIPE_USER").asString().equals("MEKANIK") || nson.get("MEKANIK").asString().equals("YA")) {
                        setSetting("MEKANIK", "TRUE");
                    } else {
                        setSetting("MEKANIK", "FALSE");
                    }

                    setSetting("result", nson.toJson());
                    //user
                    setSetting("NAMA_USER", nson.get("NAMA_USER").asString());
                    setSetting("TIPE_USER", nson.get("TIPE_USER").asString());
                    setSetting("ACCESS_MENU", nson.get("AKSES_APP").asString());
                    setSetting("POSISI", nson.get("POSISI").asString());
                    setSetting("session", nson.get("token").asString());
                    if (getIntentStringExtra("user").isEmpty()) {
                        setSetting("user", formatOnlyNumber(getIntentStringExtra("user")));
                    }else{
                        setSetting("user", formatOnlyNumber(getIntentStringExtra("user")));
                    }

                    //bengkel
                    if (nson.get("BENGKEL").asArray().size() > 1 && nson.get("TIPE_USER").asString().equals("ADMIN")) {
                        bengkelList.asArray().addAll(nson.get("BENGKEL").asArray());
                        initDialogSelectBengkel();
                    } else {
                        nson = nson.get("BENGKEL").get(0);
                        if (nson.asString().isEmpty()) {
                            showWarning("System Sedang Error, Hubungi Administrator");
                            return;
                        }
                        setSetting("CID", nson.get("CID").asString());
                        setSetting("STATUS_BENGKEL", nson.get("STATUS_BENGKEL").asString());
                        setSetting("PEMBAYARAN_ACTIVE", nson.get("PEMBAYARAN_ACTIVE").asString());
                        setSetting("NAMA_BENGKEL", nson.get("NAMA_BENGKEL").asString());
                        setSetting("JENIS_KENDARAAN", nson.get("JENIS_KENDARAAN").asString().trim());
                        setSetting("MERK_KENDARAAN_BENGKEL", nson.get("MERK_KENDARAAN").asString());
                        setSetting("KATEGORI_BENGKEL", nson.get("KATEGORI_BENGKEL").asString());
                        setSetting("MAX_ANTRIAN_EXPRESS_MENIT", nson.get("MAX_ANTRIAN_EXPRESS_MENIT").asString());
                        setSetting("MAX_ANTRIAN_STANDART_MENIT", nson.get("MAX_ANTRIAN_STANDART_MENIT").asString());
                        setSetting("DP_PERSEN", nson.get("DP_PERSEN").asString());
                        if (nson.get("BIDANG_USAHA_ARRAY").size() > 0) {
                            setSetting("BIDANG_USAHA_ARRAY", nson.get("BIDANG_USAHA_ARRAY").toJson());
                        }
                        if (nson.get("MERK_KENDARAAN_ARRAY").size() > 0) {
                            setSetting("MERK_KENDARAAN_ARRAY", nson.get("MERK_KENDARAAN_ARRAY").toJson());
                        }
                        setSetting("L", "L");

                        Intent intent = new Intent(getActivity(), MenuActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    showError(nson.get("error").asString());
                }
            }
        });
    }

    private void checkOtpRegistasi(final String kodeOtp) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "checkOtp");
                args.put("nohp", getIntentStringExtra("NO_PONSEL"));
                args.put("otp", kodeOtp);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(APIUrls.SET_REGISTRASI), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Silahkan Lanjutkan Pendaftaran!");
                    Intent intent = new Intent(getActivity(), RegistrasiBengkel_Activity.class);
                    intent.putExtra("NO_PONSEL", getIntentStringExtra("NO_PONSEL"));
                    startActivity(intent);
                    finish();
                } else {
                    showError("Gagal Request OTP");
                }
            }
        });
    }

    private void initDialogSelectBengkel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_selection_bengkel, null);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        initRvBengkel(dialogView);
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    private void initRvBengkel(View dialogView) {
        RecyclerView rvBengkel = dialogView.findViewById(R.id.recyclerView);
        rvBengkel.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvBengkel.setHasFixedSize(false);
        rvBengkel.setAdapter(new NikitaRecyclerAdapter(bengkelList, R.layout.item_selection_bengkel) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                //viewHolder.find(R.id.img_logo_bengkel, ImageView.class);
                viewHolder.find(R.id.tv_nama_bengkel, TextView.class).setText(bengkelList.get(position).get("NAMA_BENGKEL").asString());
                viewHolder.find(R.id.tv_tgl_registrasi, TextView.class).setText(bengkelList.get(position).get("TANGGAL_REGISTRASI").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                setSetting("MERK_KENDARAAN_BENGKEL", parent.get(position).get("MERK_KENDARAAN").asString());
                setSetting("KATEGORI_BENGKEL", parent.get(position).get("KATEGORI_BENGKEL").asString());
                setSetting("MAX_ANTRIAN_EXPRESS_MENIT", parent.get(position).get("MAX_ANTRIAN_EXPRESS_MENIT").asString());
                setSetting("MAX_ANTRIAN_STANDART_MENIT", parent.get(position).get("MAX_ANTRIAN_STANDART_MENIT").asString());
                setSetting("DP_PERSEN", parent.get(position).get("DP_PERSEN").asString());
                setSetting("STATUS_BENGKEL", parent.get(position).get("STATUS_BENGKEL").asString());
                setSetting("PEMBAYARAN_ACTIVE", parent.get(position).get("PEMBAYARAN_ACTIVE").asString());
                setSetting("NAMA_BENGKEL", parent.get(position).get("NAMA_BENGKEL").asString());
                setSetting("JENIS_KENDARAAN", parent.get(position).get("JENIS_KENDARAAN").asString().trim());
                setSetting("CID", parent.get(position).get("CID").asString());
                if (parent.get(position).get("BIDANG_USAHA").size() > 0) {
                    setSetting("BIDANG_USAHA_ARRAY", parent.get(position).get("BIDANG_USAHA").toJson());
                }
                if (parent.get(position).get("MERK_KENDARAAN_BENGKEL").size() > 0) {
                    setSetting("MERK_KENDARAAN_ARRAY", parent.get(position).get("MERK_KENDARAAN_BENGKEL").asString());
                }
                setSetting("L", "L");

                Intent intent = new Intent(getActivity(), MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }));
    }
}