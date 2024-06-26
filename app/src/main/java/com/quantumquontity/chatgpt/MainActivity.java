package com.quantumquontity.chatgpt;

import static com.facebook.FacebookSdk.setAutoLogAppEventsEnabled;
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
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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
import com.quantumquontity.chatgpt.service.GlobalValuesHolder;
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
import java.util.Map;
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
    private static final String AD_UNIT_ID = "ca-app-pub-6736423261679020/2309574074";
    private static final String MISHA_ZABLOTSKYI_SAMSUNG_S20_PLUS = "176DA669938D905F7ED33FCEDF06C3E1";


    /**
     * Вспомогательный класс для работы с БД
     */
    private DBHelper dbHelper;

    private PointService pointService;

    private RewardedAd rewardedAd;

    /**
     * Счетчик неудачных загрузок рекламы
     * Если будет 5 - больше не пытаемся грузить.
     */
    private int adFailLoadCounter = 0;
    private boolean rewardedAdIsLoading = false;

    private BillingService billingService;

    private ChatService chatService;
    private ChatMessageService chatMessageService;

    private GlobalValuesHolder globalValuesHolder;
    private ImageView catLogoImageView;
    private TextInputLayout inputMessageLayout;
    private ImageView chatsIconMainPage;
    private ImageView chatsIconChatPage;
    private EditText inputMessage;

    private Button startChatButton;
    private Button subscription_1_month;
    private Button subscription_3_month;
    private Button subscription_12_month;
    private LinearLayout messagesLayout;
    private RecyclerView messagesRecyclerView;
    private LinearLayout exampleRequest;
    private LinearLayout catLogoWrapper;
    private MessageCardViewAdapter messageCardViewAdapter;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private LinearLayout clearAllChat;
    private LinearLayout createNewChat;
    private LinearLayout showAdsLinerLayoutMainPage;
    private LinearLayout showAdsLinerLayoutChatPage;
    private ActionBarDrawerToggle drawerToggle;
    private TextView quantityTokenMainPage;
    private TextView quantityTokenChatPage;
    private TextView premiumExistLabel;
    private TextView requestRespawnTimerMain;
    private TextView requestRespawnTimerChat;
    private ConstraintLayout premiumExistLabelWrapper;
    private FloatingActionButton floatingScrollDownButton;
    private ExtendedFloatingActionButton floatingStopGenerationButton;
    private Button buyPremiumChatButton;
    private Button buttonTabRequestOne;
    private Button buttonTabRequestTwo;
    private Button buttonTabRequestThree;
    private Button buttonTabRequestFour;

    /**
     * Вспомогательный класс чтоб понять где мы сейчас.
     */
    private SubPage subPage = SubPage.MAIN;
    private Map<SubPage, ViewGroup> subPages = new HashMap<>();
    private long currentChatId = -1;
    private com.quantumquontity.chatgpt.data.ChatMessage currentChatMessage = null;

    private boolean chatGptIsWriting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setClientToken("8004da0cb7c5b562aa3d69417788a5b8");
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(getColor(R.color.background));
        getWindow().setNavigationBarColor(getColor(R.color.background));

        findElement();
        initGlobalValues();
        initPages();
        initServices();
        initData();
        setTestAdConfig();
        loadAd();
        setOnClickListeners();
        enableAutoLogs();
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    public void enableAutoLogs () {
        setAutoLogAppEventsEnabled(true);
    }

    private void setTestAdConfig() {
        MobileAds.initialize(this);
        // Добавляю свой телефон как тестовое устройство
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(Arrays.asList(MISHA_ZABLOTSKYI_SAMSUNG_S20_PLUS))
                .build();

        MobileAds.setRequestConfiguration(configuration);
    }

    private void initGlobalValues() {
        globalValuesHolder = new GlobalValuesHolder(this.getResources());
    }

    public GlobalValuesHolder getGlobalValuesHolder() {
        return globalValuesHolder;
    }

    private void initPages() {
        for (SubPage subPage : SubPage.getAll()) {
            ViewGroup subPageView = findViewById(subPage.getPageWrapperId());
            subPages.put(subPage, subPageView);
        }
        showPage(SubPage.MAIN);
    }

    public List<TextView> getRequestRespawnTimerTextViews(){
        return Arrays.asList(requestRespawnTimerMain, requestRespawnTimerChat);
    }

    private void loadAd() {
        if (rewardedAd == null && adFailLoadCounter < 3) {
            rewardedAdIsLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(this, AD_UNIT_ID,
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.toString());
                            rewardedAd = null;
                            rewardedAdIsLoading = false;
                            adFailLoadCounter++;
                            loadAd();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            adFailLoadCounter = 0;
                            rewardedAd = ad;
                            rewardedAdIsLoading = false;
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
                                    loadAd();
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
            setCurrentPointsOnUI();
        }
    }

    public void setCurrentPointsOnUI(){
        int currentPoints = pointService.getCurrentPoints();
        quantityTokenMainPage.setText(currentPoints + "/" + PointService.MAX_POINTS_VALUE);
        quantityTokenChatPage.setText(currentPoints + "/" + PointService.MAX_POINTS_VALUE);
    }

    private void initChats() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setReverseLayout(false);
        llm.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(llm);
        messagesRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        messageCardViewAdapter = new MessageCardViewAdapter(this, new ArrayList<>());
        messagesRecyclerView.setAdapter(messageCardViewAdapter);

        // Добавляем случатель видно ли последнее сообщение на экране,
        // и если нет - отображаем кнопку прокрутки к последнему элементу
        messagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                if (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1) {
                    floatingScrollDownButton.setVisibility(View.GONE);
                } else {
                    floatingScrollDownButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initServices() {
        dbHelper = new DBHelper(this);
        chatService = new ChatService(new ChatDao(dbHelper));
        chatMessageService = new ChatMessageService(new ChatMessageDao(dbHelper));
        billingService = new BillingService(this, this::onExistPremium);
        pointService = new PointService(this);
    }

    private void setOnClickListeners() {
        inputMessageLayout.setEndIconOnClickListener(this::onSendMessage);
        initChatsOnClickListeners();
        startChatButton.setOnClickListener(this::onStartChatClick);

        clearAllChat.setOnClickListener(this::deleteAllChat);
        createNewChat.setOnClickListener(view -> {
            if (currentChatId < 0 || !chatMessageService.getChatMessagesList(currentChatId).isEmpty()) {
                onStartChatClick(view);
            } else {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        showAdsLinerLayoutMainPage.setOnClickListener(this::showDialogAdOrPremium);
        showAdsLinerLayoutChatPage.setOnClickListener(this::showDialogAdOrPremium);

        subscription_1_month.setOnClickListener(view -> buy1MonthSubscription());
        subscription_3_month.setOnClickListener(view -> buy3MonthsSubscription());
        subscription_12_month.setOnClickListener(view -> buy12MonthsSubscription());

        buyPremiumChatButton.setOnClickListener(view -> openSubscribePage());
        buttonTabRequestOne.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestOne));
        buttonTabRequestTwo.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestTwo));
        buttonTabRequestThree.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestThree));
        buttonTabRequestFour.setOnClickListener(view -> setInputTextFromExample(view, buttonTabRequestFour));
        floatingScrollDownButton.setOnClickListener(view ->
                messagesRecyclerView.smoothScrollToPosition(messageCardViewAdapter.getItemCount()));
        floatingStopGenerationButton.setOnClickListener(view -> {
            if(chatGptIsWriting){
                // остановить печать
                chatGptIsWriting = false;
                floatingStopGenerationButton.setText(R.string.gerenerate_response);
            } else {
                // перегенерировать ответ
                List<com.quantumquontity.chatgpt.data.ChatMessage> chatMessagesList = chatMessageService.getChatMessagesList(currentChatId);
                chatMessageService.deleteChatMessage(chatMessagesList.get(chatMessagesList.size() - 1).getId());
                uploadMessagesForCurrentChat();
                onSendMessage(inputMessageLayout, null);
            }
            messagesRecyclerView.smoothScrollToPosition(messageCardViewAdapter.getItemCount());
        });
    }

    private void showDialogAdOrPremium(View view) {
        // Создание алерт-диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.show_ad_dialog, null);

        TextView quantityTokenDialog = dialogView.findViewById(R.id.quantityTokenDialog);
        quantityTokenDialog.setText(String.valueOf(pointService.getCurrentPoints()));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.showAdDialogButton).setOnClickListener(v -> {
            showADsAndGetPoint(v);
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.buyPremiumChatButtonDialog).setOnClickListener(v -> {
            openSubscribePage();
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.closeDialog).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void setInputTextFromExample(View view, Button button) {
        inputMessage.setText(button.getText().toString());
    }

    public void onExistPremium() {
        premiumExistLabel.setVisibility(View.VISIBLE);
        premiumExistLabelWrapper.setVisibility(View.VISIBLE);
        showAdsLinerLayoutMainPage.setVisibility(View.GONE);
        showAdsLinerLayoutChatPage.setVisibility(View.GONE);
        buyPremiumChatButton.setVisibility(View.GONE);
        requestRespawnTimerChat.setVisibility(View.GONE);
        requestRespawnTimerMain.setVisibility(View.GONE);
        initPoints();
    }

    private void buy1MonthSubscription() {
        toMainPage();
        billingService.buy1MonthSubscription();
    }

    private void buy3MonthsSubscription() {
        toMainPage();
        billingService.buy3MonthsSubscription();
    }

    private void buy12MonthsSubscription() {
        toMainPage();
        billingService.buy12MonthsSubscription();
    }

    public void showPage(SubPage newPage) {
        this.subPage = newPage;
        subPages.forEach((page, view) -> {
            view.setVisibility(page == newPage ? View.VISIBLE : View.GONE);
        });
    }

    private void openSubscribePage() {
        // Проставим актуальные цены
        subscription_1_month.setText(getText(R.string.month1) + "\n" + billingService.get1MonthSubscriptionCost());
        subscription_3_month.setText(getText(R.string.months3) + "\n" + billingService.get3MonthsSubscriptionCost());
        subscription_12_month.setText(getText(R.string.months12) + "\n" + billingService.get12MonthsSubscriptionCost());

        showPage(SubPage.SUBSCRIBE);
    }

    private void showADsAndGetPoint(View view) {
        if (rewardedAd != null) {
            Activity activityContext = MainActivity.this;
            rewardedAd.show(activityContext, rewardItem -> {
                // Handle the reward.
                Log.d(TAG, "The user earned the reward.");
                int rewardAmount = rewardItem.getAmount();
                // rewardItem.getType() - пока не используем. Но можт еще будем.
                int currentPoints = pointService.getCurrentPoints();
                currentPoints += rewardAmount;
                pointService.updatePoints(currentPoints);
                setCurrentPointsOnUI();
                loadAd();
            });
        } else {
            if (!rewardedAdIsLoading) {
                loadAd();
            }
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
                dropCurrentChatIfEmpty();
                toMainPage();
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

        messageCardViewAdapter.refreshData(new ArrayList<>());
        toChatPage();

        // Сохранение нового чата
        String newChatName = getResources().getString(R.string.new_chat);
        long newChatId = chatService.createNewChat(newChatName);
        currentChatId = newChatId;
        // Добавление чата в меню
        navigationView.getMenu().clear();
        initMenu();
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getMenu().getItem(0).setCheckable(true);
    }

    private void toChatPage() {
        showPage(SubPage.CHAT);
        chatsIconMainPage.setVisibility(View.GONE);
        chatsIconChatPage.setVisibility(View.VISIBLE);
        inputMessageLayout.setVisibility(View.VISIBLE);
        inputMessage.setVisibility(View.VISIBLE);
        catLogoImageView.setVisibility(View.GONE);
        startChatButton.setVisibility(View.GONE);
        messagesRecyclerView.setVisibility(View.GONE);
        messagesLayout.setVisibility(View.GONE);
        exampleRequest.setVisibility(View.VISIBLE);
        catLogoWrapper.setVisibility(View.GONE);
        premiumExistLabel.setVisibility(View.GONE);
        premiumExistLabelWrapper.setVisibility(View.GONE);
        floatingStopGenerationButton.setVisibility(messageCardViewAdapter.getItemCount() > 1 ? View.VISIBLE : View.GONE);
        floatingStopGenerationButton.setText(R.string.gerenerate_response);
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
                hideKeyboard(inputMessage);
            }
        });
        initMenu();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Если открыто меню - закроем его
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (subPage == SubPage.CHAT) {
            if (!chatGptIsWriting) {
                dropCurrentChatIfEmpty();
                if (navigationView.getCheckedItem() != null) {
                    navigationView.getCheckedItem().setChecked(false);
                }
                toMainPage();
            } else {
                Toast.makeText(this, getText(R.string.action_blocked_while_generation), Toast.LENGTH_SHORT).show();
            }
        } else if (subPage == SubPage.SUBSCRIBE) {
            toMainPage();
        } else {
            super.onBackPressed();
        }
    }

    private void dropCurrentChatIfEmpty() {
        if (currentChatId > 0 && chatMessageService.getChatMessagesList(currentChatId).isEmpty()) {
            chatService.deleteChat(currentChatId);
            currentChatId = -1;
            currentChatMessage = null;
            navigationView.getMenu().clear();
            initMenu();
        }
    }

    private void toMainPage() {
        showPage(SubPage.MAIN);
        currentChatId = -1;
        chatsIconMainPage.setVisibility(View.VISIBLE);
        chatsIconChatPage.setVisibility(View.GONE);
        inputMessage.setVisibility(View.GONE);
        catLogoImageView.setVisibility(View.VISIBLE);
        startChatButton.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.GONE);
        messagesLayout.setVisibility(View.GONE);
        catLogoWrapper.setVisibility(View.VISIBLE);
        if (billingService.isPremium()) {
            premiumExistLabel.setVisibility(View.VISIBLE);
            premiumExistLabelWrapper.setVisibility(View.VISIBLE);
            showAdsLinerLayoutMainPage.setVisibility(View.GONE);
            showAdsLinerLayoutChatPage.setVisibility(View.GONE);
            requestRespawnTimerChat.setVisibility(View.GONE);
            requestRespawnTimerMain.setVisibility(View.GONE);
        }
        exampleRequest.setVisibility(View.GONE);
        dropCurrentChatIfEmpty();
        if (navigationView.getCheckedItem() != null) {
            navigationView.getCheckedItem().setChecked(false);
        }
    }

    private void initMenu() {
        Menu menu = navigationView.getMenu();

        List<Chat> sortedList = chatService.getAll().stream()
                .sorted(Comparator.comparingLong(chat -> -chat.getId()))
                .collect(Collectors.toList());
        for (Chat chat : sortedList) {
            MenuItem menuItem = menu.add(Menu.NONE, (int) chat.getId(), Menu.NONE, "");
            menuItem.setCheckable(true);
            if (currentChatId == chat.getId()) {
                menuItem.setChecked(true);
            }
            menuItem.setActionView(R.layout.menu_item_layout);
            ImageView menuIcon = menuItem.getActionView().findViewById(R.id.menu_icon);
            TextView menuTitle = menuItem.getActionView().findViewById(R.id.menu_title);
            Button deleteChatButton = menuItem.getActionView().findViewById(R.id.deleteChatButton);
            ImageView imageEditNameChat = menuItem.getActionView().findViewById(R.id.imageEditNameChat);
            menuIcon.setImageResource(R.drawable.round_message_24);
            menuTitle.setText(chat.getName());
            imageEditNameChat.setOnClickListener(v -> {
                if (!chatGptIsWriting) {
                    EditText editText = new EditText(MainActivity.this);
                    editText.setText(chat.getName());
                    // Ограничиваем количество строк в поле редактирования
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                    editText.setMaxLines(2);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.edit_chat_name);
                    builder.setView(editText);
                    builder.setPositiveButton(R.string.apply, (dialog, which) -> {
                        String newText = editText.getText().toString();
                        // Проверяем длину текста
                        if (newText.length() < 3) {
                            // Если текст меньше 3 символов, выводим ошибку
                            Toast.makeText(MainActivity.this, R.string.min_length_3_characters, Toast.LENGTH_SHORT).show();
                        } else if (newText.length() > 40) {
                            // Если текст больше 40 символов, выводим ошибку
                            Toast.makeText(MainActivity.this, R.string.max_length_40_characters, Toast.LENGTH_SHORT).show();
                        } else {
                            // Если текст соответствует условиям
                            chatService.updateChatName(chat.getId(), editText.getText().toString());
                            Toast.makeText(MainActivity.this, R.string.chat_successfully_renamed, Toast.LENGTH_SHORT).show();
                        }
                        navigationView.getMenu().clear();
                        initMenu();
                        drawerLayout.closeDrawer(GravityCompat.START);
                    });
                    builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    Toast.makeText(this, getText(R.string.chat_rename_block_message), Toast.LENGTH_SHORT).show();
                }
            });
            deleteChatButton.setOnClickListener(v -> {
                if (!chatGptIsWriting) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.deletion_confirmation);
                    builder.setMessage(R.string.question_delete_this_chat);
                    builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                        if (chat.getId() == currentChatId) {
                            toMainPage();
                        }
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
                } else {
                    Toast.makeText(this, getText(R.string.chat_delete_block_message), Toast.LENGTH_SHORT).show();
                }
            });

        }

        navigationView.setNavigationItemSelectedListener(item -> {
            if (!chatGptIsWriting) {
                currentChatId = item.getItemId();
                uploadMessagesForCurrentChat();
                if (subPage != SubPage.CHAT) {
                    toChatPage();
                }
                item.setCheckable(true);
                item.setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                floatingStopGenerationButton.setVisibility(messageCardViewAdapter.getItemCount() > 1 ? View.VISIBLE : View.GONE);
                if (exampleRequest.getVisibility() == View.VISIBLE) {
                    exampleRequest.setVisibility(View.GONE);
                    messagesRecyclerView.setVisibility(View.VISIBLE);
                    messagesLayout.setVisibility(View.VISIBLE);
                }
                if (messageCardViewAdapter.getItemCount() == 1) {
                    exampleRequest.setVisibility(View.VISIBLE);
                    messagesRecyclerView.setVisibility(View.GONE);
                    messagesLayout.setVisibility(View.GONE);
                }
                return true;
            } else {
                Toast.makeText(this, getText(R.string.chat_switch_block_message), Toast.LENGTH_SHORT).show();
                return false;
            }
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
            messagesRecyclerView.setVisibility(View.VISIBLE);
            messagesLayout.setVisibility(View.VISIBLE);
        }
        if (inputMessage.getText().toString().isEmpty()) {
            Toast.makeText(this, getText(R.string.enter_request), Toast.LENGTH_SHORT).show();
            return;
        }
        onSendMessage(view, inputMessage.getText().toString());
    }

    private void onSendMessage(View view, String requestMessage) {
        int currentPoints = pointService.getCurrentPoints();
        if (currentPoints > 0 || isInfinityPoints()) {
            if (!isInfinityPoints()) {
                currentPoints--;
                pointService.updatePoints(currentPoints);
                setCurrentPointsOnUI();
            }

            inputMessageLayout.setEndIconOnClickListener(null);
            chatGptIsWriting = true;
            floatingStopGenerationButton.setVisibility(View.VISIBLE);
            floatingStopGenerationButton.setText(R.string.stop_generating);

            // Отключить возможность ввода в inputMessage
            inputMessage.setEnabled(false);

            inputMessage.setText("");

            if(requestMessage != null){
                createAndSaveChatMessage(requestMessage, ChatMessageRole.USER);
            }
            currentChatMessage = createAndSaveChatMessage("", ChatMessageRole.SYSTEM);
            messageCardViewAdapter.notifyItemChanged(messageCardViewAdapter.getItemCount());

            ObjectMapper mapper = defaultObjectMapper();
            OkHttpClient client = defaultClient(TOKEN, Duration.ofSeconds(30));
            Retrofit retrofit = defaultRetrofit(client, mapper);

            OpenAiApi aiApi = retrofit.create(OpenAiApi.class);
            ExecutorService executorService = client.dispatcher().executorService();
            OpenAiServiceCustom service = new OpenAiServiceCustom(aiApi, executorService);
            List<ChatMessage> messages = getLastChatMessages();

            // Установка названия чата
            if (messages.size() == 1 && requestMessage != null) {
                chatService.updateChatName(currentChatId, requestMessage.length() > 40
                        ? requestMessage.substring(0, 40) : requestMessage);
                navigationView.getMenu().clear();
                initMenu();
            }

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
                                    runOnUiThread(() -> {
                                        enableInput();
                                        chatGptIsWriting = false;
                                        floatingStopGenerationButton.setText(R.string.gerenerate_response);
                                    }),
                            this);
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        int points = pointService.getCurrentPoints();
                        points++;
                        pointService.updatePoints(points);
                        setCurrentPointsOnUI();
                        currentChatMessage.setText(getString(R.string.unknown_error));
                        System.out.println("Request error: " + e);
                        messageCardViewAdapter.updateLastItemText(currentChatMessage.getText());
                        enableInput();
                        chatGptIsWriting = false;
                        floatingStopGenerationButton.setText(R.string.gerenerate_response);
                    });
                } finally {
                    runOnUiThread(() -> {
                        chatGptIsWriting = false;
                        floatingStopGenerationButton.setText(R.string.gerenerate_response);
                    });
                }
            }).start();
        } else {
            showDialogAdOrPremium(view);
        }
    }

    public boolean isInfinityPoints() {
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
        int lastPosition = messageCardViewAdapter.addItem(new ChatMessageCardView(savedMessage));
        messagesRecyclerView.getLayoutManager().scrollToPosition(lastPosition);
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
        // inputMessageLayout.setEndIconDrawable(R.drawable.baseline_send_24);
        // inputMessageLayout.setEndIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.iconEnd)));
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
        floatingScrollDownButton = findViewById(R.id.floatingScrollDownButton);
        floatingStopGenerationButton = findViewById(R.id.floatingStopGenerationButton);
        requestRespawnTimerMain = findViewById(R.id.requestRespawnTimerMain);
        requestRespawnTimerChat = findViewById(R.id.requestRespawnTimerChat);

        subscription_1_month = findViewById(R.id.subscription_1_month);
        subscription_3_month = findViewById(R.id.subscription_3_month);
        subscription_12_month = findViewById(R.id.subscription_12_month);

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

        showAdsLinerLayoutMainPage = findViewById(R.id.showAdsLinerLayoutMainPage);
        showAdsLinerLayoutChatPage = findViewById(R.id.showAdsLinerLayoutChatPage);

        /* progressBar = findViewById(R.id.progressBar);*/
    }

    public boolean isChatGptIsWriting() {
        return chatGptIsWriting;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        billingService.onDestroy();
    }
}