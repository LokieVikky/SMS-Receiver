package com.fastexpo.smsreader.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.fastexpo.smsreader.dataentitty.Message;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message")
    List<Message> getAll();

    @Query("SELECT * FROM message WHERE is_uploaded == :isSent")
    List<Message> getAll(Boolean isSent);

    @Query("DELETE FROM message")
    void clearLogs();

    @Insert
    void insertAll(Message... users);

    @Query("UPDATE message set is_uploaded = 1, status=:status WHERE mid = :mid")
    void updateMessage(int mid, String status);

    @Delete
    void delete(Message user);

}
