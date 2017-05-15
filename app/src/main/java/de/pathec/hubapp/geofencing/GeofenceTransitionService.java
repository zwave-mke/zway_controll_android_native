package de.pathec.hubapp.geofencing;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import de.fh_zwickau.informatik.sensor.model.devices.DeviceCommand;
import de.pathec.hubapp.MainActivity;
import de.pathec.hubapp.R;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;

public class GeofenceTransitionService extends IntentService {
    public GeofenceTransitionService() {
        super(Params.LOGGING_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);

        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Util.addProtocol(this, new ProtocolItem(this, ProtocolType.WARNING, "Error during geofencing event: " + errorMsg, "GeoFencing"));
            Log.e( Params.LOGGING_TAG, errorMsg );
            return;
        }

        // Retrieve GeofenceTransition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {

            // Get the geofence that were triggered
            List<Geofence> triggeringGeofenceList = geofencingEvent.getTriggeringGeofences();

            // Create a detail message with Geofence received
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geoFenceTransition, triggeringGeofenceList);

            sendCommandToPresenceDevice(geoFenceTransition, triggeringGeofenceList);

            if (settings.getBoolean(Params.PREFS_DEBUGGING_NOTIFICATIONS, false)) {
                sendNotification(geofenceTransitionDetails, getString(R.string.app_name));
            }
        }
    }

    private void sendCommandToPresenceDevice(final int geoFenceTransition, List<Geofence> triggeringGeofenceList) {
        HubList hubList = new HubList(this);
        for ( Geofence geofence : triggeringGeofenceList ) {
            for (final HubItem hubItem : hubList.getHubList()) {
                if (hubItem.getTitle().equals(geofence.getRequestId())) {
                    if (!hubItem.getPresenceDeviceId().isEmpty()) {
                        new HubConnectionHolder(this, hubItem, true, new HubConnectionHolder.HubConnectionHolderListener() {
                            @Override
                            public void onConnection(Boolean connected, HubConnectionHolder hubConnectionHolder) {
                                if (connected) {
                                    if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                                        Log.d(Params.LOGGING_TAG, "Sending on command to " + hubItem.getPresenceDeviceId());
                                        Util.addProtocol(getApplicationContext(), new ProtocolItem(getApplicationContext(), ProtocolType.INFO, "Sending on command to " + hubItem.getPresenceDeviceId(), "GeoFencing"));
                                        hubConnectionHolder.getDeviceCommand(new DeviceCommand(hubItem.getPresenceDeviceId(),"on"));
                                    } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                                        Util.addProtocol(getApplicationContext(), new ProtocolItem(getApplicationContext(), ProtocolType.INFO, "Sending off command to " + hubItem.getPresenceDeviceId(), "GeoFencing"));
                                        Log.d(Params.LOGGING_TAG, "Sending off command to " + hubItem.getPresenceDeviceId());
                                        hubConnectionHolder.getDeviceCommand(new DeviceCommand(hubItem.getPresenceDeviceId(),"off"));
                                    }
                                } else {
                                    Util.addProtocol(getApplicationContext(), new ProtocolItem(getApplicationContext(), ProtocolType.WARNING, "Z-Way not connected!", "GeoFencing"));
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    // Create a detail message with Geofence received
    private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofenceList) {
        ArrayList<String> triggeringGeofencesList;
        triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofenceList) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }

        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody Message body received.
     */
    private void sendNotification(String messageBody, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_razberry)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}
