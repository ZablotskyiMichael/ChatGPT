package com.quantumquontity.chatgpt.dao;

import static com.quantumquontity.chatgpt.dao.DBHelper.CHAT_TABLE;
import static com.quantumquontity.chatgpt.dao.DBHelper.CHAT_TABLE_NAME_COLUMN;
import static com.quantumquontity.chatgpt.dao.DBHelper.ID_COLUMN;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.quantumquontity.chatgpt.data.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatDao {

    private final DBHelper dbHelper;

    public ChatDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Получить список чатов
     */
    public List<Chat> getChatList() {
        List<Chat> res = new ArrayList<>();
        // получение базы данных для чтения
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(CHAT_TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            // индексы колонок
            int idColumnIndex = cursor.getColumnIndex(ID_COLUMN);
            int chatNameIndex = cursor.getColumnIndex(CHAT_TABLE_NAME_COLUMN);
            do {
                res.add(new Chat(cursor.getLong(idColumnIndex),
                        cursor.getString(chatNameIndex)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        sqLiteDatabase.close();
        return res;
    }

    /**
     * Сохранение чата
     */
    public long save(Chat chat) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHAT_TABLE_NAME_COLUMN, chat.getName());

        return sqLiteDatabase.insert(CHAT_TABLE, null, values);
    }

    /** Обновляет название чата */
    public void updateChatName(long id, String newName) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cv.put(CHAT_TABLE_NAME_COLUMN, newName);
        db.update(CHAT_TABLE, cv, ID_COLUMN + " = ?", new String[]{String.valueOf(id)});
        dbHelper.close();
    }
}
