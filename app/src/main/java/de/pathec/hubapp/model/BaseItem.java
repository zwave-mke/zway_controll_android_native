package de.pathec.hubapp.model;

import android.content.Context;

import java.util.Date;

public abstract class BaseItem {
    private Integer iId;
    private Date dCreated;
    private Date dModified;

    protected Context mContext;

    public BaseItem(Context context, BaseItem item) {
        this.iId = item.getId();
        this.dCreated = item.getCreated();
        this.dModified = item.getModified();

        mContext = context;
    }

    public BaseItem(Context context, Integer iId, Date dCreated, Date dModified) {
        this.iId = iId;
        this.dCreated = dCreated;
        this.dModified = dModified;

        mContext = context;
    }

    public BaseItem(Context context) {
        this.iId = -1;
        this.dCreated = new Date();
        this.dModified = new Date();

        mContext = context;
    }

    public Integer getId() {
        return iId;
    }

    public void setId(Integer iId) {
        this.iId = iId;
    }

    public Date getCreated() {
        return dCreated;
    }

    public void setCreated(Date dCreated) {
        this.dCreated = dCreated;
    }

    public Date getModified() {
        return dModified;
    }

    public void setModified(Date dModified) {
        this.dModified = dModified;
    }
}
