package de.pathec.hubapp.model.protocol;

import android.content.Context;

import java.util.ArrayList;

import de.pathec.hubapp.BusProvider;
import de.pathec.hubapp.events.ModelAddEvent;
import de.pathec.hubapp.events.ModelUpdateEvent;
import de.pathec.hubapp.db.DatabaseHandler;

public class ProtocolList {
    private Context mContext;

    private DatabaseHandler mDatabaseHandler;

    public ProtocolList(Context context) {
        mContext = context;

        // SQLite
        mDatabaseHandler = DatabaseHandler.getInstance(mContext);
    }

    public ArrayList<ProtocolItem> getProtocolList(String status, String category) {
        return mDatabaseHandler.getAllProtocol(status, category);
    }

    public void addProtocolItem(ProtocolItem item) {
        mDatabaseHandler.addProtocol(item);
        BusProvider.postOnMain(new ModelAddEvent<>("Protocol", item));
    }

    public void deleteProtocolItem(ProtocolItem protocolItem) {
        mDatabaseHandler.deleteProtocol(protocolItem.getId());
    }

    public Integer getProtocolCount() {
        return mDatabaseHandler.getProtocolCount();
    }
}
