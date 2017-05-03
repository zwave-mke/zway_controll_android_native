package de.pathec.hubapp.util;

public class ZWayHubConfiguration {
    private String mUsername;
    private String mPassword;
    private String mRemoteService;
    private Integer mRemoteId;

    private String mLocalIp;

    // Hub's wifi
    private String mHubSSID;
    private String mHubWifiPassword;
    private String mHubIP;

    // Location
    private String mLocation;
    private Double mLongitude;
    private Double mLatitude;

    public ZWayHubConfiguration() {
        this.mUsername = "";
        this.mPassword = "";
        this.mRemoteService = "find.z-wave.me";
        this.mHubSSID = "";
        this.mHubWifiPassword = "";
        this.mHubIP = "192.168.115.1";
        this.mRemoteId = 0;
        this.mLocalIp = "";
        this.mLocation = "";
        this.mLongitude = 0.0;
        this.mLatitude = 0.0;

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

    public String getLocalIp() {
        return mLocalIp;
    }

    public void setLocalIp(String localIp) {
        this.mLocalIp = localIp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZWayHubConfiguration that = (ZWayHubConfiguration) o;

        if (mUsername != null ? !mUsername.equals(that.mUsername) : that.mUsername != null)
            return false;
        if (mPassword != null ? !mPassword.equals(that.mPassword) : that.mPassword != null)
            return false;
        if (mRemoteService != null ? !mRemoteService.equals(that.mRemoteService) : that.mRemoteService != null)
            return false;
        if (mRemoteId != null ? !mRemoteId.equals(that.mRemoteId) : that.mRemoteId != null)
            return false;
        if (mLocalIp != null ? !mLocalIp.equals(that.mLocalIp) : that.mLocalIp != null)
            return false;
        if (mHubSSID != null ? !mHubSSID.equals(that.mHubSSID) : that.mHubSSID != null)
            return false;
        if (mHubWifiPassword != null ? !mHubWifiPassword.equals(that.mHubWifiPassword) : that.mHubWifiPassword != null)
            return false;
        if (mHubIP != null ? !mHubIP.equals(that.mHubIP) : that.mHubIP != null) return false;
        if (mLocation != null ? !mLocation.equals(that.mLocation) : that.mLocation != null)
            return false;
        if (mLongitude != null ? !mLongitude.equals(that.mLongitude) : that.mLongitude != null)
            return false;
        return mLatitude != null ? mLatitude.equals(that.mLatitude) : that.mLatitude == null;

    }

    @Override
    public int hashCode() {
        int result = mUsername != null ? mUsername.hashCode() : 0;
        result = 31 * result + (mPassword != null ? mPassword.hashCode() : 0);
        result = 31 * result + (mRemoteService != null ? mRemoteService.hashCode() : 0);
        result = 31 * result + (mRemoteId != null ? mRemoteId.hashCode() : 0);
        result = 31 * result + (mLocalIp != null ? mLocalIp.hashCode() : 0);
        result = 31 * result + (mHubSSID != null ? mHubSSID.hashCode() : 0);
        result = 31 * result + (mHubWifiPassword != null ? mHubWifiPassword.hashCode() : 0);
        result = 31 * result + (mHubIP != null ? mHubIP.hashCode() : 0);
        result = 31 * result + (mLocation != null ? mLocation.hashCode() : 0);
        result = 31 * result + (mLongitude != null ? mLongitude.hashCode() : 0);
        result = 31 * result + (mLatitude != null ? mLatitude.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZWayHubConfiguration{" +
                "mUsername='" + mUsername + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mRemoteService='" + mRemoteService + '\'' +
                ", mRemoteId=" + mRemoteId +
                ", mLocalIp='" + mLocalIp + '\'' +
                ", mHubSSID='" + mHubSSID + '\'' +
                ", mHubWifiPassword='" + mHubWifiPassword + '\'' +
                ", mHubIP='" + mHubIP + '\'' +
                ", mLocation='" + mLocation + '\'' +
                ", mLongitude=" + mLongitude +
                ", mLatitude=" + mLatitude +
                '}';
    }
}
