package de.pathec.hubapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.pathec.hubapp.R;
import de.pathec.hubapp.model.help.Help;

public class HelpRecyclerViewAdapter extends RecyclerView.Adapter<HelpRecyclerViewAdapter.ViewHolder> {

    private final Context mContext;

    private final List<Help> mValues;
    private final OnHelpAdapterInteractionListener mListener;

    public HelpRecyclerViewAdapter(Context context, List<Help> items, OnHelpAdapterInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.help_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mTitle.setText(holder.mItem.getTitle());
        holder.mSubtitle.setText(holder.mItem.getSubtitle());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHelpAdapterDetail(holder.mItem);
            }
        });
    }

    // Clean all elements of the recycler
    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Help> values) {
        mValues.addAll(values);
        notifyDataSetChanged();
    }

    public void add(Help value) {
        mValues.add(0, value);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final View mView;
        private final TextView mTitle;
        private final TextView mSubtitle;
        private Help mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.help_item_title);
            mSubtitle = (TextView) view.findViewById(R.id.help_item_subtitle);
        }
    }

    public interface OnHelpAdapterInteractionListener {
        void onHelpAdapterDetail(Help help);
    }
}
