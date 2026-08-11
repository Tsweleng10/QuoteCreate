package com.example.quotecreate.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quotecreate.models.Quote;

import java.util.List;

@Dao
public interface QuoteDao {
    @Insert
    long insert(Quote quote);

    @Update
    void update(Quote quote);

    @Delete
    void delete(Quote quote);

    @Query("SELECT * FROM quotes ORDER BY date DESC")
    List<Quote> getAllQuotes();

    @Query("SELECT * FROM quotes WHERE id = :id")
    Quote getQuoteById(long id);
}