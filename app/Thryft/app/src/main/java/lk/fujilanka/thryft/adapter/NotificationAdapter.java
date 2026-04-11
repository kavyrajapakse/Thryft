package lk.fujilanka.thryft.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.model.NotificationModel;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationModel> list;

    public NotificationAdapter(List<NotificationModel> list){
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title,message,time;

        View unreadDot;

        public ViewHolder(View view){
            super(view);

            title = view.findViewById(R.id.tv_notification_title);
            message = view.findViewById(R.id.tv_notification_message);
            time = view.findViewById(R.id.tv_notification_time);
            unreadDot = view.findViewById(R.id.view_unread_dot);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        NotificationModel notification = list.get(position);

        holder.title.setText(
                notification.getTitle() != null ? notification.getTitle() : "Notification"
        );

        holder.message.setText(
                notification.getMessage() != null ? notification.getMessage() : ""
        );

        long now = System.currentTimeMillis();
        long diff = now - notification.getTimestamp();

        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);

        if(minutes < 1){
            holder.time.setText("Just now");
        }else if(minutes < 60){
            holder.time.setText(minutes + " min ago");
        }else if(hours < 24){
            holder.time.setText(hours + " hrs ago");
        }else{
            holder.time.setText((hours/24) + " days ago");
        }

        if(notification.isRead()){
            holder.unreadDot.setVisibility(View.GONE);
        }else{
            holder.unreadDot.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount(){
        return list.size();
    }
}