package com.quantumquontity.chatgpt.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Вспомогательный класс для работы с БД */
public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "chatGpt";

    public static final String ID_COLUMN = "_id";

    /** Таблица Чат */
    public static final String CHAT_TABLE = "chat";
    public static final String CHAT_TABLE_NAME_COLUMN = "name";

    /** Таблица Сообщение */
    public static final String CHAT_MESSAGE_TABLE = "message";
    public static final String CHAT_MESSAGE_CHAT_TABLE_ID_COLUMN = "chat_id";
    public static final String CHAT_MESSAGE_TEXT_COLUMN = "text";
    public static final String CHAT_MESSAGE_USER_ROLE_COLUMN = "user_role";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Выполняется при первом запуске приложения и инициализирует Базу Данных */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы чатов
        createChatTable(db);
        // Создание таблицы сообщений
        createMessageTable(db);
    }

    private void createChatTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CHAT_TABLE + " (" +
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CHAT_TABLE_NAME_COLUMN + " TEXT" +
                ")");
    }

    private void createMessageTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CHAT_MESSAGE_TABLE + " (" +
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CHAT_MESSAGE_CHAT_TABLE_ID_COLUMN + " INTEGER," +
                CHAT_MESSAGE_TEXT_COLUMN + " TEXT," +
                CHAT_MESSAGE_USER_ROLE_COLUMN + " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }
}
