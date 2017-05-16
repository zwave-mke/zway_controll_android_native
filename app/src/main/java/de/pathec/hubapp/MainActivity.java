package de.pathec.hubapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.iconics.view.IconicsTextView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceCommand;
import de.pathec.hubapp.events.ActiveHubEvent;
import de.pathec.hubapp.events.AddGeofenceEvent;
import de.pathec.hubapp.events.ConnectionEvent;
import de.pathec.hubapp.events.ConnectionStatus;
import de.pathec.hubapp.events.ConnectionType;
import de.pathec.hubapp.events.ModelDeleteEvent;
import de.pathec.hubapp.events.RemoveGeofenceEvent;
import de.pathec.hubapp.fragments.ElementsFragment;
import de.pathec.hubapp.fragments.NotificationsFragment;
import de.pathec.hubapp.fragments.LocationsFragment;
import de.pathec.hubapp.fragments.ProtocolFragment;
import de.pathec.hubapp.fragments.VideoFragment;
import de.pathec.hubapp.fragments.help.HelpDetailFragment;
import de.pathec.hubapp.fragments.help.HelpFragment;
import de.pathec.hubapp.fragments.settings.SettingsFragment;
import de.pathec.hubapp.fragments.settings.SettingsGeneralFragment;
import de.pathec.hubapp.fragments.settings.SettingsGeofencingFragment;
import de.pathec.hubapp.fragments.settings.SettingsHubsFragment;
import de.pathec.hubapp.fragments.settings.SettingsHubDetailFragment;
import de.pathec.hubapp.fragments.settings.SettingsLegalFragment;
import de.pathec.hubapp.geofencing.GeofenceTransitionService;
import de.pathec.hubapp.intro_v2.Intro;
import de.pathec.hubapp.model.all.AllLoader;
import de.pathec.hubapp.model.help.Help;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;

public class MainActivity extends AppCompatActivity
        implements ElementsFragment.OnElementsFragmentInteractionListener,
        LocationsFragment.OnLocationsFragmentInteractionListener,
        NotificationsFragment.OnNotificationsFragmentInteractionListener,
        SettingsFragment.OnSettingsFragmentInteractionListener,
        SettingsGeneralFragment.OnSettingsGeneralFragmentInteractionListener,
        SettingsHubsFragment.OnSettingsHubsFragmentInteractionListener,
        SettingsHubDetailFragment.OnSettingsHubDetailFragmentInteractionListener,
        SettingsGeofencingFragment.OnSettingsGeoFencingFragmentInteractionListener,
        ProtocolFragment.OnProtocolFragmentInteractionListener,
        HelpFragment.OnHelpFragmentInteractionListener,
        AllLoader.OnAllLoaderInteractionListener,
        IMainActivityCommunicator,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static String ELEMENTS_FRAGMENT = "elements_fragment";
    public static String LOCATIONS_FRAGMENT = "locations_fragment";
    public static String NOTIFICATIONS_FRAGMENT = "notifications_fragment";
    public static String SETTINGS_FRAGMENT = "settings_fragment";
    public static String SETTINGS_FRAGMENT_HUBS_DETAIL = "settings_fragment_hubs_detail";
    public static String PROTOCOL_FRAGMENT = "protocol_fragment";
    public static String MAP_FRAGMENT = "map_fragment";
    public static String LEGAL_FRAGMENT = "legal_fragment";
    public static String HELP_FRAGMENT = "help_fragment";
    public static String HELP_DETAIL_FRAGMENT = "help_detail_fragment";
    public static String VIDEO_FRAGMENT = "video_fragment";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; //

    private static final int GEOFENCE_REQ_CODE = 35;

    public static String INTENT_INTRO_ADDITIONAL_HUB = "intent_intro_additional_hub";

    private Context mContext;

    private Toolbar mToolbar;
    private Drawer mDrawer;

    private boolean mTwiceBackToExitPressedOnce = false;
    private Toast mTwiceBackToExitToast;

    private AllLoader mAllLoader;

    // Handler
    private Handler mToolbarProgressHandler;
    private ProgressBar mToolbarProgress;

    private Handler mToolbarSuccessHandler;
    private IconicsImageView mToolbarSuccess;

    private Handler mToolbarFailedHandler;
    private IconicsImageView mToolbarFailed;

    private HubList mHubList;
    private ArrayList<HubConnectionHolder> mHubConnectionHolder;

    private Integer mActiveHubId;

    private Fragment mCurrentFragment;

    private Vibrator mVibrator;

    private Handler mToastHandler;

    private Boolean mOnCreateRunning = false;

    private static final float GEOFENCE_RADIUS = 150.0f; // in meters

    private PendingIntent mGeofencePendingIntent;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(Params.LOGGING_TAG, "Create geofence pending intent");
        if ( mGeofencePendingIntent != null )
            return mGeofencePendingIntent;

        Intent intent = new Intent( this, GeofenceTransitionService.class);
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOnCreateRunning = true;

        Log.i(Params.LOGGING_TAG, "HubApp on create callback.");

        mContext = this;

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);

        // Check Hub configuration
        Boolean existHub = settings.getBoolean(Params.PREFS_HUB_CONFIGURED, false);
        if (!existHub) {
            Intent intent = new Intent(this, Intro.class);
            intent.putExtra(INTENT_INTRO_ADDITIONAL_HUB, false);
            startActivity(intent);
            finish();
            return;
        }

        mActiveHubId = settings.getInt(Params.PREFS_ACTIVE_HUB, -1);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mToastHandler = new Handler();

        Iconify
                .with(new MaterialCommunityModule());

        setTabStripVisibility(false);

        try { mToolbarProgressHandler = new Handler();
        } catch (Exception e) { mToolbarProgressHandler = null; }

        try { mToolbarSuccessHandler = new Handler();
        } catch (Exception e) { mToolbarSuccessHandler = null; }

        try { mToolbarFailedHandler = new Handler();
        } catch (Exception e) { mToolbarFailedHandler = null; }

        mToolbarProgress = (ProgressBar) findViewById(R.id.toolbar_progress_bar);
        mToolbarSuccess = (IconicsImageView) findViewById(R.id.toolbar_progress_success);
        mToolbarFailed = (IconicsImageView) findViewById(R.id.toolbar_progress_failed);

        mHubList = new HubList(this);

        initializeMaterialDrawer();

        initializeHubConnectionHolder();

        if (savedInstanceState != null) {
            // Restore the fragment's instance
            try {
                mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "mCurrentFragment");
            } catch (Exception e) {
                mCurrentFragment = null;
            }
        }

        createGoogleApi();

        String startView = settings.getString(Params.PREFS_START_VIEW, "");
        if (mCurrentFragment == null) {
            if (startView.isEmpty()) {
                // Default
                showFragment(ElementsFragment.newInstance(true, -1), ELEMENTS_FRAGMENT, true);
            } else if (startView.equals("Dashboard")) {
                showFragment(ElementsFragment.newInstance(true, -1), ELEMENTS_FRAGMENT, true);
            } else if (startView.equals("Elements")) {
                showFragment(ElementsFragment.newInstance(false, -1), ELEMENTS_FRAGMENT, true);
            } else if (startView.equals("Locations")) {
                showFragment(LocationsFragment.newInstance(), LOCATIONS_FRAGMENT, true);
            }
        }
    }

    private void createGoogleApi() {
        Log.d(Params.LOGGING_TAG, "Create GoogleApi()");
        if (mGoogleApiClient == null ) {
            mGoogleApiClient  = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the fragment's instance
        if (mCurrentFragment != null && mCurrentFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "mCurrentFragment", mCurrentFragment);
        }
    }

    private void initializeMaterialDrawer() {
        AccountHeader accountHeader = null;
        RelativeLayout normalHeader = (RelativeLayout) getLayoutInflater().inflate(R.layout.drawer_header, null, false);

        LinearLayout drawerFooter = (LinearLayout) getLayoutInflater().inflate(R.layout.drawer_footer, null, false);

        ArrayList<HubItem> hubList = mHubList.getHubList();
        // Prepare account drawer_header
        if (hubList.size() > 1) {
            AccountHeaderBuilder accountHeaderBuilder = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.header_background_accounts_common)
                    .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            changeActiveHub((int) profile.getIdentifier(), false);
                            return false;
                        }
                    });

            for (HubItem item : hubList) {
                accountHeaderBuilder.addProfiles(new ProfileDrawerItem()
                        .withName(item.getLocation())
                        .withEmail(item.getTitle())
                        .withIcon(item.getTileBitmap())
                        .withIdentifier(item.getId())
                );
            }

            accountHeader = accountHeaderBuilder.build();
            // Set active account id
            if (mActiveHubId != -1) {
                accountHeader.setActiveProfile(mActiveHubId);
            } else {
                mActiveHubId = hubList.get(0).getId();
                // Initial start
                accountHeader.setActiveProfile(mActiveHubId);
                // Update preferences
                SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
                settings.edit().putInt(Params.PREFS_ACTIVE_HUB, mActiveHubId).apply();
            }

        } else if (hubList.size() == 1) {
            final ImageView headerIcon = (ImageView) normalHeader.findViewById(R.id.header_icon);

            HubItem hubItem = hubList.get(0);
            if (hubItem != null) {
                Picasso.with(this).load(new File(hubItem.getTile())).resize(200, 200).into(headerIcon, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap imageBitmap = ((BitmapDrawable) headerIcon.getDrawable()).getBitmap();
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                        headerIcon.setImageDrawable(imageDrawable);
                    }

                    @Override
                    public void onError() {
                    }
                });

                // Set active Hub id
                mActiveHubId = hubItem.getId();
            } else {
                headerIcon.setImageDrawable(ContextCompat.getDrawable(this, (R.mipmap.ic_tile_placeholder)));
            }

            // Check active Hub (Initial start)
            if (mActiveHubId == -1) {
                mActiveHubId = hubList.get(0).getId();
                // Update preferences
                SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
                settings.edit().putInt(Params.PREFS_ACTIVE_HUB, mActiveHubId).apply();
            }
        }

        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withToolbar(mToolbar)
                .withActivity(this)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(false)
                .withCloseOnClick(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getString(R.string.fragment_dashboard_title)).withIcon(GoogleMaterial.Icon.gmd_dashboard).withIdentifier(1),
                        new PrimaryDrawerItem().withName(getString(R.string.fragment_elements_title)).withIcon(GoogleMaterial.Icon.gmd_list).withIdentifier(2),
                        new PrimaryDrawerItem().withName(getString(R.string.fragment_locations_title)).withIcon(GoogleMaterial.Icon.gmd_room).withIdentifier(3),
                        new PrimaryDrawerItem().withName(getString(R.string.fragment_notifications_title)).withIcon(GoogleMaterial.Icon.gmd_notifications).withIdentifier(4),
                        new PrimaryDrawerItem().withName(getString(R.string.fragment_settings_title)).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(5),
                        new PrimaryDrawerItem().withName(getString(R.string.fragment_help_title)).withIcon(GoogleMaterial.Icon.gmd_help).withIdentifier(6)


                        // Badge: .withBadge("42").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.accent))
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Fragment fragment = null;
                        String tag = "";
                        Class fragmentClass = null;

                        if (position == 1) {
                            fragment = ElementsFragment.newInstance(true, -1);
                            tag = ELEMENTS_FRAGMENT;
                        } else if (position == 2) {
                            fragmentClass = ElementsFragment.class;
                            tag = ELEMENTS_FRAGMENT;
                        } else if (position == 3) {
                            fragmentClass = LocationsFragment.class;
                            tag = LOCATIONS_FRAGMENT;
                        } else if (position == 4) {
                            fragmentClass = NotificationsFragment.class;
                            tag = NOTIFICATIONS_FRAGMENT;
                        } else if (position == 5) {
                            fragmentClass = SettingsFragment.class;
                            tag = SETTINGS_FRAGMENT;
                        } else if (position == 6) {
                            fragmentClass = HelpFragment.class;
                            tag = HELP_FRAGMENT;
                        }

                        try {
                            if (fragment == null) {
                                fragment = (Fragment) (fragmentClass != null ? fragmentClass.newInstance() : null);
                                showFragment(fragment, tag, true);
                            } else {
                                showFragment(fragment, tag, true);
                            }
                        } catch (Exception e) {
                            Log.e(Params.LOGGING_TAG, e.getMessage());
                        }

                        return false;
                    }
                })
                .withStickyFooter(drawerFooter)
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                });

        if (accountHeader != null) {
            drawerBuilder.withAccountHeader(accountHeader);
        } else {
            drawerBuilder.withHeader(normalHeader);
        }

        mDrawer = drawerBuilder.build();
    }

    private void initializeHubConnectionHolder() {
        mHubConnectionHolder = new ArrayList<>();

        // Active Hub first
        for (final HubItem item : mHubList.getHubList()) {
            if (item.getId().equals(mActiveHubId)) {
                mHubConnectionHolder.add(new HubConnectionHolder(mContext, item, true));
            }
        }

        // Not active Hubs
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (final HubItem item : mHubList.getHubList()) {
                    if (!item.getId().equals(mActiveHubId)) {
                        mHubConnectionHolder.add(new HubConnectionHolder(mContext, item, true));
                    }
                }
             }
        }, 1000);
    }

    @Override
    public void changeActiveHub(Integer newActiveHubId, Boolean refreshDrawer) {
        // Deactivate app in Z-Way module
        updateAppActiveState("0", true);

        // // Disconnect current Hub
        // HubConnectionHolder oldActiveHubConnectionHolder = getActiveHubConnectionHolder();
        // if (oldActiveHubConnectionHolder != null) {
        //     oldActiveHubConnectionHolder.disconnect();
        // }

        // Update preferences
        SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
        settings.edit().putInt(Params.PREFS_ACTIVE_HUB, newActiveHubId).apply();
        // Update active Hub
        mActiveHubId = newActiveHubId;

        if (refreshDrawer) {
            // Update drawer
            initializeMaterialDrawer();
        }

        // Connect new Hub
        HubConnectionHolder newActiveHubConnectionHolder = getActiveHubConnectionHolder();
        if (newActiveHubConnectionHolder != null) {
            newActiveHubConnectionHolder.connect();
        }

        BusProvider.postOnMain(new ActiveHubEvent(mActiveHubId));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel asynchronous Z-Way initializer
        if (mHubConnectionHolder != null) {
            for (HubConnectionHolder hubConnectionHolder : mHubConnectionHolder) {
                if (hubConnectionHolder != null) {
                    hubConnectionHolder.cancelConnect();
                }
            }
        }

        // DatabaseHandler.getInstance(getApplicationContext()).closeDB();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mOnCreateRunning) {
            mOnCreateRunning = false;
            Log.i(Params.LOGGING_TAG, "HubApp on resume callback: skip connect - already in on create.");
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    HubConnectionHolder activeHubConnectionHolder = getActiveHubConnectionHolder();
                    if (activeHubConnectionHolder != null) {
                        if (!activeHubConnectionHolder.isHubConnected()) { // if activity on create ...
                            Log.i(Params.LOGGING_TAG, "HubApp on resume callback: connect (activate app and synchronize) ...");

                            activeHubConnectionHolder.connect();
                            // after connecting: update app active state and synchronize all data ...
                        } else {
                            Log.i(Params.LOGGING_TAG, "HubApp on resume callback: activate app and synchronize ...");

                            // activate app in Z-Way module
                            updateAppActiveState("1", false);

                            synchronizeAllData();
                        }
                    } else {
                        Log.i(Params.LOGGING_TAG, "HubApp on resume callback: no Hub connection holder!");
                    }
                }
            }, 1000);
        }

        BusProvider.getInstance().register(this);
    }

    private void synchronizeAllData() {
        startLoading();

        HubConnectionHolder activeHubConnectionHolder = getActiveHubConnectionHolder();
        if (activeHubConnectionHolder == null) {
            stopLoading(-1);
            return;
        }

        IZWayApi zwayApi = activeHubConnectionHolder.getZWayApi();
        if (zwayApi == null) {
            stopLoading(-1);
            return;
        }

        if (mAllLoader != null) {
            mAllLoader.cancelSynchronization();
        }

        mAllLoader = new AllLoader(this, this, zwayApi, activeHubConnectionHolder.getHubItem().getId());
        // Only devices
        mAllLoader.loadAll(false);
    }

    @Override
    protected void onPause() {
        if (mTwiceBackToExitToast != null) {
            mTwiceBackToExitToast.cancel();
        }

        // activate app in Z-Way module
        updateAppActiveState("0", true);

        BusProvider.getInstance().unregister(this);

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Check if drawer is open
        if (mDrawer != null) {
            if (mDrawer.isDrawerOpen()) {
                mDrawer.closeDrawer();
            } else {
                // Prevent showing of blank activity!
                if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
                    if (mTwiceBackToExitPressedOnce) {
                        Log.i(Params.LOGGING_TAG, "(MainActivity) finish");
                        MainActivity.this.finish();
                        return;
                    }
                    mTwiceBackToExitPressedOnce = true;
                    mTwiceBackToExitToast = Toast.makeText(this, getString(R.string.back_button_twice), Toast.LENGTH_SHORT);
                    mTwiceBackToExitToast.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTwiceBackToExitPressedOnce = false;
                        }
                    }, 2000);
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

    @Override
    public void startLoading() {
        Log.i(Params.LOGGING_TAG, "Start loading");

        if (mToolbarProgressHandler != null) {
            mToolbarProgressHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mToolbarProgress != null) {
                        hideAllProgressViews();

                        mToolbarProgress.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void stopLoading(final Integer status) {
        Log.i(Params.LOGGING_TAG, "Stop loading");

        if (mToolbarProgressHandler != null) {
            mToolbarProgressHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mToolbarProgress != null && status == 1) {
                        hideAllProgressViews();

                        mToolbarSuccess.setVisibility(View.VISIBLE);

                        // Disable success view in 2000 milliseconds
                        if (mToolbarSuccessHandler != null) {
                            mToolbarSuccessHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mToolbarSuccess.setVisibility(View.GONE);
                                }
                            }, 2000);
                        }
                    } else if (status == -1) {
                        hideAllProgressViews();

                        mToolbarFailed.setVisibility(View.VISIBLE);

                        // Disable error view in 2000 milliseconds
                        if (mToolbarFailedHandler != null) {
                            mToolbarFailedHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mToolbarFailed.setVisibility(View.GONE);
                                }
                            }, 2000);
                        }
                    } else {
                        hideAllProgressViews();
                    }
                }
            });
        }
    }

    private synchronized void hideAllProgressViews() {
        if (mToolbarSuccessHandler != null) {
            mToolbarSuccessHandler.removeCallbacksAndMessages(null);
            mToolbarSuccess.setVisibility(View.GONE);
        }
        if (mToolbarFailedHandler != null) {
            mToolbarFailedHandler.removeCallbacksAndMessages(null);
            mToolbarFailed.setVisibility(View.GONE);
        }
        mToolbarProgress.setVisibility(View.GONE);
    }

    /**
     * Events
     */

    @Subscribe
    public void deleteEvent(ModelDeleteEvent deleteEvent) {
        if (deleteEvent.getModel().equals("Hub")) {
            Integer hubId = ((HubItem) deleteEvent.getItem()).getId();

            HubConnectionHolder hubConnectionHolderToDelete = null;
            for (HubConnectionHolder hubConnectionHolder : mHubConnectionHolder) {
                // Clear connect for deleted Hub.
                if (hubConnectionHolder.getHubItem().getId().equals(hubId)) {
                    hubConnectionHolder.cancelConnect();
                    hubConnectionHolder.disconnect();
                    hubConnectionHolderToDelete = hubConnectionHolder;
                }
            }

            if (hubConnectionHolderToDelete != null) {
                mHubConnectionHolder.remove(hubConnectionHolderToDelete);
            }

            if (hubId.equals(mActiveHubId)) {
                // Preference contains already a new valid Hub id (have a look at HubList).
                SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
                mActiveHubId = settings.getInt(Params.PREFS_ACTIVE_HUB, -1);
            }

            // Update drawer
            initializeMaterialDrawer();

            if (mActiveHubId != -1) {
                // Update new active Hub connection
                for (HubConnectionHolder hubConnectionHolder : mHubConnectionHolder) {
                    // Clear connect for deleted Hub.
                    if (hubConnectionHolder.getHubItem().getId().equals(mActiveHubId)) {
                        hubConnectionHolder.connect();
                    }
                }
            }
        }
    }

    @Subscribe
    public void connectionEvent(ConnectionEvent connectionEvent) {
        Log.i(Params.LOGGING_TAG, "(MainActivity) Connection event: " + connectionEvent.getHubItem().getId() + " (Active: " + mActiveHubId + ")");
        if (connectionEvent.getHubItem().getId().equals(mActiveHubId)) {
            if (connectionEvent.getType().equals(ConnectionType.ALL)) {
                LinearLayout drawerFooterConnecting = (LinearLayout) mDrawer.getStickyFooter().findViewById(R.id.drawer_footer_connecting);
                IconicsTextView drawerFooterConnectionState = (IconicsTextView) mDrawer.getStickyFooter().findViewById(R.id.drawer_footer_connection_state);
                if (drawerFooterConnectionState != null) {
                    drawerFooterConnectionState.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActiveHubConnectionHolder().connect();
                        }
                    });
                }

                // Set connection status in drawer footer
                if (connectionEvent.getStatus() < 0) {
                    Log.i(Params.LOGGING_TAG, "(MainActivity) Connection event - Z-Way not connected!");
                    if (drawerFooterConnectionState != null && drawerFooterConnecting != null) {
                        drawerFooterConnectionState.setText(String.format("{ion-alert} %s", connectionEvent.getMessage()));
                        drawerFooterConnectionState.setTextColor(ContextCompat.getColor(this, R.color.material_color_red));

                        drawerFooterConnecting.setVisibility(View.GONE);
                        drawerFooterConnectionState.setVisibility(View.VISIBLE);
                    }
                } else if (connectionEvent.getStatus().equals(ConnectionStatus.CONNECTED)) {
                    Log.i(Params.LOGGING_TAG, "(MainActivity) Connection event - Z-Way connected!");
                    if (drawerFooterConnectionState != null && drawerFooterConnecting != null) {
                        drawerFooterConnectionState.setText(String.format("{ion-checkmark} %s", connectionEvent.getMessage()));
                        drawerFooterConnectionState.setTextColor(ContextCompat.getColor(this, R.color.material_color_green));

                        drawerFooterConnecting.setVisibility(View.GONE);
                        drawerFooterConnectionState.setVisibility(View.VISIBLE);

                        // Synchronize all data
                        synchronizeAllData();

                        // Activate app in Z-Way module
                        updateAppActiveState("1", false);
                    }
                } else if (connectionEvent.getStatus().equals(ConnectionStatus.CONNECTING)) {
                    Log.i(Params.LOGGING_TAG, "(MainActivity) Connection event - Z-Way connecting!");
                    if (drawerFooterConnectionState != null && drawerFooterConnecting != null) {
                        drawerFooterConnectionState.setVisibility(View.GONE);
                        drawerFooterConnecting.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void updateAppActiveState(final String active, final Boolean disconnect) {
        final HubConnectionHolder hubConnectionHolder = getActiveHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                if (!checkPlayServices()) {
                    return null;
                }

                FirebaseInstanceId instanceID = FirebaseInstanceId.getInstance();
                String token = instanceID.getToken();

                Map<String, String> commandParams = new HashMap<>();
                commandParams.put("token", token);
                commandParams.put("active", active);
                DeviceCommand deviceCommand = new DeviceCommand("MobileAppSupport", "updateActiveState", commandParams);

                if (hubConnectionHolder.getZWayApi() != null) {
                    String message = hubConnectionHolder.getDeviceCommand(deviceCommand);
                    if (message != null) {
                        try {
                            if (message.equals("Not found")) {
                                Log.i(Params.LOGGING_TAG, "Z-Way app HubApp not found!");
                                return "-1";
                            } else if (new Gson().fromJson(message, JsonObject.class).get("code").getAsInt() == 3) {
                                Log.i(Params.LOGGING_TAG, "Android app not registered!");

                                Map<String, String> registerCommandParams = new HashMap<>();
                                registerCommandParams.put("token", token);
                                registerCommandParams.put("hubId", hubConnectionHolder.getHubItem().getId().toString());
                                registerCommandParams.put("title", Build.MODEL);
                                registerCommandParams.put("os", "android");

                                DeviceCommand registerDeviceCommand = new DeviceCommand("MobileAppSupport", "registerApp", registerCommandParams);
                                String registerMessage = hubConnectionHolder.getDeviceCommand(registerDeviceCommand);

                                if (registerMessage != null) {
                                    // Try activation again
                                    Map<String, String> activateAgainCommandParams = new HashMap<>();
                                    activateAgainCommandParams.put("token", token);
                                    activateAgainCommandParams.put("active", active);
                                    DeviceCommand activateAgainDeviceCommand = new DeviceCommand("HubApp", "updateActiveState", activateAgainCommandParams);

                                    // Check response, if error show a persist error message
                                    hubConnectionHolder.getDeviceCommand(activateAgainDeviceCommand);
                                }
                            }
                        } catch (JsonParseException jpe) {
                            Log.i(Params.LOGGING_TAG, "Json parse exception: " + jpe.getMessage());
                        }
                    } else {
                        Util.addProtocol(getApplicationContext(), new ProtocolItem(getApplicationContext(), ProtocolType.WARNING, "Update Z-Way App failed (Message is null)!", "ZWayApp"));
                        Log.i(Params.LOGGING_TAG, "Message is null");
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null && result.equals("-1") && active.equals("1")) {
                    Util.showMessage(getApplicationContext(), getString(R.string.z_way_module_required));
                }

                if (disconnect) {
                    hubConnectionHolder.disconnect();
                }
            }

        }.execute();
    }

    /**
     * Activity communicator callbacks
     */

    private synchronized void showFragment(Fragment fragment, String tag, Boolean addToBackStack) {
        if (fragment != null) {
            mCurrentFragment = fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_fade, R.anim.exit_fade, R.anim.enter_fade, R.anim.exit_fade)
                    .replace(R.id.fragment_container, fragment);

            if (addToBackStack) {
                fragmentTransaction.addToBackStack(tag);
            }

            fragmentTransaction.commit();
        }
    }

    @Override
    public void setTabStripVisibility(Boolean visible) {
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.main_tabs);

        if (tabs != null) {
            if (visible) {
                tabs.setVisibility(View.VISIBLE);
            } else {
                tabs.setVisibility(View.GONE);
            }
        } else {
            Log.w(Params.LOGGING_TAG, "Tab strip not found");
        }
    }

    @Override
    public FloatingActionButton setFabVisibility(Boolean visible) {
        FloatingActionButton fabBtn = (FloatingActionButton) findViewById(R.id.fab);

        if (fabBtn != null) {
            if (visible) {
                Log.i(Params.LOGGING_TAG, "Show floating action button");
                Util.showView(fabBtn, Params.FADE_ANIMATION_DURATION);
            } else {
                Log.i(Params.LOGGING_TAG, "Hide floating action button");
                Util.hideView(fabBtn, Params.FADE_ANIMATION_DURATION);
            }

            return fabBtn;
        } else {
            Log.w(Params.LOGGING_TAG, "Floating action button not found");
            return null;
        }
    }

    @Override
    public void setDrawerItemSelected(long identifier) {
        mDrawer.setSelection(identifier, false);
    }

    @Override
    public void showDefaultFragment(Boolean popBackStack) {
        if (popBackStack) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack();
        }

        showFragment(ElementsFragment.newInstance(true, -1), ELEMENTS_FRAGMENT, true);
    }

    @Override
    public void showBackArrow() {
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        mDrawer.getActionBarDrawerToggle().syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void showHamburgerIcon() {
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        mDrawer.getActionBarDrawerToggle().syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public HubConnectionHolder getActiveHubConnectionHolder() {
        for (HubConnectionHolder hubConnectionHolder : mHubConnectionHolder) {
            if (hubConnectionHolder.getHubItem().getId().equals(mActiveHubId)) {
                return hubConnectionHolder;
            }
        }

        // If no Hub connection found, check if active Hub id is wrong
        if (mHubList.getHubCount() > 0) {
            SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
            Integer hubId = mHubList.getHubList().get(0).getId();
            settings.edit().putInt(Params.PREFS_ACTIVE_HUB, hubId).apply();
            mActiveHubId = hubId;

            // Try again ...
            for (HubConnectionHolder hubConnectionHolder : mHubConnectionHolder) {
                if (hubConnectionHolder.getHubItem().getId().equals(mActiveHubId)) {
                    return hubConnectionHolder;
                }
            }
        }

        return null;
    }

    /**
     * Fragment callbacks
     */

    @Override
    public void onFragmentInteraction() {
        // Placeholder for fragments without callbacks
    }

    @Override
    public void onSettingsFragmentAddHub() {
        Intent intent = new Intent(this, Intro.class);
        intent.putExtra(INTENT_INTRO_ADDITIONAL_HUB, true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSettingsHubsFragmentDetail(HubItem item) {
        showFragment(SettingsHubDetailFragment.newInstance(item.getId()), SETTINGS_FRAGMENT_HUBS_DETAIL, true);
    }

    @Override
    public void onHelpFragmentDetail(Help help) {
        showFragment(HelpDetailFragment.newInstance(help.getTitle(), help.getSubtitle(), help.getText()), HELP_DETAIL_FRAGMENT, true);
    }

    @Override
    public void onSettingsHubDetailFragmentHubItemChanged(HubItem item) {
        // Update Hub item of connection holder to update connection settings
        HubConnectionHolder activeHubConnectionHolder = getActiveHubConnectionHolder();
        if (activeHubConnectionHolder != null) {
            activeHubConnectionHolder.setHubItem(item);
        }

        // Update drawer because Hub icon could be changed
        changeActiveHub(item.getId(), true);
    }

    @Override
    public void onSettingsHubDetailFragmentTriggerLastConnectionEvents(HubItem item) {
        for (HubConnectionHolder hubConnectionHolder : mHubConnectionHolder) {
            if (hubConnectionHolder.getHubItem().getId().equals(item.getId())) {
                hubConnectionHolder.triggerLastConnectionEvents();
            }
        }
    }

    @Override
    public void onSettingsHubDetailFragmentShowMap(final HubItem item) {
        new Handler().post(new Runnable() {
               public void run() {
                   SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                   mapFragment.getMapAsync(new OnMapReadyCallback() {
                       @Override
                       public void onMapReady(GoogleMap googleMap) {
                           Util.showMessage(getApplicationContext(), getString(R.string.fragment_settings_hub_detail_select_location));

                           setTitle(getString(R.string.fragment_map, item.getTitle()));

                           if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                               return;
                           }
                           googleMap.setMyLocationEnabled(true);

                           Location location = getLocation();
                           if (location != null) {
                               double latitude = location.getLatitude();
                               double longitude = location.getLongitude();
                               LatLng latLng = new LatLng(latitude, longitude);
                               googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                               googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                               if(item.getLongitude() != 0.0 && item.getLatitude() != 0.0) {
                                   googleMap.addMarker(new MarkerOptions().position(new LatLng(item.getLatitude(), item.getLongitude())).title(item.getTitle()));
                               }

                               googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                   @Override
                                   public void onMapLongClick(LatLng latLng) {
                                       if(mVibrator.hasVibrator()) {
                                           mVibrator.vibrate(50);
                                       }

                                       String city = "";
                                       double latitude = latLng.latitude;
                                       double longitude = latLng.longitude;

                                       Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                       try {
                                           List<Address> list = geocoder.getFromLocation(latitude, longitude, 1);
                                           if (list != null & list.size() > 0) {
                                               Address address = list.get(0);
                                               city = address.getLocality();
                                           }
                                       } catch (IOException e) {
                                           Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Can't determine location: " + e.getMessage(), "Location"));
                                       }

                                       item.setLocation(city);
                                       item.setLatitude(latitude);
                                       item.setLongitude(longitude);

                                       mHubList.updateHubItem(item);

                                       Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.INFO, "New location defined for " + item.getTitle() + ": " + city, "Location"));

                                       setUpGeofence();

                                       getSupportFragmentManager().popBackStack();
                                       showFragment(SettingsHubDetailFragment.newInstance(item.getId()), SETTINGS_FRAGMENT_HUBS_DETAIL, false);
                                   }
                               });
                           }
                       }
                   });
                   showFragment(mapFragment, MAP_FRAGMENT, true);
               }
           });
    }

    private Location getLocation() {
        // http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial/

        Location location = null;

        try {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            Boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                return null;
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) { }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) { }

                                @Override
                                public void onProviderEnabled(String provider) { }

                                @Override
                                public void onProviderDisabled(String provider) { }
                            });
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) { }

                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) { }

                                    @Override
                                    public void onProviderEnabled(String provider) { }

                                    @Override
                                    public void onProviderDisabled(String provider) { }
                                });
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onElementsFragmentShowVideoFragment(String deviceId) {
        showFragment(VideoFragment.newInstance(deviceId), VIDEO_FRAGMENT, true);
    }

    @Override
    public void onLocationsFragmentDetail(LocationItemApp locationItem) {
        showFragment(ElementsFragment.newInstance(false, locationItem.getId()), LOCATIONS_FRAGMENT, true);
    }

    @Override
    public void onSettingsGeneralProtocol(String status, String category) {
        showFragment(ProtocolFragment.newInstance(status, category), PROTOCOL_FRAGMENT, true);
    }

    @Override
    public void onSettingsGeneralOpenSourceSoftware() {
        showFragment(SettingsLegalFragment.newInstance(), LEGAL_FRAGMENT, true);
    }


    @Override
    public void onProtocolFragmentSelected(ProtocolItem protocolItem) {
        new AlertDialog.Builder(this).setTitle(getString(R.string.fragment_protocol_title) + " - " + Params.formatDateTimeGerman(protocolItem.getCreated()))
                .setMessage(protocolItem.getText())
                .setCancelable(true)
                .show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                mToastHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Google Play Services API unavailable!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void allLoaded(Integer status) {
        mAllLoader = null;
        stopLoading(status);
    }

    @Override
    public Boolean isAllLoaderActive() {
        return mAllLoader != null;
    }

    @Override
    public void setUpGeofence() {
        SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);
        Boolean isGeofencingEnabled = settings.getBoolean(Params.PREFS_HUB_GEOFENCING_ENABLED, false);
        if (isGeofencingEnabled) {
            ArrayList<HubItem> hubList = mHubList.getHubList();

            // 1. Remove all geofence
            removeGeofence();

            // 2. Add all geofence
            ArrayList<Geofence> geofenceList = new ArrayList<>();
            for (HubItem hubItem : hubList) {
                // Check location information
                if (hubItem.getLongitude() != 0.0 && hubItem.getLatitude() != 0.0) {
                    Geofence geofence = createGeofence(new LatLng(hubItem.getLatitude(), hubItem.getLongitude()), GEOFENCE_RADIUS, hubItem.getTitle());
                    geofenceList.add(geofence);
                }
            }

            if (geofenceList.size() > 0) {
                GeofencingRequest geofencingRequest = createGeofenceRequest(geofenceList);
                addGeofenceList(geofencingRequest);
            }
        }
    }

    @Override
    public void removeGeofence() {
        ArrayList<HubItem> hubList = mHubList.getHubList();
        ArrayList<String> hubTitleList = new ArrayList<>();
        for (HubItem hubItem : hubList) {
            hubTitleList.add(hubItem.getTitle());
        }

        removeGeofenceList(hubTitleList);
    }

    private Geofence createGeofence(LatLng latLng, float radius, String hubTitle) {
        Log.d(Params.LOGGING_TAG, "Create geofence");
        return new Geofence.Builder()
                .setRequestId(hubTitle)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( Geofence.NEVER_EXPIRE )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    private GeofencingRequest createGeofenceRequest(List<Geofence> geofenceList ) {
        Log.d(Params.LOGGING_TAG, "Create geofence request");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofences(geofenceList)
                .build();
    }

    private void addGeofenceList(GeofencingRequest request) {
        Log.d(Params.LOGGING_TAG, "Add geofence list");
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mGeofencePendingIntent = createGeofencePendingIntent();

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                request,
                mGeofencePendingIntent
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.i(Params.LOGGING_TAG, "Add geofence list: " + status);
                if ( status.isSuccess() ) {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.INFO, "Update geofence list successfully.", "GeoFencing"));

                    BusProvider.postOnMain(new AddGeofenceEvent(true));
                } else {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Update geofence list failed.", "GeoFencing"));

                    BusProvider.postOnMain(new AddGeofenceEvent(false));
                }
            }
        });
    }

    private void removeGeofenceList(List<String> geofenceRequestIds) {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                geofenceRequestIds
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.i(Params.LOGGING_TAG, "Remove geofence list: " + status);
                if ( status.isSuccess() ) {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.INFO, "Remove geofence list successfully.", "GeoFencing"));

                    BusProvider.postOnMain(new RemoveGeofenceEvent(true));
                } else {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Remove geofence list failed.", "GeoFencing"));

                    BusProvider.postOnMain(new RemoveGeofenceEvent(false));
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Params.LOGGING_TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(Params.LOGGING_TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(Params.LOGGING_TAG, "onConnectionFailed()");
    }
}
