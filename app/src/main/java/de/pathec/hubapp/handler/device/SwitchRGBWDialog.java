package de.pathec.hubapp.handler.device;


import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;

import de.fh_zwickau.informatik.sensor.model.devices.Color;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.util.Util;
import me.priyesh.chroma.ChromaDialog;
import me.priyesh.chroma.ColorMode;
import me.priyesh.chroma.ColorSelectListener;

class SwitchRGBWDialog {

    private Activity mActivity;
    private OnRGBWSwitchDDialogInteractionListener mListener;
    private DeviceItemApp mDeviceItem;

    SwitchRGBWDialog(Activity activity, OnRGBWSwitchDDialogInteractionListener listener, DeviceItemApp deviceItem) {
        mActivity = activity;
        mListener = listener;
        mDeviceItem = deviceItem;

    }

    private void changeLevel(Color color, Boolean callback) {
        /**
         * Update device
         */
        mDeviceItem.getMetrics().setColor(color);

        /**
         * Notify listener
         */
        if (callback) {
            mListener.onSwitchRGBWChanged(mDeviceItem);
        }
    }

    void showSwitchRGBWDialog() {
        new ChromaDialog.Builder()
                .initialColor(Util.getIntFromColor(
                    mDeviceItem.getMetrics().getColor().getRed(),
                    mDeviceItem.getMetrics().getColor().getGreen(),
                    mDeviceItem.getMetrics().getColor().getBlue())
                )
                .colorMode(ColorMode.RGB) // There's also ARGB and HSV
                .onColorSelected(new ColorSelectListener() {
                    @Override
                    public void onColorSelected(@ColorInt int intColor) {
                        Color color = new Color();
                        color.setRed(android.graphics.Color.red(intColor));
                        color.setGreen(android.graphics.Color.green(intColor));
                        color.setBlue(android.graphics.Color.blue(intColor));

                        changeLevel(color, true);
                    }
                })
                .create()
                .show(((AppCompatActivity)mActivity).getSupportFragmentManager(), "ChromaDialog");
    }

    interface OnRGBWSwitchDDialogInteractionListener {
        void onSwitchRGBWChanged(DeviceItemApp deviceItem);
    }
}
