package com.example.carmovement.vehicletracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.carmovement.app.VolleyHandler;
import com.example.carmovement.app.VolleyResponseListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, VolleyResponseListener {
    private GoogleMap mMap;
    private TimerTask NoticeTimerTask;
    private final Handler handler = new Handler();
    Timer timer = new Timer();
    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 10000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    Runnable myRunnable;
    LatLng fused_latLng2;
    LatLng sydney;
    boolean forward = false;
    Marker mMarker;
    boolean isMarkerRotating;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    LocationSettingsRequest.Builder builder;
    String mLastUpdateTime;
    ArrayList<HashMap<String, Double>> al_co_ordinates;
    int index = 0, start_flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        al_co_ordinates = new ArrayList<HashMap<String, Double>>();

        PowerManager mgr = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        sydney = new LatLng(22.573560, 88.428914);
        mMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Kolkata").icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle)).anchor(0.5f, 0.5f));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18));
        filldata();
//        filldataLive();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
        setGps();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        float t = location.getAccuracy();
        float u = location.getBearing();
        Log.d(TAG, "Firing onLocationChanged...." + t + u);
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_SHORT).show();
//        makeAni();
//        updateUI();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            fused_latLng2 = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            Toast.makeText(this, "Lat: " + lat + "lon: " + lng, Toast.LENGTH_LONG).show();
            System.out.println(lat + lng);
//            tvLocation.setText("At Time: " + mLastUpdateTime + "\n" +
//                    "Latitude: " + lat + "\n" +
//                    "Longitude: " + lng + "\n" +
//                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
//                    "Provider: " + mCurrentLocation.getProvider());
        } else {
            Log.d(TAG, "location is null ...............");
        }
//        LatLng sydney = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//        mMarker=mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Kolkata").icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,18));
//        makeAni();
//        rotateMarker(mMarker);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        timer.cancel();
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }


    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private void setGps() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;

                    case LocationSettingsStatusCodes.CANCELED:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
//        if(timer!=null)
//            timer.schedule(NoticeTimerTask, 0, 7000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult()", Integer.toString(resultCode));

        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        Toast.makeText(MapsActivity.this, "Location enabled by user!", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(MapsActivity.this, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    private void rotateMarker(final Marker marker, LatLng startPos, LatLng stopPos) {
        final double toRotation = bearingBetweenLocations(startPos, stopPos);
        DecimalFormat decimalFormat = new DecimalFormat("#");
        System.out.println(decimalFormat.format(toRotation));

        if (!isMarkerRotating) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 2000;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);

                    float rot = t * (float) toRotation + (1 - t) * startRotation;

                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;
                    }
                }
            });
        }
    }

    private void filldata() {
        HashMap<String, Double> map;

        map = new HashMap<>();
        map.put("lat", 22.573479);
        map.put("lon", 88.429080);
        al_co_ordinates.add(map);

        map = new HashMap<>();
        map.put("lat", 22.573298);
        map.put("lon", 88.429490);
        al_co_ordinates.add(map);

        map = new HashMap<>();
        map.put("lat", 22.573110);
        map.put("lon", 88.429911);
        al_co_ordinates.add(map);

        map = new HashMap<>();
        map.put("lat", 22.573442);
        map.put("lon", 88.430113);
        al_co_ordinates.add(map);

        map = new HashMap<>();
        map.put("lat", 22.573746);
        map.put("lon", 88.430300);
        al_co_ordinates.add(map);
        forward = true;
        timer = new Timer();

        NoticeTimerTask = new TimerTask() {

            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {

                    public void run() {
//                        Toast.makeText(MapsActivity.this,"al",Toast.LENGTH_LONG).show();
                        makeAni2(new LatLng(al_co_ordinates.get(index).get("lat"), al_co_ordinates.get(index).get("lon")));
                        if (forward) {
                            index++;
                            Log.d("Uber..... ", index + "");
                        } else {
                            index--;
                            Log.d("Uber..... ", index + "");
                            if (index < 0) {
                                forward = true;
                                index = 0;
                                Log.d("Uber..... ", index + "");
                            }
                        }
                        if (index == al_co_ordinates.size() && forward) {
                            index--;
                            Log.d("Uber..... ", index + "");
                            forward = false;
                        } else {
//                            forward=true;
                        }
                    }
                });
            }
        };
        timer.schedule(NoticeTimerTask, 0, 5000); //
    }

    private void makeAni2(final LatLng latlngg) {
        final LatLng startPosition = mMarker.getPosition();
        final LatLng finalPosition = latlngg;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 4000;
        final boolean hideMarker = false;
        rotateMarker(mMarker, startPosition, finalPosition);

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                        startPosition.longitude * (1 - t) + finalPosition.longitude * t);

                mMarker.setPosition(currentPosition);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 18));
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        mMarker.setVisible(false);
                    } else {
                        mMarker.setVisible(true);
                    }
                }
            }
        });
    }


    private void filldataLive() {
        final String mUrl = "";
        HashMap<String, Double> map;

        map = new HashMap<>();
        map.put("lat", 22.573479);
        map.put("lon", 88.429080);

        al_co_ordinates.add(map);

        map = new HashMap<>();
        map.put("lat", 22.573298);
        map.put("lon", 88.429490);
        al_co_ordinates.add(map);
        map = new HashMap<>();
        map.put("lat", 22.573110);
        map.put("lon", 88.429911);
        al_co_ordinates.add(map);

        map = new HashMap<>();
        map.put("lat", 22.573442);
        map.put("lon", 88.430113);
        al_co_ordinates.add(map);


        map = new HashMap<>();
        map.put("lat", 22.573746);
        map.put("lon", 88.430300);
        al_co_ordinates.add(map);
        forward = true;
        start_flag = 0;
        timer = new Timer();

        NoticeTimerTask = new TimerTask() {

            public void run() {

                handler.post(new Runnable() {

                    public void run() {
                        new VolleyHandler(MapsActivity.this, mUrl, Request.Method.GET, null, "push").makeStringReq();
                    }
                });
            }
        };
        timer.schedule(NoticeTimerTask, 0, 5000); //
    }

    @Override
    public String onSuccess(String response, String type) {
        String lat = "";
        String lng = "";

//        {"type":"push","lat":"22.5734699","lng":"88.4316598"}
        //parse json response
        try {
            JSONObject jObj = new JSONObject(response);
            lat = jObj.optString("lat");
            lng = jObj.optString("lng");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String resp = response;
        System.out.print(resp);

        if (start_flag == 0) {
            sydney = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            mMarker = mMap.addMarker(new MarkerOptions().position(sydney).icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle)).anchor(0.5f, 0.5f));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18));
            start_flag++;
        } else {
            makeAni2(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
        }
        return null;
    }

    @Override
    public String onFailure(VolleyError error) {
        return null;
    }
}
