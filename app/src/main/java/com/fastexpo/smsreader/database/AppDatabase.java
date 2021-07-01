package com.fastexpo.smsreader.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.fastexpo.smsreader.R;
import com.fastexpo.smsreader.converters.FilterTypeConverter;
import com.fastexpo.smsreader.dao.KeywordDao;
import com.fastexpo.smsreader.dao.MessageDao;
import com.fastexpo.smsreader.dataentitty.Keyword;
import com.fastexpo.smsreader.dataentitty.Message;

@Database(entities = {Message.class,Keyword.class}, version = 1)
@TypeConverters({FilterTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();
    public abstract KeywordDao keywordDao();
    public static volatile AppDatabase INSTANCE;
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, context.getString(R.string.SMSReceiverDB))
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}