package com.transitangel.transitangel.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.List;

/**
 * author yvastavaus.
 */
public class RecentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int RECENT_TRIP_ITEM_TYPE = 1;
    public static final int RECENT_SEARCH_ITEM_TYPE = 2;
    public static final int RECENT_TRIP_ITEM_HEADER_TYPE = 3;
    public static final int RECENT_SEARCH_ITEM_HEADER_TYPE = 4;
    public static final int RECENT_TRIP_ITEM_VIEW_MORE_TYPE = 5;

    private int RECENT_TRIP_LIST;
    private int RECENT_TRIP_HEADER;
    private int RECENT_TRIP_VIEW_MORE;
    private int RECENT_SEARCH_HEADER;
    private int RECENT_SEARCH_LIST;

    private List<Trip> recentTripItemList;
    private List<Trip> recentSearchItemList;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnMoreMenuClickListener {
        void onMenuItemClicked(int position, View view);
    }

    private OnItemClickListener onItemClickListener;
    private OnMoreMenuClickListener onMoreMenuClickListener;

    public RecentAdapter(Context context, @NonNull List<Trip> recentTripItemList,@NonNull List<Trip> recentSearchItemList) {
        this.recentTripItemList = recentTripItemList;
        this.recentSearchItemList = recentSearchItemList;
        updateTotalCounts();
        this.context = context;
    }

    public int getTripsCount() {
        return RECENT_TRIP_HEADER + RECENT_TRIP_LIST + RECENT_TRIP_VIEW_MORE;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnMoreMenuClickListener(OnMoreMenuClickListener onMoreMenuClickListener) {
        this.onMoreMenuClickListener = onMoreMenuClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder;
        if(viewType == RECENT_TRIP_ITEM_HEADER_TYPE || viewType == RECENT_SEARCH_ITEM_HEADER_TYPE) {
            View view = inflater.inflate(R.layout.item_recent_header, parent, false);
            viewHolder = new RecentHeaderViewHolder(view);
        } else if(viewType == RECENT_TRIP_ITEM_TYPE) {
            View view = inflater.inflate(R.layout.item_recents_trip, parent, false);
            viewHolder = new RecentTripItemViewHolder(view, onItemClickListener, onMoreMenuClickListener);
        } else if(viewType == RECENT_TRIP_ITEM_VIEW_MORE_TYPE) {
            View view = inflater.inflate(R.layout.item_see_all_trips, parent, false);
            viewHolder = new SeeAllRecentTripViewHolder(view, onItemClickListener);
        } else {
            View view = inflater.inflate(R.layout.item_search_recent_item, parent, false);
            viewHolder = new RecentSearchItemViewHolder(view, onItemClickListener, onMoreMenuClickListener);
        }
        return viewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        if(recentTripItemList.size() > 0) {
            if (position == 0) {
                return RECENT_TRIP_ITEM_HEADER_TYPE;
            }
            // Show only 3 items
            if(recentTripItemList.size() > 3) {
                if (position <= 3) {
                    return RECENT_TRIP_ITEM_TYPE;
                }

                if (position == 4) {
                    return RECENT_TRIP_ITEM_VIEW_MORE_TYPE;
                }
            } else {
                // Means that recent trip has less than 3 items.
                // So return all items as trip items for the remaining list.
                if(position <= recentTripItemList.size()) {
                    return RECENT_TRIP_ITEM_TYPE;
                }
            }
        }

        if (position == RECENT_TRIP_HEADER + RECENT_TRIP_LIST + RECENT_TRIP_VIEW_MORE) {
            return RECENT_SEARCH_ITEM_HEADER_TYPE;
        }

        return RECENT_SEARCH_ITEM_TYPE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if(viewType == RECENT_TRIP_ITEM_HEADER_TYPE) {
            RecentHeaderViewHolder headerViewHolder = (RecentHeaderViewHolder) holder;
            headerViewHolder.header.setText("Recent Trips");
        } else if (viewType == RECENT_SEARCH_ITEM_HEADER_TYPE) {
            RecentHeaderViewHolder headerViewHolder = (RecentHeaderViewHolder) holder;
            headerViewHolder.header.setText("Recent Searches");
        } else  if(viewType == RECENT_TRIP_ITEM_TYPE) {
            RecentTripItemViewHolder recentItemViewHolder = (RecentTripItemViewHolder)holder;
            position = getRecentTripPosition(position);
            Trip currentTrip = recentTripItemList.get(position);
            Stop fromStop = currentTrip.getFromStop();
            Stop toStop = currentTrip.getToStop();
            Train selectedTrain = currentTrip.getSelectedTrain();
            recentItemViewHolder.tvTripTo.setText(context.getString(R.string.trip_from_to, fromStop.getName(), toStop.getName()));
            recentItemViewHolder.tvTrainInfo.setText(context.getString(R.string.trip_train_number, selectedTrain.getNumber(), selectedTrain.getTrainStop(fromStop.getId()).getArrrivalTime()));
            recentItemViewHolder.parent.setContentDescription(context.getString(R.string.contentdescription_recent_trip, fromStop.getName(), toStop.getName(),selectedTrain.getNumber(), selectedTrain.getTrainStop(fromStop.getId()).getArrrivalTime()));
            if(currentTrip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                recentItemViewHolder.ivIcon.setImageResource(R.drawable.train_blue);
            }
        } else if(viewType == RECENT_TRIP_ITEM_VIEW_MORE_TYPE) {
            // Nothing to set here.
        } else {
            position = getSearchListPosition(position);
            RecentSearchItemViewHolder recentItemViewHolder = (RecentSearchItemViewHolder)holder;
            Trip currentTrip = recentSearchItemList.get(position);
            recentItemViewHolder.tvFrom.setText(currentTrip.getFromStop().getName() + context.getString(R.string.recent_search_to));
            recentItemViewHolder.tvTo.setText(currentTrip.getToStop().getName());
            recentItemViewHolder.parent.setContentDescription(context.getString(R.string.contentdescription_from_to, currentTrip.getFromStop().getName(), currentTrip.getToStop().getName()));
            if(currentTrip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                recentItemViewHolder.ivIcon.setImageResource(R.drawable.train_blue);
            }
        }

    }

    public int getRecentTripPosition(int position) {
        // Subtract the header
        position = position -1;
        return position;
    }

    public int getSearchListPosition(int position) {
        // Subtract the header and recent trip count.
        position = position - getTripsCount() - 1;
        return  position;
    }

    @Override
    public int getItemCount() {
        int total = RECENT_TRIP_HEADER + RECENT_TRIP_LIST + RECENT_TRIP_VIEW_MORE + RECENT_SEARCH_HEADER + RECENT_SEARCH_LIST;
        return total;
     }


    public void updateData(@NonNull List<Trip> recentTripItemList,@NonNull List<Trip> recentSearchItemList) {
        this.recentTripItemList = recentTripItemList;
        this.recentSearchItemList = recentSearchItemList;
        updateTotalCounts();
        notifyDataSetChanged();
    }

    private void updateTotalCounts() {
        RECENT_TRIP_HEADER = recentTripItemList.size() != 0 ? 1 : 0;
        RECENT_TRIP_LIST = recentTripItemList.size();
        if(recentTripItemList.size() > 3) {
            RECENT_TRIP_VIEW_MORE = 1;
            RECENT_TRIP_LIST = 3;
        }
        RECENT_SEARCH_HEADER = 1;
        RECENT_SEARCH_LIST = recentSearchItemList.size();
    }
}
