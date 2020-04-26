package com.rkrzmail.oto.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.naa.data.ImageUtil;
import com.naa.data.Utility;
import com.rkrzmail.oto.R;

import java.io.File;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class SlideFragment extends Fragment {
	
	private String img;
	private Bitmap bitmap;
	private int drw;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		img = getArguments().getString("image");
//		drw = getArguments().getInt("image");

		
	}
	public void showUi(String title,String page){
		/*Intent intent =  new Intent(getActivity(), WebUI.class);
		intent.putExtra("url", "http://neyama.com/dashboard/dashboard.html");
		intent.putExtra("title", title);
		startActivity(intent);*/
	}
	View busy ;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v=  inflater.inflate(R.layout.slide, container, false);
		if (img.endsWith(".gif")){
			v.findViewById(R.id.barBusy).setVisibility(View.GONE);
			v.findViewById(R.id.imgWelcome).setVisibility(View.GONE);
			v.findViewById(R.id.gif).setVisibility(View.VISIBLE);
			final GifImageView gifView = (GifImageView) v.findViewById(R.id.gif);
			busy =  v.findViewById(R.id.barBusy);
			busy.setVisibility(View.GONE);

			try {
				ImageUtil.rkrzmaiImageA((ImageView) v.findViewById(R.id.imgWelcome), img, new ImageUtil.ImageLoadingListener() {
					public void onLoadingStarted(String s, View view) {
						if (busy!=null){
							busy.setVisibility(View.VISIBLE);
						}
					}
					public void onLoadingFailed(String s, View view, String failReason) {
						if (busy!=null){
							busy.setVisibility(View.GONE);
						}
					}
					public void onLoadingComplete(String s, View view, Bitmap bitmap) {
						if (busy!=null){
							busy.setVisibility(View.GONE);
							String file =  (Utility.getCacheDir("imageui"+Utility.MD5(img)));
							try {
								//gifView.setGifResource(new FileInputStream(file));
								File gifFile = new File(file);
								GifDrawable gifDrawable = new GifDrawable(gifFile);
								gifView.setImageDrawable(gifDrawable);
							}catch (Exception e){}

						}
					}
					public void onLoadingCancelled(String s, View view) {
						if (busy!=null){
							busy.setVisibility(View.GONE);
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				if (busy!=null){
					busy.setVisibility(View.GONE);
				}
			}


			/*
			busy =  v.findViewById(R.id.barBusy);
			v.findViewById(R.id.imgWelcome).setVisibility(View.GONE);
			v.findViewById(R.id.web).setVisibility(View.VISIBLE);

			WebView myWebView = (WebView) v.findViewById(R.id.web);
			WebSettings webSettings = myWebView.getSettings();
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
			webSettings.setJavaScriptEnabled(true);
			webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
			myWebView.setWebChromeClient(new WebChromeClient(){
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					if (newProgress == 100){
						busy.setVisibility(View.GONE);
					}else{
						busy.setVisibility(View.VISIBLE);
					}

				}
			});
			myWebView.loadUrl(img);*/
		}else{
			busy =  v.findViewById(R.id.barBusy);
			busy.setVisibility(View.GONE);
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showUi("Promo", "");
				}
			});
			if (Utility.isNumeric(img)){
				((ImageView) v.findViewById(R.id.imgWelcome)).setImageResource(Utility.getInt(img));
				return v ;
			}
			try {
				ImageUtil.rkrzmaiImageA((ImageView) v.findViewById(R.id.imgWelcome), img, new ImageUtil.ImageLoadingListener() {
					public void onLoadingStarted(String s, View view) {
						if (busy!=null){
							busy.setVisibility(View.VISIBLE);
						}
					}
					public void onLoadingFailed(String s, View view, String failReason) {
						if (busy!=null){
							busy.setVisibility(View.GONE);
						}
					}
					public void onLoadingComplete(String s, View view, Bitmap bitmap) {
						if (busy!=null){
							busy.setVisibility(View.GONE);
						}
					}
					public void onLoadingCancelled(String s, View view) {
						if (busy!=null){
							busy.setVisibility(View.GONE);
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				if (busy!=null){
					busy.setVisibility(View.GONE);
				}
			}
		}
		return v;
	}

}
