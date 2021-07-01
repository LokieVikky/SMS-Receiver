package com.fastexpo.smsreader.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.fastexpo.smsreader.dataentitty.Keyword;
import com.fastexpo.smsreader.dataentitty.Message;
import com.fastexpo.smsreader.enums.FilterType;

import java.util.List;

@Dao
public interface KeywordDao {

    @Query("SELECT * FROM keyword WHERE filter_type == :filterType")
    List<Keyword> getKeywords(FilterType filterType);

    @Query("DELETE FROM keyword where filter_type = :filterType")
    void clearLogs(FilterType filterType);

    @Insert
    void insertAll(Keyword... keywords);

    @Query("SELECT * FROM keyword where kwid = :id")
    Keyword getKeyword(int id);

    @Delete
    void delete(Keyword keyword);

}
