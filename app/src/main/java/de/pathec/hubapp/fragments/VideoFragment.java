package de.pathec.hubapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.model.device.DeviceListApp;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Util;

public class VideoFragment extends Fragment {

    private static final String DEVICE_ID = "deviceId";

    private String mDeviceId;

    private IMainActivityCommunicator mActivityCommunicator;

    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment newInstance(String deviceId) {
        VideoFragment fragment = new VideoFragment();

        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_video, container, false);

        if(getArguments() != null) {
            mDeviceId = getArguments().getString(DEVICE_ID, "");
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        HubConnectionHolder hubConnectionHolder = mActivityCommunicator.getActiveHubConnectionHolder();
        if (hubConnectionHolder == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        DeviceListApp deviceList = new DeviceListApp(getActivity(), null, null);
        DeviceItemApp deviceItem = deviceList.getDeviceItem(mDeviceId, hubConnectionHolder.getHubItem().getId());

        IZWayApi zwayApi = hubConnectionHolder.getZWayApi();
        if (zwayApi != null) {
            String zwaySessionId = zwayApi.getZWaySessionId();
            String zwayRemoteSessionId = zwayApi.getZWayRemoteSessionId();
            String zwayTopLevelUrl = zwayApi.getTopLevelUrl();
            String path = deviceItem.getMetrics().getCameraStreamUrl(); // including leading slash!
        } else {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.fragment_camera);

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(5);
            mActivityCommunicator.setTabStripVisibility(false);
            mActivityCommunicator.setFabVisibility(false);
            mActivityCommunicator.showBackArrow();
            // mActivityCommunicator.showHamburgerIcon();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // mActivityCommunicator.showBackArrow();
        mActivityCommunicator.showHamburgerIcon();
    }
}
