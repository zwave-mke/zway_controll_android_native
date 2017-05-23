package de.pathec.hubapp.util;


import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import de.pathec.hubapp.R;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolType;

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

public class ZWayUtil {

    public static final Integer SORTING_CUSTOM = 0;
    public static final Integer SORTING_OLDEST = 1;
    public static final Integer SORTING_NEWEST = 2;
    public static final Integer SORTING_NAME_ASC = 3;
    public static final Integer SORTING_NAME_DESC = 4;
    public static final Integer SORTING_LAST_UPDATED = 5;

    public static String getDeviceTypeLabelByIdentifier(Context context, String deviceTypeIdentifier) {
        switch (deviceTypeIdentifier) {
            case DEVICE_TYPE_BATTERY:
                return context.getString(R.string.device_type_battery);
            case DEVICE_TYPE_DOORLOCK:
                return context.getString(R.string.device_type_doorlock);
            case DEVICE_TYPE_THERMOSTAT:
                return context.getString(R.string.device_type_thermostat);
            case DEVICE_TYPE_SWITCH_BINARY:
                return context.getString(R.string.device_type_switch_binary);
            case DEVICE_TYPE_SWITCH_MULTILEVEL:
                return context.getString(R.string.device_type_switch_multilevel);
            case DEVICE_TYPE_SENSOR_BINARY:
                return context.getString(R.string.device_type_sensor_binary);
            case DEVICE_TYPE_SENSOR_MULTILEVEL:
                return context.getString(R.string.device_type_sensor_multilevel);
            case DEVICE_TYPE_SWITCH_TOGGLE:
                return context.getString(R.string.device_type_switch_toggle);
            case DEVICE_TYPE_SWITCH_CONTROL:
                return context.getString(R.string.device_type_switch_control);
            case DEVICE_TYPE_TOGGLE_BUTTON:
                return context.getString(R.string.device_type_toggle_button);
            case DEVICE_TYPE_SWITCH_RGBW:
                return context.getString(R.string.device_type_switch_rgbw);
            case DEVICE_TYPE_TEXT:
                return context.getString(R.string.device_type_text);
            case DEVICE_TYPE_CAMERA:
                return context.getString(R.string.device_type_camera);
            case DEVICE_TYPE_SENSOR_DISCRETE:
                return context.getString(R.string.device_type_sensor_discrete);
            case DEVICE_TYPE_SENSOR_MULTILINE:
                return context.getString(R.string.device_type_sensor_multiline);
        }

        Util.addProtocol(context, new ProtocolItem(context, ProtocolType.WARNING, "Unknown device type: " + deviceTypeIdentifier + "!", "ZWayUtil"));
        return "Unknown";
    }

    public static String getDeviceTypeIdentifierByLabel(Context context, String deviceTypeLabel) {
        if (deviceTypeLabel.equals(context.getString(R.string.device_type_battery))) {
            return DEVICE_TYPE_BATTERY;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_doorlock))) {
            return DEVICE_TYPE_DOORLOCK;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_thermostat))) {
            return DEVICE_TYPE_THERMOSTAT;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_switch_binary))) {
            return DEVICE_TYPE_SWITCH_BINARY;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_switch_multilevel))) {
            return DEVICE_TYPE_SWITCH_MULTILEVEL;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_sensor_binary))) {
            return DEVICE_TYPE_SENSOR_BINARY;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_sensor_multilevel))) {
            return DEVICE_TYPE_SENSOR_MULTILEVEL;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_switch_toggle))) {
            return DEVICE_TYPE_SWITCH_TOGGLE;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_switch_control))) {
            return DEVICE_TYPE_SWITCH_CONTROL;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_toggle_button))) {
            return DEVICE_TYPE_TOGGLE_BUTTON;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_switch_rgbw))) {
            return DEVICE_TYPE_SWITCH_RGBW;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_text))) {
            return DEVICE_TYPE_TEXT;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_camera))) {
            return DEVICE_TYPE_CAMERA;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_sensor_discrete))) {
            return DEVICE_TYPE_SENSOR_DISCRETE;
        } else if (deviceTypeLabel.equals(context.getString(R.string.device_type_sensor_multiline))) {
            return DEVICE_TYPE_SENSOR_MULTILINE;
        }

        Util.addProtocol(context, new ProtocolItem(context, ProtocolType.WARNING, "Unknown device type: " + deviceTypeLabel + "!", "ZWayUtil"));
        return "Unknown";
    }

    public static Set<String> getBlacklistDeviceIds() {
        Set<String> blacklist = new HashSet<>();

        blacklist.add("OpenHabConnector");

        return blacklist;
    }

    public static String getOpenWeatherDescriptionById(Context context, Integer weatherId) {
        switch (weatherId) {
            case 200:
                return context.getString(R.string.open_weather_condition_200);
            case 201:
                return context.getString(R.string.open_weather_condition_201);
            case 202:
                return context.getString(R.string.open_weather_condition_202);
            case 210:
                return context.getString(R.string.open_weather_condition_210);
            case 211:
                return context.getString(R.string.open_weather_condition_211);
            case 212:
                return context.getString(R.string.open_weather_condition_212);
            case 221:
                return context.getString(R.string.open_weather_condition_221);
            case 230:
                return context.getString(R.string.open_weather_condition_230);
            case 231:
                return context.getString(R.string.open_weather_condition_231);
            case 232:
                return context.getString(R.string.open_weather_condition_232);

            case 300:
                return context.getString(R.string.open_weather_condition_300);
            case 301:
                return context.getString(R.string.open_weather_condition_301);
            case 302:
                return context.getString(R.string.open_weather_condition_302);
            case 310:
                return context.getString(R.string.open_weather_condition_310);
            case 311:
                return context.getString(R.string.open_weather_condition_311);
            case 312:
                return context.getString(R.string.open_weather_condition_312);
            case 313:
                return context.getString(R.string.open_weather_condition_313);
            case 314:
                return context.getString(R.string.open_weather_condition_314);
            case 321:
                return context.getString(R.string.open_weather_condition_321);

            case 500:
                return context.getString(R.string.open_weather_condition_500);
            case 501:
                return context.getString(R.string.open_weather_condition_501);
            case 502:
                return context.getString(R.string.open_weather_condition_502);
            case 503:
                return context.getString(R.string.open_weather_condition_503);
            case 504:
                return context.getString(R.string.open_weather_condition_504);
            case 511:
                return context.getString(R.string.open_weather_condition_511);
            case 520:
                return context.getString(R.string.open_weather_condition_520);
            case 521:
                return context.getString(R.string.open_weather_condition_521);
            case 522:
                return context.getString(R.string.open_weather_condition_522);
            case 531:
                return context.getString(R.string.open_weather_condition_531);

            case 600:
                return context.getString(R.string.open_weather_condition_600);
            case 601:
                return context.getString(R.string.open_weather_condition_601);
            case 602:
                return context.getString(R.string.open_weather_condition_602);
            case 611:
                return context.getString(R.string.open_weather_condition_611);
            case 612:
                return context.getString(R.string.open_weather_condition_612);
            case 615:
                return context.getString(R.string.open_weather_condition_615);
            case 616:
                return context.getString(R.string.open_weather_condition_616);
            case 620:
                return context.getString(R.string.open_weather_condition_620);
            case 621:
                return context.getString(R.string.open_weather_condition_621);
            case 622:
                return context.getString(R.string.open_weather_condition_622);

            case 701:
                return context.getString(R.string.open_weather_condition_701);
            case 711:
                return context.getString(R.string.open_weather_condition_711);
            case 721:
                return context.getString(R.string.open_weather_condition_721);
            case 731:
                return context.getString(R.string.open_weather_condition_731);
            case 741:
                return context.getString(R.string.open_weather_condition_741);
            case 751:
                return context.getString(R.string.open_weather_condition_751);
            case 761:
                return context.getString(R.string.open_weather_condition_761);
            case 762:
                return context.getString(R.string.open_weather_condition_762);
            case 771:
                return context.getString(R.string.open_weather_condition_771);
            case 781:
                return context.getString(R.string.open_weather_condition_781);

            case 800:
                return context.getString(R.string.open_weather_condition_800);
            case 801:
                return context.getString(R.string.open_weather_condition_801);
            case 802:
                return context.getString(R.string.open_weather_condition_802);
            case 803:
                return context.getString(R.string.open_weather_condition_803);
            case 804:
                return context.getString(R.string.open_weather_condition_804);

            case 900:
                return context.getString(R.string.open_weather_condition_900);
            case 901:
                return context.getString(R.string.open_weather_condition_901);
            case 902:
                return context.getString(R.string.open_weather_condition_902);
            case 903:
                return context.getString(R.string.open_weather_condition_903);
            case 904:
                return context.getString(R.string.open_weather_condition_904);
            case 905:
                return context.getString(R.string.open_weather_condition_905);
            case 906:
                return context.getString(R.string.open_weather_condition_906);

            case 951:
                return context.getString(R.string.open_weather_condition_951);
            case 952:
                return context.getString(R.string.open_weather_condition_952);
            case 953:
                return context.getString(R.string.open_weather_condition_953);
            case 954:
                return context.getString(R.string.open_weather_condition_954);
            case 955:
                return context.getString(R.string.open_weather_condition_955);
            case 956:
                return context.getString(R.string.open_weather_condition_956);
            case 957:
                return context.getString(R.string.open_weather_condition_957);
            case 958:
                return context.getString(R.string.open_weather_condition_958);
            case 959:
                return context.getString(R.string.open_weather_condition_959);
            case 960:
                return context.getString(R.string.open_weather_condition_960);
            case 961:
                return context.getString(R.string.open_weather_condition_961);
            case 962:
                return context.getString(R.string.open_weather_condition_962);

            default:
                return "";
        }
    }
}
