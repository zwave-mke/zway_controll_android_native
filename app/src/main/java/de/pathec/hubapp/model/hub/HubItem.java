package de.pathec.hubapp.model.hub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.Date;

import de.pathec.hubapp.model.BaseItem;

public class HubItem extends BaseItem {

    private String mTitle;
    private String mTile;

    private String mUsername;
    private String mPassword;
    private String mRemoteService;
    private Integer mRemoteId;

    // Local network access
    private String mLocalIP;

    // Hub's wifi
    private String mHubSSID;
    private String mHubWifiPassword;
    private String mHubIP;

    // Location
    private String mLocation;
    private Double mLongitude;
    private Double mLatitude;

    private String mPresenceDeviceId;

    public HubItem(Context context, HubItem item) {
        super(context, item);

        this.mTitle = item.getTitle();
        this.mTile = item.getTile();
        this.mRemoteService = item.getRemoteService();
        this.mRemoteId = item.getRemoteId();
        this.mUsername = item.getUsername();
        this.mPassword = item.getPassword();
        this.mLocalIP = item.getLocalIP();
        this.mHubSSID = item.getHubSSID();
        this.mHubWifiPassword = item.getHubWifiPassword();
        this.mHubIP = item.getHubIP();
        this.mLocation = item.getLocation();
        this.mLongitude = item.getLongitude();
        this.mLatitude = item.getLatitude();
        this.mPresenceDeviceId = item.getPresenceDeviceId();
    }

    public HubItem(Context context) {
        super(context);
    }

    public HubItem(Context context, String title, String tile, String username, String password,
                   String remoteService, Integer remoteId, String localIP, String hubSSID,
                   String hubWifiPassword, String hubIP, String location, Double longitude,
                   Double latitude, String presenceDeviceId) {
        super(context);

        this.mTitle = title;
        this.mTile = tile;
        this.mRemoteService = remoteService;
        this.mRemoteId = remoteId;
        this.mUsername = username;
        this.mPassword = password;
        this.mLocalIP = localIP;
        this.mHubSSID = hubSSID;
        this.mHubWifiPassword = hubWifiPassword;
        this.mHubIP = hubIP;
        this.mLocation = location;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mPresenceDeviceId = presenceDeviceId;
    }

    public HubItem(Context context, Integer id, Date created, Date modified, String title,
                   String tile, String username, String password, String remoteService,
                   Integer remoteId, String localIP, String hubSSID, String hubWifiPassword,
                   String hubIP, String location, Double longitude, Double latitude, String presenceDeviceId) {
        super(context, id, created, modified);

        this.mTitle = title;
        this.mTile = tile;
        this.mRemoteService = remoteService;
        this.mRemoteId = remoteId;
        this.mUsername = username;
        this.mPassword = password;
        this.mLocalIP = localIP;
        this.mHubSSID = hubSSID;
        this.mHubWifiPassword = hubWifiPassword;
        this.mHubIP = hubIP;
        this.mLocation = location;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mPresenceDeviceId = presenceDeviceId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getTile() {
        return mTile;
    }

    public void setTile(String tile) {
        this.mTile = tile;
    }

    public Bitmap getTileBitmap() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap tileBitmap = Bitmap.createBitmap(1, 1, conf);

        File tileFile = new File(mTile);
        if (tileFile.exists()) {
            tileBitmap = BitmapFactory.decodeFile(tileFile.getAbsolutePath());
        }
        return tileBitmap;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public String getLocalIP() {
        return mLocalIP;
    }

    public void setLocalIP(String localIP) {
        this.mLocalIP = localIP;
    }

    public String getRemoteService() {
        return mRemoteService;
    }

    public void setRemoteService(String remoteService) {
        this.mRemoteService = remoteService;
    }

    public Integer getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Integer remoteId) {
        this.mRemoteId = remoteId;
    }

    public String getHubSSID() {
        return mHubSSID;
    }

    public void setHubSSID(String hubSSID) {
        this.mHubSSID = hubSSID;
    }

    public String getHubWifiPassword() {
        return mHubWifiPassword;
    }

    public void setHubWifiPassword(String hubWifiPassword) {
        this.mHubWifiPassword = hubWifiPassword;
    }

    public String getHubIP() {
        return mHubIP;
    }

    public void setHubIP(String hubIP) {
        this.mHubIP = hubIP;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        this.mLongitude = longitude;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Double latitude) {
        this.mLatitude = latitude;
    }

    public void setPresenceDeviceId(String presenceDeviceId) {
        this.mPresenceDeviceId = presenceDeviceId;
    }

    public String getPresenceDeviceId() {
        return mPresenceDeviceId;
    }

    @Override
    public String toString() {
        return "HubItem{" +
                "mTitle='" + mTitle + '\'' +
                ", mTile='" + mTile + '\'' +
                ", mUsername='" + mUsername + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mRemoteService='" + mRemoteService + '\'' +
                ", mRemoteId=" + mRemoteId +
                ", mLocalIP='" + mLocalIP + '\'' +
                ", mHubSSID='" + mHubSSID + '\'' +
                ", mHubWifiPassword='" + mHubWifiPassword + '\'' +
                ", mHubIP='" + mHubIP + '\'' +
                ", mLocation='" + mLocation + '\'' +
                ", mLongitude=" + mLongitude +
                ", mLatitude=" + mLatitude +
                ", mPresenceDeviceId=" + mPresenceDeviceId +
                '}';
    }
}
