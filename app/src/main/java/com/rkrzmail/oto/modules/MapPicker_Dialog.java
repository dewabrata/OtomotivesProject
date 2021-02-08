package com.rkrzmail.oto.modules;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rkrzmail.oto.R;

import java.util.Calendar;

import im.delight.android.location.SimpleLocation;

public class MapPicker_Dialog extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap map;
    private Button btnSave;
    private SupportMapFragment mapFragment;
    private String lat,lon;


    public MapPicker_Dialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_location_peta_picker, container, false);

        mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            Toast.makeText(getActivity(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }
        mapFragment.getMapAsync(this);

        return rootView;
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
    }

    private void takeLocation() {
        //location
        SimpleLocation location = new SimpleLocation(getActivity(), false, false, 6000);
        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(getActivity());
        }
        final double latitude = location.getLatitude();
        final double longtitude = location.getLongitude();
        lat = String.valueOf(latitude);
        lon = String.valueOf(longtitude);
        LatLng current = new LatLng(latitude, longtitude);
        map.addMarker(new MarkerOptions()
                .position(current)
                .title("Current"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 10));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        Log.d("latitude", String.valueOf(latitude));
        Log.d("longtitude", String.valueOf(longtitude));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mapFragment)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .commit();
    }

}
