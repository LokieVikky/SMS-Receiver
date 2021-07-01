package com.fastexpo.smsreader.dataentitty;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int mid;

    @ColumnInfo(name = "message_from")
    public String messageFrom;

    @ColumnInfo(name = "message")
    public String messageText;

    @ColumnInfo(name = "received_time")
    public String receivedTime;

    @ColumnInfo(name = "is_uploaded")
    public Boolean isSent;

    @ColumnInfo(name = "status")
    public String status;
}