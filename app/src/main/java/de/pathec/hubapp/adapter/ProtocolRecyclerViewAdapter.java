package de.pathec.hubapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.pathec.hubapp.R;
import de.pathec.hubapp.fragments.ProtocolFragment;
import de.pathec.hubapp.model.protocol.ProtocolItem;
import de.pathec.hubapp.util.Params;

public class ProtocolRecyclerViewAdapter extends RecyclerView.Adapter<ProtocolRecyclerViewAdapter.ViewHolder> {

    private final List<ProtocolItem> mValues;
    private final ProtocolFragment.OnProtocolFragmentInteractionListener mListener;

    public ProtocolRecyclerViewAdapter(List<ProtocolItem> items, ProtocolFragment.OnProtocolFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_protocol, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mCreatedView.setText(Params.formatDateTimeGerman(mValues.get(position).getCreated()));

        String text = mValues.get(position).getText();
        if(text.trim().length() > 50) {
            text = text.substring(0, 46) + " ...";
        }

        holder.mTextView.setText(text);
        holder.sCategoryView.setText(mValues.get(position).getCategory());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onProtocolFragmentSelected(holder.mItem);
                }
            }
        });
    }

    // Clean all elements of the recycler
    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<ProtocolItem> values) {
        mValues.addAll(values);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mCreatedView;
        final TextView mTextView;
        final TextView sCategoryView;
        ProtocolItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCreatedView = (TextView) view.findViewById(R.id.protocol_item_created);
            mTextView = (TextView) view.findViewById(R.id.protocol_item_text);
            sCategoryView = (TextView) view.findViewById(R.id.protocol_item_category);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText() + "'";
        }
    }
}
