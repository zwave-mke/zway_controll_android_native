package de.pathec.hubapp.model.icon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.icons.Icon;
import de.fh_zwickau.informatik.sensor.model.icons.IconList;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.R;
import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;

public class IconListApp {
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;
    private OnIconListInteractionListener mCaller;

    private HubConnectionHolder mHubConnectionHolder;

    private AsyncTask<Integer, Void, ZWayResponse> mLoader;
    private Boolean mLoaderActive;

    public IconListApp(Context context, OnIconListInteractionListener caller, HubConnectionHolder hubConnectionHolder) {
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
            Log.i(Params.LOGGING_TAG, "(IconListApp) Cancel synchronization.");
            mLoader.cancel(true);
        }
    }

    public void getIconList(final Integer hubId, Boolean loadFromHub) {
        if (loadFromHub) {
            Log.i(Params.LOGGING_TAG, "(IconListApp) Loading data from Hub");
            mLoader = new StartLoadingIcon().execute(hubId);
        } else {
            Log.i(Params.LOGGING_TAG, "(IconListApp) Loading data from SQLite database.");
            mCaller.onIconListLoaded(mDatabaseHandler.getAllIcon(hubId), false);
        }
    }

    private class StartLoadingIcon extends AsyncTask<Integer, Void, ZWayResponse> {
        @Override
        protected void onPreExecute() {
            Log.i(Params.LOGGING_TAG, "Start loading icons: " + Params.formatDateTime(new Date()));
            mLoaderActive = true;
        }

        @Override
        protected ZWayResponse doInBackground(Integer... params) {
            if(mHubConnectionHolder != null && params[0] != null) {
                ArrayList<IconItemApp> iconList = loadAndSaveIconList(params[0], null, false);

                if (iconList != null) {
                    return new ZWayResponse(true, iconList);
                }
            }

            return new ZWayResponse(false, mDatabaseHandler.getAllIcon(params[0]));
        }

        @Override
        protected void onPostExecute(ZWayResponse result) {
            Log.i(Params.LOGGING_TAG, "Finish loading icons: " + Params.formatDateTime(new Date()));
            mLoaderActive = false;

            if (!result.dataUpdated) {
                Log.w(Params.LOGGING_TAG, "(IconListApp) Z-Way not connected!");
                mCaller.onIconListError(mContext.getString(R.string.connection_not_connected_update));
            }
            mCaller.onIconListLoaded(result.iconList, true);
        }
    }

    /**
     * @param hubId Hub id to associate the correct database entries.
     * @param zwayApi If not null use this instance for communication, otherwise use connection
     *                of list instance.
     * @param backgroundOperation Returns only a empty list (not null) if operation successfully performed.
     * @return List of profiles, empty list if background operation or null if anything goes wrong.
     */
    public ArrayList<IconItemApp> loadAndSaveIconList(Integer hubId, IZWayApi zwayApi, Boolean backgroundOperation) {
        IconList iconList;
        if (zwayApi == null) {
            iconList = mHubConnectionHolder.getIcons();
        } else {
            iconList = zwayApi.getIcons();
        }

        if (iconList != null) {
            Set<String> iconIds = new HashSet<>();

            for(Icon icon : iconList.getIcons()) {
                iconIds.add(icon.getFile());

                IconItemApp iconItemApp = mDatabaseHandler.getIcon(icon.getFile(), hubId);

                if (!iconItemApp.getFile().isEmpty()) {
                    // Load icon if not yet loaded (for example if an error occurred)
                    if (iconItemApp.getIconPath().isEmpty()) {
                        String iconPath;
                        if (zwayApi == null) {
                            iconPath = loadIcon(icon.getFile(), mHubConnectionHolder.getZWayApi());
                        } else {
                            iconPath = loadIcon(icon.getFile(), zwayApi);
                        }
                        if (iconPath != null) {
                            Log.i(Params.LOGGING_TAG, "Downloading icon successful: " + iconPath);
                            iconItemApp.setIconPath(iconPath);
                        } else {
                            iconItemApp.setIconPath("");
                        }

                        mDatabaseHandler.updateIcon(iconItemApp);

                        BusProvider.postOnMain(new ModelUpdateEvent<>("Icon", iconItemApp));
                    }
                } else {
                    IconItemApp newIconItemApp = new IconItemApp(mContext, icon, hubId);

                    String iconPath;
                    if (zwayApi == null) {
                        iconPath = loadIcon(icon.getFile(), mHubConnectionHolder.getZWayApi());
                    } else {
                        iconPath = loadIcon(icon.getFile(), zwayApi);
                    }
                    if (iconPath != null) {
                        Log.i(Params.LOGGING_TAG, "Downloading icon successful: " + iconPath);
                        newIconItemApp.setIconPath(iconPath);
                    } else {
                        newIconItemApp.setIconPath("");
                    }

                    mDatabaseHandler.addIcon(newIconItemApp);

                    BusProvider.postOnMain(new ModelAddEvent<>("Icon", newIconItemApp));
                }
            }

            // Delete icons
            ArrayList<IconItemApp> icons = mDatabaseHandler.getAllIcon(hubId);
            for (IconItemApp icon : icons) {
                if (!iconIds.contains(icon.getFile())) {
                    mDatabaseHandler.deleteIcon(icon.getFile(), hubId);
                }
            }

            if (!backgroundOperation) {
                return mDatabaseHandler.getAllIcon(hubId);
            } else {
                return new ArrayList<>();
            }
        } else {
            return null;
        }
    }

    public IconItemApp addIconItem(IconItemApp item) {
        mDatabaseHandler.addIcon(item);

        BusProvider.postOnMain(new ModelAddEvent<>("Icon", item));

        return item;
    }

    public IconItemApp getIconItem(String file, Integer hubId) {
        return mDatabaseHandler.getIcon(file, hubId);
    }

    public void updateIconItem(IconItemApp item) {
        mDatabaseHandler.updateIcon(item);

        BusProvider.postOnMain(new ModelUpdateEvent<>("Icon", item));
    }

    public void deleteIconItem(IconItemApp item) {
        mDatabaseHandler.deleteIcon(item.getFile(), item.getHubId());
    }

    class ZWayResponse {
        Boolean dataUpdated;
        ArrayList<IconItemApp> iconList;

        ZWayResponse(Boolean dataUpdated, ArrayList<IconItemApp> iconList) {
            this.dataUpdated = dataUpdated;
            this.iconList = iconList;
        }
    }

    private String loadIcon(String file, IZWayApi zwayApi) {
        if (zwayApi == null) {
            Log.w(Params.LOGGING_TAG, "Downloading image failed: Z-Way not connected");
            return null;
        }

        try {
            Bitmap icon = getIconAsBitmap(zwayApi.getTopLevelUrl() + "/smarthome/user/icons/" + file,
                    "ZWAYSession=" + zwayApi.getZWaySessionId() + "; ZBW_SESSID=" + zwayApi.getZWayRemoteSessionId());

            if (icon == null) {
                Log.w(Params.LOGGING_TAG, "Downloading image failed: Bitmap is null!");
                return null;
            }

            String iconPath = mContext.getFilesDir().getPath() + "/" + Util.saveImage(mContext, icon, "icon_" + file, "");
            if (iconPath == null) {
                Log.w(Params.LOGGING_TAG, "Downloading image failed: Can't store bitmap as file!");
            }

            Log.i(Params.LOGGING_TAG, "Downloading image successful: " + iconPath);

            return iconPath;
        } catch (IOException e) {
            Log.e(Params.LOGGING_TAG, "Downloading image failed: " + e.getMessage(), e);
        }

        return null;
    }

    private Bitmap getIconAsBitmap(String urlString, String cookies) throws IOException {
        URL url = new URL(urlString);
        URLConnection connection =  url.openConnection();

        Log.i(Params.LOGGING_TAG, "Downloading image: " + urlString);

        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;

        try {
            httpConnection = (HttpURLConnection) connection;
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setReadTimeout(60000 /* milliseconds */);
            httpConnection.setConnectTimeout(65000 /* milliseconds */);
            httpConnection.setRequestMethod("GET");
            if (!cookies.isEmpty()) {
                httpConnection.setRequestProperty("Cookie", cookies);
            }
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConnection.getInputStream();

                // Default solution
                // BitmapFactory.Options options = new BitmapFactory.Options();
                // options.inSampleSize = 2;
                // return BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, options);

                // Stream with skip method
                //return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));

                // Wrap with buffered input stream
                return BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
            }
        } catch (Exception e) {
            Log.e(Params.LOGGING_TAG, "Downloading image failed: " + e.getMessage(), e);
        } finally {
            httpConnection.disconnect();
            try {
                inputStream.close();
            } catch (Exception e) { }
        }
        return null;
    }

    /**
     * Callback interface
     */
    public interface OnIconListInteractionListener {
        void onIconListLoaded(ArrayList<IconItemApp> iconList, Boolean loadFromHub);
        void onIconListError(String message);
    }
}
