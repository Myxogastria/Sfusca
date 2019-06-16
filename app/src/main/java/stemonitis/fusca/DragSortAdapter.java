package stemonitis.fusca;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DragSortAdapter extends RecyclerView.Adapter<DragSortViewHolder> {
    private List<Medium> media;
    private MediaSortActivity activity;

    public DragSortAdapter(List<Medium> media, MediaSortActivity activity){
        this.media = media;
        this.activity = activity;
    }

    @NonNull
    @Override
    public DragSortViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_dragsort_row, parent, false);
        return new DragSortViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DragSortViewHolder holder, final int position) {
        final RecyclerView.ViewHolder viewHolder = holder;
        holder.setText(media.get(position).getNameInSettings());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startSettings(viewHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

}
