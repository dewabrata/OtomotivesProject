package com.rkrzmail.oto.modules;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;

import java.util.Map;

public class LoginActivity extends AppActivity {
    protected void onCreate(  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);

        find(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login (){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            String sResult;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("user",  find(R.id.user,EditText.class).getText().toString());
                args.put("password",   find(R.id.password,EditText.class).getText().toString());
                sResult =  (InternetX.postHttpConnection(AppApplication.getBaseUrlV3("login"),args)) ;
            }

            @Override
            public void runUI() {
                Nson nson = Nson.readNson(sResult);//test1
                if (nson.get("status").asString().equalsIgnoreCase("OK")){
                    nson = nson.get("data").get(0);
                    setSetting("L","L");

                    setSetting("result",nson.toJson());
                    setSetting("CID",nson.get("CID").asString());
                    setSetting("NAMA",nson.get("NAMA").asString());
                    setSetting("session",nson.get("token").asString());
                    setSetting("user",find(R.id.user,EditText.class).getText().toString());



                    Intent intent = new Intent(getActivity(), MenuActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    showError(nson.get("error").asString());
                }
            }
        });
    }
}
