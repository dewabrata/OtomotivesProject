package com.rkrzmail.oto.modules;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.rkrzmail.oto.R;

import java.util.Calendar;

public class TimeHourPicker_Dialog extends DialogFragment {

    private NumberPicker.OnValueChangeListener valueChangeListener;
    private OnClickDialog onClickDialog;

    public TimeHourPicker_Dialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Calendar day = Calendar.getInstance();
        View dialog = inflater.inflate(R.layout.dialog_time_hour_picker, null);
        final NumberPicker hours = dialog.findViewById(R.id.np_hour);
        final NumberPicker minutes = dialog.findViewById(R.id.np_min);

        hours.setMaxValue(24);
        minutes.setMaxValue(60);

        formatter(hours);
        formatter(minutes);

        alertBuilder.setView(dialog).setPositiveButton(Html.fromHtml("<font color='#FF4081'>OK</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int hour = hours.getValue();
                int minute = minutes.getValue();
                if (onClickDialog != null) {
                    onClickDialog.getTime(hour, minute);
                    dismiss();
                }
            }
        }).setNegativeButton(Html.fromHtml("<font color='#FF4081'>Cancel</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertBuilder.create();
    }

    public TimeHourPicker_Dialog getTimes(OnClickDialog onClickDialog) {
        this.onClickDialog = onClickDialog;
        return this;
    }


    public interface OnClickDialog {
        void getTime(int hours, int minutes);
        void getYear(int year);
    }

    private void formatter(NumberPicker numberPicker) {
        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        });
    }
}
