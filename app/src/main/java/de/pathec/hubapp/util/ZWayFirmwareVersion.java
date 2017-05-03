package de.pathec.hubapp.util;

import android.support.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZWayFirmwareVersion implements Comparable<ZWayFirmwareVersion> {
    private Integer mMajor;
    private Integer mMinor;
    private Integer mPatch;

    public ZWayFirmwareVersion(String firmwareVersion) {
        Matcher m = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(beta(\\d*))?")
                .matcher(firmwareVersion);
        if (!m.matches())
            throw new IllegalArgumentException("Malformed firmware version");

        mMajor = Integer.parseInt(m.group(1));
        mMinor = Integer.parseInt(m.group(2));
        mPatch = Integer.parseInt(m.group(3));
    }

    @Override
    public String toString() {
        return "Major: " + mMajor + "\n" + "Minor: " + mMinor + "\n" + "Patch: " + mPatch;
    }

    @Override
    public int compareTo(@NonNull ZWayFirmwareVersion that) {
        if(that == null)
            return 1;

        if (this.mMajor < that.mMajor) {
            return -1;
        } else if (this.mMajor > that.mMajor) {
            return 1;
        }

        if (this.mMinor < that.mMinor) {
            return -1;
        } else if (this.mMinor > that.mMinor) {
            return 1;
        }
        if (this.mPatch < that.mPatch) {
            return -1;
        } else if (this.mPatch > that.mPatch) {
            return 1;
        }

        return 0;
    }
}
