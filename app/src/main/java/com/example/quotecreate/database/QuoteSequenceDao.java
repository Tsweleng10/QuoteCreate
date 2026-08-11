package com.example.quotecreate.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quotecreate.models.QuoteSequence;

@Dao
public interface QuoteSequenceDao {
    @Insert
    void insert(QuoteSequence seq);

    @Query("SELECT * FROM quote_sequence WHERE id = 1")
    QuoteSequence getSequence();

    @Update
    void update(QuoteSequence seq);
}