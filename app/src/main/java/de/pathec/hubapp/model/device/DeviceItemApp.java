package de.pathec.hubapp.model.device;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.fh_zwickau.informatik.sensor.model.devices.Color;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.Icons;
import de.fh_zwickau.informatik.sensor.model.devices.Metrics;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.location.LocationListApp;

public class DeviceItemApp extends Device {

    private Integer mHubId;
    private String mIcon;
    private Integer mOrder;

    // Transient fields
    private LocationItemApp mLocationItem;

    private Context mContext;

    public DeviceItemApp(DeviceItemApp item) {
        super();

        mContext = item.getContext();

        setCreationTime(item.getCreationTime());
        setCreatorId(item.getCreatorId());
        setDeviceType(item.getDeviceType());
        setH(item.getH());
        setHasHistory(item.getHasHistory());
        setDeviceId(item.getDeviceId());
        setLocation(item.getLocation());
        setPermanentlyHidden(item.getPermanentlyHidden());
        setProbeType(item.getProbeType());
        setVisibility(item.getVisibility());
        setUpdateTime(item.getUpdateTime());

        Metrics metrics = new Metrics();
        metrics.setIcon(item.getMetrics().getIcon());
        metrics.setTitle(item.getMetrics().getTitle());
        metrics.setLevel(item.getMetrics().getLevel());
        metrics.setProbeTitle(item.getMetrics().getProbeTitle());
        metrics.setScaleTitle(item.getMetrics().getScaleTitle());

        Color color = new Color();
        color.setRed(item.getMetrics().getColor().getRed());
        color.setGreen(item.getMetrics().getColor().getGreen());
        color.setBlue(item.getMetrics().getColor().getBlue());

        metrics.setColor(color);
        metrics.setMin(item.getMetrics().getMin());
        metrics.setMax(item.getMetrics().getMax());

        metrics.setCameraStreamUrl(item.getMetrics().getCameraStreamUrl());
        metrics.setCameraHasZoomIn(item.getMetrics().getCameraHasZoomIn());
        metrics.setCameraHasZoomOut(item.getMetrics().getCameraHasZoomOut());
        metrics.setCameraHasLeft(item.getMetrics().getCameraHasLeft());
        metrics.setCameraHasRight(item.getMetrics().getCameraHasRight());
        metrics.setCameraHasUp(item.getMetrics().getCameraHasUp());
        metrics.setCameraHasDown(item.getMetrics().getCameraHasDown());
        metrics.setCameraHasOpen(item.getMetrics().getCameraHasOpen());
        metrics.setCameraHasClose(item.getMetrics().getCameraHasClose());

        metrics.setDiscreteCurrentScene(item.getMetrics().getDiscreteCurrentScene());
        metrics.setDiscreteKeyAttribute(item.getMetrics().getDiscreteKeyAttribute());
        metrics.setDiscreteState(item.getMetrics().getDiscreteState());
        metrics.setDiscreteMaxScenes(item.getMetrics().getDiscreteMaxScenes());
        metrics.setDiscreteCount(item.getMetrics().getDiscreteCount());
        metrics.setDiscreteType(item.getMetrics().getDiscreteType());

        metrics.setText(item.getMetrics().getText());

        setMetrics(metrics);
        setTags(item.getTags());

        setIcons(item.getIcons());

        mHubId = item.getHubId();
        mIcon = item.getIcon();
        mOrder = item.getOrder();
    }

    public DeviceItemApp(Context context, Device device, Integer hubId) {
        mContext = context;

        setCreationTime(device.getCreationTime());
        setCreatorId(device.getCreatorId());
        setDeviceType(device.getDeviceType());
        setH(device.getH());
        setHasHistory(device.getHasHistory());
        setDeviceId(device.getDeviceId());
        setLocation(device.getLocation());
        setPermanentlyHidden(device.getPermanentlyHidden());
        setProbeType(device.getProbeType());
        setVisibility(device.getVisibility());
        setUpdateTime(device.getUpdateTime());

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

        setMetrics(metrics);
        setTags(device.getTags());
        setIcons(device.getIcons());

        setHubId(hubId);
        this.mIcon = "";
        this.mOrder = 0;
    }

    public DeviceItemApp(Context context) {
        super();

        mContext = context;

        setCreationTime(-1);
        setCreatorId(-1);
        setDeviceType("");
        setH(-1);
        setHasHistory(false);
        setDeviceId("");
        setLocation(-1);
        setPermanentlyHidden(false);
        setProbeType("");
        setVisibility(false);
        setUpdateTime(-1);

        Metrics metrics = new Metrics();
        metrics.setIcon("");
        metrics.setTitle("");
        metrics.setLevel("");
        metrics.setProbeTitle("");
        metrics.setScaleTitle("");

        Color color = new Color();
        color.setRed(-1);
        color.setGreen(-1);
        color.setBlue(-1);

        metrics.setColor(color);
        metrics.setMin(-1);
        metrics.setMax(-1);

        metrics.setCameraStreamUrl("");
        metrics.setCameraHasZoomIn(false);
        metrics.setCameraHasZoomOut(false);
        metrics.setCameraHasLeft(false);
        metrics.setCameraHasRight(false);
        metrics.setCameraHasUp(false);
        metrics.setCameraHasDown(false);
        metrics.setCameraHasOpen(false);
        metrics.setCameraHasClose(false);

        metrics.setDiscreteCurrentScene(0);
        metrics.setDiscreteKeyAttribute(0);
        metrics.setDiscreteState("");
        metrics.setDiscreteMaxScenes(0);
        metrics.setDiscreteCount(0);
        metrics.setDiscreteType("");

        metrics.setText("");

        setMetrics(metrics);
        setTags(new ArrayList<String>());
        setIcons(new Icons());

        this.mHubId = -1;
        this.mIcon = "";
        this.mOrder = 0;
    }

    public DeviceItemApp(Context context,
                         Integer creationTime,
                         Integer creatorId,
                         String deviceType,
                         Integer h,
                         Boolean hasHistory,
                         String deviceId,
                         Integer location,
                         Boolean permanentlyHidden,
                         String probeType,
                         Boolean visibility,
                         Integer updateTime,
                         String metricsIcon,
                         String metricsTitle,
                         String metricsLevel,
                         String metricsProbeTitle,
                         String metricsScaleTitle,
                         Integer metricsColorRed,
                         Integer metricsColorGreen,
                         Integer metricsColorBlue,
                         Integer metricsMin,
                         Integer metricsMax,
                         String metricsCameraStreamUrl,
                         Boolean metricsCameraHasZoomIn,
                         Boolean metricsCameraHasZoomOut,
                         Boolean metricsCameraHasLeft,
                         Boolean metricsCameraHasRight,
                         Boolean metricsCameraHasUp,
                         Boolean metricsCameraHasDown,
                         Boolean metricsCameraHasOpen,
                         Boolean metricsCameraHasClose,
                         Integer metricsDiscreteCurrentScene,
                         Integer metricsDiscreteKeyAttribute,
                         String metricsDiscreteState,
                         Integer metricsDiscreteMaxScenes,
                         Integer metricsDiscreteCount,
                         String metricsDiscreteType,
                         String metricsText,
                         List<String> tags,
                         Icons customIcons,
                         Integer hubId,
                         String icon,
                         Integer order) {
        super();

        mContext = context;

        setCreationTime(creationTime);
        setCreatorId(creatorId);
        setDeviceType(deviceType);
        setH(h);
        setHasHistory(hasHistory);
        setDeviceId(deviceId);
        setLocation(location);
        setPermanentlyHidden(permanentlyHidden);
        setProbeType(probeType);
        setVisibility(visibility);
        setUpdateTime(updateTime);

        Metrics metrics = new Metrics();
        metrics.setIcon(metricsIcon);
        metrics.setTitle(metricsTitle);
        metrics.setLevel(metricsLevel);
        metrics.setProbeTitle(metricsProbeTitle);
        metrics.setScaleTitle(metricsScaleTitle);

        Color color = new Color();
        color.setRed(metricsColorRed);
        color.setGreen(metricsColorGreen);
        color.setBlue(metricsColorBlue);

        metrics.setColor(color);
        metrics.setMin(metricsMin);
        metrics.setMax(metricsMax);

        metrics.setCameraStreamUrl(metricsCameraStreamUrl);
        metrics.setCameraHasZoomIn(metricsCameraHasZoomIn);
        metrics.setCameraHasZoomOut(metricsCameraHasZoomOut);
        metrics.setCameraHasLeft(metricsCameraHasLeft);
        metrics.setCameraHasRight(metricsCameraHasRight);
        metrics.setCameraHasUp(metricsCameraHasUp);
        metrics.setCameraHasDown(metricsCameraHasDown);
        metrics.setCameraHasOpen(metricsCameraHasOpen);
        metrics.setCameraHasClose(metricsCameraHasClose);

        metrics.setDiscreteCurrentScene(metricsDiscreteCurrentScene);
        metrics.setDiscreteKeyAttribute(metricsDiscreteKeyAttribute);
        metrics.setDiscreteState(metricsDiscreteState);
        metrics.setDiscreteMaxScenes(metricsDiscreteMaxScenes);
        metrics.setDiscreteCount(metricsDiscreteCount);
        metrics.setDiscreteType(metricsDiscreteType);

        metrics.setText(metricsText);

        setMetrics(metrics);
        setTags(tags);
        setIcons(customIcons);

        this.mHubId = hubId;
        this.mIcon = icon;
        this.mOrder = order;

    }

    public Integer getHubId() {
        return mHubId;
    }

    public void setHubId(Integer hubId) {
        this.mHubId = hubId;
    }

    public String getIcon() {
        return mIcon;
    }

    public Bitmap getIconBitmap() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap iconBitmap = Bitmap.createBitmap(1, 1, conf);

        File iconFile = new File(mIcon);
        if (iconFile.exists()) {
            iconBitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
        }
        return iconBitmap;
    }

    public void setIcon(String icon) {
        this.mIcon = icon;
    }

    public Integer getOrder() {
        return mOrder;
    }

    public void setOrder(Integer order) {
        this.mOrder = order;
    }

    public LocationItemApp getLocationItem() {
        if (mLocationItem == null) {
            LocationListApp locationListApp = new LocationListApp(mContext, null, null);
            mLocationItem = locationListApp.getLocationItem(getLocation(), getHubId());
        }

        return mLocationItem;
    }

    public void setLocationItem(LocationItemApp locationItem) { this.mLocationItem = locationItem; }

    public Context getContext() { return mContext; }

    @Override
    public String toString() {
        return "DeviceItemApp{" +
                "device=" + super.toString() +
                ",mHubId=" + mHubId +
                ", mIcon='" + mIcon + '\'' +
                ", mOrder=" + mOrder +
                '}';
    }
}
