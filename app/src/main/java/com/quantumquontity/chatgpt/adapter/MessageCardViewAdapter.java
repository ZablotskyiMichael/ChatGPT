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

import com.quantumquontity.chatgpt.MainActivity;
import com.quantumquontity.chatgpt.R;
import com.quantumquontity.chatgpt.dto.ChatMessageCardView;

import java.util.List;

public class MessageCardViewAdapter extends RecyclerView.Adapter<MessageCardViewAdapter.ViewHolder> {

    private static final String CODE_WRAPPING = "```";

    private List<ChatMessageCardView> mDataList;
    private LayoutInflater mInflater;
    private MainActivity mainActivity;
    private ViewHolder lastHolder;


    public MessageCardViewAdapter(MainActivity mainActivity, List<ChatMessageCardView> data) {
        this.mInflater = LayoutInflater.from(mainActivity);
        this.mainActivity = mainActivity;
        this.mDataList = data;
    }

    public void refreshData(List<ChatMessageCardView> mDataList) {
        this.mDataList = mDataList;
        notifyDataSetChanged();
    }

    public int addItem(ChatMessageCardView item) {
        mDataList.add(item);
        notifyItemInserted(mDataList.size() - 1);
        return mDataList.size();
    }

    public void updateLastItemText(String newText) {
        if (!newText.isEmpty()) {
            ChatMessageCardView messageCardView = mDataList.get(mDataList.size() - 1);
            messageCardView.setText(messageCardView.getText() + newText);
            TextView currentTextOrCodeView = lastHolder.currentTextOrCodeView;
            if (lastHolder.textNow) {
                createText(currentTextOrCodeView != null ? currentTextOrCodeView.getText() + newText : newText, currentTextOrCodeView, lastHolder, false);
            } else {
                createCode(currentTextOrCodeView != null ? currentTextOrCodeView.getText() + newText : newText, currentTextOrCodeView, lastHolder);
            }
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
        if (mDataList.size() <= position) {
            if (mDataList.size() > 0) {
                createEmptyElement(holder);
            }
            return;
        }

        ChatMessageCardView data = mDataList.get(position);

        // Почистим все если зашли сюда
        // т.к. recyclerView переиспользует старые View холдеры
        clearHolder(holder);

        initNewItem(holder, data);
        createText(data.getText(), null, holder, true);
        if (mDataList.size() - 1 == position) {
            lastHolder = holder;
        }
    }

    private void createEmptyElement(ViewHolder holder) {
        holder.cardWrapper.getLayoutParams().height = mainActivity.getGlobalValuesHolder().get40dpInPx();
        ((CardView)holder.cardWrapper.getParent()).setCardElevation(0f);
    }

    private void clearHolder(ViewHolder holder) {
        holder.cardWrapper.removeAllViews();
        holder.textNow = true;
        holder.id = -1;
        holder.stopWrite();
        holder.catGptIsWriteTextView = null;
        holder.currentTextOrCodeView = null;
        holder.currentProgramingLanguage = null;
    }

    private void initNewItem(ViewHolder holder, ChatMessageCardView data) {
        holder.id = data.getId();
        RelativeLayout linearLayout = new RelativeLayout(mainActivity);
        ImageView imageUserOrSystem = new ImageView(mainActivity);
        ImageView imageCopyMessage = new ImageView(mainActivity);
        TextView nameUserOrSystem = new TextView(mainActivity);
        nameUserOrSystem.setTextColor(Color.BLACK);
        nameUserOrSystem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        if (data.getUserRole().equals("user")) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F4EDE7"));
            imageUserOrSystem.setImageResource(R.drawable.user);
            nameUserOrSystem.setText(R.string.you);
        }
        if (data.getUserRole().equals("system")) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            imageUserOrSystem.setImageResource(R.drawable.chat_icon_ans);
            imageCopyMessage.setImageResource(R.drawable.image_button_copy);
            nameUserOrSystem.setText(R.string.app_name);
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
            Toast.makeText(mainActivity, mainActivity.getText(R.string.messageCopied), Toast.LENGTH_SHORT).show();
            ClipboardManager clipboard = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("label", data.getText());
            clipboard.setPrimaryClip(clipData);
        });

        linearLayout.addView(imageUserOrSystem);
        linearLayout.addView(nameUserOrSystem);
        linearLayout.addView(imageCopyMessage);

        holder.cardWrapper.setPadding(30, 30, 30, 30);

        holder.cardWrapper.addView(linearLayout);
    }

    /**
     * @param firstText - если это первый текст в сообщении. (Чтоб понять, рисовать 3 точки или нет)
     */
    private void createText(String text, TextView textView, ViewHolder holder, boolean firstText) {
        if (text.isEmpty()) {
            if(firstText){
                holder.startWrite(mainActivity);
            }
            return;
        } else {
            holder.stopWrite();
        }
        if (textView == null) {
            textView = new TextView(mainActivity);
            textView.setAutoSizeTextTypeUniformWithConfiguration(12,16,2,1);

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
            CardView cardView = new CardView(mainActivity);
            RelativeLayout relativeLayout = new RelativeLayout(mainActivity);
            ImageView imageCopyMessage = new ImageView(mainActivity);
            TextView textCopyMessage = new TextView(mainActivity);
            TextView textProgramingLanguage = new TextView(mainActivity);

            imageCopyMessage.setImageResource(R.drawable.image_button_copy);
            relativeLayout.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.panelCardView));
            textCopyMessage.setText(R.string.copy_code);
            textCopyMessage.setTextColor(Color.parseColor("#d9d9e3"));

            // Текст языка на котором написан код
            int firstLineEnd = code.indexOf("\n");
            if(firstLineEnd > -1){
                textProgramingLanguage.setText(code.substring(0, firstLineEnd));
                code = code.substring(firstLineEnd);
            }
            textProgramingLanguage.setTextColor(Color.parseColor("#d9d9e3"));

            RelativeLayout.LayoutParams layoutParamsLanguage = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100);
            layoutParamsLanguage.addRule(RelativeLayout.ALIGN_PARENT_START);
            textProgramingLanguage.setPadding(20, 20, 20, 20);
            textProgramingLanguage.setLayoutParams(layoutParamsLanguage);
            relativeLayout.addView(textProgramingLanguage);
            holder.currentProgramingLanguage = textProgramingLanguage;

            RelativeLayout.LayoutParams layoutParamsImageCopy = new RelativeLayout.LayoutParams(100, 100);
            layoutParamsImageCopy.addRule(RelativeLayout.ALIGN_PARENT_END);
            imageCopyMessage.setPadding(20, 20, 20, 20);
            imageCopyMessage.setLayoutParams(layoutParamsImageCopy);

            int textStart = code.indexOf(CODE_WRAPPING);
            String currentCode = textStart > 0 ?
                    code.substring(firstLineEnd > 0 ? firstLineEnd + 1 : 0, textStart) :
                    firstLineEnd > 0 ? code.substring(firstLineEnd + 1) : code;

            textCopyMessage.setOnClickListener(v -> {
                Toast.makeText(mainActivity, mainActivity.getText(R.string.messageCopied), Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", currentCode);
                clipboard.setPrimaryClip(clipData);
            });
            relativeLayout.addView(imageCopyMessage);

            RelativeLayout.LayoutParams layoutParamsTextCopy = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100);
            layoutParamsTextCopy.addRule(RelativeLayout.ALIGN_PARENT_END);
            textCopyMessage.setPadding(0, 20, 120, 20);
            textCopyMessage.setLayoutParams(layoutParamsTextCopy);

            imageCopyMessage.setOnClickListener(v -> {
                Toast.makeText(mainActivity, mainActivity.getText(R.string.messageCopied), Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", currentCode);
                clipboard.setPrimaryClip(clipData);
            });

            relativeLayout.addView(textCopyMessage);
            holder.cardWrapper.addView(relativeLayout);

            cardView.setCardBackgroundColor(mainActivity.getResources().getColor(R.color.black));
            holder.cardWrapper.addView(cardView);

            textView = new TextView(mainActivity);
            textView.setPadding(16, 40, 8, 8);
            textView.setTextColor(mainActivity.getResources().getColor(R.color.white));
            textView.setAutoSizeTextTypeUniformWithConfiguration(12,16,2,1);
            cardView.addView(textView);
        }
        int textStart = code.indexOf(CODE_WRAPPING);

        // Определение текущего кода.
        // Выделяет из всего текста код (исключая тип кода и текст после кода, если они есть)
        String currentCode;
        if (holder.currentProgramingLanguage != null && holder.currentProgramingLanguage.getText().toString().isEmpty()) {
            int firstLineEnd = code.indexOf("\n");
            currentCode = textStart > 0 ?
                    code.substring(firstLineEnd > 0 ? firstLineEnd + 1 : 0, textStart) :
                    firstLineEnd > 0 ? code.substring(firstLineEnd + 1) : code;
            holder.currentProgramingLanguage.setText(firstLineEnd > -1 ? code.substring(0, firstLineEnd) : "");
        } else {
            currentCode = textStart > 0 ? code.substring(0, textStart) : code;
        }

        textView.setText(currentCode);
        holder.currentTextOrCodeView = textView;

        if (textStart > -1) {
            createText(code.substring(textStart + 3), null, holder, false);
        }
    }

    // Возвращает количество элементов в списке (карточек)
    @Override
    public int getItemCount() {
        return mDataList.size() + 1;
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

        /**
         * Текущее поле языка кода.
         */
        TextView currentProgramingLanguage;

        CardView cardView;
        LinearLayout cardWrapper;
        boolean catGptIsWriting;
        TextView catGptIsWriteTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            cardWrapper = itemView.findViewById(R.id.cardWrapper);
        }

        public void startWrite(MainActivity mainActivity){
            catGptIsWriting = true;
            catGptIsWriteTextView = new TextView(mainActivity);
            catGptIsWriteTextView.setAutoSizeTextTypeUniformWithConfiguration(12,16,2,1);
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