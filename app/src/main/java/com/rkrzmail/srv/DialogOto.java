package com.rkrzmail.srv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.rkrzmail.oto.R;

public class DialogOto extends Dialog {

    @SuppressLint("StaticFieldLeak")
    static DialogOto instance;
    private View view;
    private TextView tvMessage, tvTittle;
    private ImageView imgDialog;
    private Context context;

    public static DialogOto getInstance(Context context) {
        if (instance == null) {
            instance = new DialogOto(context);
        }
        return instance;
    }

    @SuppressLint("InflateParams")
    public DialogOto(@NonNull Context context) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setCanceledOnTouchOutside(false);
        this.context = context;

        this.view = getLayoutInflater().inflate(R.layout.dialog_confirmation, null);
        tvMessage = view.findViewById(R.id.tv_message);
        tvTittle = view.findViewById(R.id.tv_tittle);
        imgDialog = view.findViewById(R.id.img_dialog);
        onClickBatal(null);

        this.setContentView(view);
    }

    @Override
    public void show() {
        if (!((Activity) context).isFinishing()) {
            super.show();
        } else {
            instance = null;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void onClickOk(View.OnClickListener onClick){
        view.findViewById(R.id.btn_ok).setOnClickListener(onClick);
    }

    public void onClickBatal(View.OnClickListener onClick){
        if(onClick != null){
            view.findViewById(R.id.btn_batal).setOnClickListener(onClick);
        }else{
            view.findViewById(R.id.btn_batal).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    public void setMessage(String message){
        tvMessage.setText(message);
    }


}
