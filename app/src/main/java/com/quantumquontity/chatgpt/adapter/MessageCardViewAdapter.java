package com.quantumquontity.chatgpt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.quantumquontity.chatgpt.R;
import com.quantumquontity.chatgpt.dto.ChatMessageCardView;

import java.util.List;

public class MessageCardViewAdapter extends RecyclerView.Adapter<MessageCardViewAdapter.ViewHolder> {

    private static final String CODE_WRAPPING = "```";

    private List<ChatMessageCardView> mDataList;
    private LayoutInflater mInflater;
    private Context context;


    public MessageCardViewAdapter(Context context, List<ChatMessageCardView> data) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
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
        holder.cardWrapper.removeAllViews();
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        ChatMessageCardView data = mDataList.get(position);
        ImageView imageUserOrSystem = new ImageView(context);
        if(data.getUserRole().equals("user")){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F4EDE7"));
            imageUserOrSystem.setImageResource(android.R.drawable.ic_menu_help);
        }
        if(data.getUserRole().equals("system")){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            imageUserOrSystem.setImageResource(R.drawable.icon_system);
        }

        imageUserOrSystem.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        imageUserOrSystem.setPadding(0,0,40,0);

        holder.cardWrapper.setPadding(40,40,40,40);

        holder.cardWrapper.addView(imageUserOrSystem);
        createText(data.getText(), holder);
    }

    private void createText(String text, ViewHolder holder) {
        if(text.isEmpty()){
            return;
        }
        int codeStart = text.indexOf(CODE_WRAPPING);
        TextView textView = new TextView(context);
        textView.setTextSize(16);
        textView.setText(codeStart > 0 ? text.substring(0, codeStart) : text);
        holder.cardWrapper.addView(textView);
        if(codeStart > -1){
            createCode(text.substring(codeStart + 3), holder);
        }
    }

    private void createCode(String code, ViewHolder holder) {
        if(code.isEmpty()){
            return;
        }
        int textStart = code.indexOf(CODE_WRAPPING);
        CardView cardView = new CardView(context);
        cardView.setCardBackgroundColor(context.getResources().getColor(R.color.black));

        TextView textView = new TextView(context);
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setText(textStart > 0 ? code.substring(0, textStart) : code);
        textView.setTextSize(16);
        cardView.addView(textView);
        holder.cardWrapper.addView(cardView);
        if(textStart > -1){
            createText(code.substring(textStart + 3), holder);
        }
    }

    // Возвращает количество элементов в списке (карточек)
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    // Хранит и переиспользует представления элементов списка (карточек)
    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        LinearLayout cardWrapper;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            cardWrapper = itemView.findViewById(R.id.cardWrapper);
        }
    }
}