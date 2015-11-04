package pl.edu.zut.mad.appwizut2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public static class ListItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vDate;
        protected TextView vAuthor;
        protected TextView vBody;

        public ListItemViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.title);
            vDate = (TextView) v.findViewById(R.id.date);
            vAuthor = (TextView) v.findViewById(R.id.author);
            vBody = (TextView) v.findViewById(R.id.body);
          }

   }
}
