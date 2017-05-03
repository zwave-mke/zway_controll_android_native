package de.pathec.hubapp;

import android.app.Application;
import android.os.StrictMode;

import de.pathec.hubapp.db.DatabaseHandler;

public class HubAppApplication extends Application {
    private DatabaseHandler mDatabaseHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mDatabaseHandler = DatabaseHandler.getInstance(this);
    }
}
