package com.example.quotecreate.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.quotecreate.models.Company;
import com.example.quotecreate.models.LineItem;
import com.example.quotecreate.models.Quote;
import com.example.quotecreate.models.QuoteSequence;

@Database(entities = {Company.class, Quote.class, LineItem.class, QuoteSequence.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CompanyDao companyDao();
    public abstract QuoteDao quoteDao();
    public abstract LineItemDao lineItemDao();
    public abstract QuoteSequenceDao sequenceDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "quotation_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}