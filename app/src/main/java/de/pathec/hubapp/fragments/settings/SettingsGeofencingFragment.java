package de.pathec.hubapp.fragments.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.icons.Icon;
import de.fh_zwickau.informatik.sensor.model.icons.IconList;
import de.fh_zwickau.informatik.sensor.model.instances.Instance;
import de.fh_zwickau.informatik.sensor.model.instances.dummydevice.DummyDevice;
import de.fh_zwickau.informatik.sensor.model.instances.dummydevice.DummyDeviceParams;
import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.events.AddGeofenceEvent;
import de.pathec.hubapp.events.RemoveGeofenceEvent;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;

public class SettingsGeofencingFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    public static final int REQUEST_PICTURE_FROM_GALLERY = 23;
    public static final int REQUEST_PICTURE_FROM_CAMERA = 24;
    public static final int REQUEST_CROP_PICTURE = 25;

    private IMainActivityCommunicator mActivityCommunicator;

    private OnSettingsGeoFencingFragmentInteractionListener mListener;

    private SwitchCompat mGeofencingEnabled;
    private EditText mPhoneName;
    private ImageView mPhoneImage;
    private LinearLayout mGeofencingSettingsView;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    private CardView mNote;

    private String mIconPath; // Path to file

    private File mTempFileFromSource = null;
    private Uri mTempUriFromSource = null;

    private File mTempFileFromCrop = null;
    private Uri mTempUriFromCrop = null;

    private HubList mHubList;

    public SettingsGeofencingFragment() {
        // Required empty public constructor
    }

    public static SettingsGeofencingFragment newInstance() {
        return new SettingsGeofencingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_geofencing, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        mHubList = new HubList(getContext());

        mGeofencingEnabled = (SwitchCompat) getView().findViewById(R.id.fragment_settings_geofencing_phone_settings_enabled);

        mPhoneName = (EditText) getView().findViewById(R.id.fragment_settings_geofencing_phone_name_edit);
        mPhoneImage = (ImageView) getView().findViewById(R.id.fragment_settings_geofencing_phone_image);
        mPhoneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog();
            }
        });

        mNote = (CardView) getView().findViewById(R.id.fragment_settings_geofencing_note);
        for (HubItem hubItem : mHubList.getHubList()) {
            if (hubItem.getLatitude() == 0.0 || hubItem.getLongitude() == 0.0) {
                mNote.setVisibility(View.VISIBLE);
            }
        }

        Button mPhoneImageChangeBtn = (Button) getView().findViewById(R.id.fragment_settings_geofencing_phone_image_change_btn);
        mPhoneImageChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog();
            }
        });

        Button mUpdateDeviceBtn = (Button) getView().findViewById(R.id.fragment_settings_geofencing_update_device_btn);
        mUpdateDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhoneName.getText().toString().isEmpty() || mIconPath.isEmpty()) {
                    Util.showMessage(getActivity(), getString(R.string.fragment_settings_geofencing_phone_name_image_required));
                } else {
                    createOrUpdatePresenceDevice();
                }
            }
        });

        mGeofencingSettingsView = (LinearLayout) getView().findViewById(R.id.fragment_settings_geofencing_settings);

        loadData();
    }

    private void loadData() {
        SharedPreferences settings = getActivity().getSharedPreferences(Params.PREFS_NAME, 0);

        Boolean isGeofencingEnabled = settings.getBoolean(Params.PREFS_HUB_GEOFENCING_ENABLED, false);
        mGeofencingEnabled.setChecked(isGeofencingEnabled);
        if (isGeofencingEnabled) {
            mGeofencingEnabled.setText(getString(R.string.fragment_settings_geofencing_phone_settings_enabled));
            Util.showView(mGeofencingSettingsView, Params.FADE_ANIMATION_DURATION);
        } else {
            mGeofencingEnabled.setText(getString(R.string.fragment_settings_geofencing_phone_settings_disabled));
            Util.hideView(mGeofencingSettingsView, Params.FADE_ANIMATION_DURATION);
        }

        mGeofencingEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mGeofencingEnabled.setText(getString(R.string.fragment_settings_geofencing_phone_settings_enabled));
                    Util.showView(mGeofencingSettingsView, Params.FADE_ANIMATION_DURATION);
                    mActivityCommunicator.setUpGeofence();
                } else {
                    mGeofencingEnabled.setText(getString(R.string.fragment_settings_geofencing_phone_settings_disabled));
                    Util.hideView(mGeofencingSettingsView, Params.FADE_ANIMATION_DURATION);
                    mActivityCommunicator.removeGeofence();
                }
            }
        });

        mPhoneName.setText(settings.getString(Params.PREFS_HUB_PHONE_NAME, ""));
        mIconPath = settings.getString(Params.PREFS_HUB_PHONE_IMAGE, "");
        setPhoneImage();
    }

    private void setPhoneImage() {
        if (mIconPath != null && !mIconPath.isEmpty()) {
            mPhoneImage = (ImageView) getView().findViewById(R.id.fragment_settings_geofencing_phone_image);

            Picasso.with(getActivity()).load(new File(mIconPath)).resize(196, 196).into(mPhoneImage, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap imageBitmap = ((BitmapDrawable) mPhoneImage.getDrawable()).getBitmap();
                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                    imageDrawable.setCircular(true);
                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                    mPhoneImage.setImageDrawable(imageDrawable);
                }

                @Override
                public void onError() {
                    Log.i(Params.LOGGING_TAG, "Set image to image view failed!");
                    Util.addProtocol(getActivity(), new ProtocolItem(getActivity(), ProtocolType.WARNING, "Set phone image failed!", "GeoFencing"));
                    Util.showMessage(getActivity(), getString(R.string.unexpected_error));
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsGeoFencingFragmentInteractionListener) {
            mListener = (OnSettingsGeoFencingFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsGeoFencingFragmentInteractionListener");
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

        BusProvider.getInstance().register(this);

        getActivity().setTitle(R.string.fragment_settings_title);
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences settings = getActivity().getSharedPreferences(Params.PREFS_NAME, 0);

        settings.edit().putBoolean(Params.PREFS_HUB_GEOFENCING_ENABLED, mGeofencingEnabled.isChecked()).apply();
        settings.edit().putString(Params.PREFS_HUB_PHONE_NAME, mPhoneName.getText().toString()).apply();
        settings.edit().putString(Params.PREFS_HUB_PHONE_IMAGE, mIconPath).apply();

        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void addGeofenceEvent(AddGeofenceEvent addGeofenceEvent) {
        if (addGeofenceEvent.getStatus()) {
            Log.i(Params.LOGGING_TAG, "(SettingsGeofencingFragment) Add geofence successful");
        } else {
            Log.i(Params.LOGGING_TAG, "(SettingsGeofencingFragment) Add geofence failed");
        }
    }

    @Subscribe
    public void removeGeofenceEvent(RemoveGeofenceEvent removeGeofenceEvent) {
        if (removeGeofenceEvent.getStatus()) {
            Log.i(Params.LOGGING_TAG, "(SettingsGeofencingFragment) Remove geofence successful");
        } else {
            Log.i(Params.LOGGING_TAG, "(SettingsGeofencingFragment) Remove geofence failed");
        }
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

    private void createOrUpdatePresenceDevice() {
        final String phoneName = mPhoneName.getText().toString();

        new AsyncTask<Void, String, String>() {

            @Override
            protected void onPreExecute() { }

            @Override
            protected String doInBackground(Void... params) {
                HubList hubList = new HubList(getActivity());
                for (final HubItem hubItem : hubList.getHubList()) {
                    new HubConnectionHolder(getActivity(), hubItem, true, new HubConnectionHolder.HubConnectionHolderListener() {
                        @Override
                        public void onConnection(Boolean connected, HubConnectionHolder hubConnectionHolder) {
                            try {
                                // Load icons and check existence of icon
                                IconList iconList = hubConnectionHolder.getIcons();
                                String iconId = "";
                                for (Icon icon : iconList.getIcons()) {
                                    if (mIconPath.contains(icon.getOrgFile())) {
                                        iconId = icon.getFile();
                                    }
                                }

                                // Upload icon
                                if (iconId.isEmpty()) {
                                    Log.d(Params.LOGGING_TAG, "Upload icon.");
                                    hubConnectionHolder.postIcon(new File(mIconPath));

                                    // Load icon id after upload
                                    IconList iconListAfterUpload = hubConnectionHolder.getIcons();
                                    for (Icon icon : iconListAfterUpload.getIcons()) {
                                        if (mIconPath.contains(icon.getOrgFile())) {
                                            iconId = icon.getFile();
                                        }
                                    }
                                }

                                if (iconId.isEmpty()) {
                                    publishProgress("");
                                    return;
                                }

                                Log.d(Params.LOGGING_TAG, "Icon id (file) loaded: " + iconId);

                                // Load devices and check existence of presence device by checking device title with phone name
                                String deviceId = "";
                                DeviceList deviceList = hubConnectionHolder.getDevices();
                                Log.d(Params.LOGGING_TAG, "Searching for phone name: " + phoneName);
                                for (Device device : deviceList.getDevices()) {
                                    Log.d(Params.LOGGING_TAG, "Check device with title: " + device.getMetrics().getTitle());
                                    if (device.getMetrics().getTitle().trim().toLowerCase().equals(phoneName.trim().toLowerCase())) {
                                        deviceId = device.getDeviceId();
                                    }
                                }

                                // Create new device or update title/icon only
                                if (deviceId.isEmpty()) {
                                    // Create dummy device instance
                                    DummyDevice dummyDevice = new DummyDevice();
                                    dummyDevice.setTitle(phoneName);
                                    DummyDeviceParams deviceParams = new DummyDeviceParams();
                                    deviceParams.setDeviceType("switchBinary");
                                    dummyDevice.setParams(deviceParams);

                                    Instance newInstance = hubConnectionHolder.postInstance(dummyDevice);
                                    if (newInstance != null) {
                                        deviceId = newInstance.getModuleId() + "_" + newInstance.getId();
                                    }
                                }

                                if (deviceId.isEmpty()) {
                                    publishProgress("");
                                    return;
                                }

                                Log.d(Params.LOGGING_TAG, "Device id loaded: " + deviceId);

                                Device device = hubConnectionHolder.getDevice(deviceId);

                                if (device != null) {
                                    device.getMetrics().setTitle(phoneName);
                                    device.getIcons().setDefault(iconId);

                                    hubConnectionHolder.putDevice(device);
                                }

                                // Store presence device id to Hub
                                hubItem.setPresenceDeviceId(deviceId);
                                mHubList.updateHubItem(hubItem);

                                publishProgress(hubItem.getTitle());
                            } catch (Exception e) {
                                publishProgress("");
                                Util.addProtocol(getActivity(), new ProtocolItem(getActivity(), ProtocolType.WARNING,
                                        "Unexpected error during updating presence device: " + e.getMessage() + "!", "GeoFencing"));
                            }
                        }
                    });
                }

                return "";
            }

            @Override
            protected void onPostExecute(String result) { }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);

                if (values[0].isEmpty()) {
                    Util.showMessage(getActivity(), getString(R.string.operation_failed));
                } else {
                    Util.showMessage(getActivity(), getString(R.string.operation_success_with_message, values[0]));
                }
            }

        }.execute();
    }

    /**
     * Image input ...
     */

    private void showPhotoDialog() {
        if (mPhoneName.getText().toString().length() > 0 ) {
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
                                updateTile(mPhoneName.getText().toString());
                            }
                        }
                    })
                    .setTitle(getString(R.string.image_input_title))
                    .setPositiveButton(getActivity().getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alert.show();
        } else {
            Util.showMessage(getActivity(), getString(R.string.fragment_settings_geofencing_phone_name_required));
        }
    }

    private void updateTile(String title) {
        if (mPhoneImage != null) {
            if(title.isEmpty()) {
                mPhoneImage.setVisibility(View.GONE);
            } else {
                deletePhoneImage();

                mPhoneImage.setVisibility(View.VISIBLE);
                int color = mColorGenerator.getColor(title);
                mPhoneImage.setImageDrawable(TextDrawable.builder()
                        .buildRound(title.substring(0, 1).toUpperCase(), color));

                // Store as file and set path
                ColorGenerator generator = ColorGenerator.MATERIAL;
                TextDrawable tileDrawable = TextDrawable.builder().buildRound(title.substring(0, 1).toUpperCase(), generator.getColor(title));
                Bitmap tileBitmap = Util.drawableTileToBitmap(tileDrawable);
                mIconPath = getActivity().getFilesDir().getPath() + "/" + Util.saveImage(getActivity(), tileBitmap, "presence_" + title.toLowerCase() + "_" + String.valueOf(System.currentTimeMillis()), "png");
            }

        }
    }

    private void deletePhoneImage() {
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
        } else if(requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
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
        final String phoneName = mPhoneName.getText().toString().toLowerCase();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Delete old image
                    deletePhoneImage();

                    Log.d(Params.LOGGING_TAG, "Image cropped: " + uri.toString());

                    mIconPath = getActivity().getFilesDir().getPath() + "/" + "presence_" + phoneName + "_" + String.valueOf(System.currentTimeMillis()) + uri.toString().substring(uri.toString().lastIndexOf("."));

                    File croppedImage = new File(uri.getPath());
                    File image = new File(mIconPath);

                    Log.d(Params.LOGGING_TAG, "Image stored: " + image.getPath());

                    // Store temporary file as regular file
                    Util.copyFile(croppedImage, image);
                    Util.copyFile(croppedImage, image);

                    SharedPreferences settings = getActivity().getSharedPreferences(Params.PREFS_NAME, 0);
                    settings.edit().putString(Params.PREFS_HUB_PHONE_IMAGE, mIconPath).apply();
                } catch (Exception e) {
                    Util.addProtocol(getActivity(), new ProtocolItem(getActivity(), ProtocolType.WARNING,
                            "Unexpected error during set new phone image: " + e.getMessage() + "!", "GeoFencing"));

                    Log.e(Params.LOGGING_TAG, "Unexpected error: " + e.getMessage());
                    Util.showMessage(getActivity(), getString(R.string.unexpected_error));

                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setPhoneImage();
            }
        }.execute();
    }

    public interface OnSettingsGeoFencingFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
