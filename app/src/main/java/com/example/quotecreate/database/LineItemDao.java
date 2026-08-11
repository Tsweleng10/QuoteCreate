package com.example.quotecreate.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quotecreate.models.LineItem;

import java.util.List;

@Dao
public interface LineItemDao {
    @Insert
    void insert(LineItem item);

    @Update
    void update(LineItem item);

    @Delete
    void delete(LineItem item);

    @Query("SELECT * FROM line_items WHERE quoteId = :quoteId ORDER BY rowNumber ASC")
    List<LineItem> getItemsForQuote(long quoteId);

    @Query("DELETE FROM line_items WHERE quoteId = :quoteId")
    void deleteAllForQuote(long quoteId);
}