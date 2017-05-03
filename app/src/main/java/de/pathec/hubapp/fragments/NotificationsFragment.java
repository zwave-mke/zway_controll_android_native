package de.pathec.hubapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.adapter.NotificationsRecyclerViewAdapter;
import de.pathec.hubapp.events.ActiveHubEvent;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.model.device.DeviceListApp;
import de.pathec.hubapp.model.notification.NotificationItemApp;
import de.pathec.hubapp.model.notification.NotificationListApp;
import de.pathec.hubapp.ui.DividerItemDecoration;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.recycler_view_helper.OnStartDragListener;
import de.pathec.hubapp.util.recycler_view_helper.SimpleItemTouchHelperCallback;

public class NotificationsFragment extends Fragment implements
        NotificationListApp.OnNotificationListInteractionListener,
        NotificationsRecyclerViewAdapter.OnNotificationsAdapterInteractionListener,
        OnStartDragListener {

    private IMainActivityCommunicator mActivityCommunicator;

    private OnNotificationsFragmentInteractionListener mListener;

    private Fragment mThis;

    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRecyclerView;
    private NotificationsRecyclerViewAdapter mAdapter;

    private LinearLayout mNoData;

    private LinearLayout mProgressBar;

    private ItemTouchHelper mItemTouchHelper;

    private NotificationListApp mNotificationListSynchronization;

    private MenuItem mNotificationTypeMenuItem;
    private MenuItem mNotificationSourceMenuItem;

    private String mNotificationFilterType;
    private String mNotificationFilterSource;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.fragment_notifications_swipe_fresh_layout);
        mNoData = (LinearLayout) view.findViewById(R.id.fragment_notifications_no_data);
        Button mNoDataRefreshBtn = (Button) view.findViewById(R.id.fragment_notifications_no_data_refresh_btn);
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
        mProgressBar = (LinearLayout) view.findViewById(R.id.fragment_notifications_progress_bar);

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

        mAdapter = new NotificationsRecyclerViewAdapter(getActivity(), new ArrayList<NotificationItemApp>(), this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_notifications_recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setItemAnimator(null);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter, false, true);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        // Load preferences
        SharedPreferences settings = getContext().getSharedPreferences(Params.PREFS_NAME, 0);
        mNotificationFilterType = settings.getString(Params.PREFS_NOTIFICATION_FILTER_TYPE, "");
        mNotificationFilterSource = settings.getString(Params.PREFS_NOTIFICATION_FILTER_SOURCE, "");

        // SQLite
        loadData(false);

        return view;
    }

    private void loadData(Boolean loadFromHub) {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        if (loadFromHub) {
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
                            mNotificationListSynchronization = new NotificationListApp(getActivity(),
                                    (NotificationListApp.OnNotificationListInteractionListener) mThis, hubConnectionHolder);
                            mNotificationListSynchronization.getNotificationList(hubConnectionHolder.getHubItem().getId(), true);
                            return null;
                        }
                    }.execute();
                }
            }, Params.FADE_ANIMATION_DURATION + 100);
        } else {
            // Loading from SQLLite database without any animation
            NotificationListApp notificationList = new NotificationListApp(getContext(),
                    (NotificationListApp.OnNotificationListInteractionListener) mThis, hubConnectionHolder);
            notificationList.getNotificationList(hubConnectionHolder.getHubItem().getId(), false);
        }
    }

    private HubConnectionHolder getHubConnectionHolder() {
        final HubConnectionHolder hubConnectionHolder = mActivityCommunicator.getActiveHubConnectionHolder();
        if (hubConnectionHolder == null) {
            // Only show no data with message that no hub is configured!
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
        if (context instanceof OnNotificationsFragmentInteractionListener) {
            mListener = (OnNotificationsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNotificationsFragmentInteractionListener");
        }

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

        inflater.inflate(R.menu.notifications, menu);

        menu.findItem(R.id.action_clear).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_notification_clear_all)
                        .colorRes(R.color.icons)
                        .actionBarSize());

        mNotificationTypeMenuItem = menu.findItem(R.id.filter_notification_type).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_filter)
                        .colorRes(R.color.icons)
                        .actionBarSize());

        mNotificationSourceMenuItem = menu.findItem(R.id.filter_notification_source).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_database)
                        .colorRes(R.color.icons)
                        .actionBarSize());

        refreshMenuItemHighlighting();

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void refreshMenuItemHighlighting() {
        if (!mNotificationFilterType.isEmpty()) {
            mNotificationTypeMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_filter)
                    .colorRes(R.color.accent)
                    .actionBarSize());
        } else {
            mNotificationTypeMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_filter)
                    .colorRes(R.color.icons)
                    .actionBarSize());
        }
        if (!mNotificationFilterSource.isEmpty()) {
            mNotificationSourceMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_database)
                    .colorRes(R.color.accent)
                    .actionBarSize());
        } else {
            mNotificationSourceMenuItem.setIcon(new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_database)
                    .colorRes(R.color.icons)
                    .actionBarSize());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                showClearConfirmDialog();
                return true;
            case R.id.filter_notification_type:
                showNotificationTypeDialog();
                return true;
            case R.id.filter_notification_source:
                showNotificationSourceDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showNotificationTypeDialog() {
        // TODO
        Util.showMessage(getActivity(), getString(R.string.work_in_progress));
    }

    private void showNotificationSourceDialog() {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        DeviceListApp deviceList = new DeviceListApp(getContext(), null, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        // Load device sources
        Set<String> deviceSources = deviceList.getAllDeviceSources(hubConnectionHolder.getHubItem().getId());
        // Convert set to array
        final ArrayList<String> deviceSourcesAsArray = new ArrayList<>(deviceSources);
        Collections.sort(deviceSourcesAsArray);
        // Prepare array with all elements item
        ArrayList<String> deviceSourceLabelsAsArray = new ArrayList<>();
        deviceSourceLabelsAsArray.add(getString(R.string.devices_filter_notification_source_reset));
        // Change device type identifier with labels
        for (String deviceSource : deviceSourcesAsArray) {
            deviceSourceLabelsAsArray.add(deviceList.getDeviceItem(deviceSource, hubConnectionHolder.getHubItem().getId()).getMetrics().getTitle());
        }

        if (deviceSources.isEmpty()) {
            Util.showMessage(getContext(), getString(R.string.devices_filter_no_device_sources));
        } else {
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.filter_select, deviceSourceLabelsAsArray);
            alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    if (position == 0) {
                        mNotificationFilterSource = "";
                    } else {
                        mNotificationFilterSource = deviceSourcesAsArray.get(position - 1); // Reduce by one because of first entry: all elements
                    }

                    SharedPreferences settings = getContext().getSharedPreferences(Params.PREFS_NAME, 0);
                    settings.edit().putString(Params.PREFS_NOTIFICATION_FILTER_SOURCE, mNotificationFilterSource).apply();

                    loadData(false);
                    refreshMenuItemHighlighting();
                }
            }).setTitle(getString(R.string.devices_filter_notification_source));
            alert.setPositiveButton(getActivity().getString(R.string.close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    private void showClearConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.confirm_clear_notifications));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
                if (hubConnectionHolder == null) {
                    return;
                }

                NotificationListApp mNotificationList = new NotificationListApp(getContext(),
                        (NotificationListApp.OnNotificationListInteractionListener) mThis, hubConnectionHolder);
                mNotificationList.deleteAllNotificationItem(hubConnectionHolder.getHubItem().getId());

                loadData(false);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.fragment_notifications_title);

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(4);
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

        if (mNotificationListSynchronization != null) {
            mNotificationListSynchronization.cancelSynchronization();
        }

        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void activeHub(ActiveHubEvent activeHubEvent) {
        loadData(false);
    }

    @Subscribe
    public void notificationAdd(ModelAddEvent<NotificationItemApp> modelAddEvent) {
        if (modelAddEvent.getModel().equals("Notification")
                && modelAddEvent.getItem().getHubId().equals(mActivityCommunicator.getActiveHubConnectionHolder().getHubItem().getId())) {
            mAdapter.add(modelAddEvent.getItem());
        }
    }

    @Override
    public void onNotificationListLoaded(ArrayList<NotificationItemApp> notificationList, Boolean loadFromHub) {
        if (loadFromHub) {
            mActivityCommunicator.stopLoading(1);
        }

        if (notificationList != null && notificationList.size() > 0) {
            if (mRecyclerView != null) {
                mAdapter.clear();
                mAdapter.addAll(notificationList);
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
    public void onNotificationListError(String message) {
        Util.showMessage(getActivity(), message);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public Boolean onNotificationsAdapterDelete(NotificationItemApp notificationItemApp) {
        HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return false;
        }

        NotificationListApp notificationList = new NotificationListApp(getContext(),
                (NotificationListApp.OnNotificationListInteractionListener) mThis, hubConnectionHolder);
        notificationList.deleteNotificationItem(notificationItemApp);
        return true;
    }

    @Override
    public void onNotificationAdapterUseNotificationAsFilter(NotificationItemApp notificationItem) {
        // show dialog for filter notifications
        // TODO
    }

    @Override
    public void onNotificationAdapterDetail(NotificationItemApp notificationItem) {
        // show action dialog for notification
        // TODO
    }

    public interface OnNotificationsFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
