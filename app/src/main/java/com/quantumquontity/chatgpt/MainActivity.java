package com.quantumquontity.chatgpt;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.quantumquontity.chatgpt.dao.DBHelper;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TOKEN = "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";

    /** Вспомогательный класс для работы с БД */
    private DBHelper dbHelper;

    private ImageView sendMessage;
    private ImageView chatsIcon;
    private EditText inputMessage;
    private TextView resultText;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findElement();
        initServices();
        senOnClickListeners();
    }

    private void initServices() {
        dbHelper = new DBHelper(this);
    }

    private void senOnClickListeners() {
        sendMessage.setOnClickListener(this::onSendMessage);
        initChatsOnClickListeners();
    }

    private void initChatsOnClickListeners() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView menuIcon = findViewById(R.id.chatsIcon);
        menuIcon.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(MainActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_gallery) {
                Toast.makeText(MainActivity.this, "Gallery clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_slideshow) {
                Toast.makeText(MainActivity.this, "Slideshow clicked", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        chatsIcon = findViewById(R.id.chatsIcon);
        drawerLayout = findViewById(R.id.drawer_layout);
    }
}