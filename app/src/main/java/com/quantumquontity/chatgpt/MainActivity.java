package com.quantumquontity.chatgpt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TOKEN = "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";

    private ImageView sendMessage;
    private EditText inputMessage;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findElement();
        senOnClickListeners();
    }

    private void senOnClickListeners() {
        sendMessage.setOnClickListener(this::onSendMessage);
    }

    private void onSendMessage(View view) {
        hideKeyboard(view);
        if(inputMessage.getText().toString().isEmpty()){
            // кинуть ошибку
            return;
        }
        resultText.setText("");// чистим предыдущий результат
        String requestMessage = inputMessage.getText().toString();
        inputMessage.setText("");

        OpenAiService service = new OpenAiService(TOKEN);
        Thread myThread = new Thread(() -> {
            try {
                final List<ChatMessage> messages = new ArrayList<>();
                final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.USER.value(), requestMessage);
                messages.add(systemMessage);
                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                        .builder()
                        .model("gpt-3.5-turbo")
                        .messages(messages)
                        .n(1)
                        .maxTokens(250)
                        .logitBias(new HashMap<>())
                        .build();

                service.streamChatCompletion(chatCompletionRequest)
                        .subscribe(this::onResponse, Throwable::printStackTrace);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        myThread.start();
    }

    private void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void onResponse(ChatCompletionChunk chatCompletionChunk) {
        System.out.println(chatCompletionChunk.toString());
        for (ChatCompletionChoice choice : chatCompletionChunk.getChoices()) {
            ChatMessage message = choice.getMessage();
            if(message != null){
                String content = message.getContent();
                if(content != null){
                    threadSleep(100);
                    runOnUiThread(() ->
                            resultText.setText(resultText.getText() + content));
                }
            }
        }
    }

    private void threadSleep(long mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void findElement() {
        sendMessage = findViewById(R.id.sendMessage);
        inputMessage = findViewById(R.id.inputMessage);
        resultText = findViewById(R.id.resultText);
    }
}