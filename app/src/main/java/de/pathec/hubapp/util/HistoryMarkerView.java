package de.pathec.hubapp.util;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import de.pathec.hubapp.R;

public class HistoryMarkerView extends MarkerView {
    private TextView tvContent;
    private NumberFormat mFormatter;

    public HistoryMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        mFormatter = DecimalFormat.getInstance(Locale.getDefault());
        mFormatter.setMaximumFractionDigits(1);
        mFormatter.setMinimumFractionDigits(1);

        // find your layout components
        tvContent = (TextView) findViewById(R.id.history_marker_txt);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        tvContent.setText(mFormatter.format(e.getY()));

        super.refreshContent(e, highlight);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }
}
