package de.pathec.hubapp.model.notification;

import android.content.Context;

import java.util.Date;

import de.fh_zwickau.informatik.sensor.model.notifications.Message;
import de.fh_zwickau.informatik.sensor.model.notifications.Notification;

public class NotificationItemApp extends Notification {

    private Integer mHubId;

    private Context mContext;

    public NotificationItemApp(Context context, NotificationItemApp item) {
        super();

        mContext = context;

        setId(item.getId());
        mHubId = item.getHubId();

        setTimestamp(item.getTimestamp());
        setLevel(item.getLevel());

        Message message = new Message();
        message.setDev(item.getMessage().getDev());
        message.setL(item.getMessage().getL());
        setMessage(message);

        setType(item.getType());
        setSource(item.getSource());
    }

    public NotificationItemApp(Context context, Notification notification, Integer hubId) {
        mContext = context;

        setId(notification.getId());
        setHubId(hubId);

        setTimestamp(notification.getTimestamp());
        setLevel(notification.getLevel());

        Message message = new Message();
        message.setDev(notification.getMessage().getDev());
        message.setL(notification.getMessage().getL());
        setMessage(message);

        setType(notification.getType());
        setSource(notification.getSource());
    }

    public NotificationItemApp(Context context) {
        super();

        mContext = context;

        setId(-1l);
        this.mHubId = -1;

        setTimestamp(new Date());
        setLevel("");
        setMessage(new Message());
        setType("");
        setSource("");
    }

    public NotificationItemApp(Context context, Long id, Integer hubId, Date timestamp, String level, String messageDev, String messageL, String type, String source) {
        super();

        mContext = context;

        setId(id);
        this.mHubId = hubId;

        setTimestamp(timestamp);
        setLevel(level);

        Message message = new Message();
        message.setDev(messageDev);
        message.setL(messageL);
        setMessage(message);

        setType(type);
        setSource(source);
    }

    public Integer getHubId() {
        return mHubId;
    }

    public void setHubId(Integer hubId) {
        this.mHubId = hubId;
    }

    @Override
    public String toString() {
        return "LocationItemApp{" +
                "notification=" + super.toString() +
                ", mHubId=" + mHubId +
                ", mContext=" + mContext +
                '}';
    }
}
