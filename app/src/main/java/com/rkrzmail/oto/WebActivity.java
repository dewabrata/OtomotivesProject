package com.rkrzmail.oto;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.naa.data.Nson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class WebActivity extends AppActivity{
    private View view;
    private Bitmap bitBitmap = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView myWebView;
    private boolean perror = false;
    private ProgressBar progressBar ;
    public Uri imageUri;
    private static final int FILECHOOSER_RESULTCODE   = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;


    private static final int   REQUEST_BARCODE = 12;
    private static final int   REQUEST_CAMERA = 13;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* if (getIntentStringExtra("title").equalsIgnoreCase("")){
            // remove title
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        }*/

        setTitle(getIntentStringExtra("title"));
        setContentView(R.layout.web);


        view = findViewById(R.id.mparent);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        progressBar = (ProgressBar) view.findViewById(R.id.pPro);

        /*ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
        setTitle(getIntentStringExtra("title"));*/
        //setTitleUp();


        /*((SwipeRefreshLayoutX)swipeRefreshLayout).setCanChildScrollUpCallback(new SwipeRefreshLayoutX.CanChildScrollUpCallback() {
            @Override
            public boolean canSwipeRefreshChildScrollUp() {
                //true == > swipeRefreshLayout.setEnabled(false);
                //false == > swipeRefreshLayout.setEnabled(true);
                return myWebView.getScrollY() > 0;
            }
        });*/
        myWebView = (WebView) view.findViewById(R.id.web);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Nikita");
        myWebView.setWebViewClient(new AppWebViewClients(swipeRefreshLayout));

        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                }else{
                    progressBar.setProgress(newProgress);
                }

            }
            /*public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // make sure there is no existing message
                if (myActivity.uploadMessage != null) {
                    myActivity.uploadMessage.onReceiveValue(null);
                    myActivity.uploadMessage = null;
                }

                myActivity.uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    myActivity.startActivityForResult(intent, MyActivity.REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    myActivity.uploadMessage = null;
                    Toast.makeText(myActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                    return false;
                }

                return true;
            }*/

            // Android 5.0.1
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {


                if (Build.VERSION.SDK_INT >= 21) {
                    String[] acceptTypes = fileChooserParams.getAcceptTypes();

                    String acceptType = "";
                    for (int i = 0; i < acceptTypes.length; ++ i) {
                        if (acceptTypes[i] != null && acceptTypes[i].length() != 0)
                            acceptType += acceptTypes[i] + ";";
                    }
                    if (acceptType.length() == 0)
                        acceptType = "*/*";

                    final ValueCallback<Uri[]> finalFilePathCallback = filePathCallback;

                    ValueCallback<Uri> vc = new ValueCallback<Uri>() {

                        @Override
                        public void onReceiveValue(Uri value) {

                            Uri[] result;
                            if (value != null)
                                result = new Uri[]{value};
                            else
                                result = null;

                            finalFilePathCallback.onReceiveValue(result);

                        }
                    };

                    openFileChooser(vc, acceptType, "filesystem");


                    return true;
                }else{
                    return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
                }

            }

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){
                // Update message
                mUploadMessage = uploadMsg;
                try{
                    // Create AndroidExampleFolder at sdcard
                    File imageStorageDir = new File( Environment.getExternalStoragePublicDirectory(  Environment.DIRECTORY_PICTURES) , "AndroidExampleFolder");
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                    // Create camera captured image file path and name
                    File file = new File(imageStorageDir + File.separator + "IMG_"   + String.valueOf(System.currentTimeMillis())  + ".jpg");
                    mCapturedImageURI = Uri.fromFile(file);
                    // Camera capture image intent
                    final Intent captureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    // Create file chooser intent
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                    // Set camera intent to file chooser
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS , new Parcelable[] { captureIntent });
                    // On select image call onActivityResult method of activity
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                }
                catch(Exception e){
                    Toast.makeText(getBaseContext(), "Exception:"+e,Toast.LENGTH_LONG).show();
                }

            }
            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }
            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
            public boolean onConsoleMessage(ConsoleMessage cm) {
                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                //Log.d("androidruntime", "Show console messages, Used for debugging: " + message);
            }
            public void onPermissionRequest(final PermissionRequest request) {
                int t=0;
                if (Build.VERSION.SDK_INT >= 21) {
                    request.grant(request.getResources());
                } else {

                }
                //L.d("onPermissionRequest");
                //request.grant(request.getResources());
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()  {
                //swipeRefreshLayout.setRefreshing(false);
                myWebView.reload();
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setEnabled(false);
        myWebView.loadUrl(getIntentStringExtra("url"));//getIntentStringExtra("url")
       /*
        swipeRefreshLayout.setEnabled(false);
        Messagebox.showProsesBar(WebActivity.this, new Messagebox.DoubleRunnable() {
            String result;
            public void run() {
                Hashtable hashtable= new Hashtable();
                hashtable.put("xuserid", UtilityAndroid.getSetting(WebActivity.this, "ID_USER", ""));
                hashtable.put("xtoken", UtilityAndroid.getSetting(WebActivity.this, "TOKEN", ""));
                hashtable.put("ytoken", UtilityAndroid.getSetting(WebActivity.this, "TOKEN", ""));
                hashtable.put("yuserid", UtilityAndroid.getSetting(WebActivity.this, "ID_USER", ""));
                String link = UtilityAndroid.getSetting(WebActivity.this, "URL", "")+"SE/myexmob.excheckacid?";

                result = UtilityAndroid.postHttpConnection(link, hashtable);
            }
            public void runUI() {
                myWebView.clearCache(true);
                Nson nson = Nson.readNson(result);
                progressBar.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setEnabled(true);
                myWebView.loadUrl("https://exact.co.id/mywallet/#!/?acid=" + nson.getData("ACID").asString());
                Log.i("ACID", nson.getData("ACID").asString());
                myWebView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (myWebView.getScrollY() == 0) {
                            swipeRefreshLayout.setEnabled(true);
                        } else {
                            swipeRefreshLayout.setEnabled(false);
                        }
                    }

                });
            }
        });*/


    }


    public void onBackPressed() {
        if (myWebView.canGoBack()){
            myWebView.goBack();
        }else{
            super.onBackPressed();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BARCODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String textBarcode = data.getStringExtra("TEXT");
                //myWebView.evaluateJavascript("javascript:onbarcode('"+ Utility.escapeSQL(textBarcode)+"');", null);
                String js = "javascript:onbarcode('"+  (textBarcode)+ "')";
                if (Build.VERSION.SDK_INT >= 19) {
                    myWebView.evaluateJavascript(js, null);
                } else {
                    myWebView.loadUrl(js);
                }
            }
        }
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            File photo = new File(Environment.getExternalStorageDirectory(), "image");
            String fname =   Long.toHexString(System.currentTimeMillis()) +"." + Long.toHexString(System.nanoTime()) +"." + System.currentTimeMillis() +".tmp";
            // Utility.copyFile( photo.getAbsolutePath(), Utility.getDefaultTempPath(fname) );

            //camera
            // onCompressImage( Utility.getDefaultTempPath(fname)  );
            String b64 = "";
            String js = "javascript:onbarcode('"+  (b64)+ "')";
            if (Build.VERSION.SDK_INT >= 19) {
                myWebView.evaluateJavascript(js, null);
            } else {
                myWebView.loadUrl(js);
            }
        }


        if(requestCode==FILECHOOSER_RESULTCODE)  {
            if (null == this.mUploadMessage) {
                return;
            }
            Uri result=null;
            try{
                if (resultCode != RESULT_OK) {
                    result = null;
                } else {
                    // retrieve from the private variable if the intent is null
                    result = data == null ? mCapturedImageURI : data.getData();
                }
            }catch(Exception e){
                Toast.makeText(getApplicationContext(), "activity :"+e, Toast.LENGTH_LONG).show();
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
    }
    public static void onCompressImage(String   file){
        String cam = "";
        Nson ncam = Nson.readJson(cam);

        if (!ncam.get("compress").toString().equalsIgnoreCase("true")) {
            return;
        }
        int quality = ncam.containsKey("compress.quality")? ncam.get("compress.quality").asInteger() : 80 ;
        int width =  ncam.containsKey("compress.width")? ncam.get("compress.width").asInteger() : 540 ;
        int maxpx =  ncam.containsKey("compress.maxpx")? ncam.get("compress.maxpx").asInteger() : 540 ;

        String format = ncam.containsKey("compress.format")? ncam.get("compress.format").toString() : "png" ;

        width=width<=10?540:width;
        maxpx=maxpx<=10?540:maxpx;
		/*
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds=true;
			BitmapFactory.decodeFile(file, options);
			int scale=options.outWidth/width;


			options = new BitmapFactory.Options();
			options.inSampleSize=scale;
			Bitmap bmp = BitmapFactory.decodeFile(file,  options);

		    FileOutputStream fos = new FileOutputStream(file);
		    bmp.compress(format.equals("jpg")?Bitmap.CompressFormat.JPEG:Bitmap.CompressFormat.PNG, quality, fos);
		    fos.flush();
			fos.close();
		} catch (Exception e) { }
		*/
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(file, options);
            int scale= options.outWidth / width ;
            if ( ncam.containsKey("compress.maxpx") ) {
                scale=Math.max(options.outWidth, options.outHeight) / maxpx;
            }

            options = new BitmapFactory.Options();
            options.inSampleSize=scale+1;
            Bitmap bmp = BitmapFactory.decodeFile(file,  options);

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(format.equalsIgnoreCase("jpg")?Bitmap.CompressFormat.JPEG:Bitmap.CompressFormat.PNG, quality, fos);

            fos.flush();
            fos.close();
        } catch (Exception e) { }
    }
    public static void rotate (String file, final int move){
        //mmust on other thread
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file);
            Matrix matrix = new Matrix();
            matrix.postRotate(move);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) { }
    }
    private void evaluateJavaScript(String javascript) {
        if (myWebView != null) {
            String js = "javascript:" + javascript;
            if (Build.VERSION.SDK_INT >= 19) {
                myWebView.evaluateJavascript(js, null);
            } else {
                myWebView.loadUrl(js);
            }
        }
    }
    public String getIntentStringExtra(String key) {
        return getIntentStringExtra(getIntent(), key);
    }
    public String getIntentStringExtra(Intent intent, String key) {
        if (intent!=null && intent.getStringExtra(key)!=null){
            return intent.getStringExtra(key);
        }
        return "";
    }
    @Override
    protected void onStart() {
        super.onStart();
        //myWebView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //myWebView.getViewTreeObserver().removeOnScrollChangedListener(this);
    }

    public void onScrollChanged() {
        if (myWebView.getScrollY() == 0) {
            swipeRefreshLayout.setEnabled(true);
        } else {
            swipeRefreshLayout.setEnabled(false);
        }
    }
    public Nson getContacts(Context ctx) {
        Nson  list = Nson.newArray();
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (cursorInfo.moveToNext()) {
                        Nson name = Nson.newObject();
                        name.set("id",id)  ;
                        name.set("name",cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                        name.set("mobileNumber",cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                }
            }
            cursor.close();
        }
        return list;
    }
    public class WebAppInterface {
        Context mContext;


        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }
        @JavascriptInterface
        public String getinfo() {

            Nson nson= new Nson(AppApplication.getInstance().getArgsData());
            return  nson.toJson();

        }

        @JavascriptInterface
        public void logout() {

        }

        @JavascriptInterface
        public String getcontack() {
            return getcontact();
        }

        @JavascriptInterface
        public String getcontact() {
            return getContacts(WebActivity.this).toJson();
        }

        @JavascriptInterface
        public void back() {
            WebActivity.this.onBackPressed();
        }
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
        @JavascriptInterface
        public void loadUrl(String url) {
            myWebView.loadUrl(url);
        }

        @JavascriptInterface
        public void scanbarcode() {

        }
        @JavascriptInterface
        public String getGPS() {
            reqGPSinThisThread();
            return AppApplication.getLastCurrentLocation();
        }
        @JavascriptInterface
        public void showCamera() {
            Log.i("showCamera",Thread.currentThread().getName());
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED||
                    ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED||
                    ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED||
                    ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED||
                    ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(getActivity(), new String[] {android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.ACCESS_FINE_LOCATION ,android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return;
            }
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File photo = new File(Environment.getExternalStorageDirectory(), "image");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            Context context = getActivity().getApplicationContext();
					/*Uri apkURI = FileProvider.getUriForFile( context,
							context.getApplicationContext()
									.getPackageName() + ".provider", photo);
					intent.setDataAndType(apkURI, mime.getMimeTypeFromExtension("img.png"));
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);*/

            getActivity().startActivityForResult(intent, REQUEST_CAMERA);
        }

    }
    private long ob = 0;
    private final int maxTimeOutms = 11000;
    private double gpsLive =0;
    private Location currlocation;
    /* private FusedLocationProviderClient mFusedLocationClient;
     private LocationCallback mLocationCallback;*/
    public static boolean isGPSEnable(Context mContext) {
        boolean isGPSEnabled = false;
        LocationManager locationManager = (LocationManager) mContext
                .getSystemService(mContext.LOCATION_SERVICE);
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPSEnabled;
    }
    public void reqGPS(){
        reqGPS(null);
    }
    public void reqGPS(final Runnable onfinish){
    /*    if (isGPSEnable(getActivity())){
            //wailt for gps
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setExpirationDuration(6000);// 1 menit
            if (mFusedLocationClient == null){
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),"Maaf, permision GPS ditolak",Toast.LENGTH_LONG).show();
                finish();
                return ;
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null) {
                        currlocation = location;
                    }
                }
            });

            gpsLive = System.currentTimeMillis();
            mLocationCallback = new LocationCallback() {
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        if (location!=null){
                            currlocation = location;

                            if (Math.abs(System.currentTimeMillis() - gpsLive) > maxTimeOutms ){
                                if (mLocationCallback!=null && mFusedLocationClient!=null){
                                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                }
                            }

                        }
                    }
                }

                ;
            };
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    mLocationCallback,
                    null *//* Looper *//*);

            new Thread(new Runnable() {
                public void run() {
                    Location location = null;
                    ob = System.currentTimeMillis();
                    while (Math.abs(System.currentTimeMillis() - ob) < maxTimeOutms) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (activity!=null && !activity.isFinishing()){
                                    try {
                                        prb.setMessage("Search Location . . . ("+(int)((maxTimeOutms-Math.abs(System.currentTimeMillis() - ob))/1000)+")");
                                    }catch (Exception e){}
                                }
                            }
                        });
                        try {
                            Thread.sleep(1000);//1 detik
                        } catch (Exception e) {  }
                        location = currlocation;
                        try {
                            if (location != null) {
                                if (location.getAccuracy() <= 50) {
                                    break;
                                }
                            }
                        } catch (Exception e) { }
                    }
                    AppApplication.setLastCurrentLocation(location);
                    if (activity!=null  && !activity.isFinishing()){
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (activity!=null && !activity.isFinishing()){
                                    try {
                                        prb.dismiss();
                                    }catch (Exception e){}
                                    if (onfinish!=null){
                                        onfinish.run();
                                    }
                                }
                            }
                        });
                    }
                }
                Activity activity;
                private ProgressDialog prb;
                public Runnable get(Activity activity, ProgressDialog prb){
                    this.activity = activity;
                    this.prb = prb;
                    return this;
                }
            }.get(getActivity(), MessageMsg.showProgresBar(getActivity(),  "Search Location . . . "))) .start();
        }else{
            Toast.makeText(getActivity(),"GPS belum On, silahakan diaktifkan", Toast.LENGTH_SHORT).show();
            finish();
        }*/
    }
    public void reqGPSinThisThread(){
        /*if (isGPSEnable(getActivity())){
            //wailt for gps
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setExpirationDuration(6000);// 1 menit
            if (mFusedLocationClient == null){
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),"Maaf, permision GPS ditolak",Toast.LENGTH_LONG).show();
                finish();
                return ;
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null) {
                        currlocation = location;
                    }
                }
            });


            gpsLive = System.currentTimeMillis();
            mLocationCallback = new LocationCallback() {
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        if (location!=null){
                            currlocation = location;

                            if (Math.abs(System.currentTimeMillis() - gpsLive) > maxTimeOutms ){
                                if (mLocationCallback!=null && mFusedLocationClient!=null){
                                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                }
                            }

                        }
                    }
                }

                ;
            };
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    mLocationCallback,
                    null *//* Looper *//*);


            final Activity activity = getActivity();
            final ProgressDialog prb = MessageMsg.showProgresBar(getActivity(),  "Search Location . . . ");
            Location location = null;
            ob = System.currentTimeMillis();
            while (Math.abs(System.currentTimeMillis() - ob) < maxTimeOutms) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (activity!=null && !activity.isFinishing()){
                            try {
                                prb.setMessage("Search Location . . . ("+(int)((maxTimeOutms-Math.abs(System.currentTimeMillis() - ob))/1000)+")");
                            }catch (Exception e){}
                        }
                    }
                });
                try {
                    Thread.sleep(1000);//1 detik
                } catch (Exception e) {  }
                location = currlocation;
                try {
                    if (location != null) {
                        if (location.getAccuracy() <= 50) {
                            break;
                        }
                    }
                } catch (Exception e) { }
            }
            AppApplication.setLastCurrentLocation(location);

        }else{
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(),"GPS belum On, silahakan diaktifkan", Toast.LENGTH_SHORT).show();
                }
            });
        }*/
    }
    public class AppWebViewClients extends WebViewClient {
        private SwipeRefreshLayout swipeRefreshLayout;

        public AppWebViewClients(SwipeRefreshLayout swipeRefreshLayout) {
            this.swipeRefreshLayout=swipeRefreshLayout;
            //swipeRefreshLayout.setRefreshing(true);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            view.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            //progressBar.setProgress(0);
            return true;
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.i("Web", description);//net::ERR_INTERNET_DISCONNECTED
            if (description.contains("net::ERR_INTERNET_DISCONNECTED")){
                perror = true;
            }
            perror = true;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            //if (view.getTitle().contains("Web page not available"))//404 Not Found
            Log.i("Web", view.getTitle());
            if (view.getTitle().contains("Web page not available")){
                Toast.makeText(view.getContext(), "Periksa Jaringan Internet anda", Toast.LENGTH_SHORT).show();
                view.setVisibility(View.INVISIBLE);
            }else if (view.getTitle().contains("404 Not Found")) {
                view.setVisibility(View.INVISIBLE);
                Toast.makeText(view.getContext(), "Terjadi Kendala di Server", Toast.LENGTH_SHORT).show();
            }else if (perror){
                view.setVisibility(View.INVISIBLE);

            }else{
                view.setVisibility(View.VISIBLE);
            }
            if (swipeRefreshLayout!=null){
                swipeRefreshLayout.setRefreshing(false);
            }
            perror = false;
        }
    }


}
