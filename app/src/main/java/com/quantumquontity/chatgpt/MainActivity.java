package com.quantumquontity.chatgpt;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static com.theokanning.openai.service.OpenAiService.defaultRetrofit;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.quantumquontity.chatgpt.adapter.MessageCardViewAdapter;
import com.quantumquontity.chatgpt.chatGrp.OpenAiServiceCustom;
import com.quantumquontity.chatgpt.dao.ChatDao;
import com.quantumquontity.chatgpt.dao.ChatMessageDao;
import com.quantumquontity.chatgpt.dao.DBHelper;
import com.quantumquontity.chatgpt.data.Chat;
import com.quantumquontity.chatgpt.dict.SubPage;
import com.quantumquontity.chatgpt.dto.ChatMessageCardView;
import com.quantumquontity.chatgpt.service.ChatMessageService;
import com.quantumquontity.chatgpt.service.ChatService;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TOKEN = "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";
    private static final String MODEL_TYPE = "gpt-3.5-turbo";

    /**
     * Вспомогательный класс для работы с БД
     */
    private DBHelper dbHelper;

    private ChatService chatService;
    private ChatMessageService chatMessageService;
    private ImageView catLogoImageView;
    private TextInputLayout inputMessageLayout;
    private ImageView chatsIcon;
    private EditText inputMessage;

    private CircularProgressIndicator progressBar;
    private Button startChatButton;
    private RecyclerView messagesRecyclerView;
    private LinearLayout messagesLayout;
    private LinearLayout catLogoWrapper;
    private MessageCardViewAdapter messageCardViewAdapter;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private LinearLayout clearAllChat;
    private LinearLayout createNewChat;
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
        llm.setReverseLayout(false);
        llm.setStackFromEnd(true);
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
        inputMessageLayout.setEndIconOnClickListener(this::onSendMessage);
        initChatsOnClickListeners();
        startChatButton.setOnClickListener(this::onStartChatClick);


        clearAllChat.setOnClickListener(this::deleteAllChat);
        createNewChat.setOnClickListener(this::onStartChatClick);

    }

    private void deleteAllChat(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.deletion_confirmation);
            builder.setMessage(R.string.question_delete_all_chat);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                chatService.deleteAllChat();
                navigationView.getMenu().clear();
                initMenu();
                drawerLayout.closeDrawer(GravityCompat.START);
                if (subPage == SubPage.CHAT) {
                    onBackPressed();
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

    }

    private void onStartChatClick(View view) {
        // проверка, открыт ли NavigationView
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        subPage = SubPage.CHAT;
        chatsIcon.setVisibility(View.VISIBLE);
        inputMessageLayout.setVisibility(View.VISIBLE);
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
        navigationView.getMenu().clear();
        initMenu();
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
            dropCurrentChatIfEmpty();
            toMainPage();
        } else {
            super.onBackPressed();
        }
    }

    private void dropCurrentChatIfEmpty() {
        if (chatMessageService.getChatMessagesList(currentChatId).isEmpty()) {
            chatService.deleteChat(currentChatId);
            currentChatId = -1;
            currentChatMessage = null;
        }
    }

    private void toMainPage() {
        subPage = SubPage.MAIN;
        chatsIcon.setVisibility(View.GONE);
        inputMessageLayout.setVisibility(View.GONE);
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
                .sorted(Comparator.comparingLong(chat -> -chat.getId()))
                .collect(Collectors.toList());
        for (Chat chat : sortedList) {
            MenuItem menuItem = menu.add(Menu.NONE, (int) chat.getId(), Menu.NONE, "");
            menuItem.setActionView(R.layout.menu_item_layout);
            ImageView menuIcon = menuItem.getActionView().findViewById(R.id.menu_icon);
            TextView menuTitle = menuItem.getActionView().findViewById(R.id.menu_title);
            Button menuButton = menuItem.getActionView().findViewById(R.id.menu_button);
            menuIcon.setImageResource(R.drawable.round_message_24);
            menuTitle.setText(chat.getName() + " " + chat.getId());
            menuButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.deletion_confirmation);
                builder.setMessage(R.string.question_delete_this_chat);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                    chatService.deleteChat(chat.getId());
                    navigationView.getMenu().clear();
                    initMenu();
                    drawerLayout.closeDrawer(GravityCompat.START);
                });
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            });

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
        // Заменить endIcon на иконку с анимацией загрузки
        inputMessageLayout.setEndIconDrawable(R.drawable.circular_loading);
        Drawable drawable = inputMessageLayout.getEndIconDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

        // Отключить возможность ввода в inputMessage
        if (inputMessage.getText() != null) {
            inputMessage.setEnabled(false);
        }

        // Включить ProgressBar и выполнить действия по загрузке
       /* progressBar.setVisibility(View.VISIBLE);
        progressBar.show();*/

        // выполнить действия по загрузке, отправку данных на сервер ИИ

        String requestMessage = inputMessage.getText().toString();
        inputMessage.setText("");

        createAndSaveChatMessage(requestMessage, ChatMessageRole.USER);
        currentChatMessage = createAndSaveChatMessage("", ChatMessageRole.SYSTEM);

        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(TOKEN, Duration.ofSeconds(30));
        Retrofit retrofit = defaultRetrofit(client, mapper);

        OpenAiApi aiApi = retrofit.create(OpenAiApi.class);
        ExecutorService executorService = client.dispatcher().executorService();
        OpenAiServiceCustom service = new OpenAiServiceCustom(aiApi, executorService);
        List<ChatMessage> messages = getChatMessages();
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(MODEL_TYPE)
                .messages(messages)
                .n(1)
                .maxTokens(250)
                .logitBias(new HashMap<>())
                .build();

        new Thread(() -> {
            try {
                service.executeChatCompletion(chatCompletionRequest, this::onResponse);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    currentChatMessage.setText(e.getMessage());
                    messageCardViewAdapter.updateLastItemText(currentChatMessage.getText());
                });
            }
        }).start();
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
        for (ChatCompletionChoice choice : chatCompletionChunk.getChoices()) {
            ChatMessage message = choice.getMessage();
            if (message != null) {
                String content = message.getContent();
                if (content != null) {
                    runOnUiThread(() -> {
                        currentChatMessage.setText(currentChatMessage.getText() + content);
                        messageCardViewAdapter.updateLastItemText(currentChatMessage.getText());
                    });
                }
            }
        }

        // После завершения загрузки снова включить возможность ввода в inputMessageLayout и скрыть ProgressBar
        runOnUiThread(() -> {
            if (inputMessage.getText().toString().isEmpty()) {
                inputMessage.setEnabled(true);
            }
            /*progressBar.hide();*/
            // Восстановить endIcon
            inputMessageLayout.setEndIconDrawable(R.drawable.baseline_send_24);
            inputMessageLayout.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.iconEnd)));
        });
        chatMessageService.updateChatMessageText(currentChatMessage.getId(), currentChatMessage.getText());
    }

    private void findElement() {
        inputMessage = findViewById(R.id.inputMessage);
        chatsIcon = findViewById(R.id.chatsIcon);
        drawerLayout = findViewById(R.id.drawer_layout);
        startChatButton = findViewById(R.id.startChatButton);
        navigationView = findViewById(R.id.nav_view);
        catLogoImageView = findViewById(R.id.catLogoImageView);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesLayout = findViewById(R.id.messagesLayout);
        catLogoWrapper = findViewById(R.id.catLogoWrapper);
        inputMessageLayout = findViewById(R.id.inputMessageLayout);

        //нужно что бы найти HeaderView и LinearLayout внутри navigationView
        View headerLayout = navigationView.getHeaderView(0);
        clearAllChat = headerLayout.findViewById(R.id.clearAllChat);
        createNewChat = headerLayout.findViewById(R.id.createNewChat);

        /* progressBar = findViewById(R.id.progressBar);*/
    }
}