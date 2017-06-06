package de.pathec.hubapp.model.devicehistory;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistory;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryData;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryList;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;

public class DeviceHistoryListApp {
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;
    private OnDeviceHistoryListInteractionListener mCaller;

    private HubConnectionHolder mHubConnectionHolder;

    private AsyncTask<Integer, Void, ZWayResponse> mLoader;
    private Boolean mLoaderActive;

    public DeviceHistoryListApp(Context context, OnDeviceHistoryListInteractionListener caller, HubConnectionHolder hubConnectionHolder) {
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
            Log.i(Params.LOGGING_TAG, "(DeviceHistoryListApp) Cancel synchronization.");
            mLoader.cancel(true);
        }
    }

    public void getDeviceHistoryList(final Integer hubId, Boolean loadFromHub) {
        if (loadFromHub) {
            Log.i(Params.LOGGING_TAG, "(DeviceHistoryListApp) Loading data from Hub");
            mLoader = new StartLoadingDeviceHistory().execute(hubId);
        } else {
            Log.i(Params.LOGGING_TAG, "(DeviceHistoryListApp) Loading data from SQLite database.");
            mCaller.onDeviceHistoryListLoaded(mDatabaseHandler.getAllDeviceHistory(hubId), false);
        }
    }

    public void getDeviceHistory(String deviceId, Long since) {
        Log.i(Params.LOGGING_TAG, "(DeviceHistoryListApp) Loading single history from Hub");
        new StartLoadingSingleDeviceHistory().execute(deviceId, String.valueOf(since));
    }

    private class StartLoadingDeviceHistory extends AsyncTask<Integer, Void, ZWayResponse> {
        @Override
        protected void onPreExecute() {
            Log.i(Params.LOGGING_TAG, "Start loading device history: " + Params.formatDateTime(new Date()));
            mLoaderActive = true;
        }

        @Override
        protected ZWayResponse doInBackground(Integer... params) {
            if(mHubConnectionHolder != null && params[0] != null) {
                ArrayList<DeviceHistoryItemApp> deviceHistoryList = loadAndSaveDeviceHistoryList(params[0], null, false);

                if (deviceHistoryList != null) {
                    return new ZWayResponse(true, deviceHistoryList);
                }
            }

            return new ZWayResponse(false, mDatabaseHandler.getAllDeviceHistory(params[0]));
        }

        @Override
        protected void onPostExecute(ZWayResponse result) {
            Log.i(Params.LOGGING_TAG, "Finish loading device history: " + Params.formatDateTime(new Date()));
            mLoaderActive = false;

            if (!result.dataUpdated) {
                Log.w(Params.LOGGING_TAG, "(DeviceHistoryListApp) Z-Way not connected!");
                mCaller.onDeviceHistoryListError(mContext.getString(R.string.connection_not_connected_update));
            }
            mCaller.onDeviceHistoryListLoaded(result.deviceHistoryList, true);
        }
    }

    /**
     * @param hubId Hub id to associate the correct database entries.
     * @param zwayApi If not null use this instance for communication, otherwise use connection
     *                of list instance.
     * @param backgroundOperation Returns only a empty list (not null) if operation successfully performed.
     * @return List of profiles, empty list if background operation or null if anything goes wrong.
     */
    public ArrayList<DeviceHistoryItemApp> loadAndSaveDeviceHistoryList(Integer hubId, IZWayApi zwayApi, Boolean backgroundOperation) {
        DeviceHistoryList deviceHistoryList = null;
        if (zwayApi == null) {
            if (mHubConnectionHolder != null) {
                deviceHistoryList = mHubConnectionHolder.getDeviceHistories();
            }
        } else {
            deviceHistoryList = zwayApi.getDeviceHistories();
        }

        if (deviceHistoryList != null) {
            // Delete all!!!
            mDatabaseHandler.deleteAllDeviceHistory(hubId);

            for(DeviceHistory deviceHistory : deviceHistoryList.getDeviceHistoryList()) {
                DeviceHistoryItemApp newDeviceHistoryItemApp = new DeviceHistoryItemApp(mContext, deviceHistory, hubId);

                mDatabaseHandler.addDeviceHistory(newDeviceHistoryItemApp);

                BusProvider.postOnMain(new ModelAddEvent<>("DeviceHistory", newDeviceHistoryItemApp));

                  // TODO If add update mechanism then remove delete all operation above!!!
//                DeviceHistoryItemApp deviceHistoryItemApp = mDatabaseHandler.getDeviceHistory(deviceHistory.getDeviceId(), hubId);
//
//                if (!deviceHistoryItemApp.getDeviceId().equals("")) {
//                    deviceHistoryItemApp.setHistoryData(deviceHistory.getHistoryData());
//                    deviceHistoryItemApp.setDeviceType(deviceHistory.getDeviceType());
//
//                    mDatabaseHandler.updateDeviceHistory(deviceHistoryItemApp);
//
//                    BusProvider.postOnMain(new ModelUpdateEvent<>("DeviceHistory", deviceHistoryItemApp));
//                } else {
//                    DeviceHistoryItemApp newDeviceHistoryItemApp = new DeviceHistoryItemApp(mContext, deviceHistory, hubId);
//
//                    mDatabaseHandler.addDeviceHistory(newDeviceHistoryItemApp);
//
//                    BusProvider.postOnMain(new ModelAddEvent<>("DeviceHistory", newDeviceHistoryItemApp));
//                }
            }

            if (!backgroundOperation) {
                return mDatabaseHandler.getAllDeviceHistory(hubId);
            } else {
                return new ArrayList<>();
            }
        } else {
            return null;
        }
    }

    private class StartLoadingSingleDeviceHistory extends AsyncTask<String, Void, ZWayResponseSingle> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            Log.i(Params.LOGGING_TAG, "Start loading single device history: " + Params.formatDateTime(new Date()));

            dialog = new ProgressDialog(mContext);
            dialog.setMessage(mContext.getString(R.string.please_wait));
            dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected ZWayResponseSingle doInBackground(String... params) {
            if(mHubConnectionHolder != null && params[0] != null && params[1] != null) {
                return new ZWayResponseSingle(params[0], mHubConnectionHolder.getDeviceHistory(params[0], Long.parseLong(params[1])));
            }

            return null;
        }

        @Override
        protected void onPostExecute(ZWayResponseSingle result) {
            dialog.dismiss();
            dialog = null;

            Log.i(Params.LOGGING_TAG, "Finish loading single device history: " + Params.formatDateTime(new Date()));

            mCaller.onDeviceHistory(result.deviceHistory, result.deviceId);
        }
    }

    public DeviceHistoryItemApp addDeviceHistoryItem(DeviceHistoryItemApp item) {
        mDatabaseHandler.addDeviceHistory(item);

        BusProvider.postOnMain(new ModelAddEvent<>("DeviceHistory", item));

        return item;
    }

    public DeviceHistoryItemApp getDeviceHistoryItem(String deviceId, Integer hubId) {
        return mDatabaseHandler.getDeviceHistory(deviceId, hubId);
    }

    public void updateDeviceHistoryItem(DeviceHistoryItemApp item) {
        mDatabaseHandler.updateDeviceHistory(item);

        BusProvider.postOnMain(new ModelUpdateEvent<>("DeviceHistory", item));
    }

    public void deleteDeviceHistoryItem(DeviceHistoryItemApp item) {
        mDatabaseHandler.deleteDeviceHistory(item.getDeviceId(), item.getHubId());
    }

    class ZWayResponse {
        Boolean dataUpdated;
        ArrayList<DeviceHistoryItemApp> deviceHistoryList;

        ZWayResponse(Boolean dataUpdated, ArrayList<DeviceHistoryItemApp> deviceHistoryList) {
            this.dataUpdated = dataUpdated;
            this.deviceHistoryList = deviceHistoryList ;
        }
    }

    class ZWayResponseSingle {
        String deviceId;
        ArrayList<DeviceHistoryData> deviceHistory;

        ZWayResponseSingle(String deviceId, ArrayList<DeviceHistoryData> deviceHistory) {
            this.deviceId = deviceId;
            this.deviceHistory = deviceHistory ;
        }
    }

    /**
     * Callback interface
     */
    public interface OnDeviceHistoryListInteractionListener {
        void onDeviceHistoryListLoaded(ArrayList<DeviceHistoryItemApp> deviceHistoryList, Boolean loadFromHub);
        void onDeviceHistoryListError(String message);

        void onDeviceHistory(ArrayList<DeviceHistoryData> deviceHistory, String deviceId);
    }
}
