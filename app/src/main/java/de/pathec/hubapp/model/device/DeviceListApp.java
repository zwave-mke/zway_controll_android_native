package de.pathec.hubapp.model.device;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.devices.Color;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.devices.Metrics;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.model.icon.IconListApp;
import de.pathec.hubapp.model.location.LocationListApp;
import de.pathec.hubapp.model.profile.ProfileListApp;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.ZWayUtil;

public class DeviceListApp {
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;
    private OnDeviceListInteractionListener mCaller;

    private HubConnectionHolder mHubConnectionHolder;

    private AsyncTask<Integer, Void, ZWayResponse> mLoader;
    private Boolean mLoaderActive;

    public DeviceListApp(Context context, OnDeviceListInteractionListener caller, HubConnectionHolder hubConnectionHolder) {
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
            Log.i(Params.LOGGING_TAG, "(DeviceListApp) Cancel synchronization.");
            mLoader.cancel(true);
        }
    }

    public void getDeviceList(final Integer hubId, Boolean loadFromHub, Boolean onlyDashboard, Integer locationId) {
        if (loadFromHub) {
            ProfileListApp profileListApp = new ProfileListApp(mContext, null, null);
            profileListApp.loadAndSaveProfileList(hubId, mHubConnectionHolder.getZWayApi(), true);

            LocationListApp locationListApp = new LocationListApp(mContext, null, null);
            locationListApp.loadAndSaveLocationList(hubId, mHubConnectionHolder.getZWayApi(), true);

            // Device history loaded as required ...
            // DeviceHistoryListApp deviceHistoryListApp = new DeviceHistoryListApp(mContext, null, null);
            // deviceHistoryListApp.loadAndSaveDeviceHistoryList(hubId, mHubConnectionHolder.getZWayApi(), true);

            IconListApp iconListApp = new IconListApp(mContext, null, null);
            iconListApp.loadAndSaveIconList(hubId, mHubConnectionHolder.getZWayApi(), true);

            Log.i(Params.LOGGING_TAG, "(DeviceListApp) Loading data from Hub");
            mLoader = new StartLoadingDevice().execute(hubId, onlyDashboard ? 1 : 0, locationId);
        } else {
            Log.i(Params.LOGGING_TAG, "(DeviceListApp) Loading data from SQLite database.");
            mCaller.onDeviceListLoaded(mDatabaseHandler.getAllDevice(hubId, true, onlyDashboard, locationId), false);
        }
    }

    private class StartLoadingDevice extends AsyncTask<Integer, Void, ZWayResponse> {
        @Override
        protected void onPreExecute() {
            Log.i(Params.LOGGING_TAG, "Start loading devices: " + Params.formatDateTime(new Date()));
            mLoaderActive = true;
        }

        @Override
        protected ZWayResponse doInBackground(Integer... params) {
            if(mHubConnectionHolder != null && params[0] != null) {
                ArrayList<DeviceItemApp> deviceList = loadAndSaveDeviceList(params[0], null, true, params[1].equals(1), params[2], false);

                if (deviceList != null) {
                    return new ZWayResponse(true, deviceList);
                }
            }

            if (params[1].equals(1)) {
                return new ZWayResponse(false, mDatabaseHandler.getAllDevice(params[0], true, true, params[2]));
            } else {
                return new ZWayResponse(false, mDatabaseHandler.getAllDevice(params[0], true, false, params[2]));
            }
        }

        @Override
        protected void onPostExecute(ZWayResponse result) {
            Log.i(Params.LOGGING_TAG, "Finish loading devices: " + Params.formatDateTime(new Date()));
            mLoaderActive = false;

            if (!result.dataUpdated) {
                Log.w(Params.LOGGING_TAG, "(DeviceListApp) Z-Way not connected!");
                mCaller.onDeviceListError(mContext.getString(R.string.connection_not_connected_update));
            }
            mCaller.onDeviceListLoaded(result.deviceList, true);
        }
    }

    /**
     * @param hubId Hub id to associate the correct database entries.
     * @param zwayApi If not null use this instance for communication, otherwise use connection
     *                of list instance.
     * @param backgroundOperation Returns only a empty list (not null) if operation successfully performed.
     * @return List of profiles, empty list if background operation or null if anything goes wrong.
     */
    public ArrayList<DeviceItemApp> loadAndSaveDeviceList(Integer hubId, IZWayApi zwayApi, Boolean withFilter, Boolean onlyDashboard, Integer locationId, Boolean backgroundOperation) {
        DeviceList deviceList = null;
        if (zwayApi == null) {
            if (mHubConnectionHolder != null) {
                deviceList = mHubConnectionHolder.getDevices();
            }
        } else {
            deviceList = zwayApi.getDevices();
        }

        if (deviceList != null) {
            Set<String> deviceIds = new HashSet<>();

            for(Device device : deviceList.getAllDevices()) {
                if (ZWayUtil.getBlacklistDeviceIds().contains(device.getDeviceId())) {
                    continue;
                }

                deviceIds.add(device.getDeviceId());

                DeviceItemApp deviceItemApp = mDatabaseHandler.getDevice(device.getDeviceId(), hubId);

                if (!deviceItemApp.getDeviceId().equals("")) {

                    deviceItemApp.setCreationTime(device.getCreationTime());
                    deviceItemApp.setCreatorId(device.getCreatorId());
                    deviceItemApp.setDeviceType(device.getDeviceType());
                    deviceItemApp.setH(device.getH());
                    deviceItemApp.setHasHistory(device.getHasHistory());
                    deviceItemApp.setLocation(device.getLocation());
                    deviceItemApp.setPermanentlyHidden(device.getPermanentlyHidden());
                    deviceItemApp.setProbeType(device.getProbeType());
                    deviceItemApp.setVisibility(device.getVisibility());
                    deviceItemApp.setUpdateTime(device.getUpdateTime());

                    Metrics metrics = new Metrics();
                    metrics.setIcon(device.getMetrics().getIcon());
                    metrics.setTitle(device.getMetrics().getTitle());
                    metrics.setLevel(device.getMetrics().getLevel());
                    metrics.setProbeTitle(device.getMetrics().getProbeTitle());
                    metrics.setScaleTitle(device.getMetrics().getScaleTitle());

                    Color color = new Color();
                    color.setRed(device.getMetrics().getColor().getRed());
                    color.setGreen(device.getMetrics().getColor().getGreen());
                    color.setBlue(device.getMetrics().getColor().getBlue());

                    metrics.setColor(color);
                    metrics.setMin(device.getMetrics().getMin());
                    metrics.setMax(device.getMetrics().getMax());

                    metrics.setCameraStreamUrl(device.getMetrics().getCameraStreamUrl());
                    metrics.setCameraHasZoomIn(device.getMetrics().getCameraHasZoomIn());
                    metrics.setCameraHasZoomOut(device.getMetrics().getCameraHasZoomOut());
                    metrics.setCameraHasLeft(device.getMetrics().getCameraHasLeft());
                    metrics.setCameraHasRight(device.getMetrics().getCameraHasRight());
                    metrics.setCameraHasUp(device.getMetrics().getCameraHasUp());
                    metrics.setCameraHasDown(device.getMetrics().getCameraHasDown());
                    metrics.setCameraHasOpen(device.getMetrics().getCameraHasOpen());
                    metrics.setCameraHasClose(device.getMetrics().getCameraHasClose());

                    metrics.setDiscreteCurrentScene(device.getMetrics().getDiscreteCurrentScene());
                    metrics.setDiscreteKeyAttribute(device.getMetrics().getDiscreteKeyAttribute());
                    metrics.setDiscreteState(device.getMetrics().getDiscreteState());
                    metrics.setDiscreteMaxScenes(device.getMetrics().getDiscreteMaxScenes());
                    metrics.setDiscreteCount(device.getMetrics().getDiscreteCount());
                    metrics.setDiscreteType(device.getMetrics().getDiscreteType());

                    metrics.setText(device.getMetrics().getText());

                    deviceItemApp.setIcons(device.getIcons());

                    deviceItemApp.setMetrics(metrics);
                    deviceItemApp.setTags(device.getTags());

                    mDatabaseHandler.updateDevice(deviceItemApp);

                    if (deviceItemApp.getIcon().equals("")) {
                        // Download custom icon
                        loadCustomIcon(deviceItemApp, zwayApi);
                    }

                    BusProvider.postOnMain(new ModelUpdateEvent<>("Device", deviceItemApp));
                } else {
                    DeviceItemApp newDeviceItemApp = new DeviceItemApp(mContext, device, hubId);

                    mDatabaseHandler.addDevice(newDeviceItemApp);

                    // Download custom icon
                    loadCustomIcon(newDeviceItemApp, zwayApi);

                    // BusProvider.postOnMain(new ModelAddEvent<>("Device", newDeviceItemApp));
                }
            }

            // Delete devices
            ArrayList<DeviceItemApp> devices = mDatabaseHandler.getAllDevice(hubId, false, false, -1);
            for (DeviceItemApp device : devices) {
                if (!deviceIds.contains(device.getDeviceId())) {
                    mDatabaseHandler.deleteDevice(device.getDeviceId(), hubId);
                }
            }

            if (!backgroundOperation) {
                return mDatabaseHandler.getAllDevice(hubId, withFilter, onlyDashboard, locationId);
            } else {
                return new ArrayList<>();
            }
        } else {
            return null;
        }
    }

    public DeviceItemApp addDeviceItem(DeviceItemApp item) {
        mDatabaseHandler.addDevice(item);

        BusProvider.postOnMain(new ModelAddEvent<>("Device", item));

        return item;
    }

    public DeviceItemApp getDeviceItem(String deviceId, Integer hubId) {
        return mDatabaseHandler.getDevice(deviceId, hubId);
    }

    public void updateDeviceItem(DeviceItemApp item) {
        updateDeviceItem(item, true);
    }

    public void updateDeviceItem(DeviceItemApp item, Boolean postEvent) {
        mDatabaseHandler.updateDevice(item);

        if (postEvent) {
            BusProvider.postOnMain(new ModelUpdateEvent<>("Device", item));
        }
    }

    public void deleteDeviceItem(DeviceItemApp item) {
        mDatabaseHandler.deleteDevice(item.getDeviceId(), item.getHubId());
    }

    class ZWayResponse {
        Boolean dataUpdated;
        ArrayList<DeviceItemApp> deviceList;

        ZWayResponse(Boolean dataUpdated, ArrayList<DeviceItemApp> deviceList) {
            this.dataUpdated = dataUpdated;
            this.deviceList = deviceList;
        }
    }

    public Set<String> getAllDeviceTags(Integer hubId) {
        return mDatabaseHandler.getAllDeviceTags(hubId);
    }

    public Set<String> getAllDeviceTypes(Integer hubId) {
        return mDatabaseHandler.getAllDeviceTypes(hubId);
    }

    public Set<String> getAllDeviceSources(Integer hubId) {
        return mDatabaseHandler.getAllDeviceSources(hubId);
    }

    private void loadCustomIcon(DeviceItemApp deviceItem, IZWayApi zwayApi) {
        // Download custom icon
        if (deviceItem.getMetrics().getIcon().startsWith("/")) {
            String iconPath;
            if (zwayApi == null) {
                iconPath = loadIcon(deviceItem.getMetrics().getIcon(), mHubConnectionHolder.getZWayApi());
            } else {
                iconPath = loadIcon(deviceItem.getMetrics().getIcon(), zwayApi);
            }
            if (iconPath != null) {
                Log.i(Params.LOGGING_TAG, "Downloading icon successful: " + iconPath);
                deviceItem.setIcon(iconPath);

                mDatabaseHandler.updateDevice(deviceItem);
            }
        }
    }

    /**
     * @param file for example /ZAutomation/api/v1/load/modulemedia/Astronomy/altitude_day.png
     * @param zwayApi
     */
    private String loadIcon(String file, IZWayApi zwayApi) {
        if (zwayApi == null) {
            Log.w(Params.LOGGING_TAG, "Downloading image failed: Z-Way not connected");
            return null;
        }

        try {
            Bitmap icon = getIconAsBitmap(zwayApi.getTopLevelUrl() + file,
                    "ZWAYSession=" + zwayApi.getZWaySessionId() + "; ZBW_SESSID=" + zwayApi.getZWayRemoteSessionId());

            if (icon == null) {
                Log.w(Params.LOGGING_TAG, "Downloading image failed: Bitmap is null!");
                return null;
            }

            String iconPath = mContext.getFilesDir().getPath() + "/" + Util.saveImage(mContext, icon, "icon_" + file, "");
            if (iconPath == null) {
                Log.w(Params.LOGGING_TAG, "Downloading image failed: Can't store bitmap as file!");
            }

            Log.i(Params.LOGGING_TAG, "Downloading image successful: " + iconPath);

            return iconPath;
        } catch (IOException e) {
            Log.e(Params.LOGGING_TAG, "Downloading image failed: " + e.getMessage(), e);
        }

        return null;
    }

    private Bitmap getIconAsBitmap(String urlString, String cookies) throws IOException {
        URL url = new URL(urlString);
        URLConnection connection =  url.openConnection();

        Log.i(Params.LOGGING_TAG, "Downloading image: " + urlString);

        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;

        try {
            httpConnection = (HttpURLConnection) connection;
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setReadTimeout(60000 /* milliseconds */);
            httpConnection.setConnectTimeout(65000 /* milliseconds */);
            httpConnection.setRequestMethod("GET");
            if (!cookies.isEmpty()) {
                httpConnection.setRequestProperty("Cookie", cookies);
            }
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConnection.getInputStream();

                // Default solution
                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 2;
                //return BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);

                // Stream with skip method
                //return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));

                // Wrap with buffered input stream
                return BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
            } else {
                Log.e(Params.LOGGING_TAG, "Downloading image failed: " + httpConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(Params.LOGGING_TAG, "Downloading image failed: " + e.getMessage(), e);
        } finally {
            httpConnection.disconnect();
            try {
                inputStream.close();
            } catch (Exception e) { }
        }
        return null;
    }

    /**
     * Callback interface
     */
    public interface OnDeviceListInteractionListener {
        void onDeviceListLoaded(ArrayList<DeviceItemApp> deviceList, Boolean loadFromHub);
        void onDeviceListError(String message);
    }
}
