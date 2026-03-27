package com.example.careyfem;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Map<String, String>> messages = new ArrayList<>();

    public void addMessage(Map<String, String> msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> msg = messages.get(position);
        holder.tvMessage.setText(msg.get("text"));
        holder.tvTime.setText(msg.get("time"));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.container.getLayoutParams();
        if ("user".equals(msg.get("sender"))) {
            params.gravity = Gravity.END;
            holder.container.setBackgroundResource(R.drawable.bubble_user);
        } else {
            params.gravity = Gravity.START;
            holder.container.setBackgroundResource(R.drawable.bubble_ai);
        }
        holder.container.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        LinearLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            container = itemView.findViewById(R.id.container);
        }
    }
}
