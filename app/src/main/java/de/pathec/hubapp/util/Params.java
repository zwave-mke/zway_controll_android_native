package de.pathec.hubapp.util;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Params {
    public static final String LOGGING_TAG = "HubApp";

    public static final int DATABASE_VERSION = 22;
    public static final String DATABASE_NAME = "HubAppDB";

    private static final DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final DateFormat dfDateGER = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private static final DateFormat dfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private static final DateFormat dfDateTimeGER = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
    private static final DateFormat dfDateTimeGERShort = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private static final DateFormat dfDateTimeWithoutSecondsGER = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private static final DateFormat dfTimeShort = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final DateFormat dfTimeHours = new SimpleDateFormat("HH", Locale.getDefault());

    public static final String PREFS_NAME = "HubApp";

    public static final String PREFS_FCM_LAST_MESSAGE = "FCMLastMessage";
    public static final String PREFS_FCM_COUNTER = "FCMCounter";

    public static final String PREFS_DEVICE_FILTER_DEVICE_TYPE = "DeviceFilterDeviceType";
    public static final String PREFS_DEVICE_FILTER_TAG = "DeviceFilterTag";
    public static final String PREFS_DEVICE_SORTING = "DeviceSorting";

    public static final String PREFS_NOTIFICATION_FILTER_TYPE = "NotificationFilterType";
    public static final String PREFS_NOTIFICATION_FILTER_SOURCE = "NotificationFilterSource";

    public static final String PREFS_DEBUGGING_NOTIFICATIONS = "DebuggingNotifications";

    public static final String PREFS_HUB_CONFIGURED = "HubConfigured";

    public static final String PREFS_HUB_PHONE_NAME = "HubPhoneName";
    public static final String PREFS_HUB_PHONE_IMAGE = "HubPhoneImage";
    public static final String PREFS_HUB_GEOFENCING_ENABLED = "HubGeofencingEnabled";

    public static final String PREFS_START_VIEW = "StartView";

    public static final String PREFS_VIEW_BATTERY_INFO = "ViewBatteryInfo";

    public static final String PREFS_CURRENT_PROFILE_ID = "CurrentProfileId";

    public static final String PREFS_ACTIVE_HUB = "ActiveHub";

    public static String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    public static Integer FADE_ANIMATION_DURATION = 200;

    public static synchronized String formatDateTime(Date date) {
        return dfDateTime.format(date);
    }

    public static synchronized Date parseDateTime(String dateString) throws ParseException {
        return dfDateTime.parse(dateString);
    }

    public static synchronized String formatDate(Date date) {
        return dfDate.format(date);
    }

    public static synchronized Date parseDate(String dateString) throws ParseException {
        return dfDate.parse(dateString);
    }

    public static synchronized String formatDateTimeGerman(Date date) {
        return dfDateTimeGER.format(date);
    }

    public static synchronized Date parseDateTimeGerman(String dateString) throws ParseException {
        return dfDateTimeGER.parse(dateString);
    }

    public static synchronized String formatDateTimeGermanShort(Date date) {
        return dfDateTimeGERShort.format(date);
    }

    public static synchronized Date parseDateTimeGermanShort(String dateString) throws ParseException {
        return dfDateTimeGERShort.parse(dateString);
    }

    public static synchronized String formatDateTimeWithoutSecondsGerman(Date date) {
        return dfDateTimeWithoutSecondsGER.format(date);
    }

    public static synchronized Date parseDateTimeWithoutSecondsGerman(String dateString) throws ParseException {
        return dfDateTimeWithoutSecondsGER.parse(dateString);
    }

    public static synchronized String formatDateGerman(Date date) {
        return dfDateGER.format(date);
    }

    public static synchronized Date parseDateGerman(String dateString) throws ParseException {
        return dfDateGER.parse(dateString);
    }

    public static synchronized String formatTimeShort(Date date, Boolean correctTimezone) {
        if (correctTimezone) return dfTimeShort.format(correctTimezone(date));
        else return dfTimeShort.format(date);
    }

    public static synchronized Date parseTimeShort(String dateString) throws ParseException {
        return dfTimeShort.parse(dateString);
    }

    public static synchronized String formatTimeHours(Date date, Boolean correctTimezone) {
        if (correctTimezone) return dfTimeHours.format(correctTimezone(date));
        else return dfTimeHours.format(date);
    }

    public static synchronized Date parseTimeHours(String dateString) throws ParseException {
        return dfTimeHours.parse(dateString);
    }

    private static synchronized Date correctTimezone(Date date) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + (TimeZone.getDefault().getOffset(date.getTime())/(1000*60*60)));

        return calendar.getTime();
    }

    public static synchronized Date buildDate(Integer hour) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        if (calendar.get(Calendar.HOUR_OF_DAY) < hour) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        return calendar.getTime();
    }

    public static Date clearTime(Date date, Boolean hours, Boolean minutes, Boolean seconds, Boolean milliseconds) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        if (hours)
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        if (minutes)
            calendar.set(Calendar.MINUTE, 0);
        if (seconds)
            calendar.set(Calendar.SECOND, 0);
        if (milliseconds)
            calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
