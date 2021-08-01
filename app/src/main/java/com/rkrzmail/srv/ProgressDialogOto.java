package com.rkrzmail.srv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rkrzmail.oto.R;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ProgressDialogOto extends Dialog {

    @SuppressLint("StaticFieldLeak")
    static ProgressDialogOto instance;
    View view;
    TextView tvMessage;
    ImageView ivSuccess;
    ImageView ivFailure;
    LinearLayout lyProgress, lyContainerProgress;
    ProgressBar pbProgress;
    AnimationDrawable adProgressSpinner;
    Context context;
    GifImageView gifProgress;
    OnDialogDismiss onDialogDismiss;

    public OnDialogDismiss getOnDialogDismiss() {
        return onDialogDismiss;
    }

    public void setOnDialogDismiss(OnDialogDismiss onDialogDismiss) {
        this.onDialogDismiss = onDialogDismiss;
    }

    public static ProgressDialogOto getInstance(Context context) {
        if (instance == null) {
            instance = new ProgressDialogOto(context);
        }
        return instance;
    }

    @SuppressLint("InflateParams")
    public ProgressDialogOto(Context context) {
        super(context, R.style.DialogTheme);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setCanceledOnTouchOutside(false);
        this.context = context;

        view = getLayoutInflater().inflate(R.layout.dialog_progress_oto, null);
        tvMessage = (TextView) view.findViewById(R.id.textview_message);
        ivSuccess = (ImageView) view.findViewById(R.id.imageview_success);
        ivFailure = (ImageView) view.findViewById(R.id.imageview_failure);
        gifProgress = (GifImageView) view.findViewById(R.id.gif_progress);
        lyContainerProgress = view.findViewById(R.id.ly_progress_oto);
        lyProgress = view.findViewById(R.id.ly_progress_horizontal);
        pbProgress = view.findViewById(R.id.progress_bar_horizontal);
        setGifProgress();

        this.setContentView(view);
    }

    public void setDownloadProgress(){
        lyProgress.setVisibility(View.VISIBLE);
        float dimens = context.getResources().getDimension(R.dimen.progress_download_dialog);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                (int)dimens,
                (int)dimens
        );
        lyContainerProgress.setLayoutParams(layoutParams);
        pbProgress.setMax(100);
    }

    private void setGifProgress(){
        try {
            GifDrawable gifDrawable = new GifDrawable(this.context.getResources().openRawResource(R.raw.speed_progress_3));
            gifProgress.setImageDrawable(gifDrawable);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setMessage(String message) {
        tvMessage.setText(message);
    }

    @Override
    public void show() {
        if (!((Activity) context).isFinishing()) {
            super.show();
        } else {
            instance = null;
        }
    }

    public void dismissWithSuccess() {
        tvMessage.setText("Success");
        showSuccessImage();

        if (onDialogDismiss != null) {
            this.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    onDialogDismiss.onDismiss();
                }
            });
        }
        dismissHUD();
    }

    public void dismissWithSuccess(String message) {
        showSuccessImage();
        if (message != null) {
            tvMessage.setText(message);
        } else {
            tvMessage.setText("");
        }

        if (onDialogDismiss != null) {
            this.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    onDialogDismiss.onDismiss();
                }
            });
        }
        dismissHUD();
    }

    public void dismissWithFailure() {
        showFailureImage();
        tvMessage.setText("Failure");
        if (onDialogDismiss != null) {
            this.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    onDialogDismiss.onDismiss();
                }
            });
        }
        dismissHUD();
    }

    public void dismissWithFailure(String message) {
        showFailureImage();
        if (message != null) {
            tvMessage.setText(message);
        } else {
            tvMessage.setText("");
        }
        if (onDialogDismiss != null) {
            this.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    onDialogDismiss.onDismiss();
                }
            });
        }
        dismissHUD();
    }

    protected void showSuccessImage() {
        //ivProgressSpinner.setVisibility(View.GONE);
        ivSuccess.setVisibility(View.VISIBLE);
    }

    protected void showFailureImage() {
        //ivProgressSpinner.setVisibility(View.GONE);
        ivFailure.setVisibility(View.VISIBLE);
    }

    protected void reset() {
        //ivProgressSpinner.setVisibility(View.VISIBLE);
        ivFailure.setVisibility(View.GONE);
        ivSuccess.setVisibility(View.GONE);
        tvMessage.setText("Memuat ...");
    }

    @SuppressLint("StaticFieldLeak")
    protected void dismissHUD() {
        AsyncTask<String, Integer, Long> task = new AsyncTask<String, Integer, Long>() {
            @Override
            protected Long doInBackground(String... params) {
                SystemClock.sleep(500);
                return null;
            }

            @Override
            protected void onPostExecute(Long result) {
                super.onPostExecute(result);
                dismiss();
                reset();
            }
        };
        task.execute();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        /*gifProgress.post(new Runnable() {
            @Override
            public void run() {
                adProgressSpinner.start();
            }
        });*/
    }

    public interface OnDialogDismiss {
         void onDismiss();
    }

}
