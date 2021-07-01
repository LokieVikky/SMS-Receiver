package com.fastexpo.smsreader.dataentitty;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fastexpo.smsreader.enums.FilterType;

@Entity
public class Keyword {
    @PrimaryKey(autoGenerate = true)
    public int kwid;

    @ColumnInfo(name = "keyword")
    public String keyword;

    @ColumnInfo(name = "filter_type")
    public FilterType filterType;


}
