package de.pathec.hubapp.model.profile;

import android.content.Context;

import java.util.HashSet;

import de.fh_zwickau.informatik.sensor.model.profiles.Profile;

public class ProfileItemApp extends Profile {

    private Integer mHubId;

    private Context mContext;

    public ProfileItemApp(Context context, ProfileItemApp item) {
        super();

        mContext = context;

        setId(item.getId());
        mHubId = item.getHubId();

        setName(item.getName());
        setLang(item.getLang());
        setColor(item.getColor());
        setDashboard(item.getDashboard());
        setInterval(item.getInterval());
        setRooms(item.getRooms());
        setExpertView(item.getExpertView());
        setHideAllDeviceEvents(item.getHideAllDeviceEvents());
        setHideSystemEvents(item.getHideSystemEvents());
        setHideSingleDeviceEvents(item.getHideSingleDeviceEvents());
        setEmail(item.getEmail());
    }

    public ProfileItemApp(Context context, Profile profile, Integer hubId) {
        mContext = context;

        setId(profile.getId());
        setName(profile.getName());
        setLang(profile.getLang());
        setColor(profile.getColor());
        setDashboard(profile.getDashboard());
        setInterval(profile.getInterval());
        setRooms(profile.getRooms());
        setExpertView(profile.getExpertView());
        setHideAllDeviceEvents(profile.getHideAllDeviceEvents());
        setHideSystemEvents(profile.getHideSystemEvents());
        setHideSingleDeviceEvents(profile.getHideSingleDeviceEvents());
        setEmail(profile.getEmail());

        this.mHubId = hubId;
    }

    public ProfileItemApp(Context context) {
        super();

        mContext = context;

        setId(-1);
        this.mHubId = -1;

        setName("");
        setLang("");
        setColor("");
        setDashboard(new HashSet<String>());
        setInterval(-1);
        setRooms(new HashSet<Integer>());
        setExpertView(false);
        setHideAllDeviceEvents(false);
        setHideSystemEvents(false);
        setHideSingleDeviceEvents(new HashSet<String>());
        setEmail("");
    }

    public ProfileItemApp(Context context, Integer id, Integer hubId, String name, String lang,
                          String color, HashSet<String> dashboard, Integer interval,
                          HashSet<Integer> rooms, Boolean expertView, Boolean hideAllDeviceEvents,
                          Boolean hideSystemEvents, HashSet<String> hideSingleDeviceEvents,
                          String email) {
        super();

        mContext = context;

        setId(id);
        this.mHubId = hubId;

        setName(name);
        setLang(lang);
        setColor(color);
        setDashboard(dashboard);
        setInterval(interval);
        setRooms(rooms);
        setExpertView(expertView);
        setHideAllDeviceEvents(hideAllDeviceEvents);
        setHideSystemEvents(hideSystemEvents);
        setHideSingleDeviceEvents(hideSingleDeviceEvents);
        setEmail(email);
    }

    public Integer getHubId() {
        return mHubId;
    }

    public void setHubId(Integer hubId) {
        this.mHubId = hubId;
    }

    @Override
    public String toString() {
        return "ProfileItemApp{" +
                "profile=" + super.toString() +
                ", mHubId=" + mHubId +
                ", mContext=" + mContext +
                '}';
    }
}
