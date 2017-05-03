package de.pathec.hubapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.pathec.hubapp.R;
import de.pathec.hubapp.model.notification.NotificationItemApp;
import de.pathec.hubapp.util.Params;
import de.pathec.hubapp.util.recycler_view_helper.ItemTouchHelperAdapter;
import de.pathec.hubapp.util.recycler_view_helper.ItemTouchHelperViewHolder;

public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.ViewHolder>
    implements ItemTouchHelperAdapter {

    private final Context mContext;

    private final List<NotificationItemApp> mValues;
    private final OnNotificationsAdapterInteractionListener mListener;

    public NotificationsRecyclerViewAdapter(Context context, List<NotificationItemApp> items, OnNotificationsAdapterInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        // TODO different string resource depending on level
        String message = "Unknown notification type";
        if (holder.mItem.getLevel().equals("device-info")) {
            message = mContext.getString(R.string.fragment_notification_device_info_message,
                    holder.mItem.getMessage().getDev(), holder.mItem.getMessage().getL());
        }

        if(message.trim().length() > 50) {
            message = message.substring(0, 46) + " ...";
        }

        holder.mMessage.setText(message);
        // TODO different format if today or older, ...
        holder.mTimestamp.setText(Params.formatTimeShort(holder.mItem.getTimestamp(), true));

        // normal tap for notification detail
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNotificationAdapterDetail(holder.mItem);
            }
        });

        // long tap for selecting notification type or source as filter (with dialog)
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onNotificationAdapterUseNotificationAsFilter(holder.mItem);
                return true;
            }
        });
    }

    // Clean all elements of the recycler
    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    // add a list of items
    public void addAll(List<NotificationItemApp> values) {
        mValues.addAll(values);
        notifyDataSetChanged();
    }

    public void add(NotificationItemApp value) {
        mValues.add(0, value);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // do nothing
        return false;
    }

    @Override
    public void onItemSwipe(int position, int direction) {
        if (mListener.onNotificationsAdapterDelete(mValues.get(position))) {
            mValues.remove(position);
            notifyItemRemoved(position);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        private final View mView;
        private final TextView mTimestamp;
        private final TextView mMessage;
        private NotificationItemApp mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mMessage = (TextView) view.findViewById(R.id.notification_item_message);
            mTimestamp = (TextView) view.findViewById(R.id.notification_item_timestamp);
        }

        @Override
        public void onItemSelected() { }

        @Override
        public void onItemClear() { }
    }

    public interface OnNotificationsAdapterInteractionListener {
        Boolean onNotificationsAdapterDelete(NotificationItemApp notificationItem);
        void onNotificationAdapterUseNotificationAsFilter(NotificationItemApp notificationItem);
        void onNotificationAdapterDetail(NotificationItemApp notificationItem);
    }
}
