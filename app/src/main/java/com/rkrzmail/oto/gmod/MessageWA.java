package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.NikitaRecyclerAdapter;
import com.rkrzmail.oto.modules.Adapter.NikitaViewHolder;

import java.util.Map;

public class MessageWA extends AppActivity {
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("MESSAGE WA");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);






        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.activity_message_item){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtPhone, TextView.class).setText(nListArray.get(position).get("PHONE").asString());
                viewHolder.find(R.id.txtMessage, TextView.class).setText(nListArray.get(position).get("MESSAGE").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                try {
                    // http://exact.co.id/data/wapupdel.php?track=34601000000091
                    String toNumber = parent.get(position).get("PHONE").asString().trim();
                    String text = parent.get(position).get("MESSAGE").asString();
                    if (toNumber.startsWith("0")){
                        toNumber = "62" + toNumber.substring(1);
                    }else if (toNumber.startsWith("+")){
                        toNumber = toNumber.substring(1);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber + "&text=" + Utility.urlEncode(text)));
                    startActivityForResult(intent, REQUEST_WA);
                } catch (Exception e) {
                    showInfoDialog("Whatsapp dihandphone tidak terinstall", null);
                }
            }
        }));


        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("custid", UtilityAndroid.getSetting(getApplicationContext(),"CID",""));
                args.put("email", UtilityAndroid.getSetting(getApplicationContext(),"EMA",""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("message.php"),args)) ;

            }

            @Override
            public void runUI() {
                if (result.isNsonArray()) {
                    nListArray.asArray().addAll(result.asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }
    final int REQUEST_WA = 12;
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WA && resultCode == RESULT_OK){
                long e = System.currentTimeMillis();
                long s = getIntent().getLongExtra("time", 0);
                if (Math.abs(e-s)>=3000){

                }
        }
    }
}
