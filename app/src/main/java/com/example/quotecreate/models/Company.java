package com.example.quotecreate.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "company")
public class Company
{
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String registrationNo;
    public String taxNo;
    public String addressLines; // store as JSON string or newline-separated
    public String logoPath;      // path to saved logo image (optional)
}