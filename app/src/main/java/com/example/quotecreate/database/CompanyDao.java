package com.example.quotecreate.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quotecreate.models.Company;

@Dao
public interface CompanyDao {
    @Insert
    void insert(Company company);

    @Query("SELECT * FROM company LIMIT 1")
    Company getCompany();

    @Update
    void update(Company company);
}