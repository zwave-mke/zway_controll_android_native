package de.pathec.hubapp.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryData;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryList;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceCommand;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.icons.IconList;
import de.fh_zwickau.informatik.sensor.model.instances.Instance;
import de.fh_zwickau.informatik.sensor.model.locations.LocationList;
import de.fh_zwickau.informatik.sensor.model.notifications.NotificationList;
import de.fh_zwickau.informatik.sensor.model.profiles.ProfileList;
import de.fh_zwickau.informatik.sensor.model.system.SystemInfo;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.events.ConnectionEvent;
import de.pathec.hubapp.events.ConnectionStatus;
import de.pathec.hubapp.events.ConnectionType;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;

public class HubConnectionHolder implements
        HubConnection.OnHubConnectionListener {
    private Context mContext;
    private HubItem mHubItem;

    private Integer mConnectingCount = 0;

    // private HubConnection mHubsWifiConnection;
    private HubConnection mRemoteConnection;
    private HubConnection mLocalConnection;

    private HubConnectionHolderListener mListener;

    public HubConnectionHolder(Context context, HubItem hubItem, Boolean connect) {
        mContext = context;
        mHubItem = hubItem;

        // mHubsWifiConnection = new HubConnection(ConnectionType.HUBS_WIFI, hubItem, context, this);
        mRemoteConnection = new HubConnection(ConnectionType.REMOTE_ACCESS, hubItem, context, this);
        mLocalConnection = new HubConnection(ConnectionType.LOCAL_ACCESS, hubItem, context, this);

        if (connect) {
            connect();
        }
    }

    public HubConnectionHolder(Context context, HubItem hubItem, Boolean connect, HubConnectionHolderListener listener) {
        mContext = context;
        mHubItem = hubItem;
        mListener = listener;

        // mHubsWifiConnection = new HubConnection(ConnectionType.HUBS_WIFI, hubItem, context, this);
        mRemoteConnection = new HubConnection(ConnectionType.REMOTE_ACCESS, hubItem, context, this);
        mLocalConnection = new HubConnection(ConnectionType.LOCAL_ACCESS, hubItem, context, this);

        if (connect) {
            connect();
        }
    }

    public HubItem getHubItem() {
        return mHubItem;
    }

    public void setHubItem(HubItem hubItem) {
        mHubItem = hubItem;
    }

    public boolean isHubConnected() {
        // return mHubsWifiConnection.getZWayApi() != null || mLocalConnection.getZWayApi() != null || mRemoteConnection.getZWayApi() != null;
        return mLocalConnection.getZWayApi() != null || mRemoteConnection.getZWayApi() != null;
    }

    public void triggerLastConnectionEvents() {
        Log.i(Params.LOGGING_TAG, "(HubConnectionHolder) Trigger last connection state.");

        // mHubsWifiConnection.triggerLastConnectionEvent();
        mLocalConnection.triggerLastConnectionEvent();
        mRemoteConnection.triggerLastConnectionEvent();
    }

    public void connect() {
        // mHubsWifiConnection.disconnect();
        mLocalConnection.disconnect();
        mRemoteConnection.disconnect();

        mConnectingCount = 2; // TODO if WiFi then 3

        ConnectionEvent allConnectionEvent = new ConnectionEvent(ConnectionType.ALL, ConnectionStatus.CONNECTING, "", mHubItem);
        BusProvider.postOnMain(allConnectionEvent);

        // mHubsWifiConnection.connect(mHubItem);
        mLocalConnection.connect(mHubItem);
        mRemoteConnection.connect(mHubItem);
    }

    public void disconnect() {
        // mHubsWifiConnection.disconnect();
        mLocalConnection.disconnect();
        mRemoteConnection.disconnect();
    }

    public void cancelConnect() {
        Log.i(Params.LOGGING_TAG, "(HubConnectionHolder) Cancel asynchronous Z-Way initializer");

        // mHubsWifiConnection.cancelConnect();
        mLocalConnection.cancelConnect();
        mRemoteConnection.cancelConnect();
    }

    public IZWayApi getZWayApi() {
        // 1. Hubs Wifi
        // IZWayApi zwayApiHubsWifi = mHubsWifiConnection.getZWayApi();
        // if (zwayApiHubsWifi != null) {
        //     return zwayApiHubsWifi;
        // }

        // 2. Local Access
        IZWayApi zwayApiLocalAccess = mLocalConnection.getZWayApi();
        if (zwayApiLocalAccess != null) {
            return zwayApiLocalAccess;
        }

        // 3. Remote Access
        IZWayApi zwayApiRemoteAccess = mRemoteConnection.getZWayApi();
        if (zwayApiRemoteAccess != null) {
            return zwayApiRemoteAccess;
        }

        // Hub isn't connected!
        ConnectionEvent allConnectionEvent = new ConnectionEvent(ConnectionType.ALL, ConnectionStatus.NOT_CONNECTED, mContext.getString(R.string.connection_not_connected), mHubItem);
        BusProvider.postOnMain(allConnectionEvent);

        return null;
    }

    public DeviceList getDevices() {
        DeviceList deviceList = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            deviceList = zwayApi.getDevices();
        }

        if (deviceList != null) {
            return deviceList;
        } else {
            return null;
        }
    }

    public LocationList getLocations() {
        LocationList locationList = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            locationList = zwayApi.getLocations();
        }

        if (locationList != null) {
            return locationList;
        } else {
            return null;
        }
    }

    public ProfileList getProfiles() {
        ProfileList profileList = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            profileList = zwayApi.getProfiles();
        }

        if (profileList != null) {
            return profileList;
        } else {
            return null;
        }
    }

    public NotificationList getNotifications(Long since) {
        NotificationList notificationList = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            notificationList = zwayApi.getNotifications(since);
        }

        if (notificationList != null) {
            return notificationList;
        } else {
            return null;
        }
    }

    public DeviceHistoryList getDeviceHistories() {
        DeviceHistoryList deviceHistoryList = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            deviceHistoryList = zwayApi.getDeviceHistories();
        }

        if (deviceHistoryList != null) {
            return deviceHistoryList;
        } else {
            return null;
        }
    }

    public ArrayList<DeviceHistoryData> getDeviceHistory(String deviceId, Long since) {
        ArrayList<DeviceHistoryData> deviceHistory = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            deviceHistory = zwayApi.getDeviceHistory(deviceId, since);
        }

        if (deviceHistory != null) {
            return deviceHistory;
        } else {
            return null;
        }
    }

    public String getDeviceCommand(DeviceCommand deviceCommand) {
        Log.i(Params.LOGGING_TAG, "Device command: " + deviceCommand.toString());

        String message = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            message = zwayApi.getDeviceCommand(deviceCommand);
        }

        if (message != null) {
            return message;
        } else {
            return null;
        }
    }

    public IconList getIcons() {
        IconList iconList = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            iconList = zwayApi.getIcons();
        }

        if (iconList != null) {
            return iconList;
        } else {
            return null;
        }
    }

    public String postIcon(File icon) {
        String message = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            message = zwayApi.postIcon(icon);
        }

        if (message != null) {
            return message;
        } else {
            return null;
        }
    }

    public Instance postInstance(Instance newInstance) {
        Instance instance = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            instance = zwayApi.postInstance(newInstance);
        }

        if (instance != null) {
            return instance;
        } else {
            return null;
        }
    }

    public Device getDevice(String deviceId) {
        Device device = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            device = zwayApi.getDevice(deviceId);
        }

        if (device != null) {
            return device;
        } else {
            return null;
        }
    }

    public String getDeviceAsJson(String deviceId) {
        String device = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            device = zwayApi.getDeviceAsJson(deviceId);
        }

        if (device != null) {
            return device;
        } else {
            return null;
        }
    }

    public Device putDevice(Device deviceToUpdate) {
        Device device = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            device = zwayApi.putDevice(deviceToUpdate);
        }

        if (device != null) {
            return device;
        } else {
            return null;
        }
    }

    public SystemInfo getSystemInfo() {
        SystemInfo systemInfo = null;

        IZWayApi zwayApi = getZWayApi();
        if (zwayApi != null) {
            systemInfo = zwayApi.getSystemInfo();
        }

        if (systemInfo != null) {
            return systemInfo;
        } else {
            return null;
        }
    }

    @Override
    public synchronized void onIsConnected() {
        mConnectingCount--;

        checkConnection();
    }

    @Override
    public synchronized void onIsNotConnected(Boolean init) {
        if (init) {
            mConnectingCount--;
        }

        checkConnection();
    }

    private void checkConnection() {
        Log.i(Params.LOGGING_TAG, "(HubConnectionHolder) Connecting instances: " + mConnectingCount);

        if (mConnectingCount == 0) {
            if (isHubConnected()) {
                Log.i(Params.LOGGING_TAG, "(HubConnectionHolder) Hub connected!");

                if (mListener != null) {
                    mListener.onConnection(true, this);
                } else {
                    // Connected
                    ConnectionEvent allConnectionEvent = new ConnectionEvent(ConnectionType.ALL, ConnectionStatus.CONNECTED, mContext.getString(R.string.connection_connected), mHubItem);
                    BusProvider.postOnMain(allConnectionEvent);
                }
            } else {
                Log.i(Params.LOGGING_TAG, "(HubConnectionHolder) Hub is not connected!");
                Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Check connection resulted in Hub isn't connected!", "HubConnection"));

                if (mListener != null) {
                    mListener.onConnection(false, this);
                } else {
                    // Not connected
                    ConnectionEvent allConnectionEvent = new ConnectionEvent(ConnectionType.ALL, ConnectionStatus.NOT_CONNECTED, mContext.getString(R.string.connection_not_connected), mHubItem);
                    BusProvider.postOnMain(allConnectionEvent);
                }
            }
        }
    }

    public interface HubConnectionHolderListener {
        void onConnection(Boolean connected, HubConnectionHolder hubConnectionHolder);
    }
}
