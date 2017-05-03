package de.pathec.hubapp.util;

import android.animation.Animator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.model.protocol.ProtocolList;

public class Util {
    public static void showMessage(final Context context, final String text) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (text.length() > 100) {
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    }

                } catch (NullPointerException npe) {
                    Log.e(Params.LOGGING_TAG, "Unexpected exception: Message show ...");
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static int getDPI(int size, DisplayMetrics metrics){
        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

    public static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    public static Map<String, List<String>> splitQuery(String query) {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<>();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ?pair.substring(0, idx) : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    public static void addProtocol(Context context, ProtocolItem protocolItem) {
        ProtocolList protocolList = new ProtocolList(context);
        protocolList.addProtocolItem(protocolItem);
    }

    public static Bitmap drawableTileToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Save bitmap to internal app storage.
     *
     * @param context - context
     * @param bitmap - bitmap object
     * @param filename - filename without extension
     * @return filename.extension
     */
    public static String saveImage(Context context, Bitmap bitmap, String filename, String extension) {
        String fileName = "";

        if (bitmap != null) {
            try {
                File directory = context.getFilesDir();
                if (extension.isEmpty()) {
                    fileName = filename.toLowerCase();
                } else {
                    fileName = filename.toLowerCase() + "." + extension;
                }


                File file = new File(directory, fileName);

                FileOutputStream stream = new FileOutputStream(file);

                // here we Resize the Image ...
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                        byteArrayOutputStream);
                byte[] bsResized = byteArrayOutputStream.toByteArray();

                stream.write(bsResized);
                stream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream srcStream = new FileInputStream(src);
        FileOutputStream dstStream = new FileOutputStream(dst);
        byte[] bytes = new byte[1024];

        int size;
        while((size = srcStream.read(bytes)) > 0) {
            dstStream.write(bytes, 0, size);
        }

        srcStream.close();
        dstStream.close();
    }

    public static boolean isConnectedToWifi(Context context, String ssid) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
            return ssid.equals(wifiInfo.getSSID());
        }

        return false;
    }

    public static boolean connectToWifi(Context context, String ssid, String wifiPassword) {
        try {
            WifiConfiguration wifiConfig = new WifiConfiguration();

            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.status = WifiConfiguration.Status.DISABLED;
            wifiConfig.priority = 40;

            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            wifiConfig.preSharedKey = String.format("\"%s\"", wifiPassword);

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int networkId  = wifiManager.addNetwork(wifiConfig);

            if(networkId != -1) {
                boolean isDisconnected = wifiManager.disconnect();
                Log.v(Params.LOGGING_TAG, "Wfi isDisconnected : " + isDisconnected);
                boolean isEnabled = wifiManager.enableNetwork(networkId , true);
                Log.v(Params.LOGGING_TAG, "Wfi isEnabled : " + isEnabled);
                boolean isReconnected = wifiManager.reconnect();
                Log.v(Params.LOGGING_TAG, "Wfi isReconnected : " + isReconnected);
            } else {
                Log.w(Params.LOGGING_TAG, "Network ID couldn't assigned");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(Params.LOGGING_TAG, "Error occurred during connecting to Wifi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public synchronized static void switchViews(final View viewToHide, final View viewToShow) {
        switchViews(viewToHide, viewToShow, Params.FADE_ANIMATION_DURATION);
    }

    public synchronized static void switchViews(final View viewToHide, final View viewToShow, final Integer duration) {
        viewToHide.animate()
            .alpha(0.0f)
            .setDuration(duration)
            .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    viewToHide.setVisibility(View.GONE);
                    viewToShow.setVisibility(View.VISIBLE);
                    viewToShow.animate()
                            .alpha(1.0f)
                            .setDuration(duration);
                    viewToHide.animate().setListener(null);
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
    }

    public synchronized static void hideView(final View view, final Integer duration) {
        view.animate()
            .alpha(0.0f)
            .setDuration(duration)
            .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.animate().setListener(null);
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
    }

    public synchronized static void showView(final View view, final Integer duration) {
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1.0f)
            .setDuration(duration);
    }

    public synchronized static boolean isValidURL(String url) {
        URL u;

        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
    }
}
