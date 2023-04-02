package com.quantumquontity.chatgpt;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.navigation.NavigationView;
import com.quantumquontity.chatgpt.adapter.MessageCardViewAdapter;
import com.quantumquontity.chatgpt.dao.ChatDao;
import com.quantumquontity.chatgpt.dao.ChatMessageDao;
import com.quantumquontity.chatgpt.dao.DBHelper;
import com.quantumquontity.chatgpt.data.Chat;
import com.quantumquontity.chatgpt.dict.SubPage;
import com.quantumquontity.chatgpt.dto.ChatMessageCardView;
import com.quantumquontity.chatgpt.service.ChatMessageService;
import com.quantumquontity.chatgpt.service.ChatService;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final String TOKEN = "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";

    /**
     * Вспомогательный класс для работы с БД
     */
    private DBHelper dbHelper;

    private ChatService chatService;
    private ChatMessageService chatMessageService;

    private ImageView catLogoImageView;
    private ImageView sendMessage;
    private ImageView chatsIcon;
    private EditText inputMessage;

    private Button startChatButton;
    private RecyclerView messagesRecyclerView;
    private LinearLayout messagesLayout;
    private LinearLayout catLogoWrapper;
    private MessageCardViewAdapter messageCardViewAdapter;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    /**
     * Вспомогательный класс чтоб понять где мы сейчас.
     */
    private SubPage subPage = SubPage.MAIN;
    private long currentChatId = -1;
    private com.quantumquontity.chatgpt.data.ChatMessage currentChatMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findElement();
        initServices();
        initData();
        senOnClickListeners();
    }

    private void initData() {
        initChats();
    }

    private void initChats() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(llm);
        messageCardViewAdapter = new MessageCardViewAdapter(this, new ArrayList<>());
        messagesRecyclerView.setAdapter(messageCardViewAdapter);
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
        messagesRecyclerView.setVisibility(View.VISIBLE);
        messagesLayout.setVisibility(View.VISIBLE);
        catLogoWrapper.setVisibility(View.GONE);

        // Сохранение нового чата
        String newChatName = getResources().getString(R.string.new_chat);
        long newChatId = chatService.createNewChat(newChatName);
        currentChatId = newChatId;
        // Добавление чата в меню
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.add(Menu.NONE, (int) newChatId, Menu.NONE, newChatName);
        menuItem.setIcon(R.drawable.round_message_24);
        messageCardViewAdapter.refreshData(new ArrayList<>());
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
        if (subPage == SubPage.CHAT) {
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
        messagesRecyclerView.setVisibility(View.GONE);
        messagesLayout.setVisibility(View.GONE);
        catLogoWrapper.setVisibility(View.VISIBLE);
    }

    private void initMenu() {
        Menu menu = navigationView.getMenu();

        List<Chat> sortedList = chatService.getAll().stream()
                .sorted(Comparator.comparingLong(chat1 -> - chat1.getId()))
                .collect(Collectors.toList());
        for (Chat chat : sortedList) {
            MenuItem menuItem = menu.add(Menu.NONE, (int) chat.getId(), Menu.NONE, chat.getName() + " " + chat.getId());
            menuItem.setIcon(R.drawable.round_message_24);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            currentChatId = item.getItemId();
            uploadMessagesForCurrentChat();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void uploadMessagesForCurrentChat() {
        messageCardViewAdapter.refreshData(
                chatMessageService.getChatMessagesList(currentChatId)
                        .stream()
                        .map(ChatMessageCardView::new)
                        .collect(Collectors.toList())
        );
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
        if (inputMessage.getText().toString().isEmpty()) {
            // кинуть ошибку
            return;
        }
        String requestMessage = inputMessage.getText().toString();
        inputMessage.setText("");

        createAndSaveChatMessage(requestMessage, ChatMessageRole.USER);
        currentChatMessage = createAndSaveChatMessage("", ChatMessageRole.SYSTEM);

        OpenAiService service = new OpenAiService(TOKEN);
        Thread myThread = new Thread(() -> {
            try {
                List<ChatMessage> messages = getChatMessages();
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

    /**
     * Собирает историю переписки для отправки в chatGPT.
     */
    private List<ChatMessage> getChatMessages() {
        return chatMessageService.getChatMessagesList(currentChatId)
                .stream()
                .filter(message -> !message.getText().isEmpty())
                .map(message -> new ChatMessage(message.getUserRole(), message.getText()))
                .collect(Collectors.toList());
    }

    private com.quantumquontity.chatgpt.data.ChatMessage createAndSaveChatMessage(String requestMessage, ChatMessageRole role) {
        com.quantumquontity.chatgpt.data.ChatMessage savedMessage = chatMessageService.save(
                new com.quantumquontity.chatgpt.data.ChatMessage(
                        currentChatId,
                        requestMessage,
                        role.value()
                )
        );
        messageCardViewAdapter.addItem(new ChatMessageCardView(savedMessage));
        return savedMessage;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void onResponse(ChatCompletionChunk chatCompletionChunk) {
        System.out.println(chatCompletionChunk.toString());
        for (ChatCompletionChoice choice : chatCompletionChunk.getChoices()) {
            ChatMessage message = choice.getMessage();
            if (message != null) {
                String content = message.getContent();
                if (content != null) {
                    threadSleep(100);
                    runOnUiThread(() -> {
                        currentChatMessage.setText(currentChatMessage.getText() + content);
                        messageCardViewAdapter.updateLastItemText(currentChatMessage.getText());
                    });
                }
            }
        }
        chatMessageService.updateChatMessageText(currentChatMessage.getId(), currentChatMessage.getText());
    }

    private void threadSleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void findElement() {
        sendMessage = findViewById(R.id.sendMessage);
        inputMessage = findViewById(R.id.inputMessage);
        chatsIcon = findViewById(R.id.chatsIcon);
        drawerLayout = findViewById(R.id.drawer_layout);
        startChatButton = findViewById(R.id.startChatButton);
        navigationView = findViewById(R.id.nav_view);
        catLogoImageView = findViewById(R.id.catLogoImageView);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesLayout = findViewById(R.id.messagesLayout);
        catLogoWrapper = findViewById(R.id.catLogoWrapper);
    }
}