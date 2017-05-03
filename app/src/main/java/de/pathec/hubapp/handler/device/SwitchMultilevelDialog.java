package de.pathec.hubapp.handler.device;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import de.pathec.hubapp.R;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.util.Params;

import static de.fh_zwickau.informatik.sensor.ZWayConstants.DEVICE_TYPE_THERMOSTAT;

class SwitchMultilevelDialog {

    private NumberFormat mFormatter;
    private Activity mActivity;
    private OnSwitchMultilevelDialogInteractionListener mListener;
    private DeviceItemApp mDeviceItem;

    private TextView mLevelTxt, mScaleTxt;
    private SeekBar mLevelSlider;

    private Double mStep = 1.0;
    private Double mMaximum = 99.0;
    private Double mMinimum = 0.0;

    SwitchMultilevelDialog(Activity activity, OnSwitchMultilevelDialogInteractionListener listener, DeviceItemApp deviceItem) {
        mFormatter = DecimalFormat.getInstance(Locale.getDefault());
        mFormatter.setMaximumFractionDigits(1);

        mActivity = activity;
        mListener = listener;
        mDeviceItem = deviceItem;

        // Thermostat steps
        if (deviceItem.getDeviceType().equals(DEVICE_TYPE_THERMOSTAT)) {
            mStep = 0.5;
        }

        if (!compareDouble(mStep, 1.0)) {
            mFormatter.setMinimumFractionDigits(1);
        }

        // Minimum
        mMinimum = Double.valueOf(deviceItem.getMetrics().getMin());
        // Maximum
        if (!deviceItem.getMetrics().getMax().equals(0)) {
            mMaximum = Double.valueOf(deviceItem.getMetrics().getMax());
        }
    }

    private void changeLevel(String level, Boolean fromButton, Boolean callback) {
        /**
         * Update level text view
         */
        try {
            Double doubleLevel = Double.parseDouble(level.replace(",", "."));
            mLevelTxt.setText(mFormatter.format(doubleLevel));
        } catch (NumberFormatException nfe) {
            Log.e(Params.LOGGING_TAG, nfe.getMessage());
        }

        /**
         * Update slider
         */
        if (fromButton) {
            try {
                Double doubleLevel = Double.parseDouble(level.replace(",", "."));
                doubleLevel -= mMinimum;
                if (mStep.equals(0.5)) {
                    doubleLevel = doubleLevel / 0.5;
                    mLevelSlider.setProgress(doubleLevel.intValue());
                } else {
                    mLevelSlider.setProgress(doubleLevel.intValue());
                }
            } catch (NumberFormatException nfe) {
                Log.e(Params.LOGGING_TAG, "Update slider failed: " + nfe.getMessage());
            }
        }

        /**
         * Update device
         */
        mDeviceItem.getMetrics().setLevel(level);

        /**
         * Notify listener
         */
        if (callback) {
            // Pass a copy of device object!
            mListener.onSwitchMultilevelChanged(new DeviceItemApp(mDeviceItem));
        }
    }

    void showSwitchMultilevelDialog() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View switchMultilevelDialog = inflater.inflate(R.layout.dialog_switch_multilevel, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity, R.style.MaterialBaseTheme_Light_Dialog);
        alert.setView(switchMultilevelDialog);

        alert.setTitle(mDeviceItem.getMetrics().getTitle());

        /**
         * Views
         */
        mLevelTxt = (TextView) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_level);
        mScaleTxt = (TextView) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_scale);
        mLevelSlider = (SeekBar) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_level_slider);
        mLevelSlider.setMax(Double.valueOf((mMaximum - mMinimum) / mStep).intValue());

        if (!mDeviceItem.getMetrics().getScaleTitle().isEmpty()) {
            mScaleTxt.setText(mDeviceItem.getMetrics().getScaleTitle());
        }

        mLevelSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(Params.LOGGING_TAG, "Slider change");
                if (fromUser)
                    try {
                        mLevelTxt.setText(mFormatter.format(mMinimum + (progress * mStep)));
                    } catch (NumberFormatException nfe) {
                        Log.e(Params.LOGGING_TAG, nfe.getMessage());
                    }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(Params.LOGGING_TAG, "Slider stop tracking");
                changeLevel(String.valueOf(mMinimum + (seekBar.getProgress() * mStep)), false, true);
            }
        });

        /**
         * Initialize views
         */
        String level = mDeviceItem.getMetrics().getLevel();
        try {
            Double doubleLevel = Double.parseDouble(mDeviceItem.getMetrics().getLevel());

            level = mFormatter.format(doubleLevel);
        } catch (NumberFormatException nfe) {
            Log.e(Params.LOGGING_TAG, nfe.getMessage());
        }
        changeLevel(level, true, false);

        // Buttons
        Button upBtn = (Button) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_level_up);
        Button up5Btn = (Button) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_level_up_5);
        Button downBtn = (Button) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_level_down);
        Button down5Btn = (Button) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_level_down_5);
        Button fullBtn = (Button) switchMultilevelDialog.findViewById(R.id.dialog_switch_multilevel_level_full);

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double currentLevel = Double.parseDouble(mLevelTxt.getText().toString().replace(",", "."));
                    Log.d(Params.LOGGING_TAG, "Value up by " + mStep + " and current value " + currentLevel);
                    // Current level already maximum?
                    if (!compareDouble(currentLevel, mMaximum)) {
                        Log.d(Params.LOGGING_TAG, "Currently not maximum");
                        // New level more then maximum? -> set to maximum
                        if (currentLevel + mStep > mMaximum) {
                            Log.d(Params.LOGGING_TAG, "After up value more than maximum");
                            currentLevel = mMaximum;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        } else {
                            // Default handling
                            currentLevel += mStep;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        }
                    } // Already maximum
                } catch (NumberFormatException nfe) {
                    Log.e(Params.LOGGING_TAG, "Update level failed: " + nfe.getMessage());
                }
            }
        });
        up5Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double currentLevel = Double.parseDouble(mLevelTxt.getText().toString().replace(",", "."));
                    // Current level already maximum?
                    if (!compareDouble(currentLevel, mMaximum)) {
                        // New level more then maximum? -> set to maximum
                        if (currentLevel + 5.0 > mMaximum) {
                            currentLevel = mMaximum;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        } else {
                            // Default handling
                            currentLevel += 5.0;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        }
                    } // Already maximum
                } catch (NumberFormatException nfe) {
                    Log.e(Params.LOGGING_TAG, "Update level failed: " + nfe.getMessage());
                }
            }
        });
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double currentLevel = Double.parseDouble(mLevelTxt.getText().toString().replace(",", "."));
                    // Current level already minimum?
                    if (!compareDouble(currentLevel, mMinimum)) {
                        // New level less then minimum? -> set to minimum
                        if (currentLevel - mStep < mMinimum) {
                            currentLevel = mMinimum;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        } else {
                            // Default handling
                            currentLevel -= mStep;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        }
                    } // Already maximum
                } catch (NumberFormatException nfe) {
                    Log.e(Params.LOGGING_TAG, "Update level failed: " + nfe.getMessage());
                }
            }
        });
        down5Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double currentLevel = Double.parseDouble(mLevelTxt.getText().toString().replace(",", "."));
                    // Current level already minimum?
                    if (!compareDouble(currentLevel, mMinimum)) {
                        // New level less then minimum? -> set to minimum
                        if (currentLevel - 5.0 < mMinimum) {
                            currentLevel = mMinimum;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        } else {
                            // Default handling
                            currentLevel -= 5.0;
                            changeLevel(String.valueOf(currentLevel), true, true);
                        }
                    } // Already maximum
                } catch (NumberFormatException nfe) {
                    Log.e(Params.LOGGING_TAG, "Update level failed: " + nfe.getMessage());
                }
            }
        });
        fullBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changeLevel(String.valueOf(mMaximum), true, true);
            }
        });

        alert.setPositiveButton(mActivity.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private Boolean compareDouble(Double d1, Double d2) {
        return Math.abs(d1 - d2) < 0.000001;
    }

    interface OnSwitchMultilevelDialogInteractionListener {
        void onSwitchMultilevelChanged(DeviceItemApp deviceItem);
    }
}
