package de.pathec.hubapp.model.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.profiles.Profile;
import de.fh_zwickau.informatik.sensor.model.profiles.ProfileList;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;

public class ProfileListApp {
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;
    private OnProfileListInteractionListener mCaller;

    private HubConnectionHolder mHubConnectionHolder;

    private AsyncTask<Integer, Void, ZWayResponse> mLoader;
    private Boolean mLoaderActive;

    public ProfileListApp(Context context, OnProfileListInteractionListener caller, HubConnectionHolder hubConnectionHolder) {
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
            Log.i(Params.LOGGING_TAG, "(ProfileListApp) Cancel synchronization.");
            mLoader.cancel(true);
        }
    }

    public void getProfileList(final Integer hubId, Boolean loadFromHub) {
        if (loadFromHub) {
            Log.i(Params.LOGGING_TAG, "(ProfileListApp) Loading data from Hub");
            mLoader = new StartLoadingProfile().execute(hubId);
        } else {
            Log.i(Params.LOGGING_TAG, "(ProfileListApp) Loading data from SQLite database.");
            mCaller.onProfileListLoaded(mDatabaseHandler.getAllProfile(hubId), false);
        }
    }

    private class StartLoadingProfile extends AsyncTask<Integer, Void, ZWayResponse> {
        @Override
        protected void onPreExecute() {
            Log.i(Params.LOGGING_TAG, "Start loading profiles: " + Params.formatDateTime(new Date()));
            mLoaderActive = true;
        }

        @Override
        protected ZWayResponse doInBackground(Integer... params) {
            if(mHubConnectionHolder != null && params[0] != null) {
                ArrayList<ProfileItemApp> profileList = loadAndSaveProfileList(params[0], null, false);

                if (profileList != null) {
                    return new ZWayResponse(true, profileList);
                }
            }

            return new ZWayResponse(false, mDatabaseHandler.getAllProfile(params[0]));
        }

        @Override
        protected void onPostExecute(ZWayResponse result) {
            Log.i(Params.LOGGING_TAG, "Finish loading profiles: " + Params.formatDateTime(new Date()));
            mLoaderActive = false;

            if (!result.dataUpdated) {
                Log.w(Params.LOGGING_TAG, "(ProfileListApp) Z-Way not connected!");
                mCaller.onProfileListError(mContext.getString(R.string.connection_not_connected_update));
            }
            mCaller.onProfileListLoaded(result.profileList, true);
        }
    }

    /**
     * @param hubId Hub id to associate the correct database entries.
     * @param zwayApi If not null use this instance for communication, otherwise use connection
     *                of list instance.
     * @param backgroundOperation Returns only a empty list (not null) if operation successfully performed.
     * @return List of profiles, empty list if background operation or null if anything goes wrong.
     */
    public ArrayList<ProfileItemApp> loadAndSaveProfileList(Integer hubId, IZWayApi zwayApi, Boolean backgroundOperation) {
        ProfileList profileList;
        if (zwayApi == null) {
            profileList = mHubConnectionHolder.getProfiles();
        } else {
            profileList = zwayApi.getProfiles();
        }

        if (profileList != null) {
            Set<Integer> profileIds = new HashSet<>();

            // Iterate over profiles from Hub
            for (Profile profile : profileList.getProfiles()) {
                profileIds.add(profile.getId());

                ProfileItemApp profileItemApp = mDatabaseHandler.getProfile(profile.getId(), hubId);

                if (profileItemApp.getId() != -1) {

                    profileItemApp.setName(profile.getName());
                    profileItemApp.setLang(profile.getLang());
                    profileItemApp.setColor(profile.getColor());
                    profileItemApp.setDashboard(profile.getDashboard());
                    profileItemApp.setInterval(profile.getInterval());
                    profileItemApp.setRooms(profile.getRooms());
                    profileItemApp.setExpertView(profile.getExpertView());
                    profileItemApp.setHideAllDeviceEvents(profile.getHideAllDeviceEvents());
                    profileItemApp.setHideSystemEvents(profile.getHideSystemEvents());
                    profileItemApp.setHideSingleDeviceEvents(profile.getHideSingleDeviceEvents());
                    profileItemApp.setEmail(profile.getEmail());

                    mDatabaseHandler.updateProfile(profileItemApp);

                    BusProvider.postOnMain(new ModelUpdateEvent<>("Profile", profileItemApp));
                } else {
                    ProfileItemApp newProfileItemApp = new ProfileItemApp(mContext, profile, hubId);

                    mDatabaseHandler.addProfile(newProfileItemApp);

                    BusProvider.postOnMain(new ModelAddEvent<>("Profile", newProfileItemApp));
                }
            }

            // Delete profiles
            ArrayList<ProfileItemApp> profiles = mDatabaseHandler.getAllProfile(hubId);
            for (ProfileItemApp profile : profiles) {
                if (!profileIds.contains(profile.getId())) {
                    mDatabaseHandler.deleteProfile(profile.getId(), hubId);
                }
            }

            if (!backgroundOperation) {
                return mDatabaseHandler.getAllProfile(hubId);
            } else {
                return new ArrayList<>();
            }
        } else {
            return null;
        }
    }

    public ProfileItemApp addProfileItem(ProfileItemApp item) {
        mDatabaseHandler.addProfile(item);

        BusProvider.postOnMain(new ModelAddEvent<>("Profile", item));

        return item;
    }

    public ProfileItemApp getProfileItem(Integer id, Integer hubId) {
        return mDatabaseHandler.getProfile(id, hubId);
    }

    public void updateProfileItem(ProfileItemApp item) {
        mDatabaseHandler.updateProfile(item);

        BusProvider.postOnMain(new ModelUpdateEvent<>("Profile", item));
    }

    public void deleteProfileItem(ProfileItemApp item) {
        mDatabaseHandler.deleteProfile(item.getId(), item.getHubId());
    }

    class ZWayResponse {
        Boolean dataUpdated;
        ArrayList<ProfileItemApp> profileList;

        ZWayResponse(Boolean dataUpdated, ArrayList<ProfileItemApp> profileList) {
            this.dataUpdated = dataUpdated;
            this.profileList = profileList;
        }
    }

    /**
     * Callback interface
     */
    public interface OnProfileListInteractionListener {
        void onProfileListLoaded(ArrayList<ProfileItemApp> profileList, Boolean loadFromHub);
        void onProfileListError(String message);
    }
}
