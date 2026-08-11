package com.example.quotecreate.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quotecreate.R;
import com.example.quotecreate.database.AppDatabase;
import com.example.quotecreate.models.Company;
import com.example.quotecreate.models.LineItem;
import com.example.quotecreate.models.Quote;
import com.example.quotecreate.models.QuoteSequence;
import com.example.quotecreate.utils.PdfGenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuoteFormActivity extends AppCompatActivity {

    private EditText etReference, etDate, etDefaultRate;
    private RecyclerView rvLineItems;
    private Button btnAddRow, btnGeneratePDF, btnSaveQuote;
    private TextView tvTotal;
    private AppDatabase db;
    private LineItemAdapter adapter;
    private List<LineItem> lineItems = new ArrayList<>();
    private long editingQuoteId = -1;
    private Quote existingQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_form);

        etReference = findViewById(R.id.etReference);
        etDate = findViewById(R.id.etDate);
        etDefaultRate = findViewById(R.id.etDefaultRate);
        rvLineItems = findViewById(R.id.rvLineItems);
        btnAddRow = findViewById(R.id.btnAddRow);
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF);
        btnSaveQuote = findViewById(R.id.btnSaveQuote);
        tvTotal = findViewById(R.id.tvTotal);

        db = AppDatabase.getInstance(this);

        long quoteId = getIntent().getLongExtra("quoteId", -1);
        if (quoteId != -1) {
            editingQuoteId = quoteId;
            loadExistingQuote(quoteId);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy", Locale.US);
            etDate.setText(sdf.format(new Date()));
        }

        rvLineItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LineItemAdapter(lineItems, this::updateTotal);
        rvLineItems.setAdapter(adapter);

        btnAddRow.setOnClickListener(v -> {
            double defaultRate = getDefaultRate();
            LineItem item = new LineItem();
            item.rowNumber = lineItems.size() + 1;
            item.unit = "hr";
            item.quantity = 0;
            item.rate = defaultRate;
            lineItems.add(item);
            adapter.notifyItemInserted(lineItems.size() - 1);
            updateTotal();
        });

        btnSaveQuote.setOnClickListener(v -> saveQuote());
        btnGeneratePDF.setOnClickListener(v -> generatePdf());

        if (editingQuoteId == -1 && lineItems.isEmpty()) {
            btnAddRow.performClick();
        }
    }

    private void loadExistingQuote(long quoteId) {
        new Thread(() -> {
            existingQuote = db.quoteDao().getQuoteById(quoteId);
            List<LineItem> items = db.lineItemDao().getItemsForQuote(quoteId);
            runOnUiThread(() -> {
                if (existingQuote != null) {
                    etReference.setText(existingQuote.reference);
                    etDate.setText(existingQuote.date);
                    etDefaultRate.setText(String.valueOf(existingQuote.defaultRate));
                    lineItems.clear();
                    lineItems.addAll(items);
                    for (int i = 0; i < lineItems.size(); i++) {
                        lineItems.get(i).rowNumber = i + 1;
                    }
                    adapter.notifyDataSetChanged();
                    updateTotal();
                }
            });
        }).start();
    }

    private double getDefaultRate() {
        String rateStr = etDefaultRate.getText().toString().trim();
        if (rateStr.isEmpty()) return 0;
        try {
            return Double.parseDouble(rateStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateTotal() {
        double total = 0;
        for (LineItem item : lineItems) {
            total += item.quantity * item.rate;
        }
        tvTotal.setText(String.format(Locale.US, "Total: R%,.2f", total));
    }

    private void saveQuote() {
        new Thread(() -> {
            long id = saveQuoteAndGetId();
            if (id != -1) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Quote saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private long saveQuoteAndGetId() {
        String reference = etReference.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        double defaultRate = getDefaultRate();

        if (reference.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Reference is required", Toast.LENGTH_SHORT).show());
            return -1;
        }

        Quote quote = existingQuote == null ? new Quote() : existingQuote;
        quote.reference = reference;
        quote.date = date;
        quote.defaultRate = defaultRate;
        quote.disclaimerText = "This quote covers cost management from initiation to procurement. Construction-phase services require a separate scope/fee proposal.";

        if (editingQuoteId == -1) {
            String number = generateQuoteNumber();
            quote.quoteNumber = number;
            long id = db.quoteDao().insert(quote);
            quote.id = id;
        } else {
            db.quoteDao().update(quote);
            db.lineItemDao().deleteAllForQuote(quote.id);
        }

        for (LineItem item : lineItems) {
            item.quoteId = quote.id;
            db.lineItemDao().insert(item);
        }

        return quote.id;
    }

    private String generateQuoteNumber() {
        QuoteSequence seq = db.sequenceDao().getSequence();
        int last = (seq == null) ? 0 : seq.lastNumber;
        int next = last + 1;
        String number = String.format(Locale.US, "SDQU-%05d", next);
        if (seq == null) {
            seq = new QuoteSequence();
            seq.lastNumber = next;
            db.sequenceDao().insert(seq);
        } else {
            seq.lastNumber = next;
            db.sequenceDao().update(seq);
        }
        return number;
    }

    private void generatePdf() {
        new Thread(() -> {
            long quoteId = saveQuoteAndGetId();
            if (quoteId == -1) return;

            Quote freshQuote = db.quoteDao().getQuoteById(quoteId);
            List<LineItem> freshItems = db.lineItemDao().getItemsForQuote(quoteId);
            Company company = db.companyDao().getCompany();
            if (company == null) {
                runOnUiThread(() -> Toast.makeText(this, "Company profile missing", Toast.LENGTH_SHORT).show());
                return;
            }
            File pdf = PdfGenerator.generateQuotePdf(this, freshQuote, freshItems, company);
            if (pdf != null && pdf.exists()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "PDF generated: " + pdf.getName(), Toast.LENGTH_SHORT).show();
                    sharePdf(pdf);
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sharePdf(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Quote PDF"));
    }

    // ========== Inner Adapter ==========
    public class LineItemAdapter extends RecyclerView.Adapter<LineItemAdapter.ViewHolder> {
        private List<LineItem> items;
        private Runnable onChanged;

        public LineItemAdapter(List<LineItem> items, Runnable onChanged) {
            this.items = items;
            this.onChanged = onChanged;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_line_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LineItem item = items.get(position);
            holder.tvRowNumber.setText(String.valueOf(item.rowNumber));
            holder.etDesc.setText(item.description);
            holder.etQty.setText(item.quantity == 0 ? "" : String.valueOf(item.quantity));
            holder.etRate.setText(item.rate == 0 ? "" : String.valueOf(item.rate));
            updateAmount(holder, item);

            // Description watcher
            holder.etDesc.removeTextChangedListener(holder.descWatcher);
            holder.descWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    item.description = s.toString();
                }
            };
            holder.etDesc.addTextChangedListener(holder.descWatcher);

            // Qty watcher
            holder.etQty.removeTextChangedListener(holder.qtyWatcher);
            holder.qtyWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    String str = s.toString();
                    if (!str.isEmpty()) {
                        try { item.quantity = Double.parseDouble(str); } catch (NumberFormatException e) { item.quantity = 0; }
                    } else { item.quantity = 0; }
                    updateAmount(holder, item);
                    onChanged.run();
                }
            };
            holder.etQty.addTextChangedListener(holder.qtyWatcher);

            // Rate watcher
            holder.etRate.removeTextChangedListener(holder.rateWatcher);
            holder.rateWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    String str = s.toString();
                    if (!str.isEmpty()) {
                        try { item.rate = Double.parseDouble(str); } catch (NumberFormatException e) { item.rate = 0; }
                    } else { item.rate = 0; }
                    updateAmount(holder, item);
                    onChanged.run();
                }
            };
            holder.etRate.addTextChangedListener(holder.rateWatcher);

            // Delete button
            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    for (int i = 0; i < items.size(); i++) {
                        items.get(i).rowNumber = i + 1;
                    }
                    notifyItemRangeChanged(pos, items.size() - pos);
                    onChanged.run();
                }
            });
        }

        private void updateAmount(ViewHolder holder, LineItem item) {
            double amount = item.quantity * item.rate;
            holder.tvAmount.setText(String.format(Locale.US, "R%,.2f", amount));
        }

        @Override
        public int getItemCount() { return items.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRowNumber, tvAmount;
            EditText etDesc, etQty, etRate;
            ImageButton btnDelete;
            TextWatcher descWatcher, qtyWatcher, rateWatcher;

            public ViewHolder(View itemView) {
                super(itemView);
                tvRowNumber = itemView.findViewById(R.id.tvRowNumber);
                etDesc = itemView.findViewById(R.id.etDesc);
                etQty = itemView.findViewById(R.id.etQty);
                etRate = itemView.findViewById(R.id.etRate);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                btnDelete = itemView.findViewById(R.id.btnDeleteRow);
            }
        }
    }
}