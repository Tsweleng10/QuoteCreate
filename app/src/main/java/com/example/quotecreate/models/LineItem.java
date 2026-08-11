package com.example.quotecreate.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "quotes")
public class LineItem
{
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long quoteId;         // foreign key
    public int rowNumber;        // display index
    public String description;
    public double quantity;
    public String unit;          // "hr"
    public double rate;
    // amount is computed: quantity * rate (not stored)
}
