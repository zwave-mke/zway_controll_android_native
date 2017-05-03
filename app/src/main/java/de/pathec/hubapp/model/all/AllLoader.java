package de.pathec.hubapp.model.all;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.system.SystemInfo;
import de.pathec.hubapp.MainActivity;
import de.pathec.hubapp.R;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.model.device.DeviceListApp;
import de.pathec.hubapp.model.icon.IconItemApp;
import de.pathec.hubapp.model.icon.IconListApp;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.location.LocationListApp;
import de.pathec.hubapp.model.notification.NotificationItemApp;
import de.pathec.hubapp.model.notification.NotificationListApp;
import de.pathec.hubapp.model.profile.ProfileItemApp;
import de.pathec.hubapp.model.profile.ProfileListApp;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.ZWayFirmwareVersion;

public class AllLoader {

    private MainActivity mActivity;
    private OnAllLoaderInteractionListener mListener;
    private IZWayApi mZWayApi;
    private Integer mHubId;

    private AsyncTask<Boolean, Void, Integer> mLoader;
    private Boolean mLoaderActive;

    public AllLoader(MainActivity activity, OnAllLoaderInteractionListener listener, IZWayApi zwayApi, Integer hubId) {
        mActivity = activity;
        mListener = listener;
        mZWayApi = zwayApi;
        mHubId = hubId;
    }

    public void cancelSynchronization() {
        if (mLoader != null && mLoaderActive) {
            Log.i(Params.LOGGING_TAG, "(AllLoader) Cancel synchronization.");
            mLoader.cancel(true);
        }
    }

    public void loadAll(Boolean loadOnlyDevices) {
        // Use executor for initial loading
        mLoader =  new StartLoadingAll().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, loadOnlyDevices);
    }

    private class StartLoadingAll extends AsyncTask<Boolean, Void, Integer> {
        @Override
        protected void onPreExecute() {
            Log.i(Params.LOGGING_TAG, "Start loading all: " + Params.formatDateTime(new Date()));
            mLoaderActive = true;
        }

        @Override
        protected Integer doInBackground(Boolean... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(Params.LOGGING_TAG, e.getMessage());
            }

            try {
                SystemInfo systemInfo = mZWayApi.getSystemInfo();
                ZWayFirmwareVersion firmwareVersion = new ZWayFirmwareVersion(systemInfo.getCurrentFirmware());
                ZWayFirmwareVersion firmwareVersionMin = new ZWayFirmwareVersion("2.3.0");

                if (firmwareVersion.compareTo(firmwareVersionMin) == -1) {
                    return -2;
                }
            } catch (Exception e) {
                Log.e(Params.LOGGING_TAG, "Unexpected error during system info ...");
            }

            if (params[0]) {
                DeviceListApp deviceListApp = new DeviceListApp(mActivity, null, null);
                ArrayList<DeviceItemApp> devices = deviceListApp.loadAndSaveDeviceList(mHubId, mZWayApi, false, false, -1, true);

                if (devices != null) return 1; else return -1;
            } else {
                ProfileListApp profileListApp = new ProfileListApp(mActivity, null, null);
                ArrayList<ProfileItemApp> profiles = profileListApp.loadAndSaveProfileList(mHubId, mZWayApi, true);

                LocationListApp locationListApp = new LocationListApp(mActivity, null, null);
                ArrayList<LocationItemApp> locations = locationListApp.loadAndSaveLocationList(mHubId, mZWayApi, true);

                // Device history loaded as required ...
                // DeviceHistoryListApp deviceHistoryListApp = new DeviceHistoryListApp(mContext, null, null);
                // ArrayList<DeviceHistoryItemApp> history = deviceHistoryListApp.loadAndSaveDeviceHistoryList(mHubId, mZWayApi);

                NotificationListApp notificationListApp = new NotificationListApp(mActivity, null, null);
                ArrayList<NotificationItemApp> notifications = notificationListApp.loadAndSaveNotificationList(mHubId, mZWayApi, false, true);

                IconListApp iconListApp = new IconListApp(mActivity, null, null);
                ArrayList<IconItemApp> icons = iconListApp.loadAndSaveIconList(mHubId, mZWayApi, true);

                DeviceListApp deviceListApp = new DeviceListApp(mActivity, null, null);
                ArrayList<DeviceItemApp> devices = deviceListApp.loadAndSaveDeviceList(mHubId, mZWayApi, false, false, -1, true);

                // return profiles != null && locations != null && history != null && icons != null && devices != null;
                if (profiles != null && locations != null && icons != null && devices != null && notifications != null) return 1; else return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.i(Params.LOGGING_TAG, "Finish loading all: " + Params.formatDateTime(new Date()));
            mLoaderActive = false;

            if (result > 0) {
                mListener.allLoaded(1);
            } else if (result < 0) {
                if (result == -2) {

                    new AlertDialog.Builder(mActivity)
                            .setTitle(mActivity.getString(R.string.app_name))
                            .setMessage(mActivity.getString(R.string.zway_firmware_update_required))
                            .setCancelable(false)
                            .setPositiveButton(mActivity.getString(R.string.close), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mActivity.finish();
                                }
                            })
                            .show();
                }

                mListener.allLoaded(-1);
            }
        }
    }

    public interface OnAllLoaderInteractionListener {
        void allLoaded(Integer status);
    }
}
