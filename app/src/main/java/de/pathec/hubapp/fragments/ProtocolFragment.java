package de.pathec.hubapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.adapter.ProtocolRecyclerViewAdapter;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolList;
import de.pathec.hubapp.ui.DividerItemDecoration;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;

public class ProtocolFragment extends Fragment {

    private OnProtocolFragmentInteractionListener mListener;
    private IMainActivityCommunicator mActivityCommunicator;

    private static final String ARG_STATUS = "status";
    private static final String ARG_CATEGORY = "category";
    private String mStatus;
    private String mCategory;

    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRecyclerView;
    private ProtocolRecyclerViewAdapter mAdapter;

    private LinearLayout mNoData;

    public ProtocolFragment() {
    }

    public static ProtocolFragment newInstance(String status, String category) {
        ProtocolFragment fragment = new ProtocolFragment();

        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mStatus = getArguments().getString(ARG_STATUS);
            mCategory = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_protocol_list, container, false);

        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.fragment_protocol_swipe_fresh_layout);
        mNoData = (LinearLayout) view.findViewById(R.id.fragment_protocol_no_data);
        Button mNoDataRefreshBtn = (Button) view.findViewById(R.id.fragment_protocol_no_data_refresh_btn);
        mNoDataRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        mSwipeContainer.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.accent));
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        mAdapter = new ProtocolRecyclerViewAdapter(new ArrayList<ProtocolItem>(), mListener);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_protocol_recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setItemAnimator(null);

        loadData();

        return view;
    }

    private void loadData() {
        // Loading from SQLLite database without any animation
        ProtocolList mProtocolList = new ProtocolList(getContext());
        ArrayList<ProtocolItem> protocolList = mProtocolList.getProtocolList(mStatus, mCategory);

        if (protocolList != null && protocolList.size() > 0) {
            if (mRecyclerView != null) {
                mAdapter.clear();
                mAdapter.addAll(protocolList);
                mSwipeContainer.setRefreshing(false);
                Util.hideView(mNoData, Params.FADE_ANIMATION_DURATION);
                Util.showView(mSwipeContainer, Params.FADE_ANIMATION_DURATION);
            }
        } else {
            Util.hideView(mSwipeContainer, Params.FADE_ANIMATION_DURATION);
            Util.showView(mNoData, Params.FADE_ANIMATION_DURATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // set the title
        getActivity().setTitle(R.string.fragment_protocol_title);

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

        mActivityCommunicator.showHamburgerIcon();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProtocolFragmentInteractionListener) {
            mListener = (OnProtocolFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnProtocolFragmentInteractionListener");
        }

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

        super.onCreateOptionsMenu(menu, inflater);
    }

    public interface OnProtocolFragmentInteractionListener {
        void onProtocolFragmentSelected(ProtocolItem protocolItem);
    }
}
