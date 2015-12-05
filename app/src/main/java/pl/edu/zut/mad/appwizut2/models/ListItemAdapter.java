package pl.edu.zut.mad.appwizut2.models;

import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.utils.FoldableTextView;

/**
 * Created by macko on 04.11.2015.
 */
public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder>{

    private List<ListItemContainer> listItem;

    public ListItemAdapter(List<ListItemContainer> listItem) {
        this.listItem = listItem;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);

        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, int position) {
        final ListItemContainer item = listItem.get(position);
        holder.vTitle.setText(item.getTitle());
        holder.vDate.setText(item.getDate());
        holder.vAuthor.setText(item.getAuthor());
        holder.vBody.setText(item.getBody());
        holder.vId = Integer.valueOf(item.getId());

        if (ListItemViewHolder.expandedViews.contains(holder.vId)){
            holder.vBody.setExpanded(true, false, holder.vSeeMore);
            holder.mExpanded = true;
            holder.vSeeMore.setVisibility(View.GONE);
        } else {
            holder.vBody.setExpanded(false, false, holder.vSeeMore);
            holder.mExpanded = false;
            holder.vSeeMore.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public static class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView vTitle;
        private TextView vDate;
        private TextView vAuthor;
        private FoldableTextView vBody;
        private TextView vSeeMore;
        private int vId;
        private boolean mExpanded;
        private static HashSet expandedViews = null;

        public ListItemViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);

            vTitle = (TextView) v.findViewById(R.id.title);
            vDate = (TextView) v.findViewById(R.id.date);
            vAuthor = (TextView) v.findViewById(R.id.author);
            vSeeMore = (TextView) v.findViewById(R.id.seeMore);
            vBody = (FoldableTextView) v.findViewById(R.id.body);
            vBody.setOnClickListener(this);
            vBody.setLinksClickable(true);
            vBody.setAutoLinkMask(Linkify.WEB_URLS);

            if (expandedViews == null)
                expandedViews = new HashSet();
        }

        @Override
        public void onClick(View v) {
            mExpanded = !mExpanded;
            vBody.setExpanded(mExpanded, true, vSeeMore);

            if (mExpanded) {
                expandedViews.add(vId);
            } else {
                expandedViews.remove(vId);
            }
        }
    }
}
