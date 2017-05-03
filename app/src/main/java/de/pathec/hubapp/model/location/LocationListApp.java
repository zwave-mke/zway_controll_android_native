package de.pathec.hubapp.model.location;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.locations.Location;
import de.fh_zwickau.informatik.sensor.model.locations.LocationList;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.model.profile.ProfileListApp;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;

public class LocationListApp {
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;
    private OnLocationListInteractionListener mCaller;

    private HubConnectionHolder mHubConnectionHolder;

    private AsyncTask<Integer, Void, ZWayResponse> mLoader;
    private Boolean mLoaderActive;

    public LocationListApp(Context context, OnLocationListInteractionListener caller, HubConnectionHolder hubConnectionHolder) {
        mContext = context;
        mCaller = caller;
        mHubConnectionHolder = hubConnectionHolder;

        // SQLite
        mDatabaseHandler = DatabaseHandler.getInstance(mContext);
    }

    public void setHubConnectionHolder(HubConnectionHolder hubConnectionHolder) {
        mHubConnectionHolder = hubConnectionHolder;
    }

    public void cancelSynchronization() {
        if (mLoader != null && mLoaderActive) {
            Log.i(Params.LOGGING_TAG, "(LocationListApp) Cancel synchronization.");
            mLoader.cancel(true);
        }
    }

    public void getLocationList(final Integer hubId, Boolean loadFromHub) {
        if (loadFromHub) {
            ProfileListApp profileListApp = new ProfileListApp(mContext, null, null);
            profileListApp.loadAndSaveProfileList(hubId, mHubConnectionHolder.getZWayApi(), true);

            Log.i(Params.LOGGING_TAG, "(LocationListApp) Loading data from Hub");
            mLoader = new StartLoadingLocation().execute(hubId);
        } else {
            Log.i(Params.LOGGING_TAG, "(LocationListApp) Loading data from SQLite database.");
            mCaller.onLocationListLoaded(mDatabaseHandler.getAllLocation(hubId), false);
        }
    }

    private class StartLoadingLocation extends AsyncTask<Integer, Void, ZWayResponse> {
        @Override
        protected void onPreExecute() {
            Log.i(Params.LOGGING_TAG, "Start loading locations: " + Params.formatDateTime(new Date()));
            mLoaderActive = true;
        }

        @Override
        protected ZWayResponse doInBackground(Integer... params) {
            if(mHubConnectionHolder != null && params[0] != null) {
                ArrayList<LocationItemApp> locationList = loadAndSaveLocationList(params[0], null, false);

                if (locationList != null) {
                    return new ZWayResponse(true, locationList);
                }
            }

            return new ZWayResponse(false, mDatabaseHandler.getAllLocation(params[0]));
        }

        @Override
        protected void onPostExecute(ZWayResponse result) {
            Log.i(Params.LOGGING_TAG, "Finish loading locations: " + Params.formatDateTime(new Date()));
            mLoaderActive = false;

            if (!result.dataUpdated) {
                Log.w(Params.LOGGING_TAG, "(LocationListApp) Z-Way not connected!");
                mCaller.onLocationListError(mContext.getString(R.string.connection_not_connected_update));
            }
            mCaller.onLocationListLoaded(result.locationList, true);
        }
    }

    /**
     * @param hubId Hub id to associate the correct database entries.
     * @param zwayApi If not null use this instance for communication, otherwise use connection
     *                of list instance.
     * @param backgroundOperation Returns only a empty list (not null) if operation successfully performed.
     * @return List of profiles, empty list if background operation or null if anything goes wrong.
     */
    public ArrayList<LocationItemApp> loadAndSaveLocationList(Integer hubId, IZWayApi zwayApi, Boolean backgroundOperation) {
        LocationList locationList;
        if (zwayApi == null) {
            locationList = mHubConnectionHolder.getLocations();
        } else {
            locationList = zwayApi.getLocations();
        }

        // Iterate over locations from Hub
        if (locationList != null) {
            Set<Integer> locationIds = new HashSet<>();

            for(Location location : locationList.getLocations()) {
                locationIds.add(location.getId());

                ColorGenerator generator = ColorGenerator.MATERIAL;
                TextDrawable tileDrawable = TextDrawable.builder().buildRound(location.getTitle().substring(0, 1).toUpperCase(), generator.getColor(location.getTitle()));
                Bitmap tileBitmap = Util.drawableTileToBitmap(tileDrawable);
                String tileFile = Util.saveImage(mContext, tileBitmap, "location_" + location.getTitle(), "png");

                LocationItemApp locationItemApp = mDatabaseHandler.getLocation(location.getId(), hubId);

                if (locationItemApp.getId() != -1) {
                    locationItemApp.setTitle(location.getTitle());
                    locationItemApp.setTile(mContext.getFilesDir().getPath() + "/" + tileFile);
                    locationItemApp.setUserImg(location.getUserImg());

                    mDatabaseHandler.updateLocation(locationItemApp);

                    BusProvider.postOnMain(new ModelUpdateEvent<>("Location", locationItemApp));
                } else {
                    LocationItemApp newLocationItemApp = new LocationItemApp(mContext, location, hubId);

                    newLocationItemApp.setTile(mContext.getFilesDir().getPath() + "/" + tileFile);

                    mDatabaseHandler.addLocation(newLocationItemApp);

                    BusProvider.postOnMain(new ModelAddEvent<>("Location", newLocationItemApp));
                }
            }

            // Delete locations
            ArrayList<LocationItemApp> locations = mDatabaseHandler.getAllLocation(hubId);
            for (LocationItemApp location : locations) {
                if (!locationIds.contains(location.getId())) {
                    mDatabaseHandler.deleteLocation(location.getId(), hubId);
                }
            }

            if (!backgroundOperation) {
                return mDatabaseHandler.getAllLocation(hubId);
            } else {
                return new ArrayList<>();
            }
        } else {
            return null;
        }
    }

    public LocationItemApp addLocationItem(LocationItemApp item) {
        mDatabaseHandler.addLocation(item);

        BusProvider.postOnMain(new ModelAddEvent<>("Location", item));

        return item;
    }

    public LocationItemApp getLocationItem(Integer id, Integer hubId) {
        return mDatabaseHandler.getLocation(id, hubId);
    }

    public void updateLocationItem(LocationItemApp item) {
        mDatabaseHandler.updateLocation(item);

        BusProvider.postOnMain(new ModelUpdateEvent<>("Location", item));
    }

    public void deleteLocationItem(LocationItemApp item) {
        mDatabaseHandler.deleteLocation(item.getId(), item.getHubId());
    }

    class ZWayResponse {
        Boolean dataUpdated;
        ArrayList<LocationItemApp> locationList;

        ZWayResponse(Boolean dataUpdated, ArrayList<LocationItemApp> locationList) {
            this.dataUpdated = dataUpdated;
            this.locationList = locationList;
        }
    }

    /**
     * Callback interface
     */
    public interface OnLocationListInteractionListener {
        void onLocationListLoaded(ArrayList<LocationItemApp> locationList, Boolean loadFromHub);
        void onLocationListError(String message);
    }
}
