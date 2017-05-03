package de.pathec.hubapp.util;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.events.AddGeofenceEvent;
import de.pathec.hubapp.events.RemoveGeofenceEvent;
import de.pathec.hubapp.geofencing.GeofenceTransitionService;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;

public class AndroidServiceStartOnBoot extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private HubList mHubList;
    private Context mContext;

    private static final int GEOFENCE_REQ_CODE = 35;

    private static final float GEOFENCE_RADIUS = 150.0f; // in meters

    private PendingIntent mGeofencePendingIntent;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(Params.LOGGING_TAG, "Create geofence pending intent");
        if ( mGeofencePendingIntent != null )
            return mGeofencePendingIntent;

        Intent intent = new Intent( this, GeofenceTransitionService.class);
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private GoogleApiClient mGoogleApiClient;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Util.addProtocol(this, new ProtocolItem(this, ProtocolType.INFO, "Service start on boot is running.", "System"));

        mContext = this;

        mHubList = new HubList(this);

        createGoogleApi();

        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGoogleApiClient.disconnect();
    }


    private void createGoogleApi() {
        Log.d(Params.LOGGING_TAG, "Create GoogleApi()");
        if (mGoogleApiClient == null ) {
            mGoogleApiClient  = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }

    public void setUpGeofence() {
        SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
        Boolean isGeofencingEnabled = settings.getBoolean(Params.PREFS_HUB_GEOFENCING_ENABLED, false);
        if (isGeofencingEnabled) {
            ArrayList<HubItem> hubList = mHubList.getHubList();

            // 1. Remove all geofence
            removeGeofence();

            // 2. Add all geofence
            ArrayList<Geofence> geofenceList = new ArrayList<>();
            for (HubItem hubItem : hubList) {
                // Check location information
                if (hubItem.getLongitude() != 0.0 && hubItem.getLatitude() != 0.0) {
                    Geofence geofence = createGeofence(new LatLng(hubItem.getLatitude(), hubItem.getLongitude()), GEOFENCE_RADIUS, hubItem.getTitle());
                    geofenceList.add(geofence);
                }
            }

            if (geofenceList.size() > 0) {
                GeofencingRequest geofencingRequest = createGeofenceRequest(geofenceList);
                addGeofenceList(geofencingRequest);
            }
        }
    }

    public void removeGeofence() {
        ArrayList<HubItem> hubList = mHubList.getHubList();
        ArrayList<String> hubTitleList = new ArrayList<>();
        for (HubItem hubItem : hubList) {
            hubTitleList.add(hubItem.getTitle());
        }

        removeGeofenceList(hubTitleList);
    }

    private Geofence createGeofence(LatLng latLng, float radius, String hubTitle) {
        Log.d(Params.LOGGING_TAG, "Create geofence");
        return new Geofence.Builder()
                .setRequestId(hubTitle)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( Geofence.NEVER_EXPIRE )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    private GeofencingRequest createGeofenceRequest(List<Geofence> geofenceList ) {
        Log.d(Params.LOGGING_TAG, "Create geofence request");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofences(geofenceList)
                .build();
    }

    private void addGeofenceList(GeofencingRequest request) {
        Log.d(Params.LOGGING_TAG, "Add geofence list");
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mGeofencePendingIntent = createGeofencePendingIntent();

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                request,
                mGeofencePendingIntent
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.i(Params.LOGGING_TAG, "Add geofence list: " + status);
                if ( status.isSuccess() ) {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.INFO, "Update geofence list successfully.", "GeoFencing"));

                    BusProvider.postOnMain(new AddGeofenceEvent(true));
                } else {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Update geofence list failed.", "GeoFencing"));

                    BusProvider.postOnMain(new AddGeofenceEvent(false));
                }
            }
        });
    }

    private void removeGeofenceList(List<String> geofenceRequestIds) {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                geofenceRequestIds
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.i(Params.LOGGING_TAG, "Remove geofence list: " + status);
                if ( status.isSuccess() ) {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.INFO, "Remove geofence list successfully.", "GeoFencing"));

                    BusProvider.postOnMain(new RemoveGeofenceEvent(true));
                } else {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Remove geofence list failed.", "GeoFencing"));

                    BusProvider.postOnMain(new RemoveGeofenceEvent(false));
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Params.LOGGING_TAG, "onConnected()");

        setUpGeofence();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(Params.LOGGING_TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(Params.LOGGING_TAG, "onConnectionFailed()");
    }

}