package com.transitangel.transitangel.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.transitangel.transitangel.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author yvastavaus.
 */
public class RecentSearchItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    @BindView(R.id.tvFrom)
    TextView tvFrom;
    @BindView(R.id.ivIcon)
    ImageView ivIcon;
    @BindView(R.id.tvTo)
    TextView tvTo;
    @BindView(R.id.more)
    ImageView more;

    View parent;

    private final RecentAdapter.OnItemClickListener onItemClickListener;
    private final RecentAdapter.OnMoreMenuClickListener onMoreMenuClickListener;

    public RecentSearchItemViewHolder(View itemView, RecentAdapter.OnItemClickListener onItemClickListener, RecentAdapter.OnMoreMenuClickListener onMoreMenuClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.parent = itemView;
        this.onItemClickListener = onItemClickListener;
        this.onMoreMenuClickListener = onMoreMenuClickListener;
        //more.setOnClickListener(this);
        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.more) {
            onMoreMenuClickListener.onMenuItemClicked(getLayoutPosition(), view);
        } else {
            onItemClickListener.onItemClick(getLayoutPosition());
        }
    }
}
