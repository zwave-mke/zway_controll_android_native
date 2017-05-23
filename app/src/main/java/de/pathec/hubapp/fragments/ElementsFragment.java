package de.pathec.hubapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.mikepenz.iconics.view.IconicsImageView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryData;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.adapter.DevicesItemView;
import de.pathec.hubapp.adapter.DevicesRecyclerViewAdapter;
import de.pathec.hubapp.events.ActiveHubEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.handler.device.DeviceHandler;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.model.device.DeviceListApp;
import de.pathec.hubapp.model.devicehistory.DeviceHistoryItemApp;
import de.pathec.hubapp.model.devicehistory.DeviceHistoryListApp;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.location.LocationListApp;
import de.pathec.hubapp.util.HistoryMarkerView;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.ZWayUtil;
import de.pathec.hubapp.util.recycler_view_helper.OnStartDragListener;
import de.pathec.hubapp.util.recycler_view_helper.SimpleItemTouchHelperCallback;

import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_BATTERY;

public class ElementsFragment extends Fragment implements
        DeviceListApp.OnDeviceListInteractionListener,
        DeviceHistoryListApp.OnDeviceHistoryListInteractionListener,
        DevicesRecyclerViewAdapter.OnDevicesAdapterInteractionListener,
        DeviceHandler.DeviceHandlerInteractionListener,
        OnStartDragListener {

    private IMainActivityCommunicator mActivityCommunicator;

    private static final String ONLY_DASHBOARD = "onlyDashboard";
    private static final String LOCATION_ID = "locationId";

    private OnElementsFragmentInteractionListener mListener;

    private Boolean mOnlyDashboard = false;
    private Integer mLocationId = -1;

    private Fragment mThis;

    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRecyclerView;
    private DevicesRecyclerViewAdapter mAdapter;

    private LinearLayout mNoData;

    private LinearLayout mProgressBar;

    private ItemTouchHelper mItemTouchHelper;

    private DeviceHandler mDeviceHandler;

    private MenuItem mDeviceTypeMenuItem;
    private MenuItem mTagsMenuItem;
    private MenuItem mSortingMenuItem;

    private DeviceListApp mDeviceList, mDeviceListSynchronization;

    private String mDeviceFilterDeviceType;
    private String mDeviceFilterTag;
    private Integer mDeviceSorting;

    public ElementsFragment() {
        // Required empty public constructor
    }

    public static ElementsFragment newInstance(Boolean onlyDashboard, Integer locationId) {
        ElementsFragment fragment = new ElementsFragment();

        Bundle args = new Bundle();
        args.putBoolean(ONLY_DASHBOARD, onlyDashboard);
        args.putInt(LOCATION_ID, locationId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeviceList = new DeviceListApp(getContext(), this, null);
        mDeviceHandler = new DeviceHandler(getActivity(), mActivityCommunicator, this);

        mThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_elements, container, false);

        if(getArguments() != null) {
            mOnlyDashboard = getArguments().getBoolean(ONLY_DASHBOARD, false);
            mLocationId = getArguments().getInt(LOCATION_ID, -1);
        }

        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.fragment_elements_swipe_fresh_layout);

        if (!mLocationId.equals(-1)) {
            // Change margin top of recycler view
            ViewGroup.MarginLayoutParams marginLayoutParams =
                    (ViewGroup.MarginLayoutParams) mSwipeContainer.getLayoutParams();

            DisplayMetrics metrics;
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int height = Util.getDPI(54, metrics);

            marginLayoutParams.setMargins(0, height, 0, 0);
            mSwipeContainer.setLayoutParams(marginLayoutParams);

            // Show location title
            TextView locationTxt = (TextView) view.findViewById(R.id.fragment_elements_location);
            locationTxt.setVisibility(View.VISIBLE);

            LocationListApp locationList = new LocationListApp(getContext(), null, null);
            LocationItemApp location = locationList.getLocationItem(mLocationId, getHubConnectionHolder().getHubItem().getId());
            if (!location.getId().equals(-1)) {
                if (location.getId().equals(0)) {
                    locationTxt.setText(getContext().getString(R.string.fragment_locations_global_room));
                } else {
                    locationTxt.setText(location.getTitle());
                }
            } else {
                Util.showMessage(getActivity(), getString(R.string.unexpected_error));
                mActivityCommunicator.showDefaultFragment(true);
            }
        }

        mNoData = (LinearLayout) view.findViewById(R.id.fragment_elements_no_data);
        Button mNoDataRefreshBtn = (Button) view.findViewById(R.id.fragment_elements_no_data_refresh_btn);
        mNoDataRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivityCommunicator.isAllLoaderActive()) {
                    Util.showMessage(getActivity(), getString(R.string.all_loader_active));
                    mSwipeContainer.setRefreshing(false);
                } else {
                    // Z-Way
                    loadData(true);
                }
            }
        });
        mProgressBar = (LinearLayout) view.findViewById(R.id.fragment_elements_progress_bar);

        mSwipeContainer.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.accent));
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mActivityCommunicator.isAllLoaderActive()) {
                    Util.showMessage(getActivity(), getString(R.string.all_loader_active));
                    mSwipeContainer.setRefreshing(false);
                } else {
                    // Z-Way
                    loadData(true);
                }
            }
        });

        mAdapter = new DevicesRecyclerViewAdapter(getActivity(), new ArrayList<DevicesItemView>(), this, this, mLocationId.equals(-1));

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_elements_recycler_view);
        mRecyclerView.setAdapter(mAdapter);

        if (getResources().getConfiguration().orientation == 2) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        } else {
            // Default two columns
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }

        mRecyclerView.setItemAnimator(null);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter, false, false);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        // Load preferences
        SharedPreferences settings = getContext().getSharedPreferences(Params.PREFS_NAME, 0);
        mDeviceFilterDeviceType = settings.getString(Params.PREFS_DEVICE_FILTER_DEVICE_TYPE, "");
        mDeviceFilterTag = settings.getString(Params.PREFS_DEVICE_FILTER_TAG, "");
        mDeviceSorting = settings.getInt(Params.PREFS_DEVICE_SORTING, ZWayUtil.SORTING_CUSTOM);

        // SQLite
        loadData(false);

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }
    }

    private void loadData(Boolean loadFromHub) {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        if (loadFromHub) {
            Log.i(Params.LOGGING_TAG, "(ElementsFragment) Loading data from Hub.");

            Util.hideView(mSwipeContainer, Params.FADE_ANIMATION_DURATION);
            Util.hideView(mNoData, Params.FADE_ANIMATION_DURATION);
            Util.showView(mProgressBar, Params.FADE_ANIMATION_DURATION);

            // Delayed execution, because complete animation first!
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.clear(); // Model update events!

                    // Network operation!
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            mDeviceListSynchronization = new DeviceListApp(getContext(),
                                    (DeviceListApp.OnDeviceListInteractionListener) mThis, hubConnectionHolder);
                            mDeviceListSynchronization.getDeviceList(hubConnectionHolder.getHubItem().getId(), true, mOnlyDashboard, mLocationId);
                            return null;
                        }
                    }.execute();
                }
            }, Params.FADE_ANIMATION_DURATION + 100);
        } else {
            // Loading from SQLLite database without any animation
            Log.i(Params.LOGGING_TAG, "(ElementsFragment) Loading data from SQLite database.");
            DeviceListApp deviceList = new DeviceListApp(getContext(),
                    (DeviceListApp.OnDeviceListInteractionListener) mThis, hubConnectionHolder);
            deviceList.getDeviceList(hubConnectionHolder.getHubItem().getId(), false, mOnlyDashboard, mLocationId);
        }
    }

    private HubConnectionHolder getHubConnectionHolder() {
        HubConnectionHolder hubConnectionHolder = mActivityCommunicator.getActiveHubConnectionHolder();
        if (hubConnectionHolder == null) {
            // Only show no data with message that no hub is configured!
            Log.w(Params.LOGGING_TAG, "(ElementsFragment) No Hub configured");
            Util.showMessage(getActivity(), getString(R.string.warning_no_hub));

            Util.hideView(mProgressBar, Params.FADE_ANIMATION_DURATION);
            Util.hideView(mSwipeContainer, Params.FADE_ANIMATION_DURATION);
            Util.showView(mNoData, Params.FADE_ANIMATION_DURATION);

            mSwipeContainer.setRefreshing(false);
            return null;
        } else {
            return hubConnectionHolder;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnElementsFragmentInteractionListener) {
            mListener = (OnElementsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnElementsFragmentInteractionListener");
        }

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

        inflater.inflate(R.menu.elements, menu);

        mDeviceTypeMenuItem = menu.findItem(R.id.filter_device_type).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_filter)
                        .colorRes(R.color.icons)
                        .actionBarSize());

        mTagsMenuItem = menu.findItem(R.id.filter_tags).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_tag)
                        .colorRes(R.color.icons)
                        .actionBarSize());

        mSortingMenuItem = menu.findItem(R.id.filter_sorting).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_sort)
                        .colorRes(R.color.icons)
                        .actionBarSize());

        refreshMenuItemHighlighting();

        if (mOnlyDashboard) {
            mDeviceTypeMenuItem.setVisible(false);
            mTagsMenuItem.setVisible(false);
            mSortingMenuItem.setVisible(true);
        } else {
            mDeviceTypeMenuItem.setVisible(true);
            mTagsMenuItem.setVisible(true);
            mSortingMenuItem.setVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void refreshMenuItemHighlighting() {
        if (!mDeviceFilterDeviceType.isEmpty()) {
            mDeviceTypeMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_filter)
                .colorRes(R.color.accent)
                .actionBarSize());
        } else {
            mDeviceTypeMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_filter)
                .colorRes(R.color.icons)
                .actionBarSize());
        }
        if (!mDeviceFilterTag.isEmpty()) {
            mTagsMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_tag)
                .colorRes(R.color.accent)
                .actionBarSize());
        } else {
            mTagsMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_tag)
                .colorRes(R.color.icons)
                .actionBarSize());
        }
        if (mDeviceSorting != 0) {
            mSortingMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_sort)
                .colorRes(R.color.accent)
                .actionBarSize());
        } else {
            mSortingMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_sort)
                .colorRes(R.color.icons)
                .actionBarSize());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_device_type:
                showDeviceTypeDialog();
                return true;
            case R.id.filter_tags:
                showTagsDialog();
                return true;
            case R.id.filter_sorting:
                showSortingDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeviceTypeDialog() {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        DeviceListApp deviceList = new DeviceListApp(getContext(), null, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        // Load device types
        Set<String> deviceTypes = deviceList.getAllDeviceTypes(hubConnectionHolder.getHubItem().getId());
        // Convert set to array
        ArrayList<String> deviceTypesAsArray = new ArrayList<>(deviceTypes);
        Collections.sort(deviceTypesAsArray);
        // Prepare array with all elements item
        ArrayList<String> deviceTypeLabelsAsArray = new ArrayList<>();
        deviceTypeLabelsAsArray.add(getString(R.string.devices_filter_device_type_reset));
        // Change device type identifier with labels
        for (String deviceType : deviceTypesAsArray) {
            deviceTypeLabelsAsArray.add(ZWayUtil.getDeviceTypeLabelByIdentifier(getContext(), deviceType));
        }

        if (deviceTypes.isEmpty()) {
            Util.showMessage(getContext(), getString(R.string.devices_filter_no_device_types));
        } else {
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.filter_select, deviceTypeLabelsAsArray);
            alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    if (position == 0) {
                        mDeviceFilterDeviceType = "";
                    } else {
                        mDeviceFilterDeviceType = ZWayUtil.getDeviceTypeIdentifierByLabel(getContext(), adapter.getItem(position));
                    }

                    SharedPreferences settings = getContext().getSharedPreferences(Params.PREFS_NAME, 0);
                    settings.edit().putString(Params.PREFS_DEVICE_FILTER_DEVICE_TYPE, mDeviceFilterDeviceType).apply();

                    loadData(false);
                    refreshMenuItemHighlighting();
                }
            }).setTitle(getString(R.string.devices_filter_device_type));
            alert.setPositiveButton(getActivity().getString(R.string.close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    private void showTagsDialog() {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        DeviceListApp deviceList = new DeviceListApp(getContext(), null, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        // Load tags
        Set<String> tags = deviceList.getAllDeviceTags(hubConnectionHolder.getHubItem().getId());
        // Prepare array with all elements item
        ArrayList<String> allElements = new ArrayList<>();
        allElements.add(getString(R.string.devices_filter_tags_reset));
        // Convert set to array
        ArrayList<String> tagsAsArray = new ArrayList<>(tags);
        // Combine arrays
        allElements.addAll(tagsAsArray);

        if (tags.isEmpty()) {
            Util.showMessage(getContext(), getString(R.string.devices_filter_no_tags));
        } else {
            tags.add(getString(R.string.devices_filter_tags_reset));
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.filter_select, allElements.toArray(new String[allElements.size()]));
            alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    if (position == 0) {
                        mDeviceFilterTag = "";
                    } else {
                        mDeviceFilterTag = adapter.getItem(position);
                    }

                    SharedPreferences settings = getContext().getSharedPreferences(Params.PREFS_NAME, 0);
                    settings.edit().putString(Params.PREFS_DEVICE_FILTER_TAG, mDeviceFilterTag).apply();

                    loadData(false);
                    refreshMenuItemHighlighting();
                }
            }).setTitle(getString(R.string.devices_filter_tags));
            alert.setPositiveButton(getActivity().getString(R.string.close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    private void showSortingDialog() {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.filter_sorting, R.layout.filter_select);
        alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                mDeviceSorting = position;

                SharedPreferences settings = getContext().getSharedPreferences(Params.PREFS_NAME, 0);
                settings.edit().putInt(Params.PREFS_DEVICE_SORTING, mDeviceSorting).apply();

                loadData(false);
                refreshMenuItemHighlighting();
            }
        }).setTitle(getString(R.string.devices_sorting));
        alert.setPositiveButton(getActivity().getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mOnlyDashboard) {
            getActivity().setTitle(R.string.fragment_dashboard_title);
        } else {
            getActivity().setTitle(R.string.fragment_elements_title);
        }

        if(mActivityCommunicator != null) {
            if (mLocationId.equals(-1)) {
                if (mOnlyDashboard) {
                    mActivityCommunicator.setDrawerItemSelected(1);
                } else {
                    mActivityCommunicator.setDrawerItemSelected(2);
                }
            } else {
                mActivityCommunicator.setDrawerItemSelected(3);
            }
            mActivityCommunicator.setTabStripVisibility(false);
            mActivityCommunicator.setFabVisibility(false);
            // mActivityCommunicator.showBackArrow();
            mActivityCommunicator.showHamburgerIcon();
        }

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mDeviceListSynchronization != null) {
            mDeviceListSynchronization.cancelSynchronization();
        }

        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void activeHub(ActiveHubEvent activeHubEvent) {
        loadData(false);
    }

    @Subscribe
    public void deviceUpdate(ModelUpdateEvent<DeviceItemApp> modelUpdateEvent) {
        if (modelUpdateEvent.getModel().equals("Device")
                && modelUpdateEvent.getItem().getHubId().equals(mActivityCommunicator.getActiveHubConnectionHolder().getHubItem().getId())) {
            // Only if front of device card is shown!
            View view = mRecyclerView.findViewWithTag(modelUpdateEvent.getItem().getDeviceId() + "-" + modelUpdateEvent.getItem().getHubId());
            if (view != null) {
                Boolean frontView = false;
                RelativeLayout front = (RelativeLayout) view.findViewById(R.id.device_front);
                if (front != null && front.getVisibility() == View.VISIBLE) {
                    frontView = true;
                }

                DevicesItemView deviceItemView = new DevicesItemView(modelUpdateEvent.getItem(), 0, frontView);
                mAdapter.updateItem(deviceItemView);
            }
        }
    }

    @Override
    public void onDeviceListLoaded(ArrayList<DeviceItemApp> deviceList, Boolean loadFromHub) {
        if (loadFromHub) {
            mActivityCommunicator.stopLoading(1);
        }

        if (deviceList != null && deviceList.size() > 0) {
            if (mRecyclerView != null) {
                Log.i(Params.LOGGING_TAG, "(ElementsFragment) Loading data complete.");

                ArrayList<DevicesItemView> devicesItemViews = new ArrayList<>();
                for (DeviceItemApp deviceItem : deviceList) {
                    SharedPreferences settings = getActivity().getSharedPreferences(Params.PREFS_NAME, 0);
                    if ((!settings.getBoolean(Params.PREFS_VIEW_BATTERY_INFO, false))
                            && deviceItem.getDeviceType().equals(DEVICE_TYPE_BATTERY)) {
                        // If device type selection to battery ... show battery in every case
                        if (!mDeviceFilterDeviceType.equals(DEVICE_TYPE_BATTERY)) {
                            continue;
                        }
                    }
                    devicesItemViews.add(new DevicesItemView(deviceItem, 0, true));
                }
                mAdapter.clear();
                mAdapter.addAll(devicesItemViews);
                mSwipeContainer.setRefreshing(false);
                Util.hideView(mProgressBar, Params.FADE_ANIMATION_DURATION);
                Util.hideView(mNoData, Params.FADE_ANIMATION_DURATION);
                Util.showView(mSwipeContainer, Params.FADE_ANIMATION_DURATION);
            }
        } else {
            Util.hideView(mProgressBar, Params.FADE_ANIMATION_DURATION);
            Util.hideView(mSwipeContainer, Params.FADE_ANIMATION_DURATION);
            Util.showView(mNoData, Params.FADE_ANIMATION_DURATION);
        }
    }

    @Override
    public void onDeviceListError(String message) {
        Util.showMessage(getActivity(), message);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    /**
     * @param deviceItem affected device (copy of list item!)
     */
    @Override
    public void onDevicesAdapterAction(DeviceItemApp deviceItem) {
        // Forward to device handler
        mDeviceHandler.onDeviceAction(deviceItem);
    }

    /**
     * @param deviceItem affected device (copy of list item!)
     * @param action on/off
     */
    @Override
    public void onDevicesAdapterAction(DeviceItemApp deviceItem, String action) {
        // Forward to device handler
        mDeviceHandler.onDeviceAction(deviceItem, action);
    }

    @Override
    public synchronized void onDevicesAdapterSwap(final List<DeviceItemApp> list) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DeviceListApp deviceList = new DeviceListApp(getContext(), null, null);

                int order = 1;

                for (DeviceItemApp item : list) {
                    item.setOrder(order);
                    deviceList.updateDeviceItem(item, false);
                    order++;
                }

                return null;
            }
        }.execute();
    }

    @Override
    public void onDeviceHistory(DeviceItemApp deviceItem) {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            return;
        }

        final String deviceId = deviceItem.getDeviceId();

        new AsyncTask<Void, Void, ArrayList<DeviceHistoryData>>() {

            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getString(R.string.please_wait));
                dialog.setIndeterminate(true);
                dialog.show();
            }

            @Override
            protected ArrayList<DeviceHistoryData> doInBackground(Void... params) {
                Long since = (new Date().getTime() - (3600 * 24 * 1000)) / 1000; // 24 hours
                Log.i(Params.LOGGING_TAG, "Loading single device history since: " + since);

                return hubConnectionHolder.getDeviceHistory(deviceId, since);
            }

            @Override
            protected void onPostExecute(ArrayList<DeviceHistoryData> deviceHistory) {
                dialog.dismiss();
                dialog = null;

                if (deviceHistory != null) {
                    // Group history data to hours
                    Map<Integer, List<Float>> deviceHistoryGroups = new HashMap<>();
                    for (DeviceHistoryData deviceHistoryData : deviceHistory) {
                        Date timeAsDate = new Date(deviceHistoryData.getId() * 1000); // Seconds to milliseconds

                        Integer key = Integer.parseInt(Params.formatTimeHours(timeAsDate, false)); // Hour without timezone information

                        Float value = Float.parseFloat(deviceHistoryData.getLevel());
                        if (!deviceHistoryGroups.containsKey(key)) {
                            List<Float> values = new ArrayList<>();
                            values.add(value);

                            deviceHistoryGroups.put(key, values);
                        } else {
                            deviceHistoryGroups.get(key).add(value);
                        }
                    }

                    // Build new array with average per hour
                    ArrayList<DeviceHistoryData> deviceHistoryAverageGroups = new ArrayList<>();
                    for (Map.Entry<Integer, List<Float>> entry : deviceHistoryGroups.entrySet()) {
                        Float sumValue = 0.0f;
                        Integer countValue = 0;
                        for (Float value : entry.getValue()) {
                            sumValue += value;
                            countValue++;
                        }

                        Float averageValue = sumValue / countValue;

                        DeviceHistoryData deviceHistoryData = new DeviceHistoryData();
                        deviceHistoryData.setId(Params.buildDate(entry.getKey()).getTime());
                        deviceHistoryData.setLevel(String.valueOf(averageValue));

                        deviceHistoryAverageGroups.add(deviceHistoryData);
                    }

                    if (deviceHistoryAverageGroups.size() < 2) {
                        Util.showMessage(getActivity(), getString(R.string.device_history_not_enough_data));

                        mListener.onElementsFragmentShowNotifications(deviceId);
                    } else {
                        HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
                        if (hubConnectionHolder != null) {
                            DeviceItemApp deviceItem = mDeviceList.getDeviceItem(deviceId, hubConnectionHolder.getHubItem().getId());
                            showHistoryDialog(deviceHistoryAverageGroups, deviceItem.getMetrics().getTitle());
                        } else {
                            showHistoryDialog(deviceHistoryAverageGroups, "");
                        }
                    }
                } else {
                    Util.showMessage(getActivity(), getString(R.string.device_history_not_enough_data));

                    mListener.onElementsFragmentShowNotifications(deviceId);
                }
            }
        }.execute();
    }

    private void showHistoryDialog(ArrayList<DeviceHistoryData> deviceHistoryDataList, String deviceTitle) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(
                R.layout.dialog_history, null);

        LineChart chart = (LineChart) layout.findViewById(R.id.chart);

        Collections.sort(deviceHistoryDataList, new Comparator<DeviceHistoryData>() {
            @Override
            public int compare(DeviceHistoryData o1, DeviceHistoryData o2) {
               return  o1.getId().compareTo(o2.getId());
            }
        });

        Float minimumValue = 0.0f;
        Float maximumValue = 0.0f;
        List<Entry> entries = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();
        Integer index = 0;
        for (DeviceHistoryData deviceHistoryData : deviceHistoryDataList) {
            Float value = Float.parseFloat(deviceHistoryData.getLevel());
            entries.add(new Entry(index, value));

            if (index == 0) {
                minimumValue = value;
                maximumValue = value;
            } else {
                if (value < minimumValue) {
                    minimumValue = value;
                }
                if (value > maximumValue) {
                    maximumValue = value;
                }
            }

            Date timeAsDate = new Date(deviceHistoryData.getId());
            labels.add(Params.formatTimeShort(Params.clearTime(timeAsDate, false, true, true, true), true)); // Correct timezone for label
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.primary));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(getActivity(), R.color.accent));
        dataSet.setCircleRadius(4);
        dataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.primary));
        dataSet.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text));
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return labels.get((int) value);
            }
        };

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(formatter);
        xAxis.setLabelRotationAngle(-45);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        chart.setTouchEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        YAxis yAxis = chart.getAxisLeft();
        if (minimumValue < 0.0001) {
            yAxis.setAxisMinimum(0f); // start at zero
        } else {
            yAxis.setAxisMinimum(minimumValue - 2);
            yAxis.setAxisMaximum(maximumValue + 2);
        }

        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        chart.setMarker(new HistoryMarkerView(getActivity(), R.layout.history_marker));

        yAxis.setGranularity(1.0f);

        Legend legendAll = chart.getLegend();
        legendAll.setEnabled(false);

        alert.setTitle(deviceTitle);
        alert.setPositiveButton(getString(R.string.close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

        alert.setView(layout);
        alert.show();

        chart.invalidate(); // refresh
    }

    @Override
    public void onDeviceHandlerStartAction(DeviceItemApp deviceItem) {
        View view = mRecyclerView.findViewWithTag(deviceItem.getDeviceId() + "-" + deviceItem.getHubId());
        // Show progress bar
        if (view != null) {
            IconicsImageView progressSuccess = (IconicsImageView) view.findViewById(R.id.device_item_progress_success);
            if (progressSuccess != null) {
                progressSuccess.setVisibility(View.GONE);
            }
            IconicsImageView progressError = (IconicsImageView) view.findViewById(R.id.device_item_progress_error);
            if (progressError != null) {
                progressError.setVisibility(View.GONE);
            }
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.device_item_progress);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDeviceHandlerFinishAction(DeviceItemApp deviceItem, String result) {
        View view = mRecyclerView.findViewWithTag(deviceItem.getDeviceId() + "-" + deviceItem.getHubId());
        if (view != null) {
            // Hide progress bar
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.device_item_progress);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
        if (result != null) {
            // Update device in SQLite if command successfully performed
            DeviceListApp deviceList = new DeviceListApp(getContext(), null, null);
            deviceList.updateDeviceItem(new DeviceItemApp(deviceItem), false);

            DevicesItemView devicesItemView = new DevicesItemView(deviceItem, 1, true);
            mAdapter.updateItem(devicesItemView);
        } else {
            Log.i(Params.LOGGING_TAG, "Handle device action finished: Not found!");

            DevicesItemView devicesItemView = new DevicesItemView(new DeviceItemApp(deviceItem), -1, true);
            mAdapter.updateItem(devicesItemView);
        }
    }

    @Override
    public void onDeviceHandlerShowVideoFragment(String deviceId) {
        mListener.onElementsFragmentShowVideoFragment(deviceId);
    }

    @Override
    public void onDeviceHandlerShowNotificationFragment(String deviceId) {
        mListener.onElementsFragmentShowNotifications(deviceId);
    }

    @Override
    public void onDeviceHandlerHistory(DeviceItemApp deviceItem) {
        onDeviceHistory(deviceItem);
    }

    @Override
    public void onDeviceHistoryListLoaded(ArrayList<DeviceHistoryItemApp> deviceHistoryList, Boolean loadFromHub) {
        // Do nothing
    }

    @Override
    public void onDeviceHistoryListError(String message) {
        // Do nothing
    }

    @Override
    public void onDeviceHistory(ArrayList<DeviceHistoryData> deviceHistory, String deviceId) {
        // Do nothing
    }

    public interface OnElementsFragmentInteractionListener {
        void onElementsFragmentShowVideoFragment(String deviceId);
        void onElementsFragmentShowNotifications(String deviceId);
    }
}
