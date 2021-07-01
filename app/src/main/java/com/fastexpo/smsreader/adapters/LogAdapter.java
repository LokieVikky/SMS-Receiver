package com.fastexpo.smsreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.fastexpo.smsreader.R;
import com.fastexpo.smsreader.dataentitty.Message;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    List<Message> messageList;
    OnItemClickListener logClickListener;
    Context mContext;

    public LogAdapter(Context context,OnItemClickListener logClickListener) {
        messageList = new ArrayList<>();
        mContext = context;
        this.logClickListener = logClickListener;
    }

    public void setData(List<Message> messageList){
        this.messageList.clear();
        this.messageList.addAll(messageList);
        this.notifyDataSetChanged();
    }



    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log,parent,false);
        return new LogViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull LogAdapter.LogViewHolder holder, int position) {
        holder.txtDate.setText(messageList.get(position).receivedTime);
        holder.txtMessage.setText(messageList.get(position).messageText);
/*        if(messageList.get(position).isSent){
            holder.parentLayout.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        }else{
            holder.parentLayout.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        }*/
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class LogViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout parentLayout;
        TextView txtDate,txtMessage;


        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logClickListener.OnClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void OnClick(int pos);
    }

}
