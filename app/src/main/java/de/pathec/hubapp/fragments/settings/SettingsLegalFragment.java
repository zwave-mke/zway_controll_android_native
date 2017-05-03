package de.pathec.hubapp.fragments.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;

public class SettingsLegalFragment extends Fragment {

    private IMainActivityCommunicator mActivityCommunicator;

    public SettingsLegalFragment() {
        // Required empty public constructor
    }

    public static SettingsLegalFragment newInstance() {
        return new SettingsLegalFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_legal, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        WebView webView = (WebView) getView().findViewById(R.id.fragment_legal_web_view);
        webView.loadData(getString(R.string.fragment_legal_message), "text/html", "UTF-8");
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

        getActivity().setTitle(R.string.fragment_legal);

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(5);
            mActivityCommunicator.setTabStripVisibility(false);
            mActivityCommunicator.setFabVisibility(false);
            mActivityCommunicator.showBackArrow();
            // mActivityCommunicator.showHamburgerIcon();
        }
    }
}
