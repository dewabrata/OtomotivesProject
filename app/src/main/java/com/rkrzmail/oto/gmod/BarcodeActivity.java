package com.rkrzmail.oto.gmod;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class BarcodeActivity extends Activity {


	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(new FrameLayout(this));
		IntentIntegrator intentIntegrator = new IntentIntegrator(this);
		intentIntegrator.initiateScan(); // `this` is the current Activity
		int a= 0;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent = new Intent();
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null) {
			if(result.getContents() == null) {
				//Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
				intent.putExtra("TEXT", "");
				intent.putExtra("FORMAT", "");
				intent.putExtra("STATUS", "CANCEL");
			} else {
				//Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
				intent.putExtra("TEXT", result.getContents());
				intent.putExtra("FORMAT", result.getFormatName());
				intent.putExtra("STATUS", "SCAN");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
			intent.putExtra("TEXT", "");
			intent.putExtra("FORMAT", "");
			intent.putExtra("STATUS", "CANCEL");
		}
		intent.putExtra("ORIGIN", "BARCODE");
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}