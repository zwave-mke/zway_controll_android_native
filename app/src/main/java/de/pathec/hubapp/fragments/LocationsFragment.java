package de.pathec.hubapp.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.adapter.LocationsRecyclerViewAdapter;
import de.pathec.hubapp.events.ActiveHubEvent;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.location.LocationListApp;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.recycler_view_helper.OnStartDragListener;
import de.pathec.hubapp.util.recycler_view_helper.SimpleItemTouchHelperCallback;

public class LocationsFragment extends Fragment implements
        LocationListApp.OnLocationListInteractionListener,
        LocationsRecyclerViewAdapter.OnLocationsAdapterInteractionListener,
        OnStartDragListener {

    private IMainActivityCommunicator mActivityCommunicator;

    private OnLocationsFragmentInteractionListener mListener;

    private Fragment mThis;

    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRecyclerView;
    private LocationsRecyclerViewAdapter mAdapter;

    private LinearLayout mNoData;

    private LinearLayout mProgressBar;

    private ItemTouchHelper mItemTouchHelper;

    private LocationListApp mLocationListSynchronization;

    public LocationsFragment() {
        // Required empty public constructor
    }

    public static LocationsFragment newInstance() {
        return new LocationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.fragment_locations_swipe_fresh_layout);
        mNoData = (LinearLayout) view.findViewById(R.id.fragment_locations_no_data);
        Button mNoDataRefreshBtn = (Button) view.findViewById(R.id.fragment_locations_no_data_refresh_btn);
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
        mProgressBar = (LinearLayout) view.findViewById(R.id.fragment_locations_progress_bar);

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

        mAdapter = new LocationsRecyclerViewAdapter(getActivity(), new ArrayList<LocationItemApp>(), this, this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_locations_recycler_view);
        mRecyclerView.setAdapter(mAdapter);

        if (getResources().getConfiguration().orientation == 2) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        } else {
            // Default two columns
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }

        mRecyclerView.setItemAnimator(null);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter, true, false);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

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
            Log.i(Params.LOGGING_TAG, "(LocationsFragment) Loading data from Hub.");

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
                            mLocationListSynchronization = new LocationListApp(getContext(),
                                    (LocationListApp.OnLocationListInteractionListener) mThis, hubConnectionHolder);
                            mLocationListSynchronization.getLocationList(hubConnectionHolder.getHubItem().getId(), true);
                            return null;
                        }
                    }.execute();
                }
            }, Params.FADE_ANIMATION_DURATION + 100);
        } else {
            // Loading from SQLLite database without any animation
            Log.i(Params.LOGGING_TAG, "(LocationsFragment) Loading data from SQLite database.");
            LocationListApp locationList = new LocationListApp(getContext(),
                    (LocationListApp.OnLocationListInteractionListener) mThis, hubConnectionHolder);
            locationList.getLocationList(hubConnectionHolder.getHubItem().getId(), false);
        }
    }

    private HubConnectionHolder getHubConnectionHolder() {
        HubConnectionHolder hubConnectionHolder = mActivityCommunicator.getActiveHubConnectionHolder();
        if (hubConnectionHolder == null) {
            // Only show no data with message that no hub is configured!
            Log.w(Params.LOGGING_TAG, "(LocationsFragment) No Hub configured");
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
        if (context instanceof OnLocationsFragmentInteractionListener) {
            mListener = (OnLocationsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLocationsFragmentInteractionListener");
        }

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

        // Add fragment menu if necessary ...

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.fragment_locations_title);

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(3);
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

        if (mLocationListSynchronization != null) {
            mLocationListSynchronization.cancelSynchronization();
        }

        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void activeHub(ActiveHubEvent activeHubEvent) {
        loadData(false);
    }

    @Override
    public void onLocationListLoaded(ArrayList<LocationItemApp> locationList, Boolean loadFromHub) {
        if (loadFromHub) {
            mActivityCommunicator.stopLoading(1);
        }

        if (locationList != null && locationList.size() > 0) {
            if (mRecyclerView != null) {
                Log.i(Params.LOGGING_TAG, "(LocationsFragment) Loading data complete.");
                mAdapter.clear();
                mAdapter.addAll(locationList);
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
    public void onLocationListError(String message) {
        Util.showMessage(getActivity(), message);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onLocationsAdapterDetail(LocationItemApp locationItem) {
        mListener.onLocationsFragmentDetail(locationItem);
    }

    @Override
    public synchronized void onLocationsAdapterSwap(List<LocationItemApp> list) {
        LocationListApp locationList = new LocationListApp(getContext(), this, null);

        Integer order = 1;
        for (LocationItemApp item : list) {
            item.setOrder(order);
            locationList.updateLocationItem(item);
            order++;
        }
    }

    public interface OnLocationsFragmentInteractionListener {
        void onLocationsFragmentDetail(LocationItemApp locationItem);
    }
}
