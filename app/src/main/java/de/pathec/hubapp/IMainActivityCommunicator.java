package de.pathec.hubapp;

import android.support.design.widget.FloatingActionButton;

import de.pathec.hubapp.util.HubConnectionHolder;

public interface IMainActivityCommunicator {
    // View manipulation callbacks
    void setTabStripVisibility(Boolean visible);
    FloatingActionButton setFabVisibility(Boolean visible);
    void setDrawerItemSelected(long identifier);
    void showDefaultFragment(Boolean popBackStack);
    void showBackArrow();
    void showHamburgerIcon();
    void startLoading();
    void stopLoading(Integer status);

    Boolean isAllLoaderActive();

    // Connection callbacks
    HubConnectionHolder getActiveHubConnectionHolder();
    void changeActiveHub(Integer newActiveHubId, Boolean refreshDrawer);

    void setUpGeofence();
    void removeGeofence();
}
