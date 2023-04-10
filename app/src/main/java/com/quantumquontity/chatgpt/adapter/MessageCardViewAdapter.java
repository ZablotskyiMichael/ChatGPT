package com.quantumquontity.chatgpt.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.quantumquontity.chatgpt.R;
import com.quantumquontity.chatgpt.dto.ChatMessageCardView;

import java.util.List;

public class MessageCardViewAdapter extends RecyclerView.Adapter<MessageCardViewAdapter.ViewHolder> {

    private static final String CODE_WRAPPING = "```";

    private List<ChatMessageCardView> mDataList;
    private LayoutInflater mInflater;
    private Context context;
    private ViewHolder lastHolder;


    public MessageCardViewAdapter(Context context, List<ChatMessageCardView> data) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mDataList = data;
    }

    public void refreshData(List<ChatMessageCardView> mDataList) {
        this.mDataList = mDataList;
        notifyDataSetChanged();
    }

    public int addItem(ChatMessageCardView item) {
        mDataList.add(item);
        notifyItemInserted(mDataList.size() - 1);
        return mDataList.size() - 1;
    }

    public void updateLastItemText(String newText) {
        if (!newText.isEmpty()) {
            ChatMessageCardView messageCardView = mDataList.get(mDataList.size() - 1);
            messageCardView.setText(messageCardView.getText() + newText);
            /*TextView currentTextOrCodeView = lastHolder.currentTextOrCodeView;
            System.out.println("lastHolder.getId() = " + lastHolder.id + "; text = " + (currentTextOrCodeView != null ? currentTextOrCodeView.getText() + newText : newText));
            if (lastHolder.textNow) {
                createText(currentTextOrCodeView != null ? currentTextOrCodeView.getText() + newText : newText, currentTextOrCodeView, lastHolder);
            } else {
                createCode(currentTextOrCodeView != null ? currentTextOrCodeView.getText() + newText : newText, currentTextOrCodeView, lastHolder);
            }*/
            notifyItemChanged(mDataList.size() - 1);
        }
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
        holder.cardWrapper.removeAllViews();
        initNewItem(holder, data);
        System.out.println("onBindViewHolder: " + data.getId() + "; " + data.getText());
        createText(data.getText(), null, holder);
        if (mDataList.size() - 1 == position) {
            lastHolder = holder;
        }
    }

    private void initNewItem(ViewHolder holder, ChatMessageCardView data) {
        holder.id = data.getId();
        RelativeLayout linearLayout = new RelativeLayout(context);
        ImageView imageUserOrSystem = new ImageView(context);
        ImageView imageCopyMessage = new ImageView(context);
        TextView nameUserOrSystem = new TextView(context);
        nameUserOrSystem.setTextColor(Color.BLACK);
        nameUserOrSystem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        if (data.getUserRole().equals("user")) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F4EDE7"));
            imageUserOrSystem.setImageResource(R.drawable.user);
            nameUserOrSystem.setText(R.string.you);
        }
        if (data.getUserRole().equals("system")) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            imageUserOrSystem.setImageResource(R.drawable.icon_system);
            imageCopyMessage.setImageResource(R.drawable.image_button_copy);
            nameUserOrSystem.setText(R.string.cat_gtp);
        }

        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(150, 150);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_START);
        imageUserOrSystem.setLayoutParams(layoutParams1);
        imageUserOrSystem.setPadding(0, 0, 40, 0);

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
        nameUserOrSystem.setLayoutParams(layoutParams2);
        nameUserOrSystem.setPadding(0, 0, 40, 0);

        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(100, 100);
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_END);
        imageCopyMessage.setLayoutParams(layoutParams3);

        imageCopyMessage.setPadding(0, 0, 40, 0);
        imageCopyMessage.setOnClickListener(v -> {
            Toast.makeText(context, context.getText(R.string.messageCopied), Toast.LENGTH_SHORT).show();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("label", data.getText());
            clipboard.setPrimaryClip(clipData);
        });

        linearLayout.addView(imageUserOrSystem);
        linearLayout.addView(nameUserOrSystem);
        linearLayout.addView(imageCopyMessage);

        holder.cardWrapper.setPadding(30, 30, 30, 30);

        holder.cardWrapper.addView(linearLayout);
    }

    private void createText(String text, TextView textView, ViewHolder holder) {
        if (text.isEmpty()) {
            holder.startWrite(context);
            return;
        } else {
            holder.stopWrite();
        }
        if (textView == null) {
            textView = new TextView(context);
            textView.setTextSize(16);
            holder.cardWrapper.addView(textView);
        }

        int codeStart = text.indexOf(CODE_WRAPPING);
        if(codeStart == 0){
            createCode(text.substring(codeStart + 3), null, holder);
            return;
        }

        textView.setText(codeStart > 0 ? text.substring(0, codeStart) : text);
        holder.currentTextOrCodeView = textView;
        if (codeStart > -1) {
            createCode(text.substring(codeStart + 3), null, holder);
        } else {
            holder.textNow = true;
        }
    }

    private void createCode(String code, TextView textView, ViewHolder holder) {
        holder.textNow = false;
        if (textView == null) {
            CardView cardView = new CardView(context);
            RelativeLayout relativeLayout = new RelativeLayout(context);
            ImageView imageCopyMessage = new ImageView(context);
            TextView textCopyMessage = new TextView(context);
            TextView textProgramingLanguage = new TextView(context);

            imageCopyMessage.setImageResource(R.drawable.image_button_copy);
            relativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.panelCardView));
            textCopyMessage.setText(R.string.copy_code);
            textCopyMessage.setTextColor(Color.parseColor("#d9d9e3"));

            // Текст языка на котором написан код
            int firstLineEnd = code.indexOf("\n");
            textProgramingLanguage.setText(firstLineEnd > -1 ? code.substring(0, firstLineEnd) : "");
            textProgramingLanguage.setTextColor(Color.parseColor("#d9d9e3"));

            RelativeLayout.LayoutParams layoutParamsLanguage = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100);
            layoutParamsLanguage.addRule(RelativeLayout.ALIGN_PARENT_START);
            textProgramingLanguage.setPadding(20, 20, 20, 20);
            textProgramingLanguage.setLayoutParams(layoutParamsLanguage);
            relativeLayout.addView(textProgramingLanguage);

            RelativeLayout.LayoutParams layoutParamsImageCopy = new RelativeLayout.LayoutParams(100, 100);
            layoutParamsImageCopy.addRule(RelativeLayout.ALIGN_PARENT_END);
            imageCopyMessage.setPadding(20, 20, 20, 20);
            imageCopyMessage.setLayoutParams(layoutParamsImageCopy);

            int textStart = code.indexOf(CODE_WRAPPING);
            String currentCode = textStart > 0 ?
                    code.substring(firstLineEnd > 0 ? firstLineEnd + 1 : 0, textStart) :
                    firstLineEnd > 0 ? code.substring(firstLineEnd + 1) : code;

            textCopyMessage.setOnClickListener(v -> {
                Toast.makeText(context, context.getText(R.string.messageCopied), Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", currentCode);
                clipboard.setPrimaryClip(clipData);
            });
            relativeLayout.addView(imageCopyMessage);

            RelativeLayout.LayoutParams layoutParamsTextCopy = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100);
            layoutParamsTextCopy.addRule(RelativeLayout.ALIGN_PARENT_END);
            textCopyMessage.setPadding(0, 20, 120, 20);
            textCopyMessage.setLayoutParams(layoutParamsTextCopy);

            imageCopyMessage.setOnClickListener(v -> {
                Toast.makeText(context, context.getText(R.string.messageCopied), Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", currentCode);
                clipboard.setPrimaryClip(clipData);
            });

            relativeLayout.addView(textCopyMessage);
            holder.cardWrapper.addView(relativeLayout);

            cardView.setCardBackgroundColor(context.getResources().getColor(R.color.black));
            holder.cardWrapper.addView(cardView);

            textView = new TextView(context);
            textView.setPadding(16, 40, 8, 8);
            textView.setTextColor(context.getResources().getColor(R.color.white));
            textView.setTextSize(16);
            cardView.addView(textView);
        }
        int textStart = code.indexOf(CODE_WRAPPING);
        int firstLineEnd = code.indexOf("\n");
        String currentCode = textStart > 0 ?
                code.substring(firstLineEnd > 0 ? firstLineEnd + 1 : 0, textStart) :
                firstLineEnd > 0 ? code.substring(firstLineEnd + 1) : code;


        textView.setText(currentCode);
        holder.currentTextOrCodeView = textView;

        if (textStart > -1) {
            createText(code.substring(textStart + 3), null, holder);
        }
    }

    // Возвращает количество элементов в списке (карточек)
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    // Хранит и переиспользует представления элементов списка (карточек)
    public static class ViewHolder extends RecyclerView.ViewHolder {

        long id = -1;

        /**
         * Сейчас пишем код или обычный текст.
         */
        boolean textNow = true;

        /**
         * Текущее поле ввода текста или кода.
         */
        TextView currentTextOrCodeView;

        CardView cardView;
        LinearLayout cardWrapper;
        boolean catGptIsWriting;
        TextView catGptIsWriteTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            cardWrapper = itemView.findViewById(R.id.cardWrapper);
        }

        public void startWrite(Context context){
            catGptIsWriting = true;
            catGptIsWriteTextView = new TextView(context);
            catGptIsWriteTextView.setTextSize(16);
            cardWrapper.addView(catGptIsWriteTextView);
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                int count = 0;
                final String[] texts = {
                        "",
                        ".",
                        "..",
                        "..."};

                @Override
                public void run() {
                    if (catGptIsWriting) {
                        catGptIsWriteTextView.setText(texts[count]);
                        count++;
                        if (count == texts.length) {
                            count = 0;
                        }
                        handler.postDelayed(this, 500);
                    }
                }
            };
            handler.postDelayed(runnable, 500);
        }

        public void stopWrite(){
            if(catGptIsWriting){
                catGptIsWriting = false;
                cardWrapper.removeView(catGptIsWriteTextView);
                catGptIsWriteTextView = null;
            }
        }
    }
}