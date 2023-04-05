package com.quantumquontity.chatgpt.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        RelativeLayout linearLayout = new RelativeLayout(context);
        ChatMessageCardView data = mDataList.get(position);
        ImageView imageUserOrSystem = new ImageView(context);
        ImageView imageCopyMessage = new ImageView(context);
        TextView nameUserOrSystem = new TextView(context);
        nameUserOrSystem.setTextColor(Color.BLACK);
        nameUserOrSystem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        if(data.getUserRole().equals("user")){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F4EDE7"));
            imageUserOrSystem.setImageResource(R.drawable.user_icon);
            nameUserOrSystem.setText("You");
        }
        if(data.getUserRole().equals("system")){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            imageUserOrSystem.setImageResource(R.drawable.icon_system);
            imageCopyMessage.setImageResource(R.drawable.image_button_copy);
            nameUserOrSystem.setText("CatGPT");
        }

        RelativeLayout.LayoutParams layoutParams1 =  new RelativeLayout.LayoutParams(150,150);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_START);
        imageUserOrSystem.setLayoutParams(layoutParams1);
        imageUserOrSystem.setPadding(0,0,40,0);

        RelativeLayout.LayoutParams layoutParams2 =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
        nameUserOrSystem.setLayoutParams(layoutParams2);
        nameUserOrSystem.setPadding(0,0,40,0);

        RelativeLayout.LayoutParams layoutParams3 =  new RelativeLayout.LayoutParams(100,100);
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_END);
        imageCopyMessage.setLayoutParams(layoutParams3);
        imageCopyMessage.setPadding(0,0,40,0);
        imageCopyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", data.getText());
                clipboard.setPrimaryClip(clipData);
            }
        });

        linearLayout.addView(imageUserOrSystem);
        linearLayout.addView(nameUserOrSystem);
        linearLayout.addView(imageCopyMessage);

        holder.cardWrapper.setPadding(30,30,30,30);

        holder.cardWrapper.addView(linearLayout);
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