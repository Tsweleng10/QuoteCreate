package com.example.quotecreate.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quotecreate.R;
import com.example.quotecreate.database.AppDatabase;
import com.example.quotecreate.models.Company;
import com.example.quotecreate.models.Quote;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class QuoteListActivity extends AppCompatActivity {

    private RecyclerView rvQuotes;
    private FloatingActionButton fabAdd;
    private AppDatabase db;
    private QuoteAdapter adapter;

    // Interface for click events
    public interface OnQuoteClickListener {
        void onQuoteClick(Quote quote);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_list);

        rvQuotes = findViewById(R.id.rvQuotes);
        fabAdd = findViewById(R.id.fabAddQuote);
        db = AppDatabase.getInstance(this);

        rvQuotes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuoteAdapter(this, quote -> {
            Intent intent = new Intent(this, QuoteFormActivity.class);
            intent.putExtra("quoteId", quote.id);
            startActivity(intent);
        });
        rvQuotes.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            new Thread(() -> {
                Company company = db.companyDao().getCompany();
                runOnUiThread(() -> {
                    if (company == null) {
                        startActivity(new Intent(this, CompanyProfileActivity.class));
                    } else {
                        startActivity(new Intent(this, QuoteFormActivity.class));
                    }
                });
            }).start();
        });

        loadQuotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuotes();
    }

    private void loadQuotes() {
        new Thread(() -> {
            List<Quote> list = db.quoteDao().getAllQuotes();
            runOnUiThread(() -> adapter.setQuotes(list));
        }).start();
    }

    // ========== Inner Adapter ==========
    public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {
        private List<Quote> quotes;
        private OnQuoteClickListener listener;
        private Context context;

        public QuoteAdapter(Context context, OnQuoteClickListener listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate the simple list item layout with two TextViews
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new QuoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(QuoteViewHolder holder, int position) {
            Quote q = quotes.get(position);
            holder.text1.setText(q.quoteNumber);
            holder.text2.setText(q.reference + " - " + q.date);
            holder.itemView.setOnClickListener(v -> listener.onQuoteClick(q));
        }

        @Override
        public int getItemCount() {
            return quotes == null ? 0 : quotes.size();
        }

        public void setQuotes(List<Quote> list) {
            this.quotes = list;
            notifyDataSetChanged();
        }

        // ViewHolder inner class
        public class QuoteViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            public QuoteViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}