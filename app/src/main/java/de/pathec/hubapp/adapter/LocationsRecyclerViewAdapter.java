package de.pathec.hubapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.pathec.hubapp.R;
import de.pathec.hubapp.model.location.LocationItemApp;
import de.pathec.hubapp.util.recycler_view_helper.ItemTouchHelperAdapter;
import de.pathec.hubapp.util.recycler_view_helper.ItemTouchHelperViewHolder;
import de.pathec.hubapp.util.recycler_view_helper.OnStartDragListener;

public class LocationsRecyclerViewAdapter extends RecyclerView.Adapter<LocationsRecyclerViewAdapter.ViewHolder>
    implements ItemTouchHelperAdapter {

    private final Context mContext;

    private final List<LocationItemApp> mValues;
    private final OnLocationsAdapterInteractionListener mListener;
    private final OnStartDragListener mDragStartListener;

    public LocationsRecyclerViewAdapter(Context context, List<LocationItemApp> items, OnLocationsAdapterInteractionListener listener, OnStartDragListener dragStartListener) {
        mContext = context;
        mValues = items;
        mListener = listener;
        mDragStartListener = dragStartListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        if (holder.mItem.getId().equals(0)) {
            holder.mTitle.setText(mContext.getString(R.string.fragment_locations_global_room));
        } else {
            holder.mTitle.setText(holder.mItem.getTitle());
        }

        Picasso.with(mContext)
                .load(new File(holder.mItem.getTile()))
                .resize(100, 100)
                .into(holder.mTile);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onLocationsAdapterDetail(holder.mItem);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDragStartListener.onStartDrag(holder);
                return false;
            }
        });
    }

    // Clean all elements of the recycler
    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<LocationItemApp> values) {
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

    class ViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        private final View mView;
        private final TextView mTitle;
        private final ImageView mTile;
        private LocationItemApp mItem;

        private Drawable mDefaultBackground; // Store temporary the default background

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.location_item_title);
            mTile = (ImageView) view.findViewById(R.id.location_item_tile);

            mDefaultBackground = mView.getBackground();
        }

        @Override
        public void onItemSelected() {
            mView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.accent50));
        }

        @Override
        public void onItemClear() {
            mListener.onLocationsAdapterSwap(mValues);
            mView.setBackground(mDefaultBackground);
        }
    }

    public interface OnLocationsAdapterInteractionListener {
        void onLocationsAdapterDetail(LocationItemApp locationItem);
        void onLocationsAdapterSwap(List<LocationItemApp> list);
    }
}
