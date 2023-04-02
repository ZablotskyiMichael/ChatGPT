package com.quantumquontity.chatgpt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.quantumquontity.chatgpt.R;
import com.quantumquontity.chatgpt.dto.ChatMessageCardView;

import java.util.List;

public class MessageCardViewAdapter extends RecyclerView.Adapter<MessageCardViewAdapter.ViewHolder> {

    private List<ChatMessageCardView> mDataList;
    private LayoutInflater mInflater;


    public MessageCardViewAdapter(Context context, List<ChatMessageCardView> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mDataList = data;
    }

    public void refreshData(List<ChatMessageCardView> mDataList){
        this.mDataList = mDataList;
        notifyDataSetChanged();
    }

    public void addItem(ChatMessageCardView item){
        mDataList.add(item);
        notifyItemInserted(mDataList.size() - 1);
    }

    public void updateLastItemText(String newText){
        this.mDataList.get(mDataList.size() - 1).setText(newText);
        notifyItemChanged(mDataList.size() - 1);
    }

    // Создаем новый элемент списка (карточку) и связываем с ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.cardview_item, parent, false);
        return new ViewHolder(view);
    }

    // Связываем данные с элементом списка (карточкой)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessageCardView data = mDataList.get(position);
        holder.messageText.setText(data.getText());
    }

    // Возвращает количество элементов в списке (карточек)
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    // Хранит и переиспользует представления элементов списка (карточек)
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}