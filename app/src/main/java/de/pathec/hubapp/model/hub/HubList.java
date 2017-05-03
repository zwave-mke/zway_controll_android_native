package de.pathec.hubapp.model.hub;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.events.ModelDeleteEvent;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.util.Params;

public class HubList {
    private Context mContext;

    private DatabaseHandler mDatabaseHandler;

    public HubList(Context context) {
        mContext = context;

        // SQLite
        mDatabaseHandler = DatabaseHandler.getInstance(mContext);
    }

    public ArrayList<HubItem> getHubList() {
        return mDatabaseHandler.getAllHub();
    }

    public HubItem addHubItem(HubItem item) {
        Long id = mDatabaseHandler.addHub(item);
        item.setId(id.intValue());

        SharedPreferences settings = mContext.getSharedPreferences(Params.PREFS_NAME, 0);
        settings.edit().putBoolean(Params.PREFS_HUB_CONFIGURED, true).apply();

        BusProvider.postOnMain(new ModelAddEvent<>("Hub", item));

        return item;
    }

    public HubItem getHubItem(Integer id) {
        return mDatabaseHandler.getHub(id);
    }

    public boolean existHubItem(HubItem item) {
        return mDatabaseHandler.existHub(item);
    }

    public void updateHubItem(HubItem item) {
        mDatabaseHandler.updateHub(item);

        BusProvider.postOnMain(new ModelUpdateEvent<>("Hub", item));
    }

    public void deleteHubItem(HubItem item) {
        mDatabaseHandler.deleteHub(item.getId());

        // Change preference for app startup
        SharedPreferences settings = mContext.getSharedPreferences(Params.PREFS_NAME, 0);
        if(mDatabaseHandler.getHubCount() == 0) {
            settings.edit().putBoolean(Params.PREFS_HUB_CONFIGURED, false).apply();
        }

        if (settings.getInt(Params.PREFS_ACTIVE_HUB, 0) == item.getId()
                && mDatabaseHandler.getHubCount() != 0) { // Change preference for active hub
            settings.edit().putInt(Params.PREFS_ACTIVE_HUB, getHubList().get(0).getId()).apply();
        }

        BusProvider.postOnMain(new ModelDeleteEvent<>("Hub", item));
    }

    public Integer getHubCount() {
        return mDatabaseHandler.getHubCount();
    }
}
