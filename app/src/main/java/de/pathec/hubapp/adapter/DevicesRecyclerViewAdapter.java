package de.pathec.hubapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.fh_zwickau.informatik.sensor.model.devices.Icons;
import de.pathec.hubapp.R;
import de.pathec.hubapp.model.device.DeviceItemApp;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.Util;
import de.pathec.hubapp.util.recycler_view_helper.ItemTouchHelperAdapter;
import de.pathec.hubapp.util.recycler_view_helper.ItemTouchHelperViewHolder;
import de.pathec.hubapp.util.recycler_view_helper.OnStartDragListener;
import mehdi.sakout.fancybuttons.FancyButton;

import static de.fh_zwickau.informatik.sensor.ZWayConstants.*;

public class DevicesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements ItemTouchHelperAdapter {

    private final Context mContext;

    private final List<DevicesItemView> mValues;
    private final OnDevicesAdapterInteractionListener mListener;
    private final OnStartDragListener mDragStartListener;

    private NumberFormat mFormatter = DecimalFormat.getInstance(Locale.getDefault());

    private Boolean mWithLocation;

    private final int BINARY_ITEM = 0, MULTILEVEL_ITEM = 1, RGB_ITEM = 2, DISCRETE_ITEM = 3;

    public DevicesRecyclerViewAdapter(Context context, List<DevicesItemView> items,
                                      OnDevicesAdapterInteractionListener listener,
                                      OnStartDragListener dragStartListener,
                                      Boolean withLocation) {
        mContext = context;
        mValues = items;
        mListener = listener;
        mDragStartListener = dragStartListener;
        mFormatter.setMaximumFractionDigits(2);
        mWithLocation = withLocation;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case BINARY_ITEM:
                View binaryView = inflater.inflate(R.layout.device_base_item_binary, parent, false);
                viewHolder = new ItemBinaryViewHolder(binaryView);
                break;
            case MULTILEVEL_ITEM:
                View multilevelView = inflater.inflate(R.layout.device_base_item_multilevel, parent, false);
                viewHolder = new ItemMultilevelViewHolder(multilevelView);
                break;
            case RGB_ITEM:
                View rgbView = inflater.inflate(R.layout.device_base_item_rgbw, parent, false);
                viewHolder = new ItemRGBWViewHolder(rgbView);
                break;
            case DISCRETE_ITEM:
                View discreteView = inflater.inflate(R.layout.device_base_item_discrete, parent, false);
                viewHolder = new ItemDiscreteViewHolder(discreteView);
                break;
            default:
                View v = inflater.inflate(R.layout.device_base_item, parent, false);
                viewHolder = new ItemBaseViewHolder(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        switch (mValues.get(position).getDeviceItem().getDeviceType()) {
            case DEVICE_TYPE_BATTERY:
                return MULTILEVEL_ITEM;
            case DEVICE_TYPE_DOORLOCK:
                return BINARY_ITEM;
            case DEVICE_TYPE_THERMOSTAT:
                return MULTILEVEL_ITEM;
            case DEVICE_TYPE_SENSOR_BINARY:
                return BINARY_ITEM;
            case DEVICE_TYPE_SENSOR_MULTILEVEL:
                return MULTILEVEL_ITEM;
            case DEVICE_TYPE_SWITCH_BINARY:
                return BINARY_ITEM;
            case DEVICE_TYPE_SWITCH_MULTILEVEL:
                return MULTILEVEL_ITEM;
            case DEVICE_TYPE_SWITCH_TOGGLE:
                break;
            case DEVICE_TYPE_SWITCH_CONTROL:
                return BINARY_ITEM;
            case DEVICE_TYPE_TOGGLE_BUTTON:
                break;
            case DEVICE_TYPE_SWITCH_RGBW:
                return RGB_ITEM;
            case DEVICE_TYPE_TEXT:
                break;
            case DEVICE_TYPE_CAMERA:
                break;
            case DEVICE_TYPE_SENSOR_DISCRETE:
                return DISCRETE_ITEM;
            case DEVICE_TYPE_SENSOR_MULTILINE:
                return MULTILEVEL_ITEM;
            default:
                Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Unknown device type: "
                    + mValues.get(position).getDeviceItem().getDeviceType() + "!", "Device"));
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final ItemBaseViewHolder itemBaseViewHolder = (ItemBaseViewHolder) viewHolder;

        itemBaseViewHolder.mItemView = mValues.get(position);

        /*
         * Tag in the recycler view needed for searching the view by the tag and changes visibility
         * of progress bar.
         */
        itemBaseViewHolder.mView.setTag(itemBaseViewHolder.mItemView.getDeviceItem().getDeviceId() + "-" + itemBaseViewHolder.mItemView.getDeviceItem().getHubId());

        /*
         * Icons
         */
        String customIcon = "";

        Icons icons = itemBaseViewHolder.mItemView.getDeviceItem().getIcons();
        if (icons != null && !icons.getIconsLevel().getOn().isEmpty()
                && itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
            customIcon = icons.getIconsLevel().getOn();
        } else if (icons != null && !icons.getIconsLevel().getOff().isEmpty()
                && itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
            customIcon = icons.getIconsLevel().getOff();
        } else if (icons != null && !icons.getIconsLevel().getOpen().isEmpty()
                && itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("open")) {
            customIcon = icons.getIconsLevel().getOpen();
        } else if (icons != null && !icons.getIconsLevel().getClosed().isEmpty()
                && itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("closed")) {
            customIcon = icons.getIconsLevel().getOpen();
        } else if (icons != null && !icons.getDefault().isEmpty()) {
            customIcon = icons.getDefault();
        }

        if (!customIcon.isEmpty() && new File(mContext.getFilesDir().getPath() + "/icon_" + customIcon).exists()) {
            Picasso.with(mContext).load(new File(mContext.getFilesDir().getPath() + "/icon_" + customIcon)).resize(96, 96).into(itemBaseViewHolder.mIcon, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap imageBitmap = ((BitmapDrawable) itemBaseViewHolder.mIcon.getDrawable()).getBitmap();
                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), imageBitmap);
                    imageDrawable.setCircular(true);
                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                    itemBaseViewHolder.mIcon.setImageDrawable(imageDrawable);
                }

                @Override
                public void onError() {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Set custom icon failed!", "Device"));
                    Log.w(Params.LOGGING_TAG, "Set custom icon failed!");
                }
            });
        } else if (Util.isValidURL(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getIcon())) {
            Picasso.with(mContext).load(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getIcon()).resize(96, 96).into(itemBaseViewHolder.mIcon, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Set url icon failed!", "Device"));
                    Log.w(Params.LOGGING_TAG, "Set custom icon failed!");
                }
            });
        } else if (!itemBaseViewHolder.mItemView.getDeviceItem().getIcon().equals("")) {
            Picasso.with(mContext).load(new File(itemBaseViewHolder.mItemView.getDeviceItem().getIcon())).resize(96, 96).into(itemBaseViewHolder.mIcon, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap imageBitmap = ((BitmapDrawable) itemBaseViewHolder.mIcon.getDrawable()).getBitmap();
                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), imageBitmap);
                    imageDrawable.setCircular(true);
                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                    itemBaseViewHolder.mIcon.setImageDrawable(imageDrawable);
                }

                @Override
                public void onError() {
                    Util.addProtocol(mContext, new ProtocolItem(mContext, ProtocolType.WARNING, "Set icon failed!", "Device"));
                    Log.w(Params.LOGGING_TAG, "Set custom icon failed!");
                }
            });
        } else {
            switch (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getIcon()) {
                case ICON_TEMPERATURE:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_temperature)));
                    break;
                case ICON_LUMINOSITY:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_luminosity)));
                    break;
                case ICON_ULTRAVIOLET:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_ultraviolet)));
                    break;
                case ICON_SMOKE:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_smoke_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_smoke_off)));
                    }
                    break;
                case ICON_ENERGY:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_energy)));
                    break;
                case ICON_HUMIDITY:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_humidity)));
                    break;
                case ICON_METER:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_meter)));
                    break;
                case ICON_THERMOSTAT:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_thermostat)));
                    break;
                case ICON_BATTERY:
                    try {
                       Double doubleLevel = Double.parseDouble(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel());

                       if (doubleLevel.equals(0.0)) {
                           itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_battery_0)));
                       } else if (doubleLevel > 0.0 && doubleLevel <= 20.0) {
                           itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_battery_20)));
                       } else if (doubleLevel > 20.0 && doubleLevel <= 30.0) {
                           itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_battery_30)));
                       } else if (doubleLevel > 30.0 && doubleLevel <= 50.0) {
                           itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_battery_50)));
                       } else if (doubleLevel > 50.0 && doubleLevel <= 80.0) {
                           itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_battery_80)));
                       } else if (doubleLevel > 80.0 && doubleLevel <= 100.0) {
                           itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_battery_100)));
                       }
                    } catch (NumberFormatException nfe) {
                       Log.d(Params.LOGGING_TAG, nfe.getMessage());

                       itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_battery)));
                    }
                    break;
                case ICON_BLINDS:
                    try {
                        Double doubleLevel = Double.parseDouble(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel());

                        if (doubleLevel < 0.00001) {
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_blind_down)));
                        } else if (doubleLevel > 98.99999) {
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_blind_up)));
                        } else {
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_blind_half)));
                        }
                    } catch (NumberFormatException nfe) {
                        Log.d(Params.LOGGING_TAG, nfe.getMessage());

                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_blind_half)));
                    }
                    break;
                case ICON_MULTILEVEL:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getProbeType().equals(PROBE_TYPE_SWITCH_COLOR_SOFT_WHITE)) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_switch_warm_white)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getProbeType().equals(PROBE_TYPE_SWITCH_COLOR_COLD_WHITE)) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_switch_cold_white)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getProbeType().equals(PROBE_TYPE_SWITCH_COLOR_RED)) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_switch_red)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getProbeType().equals(PROBE_TYPE_SWITCH_COLOR_GREEN)) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_switch_green)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getProbeType().equals(PROBE_TYPE_SWITCH_COLOR_BLUE)) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_switch_blue)));
                    } else {
                        try {
                            Double doubleLevel = Double.parseDouble(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel());

                            if (doubleLevel < 0.00001) {
                                itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_dimmer_off)));
                            } else if (doubleLevel > 98.99999) {
                                itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_dimmer_on)));
                            } else {
                                itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_dimmer_half)));
                            }
                        } catch (NumberFormatException nfe) {
                            Log.d(Params.LOGGING_TAG, nfe.getMessage());

                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_dimmer_half)));
                        }
                    }
                    break;
                case ICON_MOTION:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_motion_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_motion_off)));
                    }
                    break;
                case ICON_TAMPER:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_tamper_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_tamper_off)));
                    }
                    break;
                case ICON_ALARM:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_alarm_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_alarm_off)));
                    } else {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_alarm)));
                    }
                    break;
                case ICON_DOOR:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getDeviceType().equals(DEVICE_TYPE_DOORLOCK)) {
                        if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("open")) {
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_lock_open)));
                        } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("close")) {
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_lock_closed)));
                        }
                    } else {
                        if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("open")) {
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_door_open)));
                        } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("close")) {
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_door_closed)));
                        }
                    }

                    break;
                case ICON_SWITCH:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_switch_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_switch_off)));
                    }
                    break;
                case ICON_COOLING:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_cooling_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_cooling_off)));
                    }
                    break;
                case ICON_FLOOD:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_flood_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_flood_off)));
                    }
                    break;
                case ICON_CO:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("on")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_co_alarm_on)));
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().equals("off")) {
                        itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_co_alarm_off)));
                    }
                    break;
                case ICON_BAROMETER:
                    itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_barometer)));
                    break;
                case ICON_GESTURE:
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getDiscreteState() != null
                            && !itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getDiscreteState().equals("")) {
                        switch (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getDiscreteState()) {
                            case "press":
                                itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_press)));
                                break;
                            case "hold":
                                itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_hold)));
                                break;
                            case "release":
                                itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_release)));
                                break;
                        }
                    }
                    break;
                default:
                    // Check device type
                    switch (itemBaseViewHolder.mItemView.getDeviceItem().getDeviceType()) {
                        case DEVICE_TYPE_CAMERA:
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_camera)));
                            break;
                        default:
                            itemBaseViewHolder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, (R.mipmap.ic_placeholder)));
                            break;
                    }
            }
        }

        /*
         * Common properties
         */

        itemBaseViewHolder.mTitle.setText(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getTitle());
        itemBaseViewHolder.mBackTitle.setText(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getTitle());
        if (mWithLocation) {
            Picasso.with(mContext)
                    .load(new File(itemBaseViewHolder.mItemView.getDeviceItem().getLocationItem().getTile()))
                    .resize(64, 64)
                    .into(itemBaseViewHolder.mLocationTile);
        } else {
            itemBaseViewHolder.mLocationTile.setVisibility(View.GONE);
        }


        // Update time
        Date updateTime = new Date((long) itemBaseViewHolder.mItemView.getDeviceItem().getUpdateTime()*1000);
        itemBaseViewHolder.mBackUpdateTime.setText(Params.formatDateTimeWithoutSecondsGerman(updateTime));

        /*
         * Front/back view
         */

        if (itemBaseViewHolder.mItemView.getFrontViewVisible()) {
            itemBaseViewHolder.mBack.setVisibility(View.GONE);
            itemBaseViewHolder.mFront.setVisibility(View.VISIBLE);

            /*
             * Display status in upper right corner:
             * - status 1 means OK
             * - status -1 means an error occurred
             */
            itemBaseViewHolder.mProgress.setVisibility(View.GONE);

            if (itemBaseViewHolder.mItemView.getStatus() == 1) {
                itemBaseViewHolder.mProgressSuccess.setVisibility(View.VISIBLE);
                // Display OK only for one second
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        itemBaseViewHolder.mProgressSuccess.setVisibility(View.GONE);
                    }
                }, 1000);
                itemBaseViewHolder.mItemView.setStatus(0);
            } else if (itemBaseViewHolder.mItemView.getStatus() == -1) {
                itemBaseViewHolder.mProgressError.setVisibility(View.VISIBLE); // Remains until the element is updated (with status 1 or 0)
                itemBaseViewHolder.mItemView.setStatus(0);
            } else {
                // Default status 0 won't display any information on upper right corner
                itemBaseViewHolder.mProgressSuccess.setVisibility(View.GONE);
                itemBaseViewHolder.mProgressError.setVisibility(View.GONE);
            }
        } else {
            itemBaseViewHolder.mFront.setVisibility(View.GONE);
            itemBaseViewHolder.mBack.setVisibility(View.VISIBLE);
        }

        /*
         * Normal click on card
         */
        itemBaseViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemBaseViewHolder.mItemView.getFrontViewVisible()) {
                    mListener.onDevicesAdapterAction(new DeviceItemApp(itemBaseViewHolder.mItemView.getDeviceItem()));
                }
            }
        });

        /*
         * Long click on card
         */
        itemBaseViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*
                 * Flip front/back
                 */
                if (itemBaseViewHolder.mItemView.getFrontViewVisible()) {
                    itemBaseViewHolder.mItemView.setFrontViewVisible(false);

                    itemBaseViewHolder.mFront.setVisibility(View.GONE);
                    itemBaseViewHolder.mBack.setVisibility(View.VISIBLE);
                } else {
                    itemBaseViewHolder.mItemView.setFrontViewVisible(true);

                    itemBaseViewHolder.mBack.setVisibility(View.GONE);
                    itemBaseViewHolder.mFront.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        /*
         * Flip button
         */
        itemBaseViewHolder.mBackFlipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Flip front/back
                 */
                if (itemBaseViewHolder.mItemView.getFrontViewVisible()) {
                    itemBaseViewHolder.mItemView.setFrontViewVisible(false);

                    itemBaseViewHolder.mFront.setVisibility(View.GONE);
                    itemBaseViewHolder.mBack.setVisibility(View.VISIBLE);
                } else {
                    itemBaseViewHolder.mItemView.setFrontViewVisible(true);

                    itemBaseViewHolder.mBack.setVisibility(View.GONE);
                    itemBaseViewHolder.mFront.setVisibility(View.VISIBLE);
                }
            }
        });

        /*
         * Move button
         */
        itemBaseViewHolder.mBackMoveBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDragStartListener.onStartDrag(itemBaseViewHolder);
                return true;
            }
        });

        /*
         * History button
         */
        if (itemBaseViewHolder.mItemView.getDeviceItem().getDeviceType().equals(DEVICE_TYPE_SENSOR_MULTILEVEL)) {
            itemBaseViewHolder.mBackHistoryBtn.setVisibility(View.VISIBLE);

            itemBaseViewHolder.mBackHistoryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeviceHistory(itemBaseViewHolder.mItemView.getDeviceItem());
                }
            });
        } else {
            itemBaseViewHolder.mBackHistoryBtn.setVisibility(View.GONE);
        }

        /*
         * Depending on the device type
         */
        switch (itemBaseViewHolder.getItemViewType()) {
            case BINARY_ITEM:
                ItemBinaryViewHolder binaryView = (ItemBinaryViewHolder) itemBaseViewHolder;

                String state = mContext.getString(R.string.device_level_unknown);
                if (itemBaseViewHolder.mItemView.getDeviceItem().getDeviceType().equals(DEVICE_TYPE_DOORLOCK)) {
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().toLowerCase().equals("open")) {
                        state = mContext.getString(R.string.device_level_open);
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().toLowerCase().equals("close")) {
                        state = mContext.getString(R.string.device_level_close);
                    }
                } else {
                    if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().toLowerCase().equals("on")) {
                        state = mContext.getString(R.string.device_level_on);
                    } else if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel().toLowerCase().equals("off")) {
                        state = mContext.getString(R.string.device_level_off);
                    }
                }

                binaryView.mLevel.setText(state);

                /*
                 * Swipe on card
                 */

//                binaryView.itemView.setOnTouchListener(new OnSwipeTouchListener(mContext) {
//                    @Override
//                    public void onSwipeDown() {
//                        // Do nothing
//                    }
//
//                    @Override
//                    public void onSwipeLeft() {
//                        if (itemBaseViewHolder.mItemView.getFrontViewVisible()) {
//                            mListener.onDevicesAdapterAction(new DeviceItemApp(itemBaseViewHolder.mItemView.getDeviceItem()), "off");
//                        }
//                    }
//
//                    @Override
//                    public void onSwipeUp() {
//                        // Do nothing
//                    }
//
//                    @Override
//                    public void onSwipeRight() {
//                        if (itemBaseViewHolder.mItemView.getFrontViewVisible()) {
//                            mListener.onDevicesAdapterAction(new DeviceItemApp(itemBaseViewHolder.mItemView.getDeviceItem()), "on");
//                        }
//                    }
//
//                    @Override
//                    public void onLongClick() {
//                        if (itemBaseViewHolder.mItemView.getFrontViewVisible()) {
//                            itemBaseViewHolder.mItemView.setFrontViewVisible(false);
//
//                            itemBaseViewHolder.mFront.setVisibility(View.GONE);
//                            itemBaseViewHolder.mBack.setVisibility(View.VISIBLE);
//                        } else {
//                            itemBaseViewHolder.mItemView.setFrontViewVisible(true);
//
//                            itemBaseViewHolder.mBack.setVisibility(View.GONE);
//                            itemBaseViewHolder.mFront.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });

                break;
            case MULTILEVEL_ITEM:
                ItemMultilevelViewHolder multilevelView = (ItemMultilevelViewHolder) itemBaseViewHolder;

                String level = itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel();
                try {
                    Double doubleLevel = Double.parseDouble(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getLevel());

                    level = mFormatter.format(doubleLevel);
                } catch (NumberFormatException nfe) {
                    Log.d(Params.LOGGING_TAG, nfe.getMessage());
                }

                multilevelView.mLevel.setText(level);
                multilevelView.mScale.setText(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getScaleTitle());

                break;
            case RGB_ITEM:
                ItemRGBWViewHolder rgbView = (ItemRGBWViewHolder) itemBaseViewHolder;

                rgbView.mRGB.setBackgroundColor(Util.getIntFromColor(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getColor().getRed(),
                        itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getColor().getGreen(),
                        itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getColor().getBlue()));

                break;
            case DISCRETE_ITEM:
                ItemDiscreteViewHolder discreteView = (ItemDiscreteViewHolder) itemBaseViewHolder;

                String discreteLevel = mContext.getString(R.string.device_level_unknown);

                if (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getDiscreteType().equals("B")) {
                    String discreteState = "";
                    switch (itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getDiscreteState()) {
                        case "press":
                            discreteState = mContext.getString(R.string.device_sensor_discrete_press);
                            break;
                        case "hold":
                            discreteState = mContext.getString(R.string.device_sensor_discrete_hold);
                            break;
                        case "release":
                            discreteState = mContext.getString(R.string.device_sensor_discrete_release);
                            break;
                    }

                    discreteLevel = String.valueOf(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getDiscreteCount()) + "x "
                        +  mContext.getString(R.string.device_sensor_discrete_button) +" #" + String.valueOf(itemBaseViewHolder.mItemView.getDeviceItem().getMetrics().getDiscreteCurrentScene())
                        + " " + discreteState;
                }

                discreteView.mLevel.setText(discreteLevel);

                break;
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<DevicesItemView> values) {
        mValues.addAll(values);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mValues, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mValues, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemSwipe(int position, int direction) {
        // Do nothing
    }

    class ItemBaseViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private View mView;
        private RelativeLayout mFront, mBack;
        private TextView mTitle;
        private ImageView mIcon;
        private ImageView mLocationTile;
        private ProgressBar mProgress;
        private IconicsImageView mProgressSuccess;
        private IconicsImageView mProgressError;
        private DevicesItemView mItemView;

        /**
         * Back
         */
        private FancyButton mBackFlipBtn, mBackMoveBtn, mBackHistoryBtn;
        private TextView mBackTitle;
        private TextView mBackUpdateTime;

        private Drawable mDefaultBackground; // Store temporary the default background

        ItemBaseViewHolder(View view) {
            super(view);
            mView = view;

            mFront = (RelativeLayout) view.findViewById(R.id.device_front);
            mBack = (RelativeLayout) view.findViewById(R.id.device_back);

            mTitle = (TextView) view.findViewById(R.id.device_item_title);

            mIcon = (ImageView) view.findViewById(R.id.device_item_icon);
            mLocationTile = (ImageView) view.findViewById(R.id.device_item_location_tile);

            mProgress = (ProgressBar) view.findViewById(R.id.device_item_progress);
            mProgressSuccess = (IconicsImageView) view.findViewById(R.id.device_item_progress_success);
            mProgressError = (IconicsImageView) view.findViewById(R.id.device_item_progress_error);

            /*
             * Back
             */
            mBackFlipBtn = (FancyButton) view.findViewById(R.id.device_item_flip_btn);
            mBackMoveBtn = (FancyButton) view.findViewById(R.id.device_item_move_btn);
            mBackHistoryBtn = (FancyButton) view.findViewById(R.id.device_item_history_btn);

            mBackTitle = (TextView) view.findViewById(R.id.device_item_back_title);
            mBackUpdateTime = (TextView) view.findViewById(R.id.device_item_update_time);

            mDefaultBackground = mView.getBackground();
        }

        @Override
        public void onItemSelected() {
            mView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.accent50));
        }

        @Override
        public void onItemClear() {
            ArrayList<DeviceItemApp> deviceItems = new ArrayList<>();
            for (int i = 0; i < mValues.size(); i++) {
                deviceItems.add(mValues.get(i).getDeviceItem());
            }
            mListener.onDevicesAdapterSwap(deviceItems);
            mView.setBackground(mDefaultBackground);
        }
    }

    private class ItemBinaryViewHolder extends ItemBaseViewHolder {

        private TextView mLevel;

        ItemBinaryViewHolder(View view) {
            super(view);

            mLevel = (TextView) view.findViewById(R.id.device_item_level);
        }
    }

    private class ItemDiscreteViewHolder extends ItemBaseViewHolder {

        private TextView mLevel;

        ItemDiscreteViewHolder(View view) {
            super(view);

            mLevel = (TextView) view.findViewById(R.id.device_item_level);
        }
    }

    private class ItemMultilevelViewHolder extends ItemBaseViewHolder {

        private TextView mLevel;
        private TextView mScale;

        ItemMultilevelViewHolder(View view) {
            super(view);

            mLevel = (TextView) view.findViewById(R.id.device_item_level);
            mScale = (TextView) view.findViewById(R.id.device_item_scale);
        }
    }

    private class ItemRGBWViewHolder extends ItemBaseViewHolder {

        private CardView mRGB;

        ItemRGBWViewHolder(View view) {
            super(view);

            mRGB = (CardView) view.findViewById(R.id.device_item_rgb_color);
        }
    }

    public synchronized void updateItem(DevicesItemView deviceItemView) {
        // Log.i(Params.LOGGING_TAG, "Device view adapter - Update item ...");

        int position = getPositionByItem(deviceItemView);
        if (position != -1) {
            Integer newUpdateTime = deviceItemView.getDeviceItem().getUpdateTime();
            Integer currentUpdateTime = mValues.get(position).getDeviceItem().getUpdateTime();

            String newLevel = deviceItemView.getDeviceItem().getMetrics().getLevel();
            String currentLevel = mValues.get(position).getDeviceItem().getMetrics().getLevel();

            // Log.i(Params.LOGGING_TAG, "Device view adapter - Update item: New level is: " + newLevel);
            // Log.i(Params.LOGGING_TAG, "Device view adapter - Update item: Current level is: " + currentLevel);

            Boolean colorChanged = false;

            Integer newRed = deviceItemView.getDeviceItem().getMetrics().getColor().getRed();
            Integer newGreen = deviceItemView.getDeviceItem().getMetrics().getColor().getGreen();
            Integer newBlue = deviceItemView.getDeviceItem().getMetrics().getColor().getBlue();

            Integer currentRed = mValues.get(position).getDeviceItem().getMetrics().getColor().getRed();
            Integer currentGreen = mValues.get(position).getDeviceItem().getMetrics().getColor().getGreen();
            Integer currentBlue = mValues.get(position).getDeviceItem().getMetrics().getColor().getBlue();

            if (deviceItemView.getDeviceItem().getDeviceType().equals(DEVICE_TYPE_SWITCH_RGBW)) {
                if (!newRed.equals(currentRed) || !newGreen.equals(currentGreen) || !newBlue.equals(currentBlue)) {
                    colorChanged = true;
                }
            }

            if (deviceItemView.getFrontViewVisible()) {
                /*
                 * Changes for front view
                 */
                if (!newLevel.equals(currentLevel) || colorChanged) {
                    // Log.i(Params.LOGGING_TAG, "Device view adapter - Update item: Level or color changed on position: " + position
                    //         + " with level: " + deviceItemView.getDeviceItem().getMetrics().getLevel());

                    mValues.set(position, deviceItemView);
                    notifyItemChanged(position);
                }
            } else {
                /*
                 * Changes for back view
                 */
                if (!newUpdateTime.equals(currentUpdateTime)) {
                    mValues.set(position, deviceItemView);
                    notifyItemChanged(position);
                }
            }
        } else {
            Log.i(Params.LOGGING_TAG, "Device view adapter - Update item: Not found!");
        }
    }

    private synchronized int getPositionByItem(DevicesItemView deviceItemView) {
        for (int i = 0; i < mValues.size(); i++) {
            if (mValues.get(i).getDeviceItem().getDeviceId().equals(deviceItemView.getDeviceItem().getDeviceId())
                    && mValues.get(i).getDeviceItem().getHubId().equals(deviceItemView.getDeviceItem().getHubId())) {
                return i;
            }
        }

        return -1;
    }

    public interface OnDevicesAdapterInteractionListener {
        void onDevicesAdapterAction(DeviceItemApp deviceItem);
        void onDevicesAdapterAction(DeviceItemApp deviceItem, String action);
        void onDevicesAdapterSwap(List<DeviceItemApp> list);
        void onDeviceHistory(DeviceItemApp deviceItem);
    }
}
