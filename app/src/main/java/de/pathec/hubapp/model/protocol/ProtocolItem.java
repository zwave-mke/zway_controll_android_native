package de.pathec.hubapp.model.protocol;

import android.content.Context;

import java.util.Date;

import de.pathec.hubapp.model.BaseItem;

public class ProtocolItem extends BaseItem {
    private String mStatus; // Info, Success, Error, Warning
    private String mText;
    private String mCategory; // System

    public ProtocolItem(Context context) {
        super(context);
    }

    public ProtocolItem(Context context, String sStatus, String sText, String sCategory) {
        super(context);

        this.mStatus = sStatus;
        this.mText = sText;
        this.mCategory = sCategory;
    }

    public ProtocolItem(Context context, Integer id, Date created, Date modified, String status, String text, String category) {
        super(context, id, created, modified);

        this.mStatus = status;
        this.mText = text;
        this.mCategory = category;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    @Override
    public String toString() {
        return "ProtocolItem{" +
                "mStatus='" + mStatus + '\'' +
                ", mText='" + mText + '\'' +
                ", mCategory='" + mCategory + '\'' +
                '}';
    }
}
