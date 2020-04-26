package com.naa.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;



import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtil extends AsyncTask<Void, Void, Bitmap> {

	public interface ImageLoadingListener {
		void onLoadingStarted(String var1, View var2);

		void onLoadingFailed(String var1, View var2, String var3);

		void onLoadingComplete(String var1, View var2, Bitmap var3);

		void onLoadingCancelled(String var1, View var2);
	}

	public static void rkrzmaiImageA(ImageView view, String url, ImageLoadingListener listener) {
		String file =  (Utility.getCacheDir("imageui"+Utility.MD5(url)));
		if ( new File(file).exists()) {
			if (listener!=null){
				listener.onLoadingStarted(url, view);
			}
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds=true;
				BitmapFactory.decodeFile(file, options);
				int density  = (int) view.getResources().getDisplayMetrics().density;
				int scale = options.outWidth/view.getResources().getDisplayMetrics().widthPixels / density;

				options = new BitmapFactory.Options();
				options.inSampleSize=scale;
				Bitmap myBitmap = BitmapFactory.decodeFile(file,  options);
				view.setTag(file);
				view.setImageBitmap(myBitmap);
				if (listener!=null && myBitmap!=null){
					listener.onLoadingComplete(url, view, myBitmap);
				}
			} catch (Exception e) {
				if (listener!=null){
					listener.onLoadingCancelled(url, view);
				}
				try {
					new File(file).delete();
				}catch  (Exception w) {}
			}
			return;
		}
		new ImageUtil(url, file, view, listener).execute();

	}
	private String url;
	private String cname;
	ImageLoadingListener imageLoadingListener;
	private ImageView imageView;

	public ImageUtil(String url, String bufferfile, ImageView imageView, ImageLoadingListener listener) {
		this.url = url;
		this.cname =  bufferfile;
		this.imageView = imageView;
		this.imageLoadingListener=listener;
		if (imageLoadingListener!=null){
			imageLoadingListener.onLoadingStarted(url, imageView);
		}
	}
	@Override
	protected Bitmap doInBackground(Void... params) {
		try {
			URL urlConnection = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlConnection
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			if (cname!=null) {
				Utility.copyFile(connection.getInputStream(), cname);
				InputStream input = connection.getInputStream();

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds=true;
				BitmapFactory.decodeFile(cname, options);
				int scale=options.outWidth/imageView.getResources().getDisplayMetrics().widthPixels;

				options = new BitmapFactory.Options();
				options.inSampleSize=scale;
				Bitmap myBitmap = BitmapFactory.decodeFile(cname,  options);
				return myBitmap;
			}else{
				InputStream input = connection.getInputStream();
				Bitmap myBitmap = BitmapFactory.decodeStream(input);

				return myBitmap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		imageView.setImageBitmap(result);
		imageView.setTag(this.cname);
		if (result instanceof Bitmap){
			if (imageLoadingListener!=null){
				imageLoadingListener.onLoadingComplete(url, imageView, result);
			}
		}else{
			if (imageLoadingListener!=null){
				imageLoadingListener.onLoadingCancelled(url, imageView);
			}
		}
	}

	public static void displayImage(ImageView view, String path, ImageLoadingListener listener) {
		rkrzmaiImageA(view, path, listener);
		/*ImageLoader loader = ImageLoader.getInstance();
		try {
			loader.displayImage(path, view, DEFAULT_DISPLAY_IMAGE_OPTIONS, listener);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			loader.clearMemoryCache();
		}*/
	}
	

	

}
