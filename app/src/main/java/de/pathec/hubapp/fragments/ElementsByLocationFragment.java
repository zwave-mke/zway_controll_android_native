package de.pathec.hubapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.events.LocationTabSlideEvent;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.location.LocationListApp;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;

public class ElementsByLocationFragment extends Fragment {

    private OnElementsByLocationFragmentInteractionListener mListener;
    private IMainActivityCommunicator mActivityCommunicator;

    private static final String LOCATION_ID = "locationId";

    private Integer mLocationId = -1;

    private ViewPager mPager;

    private ArrayList<LocationItemApp> mLocations;

    private Vibrator mVibrator;

    public ElementsByLocationFragment() {
        // Required empty public constructor
    }

    public static ElementsByLocationFragment newInstance(Integer locationId) {
        ElementsByLocationFragment fragment = new ElementsByLocationFragment();

        Bundle args = new Bundle();
        args.putInt(LOCATION_ID, locationId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            mLocationId = getArguments().getInt(LOCATION_ID, -1);
        }

        mVibrator =  (Vibrator) getActivity().getSystemService(Activity.VIBRATOR_SERVICE);

        HubConnectionHolder hubConnectionHolder = mActivityCommunicator.getActiveHubConnectionHolder();

        if (hubConnectionHolder != null) {
            LocationListApp locationList = new LocationListApp(getContext(), null, null);
            mLocations = locationList.getLocationList(hubConnectionHolder.getHubItem().getId());
        } else { // prevent null pointer exceptions
            mLocations = new ArrayList<>();
            mActivityCommunicator.showDefaultFragment(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_elements_by_location, container, false);

        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(new ElementsByLocationPagerAdapter(getChildFragmentManager()));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getActivity().findViewById(R.id.main_tabs);
        tabs.setViewPager(mPager);

        if (mLocationId != -1) {
            mPager.setCurrentItem(getPagerItemByLocationId());
        }

        return view;
    }

    private Integer getPagerItemByLocationId() {
        Integer itemNum = 0;

        for (LocationItemApp item : mLocations) {
            if (item.getId() == mLocationId) {
                return itemNum;
            }

            itemNum++;
        }

        return 0;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        super.onAttach(context);
        if (context instanceof OnElementsByLocationFragmentInteractionListener) {
            mListener = (OnElementsByLocationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnElementsByLocationFragmentInteractionListener");
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
    public void onResume() {
        super.onResume();

        mActivityCommunicator.stopLoading(0);

        getActivity().setTitle(R.string.fragment_elements_title);

        Log.i(Params.LOGGING_TAG, "Current page: " + mPager.getCurrentItem());

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(5);
            mActivityCommunicator.setTabStripVisibility(true);
            // mActivityCommunicator.showBackArrow();
            mActivityCommunicator.showHamburgerIcon();
        }

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void locationTabSlideEvent(LocationTabSlideEvent locationTabSlideEvent) {
        if(mVibrator.hasVibrator()) {
            mVibrator.vibrate(50);
        }

        if (locationTabSlideEvent.getDirection() == -1) {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
        } else if (locationTabSlideEvent.getDirection() == 1) {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mActivityCommunicator.setTabStripVisibility(false);
    }

    public class ElementsByLocationPagerAdapter extends FragmentPagerAdapter {
        ElementsByLocationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mLocations.get(position).getTitle();
        }

        @Override
        public int getCount() {
            return mLocations.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ElementsFragment.newInstance(false, mLocations.get(position).getId());
        }
    }

    public interface OnElementsByLocationFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
