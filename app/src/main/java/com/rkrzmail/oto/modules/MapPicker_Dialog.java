package com.rkrzmail.oto.modules;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;

import java.util.Calendar;
import java.util.Objects;

import im.delight.android.location.SimpleLocation;

public class MapPicker_Dialog extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap map;
    private Button btnGetLocation;
    private SupportMapFragment mapFragment;
    private String getLatitude = "", getLongitude = "";
    private AppActivity activity;

    private GetLocation getLocation;
    private LocationManager locationManager;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private boolean isFisrtTime = false;

    public MapPicker_Dialog() {
    }

    public interface GetLocation {
        void getLatLong(String latitude, String longitude);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_location_peta_picker, container, false);
        activity = ((AppActivity) getActivity());
        assert activity != null;
        locationManager = (LocationManager) activity.getActivity().getSystemService(Context.LOCATION_SERVICE);

        assert getFragmentManager() != null;
        mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        btnGetLocation = rootView.findViewById(R.id.btn_get_location);
        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeLocation();
            }
        });

        if (mapFragment == null) {
            Toast.makeText(getActivity(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = Objects.requireNonNull(getDialog().getWindow()).getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(params);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setPadding(10, 180, 10, 10);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);

        Location location = new Location(LocationManager.GPS_PROVIDER);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void takeLocation() {
        SimpleLocation location = new SimpleLocation(activity.getActivity(), false, false, 6000);
        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(activity.getActivity());
        }
        final double latitude = location.getLatitude();
        final double longtitude = location.getLongitude();

        getLatitude = String.valueOf(latitude);
        getLongitude = String.valueOf(longtitude);
        if (getLocation != null) {
            getLocation.getLatLong(getLatitude, getLongitude);
        }

        LatLng current = new LatLng(latitude, longtitude);
        map.addMarker(new MarkerOptions()
                .position(current)
                .title("Current"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 10));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        Log.d("latitude", String.valueOf(latitude));
        Log.d("longtitude", String.valueOf(longtitude));
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mapFragment)
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .commit();
    }

    public void getBengkelLocation(GetLocation getLocation) {
        this.getLocation = getLocation;
    }
}
