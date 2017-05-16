package de.pathec.hubapp.intro_v2;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.IZWayApiCallbacks;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistory;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryList;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceCommand;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.instances.Instance;
import de.fh_zwickau.informatik.sensor.model.instances.InstanceList;
import de.fh_zwickau.informatik.sensor.model.locations.Location;
import de.fh_zwickau.informatik.sensor.model.locations.LocationList;
import de.fh_zwickau.informatik.sensor.model.modules.ModuleList;
import de.fh_zwickau.informatik.sensor.model.namespaces.NamespaceList;
import de.fh_zwickau.informatik.sensor.model.notifications.Notification;
import de.fh_zwickau.informatik.sensor.model.notifications.NotificationList;
import de.fh_zwickau.informatik.sensor.model.profiles.Profile;
import de.fh_zwickau.informatik.sensor.model.profiles.ProfileList;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.controller.ZWaveController;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.devices.ZWaveDevice;
import de.fh_zwickau.informatik.sensor.z_way_library.ZWayApiAndroid;
import de.pathec.hubapp.MainActivity;
import de.pathec.hubapp.R;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.model.device.DeviceListApp;
import de.pathec.hubapp.model.devicehistory.DeviceHistoryItemApp;
import de.pathec.hubapp.model.devicehistory.DeviceHistoryListApp;
import de.pathec.hubapp.model.hub.HubItem;
import de.pathec.hubapp.model.hub.HubList;
import de.pathec.hubapp.model.icon.IconItemApp;
import de.pathec.hubapp.model.icon.IconListApp;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.model.location.LocationListApp;
import de.pathec.hubapp.model.notification.NotificationItemApp;
import de.pathec.hubapp.model.notification.NotificationListApp;
import de.pathec.hubapp.model.profile.ProfileItemApp;
import de.pathec.hubapp.model.profile.ProfileListApp;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.ui.CameraSourcePreview;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.Validator;
import de.pathec.hubapp.util.ZWayDiscovery;
import de.pathec.hubapp.util.ZWayHubConfiguration;

public class Intro extends AppCompatActivity implements
        ZWayDiscovery.ZWayDiscoveryInteractionListener,
        IZWayApiCallbacks {

    private static final int HUB_APP_PERMISSION_REQUEST_CAMERA = 1;

    private LinearLayout mPermissionLayout, mCameraLayout, mQRCodeLayout, mManualLayout, mConnectingLayout;

    private EditText mUsernameEdit, mPasswordEdit, mRemoteIdEdit, mLocalIpEdit;
    private TextInputLayout mUsernameEditLayout, mPasswordEditLayout;

    private CameraSourcePreview mCameraPreview;
    private Vibrator mVibrator;

    // Discovery
    private TextView mDiscoveryProgressMessageTxt;
    private ProgressBar mDiscoveryProgress;
    private ArrayList<String> mDiscoveryResults;
    private Integer mDiscoveryCount = 0;
    private Integer mDiscoveryChecked = 0;

    // Connection View
    private IconicsImageView mConnectingFailed;
    private IconicsImageView mConnectingSuccess;
    private ProgressBar mConnectingProgress;
    private TextView mConnectingProgressMessageTxt;

    private AsyncTask<Void, String, Void> mConnectingTask;
    private HubItem mHubItem;

    private Button mDetailsBtn, mFinishBtn, mDiscoveryBtn;

    private ZWayHubConfiguration mZWayHubConfiguration;
    private ArrayList<ProtocolItem> mProtocolList = new ArrayList<>();

    private Boolean mHubWifiConnected = false;
    private Boolean mConnectingComplete = false;

    // Back button
    private boolean mTwiceBackToExitPressedOnce = false;
    private Toast mTwiceBackToExitToast;

    private Handler mToastHandler;

    // QR code tracked
    private Boolean mQRCodeTracked = false;
    private Handler mQRCodeTrackedHandler;

    private ZWayDiscovery mZWayDiscovery;

    private Validator mValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        mQRCodeTrackedHandler = new Handler();

        // Views
        mPermissionLayout = (LinearLayout) findViewById(R.id.intro_permission);
        mCameraLayout = (LinearLayout) findViewById(R.id.intro_camera);
        mQRCodeLayout = (LinearLayout) findViewById(R.id.intro_qr_code);
        mManualLayout = (LinearLayout) findViewById(R.id.intro_manual);
        mConnectingLayout = (LinearLayout) findViewById(R.id.intro_connecting);

        // Initialize views
        mQRCodeLayout.setVisibility(View.GONE);
        mCameraLayout.setVisibility(View.GONE);
        mManualLayout.setVisibility(View.GONE);
        mPermissionLayout.setVisibility(View.GONE);
        mConnectingLayout.setVisibility(View.GONE);

        // Manual edit texts
        mUsernameEdit = (EditText) findViewById(R.id.intro_username_edit);
        mPasswordEdit = (EditText) findViewById(R.id.intro_password_edit);
        mRemoteIdEdit = (EditText) findViewById(R.id.intro_remote_id_edit);
        mLocalIpEdit = (EditText) findViewById(R.id.intro_local_ip_edit);

        mUsernameEdit.addTextChangedListener(new IntroManualTextWatcher(mUsernameEdit));
        mPasswordEdit.addTextChangedListener(new IntroManualTextWatcher(mPasswordEdit));

        mUsernameEditLayout = (TextInputLayout) findViewById(R.id.intro_username_edit_layout);
        mPasswordEditLayout = (TextInputLayout) findViewById(R.id.intro_password_edit_layout);

        // Connecting
        mConnectingFailed = (IconicsImageView) findViewById(R.id.intro_connecting_failed);
        mConnectingSuccess = (IconicsImageView) findViewById(R.id.intro_connecting_success);
        mConnectingProgress = (ProgressBar) findViewById(R.id.intro_connecting_progress_bar);
        mConnectingProgressMessageTxt = (TextView) findViewById(R.id.intro_connecting_progress_message);

        // Discovery
        mDiscoveryProgressMessageTxt = (TextView) findViewById(R.id.intro_manual_local_access_progress_message);
        mDiscoveryProgress = (ProgressBar) findViewById(R.id.intro_manual_local_access_progress_bar);
        mDiscoveryResults = new ArrayList<>();

        // Buttons
        Button qrCodeBtn = (Button) findViewById(R.id.intro_qr_code_btn);
        Button manualBtn = (Button) findViewById(R.id.intro_manual_btn);
        Button permissionBtn = (Button) findViewById(R.id.intro_permission_btn);
        Button connectBtn = (Button) findViewById(R.id.intro_connect_btn);
        mDiscoveryBtn = (Button) findViewById(R.id.intro_manual_local_access_search_btn);
        mDetailsBtn = (Button) findViewById(R.id.intro_connecting_details_btn);
        mFinishBtn = (Button) findViewById(R.id.intro_finish_btn);

        mToastHandler = new Handler();

        qrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.switchViews(mManualLayout, mQRCodeLayout);
            }
        });

        manualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.switchViews(mQRCodeLayout, mManualLayout);
            }
        });

        permissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, HUB_APP_PERMISSION_REQUEST_CAMERA);
                }
            }
        });

        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectingComplete) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    Util.showMessage(getApplicationContext(), getString(R.string.please_wait));
                }
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateAll()) {
                    Util.showMessage(getApplicationContext(), getString(R.string.form_hub_item_configuration_not_complete));
                    return;
                }

                mZWayHubConfiguration = new ZWayHubConfiguration();
                mZWayHubConfiguration.setUsername(mUsernameEdit.getText().toString());
                mZWayHubConfiguration.setPassword(mPasswordEdit.getText().toString());
                try {
                    mZWayHubConfiguration.setRemoteId(Integer.parseInt(mRemoteIdEdit.getText().toString()));
                } catch (NumberFormatException nfe) {
                    mZWayHubConfiguration.setRemoteId(null);
                }
                mZWayHubConfiguration.setLocalIp(mLocalIpEdit.getText().toString());

                Util.switchViews(mManualLayout, mConnectingLayout);

                initHub();
            }
        });

        mDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailDialog();
            }
        });

        mDiscoveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mZWayDiscovery.isRunning()) {
                    mDiscoveryBtn.setText(getString(android.R.string.cancel));
                    startDiscovery();
                } else {
                    mDiscoveryBtn.setText(getString(R.string.intro_manual_local_access_search_btn));
                    cancelDiscovery();
                }
            }
        });

        mVibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
        mZWayDiscovery = new ZWayDiscovery(this);
        mValidator = new Validator(this, this);
    }

    private void showDetailDialog() {
        String[] messages = new String[mProtocolList.size()];
        Integer index = 0;
        for (ProtocolItem protocolItem : mProtocolList) {
            messages[index] = protocolItem.getText();
            index++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MaterialBaseTheme_Light_Dialog);
        builder.setTitle(getString(R.string.intro_connecting_details_title))
            .setItems(messages, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                }
            })
            .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create().show();
    }

    private class IntroManualTextWatcher implements TextWatcher {

        private View view;

        private IntroManualTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.intro_username_edit:
                    mValidator.validateBlank(true, mUsernameEditLayout, mUsernameEdit, R.string.form_hub_item_username);
                    break;
                case R.id.intro_password_edit:
                    mValidator.validateBlank(true, mPasswordEditLayout, mPasswordEdit, R.string.form_hub_item_password);
                    break;
            }
        }
    }

    private boolean validateAll() {
        return mValidator.validateBlank(false, mUsernameEditLayout, mUsernameEdit, R.string.form_hub_item_username)
                && mValidator.validateBlank(false, mPasswordEditLayout, mPasswordEdit, R.string.form_hub_item_password);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mQRCodeLayout.getVisibility() == View.GONE
            && mManualLayout.getVisibility() == View.GONE
            && mConnectingLayout.getVisibility() == View.GONE) {
            mQRCodeLayout.setVisibility(View.VISIBLE);
        }

        if (mQRCodeLayout.getVisibility() == View.VISIBLE) {
            // Check camera permission
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Util.switchViews(mPermissionLayout, mCameraLayout);
                startCamera();
            } else {
                mQRCodeLayout.setVisibility(View.VISIBLE);
                Util.switchViews(mCameraLayout, mPermissionLayout);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mTwiceBackToExitToast != null) {
            mTwiceBackToExitToast.cancel();
        }

        stopCamera();
        cancelDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopCamera();
        cancelDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case HUB_APP_PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                }
            }
        }
    }

    private void cancelDiscovery() {
        mDiscoveryProgress.setVisibility(View.GONE);
        mDiscoveryProgressMessageTxt.setVisibility(View.GONE);
        mDiscoveryProgressMessageTxt.setText("");

        mZWayDiscovery.cancelScan();
    }

    private void startDiscovery() {
        if (!mZWayDiscovery.isRunning()) {
            mDiscoveryProgress.setVisibility(View.VISIBLE);
            mDiscoveryProgressMessageTxt.setVisibility(View.VISIBLE);
            mDiscoveryProgressMessageTxt.setText("");

            mDiscoveryResults.clear();
            mDiscoveryChecked = 0;
            mDiscoveryCount = 0;

            mZWayDiscovery.startScan();
        }
    }

    private void stopCamera() {
        if (mCameraPreview != null) {
            mCameraPreview.stop();
            mCameraPreview = null;
        }
    }

    private void startCamera() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        if (!barcodeDetector.isOperational()) {
            Log.w(Params.LOGGING_TAG, "Detector dependencies are not yet available.");
            Util.addProtocol(this, new ProtocolItem(this, ProtocolType.ERROR, "Detector dependencies are not yet available.", "System"));
            return;
        }

        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(new BarcodeTrackerFactory()).build());

        CameraSource mCameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(194, 300)
                .setRequestedFps(10.0f)
                .setAutoFocusEnabled(true).build();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(Params.LOGGING_TAG, "Camera permission required.");
                Util.addProtocol(this, new ProtocolItem(this, ProtocolType.ERROR, "Camera permission required.", "System"));
                return;
            }

            mCameraPreview = (CameraSourcePreview) findViewById(R.id.intro_camera_preview);
            mCameraPreview.start(mCameraSource);
        } catch (IOException e) {
            Log.w(Params.LOGGING_TAG, "Unable to start camera source.", e);
            Util.addProtocol(this, new ProtocolItem(this, ProtocolType.ERROR, "Unable to start camera source.", "System"));
            mCameraSource.release();
        }
    }

    @Override
    public void onZWayDiscoveryFound(String ipAddress) {
        mDiscoveryResults.add(ipAddress);
    }

    @Override
    public void onZWayDiscoveryAddressCount(Integer addressCount) {
        mDiscoveryCount = addressCount;
    }

    @Override
    public void onZWayDiscoveryAddressChecked(Integer progressCount) {
        mDiscoveryChecked = progressCount;

        if (mDiscoveryChecked.equals(mDiscoveryCount)) {
            mDiscoveryBtn.setText(getString(R.string.intro_manual_local_access_search_btn));

            Integer resultCount = mDiscoveryResults.size();

            if (resultCount == 1) {
                mLocalIpEdit.setText(mDiscoveryResults.get(0));
            } else if (resultCount == 0) {
                mLocalIpEdit.setText("");
                Util.showMessage(this, getString(R.string.intro_manual_local_access_no_ip_found));
            } else {
                showLocalAccessDialog();
            }

            mDiscoveryProgress.setVisibility(View.GONE);
            mDiscoveryProgressMessageTxt.setVisibility(View.GONE);
            mDiscoveryProgressMessageTxt.setText("");
        } else {
            mDiscoveryProgressMessageTxt.setText(mDiscoveryChecked + "/" + mDiscoveryCount);
        }
    }

    private void showLocalAccessDialog() {
        LayoutInflater inflater = getLayoutInflater();
        final View discoveryResultDialog = inflater.inflate(R.layout.dialog_discovery_result, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.MaterialBaseTheme_Light_Dialog);
        alert.setView(discoveryResultDialog);

        alert.setTitle(getString(R.string.intro_manual_local_access_results_title));

        final String results[] = mDiscoveryResults.toArray(new String[mDiscoveryResults.size()]);
        final Spinner discoveryResults = (Spinner) discoveryResultDialog.findViewById(R.id.dialog_discovery_result_results);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, results);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        discoveryResults.setAdapter(adapter);
        discoveryResults.setSelection(0, false);

        alert.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedIpAddress = (String) discoveryResults.getSelectedItem();

                if (selectedIpAddress != null) {
                    mLocalIpEdit.setText(selectedIpAddress);
                    dialog.dismiss();
                }
            }
        });

        final TextView message = (TextView) discoveryResultDialog.findViewById(R.id.dialog_discovery_result_message);
        message.setText(getString(R.string.intro_manual_local_access_results_message));
        alert.show();
    }

    private class BarcodeTracker extends Tracker<Barcode> {
        @Override
        public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
            if(!mQRCodeTracked) {
                if(mVibrator.hasVibrator()) {
                    mVibrator.vibrate(50);
                }

                ZWayHubConfiguration configuration = new ZWayHubConfiguration();

                try {
                    Map<String, List<String>> params = Util.splitQuery(item.rawValue);

                    for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                        switch (entry.getKey().trim().toLowerCase()) {
                            case "service":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setRemoteService(entry.getValue().get(0));
                                }
                                break;
                            case "id":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setRemoteId(Integer.parseInt(entry.getValue().get(0)));
                                }
                                break;
                            case "login":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setUsername(entry.getValue().get(0));
                                }
                                break;
                            case "passwd":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setPassword(entry.getValue().get(0));
                                }
                                break;
                            case "ssid":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setHubSSID(entry.getValue().get(0));
                                }
                                break;
                            case "wpa":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setHubWifiPassword(entry.getValue().get(0));
                                }
                                break;
                            case "ip":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setHubIP(entry.getValue().get(0));
                                }
                                break;
                            case "eth":
                                if (entry.getValue().get(0) != null) {
                                    configuration.setLocalIp(entry.getValue().get(0));
                                }
                                break;
                            default:
                                Log.w(Params.LOGGING_TAG, "Unknown QR-Code tag found: " + entry.getKey() + ".");
                                ProtocolItem protocolItem = new ProtocolItem(getApplicationContext(), ProtocolType.WARNING, "Unknown QR-Code tag found: " + entry.getKey() + ".", "System");
                                Util.addProtocol(getApplicationContext(), protocolItem);
                        }
                    }
                } catch (Exception e) {
                    Log.w(Params.LOGGING_TAG, "Unable to parse QR-Code values.", e);
                    Util.addProtocol(getApplicationContext(), new ProtocolItem(getApplicationContext(), ProtocolType.ERROR, "Unable to parse QR-Code values.", "System"));
                }

                // Validate required information
                if(!(configuration.getUsername().isEmpty() && configuration.getPassword().isEmpty())) {
                    mQRCodeTracked = true;
                    mZWayHubConfiguration = configuration;
                    mQRCodeTrackedHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Util.switchViews(mQRCodeLayout, mConnectingLayout);

                            initHub();
                        }
                    });
                }
            }
        }
    }

    private class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
        @Override
        public Tracker<Barcode> create(Barcode barcode) {
            return new BarcodeTracker();
        }
    }

    @Override
    public void onBackPressed() {
        if(mQRCodeLayout.getVisibility() == View.VISIBLE || mManualLayout.getVisibility() == View.VISIBLE) {
            if (mTwiceBackToExitPressedOnce) {
                Intro.this.finish();
                return;
            }
            mTwiceBackToExitPressedOnce = true;
            mTwiceBackToExitToast = Toast.makeText(this, getString(R.string.back_button_twice), Toast.LENGTH_SHORT);
            mTwiceBackToExitToast.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTwiceBackToExitPressedOnce = false;
                }
            }, 2000);
        } else if (mConnectingLayout.getVisibility() == View.VISIBLE) {
            if (mConnectingTask != null) {
                mConnectingTask.cancel(true);
            }
            mConnectingTask = null;
            mQRCodeTracked = false;

            rollbackInitialization();

            Util.switchViews(mConnectingLayout, mQRCodeLayout);
        } else {
            super.onBackPressed();
        }
    }

    private void initHub() {
        final HubList hubList = new HubList(this);
        final Activity activity = this;
        final SharedPreferences settings = getSharedPreferences(Params.PREFS_NAME, 0);

        mConnectingTask = new AsyncTask<Void, String, Void>() {
            @Override
            protected void onPreExecute() {
                mConnectingProgress.setVisibility(View.VISIBLE);
                mConnectingFailed.setVisibility(View.GONE);
                mConnectingSuccess.setVisibility(View.GONE);

                mDetailsBtn.setVisibility(View.GONE);
                mFinishBtn.setVisibility(View.GONE);
            }

            @Override
            protected Void doInBackground(final Void... params) {
                publishProgress(getString(R.string.please_wait), "0");

                mProtocolList.clear();

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                String hubTitle = "Home";
                if (hubList.getHubCount() > 0) {
                    hubTitle = hubTitle + " " + (hubList.getHubCount() + 1);
                }

                // Prepare Hub item
                ColorGenerator generator = ColorGenerator.MATERIAL;
                TextDrawable tileDrawable = TextDrawable.builder().buildRound(hubTitle.substring(0, 1).toUpperCase(), generator.getColor(hubTitle));
                Bitmap tileBitmap = Util.drawableTileToBitmap(tileDrawable);
                String tileFile = Util.saveImage(getApplicationContext(), tileBitmap, "hub_" + hubTitle, "png");

                mHubItem = new HubItem(getApplicationContext(),
                        hubTitle,
                        getApplicationContext().getFilesDir().getPath() + "/" + tileFile,
                        mZWayHubConfiguration.getUsername(),
                        mZWayHubConfiguration.getPassword(),
                        mZWayHubConfiguration.getRemoteService(),
                        mZWayHubConfiguration.getRemoteId(),
                        mZWayHubConfiguration.getLocalIp(),
                        mZWayHubConfiguration.getHubSSID(),
                        mZWayHubConfiguration.getHubWifiPassword(),
                        mZWayHubConfiguration.getHubIP(),
                        mZWayHubConfiguration.getLocation(),
                        mZWayHubConfiguration.getLongitude(),
                        mZWayHubConfiguration.getLatitude(),
                        ""
                );

                if (hubList.existHubItem(mHubItem)) {
                    Log.i(Params.LOGGING_TAG, getString(R.string.intro_connecting_message_hub_already_exists));
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_hub_already_exists), "Intro"));

                    publishProgress(getString(R.string.intro_connecting_message_hub_already_exists), "-1");
                    return null;
                }

                // Store Hub item to get id.
                mHubItem = hubList.addHubItem(mHubItem);

                IZWayApi mZWayApi = null;

                IZWayApi zwayApiHubWifi = null;
                IZWayApi zwayApiRemoteAccess = null;
                IZWayApi zwayApiLocalAccess = null;

                if (mHubWifiConnected) {
                    Log.i(Params.LOGGING_TAG, "Check access via WiFi of the Hub: " + mZWayHubConfiguration.toString());
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_check_access_via_wifi), "Intro"));

                    // Hubs Wifi
                    zwayApiHubWifi = new ZWayApiAndroid(mZWayHubConfiguration.getLocalIp(), 8083, "http", mZWayHubConfiguration.getUsername(),
                            mZWayHubConfiguration.getPassword(), 0, false, (IZWayApiCallbacks) activity, getApplicationContext());

                    if (zwayApiHubWifi != null) {
                        Log.i(Params.LOGGING_TAG, "Access via Wifi of the Hub successfully established.");
                        addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_via_wifi_established), "Intro"));
                    }
                } else {
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_via_wifi_skip), "Intro"));
                }

                if (!mZWayHubConfiguration.getRemoteService().isEmpty()
                        && mZWayHubConfiguration.getRemoteId() != null
                        && mZWayHubConfiguration.getRemoteId() > 0) {
                    Log.i(Params.LOGGING_TAG, "Check access via remote service: " + mZWayHubConfiguration.toString());
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_check_access_via_remote), "Intro"));

                    // Remote access
                    zwayApiRemoteAccess = new ZWayApiAndroid(mZWayHubConfiguration.getRemoteService(), 443, "https",
                            mZWayHubConfiguration.getUsername(), mZWayHubConfiguration.getPassword(),
                            mZWayHubConfiguration.getRemoteId(), true, (IZWayApiCallbacks) activity, getApplicationContext());

                    if (zwayApiRemoteAccess != null) {
                        Log.i(Params.LOGGING_TAG, "Access via remote service successfully established.");
                        addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_via_remote_established), "Intro"));
                    }
                } else {
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_via_remote_skip), "Intro"));
                }

                if (!mZWayHubConfiguration.getLocalIp().isEmpty()) {
                    Log.i(Params.LOGGING_TAG, "Check connection via local access: " + mZWayHubConfiguration.toString());
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_check_access_via_local), "Intro"));

                    // Local access
                    zwayApiLocalAccess = new ZWayApiAndroid(mZWayHubConfiguration.getLocalIp(), 8083, "http",
                            mZWayHubConfiguration.getUsername(), mZWayHubConfiguration.getPassword(),
                            0, false, (IZWayApiCallbacks) activity, getApplicationContext());

                    if (zwayApiLocalAccess != null) {
                        Log.i(Params.LOGGING_TAG, "Connection via local access successfully established.");
                        addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_via_local_established), "Intro"));
                    }
                }  else {
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_via_local_skip), "Intro"));
                }

                // Choose the best connection for initialization
                if (zwayApiHubWifi != null) mZWayApi = zwayApiHubWifi;
                if (zwayApiRemoteAccess != null) mZWayApi = zwayApiRemoteAccess;
                if (zwayApiLocalAccess != null) mZWayApi = zwayApiLocalAccess;

                // Connection
                if (mZWayApi == null) {
                    Log.i(Params.LOGGING_TAG, "Connection to Hub has failed with all variants.");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_failed_with_all), "Intro"));

                    publishProgress(getString(R.string.intro_connecting_message_z_way_connection_failed), "-1");
                    return null;
                }

                Log.i(Params.LOGGING_TAG, "Connection to Hub successfully established at least with one variant.");
                addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_access_successful_at_least_with_one), "Intro"));

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Authentication
                if (mZWayApi.getLogin() == null) {
                    Log.i(Params.LOGGING_TAG, "Authentication failed!");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_z_way_authentication_failed), "Intro"));

                    publishProgress(getString(R.string.intro_connecting_message_z_way_authentication_failed), "-1");
                    return null;
                }

                Log.i(Params.LOGGING_TAG, "Authentication was successful.");
                addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_z_way_authentication_successful), "Intro"));

                publishProgress(getString(R.string.intro_connecting_message_z_way_authentication_successful), "0");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Current profile
                Profile currentProfile = mZWayApi.getCurrentProfile();
                if (currentProfile == null) {
                    Log.i(Params.LOGGING_TAG, "Loading of current profile failed");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_current_profile_failed), "Intro"));

                    publishProgress(getString(R.string.intro_connecting_message_current_profile_failed), "-1");
                    return null;
                }

                settings.edit().putInt(Params.PREFS_CURRENT_PROFILE_ID, currentProfile.getId()).apply();

                Log.i(Params.LOGGING_TAG, "Current profile successfully loaded");
                addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_current_profile_loaded), "Intro"));

                publishProgress(getString(R.string.intro_connecting_message_current_profile_loaded), "0");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Devices
                DeviceListApp deviceListApp = new DeviceListApp(getApplicationContext(), null, null);
                ArrayList<DeviceItemApp> deviceList = deviceListApp.loadAndSaveDeviceList(mHubItem.getId(), mZWayApi, false, false, -1, true);
                if (deviceList == null) {
                    Log.i(Params.LOGGING_TAG, "Loading of device data failed");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_device_data_failed), "Intro"));

                    publishProgress(getString(R.string.intro_connecting_message_device_data_failed), "-1");
                    return null;
                }

                Log.i(Params.LOGGING_TAG, "All device data successfully loaded");
                addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_device_data_loaded), "Intro"));

                publishProgress(getString(R.string.intro_connecting_message_device_data_loaded), "0");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Device History
                //DeviceHistoryListApp deviceHistoryListApp = new DeviceHistoryListApp(getApplicationContext(), null, null);
                //ArrayList<DeviceHistoryItemApp> deviceHistoryList = deviceHistoryListApp.loadAndSaveDeviceHistoryList(mHubItem.getId(), mZWayApi, true);
                // Don't check result, could be null if history deactivated!

                //if (deviceHistoryList == null) {
                //    Log.i(Params.LOGGING_TAG, "Loading of device history data failed");
                //    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_device_history_data_failed), "Intro"));
                //} else {
                //    Log.i(Params.LOGGING_TAG, "All device history data successfully loaded");
                //    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_device_history_data_loaded), "Intro"));
                //}

                //publishProgress(getString(R.string.intro_connecting_message_device_history_data_loaded), "0");
                //try {
                //    Thread.sleep(500);
                //} catch (InterruptedException e) {
                //    Log.e(Params.LOGGING_TAG, e.getMessage());
                //}

                // Icons
                IconListApp iconListApp = new IconListApp(getApplicationContext(), null, null);
                ArrayList<IconItemApp> iconList = iconListApp.loadAndSaveIconList(mHubItem.getId(), mZWayApi, true);
                // Don't check result, could be null if history deactivated!

                if (iconList == null) {
                    Log.i(Params.LOGGING_TAG, "Loading of icons failed");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_icon_data_failed), "Intro"));
                } else {
                    Log.i(Params.LOGGING_TAG, "All icons successfully loaded");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_icon_data_loaded), "Intro"));
                }

                publishProgress(getString(R.string.intro_connecting_message_icon_data_loaded), "0");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Locations
                LocationListApp locationListApp = new LocationListApp(getApplicationContext(), null, null);
                ArrayList<LocationItemApp> locationList = locationListApp.loadAndSaveLocationList(mHubItem.getId(), mZWayApi, true);
                if (locationList == null) {
                    Log.i(Params.LOGGING_TAG, "Loading of location data failed");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_location_data_failed), "Intro"));

                    publishProgress(getString(R.string.intro_connecting_message_location_data_failed), "-1");
                }

                Log.i(Params.LOGGING_TAG, "All location data successfully loaded");
                addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_location_data_loaded), "Intro"));

                publishProgress(getString(R.string.intro_connecting_message_location_data_loaded), "0");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Notifications
                NotificationListApp notificationListApp = new NotificationListApp(getApplicationContext(), null, null);
                ArrayList<NotificationItemApp> notificationList = notificationListApp.loadAndSaveNotificationList(mHubItem.getId(), mZWayApi, false, true);
                // Don't check result, could be null if data is too large!

                if (notificationList == null) {
                    Log.i(Params.LOGGING_TAG, "Loading of notification data failed");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_notification_data_failed), "Intro"));
                } else {
                    Log.i(Params.LOGGING_TAG, "All notification data successfully loaded");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_notification_data_loaded), "Intro"));
                }

                publishProgress(getString(R.string.intro_connecting_message_notification_data_loaded), "0");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Profiles
                ProfileListApp profileListApp = new ProfileListApp(getApplicationContext(), null, null);
                ArrayList<ProfileItemApp> profileList = profileListApp.loadAndSaveProfileList(mHubItem.getId(), mZWayApi, true);
                if (profileList == null) {
                    Log.i(Params.LOGGING_TAG, "Loading of profile data failed");
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_profile_data_failed), "Intro"));

                    publishProgress(getString(R.string.intro_connecting_message_profile_data_failed), "-1");
                }

                Log.i(Params.LOGGING_TAG, "All profile data successfully loaded");
                addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_connecting_message_profile_data_loaded), "Intro"));

                publishProgress(getString(R.string.intro_connecting_message_profile_data_loaded), "0");
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                }

                // Register at Z-Way app
                if (!checkPlayServices()) {
                    return null;
                }

                Log.i(Params.LOGGING_TAG, "Setting up the Z-Way app needed for the notifications");
                addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_setting_up_z_way_app), "Intro"));
                try {
                    FirebaseInstanceId instanceID = FirebaseInstanceId.getInstance();
                    String token = instanceID.getToken();

                    if (token != null) {
                        Log.i(Params.LOGGING_TAG, "FCM registration token: " + token);

                        // Subscribe to topic channels
                        FirebaseMessaging.getInstance().subscribeToTopic("global");

                        Map<String, String> commandParams = new HashMap<>();
                        commandParams.put("token", token);
                        commandParams.put("hubId", mHubItem.getId().toString());
                        commandParams.put("title", Build.MODEL);
                        commandParams.put("os", "android");

                        DeviceCommand deviceCommand = new DeviceCommand("MobileAppSupport", "registerApp", commandParams);
                        String message = mZWayApi.getDeviceCommand(deviceCommand);
                        if (message != null) {
                            Log.i(Params.LOGGING_TAG, "Sending the register command to the app resulted in: " + message);

                            publishProgress(getString(R.string.intro_connecting_message_app_registration_successfully), "0");

                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e) {
                                Log.e(Params.LOGGING_TAG, e.getMessage());
                            }

                            publishProgress(getString(R.string.intro_connecting_message_initialize_hub_successfully), "1");
                            mConnectingComplete = true; // Enable finish button!
                        } else {
                            Log.i(Params.LOGGING_TAG, "Sending the register command to the app resulted in an internal Z-Way error");
                            addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_setting_up_z_way_app_internal_error), "Intro"));

                            Log.i(Params.LOGGING_TAG, "Message is null");
                            publishProgress(getString(R.string.intro_connecting_message_app_registration_failed), "-1");
                        }
                    } else {
                        Log.i(Params.LOGGING_TAG, "Firebase Cloud Message unavailable");
                        addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_setting_up_z_way_firebase_unavailable), "Intro"));

                        Log.i(Params.LOGGING_TAG, "Token is null");
                        publishProgress(getString(R.string.intro_connecting_message_app_registration_failed), "-1");
                    }
                } catch (Exception e) {
                    Log.d(Params.LOGGING_TAG, "Failed to register app to the Hub", e);

                    Log.i(Params.LOGGING_TAG, "Unexpected error occurred during register app in Z-Way: " + e.getMessage());
                    addProtocolItem(new ProtocolItem(getApplicationContext(), ProtocolType.INFO, getString(R.string.intro_setting_up_z_way_unexpected_error, e.getMessage()), "Intro"));
                }

                return null;
            }

            protected void onProgressUpdate(String... progress) {
                if (Integer.valueOf(progress[1]).equals(-1)) {
                    rollbackInitialization();
                }
                updateConnectingProgress(progress[0], Integer.valueOf(progress[1]));
            }

            @Override
            protected void onPostExecute(Void result) {
            }
        }.execute();
    }

    private void rollbackInitialization() {
        if (mHubItem != null && mHubItem.getId() != -1) {
            new HubList(this).deleteHubItem(mHubItem);
        }
        mHubItem = null;
    }

    private void updateConnectingProgress(String message, Integer status) {
        mConnectingProgressMessageTxt.setText(message);

        if (status.equals(-1)) {
            mConnectingProgress.setVisibility(View.GONE);
            mConnectingFailed.setVisibility(View.VISIBLE);

            mDetailsBtn.setVisibility(View.VISIBLE);
        } else if (status.equals(1)) {
            mConnectingComplete = true;

            mConnectingProgress.setVisibility(View.GONE);
            mConnectingSuccess.setVisibility(View.VISIBLE);

            mFinishBtn.setVisibility(View.VISIBLE);
        }
    }

    public void addProtocolItem(ProtocolItem protocolItem) {
        mProtocolList.add(protocolItem);
        Util.addProtocol(this, protocolItem);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                mToastHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Google Play Services API unavailable!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void getStatusResponse(String s) {

    }

    @Override
    public void getRestartResponse(Boolean aBoolean) {

    }

    @Override
    public void getLoginResponse(String s) {
        Log.d(Params.LOGGING_TAG, "(Intro) Authentication successful: " + s);
        addProtocolItem(new ProtocolItem(this, ProtocolType.INFO, getString(R.string.z_way_message_authentication_success), "Z-Way"));
    }

    @Override
    public void getNamespacesResponse(NamespaceList namespaceList) {

    }

    @Override
    public void getModulesResponse(ModuleList moduleList) {

    }

    @Override
    public void getInstancesResponse(InstanceList instanceList) {

    }

    @Override
    public void postInstanceResponse(Instance instance) {

    }

    @Override
    public void getInstanceResponse(Instance instance) {

    }

    @Override
    public void putInstanceResponse(Instance instance) {

    }

    @Override
    public void deleteInstanceResponse(boolean b) {

    }

    @Override
    public void getDevicesResponse(DeviceList deviceList) {

    }

    @Override
    public void putDeviceResponse(Device device) {

    }

    @Override
    public void getDeviceResponse(Device device) {

    }

    @Override
    public void getDeviceCommandResponse(String s) {

    }

    @Override
    public void getLocationsResponse(LocationList locationList) {

    }

    @Override
    public void postLocationResponse(Location location) {

    }

    @Override
    public void getLocationResponse(Location location) {

    }

    @Override
    public void putLocationResponse(Location location) {

    }

    @Override
    public void deleteLocationResponse(boolean b) {

    }

    @Override
    public void getProfilesResponse(ProfileList profileList) {

    }

    @Override
    public void postProfileResponse(Profile profile) {

    }

    @Override
    public void getProfileResponse(Profile profile) {

    }

    @Override
    public void putProfileResponse(Profile profile) {

    }

    @Override
    public void deleteProfileResponse(boolean b) {

    }

    @Override
    public void getNotificationsResponse(NotificationList notificationList) {

    }

    @Override
    public void getNotificationResponse(Notification notification) {

    }

    @Override
    public void putNotificationResponse(Notification notification) {

    }

    @Override
    public void getDeviceHistoriesResponse(DeviceHistoryList deviceHistoryList) {

    }

    @Override
    public void getDeviceHistoryResponse(DeviceHistory deviceHistory) {

    }

    @Override
    public void getZWaveDeviceResponse(ZWaveDevice zWaveDevice) {

    }

    @Override
    public void getZWaveControllerResponse(ZWaveController zWaveController) {

    }

    @Override
    public void apiError(String s, boolean b) {
        Log.e(Params.LOGGING_TAG, "(Intro) Z-Way API error: " + s);
        addProtocolItem(new ProtocolItem(this, ProtocolType.ERROR, getString(R.string.z_way_message_api_error) + " " + s, "Z-Way"));
    }

    @Override
    public void httpStatusError(int i, String s, boolean b) {
        Log.e(Params.LOGGING_TAG, "(Intro) Z-Way API error (HTTP status): " + s + "(" + String.valueOf(i) + ")");
        addProtocolItem(new ProtocolItem(this, ProtocolType.ERROR, getString(R.string.z_way_message_api_error_http_status) + " " + s + "(" + String.valueOf(i)+ ")", "Z-Way"));
    }

    @Override
    public void authenticationError() {
        Log.e(Params.LOGGING_TAG, "(Intro) Z-Way API error (Authentication)");
        addProtocolItem(new ProtocolItem(this, ProtocolType.ERROR, getString(R.string.z_way_message_api_error_authentication), "Z-Way"));
    }

    @Override
    public void responseFormatError(String s, boolean b) {
        Log.e(Params.LOGGING_TAG, "(Intro) Z-Way API error (Server response): " + s);
        addProtocolItem(new ProtocolItem(this, ProtocolType.ERROR, getString(R.string.z_way_message_api_error_authentication), "Z-Way"));
    }

    @Override
    public void message(int i, String s) {
        Log.i(Params.LOGGING_TAG, "(Intro) Z-Way API message: " + s);
    }
}
