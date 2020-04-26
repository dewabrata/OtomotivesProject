package com.rkrzmail.oto.gmod;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

import java.util.List;

public class Stock_OpnameActivity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock__opname);

        Nson nson = Nson.newArray().add("HILANG").add("RUSAK").add("PINDAH FOLDER").add("LAINNYA");
        ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, nson.asArray()){
            public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                View v =  super.getView(position, convertView, parent);
                /*if (v instanceof TextView){
                    ((TextView) v).setText( ));
                }*/
                return v;
            }
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v =  super.getView(position, convertView, parent);
                /*if (v instanceof TextView){
                    ((TextView) v).setText(result.get(position).get("KELURAHAN").asString());
                }*/
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner prov =  findViewById(R.id.spnSebab);
        prov.setAdapter(adapter);

    }
}
