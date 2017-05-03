package de.pathec.hubapp.model.devicehistory;

import android.content.Context;

import java.util.ArrayList;

import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistory;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryData;

public class DeviceHistoryItemApp extends DeviceHistory {

    private Integer mHubId;

    private Context mContext;

    public DeviceHistoryItemApp(Context context, DeviceHistoryItemApp item) {
        super();

        mContext = context;

        setDeviceId(item.getDeviceId());
        setDeviceType(item.getDeviceType());
        setHistoryData(item.getHistoryData());

        mHubId = item.getHubId();
    }

    public DeviceHistoryItemApp(Context context, DeviceHistory deviceHistory, Integer hubId) {
        mContext = context;

        setDeviceId(deviceHistory.getDeviceId());
        setDeviceType(deviceHistory.getDeviceType());
        setHistoryData(deviceHistory.getHistoryData());

        mHubId = hubId;
    }

    public DeviceHistoryItemApp(Context context) {
        super();

        mContext = context;

        setDeviceId("");
        setDeviceType("");
        setHistoryData(new ArrayList<DeviceHistoryData>());

        this.mHubId = -1;

    }

    public DeviceHistoryItemApp(Context context, String deviceId, Integer hubId, String deviceType, ArrayList<DeviceHistoryData> deviceHistoryData) {
        super();

        mContext = context;

        setDeviceId(deviceId);
        setDeviceType(deviceType);
        setHistoryData(deviceHistoryData);

        this.mHubId = hubId;


    }

    public Integer getHubId() {
        return mHubId;
    }

    public void setHubId(Integer hubId) {
        this.mHubId = hubId;
    }

    @Override
    public String toString() {
        return "DeviceHistoryItemApp{" +
                "deviceHistory=" + super.toString() +
                ", mHubId=" + mHubId +
                ", mContext=" + mContext +
                '}';
    }
}
