package com.rkrzmail.oto.modules;

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

public class YearPicker_Dialog extends DialogFragment {

    private TimePicker_Dialog.OnClickDialog onClickDialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Calendar cal = Calendar.getInstance();
        View dialog = inflater.inflate(R.layout.dialog_year_picker, null);
        final NumberPicker npYears = dialog.findViewById(R.id.np_years);

        int year = cal.get(Calendar.YEAR);
        npYears.setMinValue(1990);
        npYears.setMaxValue(2021);
        npYears.setValue(year);

        alertBuilder.setView(dialog).setPositiveButton(Html.fromHtml("<font color='#FF4081'>OK</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onClickDialog != null) {
                    onClickDialog.getYear(npYears.getValue());
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

    public YearPicker_Dialog getYears(TimePicker_Dialog.OnClickDialog onClickDialog) {
        this.onClickDialog = onClickDialog;
        return this;
    }
}
