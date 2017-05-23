package de.pathec.hubapp.fragments.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class SettingsGeneralFragment extends Fragment {

    private IMainActivityCommunicator mActivityCommunicator;

    private OnSettingsGeneralFragmentInteractionListener mListener;

    private Spinner mStartViewSpinner;

    private CheckBox mViewOptionsBatteryInfoCbx;

    private TextView mDebuggingNotificationsCounterTxt;
    private CheckBox mDebuggingNotificationsCbx;

    private TextView mChangelogTxt;

    public SettingsGeneralFragment() {
        // Required empty public constructor
    }

    public static SettingsGeneralFragment newInstance() {
        return new SettingsGeneralFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_general, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        // Start view
        mStartViewSpinner = (Spinner) getView().findViewById(R.id.fragment_settings_general_view_starting_point);
        ArrayAdapter<CharSequence> adapter =  ArrayAdapter.createFromResource(getActivity(), R.array.start_views, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStartViewSpinner.setAdapter(adapter);

        // View options
        mViewOptionsBatteryInfoCbx = (CheckBox) getView().findViewById(R.id.fragment_settings_general_view_battery_info_checkbox);

        // About
        Button openSourceSoftwareBtn = (Button) getView().findViewById(R.id.fragment_settings_general_open_source_btn);
        openSourceSoftwareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSettingsGeneralOpenSourceSoftware();
            }
        });

        // Debugging
        mDebuggingNotificationsCounterTxt = (TextView) getView().findViewById(R.id.fragment_settings_general_debugging_notifications_counter);
        mDebuggingNotificationsCbx = (CheckBox) getView().findViewById(R.id.fragment_settings_general_debugging_notifications_checkbox);

        Button protocolBtn = (Button) getView().findViewById(R.id.fragment_settings_general_debugging_protocol);
        protocolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSettingsGeneralProtocol("", "");
            }
        });

        // Changelog
        mChangelogTxt = (TextView) getView().findViewById(R.id.fragment_settings_general_changelog);
    }

    private void loadData() {
        SharedPreferences settings = getActivity().getSharedPreferences(Params.PREFS_NAME, 0);

        // Start view
        String startView = settings.getString(Params.PREFS_START_VIEW, "");
        switch (startView) {
            case "Dashboard":
                mStartViewSpinner.setSelection(0);
                break;
            case "Elements":
                mStartViewSpinner.setSelection(1);
                break;
            case "Locations":
                mStartViewSpinner.setSelection(2);
                break;
        }

        // View options
        mViewOptionsBatteryInfoCbx.setChecked(settings.getBoolean(Params.PREFS_VIEW_BATTERY_INFO, false));

        // Debugging
        mDebuggingNotificationsCounterTxt.setText(String.format("Notification counter: %s", String.valueOf(settings.getLong(Params.PREFS_FCM_COUNTER, 0))));
        mDebuggingNotificationsCbx.setChecked(settings.getBoolean(Params.PREFS_DEBUGGING_NOTIFICATIONS, false));

        // Changelog
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mChangelogTxt.setText(Html.fromHtml(getString(R.string.fragment_settings_general_changelog),Html.FROM_HTML_MODE_LEGACY));
        } else {
            mChangelogTxt.setText(Html.fromHtml(getString(R.string.fragment_settings_general_changelog)));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsGeneralFragmentInteractionListener) {
            mListener = (OnSettingsGeneralFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsGeneralFragmentInteractionListener");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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

        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences settings = getActivity().getSharedPreferences(Params.PREFS_NAME, 0);
        settings.edit().putBoolean(Params.PREFS_DEBUGGING_NOTIFICATIONS, mDebuggingNotificationsCbx.isChecked()).apply();

        String startView = "Dashboard";
        switch (mStartViewSpinner.getSelectedItemPosition()) {
            case 0:
                startView = "Dashboard";
                break;
            case 1:
                startView = "Elements";
                break;
            case 2:
                startView = "Locations";
                break;
        }
        settings.edit().putString(Params.PREFS_START_VIEW, startView).apply();

        settings.edit().putBoolean(Params.PREFS_VIEW_BATTERY_INFO, mViewOptionsBatteryInfoCbx.isChecked()).apply();

        Util.addProtocol(getActivity(), new ProtocolItem(getActivity(), ProtocolType.INFO, "General settings saved!", "Settings"));
    }

    public interface OnSettingsGeneralFragmentInteractionListener {
        void onSettingsGeneralProtocol(String status, String category);
        void onSettingsGeneralOpenSourceSoftware();
    }
}
