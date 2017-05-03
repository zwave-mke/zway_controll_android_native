package de.pathec.hubapp.fragments.settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.events.ActiveHubEvent;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.util.Util;

public class SettingsHubsFragment extends Fragment {

    private IMainActivityCommunicator mActivityCommunicator;

    private OnSettingsHubsFragmentInteractionListener mListener;

    private HubList mHubList;

    private Vibrator mVibrator;

    public SettingsHubsFragment() {
        // Required empty public constructor
    }

    public static SettingsHubsFragment newInstance() {
        return new SettingsHubsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHubList = new HubList(getActivity());
        mVibrator =  (Vibrator) getActivity().getSystemService(Activity.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_hubs, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadData();
    }

    private void loadData() {
        if(getView() == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        LinearLayout mHubListLayout = (LinearLayout) getView().findViewById(R.id.fragment_settings_hubs_list);
        mHubListLayout.removeAllViews();

        ArrayList<HubItem> hubs =  mHubList.getHubList();

        for (final HubItem item : hubs) {
            final CardView hubLayout = (CardView) getActivity().getLayoutInflater().inflate(R.layout.fragment_settings_hubs_item, mHubListLayout, false);

            final ImageView hubTile = (ImageView) hubLayout.findViewById(R.id.fragments_settings_hubs_item_tile_img);
            Picasso.with(getActivity()).load(new File(item.getTile())).resize(200, 200).into(hubTile, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap imageBitmap = ((BitmapDrawable) hubTile.getDrawable()).getBitmap();
                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                    imageDrawable.setCircular(true);
                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                    hubTile.setImageDrawable(imageDrawable);
                }
                @Override
                public void onError() { }
            });

            TextView hubTitle = (TextView) hubLayout.findViewById(R.id.fragments_settings_hubs_item_title_lbl);
            hubTitle.setText(item.getTitle());

            TextView hubLocation = (TextView) hubLayout.findViewById(R.id.fragments_settings_hubs_item_location_lbl);
            hubLocation.setText(item.getLocation());

            IconicsImageView hubSelected = (IconicsImageView) hubLayout.findViewById(R.id.fragment_settings_hubs_item_selected);
            if (mActivityCommunicator.getActiveHubConnectionHolder().getHubItem().getId().equals(item.getId())) {
                hubSelected.setVisibility(View.VISIBLE);
            } else {
                hubSelected.setVisibility(View.GONE);
            }

            hubLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onSettingsHubsFragmentDetail(item);
                }
            });

            hubLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(mVibrator.hasVibrator()) {
                        mVibrator.vibrate(50);
                    }

                    mActivityCommunicator.changeActiveHub(item.getId(), true);
                    return true;
                }
            });

            mHubListLayout.addView(hubLayout);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsHubsFragmentInteractionListener) {
            mListener = (OnSettingsHubsFragmentInteractionListener) context;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.fragment_settings_title);

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
        BusProvider.getInstance().unregister(this);

        super.onPause();
    }

    @Subscribe
    public void activeHub(ActiveHubEvent activeHubEvent) {
        loadData();
    }

    public interface OnSettingsHubsFragmentInteractionListener {
        void onSettingsHubsFragmentDetail(HubItem item);
    }
}
