package stemonitis.fusca;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DragSortViewHolder extends RecyclerView.ViewHolder {
    private TextView tv;

    public DragSortViewHolder(@NonNull View itemView){
        super(itemView);
        tv = itemView.findViewById(R.id.rowTextView);
    }

    public void setText(String text){
        tv.setText(text);
    }
}
