package de.pathec.hubapp.util;

import android.content.Context;
import android.net.Uri;
import com.squareup.picasso.UrlConnectionDownloader;
import java.io.IOException;
import java.net.HttpURLConnection;

public  class CookieImageDownloader extends UrlConnectionDownloader{

    String mSessionId;
    String mRemoteSessionId;
    String mTopLevelUrl;

    public CookieImageDownloader(Context context, String sessionId, String remoteSessionId,
             String topLevelUrl) {
        super(context);

        mSessionId = sessionId;
        mRemoteSessionId = remoteSessionId;

        mTopLevelUrl = topLevelUrl;
    }

    public String getTopLevelUrl() {
        return mTopLevelUrl;
    }

    @Override
    protected HttpURLConnection openConnection(Uri path) throws IOException{
        HttpURLConnection conn = super.openConnection(path);

        if (!mSessionId.isEmpty())
            conn.setRequestProperty("Cookie","ZWAYSession" + "=" + mSessionId );
        if (!mRemoteSessionId.isEmpty())
            conn.setRequestProperty("Cookie","ZBW_SESSID" + "=" + mRemoteSessionId );

        return conn;
    }
}