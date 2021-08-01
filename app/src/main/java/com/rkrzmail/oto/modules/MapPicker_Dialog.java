package com.rkrzmail.oto.modules;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

import java.util.Objects;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapPicker_Dialog extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap map;
    private Button btnGetLocation;
    private SupportMapFragment mapFragment;
    private String getLatitude = "", getLongitude = "";
    private AppActivity activity;
    private EditText etSearch;
    private ImageView imgSelectLocation;
    private RecyclerView rvPlaces;

    private GetLocation getLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng locationSelected;

    // private PlacesAutoCompleteAdapter placesAutoCompleteAdapter;

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

    int REQUEST_CODE_AUTOCOMPLETE = 8789;

    @SuppressLint({"MissingPermission", "ClickableViewAccessibility"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_location_peta_picker, container, false);
        activity = ((AppActivity) getActivity());

        assert getFragmentManager() != null;
        mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        btnGetLocation = rootView.findViewById(R.id.btn_get_location);
        etSearch = rootView.findViewById(R.id.et_search);
        imgSelectLocation = rootView.findViewById(R.id.img_select_location_manualy);
        rvPlaces = rootView.findViewById(R.id.rv_places);

        //initAutoCompletePalaces();

        imgSelectLocation.setVisibility(View.GONE);
        //etSearch.addTextChangedListener(filterTextWatcher);
        etSearch.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return true;
            }
        });

        imgSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeLocation();
            }
        });
        mapFragment.getMapAsync(this);
        //initAutoCompletePalaces();
        return rootView;
    }

/*
    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals("")) {
                placesAutoCompleteAdapter.getFilter().filter(s.toString());
                if (rvPlaces.getVisibility() == View.GONE) {
                    rvPlaces.setVisibility(View.VISIBLE);
                }
            } else {
                if (rvPlaces.getVisibility() == View.VISIBLE) {
                    rvPlaces.setVisibility(View.GONE);
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };


    private void initAutoCompletePalaces() {
        Places.initialize(Objects.requireNonNull(getContext()), getResources().getString(R.string.GoogleMapsApi));
        placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(getContext());
        rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaces.setAdapter(placesAutoCompleteAdapter);
        Objects.requireNonNull(rvPlaces.getAdapter()).notifyDataSetChanged();

        placesAutoCompleteAdapter.setClickListener(new PlacesAutoCompleteAdapter.ClickListener() {
            @Override
            public void click(Place place) {
                locationSelected = place.getLatLng();
                map.clear();
                map.addMarker(new MarkerOptions()
                        .title("Lokasi Anda")
                        .position(locationSelected)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            }
        });
    }
*/

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
        if (checkPermission()) {
            map = googleMap;
            map.setPadding(10, 180, 10, 10);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomGesturesEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(false);
            //map.getUiSettings().setZoomControlsEnabled(true);
            map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                LatLng latLng;

                @Override
                public void onMarkerDragStart(Marker marker) {
                    latLng = marker.getPosition();
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    map.clear();
                    latLng = marker.getPosition();
               /* map.addMarker(new MarkerOptions()
                        .title("Lokasi Anda")
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));*/
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                }
            });

            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    locationSelected = new LatLng(location.getLatitude(), location.getLongitude());
                    map.clear();
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationSelected, 16));
                    map.addMarker(new MarkerOptions()
                            .title("Lokasi Anda")
                            .position(locationSelected)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            };


            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationSelected = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            map.clear();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationSelected, 16));
            map.addMarker(new MarkerOptions()
                    .title("Lokasi Anda")
                    .position(locationSelected)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            requestPermission();
        }
    }


    private void takeLocation() {
        final double latitude = locationSelected.latitude;
        final double longtitude = locationSelected.longitude;

        getLatitude = String.valueOf(latitude);
        getLongitude = String.valueOf(longtitude);
        if (getLocation != null) {
            getLocation.getLatLong(getLatitude, getLongitude);
        }

        dismiss();
    }

    int REQUEST_LOCATION = 8765;

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                if (checkPermission()) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                } else {
                    activity.showWarning("Anda Harus Mengijinkan Akses Lokasi!");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mapFragment)
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .remove((Fragment) mapFragment)
                    .commit();
    }

    public void getBengkelLocation(GetLocation getLocation) {
        this.getLocation = getLocation;
    }
}
