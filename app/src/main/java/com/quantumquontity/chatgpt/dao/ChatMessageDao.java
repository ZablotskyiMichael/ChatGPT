package com.quantumquontity.chatgpt.dao;

import static com.quantumquontity.chatgpt.dao.DBHelper.CHAT_MESSAGE_CHAT_TABLE_ID_COLUMN;
import static com.quantumquontity.chatgpt.dao.DBHelper.CHAT_MESSAGE_TABLE;
import static com.quantumquontity.chatgpt.dao.DBHelper.CHAT_MESSAGE_TEXT_COLUMN;
import static com.quantumquontity.chatgpt.dao.DBHelper.CHAT_MESSAGE_USER_ROLE_COLUMN;
import static com.quantumquontity.chatgpt.dao.DBHelper.ID_COLUMN;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.quantumquontity.chatgpt.data.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageDao {

    private final DBHelper dbHelper;

    public ChatMessageDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Получить список сообщений чата.
     */
    public List<ChatMessage> getChatMessagesList(long chatId) {
        List<ChatMessage> res = new ArrayList<>();
        // получение базы данных для чтения
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        String selection = CHAT_MESSAGE_CHAT_TABLE_ID_COLUMN + " = ?";
        String[] selectionArgs = {String.valueOf(chatId)};
        Cursor cursor = sqLiteDatabase.query(CHAT_MESSAGE_TABLE, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            // индексы колонок
            int idColumnIndex = cursor.getColumnIndex(ID_COLUMN);
            int chatIdIndex = cursor.getColumnIndex(CHAT_MESSAGE_CHAT_TABLE_ID_COLUMN);
            int textIndex = cursor.getColumnIndex(CHAT_MESSAGE_TEXT_COLUMN);
            int userRoleIndex = cursor.getColumnIndex(CHAT_MESSAGE_USER_ROLE_COLUMN);
            do {
                res.add(new ChatMessage(cursor.getLong(idColumnIndex),
                        cursor.getLong(chatIdIndex),
                        cursor.getString(textIndex),
                        cursor.getString(userRoleIndex)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        sqLiteDatabase.close();
        return res;
    }

    /**
     * Сохранение сообщения.
     */
    public long save(ChatMessage chatMessage) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHAT_MESSAGE_CHAT_TABLE_ID_COLUMN, chatMessage.getChatId());
        values.put(CHAT_MESSAGE_TEXT_COLUMN, chatMessage.getText());
        values.put(CHAT_MESSAGE_USER_ROLE_COLUMN, chatMessage.getUserRole());

        return sqLiteDatabase.insert(CHAT_MESSAGE_TABLE, null, values);
    }

    /**
     * Удаляет все сообщения чата по id чата.
     */
    public void deleteChatMessagesByChatId(long chatId) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                CHAT_MESSAGE_TABLE,
                CHAT_MESSAGE_CHAT_TABLE_ID_COLUMN + " = ?",
                new String[]{String.valueOf(chatId)}
        );
        dbHelper.close();
    }
}
