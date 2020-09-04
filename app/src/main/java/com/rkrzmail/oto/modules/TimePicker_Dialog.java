package com.rkrzmail.oto.modules;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

import com.rkrzmail.oto.R;

import java.util.Calendar;

public class TimePicker_Dialog extends DialogFragment {

    private NumberPicker.OnValueChangeListener valueChangeListener;
    private OnClickDialog onClickDialog;

    public TimePicker_Dialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Calendar day = Calendar.getInstance();
        View dialog = inflater.inflate(R.layout.dialog_time_picker, null);
        final NumberPicker days = dialog.findViewById(R.id.np_day);
        final NumberPicker hours = dialog.findViewById(R.id.np_hour);
        final NumberPicker minutes = dialog.findViewById(R.id.np_min);

        days.setMaxValue(99);
        hours.setMaxValue(24);
        minutes.setMaxValue(60);
        formatter(days);
        formatter(hours);
        formatter(minutes);

        alertBuilder.setView(dialog).setPositiveButton(Html.fromHtml("<font color='#FF4081'>OK</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int day = days.getValue();
                int hour = hours.getValue();
                int minute = minutes.getValue();
                if (onClickDialog != null) {
                    onClickDialog.getTime(day, hour, minute);
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

    public TimePicker_Dialog getTimes(OnClickDialog onClickDialog) {
        this.onClickDialog = onClickDialog;
        return this;
    }

    public interface OnClickDialog {
        void getTime(int day, int hours, int minutes);
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
