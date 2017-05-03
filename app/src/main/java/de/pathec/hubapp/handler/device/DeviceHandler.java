package de.pathec.hubapp.handler.device;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.fh_zwickau.informatik.sensor.model.devices.DeviceCommand;
import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.util.HubConnectionHolder;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.ZWayUtil;

import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_BATTERY;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_CAMERA;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_DOORLOCK;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SENSOR_BINARY;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SENSOR_DISCRETE;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SENSOR_MULTILEVEL;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SENSOR_MULTILINE;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SWITCH_BINARY;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SWITCH_CONTROL;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SWITCH_MULTILEVEL;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SWITCH_RGBW;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_SWITCH_TOGGLE;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_TEXT;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_THERMOSTAT;
import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_TOGGLE_BUTTON;

public class DeviceHandler implements SwitchMultilevelDialog.OnSwitchMultilevelDialogInteractionListener,
        SwitchRGBWDialog.OnRGBWSwitchDDialogInteractionListener {

    private Activity mActivity;
    private IMainActivityCommunicator mActivityCommunicator;
    private DeviceHandlerInteractionListener mListener;

    private NumberFormat mFormatter = DecimalFormat.getInstance(Locale.getDefault());

    public DeviceHandler(Activity activity, IMainActivityCommunicator activityCommunicator,
                         DeviceHandlerInteractionListener listener) {
        mActivity = activity;
        mActivityCommunicator = activityCommunicator;
        mListener = listener;

        mFormatter.setMaximumFractionDigits(2);
    }

    private HubConnectionHolder getHubConnectionHolder() {
        HubConnectionHolder hubConnectionHolder = mActivityCommunicator.getActiveHubConnectionHolder();
        return hubConnectionHolder != null ? hubConnectionHolder : null;
    }

    private void deviceCommand(final DeviceItemApp deviceItem, final DeviceCommand deviceCommand) {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            return;
        }

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                // start loading for example ...
                mListener.onDeviceHandlerStartAction(deviceItem);
            }

            @Override
            protected String doInBackground(Void... params) {
                // perform http request
                return hubConnectionHolder.getDeviceCommand(deviceCommand);
            }

            @Override
            protected void onPostExecute(String result) {
                // if request was successfully, update device instance by command information
                if (result != null) {
                    // update level of device if command successfully performed
                    switch (deviceItem.getDeviceType()) {
                        case DEVICE_TYPE_BATTERY:
                            break;
                        case DEVICE_TYPE_DOORLOCK:
                            deviceItem.getMetrics().setLevel(deviceCommand.getCommand());
                            break;
                        case DEVICE_TYPE_THERMOSTAT:
                            deviceItem.getMetrics().setLevel(deviceCommand.getParams().get("level"));
                            break;
                        case DEVICE_TYPE_SENSOR_BINARY:
                            break;
                        case DEVICE_TYPE_SENSOR_MULTILEVEL:
                            break;
                        case DEVICE_TYPE_SWITCH_BINARY:
                            deviceItem.getMetrics().setLevel(deviceCommand.getCommand());
                            break;
                        case DEVICE_TYPE_SWITCH_MULTILEVEL:
                            deviceItem.getMetrics().setLevel(deviceCommand.getParams().get("level"));
                            break;
                        case DEVICE_TYPE_SWITCH_TOGGLE:
                            deviceItem.getMetrics().setLevel(deviceCommand.getCommand());
                            break;
                        case DEVICE_TYPE_SWITCH_CONTROL:
                            deviceItem.getMetrics().setLevel(deviceCommand.getCommand());
                            break;
                        case DEVICE_TYPE_TOGGLE_BUTTON:
                            deviceItem.getMetrics().setLevel(deviceCommand.getCommand());
                            break;
                        case DEVICE_TYPE_SWITCH_RGBW:
                            Map<String, String> params = deviceCommand.getParams();
                            deviceItem.getMetrics().getColor().setRed(Integer.parseInt(params.get("red")));
                            deviceItem.getMetrics().getColor().setGreen(Integer.parseInt(params.get("green")));
                            deviceItem.getMetrics().getColor().setBlue(Integer.parseInt(params.get("blue")));
                            break;
                        case DEVICE_TYPE_TEXT:
                            break;
                        case DEVICE_TYPE_SENSOR_DISCRETE:
                            break;
                        case DEVICE_TYPE_SENSOR_MULTILINE:
                            break;
                    }

                    // update time
                    deviceItem.setUpdateTime((int) (new Date().getTime() / 1000));

                    // notify fragment (-> recycler view -> adapter)
                    mListener.onDeviceHandlerFinishAction(deviceItem, result);
                } else {
                    // if command wasn't successfully
                    mListener.onDeviceHandlerFinishAction(deviceItem, result);
                    Util.showMessage(mActivity, mActivity.getString(R.string.connection_not_connected));
                }
            }
        }.execute();
    }

    public void onDeviceAction(DeviceItemApp deviceItem) {
        onDeviceAction(deviceItem, "");
    }

    public void onDeviceAction(DeviceItemApp deviceItem, String action) {
        switch (deviceItem.getDeviceType()) {
            case DEVICE_TYPE_BATTERY:
                break;
            case DEVICE_TYPE_DOORLOCK:
                handleDoorlockAction(deviceItem, action);
                return;
            case DEVICE_TYPE_THERMOSTAT:
                handleSwitchMultilevelAction(deviceItem);
                return;
            case DEVICE_TYPE_SENSOR_BINARY:
                break;
            case DEVICE_TYPE_SENSOR_MULTILEVEL:
                break;
            case DEVICE_TYPE_SWITCH_BINARY:
                handleSwitchBinaryAction(deviceItem, false, action);
                return;
            case DEVICE_TYPE_SWITCH_MULTILEVEL:
                handleSwitchMultilevelAction(deviceItem);
                return;
            case DEVICE_TYPE_SWITCH_TOGGLE:
                handleSwitchBinaryAction(deviceItem, true, action);
                return;
            case DEVICE_TYPE_SWITCH_CONTROL:
                handleSwitchBinaryAction(deviceItem, false, action);
                return;
            case DEVICE_TYPE_TOGGLE_BUTTON:
                handleSwitchBinaryAction(deviceItem, true, action);
                return;
            case DEVICE_TYPE_SWITCH_RGBW:
                handleSwitchRGBAction(deviceItem);
                return;
            case DEVICE_TYPE_TEXT:
                handleTextAction(deviceItem);
                return;
            case DEVICE_TYPE_CAMERA:
                //handleCameraAction(deviceItem);
                //return;
                break;
            case DEVICE_TYPE_SENSOR_DISCRETE:
                break;
            case DEVICE_TYPE_SENSOR_MULTILINE:
                handleMultilineAction(deviceItem);
                return;
        }

        Util.showMessage(mActivity, mActivity.getString(R.string.device_no_action, ZWayUtil.getDeviceTypeLabelByIdentifier(mActivity, deviceItem.getDeviceType())));
    }

    /**
     * Handle command for binary switches.
     *
     * @param deviceItem device instance
     * @param onlyOn if only on command is defined
     * @param action on or off, if empty - use the alternating of current level
     */
    private void handleSwitchBinaryAction(DeviceItemApp deviceItem, Boolean onlyOn, String action) {
        // starting device command to Z-Way
        if (onlyOn) {
            DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), "on");
            deviceCommand(deviceItem, deviceCommand);
        } else if (action.isEmpty()) {
            String oldLevel = deviceItem.getMetrics().getLevel();
            DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), oldLevel.equals("off") ? "on" : "off");
            deviceCommand(deviceItem, deviceCommand);
        } else {
            DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), action);
            deviceCommand(deviceItem, deviceCommand);
        }
    }

    /**
     * Handle command for doorlock devices.
     *
     * @param deviceItem device instance
     * @param action open or close, if empty - use the alternating of current level
     */
    private void handleDoorlockAction(DeviceItemApp deviceItem, String action) {
        // starting device command to Z-Way
        if (action.isEmpty()) {
            String oldLevel = deviceItem.getMetrics().getLevel();
            DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), oldLevel.equals("close") ? "open" : "close");
            deviceCommand(deviceItem, deviceCommand);
        } else {
            DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), action.equals("on") ? "open" : "close");
            deviceCommand(deviceItem, deviceCommand);
        }
    }

    /**
     * Handle command for switch multilevel devices by showing multilevel dialog.
     *
     * @param deviceItem device instance
     */
    private void handleSwitchMultilevelAction(DeviceItemApp deviceItem) {
        SwitchMultilevelDialog switchMultilevelDialog = new SwitchMultilevelDialog(mActivity, this, deviceItem);
        switchMultilevelDialog.showSwitchMultilevelDialog();
    }

    /**
     * Handle command for switch rgb devices by showing color picker dialog.
     *
     * @param deviceItem device instance
     */
    private void handleSwitchRGBAction(DeviceItemApp deviceItem) {
        SwitchRGBWDialog switchRGBDialog = new SwitchRGBWDialog(mActivity, this, deviceItem);
        switchRGBDialog.showSwitchRGBWDialog();
    }

    /**
     * Handle command for text devices by showing text dialog.
     *
     * @param deviceItem device instance
     */
    private void handleTextAction(DeviceItemApp deviceItem) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity, R.style.MaterialBaseTheme_Light_Dialog);
        alert.setTitle(deviceItem.getMetrics().getTitle());
        alert.setMessage(Util.fromHtml(deviceItem.getMetrics().getText()));
        alert.setPositiveButton(mActivity.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    /**
     * Handle command for multiline devices by showing dialog depending on multiline type.
     *
     * @param deviceItem device instance
     */
    private void handleMultilineAction(final DeviceItemApp deviceItem) {
        final HubConnectionHolder hubConnectionHolder = getHubConnectionHolder();
        if (hubConnectionHolder == null) {
            Util.showMessage(mActivity, mActivity.getString(R.string.unexpected_error));
            return;
        }

        new AsyncTask<Void, Void, String>() {

            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                dialog = new ProgressDialog(mActivity);
                dialog.setMessage(mActivity.getString(R.string.please_wait));
                dialog.setIndeterminate(true);
                dialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                return hubConnectionHolder.getDeviceAsJson(deviceItem.getDeviceId());
            }

            @Override
            protected void onPostExecute(String result) {
                dialog.dismiss();
                dialog = null;

                if (result != null) {
                    try {
                        Gson gson = new Gson();
                        // Response -> String -> Json -> extract data field
                        JsonObject deviceAsJson = gson.fromJson(result, JsonObject.class).get("data").getAsJsonObject();
                        String multilineType = deviceAsJson.get("metrics").getAsJsonObject().get("multilineType").getAsString();

                        String multilineText = deserializeMultilineDeviceType(multilineType, deviceAsJson);
                        if (!multilineText.equals("")) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(mActivity, R.style.MaterialBaseTheme_Light_Dialog);
                            alert.setTitle(deviceItem.getMetrics().getTitle());
                            alert.setMessage(Util.fromHtml(multilineText));
                            alert.setPositiveButton(mActivity.getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            alert.show();
                        }
                    } catch (JsonParseException e) {
                        Util.showMessage(mActivity, mActivity.getString(R.string.unexpected_error));
                    }
                } else {
                    Util.showMessage(mActivity, mActivity.getString(R.string.unexpected_error));
                }
            }
        }.execute();
    }

    private String deserializeMultilineDeviceType(String multilineType, JsonObject deviceAsJson) {
        switch (multilineType) {
            case "openWeather":
                try {
                    JsonObject metrics = deviceAsJson.get("metrics").getAsJsonObject();
                    // pressure
                    String pressure = metrics.get("zwaveOpenWeather").getAsJsonObject().get("main").getAsJsonObject().get("pressure").getAsString();

                    // humidity
                    Double doubleHumidity = Double.parseDouble(metrics.get("zwaveOpenWeather").getAsJsonObject().get("main").getAsJsonObject().get("humidity").getAsString());
                    String humidity = mFormatter.format(doubleHumidity);

                    // weather
                    Integer weatherItem = 0;
                    String weather = "";
                    for (JsonElement jsonElement :  metrics.get("zwaveOpenWeather").getAsJsonObject().get("weather").getAsJsonArray()) {
                        if (weatherItem > 0) {
                            weather += ", ";
                        }

                        weatherItem ++;
                        weather += ZWayUtil.getOpenWeatherDescriptionById(mActivity, jsonElement.getAsJsonObject().get("id").getAsInt());
                    }

                    // wind
                    Double doubleWind = Double.parseDouble(metrics.get("zwaveOpenWeather").getAsJsonObject().get("wind").getAsJsonObject().get("speed").getAsString());
                    String wind = mFormatter.format(doubleWind);

                    // last update
                    Long lastUpdate = deviceAsJson.get("updateTime").getAsLong();


                    return "<strong>" + mActivity.getString(R.string.device_open_weather_humidity) + ":</strong> " + humidity + " %<br><br>"
                            + "<strong>" + mActivity.getString(R.string.device_open_weather_pressure) + ":</strong> " + pressure + " hPa<br><br>"
                            + "<strong>" + mActivity.getString(R.string.device_open_weather_weather) + ":</strong> " + weather + "<br><br>"
                            + "<strong>" + mActivity.getString(R.string.device_open_weather_wind) + ":</strong> " + wind + " m/s<br><br>"
                            + "<strong>" + mActivity.getString(R.string.device_open_weather_last_update) + ":</strong> " + Params.formatTimeShort(new Date(lastUpdate*1000), false);
                } catch (Exception e) {
                    Log.e(Params.LOGGING_TAG, e.getMessage());
                    Util.showMessage(mActivity, mActivity.getString(R.string.unexpected_error));
                }

                return "";
            default:
                Util.showMessage(mActivity, mActivity.getString(R.string.device_sensor_multiline_unknown));
                return "";
        }
    }

    /**
     * Handle command for camera devices by delegating command to caller.
     *
     * @param deviceItem device instance
     */
    private void handleCameraAction(DeviceItemApp deviceItem) {
        mListener.onDeviceHandlerShowVideoFragment(deviceItem.getDeviceId());
    }

    /**
     * Handle result of multilevel switch dialog.
     *
     * @param deviceItem device instance with new level
     */
    @Override
    public void onSwitchMultilevelChanged(DeviceItemApp deviceItem) {
        HashMap<String, String> params = new HashMap<>();
        params.put("level", deviceItem.getMetrics().getLevel());
        DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), "exact", params);
        deviceCommand(deviceItem, deviceCommand);
    }

    /**
     * Handle result of color picker dialog.
     *
     * @param deviceItem device instance with new color information
     */
    @Override
    public void onSwitchRGBWChanged(DeviceItemApp deviceItem) {
        // check level first (on/off)
        if (deviceItem.getMetrics().getLevel().toLowerCase().equals("off")) {
            DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), "on");
            deviceCommand(deviceItem, deviceCommand);
        }

        // update color
        HashMap<String, String> params = new HashMap<>();
        params.put("red", deviceItem.getMetrics().getColor().getRed().toString());
        params.put("green", deviceItem.getMetrics().getColor().getGreen().toString());
        params.put("blue", deviceItem.getMetrics().getColor().getBlue().toString());
        DeviceCommand deviceCommand = new DeviceCommand(deviceItem.getDeviceId(), "exact", params);
        deviceCommand(deviceItem, deviceCommand);
    }

    public interface DeviceHandlerInteractionListener {
        void onDeviceHandlerStartAction(DeviceItemApp deviceItem);
        void onDeviceHandlerFinishAction(DeviceItemApp deviceItem, String result);

        void onDeviceHandlerShowVideoFragment(String deviceId);
    }
}
