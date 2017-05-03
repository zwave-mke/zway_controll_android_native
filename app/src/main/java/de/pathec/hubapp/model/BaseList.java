package de.pathec.hubapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import de.pathec.hubapp.db.DatabaseHandler;
import de.pathec.hubapp.util.Util;

public class BaseList {
    protected  MaterialDialog mProgressDialog;
    protected Handler mUpdateProgressDialogHandler;

    protected Context mContext;

    protected Boolean mShowLoadingDialogs = false;

    protected DatabaseHandler mDatabaseHandler;

    public BaseList(Context context, Boolean showLoadingDialogs) {
        mContext = context;

        // sqllite
        mDatabaseHandler = DatabaseHandler.getInstance(mContext);

        mShowLoadingDialogs = showLoadingDialogs;
        if(mShowLoadingDialogs) // Can't create handler inside thread that has not called Looper.prepare() - GCM Operation
            mUpdateProgressDialogHandler = new Handler();
    }

    /**
     * show loading dialog and creates it if necessary.
     * Code is AsyncTask safety!
     *
     * @param title - title of progress dialog
     * @param message - message of progress dialog
     * @param progress - show progress
     * @param max - max progress
     */
    protected void showLoadingDialog(final String title, final String message, final Boolean progress, final int max) {
        if(mShowLoadingDialogs) {
            mUpdateProgressDialogHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }

                    // create dialog if necessary
                    if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                        if(progress) {
                            mProgressDialog = new MaterialDialog.Builder(mContext)
                                    .title(title)
                                    .content(message)
                                    .progress(false, max).build();
                        } else {
                            mProgressDialog = new MaterialDialog.Builder(mContext)
                                    .title(title)
                                    .content(message)
                                    .progress(true, 0).build();
                        }

                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setCanceledOnTouchOutside(false);
                    }

                    mProgressDialog.show();
                }
            });
        }
    }

    protected void updateLoadingDialog(final String message, final Integer actProgress) {
        if(mShowLoadingDialogs) {
            mUpdateProgressDialogHandler.post(new Runnable() {
                @Override
                public void run() {
                    // create dialog if necessary
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        // update values
                        mProgressDialog.setContent(message);
                        mProgressDialog.incrementProgress(actProgress);
                    }
                }
            });
        }
    }

    /**
     * Dismiss loading dialog if necessary.
     * Code is AsyncTask safety!
     */
    protected void dismissLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            if(mShowLoadingDialogs) {
                mUpdateProgressDialogHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                });
            }
        }
    }

    /**
     * Decorates the default show message method with AsyncTask safety handler.
     *
     * @param text - text of toast
     */
    protected void showMessage(final String text) {
        if(mShowLoadingDialogs) {
            mUpdateProgressDialogHandler.post(new Runnable() {
                @Override
                public void run() {
                    Util.showMessage(mContext, text);
                }
            });
        }
    }

    /**
     * Download a image file from passed url.
     *
     * @param url - image url
     * @return bitmap
     */
    protected Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        InputStream stream;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;

        try {
            stream = getHttpConnection(url);
            bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Needed for downloading image.
     *
     * @param urlString - url
     * @return stream
     * @throws IOException
     */
    protected InputStream getHttpConnection(String urlString) throws IOException {
        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stream;
    }

    /**
     * Save bitmap to internal app storage.
     *
     * @param bitmap - bitmap object
     * @param filename - filename without extension
     * @return filename.extension
     */
    protected String saveImage(Bitmap bitmap, String filename) {
        String fileName = "";

        if (bitmap != null) {
            try {
                File directory = mContext.getFilesDir();
                fileName = filename.toLowerCase() + ".jpg";

                File file = new File(directory, fileName);

                FileOutputStream stream = new FileOutputStream(file);

                // here we Resize the Image ...
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
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
}
