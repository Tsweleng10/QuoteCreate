package com.example.quotecreate.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "line_items")
public class LineItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long quoteId;      // This matches the query!
    public int rowNumber;     // This matches the query!
    public String description;
    public double quantity;
    public String unit;       // "hr"
    public double rate;
}