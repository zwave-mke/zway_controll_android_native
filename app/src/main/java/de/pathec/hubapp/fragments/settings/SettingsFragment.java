package de.pathec.hubapp.fragments.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.util.Params;

public class SettingsFragment extends Fragment {

    private OnSettingsFragmentInteractionListener mListener;
    private IMainActivityCommunicator mActivityCommunicator;

    private ViewPager mPager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(new SettingsPagerAdapter(getChildFragmentManager()));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mActivityCommunicator.setFabVisibility(false);
                        break;
                    case 1:
                        FloatingActionButton fabBtn = mActivityCommunicator.setFabVisibility(true);
                        fabBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_action_add)));
                        fabBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mListener.onSettingsFragmentAddHub();
                            }
                        });
                        break;
                    case 2:
                        mActivityCommunicator.setFabVisibility(false);
                        break;
                    default:
                        mActivityCommunicator.setFabVisibility(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getActivity().findViewById(R.id.main_tabs);
        tabs.setViewPager(mPager);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        super.onAttach(context);
        if (context instanceof OnSettingsFragmentInteractionListener) {
            mListener = (OnSettingsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsFragmentInteractionListener");
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

        getActivity().setTitle(R.string.fragment_settings_title);

        Log.i(Params.LOGGING_TAG, "Current page: " + mPager.getCurrentItem());

        if (mPager.getCurrentItem() == 0) {
            mActivityCommunicator.setFabVisibility(false);
        } else if (mPager.getCurrentItem() == 1) {
            FloatingActionButton fabBtn = mActivityCommunicator.setFabVisibility(true);
            fabBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), (R.drawable.ic_action_add)));
            fabBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onSettingsFragmentAddHub();
                }
            });
        } else if (mPager.getCurrentItem() == 2) {
            mActivityCommunicator.setFabVisibility(false);
        }

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(5);
            mActivityCommunicator.setTabStripVisibility(true);
            // mActivityCommunicator.showBackArrow();
            mActivityCommunicator.showHamburgerIcon();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mActivityCommunicator.setTabStripVisibility(false);
    }

    public class SettingsPagerAdapter extends FragmentPagerAdapter {
        SettingsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.fragment_settings_general_title);
                case 1:
                    return getString(R.string.fragment_settings_hubs_title);
                //case 2:
                //    return getString(R.string.fragment_settings_geofencing_title);
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        } // TODO with geofencing 3

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SettingsGeneralFragment.newInstance();
                case 1:
                    return SettingsHubsFragment.newInstance();
                //case 2:
                //    return SettingsGeofencingFragment.newInstance();
            }

            return null;
        }
    }

    public interface OnSettingsFragmentInteractionListener {
        void onSettingsFragmentAddHub();
    }
}
