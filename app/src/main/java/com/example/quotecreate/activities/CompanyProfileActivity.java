package com.example.quotecreate.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quotecreate.R;
import com.example.quotecreate.database.AppDatabase;
import com.example.quotecreate.models.Company;

public class CompanyProfileActivity extends AppCompatActivity {

    private EditText etName, etRegNo, etTaxNo, etAddress;
    private Button btnSave;
    private AppDatabase db;
    private Company existingCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_profile);

        etName = findViewById(R.id.etCompanyName);
        etRegNo = findViewById(R.id.etRegistrationNo);
        etTaxNo = findViewById(R.id.etTaxNo);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSaveCompany);

        db = AppDatabase.getInstance(this);

        // Load existing data if any
        new Thread(() -> {
            existingCompany = db.companyDao().getCompany();
            runOnUiThread(() -> {
                if (existingCompany != null) {
                    etName.setText(existingCompany.name);
                    etRegNo.setText(existingCompany.registrationNo);
                    etTaxNo.setText(existingCompany.taxNo);
                    etAddress.setText(existingCompany.addressLines);
                }
            });
        }).start();

        btnSave.setOnClickListener(v -> saveCompany());
    }

    private void saveCompany() {
        String name = etName.getText().toString().trim();
        String regNo = etRegNo.getText().toString().trim();
        String taxNo = etTaxNo.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Company Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Company company = existingCompany == null ? new Company() : existingCompany;
            company.name = name;
            company.registrationNo = regNo;
            company.taxNo = taxNo;
            company.addressLines = address;
            // logoPath remains null for now

            if (existingCompany == null) {
                db.companyDao().insert(company);
            } else {
                db.companyDao().update(company);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Company saved", Toast.LENGTH_SHORT).show();
                finish(); // go back to quote list
            });
        }).start();
    }
}