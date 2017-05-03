package de.pathec.hubapp.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.IZWayApiCallbacks;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistory;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryList;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.instances.Instance;
import de.fh_zwickau.informatik.sensor.model.instances.InstanceList;
import de.fh_zwickau.informatik.sensor.model.locations.Location;
import de.fh_zwickau.informatik.sensor.model.locations.LocationList;
import de.fh_zwickau.informatik.sensor.model.modules.ModuleList;
import de.fh_zwickau.informatik.sensor.model.namespaces.NamespaceList;
import de.fh_zwickau.informatik.sensor.model.notifications.Notification;
import de.fh_zwickau.informatik.sensor.model.notifications.NotificationList;
import de.fh_zwickau.informatik.sensor.model.profiles.Profile;
import de.fh_zwickau.informatik.sensor.model.profiles.ProfileList;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.controller.ZWaveController;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.devices.ZWaveDevice;
import de.fh_zwickau.informatik.sensor.z_way_library.ZWayApiAndroid;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.events.ConnectionEvent;
import de.pathec.hubapp.events.ConnectionStatus;
import de.pathec.hubapp.events.ConnectionType;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.protocol.ProtocolItem;

class HubConnection implements IZWayApiCallbacks {

    private OnHubConnectionListener mListener;

    private Context mContext;

    private HubItem mHubItem;
    private IZWayApi mZWayAPI;
    private AsyncTask<Void, Void, Boolean> mZWayApiInitializer;
    private ConnectionEvent mLastConnectionEvent;

    private Integer mConnectionType;

    HubConnection(Integer connectionType, HubItem hubItem, Context context, OnHubConnectionListener listener) {
        mListener = listener;

        mConnectionType = connectionType;
        mHubItem = hubItem;
        mContext = context;
    }

    private void log(String message) {
        String connectionTypeName = "";
        // if (mConnectionType.equals(1)) {
        //     connectionTypeName = "Hubs Wifi";
        // } else if (mConnectionType.equals(2)) {
        if (mConnectionType.equals(2)) {
            connectionTypeName = "Remote access";
        } else if (mConnectionType.equals(3)) {
            connectionTypeName = "Local access";
        }
        Log.i(Params.LOGGING_TAG, "(HubConnection - Hub " + mHubItem.getId() + " - " + connectionTypeName + ") " + message);
    }

    IZWayApi getZWayApi() {
        if(mZWayAPI != null) {
            log("Z-Way connected.");
            return mZWayAPI;
        } else {
            log("Z-Way not connected!");
            return null;
        }
    }

    void triggerLastConnectionEvent() {
        log("Trigger last connection state.");
        if (mLastConnectionEvent != null) {
            BusProvider.postOnMain(mLastConnectionEvent);
        }
    }

    private void connect() {
        mZWayAPI = null;

        updateConnection();
    }

    void connect(HubItem hubItem) {
        this.mHubItem = hubItem;

        connect();
    }

    void disconnect() {
        mZWayAPI = null;
    }

    void cancelConnect() {
        log("Cancel asynchronous Z-Way initializer.");

        if (mZWayApiInitializer != null) {
            mZWayApiInitializer.cancel(true);
        }
    }

    private synchronized void updateConnection() {
        if (mZWayApiInitializer != null) {
            if (mZWayApiInitializer.getStatus().equals(AsyncTask.Status.RUNNING) || mZWayApiInitializer.getStatus().equals(AsyncTask.Status.PENDING)) {
                mZWayApiInitializer.cancel(true);
            }
        }

        mZWayApiInitializer = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                log("Update connection (AsyncTask)");
            }

            @Override
            protected Boolean doInBackground( final Void ... params ) {
                IZWayApi zwayApi = null;

                // Check configuration
                // if (mConnectionType.equals(ConnectionType.HUBS_WIFI)) {
                //     // Check configuration
                //     if (mHubItem.getHubSSID().isEmpty() || mHubItem.getHubWifiPassword().isEmpty() || mHubItem.getHubIP().isEmpty()) {
                //         mZWayAPI = null;
                //         mListener.onIsNotConnected(true);

                //         mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.CONFIGURATION_ERROR, mContext.getString(R.string.connection_configuration_error), mHubItem);
                //         BusProvider.postOnMain(mLastConnectionEvent);

                //         return false;
                //     }

                //     // Check Wifi status
                //     WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                //     if (!wifi.isWifiEnabled()) {
                //         wifi.setWifiEnabled(true);
                //         mZWayAPI = null;
                //         mListener.onIsNotConnected(true);

                //         mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.UNAVAILABLE, mContext.getString(R.string.connection_configuration_error), mHubItem);
                //         BusProvider.postOnMain(mLastConnectionEvent);

                //         return false;
                //     }

                //     // Connect to Wifi
                //     Boolean result = Util.connectToWifi(mContext, mHubItem.getHubSSID(), mHubItem.getHubWifiPassword());
                //     if (!result) {
                //         mZWayAPI = null;
                //         mListener.onIsNotConnected(true);

                //         mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.UNAVAILABLE, mContext.getString(R.string.connection_unavailable), mHubItem);
                //         BusProvider.postOnMain(mLastConnectionEvent);

                //         return false;
                //     }

                //     // Connect to Hub
                //     zwayApi = checkZWayConnection(8083, "http", mHubItem.getHubIP(), false);
                // } else if (mConnectionType.equals(ConnectionType.REMOTE_ACCESS)) {
                if (mConnectionType.equals(ConnectionType.REMOTE_ACCESS)) {
                    if (mHubItem.getRemoteService().isEmpty() || mHubItem.getRemoteId() == null || mHubItem.getRemoteId() == 0) {
                        mZWayAPI = null;
                        mListener.onIsNotConnected(true);

                        mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.CONFIGURATION_ERROR, mContext.getString(R.string.connection_configuration_error), mHubItem);
                        BusProvider.postOnMain(mLastConnectionEvent);

                        return false;
                    }

                    // Connect to Hub
                    zwayApi = checkZWayConnection(443, "https", mHubItem.getRemoteService(), true);
                } else if (mConnectionType.equals(ConnectionType.LOCAL_ACCESS)) {
                    if (mHubItem.getLocalIP().isEmpty()) {
                        mZWayAPI = null;
                        mListener.onIsNotConnected(true);

                        mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.CONFIGURATION_ERROR, mContext.getString(R.string.connection_configuration_error), mHubItem);
                        BusProvider.postOnMain(mLastConnectionEvent);

                        return false;
                    }

                    // Connect to Hub
                    zwayApi = checkZWayConnection(8083, "http", mHubItem.getLocalIP(), false);
                }

                if(zwayApi != null) {
                    mZWayAPI = zwayApi;
                    mListener.onIsConnected();

                    mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.CONNECTED, mContext.getString(R.string.connection_connected), mHubItem);
                    BusProvider.postOnMain(mLastConnectionEvent);

                    return true;
                } else {
                    mZWayAPI = null;
                    mListener.onIsNotConnected(true);

                    mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.NOT_CONNECTED, mContext.getString(R.string.connection_not_connected), mHubItem);
                    BusProvider.postOnMain(mLastConnectionEvent);

                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) { }
        }.execute();
    }

    /**
     * Initialize new Z-Way API and perform a login request.
     *
     * @param port HTTP port
     * @param protocol HTTP protocol
     * @return Z-Way API instance if successfully connected or null
     */
    private IZWayApi checkZWayConnection(Integer port, String protocol, String ipAddress, Boolean useRemoteService) {
        if (!mHubItem.getUsername().isEmpty() && !mHubItem.getPassword().isEmpty()) {
            IZWayApi zwayApi = new ZWayApiAndroid(
                    ipAddress,
                    port,
                    protocol,
                    mHubItem.getUsername(),
                    mHubItem.getPassword(),
                    mHubItem.getRemoteId(),
                    useRemoteService,
                    this,
                    mContext);

            if(zwayApi.getLogin() != null) {
                return zwayApi;
            } else {
                return null;
            }
        } else {
            mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.CONFIGURATION_ERROR, mContext.getString(R.string.connection_configuration_error), mHubItem);
            BusProvider.postOnMain(mLastConnectionEvent);
            return null;
        }
    }

    /**
     * Callback interface
     */
    interface OnHubConnectionListener {
        void onIsConnected();
        void onIsNotConnected(Boolean init);
    }

    @Override
    public void getStatusResponse(String s) {

    }

    @Override
    public void getRestartResponse(Boolean aBoolean) {

    }

    @Override
    public void getLoginResponse(String s) {
        log("Z-Way successfully authenticated.");

        ProtocolItem protocolItem = new ProtocolItem(mContext, ProtocolType.INFO, mContext.getString(R.string.z_way_message_authentication_success), "Z-Way");
        Util.addProtocol(mContext, protocolItem);
    }

    @Override
    public void getNamespacesResponse(NamespaceList namespaceList) {

    }

    @Override
    public void getModulesResponse(ModuleList moduleList) {

    }

    @Override
    public void getInstancesResponse(InstanceList instanceList) {

    }

    @Override
    public void postInstanceResponse(Instance instance) {

    }

    @Override
    public void getInstanceResponse(Instance instance) {

    }

    @Override
    public void putInstanceResponse(Instance instance) {

    }

    @Override
    public void deleteInstanceResponse(boolean b) {

    }

    @Override
    public void getDevicesResponse(DeviceList deviceList) {

    }

    @Override
    public void putDeviceResponse(Device device) {

    }

    @Override
    public void getDeviceResponse(Device device) {

    }

    @Override
    public void getDeviceCommandResponse(String s) {

    }

    @Override
    public void getLocationsResponse(LocationList locationList) {

    }

    @Override
    public void postLocationResponse(Location location) {

    }

    @Override
    public void getLocationResponse(Location location) {

    }

    @Override
    public void putLocationResponse(Location location) {

    }

    @Override
    public void deleteLocationResponse(boolean b) {

    }

    @Override
    public void getProfilesResponse(ProfileList profileList) {

    }

    @Override
    public void postProfileResponse(Profile profile) {

    }

    @Override
    public void getProfileResponse(Profile profile) {

    }

    @Override
    public void putProfileResponse(Profile profile) {

    }

    @Override
    public void deleteProfileResponse(boolean b) {

    }

    @Override
    public void getNotificationsResponse(NotificationList notificationList) {

    }

    @Override
    public void getNotificationResponse(Notification notification) {

    }

    @Override
    public void putNotificationResponse(Notification notification) {

    }

    @Override
    public void getDeviceHistoriesResponse(DeviceHistoryList deviceHistoryList) {

    }

    @Override
    public void getDeviceHistoryResponse(DeviceHistory deviceHistory) {

    }

    @Override
    public void getZWaveDeviceResponse(ZWaveDevice zWaveDevice) {

    }

    @Override
    public void getZWaveControllerResponse(ZWaveController zWaveController) {

    }

    @Override
    public void apiError(String s, boolean b) {
        log("Z-Way API error: " + s);

        ProtocolItem protocolItem = new ProtocolItem(mContext, ProtocolType.ERROR, mContext.getString(R.string.z_way_message_api_error) + " " + s, "Z-Way");
        Util.addProtocol(mContext, protocolItem);

        mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.NOT_CONNECTED, mContext.getString(R.string.connection_not_connected), mHubItem);
        BusProvider.postOnMain(mLastConnectionEvent);
        mZWayAPI = null;
        mListener.onIsNotConnected(false);
    }

    @Override
    public void httpStatusError(int i, String s, boolean b) {
        log("Z-Way API error (HTTP status): " + s + "(" + String.valueOf(i)+ ")");

        ProtocolItem protocolItem = new ProtocolItem(mContext, ProtocolType.ERROR, mContext.getString(R.string.z_way_message_api_error_http_status) + " " + s + "(" + String.valueOf(i)+ ")", "Z-Way");
        Util.addProtocol(mContext, protocolItem);

        if (i != 404) {
            mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.NOT_CONNECTED, mContext.getString(R.string.connection_not_connected), mHubItem);
            BusProvider.postOnMain(mLastConnectionEvent);
            mZWayAPI = null;
            mListener.onIsNotConnected(false);
        }
    }

    @Override
    public void authenticationError() {
        log("Z-Way API error (Authentication)");

        ProtocolItem protocolItem = new ProtocolItem(mContext, ProtocolType.ERROR, mContext.getString(R.string.z_way_message_api_error_authentication), "Z-Way");
        Util.addProtocol(mContext, protocolItem);

        mLastConnectionEvent = new ConnectionEvent(mConnectionType, ConnectionStatus.NOT_CONNECTED, mContext.getString(R.string.connection_not_connected), mHubItem);
        BusProvider.postOnMain(mLastConnectionEvent);
        mZWayAPI = null;
        mListener.onIsNotConnected(false);
    }

    @Override
    public void responseFormatError(String s, boolean b) {
        log("Z-Way API error (Response format): " + s);

        ProtocolItem protocolItem = new ProtocolItem(mContext, ProtocolType.ERROR, mContext.getString(R.string.z_way_message_api_error_authentication), "Z-Way");
        Util.addProtocol(mContext, protocolItem);
    }

    @Override
    public void message(int i, String s) {
        log("Z-Way API message: " + s);
    }
}
