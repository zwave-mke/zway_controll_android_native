package de.pathec.hubapp.model.notification;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.notifications.Notification;
import de.fh_zwickau.informatik.sensor.model.notifications.NotificationList;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;

public class NotificationListApp {
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;
    private OnNotificationListInteractionListener mCaller;

    private HubConnectionHolder mHubConnectionHolder;

    private AsyncTask<Integer, Void, ZWayResponse> mLoader;
    private Boolean mLoaderActive;

    public NotificationListApp(Context context, OnNotificationListInteractionListener caller, HubConnectionHolder hubConnectionHolder) {
        mContext = context;
        mCaller = caller;
        mHubConnectionHolder = hubConnectionHolder;

        // SQLite
        mDatabaseHandler = DatabaseHandler.getInstance(mContext);
    }

    public void setHubConnectionHolder(HubConnectionHolder hubConnectionHolder) {
        mHubConnectionHolder = hubConnectionHolder;
    }

    public void cancelSynchronization() {
        if (mLoader != null && mLoaderActive) {
            Log.i(Params.LOGGING_TAG, "(NotificationListApp) Cancel synchronization.");
            mLoader.cancel(true);
        }
    }

    public void getNotificationList(final Integer hubId, Boolean loadFromHub) {
        if (loadFromHub) {
            mLoader = new StartLoadingNotification().execute(hubId);
        } else {
            mCaller.onNotificationListLoaded(mDatabaseHandler.getAllNotification(hubId, true), false);
        }
    }

    private class StartLoadingNotification extends AsyncTask<Integer, Void, ZWayResponse> {
        @Override
        protected void onPreExecute() {
            mLoaderActive = true;
        }

        @Override
        protected ZWayResponse doInBackground(Integer... params) {
            if(mHubConnectionHolder != null && params[0] != null) {
                ArrayList<NotificationItemApp> deviceList = loadAndSaveNotificationList(params[0], null, true, false);

                if (deviceList != null) {
                    return new ZWayResponse(true, deviceList);
                }
            }

            return new ZWayResponse(false, mDatabaseHandler.getAllNotification(params[0], true));
        }

        @Override
        protected void onPostExecute(ZWayResponse result) {
            mLoaderActive = false;

            if (!result.dataUpdated) {
                Log.w(Params.LOGGING_TAG, "(NotificationListApp) Z-Way not connected!");
                mCaller.onNotificationListError(mContext.getString(R.string.connection_not_connected_update));
            }
            mCaller.onNotificationListLoaded(result.notificationList, true);
        }
    }

    /**
     * @param hubId Hub id to associate the correct database entries.
     * @param zwayApi If not null use this instance for communication, otherwise use connection
     *                of list instance.
     * @param backgroundOperation Returns only a empty list (not null) if operation successfully performed.
     * @return List of profiles, empty list if background operation or null if anything goes wrong.
     */
    public ArrayList<NotificationItemApp> loadAndSaveNotificationList(Integer hubId, IZWayApi zwayApi, Boolean withFilter, Boolean backgroundOperation) {
        Long since = (new Date().getTime() - (3600 * 12 * 1000)); // 12 hours
        Log.i(Params.LOGGING_TAG, "Loading notification since: " + since);

        NotificationList notificationList;
        if (zwayApi == null) {
            notificationList = mHubConnectionHolder.getNotifications(since);
        } else {
            notificationList = zwayApi.getNotifications(since);
        }

        if (notificationList != null) {
            // Delete all!!!
            mDatabaseHandler.deleteAllNotification(hubId);

            for(Notification notification : notificationList.getNotifications()) {
                NotificationItemApp newNotificationItemApp = new NotificationItemApp(mContext, notification, hubId);

                mDatabaseHandler.addNotification(newNotificationItemApp);

                BusProvider.postOnMain(new ModelAddEvent<>("Notification", newNotificationItemApp));

//                // TODO If add update mechanism then remove delete all operation above!!!
//                NotificationItemApp notificationItemApp = mDatabaseHandler.getNotification(notification.getId(), hubId, notification.getSource());
//
//                if (notificationItemApp.getId() != -1) {
//                    notificationItemApp.setTimestamp(notification.getTimestamp());
//                    notificationItemApp.setLevel(notification.getLevel());
//
//                    Message message = new Message();
//                    message.setDev(notification.getMessage().getDev());
//                    message.setL(notification.getMessage().getL());
//                    notificationItemApp.setMessage(message);
//
//                    notificationItemApp.setType(notification.getType());
//                    notificationItemApp.setSource(notification.getSource());
//
//                    mDatabaseHandler.updateNotification(notificationItemApp);
//
//                    BusProvider.postOnMain(new ModelUpdateEvent<>("Notification", notificationItemApp));
//                } else {
//                    NotificationItemApp newNotificationItemApp = new NotificationItemApp(mContext, notification, hubId);
//
//                    mDatabaseHandler.addNotification(newNotificationItemApp);
//
//                    BusProvider.postOnMain(new ModelAddEvent<>("Notification", newNotificationItemApp));
//                }
            }

            if (!backgroundOperation) {
                return mDatabaseHandler.getAllNotification(hubId, withFilter);
            } else {
                return new ArrayList<>();
            }
        } else {
            return null;
        }
    }

    public NotificationItemApp addNotificationItem(NotificationItemApp item) {
        mDatabaseHandler.addNotification(item);

        BusProvider.postOnMain(new ModelAddEvent<>("Notification", item));

        return item;
    }

    public NotificationItemApp getNotificationItem(Long id, Integer hubId, String source) {
        return mDatabaseHandler.getNotification(id, hubId, source);
    }

    public void updateNotificationItem(NotificationItemApp item) {
        mDatabaseHandler.updateNotification(item);

        BusProvider.postOnMain(new ModelUpdateEvent<>("Notification", item));
    }

    public void deleteNotificationItem(NotificationItemApp item) {
        mDatabaseHandler.deleteNotification(item.getId(), item.getHubId(), item.getSource());
    }

    public void deleteAllNotificationItem(Integer hubId) {
        mDatabaseHandler.deleteAllNotification(hubId);
    }

    class ZWayResponse {
        Boolean dataUpdated;
        ArrayList<NotificationItemApp> notificationList;

        ZWayResponse(Boolean dataUpdated, ArrayList<NotificationItemApp> notificationList) {
            this.dataUpdated = dataUpdated;
            this.notificationList = notificationList;
        }
    }

    /**
     * Callback interface
     */
    public interface OnNotificationListInteractionListener {
        void onNotificationListLoaded(ArrayList<NotificationItemApp> notificationList, Boolean loadFromHub);
        void onNotificationListError(String message);
    }
}
