package de.pathec.hubapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceListDeserializer;
import de.fh_zwickau.informatik.sensor.model.notifications.Notification;
import de.fh_zwickau.informatik.sensor.model.notifications.NotificationListDeserializer;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.model.device.DeviceListApp;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.location.LocationListApp;
import de.pathec.hubapp.model.notification.NotificationItemApp;
import de.pathec.hubapp.model.notification.NotificationListApp;
import de.pathec.hubapp.util.Params;

public class FCMMessagingService extends FirebaseMessagingService implements
        DeviceListApp.OnDeviceListInteractionListener,
        NotificationListApp.OnNotificationListInteractionListener,
        LocationListApp.OnLocationListInteractionListener {
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
        settings.edit().putString(Params.PREFS_FCM_LAST_MESSAGE, Params.formatDateTimeGerman(new Date())).apply();

        long counter = settings.getLong(Params.PREFS_FCM_COUNTER, 0);
        settings.edit().putLong(Params.PREFS_FCM_COUNTER, ++counter).apply();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Log.i(Params.LOGGING_TAG, "Message data payload: " + data);

            if (data.get("type").equals("device:change:metrics:level")) {
                // Deserialize message
                JsonObject deviceAsJson = new Gson().fromJson(data.get("data"), JsonObject.class);
                Device device = new DeviceListDeserializer().deserializeDevice(deviceAsJson, null);

                if (device != null) {
                    // Load device item from SQLite
                    DeviceListApp deviceListApp = new DeviceListApp(this, this, null);
                    DeviceItemApp deviceItem = deviceListApp.getDeviceItem(device.getDeviceId(), Integer.parseInt(data.get("hubId")));

                    // Update device item in SQLite if level/update time changed
                    if (!deviceItem.getDeviceId().isEmpty()) {
                        if (!deviceItem.getMetrics().getLevel().equals(device.getMetrics().getLevel())
                                || !(deviceItem.getUpdateTime().equals(device.getUpdateTime()))) {

                            Log.i(Params.LOGGING_TAG, "Update device");

                            deviceItem.getMetrics().setLevel(device.getMetrics().getLevel());
                            deviceItem.setUpdateTime(device.getUpdateTime());

                            deviceListApp.updateDeviceItem(deviceItem);
                        } else {
                            Log.i(Params.LOGGING_TAG, "No update device - same level and update time");
                        }
                    } else {
                        Log.i(Params.LOGGING_TAG, "Device not found"); // Device created in an other message type!
                    }

                    if (settings.getBoolean(Params.PREFS_DEBUGGING_NOTIFICATIONS, false)) {
                        sendNotification("Device update - " + device.getDeviceId() + " to " + device.getMetrics().getLevel(), getString(R.string.app_name));
                    }

                    // if (data.get("alarm").equals("true") && device.getMetrics().getLevel().equals("on")) {
                    //     LocationListApp locationListApp = new LocationListApp(this, this, null);
                    //     LocationItemApp locationItem = locationListApp.getLocationItem(device.getLocation(), Integer.parseInt(data.get("hubId")));
                    //     if (locationItem != null && !locationItem.getId().equals(0)) {
                    //         sendAlarmNotification(getString(R.string.notification_alarm_with_room, device.getMetrics().getTitle(), locationItem.getTitle()), getString(R.string.app_name));
                    //     } else {
                    //         sendAlarmNotification(getString(R.string.notification_alarm, device.getMetrics().getTitle()), getString(R.string.app_name));
                    //     }
                    // }
                }
            } else if (data.get("type").equals("notification:add")) {
                NotificationListApp notificationListApp = new NotificationListApp(this, this, null);

                JsonObject notificationAsJson = new Gson().fromJson(data.get("data"), JsonObject.class);
                Notification notification = new NotificationListDeserializer().deserializeNotification(notificationAsJson);

                if (notification != null) {
                    NotificationItemApp notificationItemApp = notificationListApp.getNotificationItem(notification.getId(), Integer.parseInt(data.get("hubId")), notification.getSource());
                    if (notificationItemApp.getId() == -1) {
                        NotificationItemApp newNotificationItemApp = new NotificationItemApp(this, notification, Integer.parseInt(data.get("hubId")));
                        notificationListApp.addNotificationItem(newNotificationItemApp);
                    }
                }
            } else if (data.get("type").equals("alarm:message")) {
                HubList hubList = new HubList(this);
                HubItem hubItem = hubList.getHubItem(Integer.parseInt(data.get("hubId")));

                if (hubItem.getId() != -1) {
                    sendAlarmNotification(data.get("data"), getString(R.string.app_name) + " (" + hubItem.getTitle() + ")");
                } else {
                    sendAlarmNotification(data.get("data"), getString(R.string.app_name));
                }
            } else if (data.get("type").equals("alarm:event")) {
                HubList hubList = new HubList(this);
                HubItem hubItem = hubList.getHubItem(Integer.parseInt(data.get("hubId")));

                if (hubItem.getId() != -1) {
                    sendAlarmNotification(data.get("data"), getString(R.string.app_name) + " (" + hubItem.getTitle() + ")");
                } else {
                    sendAlarmNotification(data.get("data"), getString(R.string.app_name));
                }
            }
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
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

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendAlarmNotification(String messageBody, String title) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_razberry)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(UUID.randomUUID().hashCode(), notificationBuilder.build());
    }

    @Override
    public void onDeviceListLoaded(ArrayList<DeviceItemApp> deviceList, Boolean loadFromHub) {
        // Do nothing
    }

    @Override
    public void onDeviceListError(String message) {
        // Do nothing
    }

    @Override
    public void onNotificationListLoaded(ArrayList<NotificationItemApp> notificationList, Boolean loadFromHub) {
        // Do nothing
    }

    @Override
    public void onNotificationListError(String message) {
        // Do nothing
    }

    @Override
    public void onLocationListLoaded(ArrayList<LocationItemApp> locationList, Boolean loadFromHub) {
        // Do nothing
    }

    @Override
    public void onLocationListError(String message) {
        // Do nothing
    }
}
