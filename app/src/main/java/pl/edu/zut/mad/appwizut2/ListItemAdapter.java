package pl.edu.zut.mad.appwizut2;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;

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
                .inflate(R.layout.item_layout, parent, false);

        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        ListItemContainer item = listItem.get(position);
        holder.vTitle.setText(item.getTitle());
        holder.vDate.setText(item.getDate());
        holder.vAuthor.setText(item.getAuthor());
        holder.vBody.setText(item.getBody());

        if (holder.vBody.getTag() == null) {
            holder.vBody.setTag(position);
        }
        if (ListItemViewHolder.expandedViews.contains(holder.vDate.getText())) {
            holder.vBody.setExpanded(true, false);
        } else {
            holder.vBody.setExpanded(false, false);
        }
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public static class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView vTitle;
        private TextView vDate;
        private TextView vAuthor;
        private FoldableTextView vBody;
        private boolean mExpanded;
        private static HashSet expandedViews = null;

        public ListItemViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            vTitle = (TextView) v.findViewById(R.id.title);
            vDate = (TextView) v.findViewById(R.id.date);
            vAuthor = (TextView) v.findViewById(R.id.author);
            vBody = (FoldableTextView) v.findViewById(R.id.body);

            if (expandedViews == null)
                expandedViews = new HashSet();
        }


        @Override
        public void onClick(View v) {
         //   Log.e(TAG, "onClick tag = " + vBody.getTag());
            mExpanded = !mExpanded;

            if (mExpanded) {
                expandedViews.add(vDate.getText());
            } else {
                expandedViews.remove(vDate.getText());
            }
            vBody.setExpanded(mExpanded, true);
        }
    }
}
