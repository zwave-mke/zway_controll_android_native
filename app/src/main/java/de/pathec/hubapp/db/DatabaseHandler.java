package de.pathec.hubapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryData;
import de.fh_zwickau.informatik.sensor.model.devices.Icons;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.model.devicehistory.DeviceHistoryItemApp;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.icon.IconItemApp;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.notification.NotificationItemApp;
import de.pathec.hubapp.model.profile.ProfileItemApp;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.ZWayUtil;

public class DatabaseHandler extends SQLiteOpenHelper {
    private Context mContext;

    private static final String TABLE_PROTOCOL = "hubAppDBProtocol";
    private static final String TABLE_HUB = "hubAppDBHub";
    private static final String TABLE_LOCATION = "hubAppDBLocation";
    private static final String TABLE_NOTIFICATION = "hubAppDBNotification";
    private static final String TABLE_DEVICE = "hubAppDBDevice";
    private static final String TABLE_PROFILE = "hubAppDBProfile";
    private static final String TABLE_DEVICE_HISTORY = "hubAppDBDeviceHistory";
    private static final String TABLE_ICON = "hubAppDBIcon";

    private static final String KEY_COMMON_ID = "mId";
    private static final String KEY_COMMON_CREATED = "mCreated";
    private static final String KEY_COMMON_MODIFIED = "mModified";
    private static final String KEY_COMMON_ORDER = "mOrder";

    private static final String KEY_COMMON_HUB_ID = "mHubId";

    // Protocol
    private static final String KEY_PROTOCOL_STATUS = "mStatus";
    private static final String KEY_PROTOCOL_TEXT = "mText";
    private static final String KEY_PROTOCOL_CATEGORY = "mCategory";

    // Hub
    private static final String KEY_HUB_TITLE = "mTitle";
    private static final String KEY_HUB_TILE = "mTile";
    private static final String KEY_HUB_USERNAME = "mUsername";
    private static final String KEY_HUB_PASSWORD = "mPassword";
    private static final String KEY_HUB_REMOTE_SERVICE = "mRemoteService";
    private static final String KEY_HUB_REMOTE_ID = "mRemoteId";
    private static final String KEY_HUB_LOCAL_IP = "mLocalIP";
    private static final String KEY_HUB_HUB_SSID = "mHubSSID";
    private static final String KEY_HUB_HUB_WIFI_PASSWORD = "mHubWifiPassword";
    private static final String KEY_HUB_HUB_IP = "mHubIP";
    private static final String KEY_HUB_LOCATION = "mLocation";
    private static final String KEY_HUB_LONGITUDE = "mLongitude";
    private static final String KEY_HUB_LATITUDE = "mLatitude";
    private static final String KEY_HUB_PRESENCE_DEVICE_ID = "mPresenceDeviceId";

    // Locations
    private static final String KEY_LOCATION_TITLE = "mTitle";
    private static final String KEY_LOCATION_TILE = "mTile";
    private static final String KEY_LOCATION_USER_IMG = "mUserImg";

    // Notifications
    private static final String KEY_NOTIFICATION_TIMESTAMP = "mTimestamp";
    private static final String KEY_NOTIFICATION_LEVEL = "mLevel";
    private static final String KEY_NOTIFICATION_MESSAGE_DEV = "mMessageDev";
    private static final String KEY_NOTIFICATION_MESSAGE_L = "mMessageL";
    private static final String KEY_NOTIFICATION_TYPE = "mType";
    private static final String KEY_NOTIFICATION_SOURCE = "mSource";

    // Devices
    private static final String KEY_DEVICE_CREATION_TIME = "mCreationTime";
    private static final String KEY_DEVICE_CREATOR_ID = "mCreatorId";
    private static final String KEY_DEVICE_DEVICE_TYPE = "mDeviceType";
    private static final String KEY_DEVICE_H = "mH";
    private static final String KEY_DEVICE_HAS_HISTORY = "mHasHistory";
    private static final String KEY_DEVICE_DEVICE_ID = "mDeviceId"; // Primary key (with Hub Id)
    private static final String KEY_DEVICE_LOCATION = "mLocation";
    private static final String KEY_DEVICE_PERMANENTLY_HIDDEN = "mPermanentlyHidden";
    private static final String KEY_DEVICE_PROBE_TYPE = "mProbeType";
    private static final String KEY_DEVICE_VISIBILITY = "mVisibility";
    private static final String KEY_DEVICE_UPDATE_TIME = "mUpdateTime";
    private static final String KEY_DEVICE_METRICS_ICON = "mMetricsIcon";
    private static final String KEY_DEVICE_METRICS_TITLE = "mMetricsTitle";
    private static final String KEY_DEVICE_METRICS_LEVEL = "mMetricsLevel";
    private static final String KEY_DEVICE_METRICS_PROBE_TITLE = "mMetricsProbeTitle";
    private static final String KEY_DEVICE_METRICS_SCALE_TITLE = "mMetricsScaleTitle";
    private static final String KEY_DEVICE_METRICS_CAMERA_STREAM_URL = "mMetricsCameraStreamUrl";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_IN = "mMetricsCameraHasZoomIn";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OUT = "mMetricsCameraHasZoomOut";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_LEFT = "mMetricsCameraHasLeft";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_RIGHT = "mMetricsCameraHasRight";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_UP = "mMetricsCameraHasZoomUp";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_DOWN = "mMetricsCameraHasZoomDown";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OPEN = "mMetricsCameraHasZoomOpen";
    private static final String KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_CLOSE = "mMetricsCameraHasZoomClose";
    private static final String KEY_DEVICE_METRICS_DISCRETE_CURRENT_SCENE = "mMetricsDiscreteCurrentScene";
    private static final String KEY_DEVICE_METRICS_DISCRETE_KEY_ATTRIBUTE = "mMetricsDiscreteKeyAttribute";
    private static final String KEY_DEVICE_METRICS_DISCRETE_STATE = "mMetricsDiscreteState";
    private static final String KEY_DEVICE_METRICS_DISCRETE_MAX_SCENES = "mMetricsDiscreteMaxScenes";
    private static final String KEY_DEVICE_METRICS_DISCRETE_COUNT = "mMetricsDiscreteCount";
    private static final String KEY_DEVICE_METRICS_DISCRETE_TYPE = "mMetricsDiscreteType";
    private static final String KEY_DEVICE_METRICS_TEXT = "mMetricsText";
    private static final String KEY_DEVICE_COLOR_RED = "mColorRed";
    private static final String KEY_DEVICE_COLOR_GREEN = "mColorGreen";
    private static final String KEY_DEVICE_COLOR_BLUE = "mColorBlue";
    private static final String KEY_DEVICE_MIN = "mMin";
    private static final String KEY_DEVICE_MAX = "mMax";
    private static final String KEY_DEVICE_TAGS = "mTags";
    private static final String KEY_DEVICE_ICON = "mIcon";
    private static final String KEY_DEVICE_CUSTOM_ICONS = "mCustomIcons";

    // Profiles
    private static final String KEY_PROFILE_NAME = "mName";
    private static final String KEY_PROFILE_LANG = "mLang";
    private static final String KEY_PROFILE_COLOR = "mColor";
    private static final String KEY_PROFILE_DASHBOARD = "mDashboard";
    private static final String KEY_PROFILE_INTERVAL = "mInterval";
    private static final String KEY_PROFILE_ROOMS = "mRooms";
    private static final String KEY_PROFILE_EXPERT_VIEW = "mExpertView";
    private static final String KEY_PROFILE_HIDE_ALL_DEVICE_EVENTS = "mHideAllDeviceEvents";
    private static final String KEY_PROFILE_HIDE_SYSTEM_EVENTS = "mHideSystemEvents";
    private static final String KEY_PROFILE_HIDE_SINGLE_DEVICE_EVENTS = "mHideSingleDeviceEvents";
    private static final String KEY_PROFILE_EMAIL = "mEmail";

    // Device history
    private static final String KEY_DEVICE_HISTORY_DEVICE_ID = "mDeviceId";
    private static final String KEY_DEVICE_HISTORY_DEVICE_TYPE = "mDeviceTYPE";
    private static final String KEY_DEVICE_HISTORY_DEVICE_HISTORY_DATA = "mDeviceHistoryData";

    // Icon
    private static final String KEY_DEVICE_ICON_ICON_PATH = "mIconPath";
    private static final String KEY_DEVICE_ICON_FILE = "mFile";
    private static final String KEY_DEVICE_ICON_ORG_FILE = "mOrgFile";
    private static final String KEY_DEVICE_ICON_SOURCE = "mSource";
    private static final String KEY_DEVICE_ICON_NAME = "mName";
    private static final String KEY_DEVICE_ICON_ID = "mId";
    private static final String KEY_DEVICE_ICON_TIMESTAMP = "mTimestamp";
    private static final String KEY_DEVICE_ICON_SOURCE_TITLE = "mSourceTitle";

    private Type collectionType = new TypeToken<Collection<String>>(){}.getType();
    private Type collectionTypeHistoryData = new TypeToken<Collection<DeviceHistoryData>>(){}.getType();

    private Type setType = new TypeToken<HashSet<String>>(){}.getType();
    private Type setTypeInteger = new TypeToken<HashSet<Integer>>(){}.getType();

    private static DatabaseHandler sINSTANCE;
    private static Object sLOCK = "";

    public synchronized static DatabaseHandler getInstance(Context context) {
        if(sINSTANCE == null) {
            synchronized (sLOCK) {
                if(sINSTANCE == null && context != null) {
                    sINSTANCE = new DatabaseHandler(context);
                }
            }
        }
        return sINSTANCE;
    }

    private DatabaseHandler(Context context) {
        super(context, Params.DATABASE_NAME, null, Params.DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROTOCOL_TABLE = "CREATE TABLE "
                + TABLE_PROTOCOL + "("
                + KEY_COMMON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_COMMON_CREATED + " TEXT,"
                + KEY_COMMON_MODIFIED + " TEXT,"
                + KEY_PROTOCOL_STATUS + " TEXT,"
                + KEY_PROTOCOL_TEXT + " TEXT,"
                + KEY_PROTOCOL_CATEGORY + " TEXT"
                + ")";
        db.execSQL(CREATE_PROTOCOL_TABLE);

        String CREATE_HUB_TABLE = "CREATE TABLE "
                + TABLE_HUB + "("
                + KEY_COMMON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_COMMON_CREATED + " TEXT,"
                + KEY_COMMON_MODIFIED + " TEXT,"
                + KEY_HUB_TITLE + " TEXT,"
                + KEY_HUB_TILE + " TEXT,"
                + KEY_HUB_USERNAME + " TEXT,"
                + KEY_HUB_PASSWORD + " TEXT,"
                + KEY_HUB_REMOTE_SERVICE + " TEXT,"
                + KEY_HUB_REMOTE_ID + " INTEGER,"
                + KEY_HUB_LOCAL_IP + " TEXT,"
                + KEY_HUB_HUB_SSID + " TEXT,"
                + KEY_HUB_HUB_WIFI_PASSWORD + " TEXT,"
                + KEY_HUB_HUB_IP + " TEXT,"
                + KEY_HUB_LOCATION + " TEXT,"
                + KEY_HUB_LONGITUDE + " REAL,"
                + KEY_HUB_LATITUDE + " REAL,"
                + KEY_HUB_PRESENCE_DEVICE_ID + " TEXT"
                + ")";
        db.execSQL(CREATE_HUB_TABLE);

        String CREATE_LOCATION_TABLE = "CREATE TABLE "
                + TABLE_LOCATION + "("
                + KEY_COMMON_ID + " INTEGER," // Id and Hub id build the primary key
                + KEY_COMMON_HUB_ID + " INTEGER,"
                + KEY_LOCATION_TITLE + " TEXT,"
                + KEY_LOCATION_TILE + " TEXT,"
                + KEY_LOCATION_USER_IMG + " TEXT,"
                + KEY_COMMON_ORDER + " INTEGER"
                + ")";
        db.execSQL(CREATE_LOCATION_TABLE);

        String CREATE_NOTIFICATION_TABLE = "CREATE TABLE "
                + TABLE_NOTIFICATION + "("
                + KEY_COMMON_ID + " INTEGER," // Id and Hub id build the primary key
                + KEY_COMMON_HUB_ID + " INTEGER,"
                + KEY_NOTIFICATION_TIMESTAMP + " TEXT,"
                + KEY_NOTIFICATION_LEVEL + " TEXT,"
                + KEY_NOTIFICATION_MESSAGE_DEV + " TEXT,"
                + KEY_NOTIFICATION_MESSAGE_L + " TEXT,"
                + KEY_NOTIFICATION_TYPE + " TEXT,"
                + KEY_NOTIFICATION_SOURCE + " TEXT"
                + ")";
        db.execSQL(CREATE_NOTIFICATION_TABLE);

        String CREATE_DEVICE_TABLE = "CREATE TABLE "
                + TABLE_DEVICE + "("
                + KEY_DEVICE_CREATION_TIME + " INTEGER,"
                + KEY_DEVICE_CREATOR_ID + " INTEGER,"
                + KEY_DEVICE_DEVICE_TYPE + " TEXT,"
                + KEY_DEVICE_H + " INTEGER,"
                + KEY_DEVICE_HAS_HISTORY + " INTEGER,"
                + KEY_DEVICE_DEVICE_ID + " INTEGER," // Device id and Hub id build the primary key
                + KEY_DEVICE_LOCATION + " INTEGER,"
                + KEY_DEVICE_PERMANENTLY_HIDDEN + " INTEGER,"
                + KEY_DEVICE_PROBE_TYPE + " TEXT,"
                + KEY_DEVICE_VISIBILITY + " INTEGER,"
                + KEY_DEVICE_UPDATE_TIME + " INTEGER,"
                + KEY_DEVICE_METRICS_ICON + " TEXT,"
                + KEY_DEVICE_METRICS_TITLE + " TEXT,"
                + KEY_DEVICE_METRICS_LEVEL + " TEXT,"
                + KEY_DEVICE_METRICS_PROBE_TITLE + " TEXT,"
                + KEY_DEVICE_METRICS_SCALE_TITLE + " TEXT,"
                + KEY_DEVICE_COLOR_RED + " INTEGER,"
                + KEY_DEVICE_COLOR_GREEN + " INTEGER,"
                + KEY_DEVICE_COLOR_BLUE + " INTEGER,"
                + KEY_DEVICE_MIN + " INTEGER,"
                + KEY_DEVICE_MAX + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_STREAM_URL + " TEXT,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_IN + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OUT + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_LEFT + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_RIGHT + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_UP + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_DOWN + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OPEN + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_CLOSE + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_CURRENT_SCENE + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_KEY_ATTRIBUTE + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_STATE + " TEXT,"
                + KEY_DEVICE_METRICS_DISCRETE_MAX_SCENES + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_COUNT + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_TYPE + " TEXT,"
                + KEY_DEVICE_METRICS_TEXT + " TEXT,"
                + KEY_DEVICE_TAGS + " TEXT,"
                + KEY_DEVICE_CUSTOM_ICONS + " TEXT,"
                + KEY_COMMON_HUB_ID + " INTEGER,"
                + KEY_DEVICE_ICON + " TEXT,"
                + KEY_COMMON_ORDER + " INTEGER"
                + ")";
        db.execSQL(CREATE_DEVICE_TABLE);

        String CREATE_PROFILE_TABLE = "CREATE TABLE "
                + TABLE_PROFILE + "("
                + KEY_COMMON_ID + " INTEGER," // Id and Hub id build the primary key
                + KEY_COMMON_HUB_ID + " INTEGER,"
                + KEY_PROFILE_NAME + " TEXT,"
                + KEY_PROFILE_LANG + " TEXT,"
                + KEY_PROFILE_COLOR + " TEXT,"
                + KEY_PROFILE_DASHBOARD + " TEXT,"
                + KEY_PROFILE_INTERVAL + " INTEGER,"
                + KEY_PROFILE_ROOMS + " TEXT,"
                + KEY_PROFILE_EXPERT_VIEW + " INTEGER,"
                + KEY_PROFILE_HIDE_ALL_DEVICE_EVENTS + " INTEGER,"
                + KEY_PROFILE_HIDE_SYSTEM_EVENTS + " INTEGER,"
                + KEY_PROFILE_HIDE_SINGLE_DEVICE_EVENTS + " TEXT,"
                + KEY_PROFILE_EMAIL + " TEXT"
                + ")";
        db.execSQL(CREATE_PROFILE_TABLE);

        String CREATE_DEVICE_HISTORY_TABLE = "CREATE TABLE "
                + TABLE_DEVICE_HISTORY + "("
                + KEY_DEVICE_HISTORY_DEVICE_ID + " TEXT," // Device id and Hub id build the primary key
                + KEY_COMMON_HUB_ID + " INTEGER,"
                + KEY_DEVICE_HISTORY_DEVICE_TYPE + " TEXT,"
                + KEY_DEVICE_HISTORY_DEVICE_HISTORY_DATA + " TEXT"
                + ")";
        db.execSQL(CREATE_DEVICE_HISTORY_TABLE);

        String CREATE_ICON_TABLE = "CREATE TABLE "
                + TABLE_ICON + "("
                + KEY_COMMON_HUB_ID + " INTEGER,"
                + KEY_DEVICE_ICON_ICON_PATH + " TEXT,"
                + KEY_DEVICE_ICON_FILE + " TEXT," // File and Hub id build the primary key
                + KEY_DEVICE_ICON_ORG_FILE + " TEXT,"
                + KEY_DEVICE_ICON_SOURCE + " TEXT,"
                + KEY_DEVICE_ICON_NAME + " TEXT,"
                + KEY_DEVICE_ICON_ID + " TEXT,"
                + KEY_DEVICE_ICON_TIMESTAMP + " INTEGER,"
                + KEY_DEVICE_ICON_SOURCE_TITLE + " TEXT"
                + ")";
        db.execSQL(CREATE_ICON_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROTOCOL);
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_HUB);
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE);
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE_HISTORY);
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_ICON);

        // onCreate(db);

        String CREATE_DEVICE_TABLE = "CREATE TABLE "
                + TABLE_DEVICE + "("
                + KEY_DEVICE_CREATION_TIME + " INTEGER,"
                + KEY_DEVICE_CREATOR_ID + " INTEGER,"
                + KEY_DEVICE_DEVICE_TYPE + " TEXT,"
                + KEY_DEVICE_H + " INTEGER,"
                + KEY_DEVICE_HAS_HISTORY + " INTEGER,"
                + KEY_DEVICE_DEVICE_ID + " INTEGER," // Device id and Hub id build the primary key
                + KEY_DEVICE_LOCATION + " INTEGER,"
                + KEY_DEVICE_PERMANENTLY_HIDDEN + " INTEGER,"
                + KEY_DEVICE_PROBE_TYPE + " TEXT,"
                + KEY_DEVICE_VISIBILITY + " INTEGER,"
                + KEY_DEVICE_UPDATE_TIME + " INTEGER,"
                + KEY_DEVICE_METRICS_ICON + " TEXT,"
                + KEY_DEVICE_METRICS_TITLE + " TEXT,"
                + KEY_DEVICE_METRICS_LEVEL + " TEXT,"
                + KEY_DEVICE_METRICS_PROBE_TITLE + " TEXT,"
                + KEY_DEVICE_METRICS_SCALE_TITLE + " TEXT,"
                + KEY_DEVICE_COLOR_RED + " INTEGER,"
                + KEY_DEVICE_COLOR_GREEN + " INTEGER,"
                + KEY_DEVICE_COLOR_BLUE + " INTEGER,"
                + KEY_DEVICE_MIN + " INTEGER,"
                + KEY_DEVICE_MAX + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_STREAM_URL + " TEXT,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_IN + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OUT + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_LEFT + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_RIGHT + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_UP + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_DOWN + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OPEN + " INTEGER,"
                + KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_CLOSE + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_CURRENT_SCENE + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_KEY_ATTRIBUTE + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_STATE + " TEXT,"
                + KEY_DEVICE_METRICS_DISCRETE_MAX_SCENES + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_COUNT + " INTEGER,"
                + KEY_DEVICE_METRICS_DISCRETE_TYPE + " TEXT,"
                + KEY_DEVICE_METRICS_TEXT + " TEXT,"
                + KEY_DEVICE_TAGS + " TEXT,"
                + KEY_DEVICE_CUSTOM_ICONS + " TEXT,"
                + KEY_COMMON_HUB_ID + " INTEGER,"
                + KEY_DEVICE_ICON + " TEXT,"
                + KEY_COMMON_ORDER + " INTEGER"
                + ")";
        db.execSQL(CREATE_DEVICE_TABLE);
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public long addProtocol(ProtocolItem item) {
        deleteOldProtocol();

        ContentValues values = new ContentValues();
        // Autoincrement - values.put(KEY_PROTOCOL_ID, item.getId());

        values.put(KEY_COMMON_CREATED, Params.formatDateTime(item.getCreated()));
        values.put(KEY_COMMON_MODIFIED, Params.formatDateTime(item.getModified()));
        values.put(KEY_PROTOCOL_STATUS, item.getStatus());
        values.put(KEY_PROTOCOL_TEXT, item.getText());
        values.put(KEY_PROTOCOL_CATEGORY, item.getCategory());

        return getWritableDatabase().insert(TABLE_PROTOCOL, null, values);
    }

    private void deleteOldProtocol() {
        String deleteQuery =
                "DELETE FROM " + TABLE_PROTOCOL
                        + " WHERE " + KEY_COMMON_ID + " < ("
                        + " SELECT MIN(" + KEY_COMMON_ID + ")"
                        + " FROM (SELECT " + KEY_COMMON_ID + " FROM " + TABLE_PROTOCOL + " ORDER BY " + KEY_COMMON_ID + " DESC" + " LIMIT 499)"
                        + " )";

        getWritableDatabase().execSQL(deleteQuery);
    }

    public Integer updateProtocol(ProtocolItem item) {
        ContentValues values = new ContentValues();
        // Autoincrement - values.put(KEY_PROTOCOL_ID, item.getId());

        // only if add new protocol - values.put(KEY_PROTOCOL_CREATED, Params.formatDateTime(item.getCreated()));
        values.put(KEY_COMMON_MODIFIED, Params.formatDateTime(item.getModified()));
        values.put(KEY_PROTOCOL_STATUS, item.getStatus());
        values.put(KEY_PROTOCOL_TEXT, item.getText());
        values.put(KEY_PROTOCOL_CATEGORY, item.getCategory());

        return getWritableDatabase().update(TABLE_PROTOCOL, values, KEY_COMMON_ID + "=" + item.getId(), null);
    }

    public ProtocolItem getProtocol(Integer iId) {
        ProtocolItem item = new ProtocolItem(mContext);

        Cursor cursor = getReadableDatabase().query(TABLE_PROTOCOL, new String[]{
                        KEY_COMMON_ID,
                        KEY_COMMON_CREATED,
                        KEY_COMMON_MODIFIED,
                        KEY_PROTOCOL_STATUS,
                        KEY_PROTOCOL_TEXT,
                        KEY_PROTOCOL_CATEGORY},
                KEY_COMMON_ID + "=?",
                new String[]{String.valueOf(iId)}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                Date dCreated = new Date();
                try {
                    dCreated = Params.parseDateTime(cursor.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date dModified = new Date();
                try {
                    dModified = Params.parseDateTime(cursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                item = new ProtocolItem(mContext, cursor.getInt(0), // mId
                        dCreated, // mCreated
                        dModified, // mModified
                        cursor.getString(3), // mStatus
                        cursor.getString(4), // mText
                        cursor.getString(5) // mCategory
                );
            }
            cursor.close();
        }
        return item;
    }

    public ArrayList<ProtocolItem> getAllProtocol(String status, String category) {
        ArrayList<ProtocolItem> items = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_PROTOCOL;

        if(!status.equals("")) {
            selectQuery += " WHERE " + KEY_PROTOCOL_STATUS + " = '" + status + "'";
        }

        if(!category.equals("")) {
            selectQuery += " WHERE " + KEY_PROTOCOL_CATEGORY + " = '" + category + "'";
        }

        selectQuery += " ORDER BY " + KEY_COMMON_ID + " DESC";

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Date dCreated = new Date();
                try {
                    dCreated = Params.parseDateTime(cursor.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date dModified = new Date();
                try {
                    dModified = Params.parseDateTime(cursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                items.add(new ProtocolItem(mContext, cursor.getInt(0), // mId
                        dCreated, // mCreated
                        dModified, // mModified
                        cursor.getString(3), // mStatus
                        cursor.getString(4), // mText
                        cursor.getString(5) // mCategory

                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public Integer getProtocolCount() {
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_PROTOCOL;

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);
        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    public void deleteAllProtocol() {
        getWritableDatabase().delete(TABLE_PROTOCOL, null, null);
    }

    public void deleteProtocol(Integer iId) {
        getWritableDatabase().delete(TABLE_PROTOCOL, KEY_COMMON_ID + "=" + iId + "", null);
    }

    public long addHub(HubItem item) {
        ContentValues values = new ContentValues();
        // Autoincrement - values.put(KEY_PROTOCOL_ID, item.getId());

        values.put(KEY_COMMON_CREATED, Params.formatDateTime(item.getCreated()));
        values.put(KEY_COMMON_MODIFIED, Params.formatDateTime(item.getModified()));
        values.put(KEY_HUB_TITLE, item.getTitle());
        values.put(KEY_HUB_TILE, item.getTile());
        values.put(KEY_HUB_REMOTE_SERVICE, item.getRemoteService());
        values.put(KEY_HUB_REMOTE_ID, item.getRemoteId());
        values.put(KEY_HUB_USERNAME, item.getUsername());
        values.put(KEY_HUB_PASSWORD, item.getPassword());
        values.put(KEY_HUB_LOCAL_IP, item.getLocalIP());
        values.put(KEY_HUB_HUB_SSID, item.getHubSSID());
        values.put(KEY_HUB_HUB_WIFI_PASSWORD, item.getHubWifiPassword());
        values.put(KEY_HUB_HUB_IP, item.getHubIP());
        values.put(KEY_HUB_LOCATION, item.getLocation());
        values.put(KEY_HUB_LONGITUDE, item.getLongitude());
        values.put(KEY_HUB_LATITUDE, item.getLatitude());
        values.put(KEY_HUB_PRESENCE_DEVICE_ID, item.getPresenceDeviceId());

        return getWritableDatabase().insert(TABLE_HUB, null, values);
    }

    public Integer updateHub(HubItem item) {
        ContentValues values = new ContentValues();
        // Autoincrement - values.put(KEY_PROTOCOL_ID, item.getId());

        // only if add new protocol - values.put(KEY_PROTOCOL_CREATED, Params.formatDateTime(item.getCreated()));
        values.put(KEY_COMMON_MODIFIED, Params.formatDateTime(item.getModified()));
        values.put(KEY_HUB_TITLE, item.getTitle());
        values.put(KEY_HUB_TILE, item.getTile());
        values.put(KEY_HUB_REMOTE_SERVICE, item.getRemoteService());
        values.put(KEY_HUB_REMOTE_ID, item.getRemoteId());
        values.put(KEY_HUB_USERNAME, item.getUsername());
        values.put(KEY_HUB_PASSWORD, item.getPassword());
        values.put(KEY_HUB_LOCAL_IP, item.getLocalIP());
        values.put(KEY_HUB_HUB_SSID, item.getHubSSID());
        values.put(KEY_HUB_HUB_WIFI_PASSWORD, item.getHubWifiPassword());
        values.put(KEY_HUB_HUB_IP, item.getHubIP());
        values.put(KEY_HUB_LOCATION, item.getLocation());
        values.put(KEY_HUB_LONGITUDE, item.getLongitude());
        values.put(KEY_HUB_LATITUDE, item.getLatitude());
        values.put(KEY_HUB_PRESENCE_DEVICE_ID, item.getPresenceDeviceId());

        return getWritableDatabase().update(TABLE_HUB, values, KEY_COMMON_ID + "=" + item.getId(), null);
    }

    public HubItem getHub(Integer iId) {
        HubItem item = new HubItem(mContext);

        Cursor cursor = getReadableDatabase().query(TABLE_HUB, new String[]{
                        KEY_COMMON_ID,
                        KEY_COMMON_CREATED,
                        KEY_COMMON_MODIFIED,
                        KEY_HUB_TITLE,
                        KEY_HUB_TILE,
                        KEY_HUB_USERNAME,
                        KEY_HUB_PASSWORD,
                        KEY_HUB_REMOTE_SERVICE,
                        KEY_HUB_REMOTE_ID,
                        KEY_HUB_LOCAL_IP,
                        KEY_HUB_HUB_SSID,
                        KEY_HUB_HUB_WIFI_PASSWORD,
                        KEY_HUB_HUB_IP,
                        KEY_HUB_LOCATION,
                        KEY_HUB_LONGITUDE,
                        KEY_HUB_LATITUDE,
                        KEY_HUB_PRESENCE_DEVICE_ID},
                KEY_COMMON_ID + "=?",
                new String[]{String.valueOf(iId)}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                Date dCreated = new Date();
                try {
                    dCreated = Params.parseDateTime(cursor.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date dModified = new Date();
                try {
                    dModified = Params.parseDateTime(cursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                item = new HubItem(mContext, cursor.getInt(0), // mId
                        dCreated, // mCreated
                        dModified, // mModified
                        cursor.getString(3), // mTitle
                        cursor.getString(4), // mTile
                        cursor.getString(5), // mUsername
                        cursor.getString(6), // mPassword
                        cursor.getString(7), // mRemoteService
                        cursor.getInt(8), // mRemoteId
                        cursor.getString(9), // mLocalIP
                        cursor.getString(10), // mHubSSID
                        cursor.getString(11), // mHubWifiPassword
                        cursor.getString(12), // mHubIP
                        cursor.getString(13), // mLocation
                        cursor.getDouble(14), // mLongitude
                        cursor.getDouble(15), // mLatitude
                        cursor.getString(16) // mPresenceDeviceId
                );
            }
            cursor.close();
        }
        return item;
    }

    public Boolean existHub(HubItem item) {
        String selectQuery = "SELECT * FROM " + TABLE_HUB;
        // Title in every case
        selectQuery += " WHERE " + KEY_HUB_TITLE + " = '" + item.getTitle() + "'";
        // Remote id only if set
        if (item.getRemoteId() != null && !item.getRemoteId().equals(0)) {
            selectQuery += " OR " + KEY_HUB_REMOTE_ID + " = " + item.getRemoteId();
        }

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                return true;
            }
            cursor.close();
        }

        return false;
    }

    public ArrayList<HubItem> getAllHub() {
        ArrayList<HubItem> items = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_HUB;
        selectQuery += " ORDER BY " + KEY_COMMON_ID + " DESC";

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Date dCreated = new Date();
                try {
                    dCreated = Params.parseDateTime(cursor.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date dModified = new Date();
                try {
                    dModified = Params.parseDateTime(cursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                items.add(new HubItem(mContext, cursor.getInt(0), // mId
                        dCreated, // mCreated
                        dModified, // mModified
                        cursor.getString(3), // mTitle
                        cursor.getString(4), // mTile
                        cursor.getString(5), // mUsername
                        cursor.getString(6), // mPassword
                        cursor.getString(7), // mRemoteService
                        cursor.getInt(8), // mRemoteId
                        cursor.getString(9), // mLocalIP
                        cursor.getString(10), // mHubSSID
                        cursor.getString(11), // mHubWifiPassword
                        cursor.getString(12), // mHubIP
                        cursor.getString(13), // mLocation
                        cursor.getDouble(14), // mLongitude
                        cursor.getDouble(15), // mLatitude
                        cursor.getString(16)
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public Integer getHubCount() {
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_HUB;

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);
        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    public void deleteAllHub() {
        getWritableDatabase().delete(TABLE_HUB, null, null);
    }

    public void deleteHub(Integer iId) {
        deleteAllLocation(iId);
        deleteAllNotification(iId);
        deleteAllDevice(iId);
        deleteAllProfile(iId);
        deleteAllDeviceHistory(iId);
        deleteAllIcon(iId);

        getWritableDatabase().delete(TABLE_HUB, KEY_COMMON_ID + "=" + iId + "", null);
    }

    public Boolean addLocation(LocationItemApp item) {
        ContentValues values = new ContentValues();

        Integer order = item.getOrder();
        if (order.equals(0)) {
            Cursor cursor = getReadableDatabase().query(TABLE_LOCATION, new String[]{"MAX(" + KEY_COMMON_ORDER + ")"}, null, null, null, null, null);
            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                order = cursor.getInt(0);
                order++;
            } else {
                order = 1;
            }
            cursor.close();
        }

        values.put(KEY_COMMON_ID, item.getId()); // No autoincrement
        values.put(KEY_COMMON_HUB_ID, item.getHubId());
        values.put(KEY_LOCATION_TITLE, item.getTitle());
        values.put(KEY_LOCATION_TILE, item.getTile());
        values.put(KEY_LOCATION_USER_IMG, item.getUserImg());
        values.put(KEY_COMMON_ORDER, order);

        return getWritableDatabase().insert(TABLE_LOCATION, null, values) != -1;
    }

    public Integer updateLocation(LocationItemApp item) {
        ContentValues values = new ContentValues();

        // Update without id and Hub id!
        values.put(KEY_LOCATION_TITLE, item.getTitle());
        values.put(KEY_LOCATION_TILE, item.getTile());
        values.put(KEY_LOCATION_USER_IMG, item.getUserImg());
        values.put(KEY_COMMON_ORDER, item.getOrder());

        return getWritableDatabase().update(TABLE_LOCATION, values, KEY_COMMON_ID + "=" + item.getId()
                + " AND " + KEY_COMMON_HUB_ID + "=" + item.getHubId(), null);
    }

    public LocationItemApp getLocation(Integer iId, Integer iHubId) {
        LocationItemApp item = new LocationItemApp(mContext);

        String selection = KEY_COMMON_ID + " = " + iId + " AND " + KEY_COMMON_HUB_ID + " = " + iHubId;

        Cursor cursor = getReadableDatabase().query(TABLE_LOCATION, new String[]{
                        KEY_COMMON_ID,
                        KEY_COMMON_HUB_ID,
                        KEY_LOCATION_TITLE,
                        KEY_LOCATION_TILE,
                        KEY_LOCATION_USER_IMG,
                        KEY_COMMON_ORDER},
                selection, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                item = new LocationItemApp(mContext, cursor.getInt(0), // mId
                        cursor.getInt(1), // mHubId
                        cursor.getString(2), // mTitle
                        cursor.getString(3), // mTile
                        cursor.getString(4), // mUserImg
                        cursor.getInt(5) // Order
                );
            }
            cursor.close();
        }
        return item;
    }

    public ArrayList<LocationItemApp> getAllLocation(Integer iHubId) {
        ArrayList<LocationItemApp> items = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATION;
        selectQuery += " WHERE " + KEY_COMMON_HUB_ID + " = " + iHubId;
        selectQuery += " ORDER BY " + KEY_COMMON_ORDER + " ASC, " + KEY_LOCATION_TITLE + " ASC";

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                items.add(new LocationItemApp(mContext, cursor.getInt(0), // mId
                        cursor.getInt(1), // mHubId
                        cursor.getString(2), // mTitle
                        cursor.getString(3), // mTile
                        cursor.getString(4), // mUserImg
                        cursor.getInt(5) // Order
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public void deleteLocation(Integer iId, Integer iHubId) {
        getWritableDatabase().delete(TABLE_LOCATION, KEY_COMMON_ID + "=" + iId
                + " AND " + KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public void deleteAllLocation(Integer iHubId) {
        getWritableDatabase().delete(TABLE_LOCATION, KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public Boolean addNotification(NotificationItemApp item) {
        deleteOldNotification();

        ContentValues values = new ContentValues();

        values.put(KEY_COMMON_ID, item.getId()); // No autoincrement
        values.put(KEY_COMMON_HUB_ID, item.getHubId());
        values.put(KEY_NOTIFICATION_TIMESTAMP, Params.formatDateTime(item.getTimestamp()));
        values.put(KEY_NOTIFICATION_LEVEL, item.getLevel());
        values.put(KEY_NOTIFICATION_MESSAGE_DEV, item.getMessage().getDev());
        values.put(KEY_NOTIFICATION_MESSAGE_L, item.getMessage().getL());
        values.put(KEY_NOTIFICATION_TYPE, item.getType());
        values.put(KEY_NOTIFICATION_SOURCE, item.getSource());

        return getWritableDatabase().insert(TABLE_NOTIFICATION, null, values) != -1;
    }

    public Integer updateNotification(NotificationItemApp item) {
        ContentValues values = new ContentValues();

        // Update without id and Hub id!
        values.put(KEY_NOTIFICATION_TIMESTAMP, Params.formatDateTime(item.getTimestamp()));
        values.put(KEY_NOTIFICATION_LEVEL, item.getLevel());
        values.put(KEY_NOTIFICATION_MESSAGE_DEV, item.getMessage().getDev());
        values.put(KEY_NOTIFICATION_MESSAGE_L, item.getMessage().getL());
        values.put(KEY_NOTIFICATION_TYPE, item.getType());
        values.put(KEY_NOTIFICATION_SOURCE, item.getSource());

        return getWritableDatabase().update(TABLE_NOTIFICATION, values, KEY_COMMON_ID + "=" + item.getId()
                + " AND " + KEY_COMMON_HUB_ID + "=" + item.getHubId(), null);
    }

    public NotificationItemApp getNotification(Long iId, Integer iHubId, String sSource) {
        NotificationItemApp item = new NotificationItemApp(mContext);

        String selection = KEY_COMMON_ID + " = " + iId
                + " AND " + KEY_COMMON_HUB_ID + " = " + iHubId
                + " AND " + KEY_NOTIFICATION_SOURCE + " = '" + sSource.trim() + "'";

        Cursor cursor = getReadableDatabase().query(TABLE_NOTIFICATION, new String[]{
                        KEY_COMMON_ID,
                        KEY_COMMON_HUB_ID,
                        KEY_NOTIFICATION_TIMESTAMP,
                        KEY_NOTIFICATION_LEVEL,
                        KEY_NOTIFICATION_MESSAGE_DEV,
                        KEY_NOTIFICATION_MESSAGE_L,
                        KEY_NOTIFICATION_TYPE,
                        KEY_NOTIFICATION_SOURCE},
                selection, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {

                Date dTimestamp = new Date();
                try {
                    dTimestamp = Params.parseDateTime(cursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                item = new NotificationItemApp(mContext, cursor.getLong(0), // mId
                        cursor.getInt(1), // mHubId
                        dTimestamp, // mTimestamp
                        cursor.getString(3), // mLevel
                        cursor.getString(4), // mMessageDev
                        cursor.getString(5), // mMessageL
                        cursor.getString(6), // mType
                        cursor.getString(7) // mSource
                );
            }
            cursor.close();
        }
        return item;
    }

    public ArrayList<NotificationItemApp> getAllNotification(Integer iHubId, Boolean bWithFilter) {
        ArrayList<NotificationItemApp> items = new ArrayList<>();

        // Load preferences
        SharedPreferences settings = mContext.getSharedPreferences(Params.PREFS_NAME, 0);
        String notificationFilterType = settings.getString(Params.PREFS_NOTIFICATION_FILTER_TYPE, "");
        String notificationFilterSource = settings.getString(Params.PREFS_NOTIFICATION_FILTER_SOURCE, "");

        String selectQuery = "SELECT * FROM " + TABLE_NOTIFICATION;
        selectQuery += " WHERE " + KEY_COMMON_HUB_ID + " = " + iHubId;

        if (bWithFilter) {
            if (!notificationFilterType.isEmpty()) {
                selectQuery += " AND " + KEY_NOTIFICATION_TYPE + " = '" + notificationFilterType + "'";
            }
            if (!notificationFilterSource.isEmpty()) {
                selectQuery += " AND " + KEY_NOTIFICATION_SOURCE + " = '" + notificationFilterSource + "'";
            }
        }

        selectQuery += " ORDER BY " + KEY_NOTIFICATION_TIMESTAMP + " DESC";

        Log.d(Params.LOGGING_TAG, selectQuery);

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                Date dTimestamp = new Date();
                try {
                    dTimestamp = Params.parseDateTime(cursor.getString(2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                items.add(new NotificationItemApp(mContext, cursor.getLong(0), // mId
                        cursor.getInt(1), // mHubId
                        dTimestamp, // mTimestamp
                        cursor.getString(3), // mLevel
                        cursor.getString(4), // mMessageDev
                        cursor.getString(5), // mMessageL
                        cursor.getString(6), // mType
                        cursor.getString(7) // mSource
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public Integer getNotificationCount() {
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_NOTIFICATION;

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);
        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    public void deleteNotification(Long iId, Integer iHubId, String sSource) {
        getWritableDatabase().delete(TABLE_NOTIFICATION, KEY_COMMON_ID + "=" + iId
                + " AND " + KEY_COMMON_HUB_ID + "=" + iHubId
                + " AND " + KEY_NOTIFICATION_SOURCE + "='" + sSource + "'", null);
    }

    public void deleteAllNotification(Integer iHubId) {
        getWritableDatabase().delete(TABLE_NOTIFICATION, KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    private void deleteOldNotification() {
        String deleteQuery =
                "DELETE FROM " + TABLE_NOTIFICATION
                        + " WHERE " + KEY_COMMON_ID + " < ("
                        + " SELECT MIN(" + KEY_COMMON_ID + ")"
                        + " FROM (SELECT " + KEY_COMMON_ID + " FROM " + TABLE_NOTIFICATION + " ORDER BY " + KEY_COMMON_ID + " DESC" + " LIMIT 499)"
                        + " )";

        getWritableDatabase().execSQL(deleteQuery);
    }

    public Boolean addDevice(DeviceItemApp item) {
        ContentValues values = new ContentValues();

        Integer order = item.getOrder();
        if (order.equals(0)) {
            Cursor cursor = getReadableDatabase().query(TABLE_DEVICE, new String[]{"MAX(" + KEY_COMMON_ORDER + ")"}, null, null, null, null, null);
            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                order = cursor.getInt(0);
                order++;
            } else {
                order = 1;
            }
            cursor.close();
        }

        values.put(KEY_DEVICE_CREATION_TIME, item.getCreationTime());
        values.put(KEY_DEVICE_CREATOR_ID, item.getCreatorId());
        values.put(KEY_DEVICE_DEVICE_TYPE, item.getDeviceType());
        values.put(KEY_DEVICE_H, item.getH());
        values.put(KEY_DEVICE_HAS_HISTORY, item.getHasHistory() ? 1 : 0);
        values.put(KEY_DEVICE_DEVICE_ID, item.getDeviceId()); // Primary key (with Hub Id)
        values.put(KEY_DEVICE_LOCATION, item.getLocation());
        values.put(KEY_DEVICE_PERMANENTLY_HIDDEN, item.getPermanentlyHidden() ? 1 : 0);
        values.put(KEY_DEVICE_PROBE_TYPE, item.getProbeType());
        values.put(KEY_DEVICE_VISIBILITY, item.getVisibility() ? 1 : 0);
        values.put(KEY_DEVICE_UPDATE_TIME, item.getUpdateTime());
        values.put(KEY_DEVICE_METRICS_ICON, item.getMetrics().getIcon());
        values.put(KEY_DEVICE_METRICS_TITLE, item.getMetrics().getTitle());
        values.put(KEY_DEVICE_METRICS_LEVEL, item.getMetrics().getLevel());
        values.put(KEY_DEVICE_METRICS_PROBE_TITLE, item.getMetrics().getProbeTitle());
        values.put(KEY_DEVICE_METRICS_SCALE_TITLE, item.getMetrics().getScaleTitle());
        values.put(KEY_DEVICE_COLOR_RED, item.getMetrics().getColor().getRed());
        values.put(KEY_DEVICE_COLOR_GREEN, item.getMetrics().getColor().getGreen());
        values.put(KEY_DEVICE_COLOR_BLUE, item.getMetrics().getColor().getBlue());
        values.put(KEY_DEVICE_MIN, item.getMetrics().getMin());
        values.put(KEY_DEVICE_MAX, item.getMetrics().getMax());
        values.put(KEY_DEVICE_METRICS_CAMERA_STREAM_URL, item.getMetrics().getCameraStreamUrl());
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_IN, item.getMetrics().getCameraHasZoomIn() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OUT, item.getMetrics().getCameraHasZoomOut() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_LEFT, item.getMetrics().getCameraHasLeft() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_RIGHT, item.getMetrics().getCameraHasRight() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_UP, item.getMetrics().getCameraHasUp() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_DOWN, item.getMetrics().getCameraHasDown() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OPEN, item.getMetrics().getCameraHasOpen() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_CLOSE, item.getMetrics().getCameraHasClose() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_DISCRETE_CURRENT_SCENE, item.getMetrics().getDiscreteCurrentScene());
        values.put(KEY_DEVICE_METRICS_DISCRETE_KEY_ATTRIBUTE, item.getMetrics().getDiscreteKeyAttribute());
        values.put(KEY_DEVICE_METRICS_DISCRETE_STATE, item.getMetrics().getDiscreteState());
        values.put(KEY_DEVICE_METRICS_DISCRETE_MAX_SCENES, item.getMetrics().getDiscreteMaxScenes());
        values.put(KEY_DEVICE_METRICS_DISCRETE_COUNT, item.getMetrics().getDiscreteCount());
        values.put(KEY_DEVICE_METRICS_DISCRETE_TYPE, item.getMetrics().getDiscreteType());
        values.put(KEY_DEVICE_METRICS_TEXT, item.getMetrics().getText());
        values.put(KEY_DEVICE_TAGS, new Gson().toJson(item.getTags()));
        values.put(KEY_DEVICE_CUSTOM_ICONS, new Gson().toJson(item.getIcons()));
        values.put(KEY_DEVICE_ICON, item.getIcon());
        values.put(KEY_COMMON_HUB_ID, item.getHubId());
        values.put(KEY_COMMON_ORDER, order);

        return getWritableDatabase().insert(TABLE_DEVICE, null, values) != -1;
    }

    public Integer updateDevice(DeviceItemApp item) {
        ContentValues values = new ContentValues();

        // Update without device id and Hub id!
        values.put(KEY_DEVICE_CREATION_TIME, item.getCreationTime());
        values.put(KEY_DEVICE_CREATOR_ID, item.getCreatorId());
        values.put(KEY_DEVICE_DEVICE_TYPE, item.getDeviceType());
        values.put(KEY_DEVICE_H, item.getH());
        values.put(KEY_DEVICE_HAS_HISTORY, item.getHasHistory() ? 1 : 0);
        values.put(KEY_DEVICE_LOCATION, item.getLocation());
        values.put(KEY_DEVICE_PERMANENTLY_HIDDEN, item.getPermanentlyHidden() ? 1 : 0);
        values.put(KEY_DEVICE_PROBE_TYPE, item.getProbeType());
        values.put(KEY_DEVICE_VISIBILITY, item.getVisibility() ? 1 : 0);
        values.put(KEY_DEVICE_UPDATE_TIME, item.getUpdateTime());
        values.put(KEY_DEVICE_METRICS_ICON, item.getMetrics().getIcon());
        values.put(KEY_DEVICE_METRICS_TITLE, item.getMetrics().getTitle());
        values.put(KEY_DEVICE_METRICS_LEVEL, item.getMetrics().getLevel());
        values.put(KEY_DEVICE_METRICS_PROBE_TITLE, item.getMetrics().getProbeTitle());
        values.put(KEY_DEVICE_METRICS_SCALE_TITLE, item.getMetrics().getScaleTitle());
        values.put(KEY_DEVICE_COLOR_RED, item.getMetrics().getColor().getRed());
        values.put(KEY_DEVICE_COLOR_GREEN, item.getMetrics().getColor().getGreen());
        values.put(KEY_DEVICE_COLOR_BLUE, item.getMetrics().getColor().getBlue());
        values.put(KEY_DEVICE_MIN, item.getMetrics().getMin());
        values.put(KEY_DEVICE_MAX, item.getMetrics().getMax());
        values.put(KEY_DEVICE_METRICS_CAMERA_STREAM_URL, item.getMetrics().getCameraStreamUrl());
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_IN, item.getMetrics().getCameraHasZoomIn() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OUT, item.getMetrics().getCameraHasZoomOut() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_LEFT, item.getMetrics().getCameraHasLeft() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_RIGHT, item.getMetrics().getCameraHasRight() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_UP, item.getMetrics().getCameraHasUp() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_DOWN, item.getMetrics().getCameraHasDown() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OPEN, item.getMetrics().getCameraHasOpen() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_CLOSE, item.getMetrics().getCameraHasClose() ? 1 : 0);
        values.put(KEY_DEVICE_METRICS_DISCRETE_CURRENT_SCENE, item.getMetrics().getDiscreteCurrentScene());
        values.put(KEY_DEVICE_METRICS_DISCRETE_KEY_ATTRIBUTE, item.getMetrics().getDiscreteKeyAttribute());
        values.put(KEY_DEVICE_METRICS_DISCRETE_STATE, item.getMetrics().getDiscreteState());
        values.put(KEY_DEVICE_METRICS_DISCRETE_MAX_SCENES, item.getMetrics().getDiscreteMaxScenes());
        values.put(KEY_DEVICE_METRICS_DISCRETE_COUNT, item.getMetrics().getDiscreteCount());
        values.put(KEY_DEVICE_METRICS_DISCRETE_TYPE, item.getMetrics().getDiscreteType());
        values.put(KEY_DEVICE_METRICS_TEXT, item.getMetrics().getText());
        values.put(KEY_DEVICE_TAGS, new Gson().toJson(item.getTags()));
        values.put(KEY_DEVICE_CUSTOM_ICONS, new Gson().toJson(item.getIcons()));
        values.put(KEY_DEVICE_ICON, item.getIcon());
        values.put(KEY_COMMON_ORDER, item.getOrder());

        return getWritableDatabase().update(TABLE_DEVICE, values, KEY_DEVICE_DEVICE_ID + " = '" + item.getDeviceId()
                + "' AND " + KEY_COMMON_HUB_ID + "=" + item.getHubId(), null);
    }

    public DeviceItemApp getDevice(String sDeviceId, Integer iHubId) {
        DeviceItemApp item = new DeviceItemApp(mContext);

        String selection = KEY_DEVICE_DEVICE_ID + " = '" + sDeviceId + "' AND " + KEY_COMMON_HUB_ID + " = " + iHubId;

        Cursor cursor = getReadableDatabase().query(TABLE_DEVICE, new String[]{
                        KEY_DEVICE_CREATION_TIME,
                        KEY_DEVICE_CREATOR_ID,
                        KEY_DEVICE_DEVICE_TYPE,
                        KEY_DEVICE_H,
                        KEY_DEVICE_HAS_HISTORY,
                        KEY_DEVICE_DEVICE_ID,
                        KEY_DEVICE_LOCATION,
                        KEY_DEVICE_PERMANENTLY_HIDDEN,
                        KEY_DEVICE_PROBE_TYPE,
                        KEY_DEVICE_VISIBILITY,
                        KEY_DEVICE_UPDATE_TIME,
                        KEY_DEVICE_METRICS_ICON,
                        KEY_DEVICE_METRICS_TITLE,
                        KEY_DEVICE_METRICS_LEVEL,
                        KEY_DEVICE_METRICS_PROBE_TITLE,
                        KEY_DEVICE_METRICS_SCALE_TITLE,
                        KEY_DEVICE_COLOR_RED,
                        KEY_DEVICE_COLOR_GREEN,
                        KEY_DEVICE_COLOR_BLUE,
                        KEY_DEVICE_MIN,
                        KEY_DEVICE_MAX,
                        KEY_DEVICE_METRICS_CAMERA_STREAM_URL,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_IN,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OUT,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_LEFT,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_RIGHT,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_UP,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_DOWN,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_OPEN,
                        KEY_DEVICE_METRICS_CAMERA_HAS_ZOOM_CLOSE,
                        KEY_DEVICE_METRICS_DISCRETE_CURRENT_SCENE,
                        KEY_DEVICE_METRICS_DISCRETE_KEY_ATTRIBUTE,
                        KEY_DEVICE_METRICS_DISCRETE_STATE,
                        KEY_DEVICE_METRICS_DISCRETE_MAX_SCENES,
                        KEY_DEVICE_METRICS_DISCRETE_COUNT,
                        KEY_DEVICE_METRICS_DISCRETE_TYPE,
                        KEY_DEVICE_METRICS_TEXT,
                        KEY_DEVICE_TAGS,
                        KEY_DEVICE_CUSTOM_ICONS,
                        KEY_COMMON_HUB_ID,
                        KEY_DEVICE_ICON,
                        KEY_COMMON_ORDER},
                selection, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                List<String> tags = new Gson().fromJson(cursor.getString(37), collectionType);
                Icons customIcons = new Gson().fromJson(cursor.getString(38), Icons.class);

                item = new DeviceItemApp(mContext, cursor.getInt(0), // mCreationTime
                        cursor.getInt(1), // mCreatorId
                        cursor.getString(2), // mDeviceType
                        cursor.getInt(3), //  mH
                        cursor.getInt(4) != 0, // mHasHistory
                        cursor.getString(5), // mDeviceId
                        cursor.getInt(6), // mLocation
                        cursor.getInt(7) != 0, // mPermanentlyHidden
                        cursor.getString(8), // mProbeType
                        cursor.getInt(9) != 0, // mVisibility
                        cursor.getInt(10), // mUpdateTime
                        cursor.getString(11), // mMetricsIcon
                        cursor.getString(12), // mMetricsTitle
                        cursor.getString(13), // mMetricsLevel
                        cursor.getString(14), // mMetricsProbeTitle
                        cursor.getString(15), // mMetricsScaleTitle
                        cursor.getInt(16), // mMetricsColorRed
                        cursor.getInt(17), // mMetricsColorGreen
                        cursor.getInt(18), // mMetricsColorBlue
                        cursor.getInt(19), // mMetricsMin
                        cursor.getInt(20), // mMetricsMax
                        cursor.getString(21), // mMetricsCameraStreamUrl
                        cursor.getInt(22) != 0, // mMetricsCameraHasZoomIn
                        cursor.getInt(23) != 0, // mMetricsCameraHasZoomOut
                        cursor.getInt(24) != 0, // mMetricsCameraHasLeft
                        cursor.getInt(25) != 0, // mMetricsCameraHasRight
                        cursor.getInt(26) != 0, // mMetricsCameraHasUp
                        cursor.getInt(27) != 0, // mMetricsCameraHasDown
                        cursor.getInt(28) != 0, // mMetricsCameraHasOpen
                        cursor.getInt(29) != 0, // mMetricsCameraHasClose
                        cursor.getInt(30), // mDiscreteCurrentScene
                        cursor.getInt(31), // mDiscreteKeyAttribute
                        cursor.getString(32), // mDiscreteState
                        cursor.getInt(33), // mDiscreteMaxScenes
                        cursor.getInt(34), // mDiscreteCount
                        cursor.getString(35), // mDiscreteType
                        cursor.getString(36), // mMetricsText
                        tags, // mTags
                        customIcons, // mCustomIcons
                        cursor.getInt(39), // mHubId
                        cursor.getString(40), // mIcon
                        cursor.getInt(41) // mOrder
                );

                item.setLocationItem(getLocation(item.getLocation(), iHubId));
            }
            cursor.close();
        }
        return item;
    }

    public ArrayList<DeviceItemApp> getAllDevice(Integer iHubId, Boolean bWithFilter, Boolean bOnlyDashboard, Integer locationId) {
        // Load preferences
        SharedPreferences settings = mContext.getSharedPreferences(Params.PREFS_NAME, 0);
        String deviceFilterDeviceType = settings.getString(Params.PREFS_DEVICE_FILTER_DEVICE_TYPE, "");
        String deviceFilterTag = settings.getString(Params.PREFS_DEVICE_FILTER_TAG, "");
        Integer deviceSorting = settings.getInt(Params.PREFS_DEVICE_SORTING, ZWayUtil.SORTING_CUSTOM);

        String selectQuery = "SELECT * FROM " + TABLE_DEVICE;
        selectQuery += " WHERE " + KEY_COMMON_HUB_ID + " = " + iHubId
                + " AND " + KEY_DEVICE_VISIBILITY + " = 1"
                + " AND " + KEY_DEVICE_PERMANENTLY_HIDDEN + " = 0";

        if (locationId != -1) {
            selectQuery += " AND " + KEY_DEVICE_LOCATION + " = " + locationId;
        }

        if (bWithFilter) {
            if (bOnlyDashboard) {
                // Dashboard without device type and tag elements
                ProfileItemApp currentProfile = getProfile(settings.getInt(Params.PREFS_CURRENT_PROFILE_ID, -1), iHubId);
                if (currentProfile.getId() != -1 && currentProfile.getDashboard().size() > 0) {
                    selectQuery += " AND (";

                    Integer i = 0;
                    for (String dashboard : currentProfile.getDashboard()) {
                        if (i > 0) {
                            selectQuery += " OR ";
                        }
                        selectQuery += KEY_DEVICE_DEVICE_ID + " = '" + dashboard + "'";

                        i++;
                    }

                    selectQuery += " )";
                } else {
                    // No dashboard elements available!
                    return new ArrayList<>();
                }

                if (deviceSorting.equals(ZWayUtil.SORTING_CUSTOM)) {
                    selectQuery += " ORDER BY " + KEY_COMMON_ORDER + " ASC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_OLDEST)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_CREATION_TIME + " ASC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_NEWEST)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_CREATION_TIME + " DESC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_NAME_ASC)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_NAME_DESC)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_METRICS_TITLE + " DESC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_LAST_UPDATED)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_UPDATE_TIME + " DESC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                }
            } else {
                if (!deviceFilterDeviceType.isEmpty()) {
                    selectQuery += " AND " + KEY_DEVICE_DEVICE_TYPE + " = '" + deviceFilterDeviceType + "'";
                }
                if (!deviceFilterTag.isEmpty()) {
                    selectQuery += " AND " + KEY_DEVICE_TAGS + " LIKE '%" + deviceFilterTag + "%'";
                }
                if (deviceSorting.equals(ZWayUtil.SORTING_CUSTOM)) {
                    selectQuery += " ORDER BY " + KEY_COMMON_ORDER + " ASC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_OLDEST)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_CREATION_TIME + " ASC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_NEWEST)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_CREATION_TIME + " DESC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_NAME_ASC)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_METRICS_TITLE + " ASC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_NAME_DESC)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_METRICS_TITLE + " DESC";
                } else if (deviceSorting.equals(ZWayUtil.SORTING_LAST_UPDATED)) {
                    selectQuery += " ORDER BY " + KEY_DEVICE_UPDATE_TIME + " DESC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
                }
            }
        } else {
            selectQuery += " ORDER BY " + KEY_COMMON_ORDER + " ASC, " + KEY_DEVICE_METRICS_TITLE + " ASC";
        }

        return getAllDevices(selectQuery, iHubId);
    }

    private ArrayList<DeviceItemApp> getAllDevices(String query, Integer iHubId) {
        ArrayList<DeviceItemApp> items = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                List<String> tags = new Gson().fromJson(cursor.getString(37), collectionType);
                Icons customIcons = new Gson().fromJson(cursor.getString(38), Icons.class);

                DeviceItemApp item = new DeviceItemApp(mContext, cursor.getInt(0), // mCreationTime
                        cursor.getInt(1), // mCreatorId
                        cursor.getString(2), // mDeviceType
                        cursor.getInt(3), //  mH
                        cursor.getInt(4) != 0, // mHasHistory
                        cursor.getString(5), // mDeviceId
                        cursor.getInt(6), // mLocation
                        cursor.getInt(7) != 0, // mPermanentlyHidden
                        cursor.getString(8), // mProbeType
                        cursor.getInt(9) != 0, // mVisibility
                        cursor.getInt(10), // mUpdateTime
                        cursor.getString(11), // mMetricsIcon
                        cursor.getString(12), // mMetricsTitle
                        cursor.getString(13), // mMetricsLevel
                        cursor.getString(14), // mMetricsProbeTitle
                        cursor.getString(15), // mMetricsScaleTitle
                        cursor.getInt(16), // mMetricsColorRed
                        cursor.getInt(17), // mMetricsColorGreen
                        cursor.getInt(18), // mMetricsColorBlue
                        cursor.getInt(19), // mMetricsMin
                        cursor.getInt(20), // mMetricsMax
                        cursor.getString(21), // mMetricsCameraStreamUrl
                        cursor.getInt(22) != 0, // mMetricsCameraHasZoomIn
                        cursor.getInt(23) != 0, // mMetricsCameraHasZoomOut
                        cursor.getInt(24) != 0, // mMetricsCameraHasLeft
                        cursor.getInt(25) != 0, // mMetricsCameraHasRight
                        cursor.getInt(26) != 0, // mMetricsCameraHasUp
                        cursor.getInt(27) != 0, // mMetricsCameraHasDown
                        cursor.getInt(28) != 0, // mMetricsCameraHasOpen
                        cursor.getInt(29) != 0, // mMetricsCameraHasClose
                        cursor.getInt(30), // mDiscreteCurrentScene
                        cursor.getInt(31), // mDiscreteKeyAttribute
                        cursor.getString(32), // mDiscreteState
                        cursor.getInt(33), // mDiscreteMaxScenes
                        cursor.getInt(34), // mDiscreteCount
                        cursor.getString(35), // mDiscreteType
                        cursor.getString(36), // mMetricsText
                        tags, // mTags
                        customIcons, // mCustomIcons
                        cursor.getInt(39), // mHubId
                        cursor.getString(40), // mIcon
                        cursor.getInt(41) // mOrder
                );

                item.setLocationItem(getLocation(item.getLocation(), iHubId));

                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public Set<String> getAllDeviceTags(Integer iHubId) {
        HashSet<String> tags = new HashSet<>();
        ArrayList<DeviceItemApp> devices = getAllDevice(iHubId, false, false, -1);

        for (DeviceItemApp deviceItem : devices) {
            tags.addAll(deviceItem.getTags());
        }

        return tags;
    }

    public Set<String> getAllDeviceTypes(Integer iHubId) {
        HashSet<String> deviceTypes = new HashSet<>();
        ArrayList<DeviceItemApp> devices = getAllDevice(iHubId, false, false, -1);

        for (DeviceItemApp deviceItem : devices) {
            deviceTypes.add(deviceItem.getDeviceType());
        }

        return deviceTypes;
    }

    public Set<String> getAllDeviceSources(Integer iHubId) {
        HashSet<String> deviceSources = new HashSet<>();
        ArrayList<DeviceItemApp> devices = getAllDevice(iHubId, false, false, -1);

        for (DeviceItemApp deviceItem : devices) {
            deviceSources.add(deviceItem.getDeviceId());
        }

        return deviceSources;
    }

    public void deleteDevice(String sDeviceId, Integer iHubId) {
        getWritableDatabase().delete(TABLE_DEVICE, KEY_DEVICE_DEVICE_ID + " = '" + sDeviceId
                + "' AND " + KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public void deleteAllDevice(Integer iHubId) {
        getWritableDatabase().delete(TABLE_DEVICE, KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public Boolean addProfile(ProfileItemApp item) {
        ContentValues values = new ContentValues();

        values.put(KEY_COMMON_ID, item.getId()); // No autoincrement
        values.put(KEY_COMMON_HUB_ID, item.getHubId());
        values.put(KEY_PROFILE_NAME, item.getName());
        values.put(KEY_PROFILE_LANG, item.getLang());
        values.put(KEY_PROFILE_COLOR, item.getColor());
        values.put(KEY_PROFILE_DASHBOARD, new Gson().toJson(item.getDashboard()));
        values.put(KEY_PROFILE_INTERVAL, item.getInterval());
        values.put(KEY_PROFILE_ROOMS, new Gson().toJson(item.getRooms()));
        values.put(KEY_PROFILE_EXPERT_VIEW, item.getExpertView() ? 1 : 0);
        values.put(KEY_PROFILE_HIDE_ALL_DEVICE_EVENTS, item.getHideAllDeviceEvents() ? 1 : 0);
        values.put(KEY_PROFILE_HIDE_SYSTEM_EVENTS, item.getHideSystemEvents() ? 1 : 0);
        values.put(KEY_PROFILE_HIDE_SINGLE_DEVICE_EVENTS, new Gson().toJson(item.getHideSingleDeviceEvents()));
        values.put(KEY_PROFILE_EMAIL, item.getEmail());

        return getWritableDatabase().insert(TABLE_PROFILE, null, values) != -1;
    }

    public Integer updateProfile(ProfileItemApp item) {
        ContentValues values = new ContentValues();

        // Update without id and Hub id!
        values.put(KEY_PROFILE_NAME, item.getName());
        values.put(KEY_PROFILE_LANG, item.getLang());
        values.put(KEY_PROFILE_COLOR, item.getColor());
        values.put(KEY_PROFILE_DASHBOARD, new Gson().toJson(item.getDashboard()));
        values.put(KEY_PROFILE_INTERVAL, item.getInterval());
        values.put(KEY_PROFILE_ROOMS, new Gson().toJson(item.getRooms()));
        values.put(KEY_PROFILE_EXPERT_VIEW, item.getExpertView() ? 1 : 0);
        values.put(KEY_PROFILE_HIDE_ALL_DEVICE_EVENTS, item.getHideAllDeviceEvents() ? 1 : 0);
        values.put(KEY_PROFILE_HIDE_SYSTEM_EVENTS, item.getHideSystemEvents() ? 1 : 0);
        values.put(KEY_PROFILE_HIDE_SINGLE_DEVICE_EVENTS, new Gson().toJson(item.getHideSingleDeviceEvents()));
        values.put(KEY_PROFILE_EMAIL, item.getEmail());

        return getWritableDatabase().update(TABLE_PROFILE, values, KEY_COMMON_ID + "=" + item.getId()
                + " AND " + KEY_COMMON_HUB_ID + "=" + item.getHubId(), null);
    }

    public ProfileItemApp getProfile(Integer iId, Integer iHubId) {
        ProfileItemApp item = new ProfileItemApp(mContext);

        String selection = KEY_COMMON_ID + " = " + iId + " AND " + KEY_COMMON_HUB_ID + " = " + iHubId;

        Cursor cursor = getReadableDatabase().query(TABLE_PROFILE, new String[]{
                        KEY_COMMON_ID,
                        KEY_COMMON_HUB_ID,
                        KEY_PROFILE_NAME,
                        KEY_PROFILE_LANG,
                        KEY_PROFILE_COLOR,
                        KEY_PROFILE_DASHBOARD,
                        KEY_PROFILE_INTERVAL,
                        KEY_PROFILE_ROOMS,
                        KEY_PROFILE_EXPERT_VIEW,
                        KEY_PROFILE_HIDE_ALL_DEVICE_EVENTS,
                        KEY_PROFILE_HIDE_SYSTEM_EVENTS,
                        KEY_PROFILE_HIDE_SINGLE_DEVICE_EVENTS,
                        KEY_PROFILE_EMAIL},
                selection, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                HashSet<String> dashboard = new Gson().fromJson(cursor.getString(5), setType);
                HashSet<Integer> rooms = new Gson().fromJson(cursor.getString(7), setTypeInteger);
                HashSet<String> hideSingleDeviceEvents = new Gson().fromJson(cursor.getString(11), setType);

                item = new ProfileItemApp(mContext, cursor.getInt(0), // mId
                        cursor.getInt(1), // mHubId
                        cursor.getString(2), // mName
                        cursor.getString(3), // mLang
                        cursor.getString(4), // mColor
                        dashboard, // mDashboard
                        cursor.getInt(6), // mInterval
                        rooms, // mRooms
                        cursor.getInt(8) != 0, // mExpertView
                        cursor.getInt(9) != 0, // mHideAllDeciceEvents
                        cursor.getInt(10) != 0, // mHideSystemEvents
                        hideSingleDeviceEvents, // mHideSingleDeciveEvents
                        cursor.getString(12) // mEmail
                );
            }
            cursor.close();
        }
        return item;
    }

    public ArrayList<ProfileItemApp> getAllProfile(Integer iHubId) {
        ArrayList<ProfileItemApp> items = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_PROFILE;
        selectQuery += " WHERE " + KEY_COMMON_HUB_ID + " = " + iHubId;
        selectQuery += " ORDER BY " + KEY_COMMON_ID + " ASC";

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashSet<String> dashboard = new Gson().fromJson(cursor.getString(5), setType);
                HashSet<Integer> rooms = new Gson().fromJson(cursor.getString(7), setTypeInteger);
                HashSet<String> hideSingleDeviceEvents = new Gson().fromJson(cursor.getString(11), setType);

                items.add(new ProfileItemApp(mContext, cursor.getInt(0), // mId
                        cursor.getInt(1), // mHubId
                        cursor.getString(2), // mName
                        cursor.getString(3), // mLang
                        cursor.getString(4), // mColor
                        dashboard, // mDashboard
                        cursor.getInt(6), // mInterval
                        rooms, // mRooms
                        cursor.getInt(8) != 0, // mExpertView
                        cursor.getInt(9) != 0, // mHideAllDeciceEvents
                        cursor.getInt(10) != 0, // mHideSystemEvents
                        hideSingleDeviceEvents, // mHideSingleDeciveEvents
                        cursor.getString(12) // mEmail
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public void deleteProfile(Integer iId, Integer iHubId) {
        getWritableDatabase().delete(TABLE_PROFILE, KEY_COMMON_ID + "=" + iId
                + " AND " + KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public void deleteAllProfile(Integer iHubId) {
        getWritableDatabase().delete(TABLE_PROFILE, KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public Boolean addDeviceHistory(DeviceHistoryItemApp item) {
        ContentValues values = new ContentValues();

        values.put(KEY_DEVICE_HISTORY_DEVICE_ID, item.getDeviceId()); // No autoincrement
        values.put(KEY_COMMON_HUB_ID, item.getHubId());
        values.put(KEY_DEVICE_HISTORY_DEVICE_TYPE, item.getDeviceType());
        values.put(KEY_DEVICE_HISTORY_DEVICE_HISTORY_DATA, new Gson().toJson(item.getHistoryData()));

        return getWritableDatabase().insert(TABLE_DEVICE_HISTORY, null, values) != -1;
    }

    public Integer updateDeviceHistory(DeviceHistoryItemApp item) {
        ContentValues values = new ContentValues();

        // Update without id and Hub id!
        values.put(KEY_DEVICE_HISTORY_DEVICE_TYPE, item.getDeviceType());
        values.put(KEY_DEVICE_HISTORY_DEVICE_HISTORY_DATA, new Gson().toJson(item.getHistoryData()));

        return getWritableDatabase().update(TABLE_DEVICE_HISTORY, values, KEY_DEVICE_HISTORY_DEVICE_ID + "='" + item.getDeviceId() + "'"
                + " AND " + KEY_COMMON_HUB_ID + "=" + item.getHubId(), null);
    }

    public DeviceHistoryItemApp getDeviceHistory(String sDeviceId, Integer iHubId) {
        DeviceHistoryItemApp item = new DeviceHistoryItemApp(mContext);

        String selection = KEY_DEVICE_HISTORY_DEVICE_ID + " = '" + sDeviceId + "' AND " + KEY_COMMON_HUB_ID + " = " + iHubId;

        Cursor cursor = getReadableDatabase().query(TABLE_DEVICE_HISTORY, new String[]{
                        KEY_DEVICE_HISTORY_DEVICE_ID,
                        KEY_COMMON_HUB_ID,
                        KEY_DEVICE_HISTORY_DEVICE_TYPE,
                        KEY_DEVICE_HISTORY_DEVICE_HISTORY_DATA},
                selection, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                ArrayList<DeviceHistoryData> historyData = new Gson().fromJson(cursor.getString(3), collectionTypeHistoryData);

                item = new DeviceHistoryItemApp(mContext, cursor.getString(0), // mDeviceId
                        cursor.getInt(1), // mHubId
                        cursor.getString(2), // mDeviceType
                        historyData // mHistoryData
                );
            }
            cursor.close();
        }
        return item;
    }

    public ArrayList<DeviceHistoryItemApp> getAllDeviceHistory(Integer iHubId) {
        ArrayList<DeviceHistoryItemApp> items = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_DEVICE_HISTORY;
        selectQuery += " WHERE " + KEY_COMMON_HUB_ID + " = " + iHubId;
        selectQuery += " ORDER BY " + KEY_DEVICE_HISTORY_DEVICE_ID + " ASC";

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ArrayList<DeviceHistoryData> historyData = new Gson().fromJson(cursor.getString(3), collectionTypeHistoryData);

                items.add(new DeviceHistoryItemApp(mContext, cursor.getString(0), // mDeviceId
                        cursor.getInt(1), // mHubId
                        cursor.getString(2), // mDeviceType
                        historyData // mHistoryData
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public void deleteDeviceHistory(String sDeviceId, Integer iHubId) {
        getWritableDatabase().delete(TABLE_DEVICE_HISTORY, KEY_DEVICE_HISTORY_DEVICE_ID + "='" + sDeviceId + "'"
                + " AND " + KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public void deleteAllDeviceHistory(Integer iHubId) {
        getWritableDatabase().delete(TABLE_DEVICE_HISTORY, KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public Boolean addIcon(IconItemApp item) {
        ContentValues values = new ContentValues();

        values.put(KEY_COMMON_HUB_ID, item.getHubId());
        values.put(KEY_DEVICE_ICON_ICON_PATH, item.getIconPath());
        values.put(KEY_DEVICE_ICON_FILE, item.getFile());
        values.put(KEY_DEVICE_ICON_ORG_FILE, item.getOrgFile());
        values.put(KEY_DEVICE_ICON_SOURCE, item.getSource());
        values.put(KEY_DEVICE_ICON_NAME, item.getName());
        values.put(KEY_DEVICE_ICON_ID, item.getId());
        values.put(KEY_DEVICE_ICON_TIMESTAMP, item.getTimestamp());
        values.put(KEY_DEVICE_ICON_SOURCE_TITLE, item.getSourceTitle());

        return getWritableDatabase().insert(TABLE_ICON, null, values) != -1;
    }

    public Integer updateIcon(IconItemApp item) {
        ContentValues values = new ContentValues();

        // Update without file and Hub id!
        values.put(KEY_DEVICE_ICON_ICON_PATH, item.getIconPath());
        values.put(KEY_DEVICE_ICON_ORG_FILE, item.getOrgFile());
        values.put(KEY_DEVICE_ICON_SOURCE, item.getSource());
        values.put(KEY_DEVICE_ICON_NAME, item.getName());
        values.put(KEY_DEVICE_ICON_ID, item.getId());
        values.put(KEY_DEVICE_ICON_TIMESTAMP, item.getTimestamp());
        values.put(KEY_DEVICE_ICON_SOURCE_TITLE, item.getSourceTitle());

        return getWritableDatabase().update(TABLE_ICON, values, KEY_DEVICE_ICON_FILE + "='" + item.getFile() + "'"
                + " AND " + KEY_COMMON_HUB_ID + "=" + item.getHubId(), null);
    }

    public IconItemApp getIcon(String sFile, Integer iHubId) {
        IconItemApp item = new IconItemApp(mContext);

        String selection = KEY_DEVICE_ICON_FILE + " = '" + sFile + "' AND " + KEY_COMMON_HUB_ID + " = " + iHubId;

        Cursor cursor = getReadableDatabase().query(TABLE_ICON, new String[]{
                        KEY_COMMON_HUB_ID,
                        KEY_DEVICE_ICON_ICON_PATH,
                        KEY_DEVICE_ICON_FILE,
                        KEY_DEVICE_ICON_ORG_FILE,
                        KEY_DEVICE_ICON_SOURCE,
                        KEY_DEVICE_ICON_NAME,
                        KEY_DEVICE_ICON_ID,
                        KEY_DEVICE_ICON_TIMESTAMP,
                        KEY_DEVICE_ICON_SOURCE_TITLE},
                selection, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                item = new IconItemApp(mContext, cursor.getInt(0), // mHubId
                        cursor.getString(1), // mIconPath
                        cursor.getString(2), // mFile
                        cursor.getString(3), // mOrgFile
                        cursor.getString(4), // mSource
                        cursor.getString(5), // mName
                        cursor.getString(6), // mId
                        cursor.getLong(7), // mTimestamp
                        cursor.getString(8) // mSourceTitle
                );
            }
            cursor.close();
        }
        return item;
    }

    public ArrayList<IconItemApp> getAllIcon(Integer iHubId) {
        ArrayList<IconItemApp> items = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ICON;
        selectQuery += " WHERE " + KEY_COMMON_HUB_ID + " = " + iHubId;
        selectQuery += " ORDER BY " + KEY_DEVICE_ICON_TIMESTAMP + " DESC";

        Cursor cursor = getReadableDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                items.add(new IconItemApp(mContext, cursor.getInt(0), // mHubId
                        cursor.getString(1), // mIconPath
                        cursor.getString(2), // mFile
                        cursor.getString(3), // mOrgFile
                        cursor.getString(4), // mSource
                        cursor.getString(5), // mName
                        cursor.getString(6), // mId
                        cursor.getLong(7), // mTimestamp
                        cursor.getString(8) // mSourceTitle
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    public void deleteIcon(String sFile, Integer iHubId) {
        getWritableDatabase().delete(TABLE_ICON, KEY_DEVICE_ICON_FILE + "='" + sFile + "'"
                + " AND " + KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }

    public void deleteAllIcon(Integer iHubId) {
        getWritableDatabase().delete(TABLE_ICON, KEY_COMMON_HUB_ID + "=" + iHubId, null);
    }
}
