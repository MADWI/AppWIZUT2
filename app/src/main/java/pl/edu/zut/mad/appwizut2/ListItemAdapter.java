package pl.edu.zut.mad.appwizut2;

import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    public static class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView vTitle;
        private TextView vDate;
        private TextView vAuthor;
        private TextView vBody;

        public ListItemViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            vTitle = (TextView) v.findViewById(R.id.title);
            vDate = (TextView) v.findViewById(R.id.date);
            vAuthor = (TextView) v.findViewById(R.id.author);
            vBody = (TextView) v.findViewById(R.id.body);
          }

        @Override
        public void onClick(View v) {

            vBody.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            Log.i("Height measure", "" + vBody.getMeasuredHeight());

            ValueAnimator anim;
            final int MIN_BODY_HEIGHT = (int) vBody.getContext().getResources().getDimension(R.dimen.body_of_card_min_height);
            if (vBody.getHeight() == MIN_BODY_HEIGHT) {
                anim = ValueAnimator.ofInt(vBody.getHeight(), vBody.getMeasuredHeight());
            } else {
                anim = ValueAnimator.ofInt(vBody.getHeight(), MIN_BODY_HEIGHT);
            }
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = vBody.getLayoutParams();
                    layoutParams.height = val;
                    vBody.setLayoutParams(layoutParams);
                }
            });

            anim.setDuration(500);
            anim.start();
        }
    }
}
