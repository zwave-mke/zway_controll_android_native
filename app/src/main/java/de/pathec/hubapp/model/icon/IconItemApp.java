package de.pathec.hubapp.model.icon;

import android.content.Context;

import de.fh_zwickau.informatik.sensor.model.icons.Icon;

public class IconItemApp extends Icon {

    private Integer mHubId;
    private String mIconPath;

    private Context mContext;

    public IconItemApp(Context context, IconItemApp item) {
        super();

        mContext = context;

        mHubId = item.getHubId();
        mIconPath = item.getIconPath();

        setFile(item.getFile());
        setOrgFile(item.getOrgFile());
        setSource(item.getSource());
        setName(item.getName());
        setId(item.getId());
        setTimestamp(item.getTimestamp());
        setSourceTitle(item.getSourceTitle());

    }

    public IconItemApp(Context context, Icon icon, Integer hubId) {
        mContext = context;

        mHubId = hubId;
        mIconPath = "";

        setFile(icon.getFile());
        setOrgFile(icon.getOrgFile());
        setSource(icon.getSource());
        setName(icon.getName());
        setId(icon.getId());
        setTimestamp(icon.getTimestamp());
        setSourceTitle(icon.getSourceTitle());
    }

    public IconItemApp(Context context) {
        super();

        mContext = context;

        this.mHubId = -1;
        this.mIconPath = "";

        setFile("");
        setOrgFile("");
        setSource("");
        setName("");
        setId("");
        setTimestamp(-1l);
        setSourceTitle("");
    }

    public IconItemApp(Context context, Integer hubId, String iconPath, String file, String orgFile, String source, String name, String id, Long timestamp, String sourceTitle) {
        super();

        mContext = context;

        this.mHubId = hubId;
        this.mIconPath = iconPath;

        setFile(file);
        setOrgFile(orgFile);
        setSource(source);
        setName(name);
        setId(id);
        setTimestamp(timestamp);
        setSourceTitle(sourceTitle);

    }

    public Integer getHubId() {
        return mHubId;
    }

    public void setHubId(Integer hubId) {
        this.mHubId = hubId;
    }

    public String getIconPath() { return mIconPath; }

    public void setIconPath(String iconPath) { this.mIconPath = iconPath; }

    @Override
    public String toString() {
        return "IconItemApp{" +
                "location=" + super.toString() +
                ", mHubId=" + mHubId +
                ", mIcon='" + mIconPath + '\'' +
                '}';
    }
}
