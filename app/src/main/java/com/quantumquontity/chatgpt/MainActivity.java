package com.quantumquontity.chatgpt;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static com.theokanning.openai.service.OpenAiService.defaultRetrofit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.quantumquontity.chatgpt.adapter.MessageCardViewAdapter;
import com.quantumquontity.chatgpt.billing.BillingService;
import com.quantumquontity.chatgpt.chatGrp.OpenAiServiceCustom;
import com.quantumquontity.chatgpt.dao.ChatDao;
import com.quantumquontity.chatgpt.dao.ChatMessageDao;
import com.quantumquontity.chatgpt.dao.DBHelper;
import com.quantumquontity.chatgpt.data.Chat;
import com.quantumquontity.chatgpt.dict.SubPage;
import com.quantumquontity.chatgpt.dict.Suggests;
import com.quantumquontity.chatgpt.dto.ChatMessageCardView;
import com.quantumquontity.chatgpt.service.ChatMessageService;
import com.quantumquontity.chatgpt.service.ChatService;
import com.quantumquontity.chatgpt.service.PointService;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TOKEN = "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";
    private static final String MODEL_TYPE = "gpt-3.5-turbo";
    private static final String TAG = "MainActivity";
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    /**
     * Вспомогательный класс для работы с БД
     */
    private DBHelper dbHelper;

    private PointService pointService;

    private RewardedAd rewardedAd;

    private BillingService billingService;

    private ChatService chatService;
    private ChatMessageService chatMessageService;
    private ImageView catLogoImageView;
    private TextInputLayout inputMessageLayout;
    private ImageView chatsIconMainPage;
    private ImageView chatsIconChatPage;
    private EditText inputMessage;

    private Button startChatButton;
    private Button subscription_1_month;
    private Button subscription_3_month;
    private Button subscription_12_month;
    private RecyclerView messagesRecyclerView;
    private LinearLayout messagesLayout;
    private LinearLayout exampleRequest;
    private LinearLayout catLogoWrapper;
    private MessageCardViewAdapter messageCardViewAdapter;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private LinearLayout clearAllChat;
    private LinearLayout createNewChat;
    private ActionBarDrawerToggle drawerToggle;
    private TextView quantityTokenMainPage;
    private TextView quantityTokenChatPage;
    private TextView premiumExistLabel;
    private ConstraintLayout premiumExistLabelWrapper;
    private LinearLayout chatPageWrapper;
    private LinearLayout mainPageWrapper;
    private Button buyPremiumChatButton;
    private Button buttonTabRequestOne;
    private Button buttonTabRequestTwo;
    private Button buttonTabRequestThree;
    private Button buttonTabRequestFour;

    /**
     * Вспомогательный класс чтоб понять где мы сейчас.
     */
    private SubPage subPage = SubPage.MAIN;
    private long currentChatId = -1;
    private int currentPoints = 0;
    private com.quantumquontity.chatgpt.data.ChatMessage currentChatMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        findElement();
        initServices();
        initData();
        loadAd();
        setOnClickListeners();
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, AD_UNIT_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.toString());
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Ad was loaded.");
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                rewardedAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                rewardedAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                    }
                });
    }

    private void initData() {
        initChats();
        initPoints();
    }

    private void initPoints() {
        if (isInfinityPoints()) {
            quantityTokenMainPage.setText(String.valueOf('\u221E'));
            quantityTokenChatPage.setText(String.valueOf('\u221E'));
        } else {
            currentPoints = pointService.getCurrentPoints();
            quantityTokenMainPage.setText(String.valueOf(currentPoints));
            quantityTokenChatPage.setText(String.valueOf(currentPoints));
        }
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
        pointService = new PointService(this);
        billingService = new BillingService(this, this::onExistPremium);
    }

    private void setOnClickListeners() {
        inputMessageLayout.setEndIconOnClickListener(this::onSendMessage);
        initChatsOnClickListeners();
        startChatButton.setOnClickListener(this::onStartChatClick);

        clearAllChat.setOnClickListener(this::deleteAllChat);
        createNewChat.setOnClickListener(this::onStartChatClick);

        buyPremiumChatButton.setOnClickListener(this::openSubscribePage);
        buttonTabRequestOne.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestOne));
        buttonTabRequestTwo.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestTwo));
        buttonTabRequestThree.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestThree));
        buttonTabRequestFour.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestFour));

    }

    private void setInputTextFromExample(View view, Button button) {
        inputMessage.setText(button.getText().toString());
    }

    public void onExistPremium() {
        premiumExistLabel.setVisibility(View.VISIBLE);
        premiumExistLabelWrapper.setVisibility(View.VISIBLE);
        buyPremiumChatButton.setVisibility(View.GONE);
        initPoints();
    }

    private void buy1MonthSubscription(Dialog dialog) {
        dialog.dismiss();
        billingService.buy1MonthSubscription();
    }

    private void buy3MonthsSubscription(Dialog dialog) {
        dialog.dismiss();
        billingService.buy3MonthsSubscription();
    }

    private void buy12MonthsSubscription(Dialog dialog) {
        dialog.dismiss();
        billingService.buy12MonthsSubscription();
    }

    private void openSubscribePage(View view) {
        final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar);
        dialog.setContentView(R.layout.subscribe_page);

        subscription_1_month = dialog.findViewById(R.id.subscription_1_month);
        subscription_3_month = dialog.findViewById(R.id.subscription_3_month);
        subscription_12_month = dialog.findViewById(R.id.subscription_12_month);

        subscription_1_month.setOnClickListener(view1 -> buy1MonthSubscription(dialog));
        subscription_3_month.setOnClickListener(view1 -> buy3MonthsSubscription(dialog));
        subscription_12_month.setOnClickListener(view1 -> buy12MonthsSubscription(dialog));

        // Проставим актуальные цены
        subscription_1_month.setText(getText(R.string.month1) + "\n" + billingService.get1MonthSubscriptionCost());
        subscription_3_month.setText(getText(R.string.months3) + "\n" + billingService.get3MonthsSubscriptionCost());
        subscription_12_month.setText(getText(R.string.months12) + "\n" + billingService.get12MonthsSubscriptionCost());

        dialog.show();

    }

    private void showADsAndGetPoint(View view) {
        if (rewardedAd != null) {
            Activity activityContext = MainActivity.this;
            rewardedAd.show(activityContext, rewardItem -> {
                // Handle the reward.
                Log.d(TAG, "The user earned the reward.");
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();
                currentPoints++;
                pointService.updatePoints(currentPoints);
                quantityTokenMainPage.setText(String.valueOf(currentPoints));
                quantityTokenChatPage.setText(String.valueOf(currentPoints));
                loadAd();
            });
        } else {
            Toast.makeText(this, getText(R.string.ad_is_not_available), Toast.LENGTH_SHORT).show();
        }

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
        List<Suggests> suggestsList = Arrays.asList(Suggests.values());

        Set<Integer> uniqueeNumders = getUniqueeNumders(4, suggestsList.size());
        Iterator<Integer> iterator = uniqueeNumders.iterator();

        buttonTabRequestOne.setText(getString(suggestsList.get(iterator.next()).getText()));
        buttonTabRequestTwo.setText(getString(suggestsList.get(iterator.next()).getText()));
        buttonTabRequestThree.setText(getString(suggestsList.get(iterator.next()).getText()));
        buttonTabRequestFour.setText(getString(suggestsList.get(iterator.next()).getText()));


        subPage = SubPage.CHAT;
        chatPageWrapper.setVisibility(View.VISIBLE);
        mainPageWrapper.setVisibility(View.GONE);
        chatsIconMainPage.setVisibility(View.VISIBLE);
        chatsIconChatPage.setVisibility(View.VISIBLE);
        inputMessageLayout.setVisibility(View.VISIBLE);
        inputMessage.setVisibility(View.VISIBLE);
        catLogoImageView.setVisibility(View.GONE);
        startChatButton.setVisibility(View.GONE);
        messagesRecyclerView.setVisibility(View.GONE);
        exampleRequest.setVisibility(View.VISIBLE);
        messagesLayout.setVisibility(View.VISIBLE);
        catLogoWrapper.setVisibility(View.GONE);
        premiumExistLabel.setVisibility(View.GONE);
        premiumExistLabelWrapper.setVisibility(View.GONE);

        // Сохранение нового чата
        String newChatName = getResources().getString(R.string.new_chat);
        long newChatId = chatService.createNewChat(newChatName);
        currentChatId = newChatId;
        // Добавление чата в меню
        navigationView.getMenu().clear();
        initMenu();
        messageCardViewAdapter.refreshData(new ArrayList<>());
    }

    private Set<Integer> getUniqueeNumders(int count, int maxNumber) {
        Random random = new Random();
        Set<Integer> uniqueNumbers = new HashSet<>();

        while (uniqueNumbers.size() < count) {
            uniqueNumbers.add(random.nextInt(maxNumber));
        }
        return uniqueNumbers;
    }

    private void initChatsOnClickListeners() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        chatsIconMainPage.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        chatsIconChatPage.setOnClickListener(view -> {
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
        chatPageWrapper.setVisibility(View.GONE);
        mainPageWrapper.setVisibility(View.VISIBLE);
        chatsIconMainPage.setVisibility(View.GONE);
        chatsIconChatPage.setVisibility(View.GONE);
        inputMessage.setVisibility(View.GONE);
        catLogoImageView.setVisibility(View.VISIBLE);
        startChatButton.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.GONE);
        messagesLayout.setVisibility(View.GONE);
        catLogoWrapper.setVisibility(View.VISIBLE);
        premiumExistLabel.setVisibility(View.VISIBLE);
        premiumExistLabelWrapper.setVisibility(View.VISIBLE);
        exampleRequest.setVisibility(View.GONE);
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
            if (exampleRequest.getVisibility() == View.VISIBLE) {
                exampleRequest.setVisibility(View.GONE);
                messagesRecyclerView.setVisibility(View.VISIBLE);
                messagesLayout.setVisibility(View.VISIBLE);
            }
            if (messageCardViewAdapter.getItemCount() == 0) {
                exampleRequest.setVisibility(View.VISIBLE);
                messagesRecyclerView.setVisibility(View.GONE);
            }
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
        if (exampleRequest.getVisibility() == View.VISIBLE) {
            exampleRequest.setVisibility(View.GONE);
            messagesLayout.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
        }
        if (inputMessage.getText().toString().isEmpty()) {
            // кинуть ошибку
            return;
        }

        if (currentPoints > 0 || isInfinityPoints()) {
            if (!isInfinityPoints()) {
                currentPoints--;
                pointService.updatePoints(currentPoints);
                quantityTokenMainPage.setText(String.valueOf(currentPoints));
                quantityTokenChatPage.setText(String.valueOf(currentPoints));
            }

            inputMessageLayout.setEndIconOnClickListener(null);

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
            List<ChatMessage> messages = getLastChatMessages();
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model(MODEL_TYPE)
                    .messages(messages)
                    .n(1)
                    .maxTokens(500)
                    .logitBias(new HashMap<>())
                    .build();

            new Thread(() -> {
                try {
                    service.executeChatCompletion(chatCompletionRequest,
                            this::onResponse,
                            () -> // После завершения загрузки снова включить возможность ввода в inputMessageLayout и скрыть ProgressBar
                                    runOnUiThread(this::enableInput));
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        // TODO вернуть балл на счет?
                        currentChatMessage.setText(getString(R.string.unknown_error));
                        System.out.println("Request error: " + e);
                        messageCardViewAdapter.updateLastItemText(currentChatMessage.getText());
                        enableInput();
                    });
                }
            }).start();
        }
    }

    private boolean isInfinityPoints() {
        return billingService.isPremium();
    }

    /**
     * Собирает историю переписки для отправки в chatGPT.
     */
    private List<ChatMessage> getLastChatMessages() {
        List<ChatMessage> messages = chatMessageService.getChatMessagesList(currentChatId)
                .stream()
                .filter(message -> !message.getText().isEmpty())
                .map(message -> new ChatMessage(message.getUserRole(), message.getText()))
                .collect(Collectors.toList());

        int maxLength = 2000;
        int totalLength = 0;
        List<ChatMessage> lastMessages = new ArrayList<>();

        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (totalLength + message.getContent().length() <= maxLength) {
                lastMessages.add(message);
                totalLength += message.getContent().length();
            } else {
                break;
            }
        }

        Collections.reverse(lastMessages);
        return lastMessages;
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
                        messageCardViewAdapter.updateLastItemText(content);
                    });
                }
            }
        }
        chatMessageService.updateChatMessageText(currentChatMessage.getId(), currentChatMessage.getText());
    }

    private void enableInput() {
        inputMessageLayout.setEndIconOnClickListener(this::onSendMessage);
        if (inputMessage.getText().toString().isEmpty()) {
            inputMessage.setEnabled(true);
        }
        /*progressBar.hide();*/
        // Восстановить endIcon
        inputMessageLayout.setEndIconDrawable(R.drawable.baseline_send_24);
        inputMessageLayout.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.iconEnd)));
    }

    private void findElement() {
        inputMessage = findViewById(R.id.inputMessage);
        chatsIconMainPage = findViewById(R.id.chatsIconMainPage);
        chatsIconChatPage = findViewById(R.id.chatsIconChatPage);
        drawerLayout = findViewById(R.id.drawer_layout);
        startChatButton = findViewById(R.id.startChatButton);
        navigationView = findViewById(R.id.nav_view);
        catLogoImageView = findViewById(R.id.catLogoImageView);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesLayout = findViewById(R.id.messagesLayout);
        catLogoWrapper = findViewById(R.id.catLogoWrapper);
        inputMessageLayout = findViewById(R.id.inputMessageLayout);
        premiumExistLabel = findViewById(R.id.premiumExistLabel);
        premiumExistLabelWrapper = findViewById(R.id.premiumExistLabelWrapper);
        chatPageWrapper = findViewById(R.id.chatPageWrapper);
        mainPageWrapper = findViewById(R.id.mainPageWrapper);

        //нужно что бы найти HeaderView и LinearLayout внутри navigationView
        View headerLayout = navigationView.getHeaderView(0);
        clearAllChat = headerLayout.findViewById(R.id.clearAllChat);
        createNewChat = headerLayout.findViewById(R.id.createNewChat);
        quantityTokenMainPage = findViewById(R.id.quantityTokenMainPage);
        quantityTokenChatPage = findViewById(R.id.quantityTokenChatPage);
        buyPremiumChatButton = findViewById(R.id.buyPremiumChatButton);

        exampleRequest = findViewById(R.id.exampleRequest);
        buttonTabRequestOne = findViewById(R.id.buttonTabRequestOne);
        buttonTabRequestTwo = findViewById(R.id.buttonTabRequestTwo);
        buttonTabRequestThree = findViewById(R.id.buttonTabRequestThree);
        buttonTabRequestFour = findViewById(R.id.buttonTabRequestFour);

        /* progressBar = findViewById(R.id.progressBar);*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        billingService.onDestroy();
    }
}