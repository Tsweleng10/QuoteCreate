package com.example.quotecreate.models;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "quote_sequence")
public class QuoteSequence
{
    @PrimaryKey
    public int id = 1; // only one row
    public int lastNumber; // the last used number
}