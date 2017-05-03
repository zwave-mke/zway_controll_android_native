package de.pathec.hubapp.fragments.help;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.adapter.HelpRecyclerViewAdapter;
import de.pathec.hubapp.model.help.Help;
import de.pathec.hubapp.ui.DividerItemDecoration;

public class HelpFragment extends Fragment implements
        HelpRecyclerViewAdapter.OnHelpAdapterInteractionListener {

    private IMainActivityCommunicator mActivityCommunicator;

    private OnHelpFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private HelpRecyclerViewAdapter mAdapter;

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        List<Help> helpList = new ArrayList<>();
        helpList.add(new Help(getString(R.string.fragment_help_overview_title), "", "01_features"));
        helpList.add(new Help(getString(R.string.fragment_help_navigation_title), "", "02_navigation"));
        helpList.add(new Help(getString(R.string.fragment_help_synchronization_title), "", "03_synchronization"));
        helpList.add(new Help(getString(R.string.fragment_help_faq_title), "", "faq"));

        mAdapter = new HelpRecyclerViewAdapter(getActivity(), helpList, this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_help_recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setItemAnimator(null);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHelpFragmentInteractionListener) {
            mListener = (OnHelpFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHelpFragmentInteractionListener");
        }

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

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

        getActivity().setTitle(R.string.fragment_help_title);

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(6);
            mActivityCommunicator.setTabStripVisibility(false);
            mActivityCommunicator.setFabVisibility(false);
            // mActivityCommunicator.showBackArrow();
            mActivityCommunicator.showHamburgerIcon();
        }
    }

    @Override
    public void onHelpAdapterDetail(Help help) {
        mListener.onHelpFragmentDetail(help);
    }

    public interface OnHelpFragmentInteractionListener {
        void onHelpFragmentDetail(Help help);
    }
}
