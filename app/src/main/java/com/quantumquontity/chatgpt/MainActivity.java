package com.quantumquontity.chatgpt;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.quantumquontity.chatgpt.dao.ChatDao;
import com.quantumquontity.chatgpt.dao.ChatMessageDao;
import com.quantumquontity.chatgpt.dao.DBHelper;
import com.quantumquontity.chatgpt.data.Chat;
import com.quantumquontity.chatgpt.dict.SubPage;
import com.quantumquontity.chatgpt.service.ChatMessageService;
import com.quantumquontity.chatgpt.service.ChatService;
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

    private ChatService chatService;
    private ChatMessageService chatMessageService;

    private ImageView catLogoImageView;
    private ImageView sendMessage;
    private ImageView chatsIcon;
    private EditText inputMessage;

    private Button startChatButton;
    private TextView resultText;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    /**
     * Вспомогательный класс чтоб понять где мы сейчас.
     */
    private SubPage subPage = SubPage.MAIN;

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
        chatService = new ChatService(new ChatDao(dbHelper));
        chatMessageService = new ChatMessageService(new ChatMessageDao(dbHelper));
    }

    private void senOnClickListeners() {
        sendMessage.setOnClickListener(this::onSendMessage);
        initChatsOnClickListeners();
        startChatButton.setOnClickListener(this::onStartChatClick);
    }

    private void onStartChatClick(View view) {
        subPage = SubPage.CHAT;
        chatsIcon.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);
        inputMessage.setVisibility(View.VISIBLE);
        catLogoImageView.setVisibility(View.GONE);
        startChatButton.setVisibility(View.GONE);

        // Сохранение нового чата
        String newChatName = getResources().getString(R.string.new_chat);
        chatService.createNewChat(newChatName);

        // Добавление чата в меню
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.add(Menu.NONE, menu.size(), Menu.NONE, newChatName);
        menuItem.setIcon(R.drawable.round_message_24);
    }

    private void initChatsOnClickListeners() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        chatsIcon.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

       initMenu();
    }

    @Override
    public void onBackPressed() {
        if(subPage == SubPage.CHAT){
            toMainPage();
        } else {
            super.onBackPressed();
        }
    }

    private void toMainPage() {
        subPage = SubPage.MAIN;
        chatsIcon.setVisibility(View.GONE);
        sendMessage.setVisibility(View.GONE);
        inputMessage.setVisibility(View.GONE);
        catLogoImageView.setVisibility(View.VISIBLE);
        startChatButton.setVisibility(View.VISIBLE);
    }

    private void initMenu() {
        Menu menu = navigationView.getMenu();

        int counter = 0;
        for (Chat chat : chatService.getAll()) {
            MenuItem menuItem = menu.add(Menu.NONE, counter++, Menu.NONE, chat.getName());
            menuItem.setIcon(R.drawable.round_message_24);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Chat chat = chatService.getAll().get(id);
            Toast.makeText(MainActivity.this, chat.getName(), Toast.LENGTH_SHORT).show();

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
        startChatButton = findViewById(R.id.startChatButton);
        navigationView = findViewById(R.id.nav_view);
        catLogoImageView = findViewById(R.id.catLogoImageView);
    }
}