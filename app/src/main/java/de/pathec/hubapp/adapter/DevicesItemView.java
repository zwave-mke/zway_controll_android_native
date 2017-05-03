package de.pathec.hubapp.adapter;

import de.pathec.hubapp.model.device.DeviceItemApp;

public class DevicesItemView {
    private DeviceItemApp mDeviceItem;
    private Integer mStatus;
    private Boolean mFrontViewVisible;

    public DevicesItemView(DeviceItemApp deviceItem, Integer status, Boolean frontViewVisible) {
        mDeviceItem = deviceItem;
        mStatus = status;
        mFrontViewVisible = frontViewVisible;
    }

    public DeviceItemApp getDeviceItem() {
        return mDeviceItem;
    }

    public void setDeviceItem(DeviceItemApp deviceItem) {
        this.mDeviceItem = deviceItem;
    }

    public Integer getStatus() {
        return mStatus;
    }

    public void setStatus(Integer status) {
        this.mStatus = status;
    }

    public Boolean getFrontViewVisible() { return mFrontViewVisible; }

    public void setFrontViewVisible(Boolean frontView) { this.mFrontViewVisible = frontView; }
}
