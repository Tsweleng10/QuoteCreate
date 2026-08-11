package com.example.quotecreate.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quotes")

public class Quote
{
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String quoteNumber;   // e.g., "SDQU-00710"
    public String date;          // store as String "28-Jul-26"
    public String reference;     // project name
    public double defaultRate;   // global hourly rate
    public String disclaimerText;
    // Optionally store total (computed, but can be cached)
}
