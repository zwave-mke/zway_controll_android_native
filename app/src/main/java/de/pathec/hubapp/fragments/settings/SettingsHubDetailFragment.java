package de.pathec.hubapp.fragments.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.mikepenz.iconics.view.IconicsTextView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.events.ConnectionEvent;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.events.ConnectionType;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.Validator;

public class SettingsHubDetailFragment extends Fragment {

    private IMainActivityCommunicator mActivityCommunicator;

    private static final String HUB_ITEM_ID = "id";

    private static final int REQUEST_MAP_PERMISSION = 30;

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    public static final int REQUEST_PICTURE_FROM_GALLERY = 23;
    public static final int REQUEST_PICTURE_FROM_CAMERA = 24;
    public static final int REQUEST_CROP_PICTURE = 25;

    private OnSettingsHubDetailFragmentInteractionListener mListener;

    private Integer mHubItemId;
    private HubList mHubList;

    private Validator mValidator;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    private EditText mTitleEdit, mRemoteServiceEdit, mRemoteIdEdit, mUsernameEdit, mPasswordEdit,
            mLocalIpEdit, mHubSSIDEdit, mHubWifiPasswordEdit, mHubIpEdit;
    private TextInputLayout mTitleLayout, mUsernameLayout,
            mPasswordLayout;
    private ImageView mTileImage;

    private TextView mLocationTxt, mLocationCoordinatesTxt;

    private ProgressBar mRemoteAccessStatusLoading, mLocalAccessStatusLoading, mHubsWifiStatusLoading;

    private IconicsTextView mRemoteAccessStatusTxt, mLocalAccessStatusTxt, mHubsWifiStatusTxt;

    private String mIconPath; // Path to file

    private File mTempFileFromSource = null;
    private Uri mTempUriFromSource = null;

    private File mTempFileFromCrop = null;
    private Uri mTempUriFromCrop = null;

    public SettingsHubDetailFragment() {
        // Required empty public constructor
    }

    public static SettingsHubDetailFragment newInstance(Integer hubItemId) {
        SettingsHubDetailFragment fragment = new SettingsHubDetailFragment();
        Bundle args = new Bundle();
        args.putInt(HUB_ITEM_ID, hubItemId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mValidator = new Validator(getContext(), getActivity());
        mHubList = new HubList(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings_hub_detail, container, false);

        if(getArguments() != null) {
            mHubItemId = getArguments().getInt(HUB_ITEM_ID);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTempFileFromSource != null) {
            outState.putString("mTempFileFromSource", mTempFileFromSource.getPath());
        }
        if (mTempFileFromCrop != null) {
            outState.putString("mTempFileFromCrop", mTempFileFromCrop.getPath());
        }
        if (mIconPath != null) {
            outState.putString("mIconPath", mIconPath);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mTempFileFromSource")) {
                mTempFileFromSource = new File(savedInstanceState.getString("mTempFileFromSource"));
                mTempUriFromSource = Uri.fromFile(mTempFileFromSource);
            }
            if (savedInstanceState.containsKey("mTempFileFromCrop")) {
                mTempFileFromCrop = new File(savedInstanceState.getString("mTempFileFromCrop"));
                mTempUriFromCrop = Uri.fromFile(mTempFileFromCrop);
            }
            if (savedInstanceState.containsKey("mIconPath")) {
                mIconPath = savedInstanceState.getString("mIconPath");
            }
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        mTileImage = (ImageView) getView().findViewById(R.id.fragment_settings_hub_detail_tile_img);
        mTileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog();
            }
        });

        Button mTileImageChangeBtn = (Button) getView().findViewById(R.id.fragment_settings_hub_detail_tile_img_change_btn);
        mTileImageChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog();
            }
        });

        Button mLocationChangeBtn = (Button) getView().findViewById(R.id.fragment_settings_hub_detail_location_btn);
        mLocationChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap();
            }
        });

        mTitleEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_title_edit);
        mRemoteServiceEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_remote_service_edit);
        mRemoteIdEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_remote_id_edit);
        mUsernameEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_username_edit);
        mPasswordEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_password_edit);
        mLocalIpEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_local_ip_edit);
        mHubSSIDEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_hub_ssid_edit);
        mHubWifiPasswordEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_hub_wifi_password_edit);
        mHubIpEdit = (EditText) getView().findViewById(R.id.fragment_settings_hub_detail_hub_ip_edit);

        mTitleEdit.addTextChangedListener(new HubItemTextWatcher(mTitleEdit));
        mUsernameEdit.addTextChangedListener(new HubItemTextWatcher(mUsernameEdit));
        mPasswordEdit.addTextChangedListener(new HubItemTextWatcher(mPasswordEdit));

        mTitleLayout = (TextInputLayout) getView().findViewById(R.id.fragment_settings_hub_detail_title_edit_layout);
        mUsernameLayout = (TextInputLayout) getView().findViewById(R.id.fragment_settings_hub_detail_username_edit_layout);
        mPasswordLayout = (TextInputLayout) getView().findViewById(R.id.fragment_settings_hub_detail_password_edit_layout);

        mRemoteAccessStatusTxt = (IconicsTextView) getView().findViewById(R.id.fragment_settings_hub_detail_remote_access_status);
        mLocalAccessStatusTxt = (IconicsTextView) getView().findViewById(R.id.fragment_settings_hub_detail_local_access_status);
        mHubsWifiStatusTxt = (IconicsTextView) getView().findViewById(R.id.fragment_settings_hub_detail_hubs_wifi_status);

        mRemoteAccessStatusLoading = (ProgressBar) getView().findViewById(R.id.fragment_settings_hub_detail_remote_access_status_loading);
        mLocalAccessStatusLoading = (ProgressBar) getView().findViewById(R.id.fragment_settings_hub_detail_local_access_status_loading);
        mHubsWifiStatusLoading = (ProgressBar) getView().findViewById(R.id.fragment_settings_hub_detail_hubs_wifi_status_loading);

        mLocationTxt = (TextView) getView().findViewById(R.id.fragment_settings_hub_detail_hub_location);
        mLocationCoordinatesTxt = (TextView) getView().findViewById(R.id.fragment_settings_hub_detail_hub_location_coordinates);

        loadData();
    }

    private void showMap() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_MAP_PERMISSION);
        } else {
            mListener.onSettingsHubDetailFragmentShowMap(mHubList.getHubItem(mHubItemId));
        }
    }

    private void loadData() {
        HubItem hubItem = mHubList.getHubItem(mHubItemId);

        if(hubItem != null && !hubItem.getId().equals(-1)) {
            getActivity().setTitle(getString(R.string.fragment_settings_hubs_detail_title));

            mTitleEdit.setText(hubItem.getTitle());

            mIconPath = hubItem.getTile();
            Picasso.with(getActivity()).load(new File(mIconPath)).resize(200, 200).into(mTileImage, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap imageBitmap = ((BitmapDrawable) mTileImage.getDrawable()).getBitmap();
                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                    imageDrawable.setCircular(true);
                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                    mTileImage.setImageDrawable(imageDrawable);
                }
                @Override
                public void onError() { }
            });

            mRemoteServiceEdit.setText(hubItem.getRemoteService());
            mRemoteIdEdit.setText(String.valueOf(hubItem.getRemoteId()));
            mUsernameEdit.setText(hubItem.getUsername());
            mPasswordEdit.setText(hubItem.getPassword());

            mLocalIpEdit.setText(hubItem.getLocalIP());

            mHubSSIDEdit.setText(hubItem.getHubSSID());
            mHubWifiPasswordEdit.setText(hubItem.getHubWifiPassword());
            mHubIpEdit.setText(hubItem.getHubIP());

            if (hubItem.getLocation().isEmpty()) {
                mLocationTxt.setText(getString(R.string.fragment_settings_hub_detail_location_undefined));
            } else {
                DecimalFormat df = new DecimalFormat("#.00");

                mLocationTxt.setText(hubItem.getLocation());
                mLocationCoordinatesTxt.setText(df.format(hubItem.getLatitude()) + "; " + df.format(hubItem.getLongitude()));
            }

        } else {
            // Scenario: Hub detail -> drawer -> ... delete hub -> go back to first hub detail!
            Log.i(Params.LOGGING_TAG, "Hub no longer available!");

            // Go back
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private class HubItemTextWatcher implements TextWatcher {

        private View view;

        private HubItemTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.fragment_settings_hub_detail_title_edit:
                    mValidator.validateBlank(true, mTitleLayout, mTitleEdit, R.string.form_hub_item_title);
                    break;
                case R.id.fragment_settings_hub_detail_username_edit:
                    mValidator.validateBlank(true, mUsernameLayout, mUsernameEdit, R.string.form_hub_item_username);
                    break;
                case R.id.fragment_settings_hub_detail_password_edit:
                    mValidator.validateBlank(true, mPasswordLayout, mPasswordEdit, R.string.form_hub_item_password);
                    break;
            }
        }
    }

    private boolean validateAll() {
        return mValidator.validateBlank(false, mTitleLayout, mTitleEdit, R.string.form_hub_item_title)
                && mValidator.validateBlank(false, mUsernameLayout, mUsernameEdit, R.string.form_hub_item_username)
                && mValidator.validateBlank(false, mPasswordLayout, mPasswordEdit, R.string.form_hub_item_password);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsHubDetailFragmentInteractionListener) {
            mListener = (OnSettingsHubDetailFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsHubsDetailFragmentInteractionListener");
        }

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

        inflater.inflate(R.menu.actions, menu);

        menu.findItem(R.id.action_delete).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_delete)
                        .colorRes(R.color.icons)
                        .actionBarSize())
                .setVisible(true);

        menu.findItem(R.id.action_save).setIcon(
                new IconDrawable(getActivity(), MaterialCommunityIcons.mdi_check)
                        .colorRes(R.color.icons)
                        .actionBarSize())
                .setVisible(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Params.LOGGING_TAG, "Hub detail on option item selected");
        switch (item.getItemId()) {
            case R.id.action_save:
                saveData();
                return true;
            case R.id.action_delete:
                showDeleteConfirmDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmDialog() {
        final HubItem hubItem = mHubList.getHubItem(mHubItemId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.confirm_delete, hubItem.getTitle()));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mHubList.deleteHubItem(hubItem);

                dialog.dismiss();

                // Go back
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveData() {
        final String title = mTitleEdit.getText().toString().trim();
        final String remoteService = mRemoteServiceEdit.getText().toString().trim();
        Integer remoteIdTmp;
        try {
            remoteIdTmp = Integer.parseInt(mRemoteIdEdit.getText().toString());
        } catch (NumberFormatException nfe){
            remoteIdTmp = 0;
        }

        final Integer remoteId = remoteIdTmp;
        final String username = mUsernameEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString();
        final String localIP = mLocalIpEdit.getText().toString();
        final String hubSSID = mHubSSIDEdit.getText().toString();
        final String hubWifiPassword = mHubWifiPasswordEdit.getText().toString();
        final String hubIP = mHubIpEdit.getText().toString();

        if (!validateAll()) {
            Util.showMessage(getActivity(), getString(R.string.validation_failed));

            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                Util.switchViews(mRemoteAccessStatusTxt, mRemoteAccessStatusLoading, Params.FADE_ANIMATION_DURATION);
                Util.switchViews(mLocalAccessStatusTxt, mLocalAccessStatusLoading, Params.FADE_ANIMATION_DURATION);
                // Util.switchViews(mHubsWifiStatusTxt, mHubsWifiStatusLoading, Params.FADE_ANIMATION_DURATION);
            }

            @Override
            protected Void doInBackground( final Void ... params ) {
                // Important (animation)
                try {
                    Thread.sleep(Params.FADE_ANIMATION_DURATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                HubItem hubItem = mHubList.getHubItem(mHubItemId);
                if (hubItem != null) {
                    hubItem.setTitle(title);
                    hubItem.setTile(mIconPath);
                    hubItem.setRemoteService(remoteService);
                    hubItem.setRemoteId(remoteId);
                    hubItem.setUsername(username);
                    hubItem.setPassword(password);
                    hubItem.setLocalIP(localIP);
                    hubItem.setHubSSID(hubSSID);
                    hubItem.setHubWifiPassword(hubWifiPassword);
                    hubItem.setHubIP(hubIP);

                    mHubList.updateHubItem(hubItem);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mListener.onSettingsHubDetailFragmentHubItemChanged(mHubList.getHubItem(mHubItemId));

                // Go back
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(5);
            mActivityCommunicator.setTabStripVisibility(false);
            mActivityCommunicator.setFabVisibility(false);
            mActivityCommunicator.showBackArrow();
            // mActivityCommunicator.showHamburgerIcon();
        }

        // Initialize status
        Util.switchViews(mRemoteAccessStatusTxt, mRemoteAccessStatusLoading, Params.FADE_ANIMATION_DURATION);
        Util.switchViews(mLocalAccessStatusTxt, mLocalAccessStatusLoading, Params.FADE_ANIMATION_DURATION);
        // Util.switchViews(mHubsWifiStatusTxt, mHubsWifiStatusLoading, Params.FADE_ANIMATION_DURATION);
        mListener.onSettingsHubDetailFragmentTriggerLastConnectionEvents(mHubList.getHubItem(mHubItemId));
    }

    @Override
    public void onPause() {
        super.onPause();

        mActivityCommunicator.showHamburgerIcon();

        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void connectionEvent(ConnectionEvent connectionEvent) {
        if (connectionEvent.getHubItem().getId().equals(mHubItemId)) {
            //if (connectionEvent.getType().equals(ConnectionType.HUBS_WIFI)) {
            //    Log.i(Params.LOGGING_TAG, "Connection event (Hubs Wifi): " + connectionEvent.getMessage());
            //    if (mHubsWifiStatusTxt != null && mHubsWifiStatusLoading != null) {
            //        if (connectionEvent.getStatus() < 0) {
            //            mHubsWifiStatusTxt.setText(String.format("{ion-alert} %s", connectionEvent.getMessage()));
            //            mHubsWifiStatusTxt.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_color_red));
            //        } else {
            //            mHubsWifiStatusTxt.setText(String.format("{ion-checkmark} %s", connectionEvent.getMessage()));
            //            mHubsWifiStatusTxt.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_color_green));
            //        }

            //        Util.switchViews(mHubsWifiStatusLoading, mHubsWifiStatusTxt, Params.FADE_ANIMATION_DURATION);
            //    }
            // } else if (connectionEvent.getType().equals(ConnectionType.REMOTE_ACCESS)) {

            mHubsWifiStatusTxt.setText(String.format("{ion-alert} %s", getString(R.string.work_in_progress)));
            Util.switchViews(mHubsWifiStatusLoading, mHubsWifiStatusTxt, Params.FADE_ANIMATION_DURATION);

            if (connectionEvent.getType().equals(ConnectionType.REMOTE_ACCESS)) {
                Log.i(Params.LOGGING_TAG, "Connection event (Remote access): " + connectionEvent.getMessage());
                if (mRemoteAccessStatusTxt != null && mRemoteAccessStatusLoading != null) {
                    if (connectionEvent.getStatus() < 0) {
                        mRemoteAccessStatusTxt.setText(String.format("{ion-alert} %s", connectionEvent.getMessage()));
                        mRemoteAccessStatusTxt.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_color_red));
                    } else {
                        mRemoteAccessStatusTxt.setText(String.format("{ion-checkmark} %s", connectionEvent.getMessage()));
                        mRemoteAccessStatusTxt.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_color_green));
                    }

                    Util.switchViews(mRemoteAccessStatusLoading, mRemoteAccessStatusTxt, Params.FADE_ANIMATION_DURATION);
                }
            } else if (connectionEvent.getType().equals(ConnectionType.LOCAL_ACCESS)) {
                Log.i(Params.LOGGING_TAG, "Connection event (Local access): " + connectionEvent.getMessage());
                if (mLocalAccessStatusTxt != null && mLocalAccessStatusLoading != null) {
                    if (connectionEvent.getStatus() < 0) {
                        mLocalAccessStatusTxt.setText(String.format("{ion-alert} %s", connectionEvent.getMessage()));
                        mLocalAccessStatusTxt.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_color_red));
                    } else {
                        mLocalAccessStatusTxt.setText(String.format("{ion-checkmark} %s", connectionEvent.getMessage()));
                        mLocalAccessStatusTxt.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_color_green));
                    }

                    Util.switchViews(mLocalAccessStatusLoading, mLocalAccessStatusTxt, Params.FADE_ANIMATION_DURATION);
                }
            }
        }
    }

    /**
     * Image input ...
     */

    private void showPhotoDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.image_input, R.layout.filter_select);
        alert.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                if (position == 0) {
                    if (Build.VERSION.SDK_INT < 23) {
                        selectImageFromGallery();
                    } else {
                        initGalleryPermission();
                    }
                } else if (position == 1) {
                    if (Build.VERSION.SDK_INT < 23) {
                        takePhotoWithCamera();
                    } else {
                        initCameraPermission();
                    }
                } else if (position == 2) {
                    updateTile(mTitleEdit.getText().toString());
                }
            }
        }).setTitle(getString(R.string.image_input_title));
        alert.setPositiveButton(getActivity().getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void updateTile(String title) {
        if (mTileImage != null) {
            if(title.isEmpty()) {
                mTileImage.setVisibility(View.GONE);
            } else {
                deleteTileImage();

                mTileImage.setVisibility(View.VISIBLE);
                int color = mColorGenerator.getColor(title);
                mTileImage.setImageDrawable(TextDrawable.builder()
                        .buildRound(title.substring(0, 1).toUpperCase(), color));

                // Store as file and set path
                ColorGenerator generator = ColorGenerator.MATERIAL;
                TextDrawable tileDrawable = TextDrawable.builder().buildRound(title.substring(0, 1).toUpperCase(), generator.getColor(title));
                Bitmap tileBitmap = Util.drawableTileToBitmap(tileDrawable);
                mIconPath = getActivity().getFilesDir().getPath() + "/" + Util.saveImage(getActivity(), tileBitmap, "hub_" + title.toLowerCase() + "_" + String.valueOf(System.currentTimeMillis()), "png");
            }

        }
    }

    private void deleteTileImage() {
        if (mIconPath != null && !mIconPath.isEmpty()) {
            // Delete old image
            File oldImage = new File(mIconPath);
            if (oldImage.exists()) {
                Boolean fileDeleted = oldImage.delete();
                Log.d(Params.LOGGING_TAG, "Delete " + mIconPath + " resulting in: " + fileDeleted);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Util.showMessage(getActivity(), getString(R.string.permission_to_use_camera));
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            takePhotoWithCamera();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initGalleryPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Util.showMessage(getActivity(), getString(R.string.permission_to_read_storage));
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            selectImageFromGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhotoWithCamera();
            } else {
                Util.showMessage(getActivity(), getString(R.string.permission_denied_by_user));
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
            } else {
                Util.showMessage(getActivity(), getString(R.string.permission_denied_by_user));
            }
        } else if (requestCode == REQUEST_MAP_PERMISSION) {
            if (permissions.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMap();
            } else {
                Util.showMessage(getActivity(), getString(R.string.permission_denied_by_user));
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_PICTURE_FROM_GALLERY) && (resultCode == Activity.RESULT_OK)) {
            onImageSelectedFromGallery(data.getData());
        } else if ((requestCode == REQUEST_PICTURE_FROM_CAMERA) && (resultCode == Activity.RESULT_OK)) {
            onImageTakenFromCamera(mTempUriFromSource);
        } else if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == Activity.RESULT_OK)) {
            onImageCropped(mTempUriFromCrop);
        }
    }

    public void selectImageFromGallery() {
        if (mTempFileFromSource == null) {
            try {
                mTempFileFromSource = File.createTempFile("choose", "", getActivity().getExternalCacheDir());
                mTempUriFromSource = Uri.fromFile(mTempFileFromSource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempUriFromSource);
        startActivityForResult(intent, REQUEST_PICTURE_FROM_GALLERY);
    }

    public void takePhotoWithCamera() {
        if (mTempFileFromSource == null) {
            try {
                mTempFileFromSource = File.createTempFile("choose", "", getActivity().getExternalCacheDir());
                mTempUriFromSource = Uri.fromFile(mTempFileFromSource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempUriFromSource);
        startActivityForResult(intent, REQUEST_PICTURE_FROM_CAMERA);
    }

    public void requestCropImage(Uri uri, int outputX, int outputY, int aspectX, int aspectY) {
        // File extension
        String imageFileExtension = ".";

        try {
            String[] filePathColumn = {MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor =
                    getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor.moveToFirst()) {
                int fileNameIndex = cursor.getColumnIndex(filePathColumn[1]);
                String fileName = cursor.getString(fileNameIndex);
                imageFileExtension += fileName.replaceAll("^.*\\.", "");
            }
            cursor.close();
        } catch (Exception e) {
            imageFileExtension = ".jpg";
        }

        if (mTempFileFromCrop == null) {
            try {
                mTempFileFromCrop = File.createTempFile("crop", imageFileExtension, getActivity().getExternalCacheDir());
                mTempUriFromCrop = Uri.fromFile(mTempFileFromCrop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // open crop intent when user selects image
        final Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("output", mTempUriFromCrop);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CROP_PICTURE);
    }

    public void onImageSelectedFromGallery(Uri uri) {
        requestCropImage(uri, 196, 196, 1, 1);
    }

    public void onImageTakenFromCamera(Uri uri) {
        requestCropImage(uri, 196, 196, 1, 1);
    }

    public void onImageCropped(final Uri uri) {
//        try {
//            mIconPath = uri.getPath();
//
//            Picasso.with(getActivity()).load(new File(uri.getPath())).resize(200, 200).into(mTileImage, new Callback() {
//                @Override
//                public void onSuccess() {
//                    Bitmap imageBitmap = ((BitmapDrawable) mTileImage.getDrawable()).getBitmap();
//                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
//                    imageDrawable.setCircular(true);
//                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
//                    mTileImage.setImageDrawable(imageDrawable);
//                }
//                @Override
//                public void onError() {
//                    Log.e(Params.LOGGING_TAG, "Unexpected error");
//                    Util.showMessage(getActivity(), getString(R.string.unexpected_error));
//                }
//            });
//        } catch (Exception e) {
//            Log.e(Params.LOGGING_TAG, "Unexpected error: " + e.getMessage());
//            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
//        }

        final String hubTitle = mTitleEdit.getText().toString().toLowerCase();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Delete old image
                    deleteTileImage();

                    Log.d(Params.LOGGING_TAG, "Image cropped: " + uri.toString());

                    mIconPath = getActivity().getFilesDir().getPath() + "/" + "hub_" + hubTitle + "_" + String.valueOf(System.currentTimeMillis()) + uri.toString().substring(uri.toString().lastIndexOf("."));

                    File croppedImage = new File(uri.getPath());
                    File image = new File(mIconPath);

                    Log.d(Params.LOGGING_TAG, "Image stored: " + image.getPath());

                    // Store temporary file as regular file
                    Util.copyFile(croppedImage, image);
                    Util.copyFile(croppedImage, image);
                } catch (Exception e) {
                    Util.addProtocol(getActivity(), new ProtocolItem(getActivity(), ProtocolType.WARNING,
                            "Unexpected error during set new hub image: " + e.getMessage() + "!", "HubDetail"));

                    Log.e(Params.LOGGING_TAG, "Unexpected error: " + e.getMessage());
                    Util.showMessage(getActivity(), getString(R.string.unexpected_error));

                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mTileImage != null) {
                    Picasso.with(getActivity()).load(new File(mIconPath)).resize(196, 196).into(mTileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap imageBitmap = ((BitmapDrawable) mTileImage.getDrawable()).getBitmap();
                            RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                            imageDrawable.setCircular(true);
                            imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                            mTileImage.setImageDrawable(imageDrawable);
                        }

                        @Override
                        public void onError() {
                            Log.i(Params.LOGGING_TAG, "Set image to image view failed!");
                            Util.addProtocol(getActivity(), new ProtocolItem(getActivity(), ProtocolType.WARNING, "Set hub image failed!", "HubDetail"));
                            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
                        }
                    });
                }
            }
        }.execute();
    }

    public interface OnSettingsHubDetailFragmentInteractionListener {
        void onSettingsHubDetailFragmentHubItemChanged(HubItem item);
        void onSettingsHubDetailFragmentTriggerLastConnectionEvents(HubItem item);
        void onSettingsHubDetailFragmentShowMap(HubItem item);
    }
}
