package de.pathec.hubapp.util;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class ZWayDiscovery {

    private ZWayDiscoveryInteractionListener mListener;
    private Integer mAddressCount = 0;
    private Integer mAddressChecked = 0;

    private AsyncTask mScanTask = null;
    private Boolean mIsRunning = false;

    public ZWayDiscovery(ZWayDiscoveryInteractionListener listener) {
        mListener = listener;
    }

    private synchronized void setCount(Integer count) {
        mAddressCount = count;

        mListener.onZWayDiscoveryAddressCount(mAddressCount);
    }

    private synchronized  void incrementChecked() {
        mAddressChecked++;
    }

    public Boolean isRunning() {
        return mIsRunning;
    }

    public void cancelScan() {
        if (mScanTask != null) {
            mScanTask.cancel(true);
        }
    }

    public void startScan() {
        if(!mIsRunning) {
            mIsRunning = true;
            mAddressCount = 0;
            mAddressChecked = 0;

            mScanTask = new AsyncTask<Void, Integer, Void>() {
                @Override
                protected void onPreExecute() {
                }

                @Override
                protected Void doInBackground( final Void ... params ) {
                    ValidateIPV4 validator = new ValidateIPV4();

                    try {
                        Enumeration<NetworkInterface> enumNetworkInterface = NetworkInterface.getNetworkInterfaces();
                        while (enumNetworkInterface.hasMoreElements()) {
                            NetworkInterface networkInterface = enumNetworkInterface.nextElement();
                            if (networkInterface.isUp() && !networkInterface.isVirtual() && !networkInterface.isLoopback()) {
                                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                                    if (validator.isValidIPV4(address.getAddress().getHostAddress())) {
                                        String ipAddress = address.getAddress().getHostAddress();
                                        Short prefix = address.getNetworkPrefixLength();

                                        Log.i(Params.LOGGING_TAG, "Scan IP address for Z-Way Server: " + ipAddress);

                                        String subnet = ipAddress + "/" + prefix;
                                        SubnetUtils utils = new SubnetUtils(subnet);
                                        String[] addresses = utils.getInfo().getAllAddresses();

                                        setCount(addresses.length);

                                        for (final String addressInSubnet : addresses) {
                                            if (!isCancelled()){
                                                scanZWayServer(addressInSubnet);

                                                incrementChecked();
                                                publishProgress(mAddressChecked);
                                                Log.i(Params.LOGGING_TAG, "Current address: " + addressInSubnet + " (" + mAddressChecked + "/" + mAddressCount + ")");

                                                if (mAddressChecked.equals(mAddressCount)) {
                                                    mIsRunning = false;
                                                }
                                            } else {
                                                mIsRunning = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SocketException e) {
                        Log.w(Params.LOGGING_TAG, "Error occurred while searching Z-Way servers: " + e.getMessage());
                    }

                    return null;
                }

                protected void onProgressUpdate(Integer... progress) {
                    mListener.onZWayDiscoveryAddressChecked(progress[0]);
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (mAddressChecked < mAddressCount) {
                        mListener.onZWayDiscoveryAddressChecked(mAddressCount);
                    }
                }

                @Override
                protected void onCancelled() {
                    Log.i(Params.LOGGING_TAG, "Discovery cancelled.");
                }
            }.execute();
        } else {
            Log.i(Params.LOGGING_TAG, "Discovery is already running.");
        }
    }

    private boolean pingHost(String host, int port, int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
        return true;
    }

    private void scanZWayServer (String ipAddress) {
        if (!pingHost(ipAddress, 8083, 1000)) {
            return; // Error occurred while searching Z-Way servers (Unreachable)
        }

        try {
            URL url = new URL("http://" + ipAddress + ":8083/ZAutomation/api/v1/status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() == 401) {
                Log.i(Params.LOGGING_TAG, "(DiscoverySuccess) Z-Way server found on host: " + ipAddress);

                mListener.onZWayDiscoveryFound(ipAddress);
            } else {
                Log.i(Params.LOGGING_TAG, "No Z-Way server found on host: " + ipAddress);
            }

            connection.disconnect();
        } catch (Exception e) {
            Log.i(Params.LOGGING_TAG, "Z-Way server not found on host: " + ipAddress + " (" + e.getMessage() + ")");
        }
    }

    private class ValidateIPV4 {
        private final String ipV4Regex = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        private Pattern ipV4Pattern = Pattern.compile(ipV4Regex);

        boolean isValidIPV4(final String s) {
            return ipV4Pattern.matcher(s).matches();
        }
    }

    public interface ZWayDiscoveryInteractionListener {
        void onZWayDiscoveryFound(String ipAddress);
        void onZWayDiscoveryAddressCount(Integer addressCount);
        void onZWayDiscoveryAddressChecked(Integer progressCount);
    }
}
