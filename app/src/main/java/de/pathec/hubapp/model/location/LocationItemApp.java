package de.pathec.hubapp.model.location;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

import de.fh_zwickau.informatik.sensor.model.locations.Location;
import de.pathec.hubapp.R;
import de.pathec.hubapp.util.Params;

public class LocationItemApp extends Location {

    private Integer mHubId;
    private String mTile;
    private Integer mOrder;

    private Context mContext;

    public LocationItemApp(Context context, LocationItemApp item) {
        super();

        mContext = context;

        setId(item.getId());
        mHubId = item.getHubId();
        setTitle(item.getTitle());
        mTile = item.getTile();
        setUserImg(item.getUserImg());
        mOrder = item.getOrder();

    }

    public LocationItemApp(Context context, Location location, Integer hubId) {
        mContext = context;

        setId(location.getId());
        setTitle(location.getTitle());
        setUserImg(location.getUserImg());

        mHubId = hubId;
        mTile = "";
        mOrder = 0;
    }

    public LocationItemApp(Context context) {
        super();

        mContext = context;

        setId(-1);
        this.mHubId = -1;
        setTitle("");
        this.mTile = "";
        setUserImg("");
        this.mOrder = 0;
    }

    public LocationItemApp(Context context, Integer id, Integer hubId, String title, String tile, String userImg, Integer order) {
        super();

        mContext = context;

        setId(id);
        this.mHubId = hubId;
        setTitle(title);
        this.mTile = tile;
        setUserImg(userImg);
        this.mOrder = order;

    }

    public Integer getHubId() {
        return mHubId;
    }

    public void setHubId(Integer hubId) {
        this.mHubId = hubId;
    }

    public String getTile() {
        return mTile;
    }

    public Bitmap getTileBitmap() {
        Bitmap tileBitmap;

        File tileFile = new File(mTile);
        if (tileFile.exists()) {
            try {
                tileBitmap = BitmapFactory.decodeFile(tileFile.getAbsolutePath());
            } catch (OutOfMemoryError oom) {
                Log.i(Params.LOGGING_TAG, "Unexpected exception during loading location tile!");
                tileBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_tile_placeholder);
            }
        } else {
            Log.i(Params.LOGGING_TAG, "Location tile file doesn't exist (" + mTile + ")!");
            tileBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_tile_placeholder);
        }
        return tileBitmap;
    }

    public void setTile(String tile) {
        this.mTile = tile;
    }

    public Integer getOrder() {
        return mOrder;
    }

    public void setOrder(Integer order) {
        this.mOrder = order;
    }

    @Override
    public String toString() {
        return "LocationItemApp{" +
                "location=" + super.toString() +
                ", mHubId=" + mHubId +
                ", mTile='" + mTile + '\'' +
                ", mOrder=" + mOrder +
                ", mContext=" + mContext +
                '}';
    }
}
