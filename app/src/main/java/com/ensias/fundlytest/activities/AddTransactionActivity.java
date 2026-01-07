package com.ensias.fundlytest.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.models.Category;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddTransactionActivity extends AppCompatActivity {

    private DataManager dataManager;
    private TabLayout tabLayout;
    private EditText etDate;
    private ImageButton btnPickDate;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private TextView btnAddCategoryInline;
    private EditText noteInput;
    private Button btnSave;

    private List<Category> categories = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private String currentType = "expense";
    private Date selectedDate = new Date();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dataManager = new DataManager();

        setupViews();
        setupTabs();
        setupDatePicker();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.close();
        }
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageButton btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> openAddCategory());
        }

        tabLayout = findViewById(R.id.tabLayout);
        etDate = findViewById(R.id.etDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddCategoryInline = findViewById(R.id.btnAddCategoryInline);
        noteInput = findViewById(R.id.noteInput);
        btnSave = findViewById(R.id.btnSaveTransaction);

        etDate.setText(dateFormat.format(selectedDate));

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Expenses"), true);
        tabLayout.addTab(tabLayout.newTab().setText("Income"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentType = tab.getPosition() == 0 ? "expense" : "income";
                loadCategories();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadCategories();
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> showDatePicker());
        btnPickDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth, 12, 0, 0);
                    selectedDate = c.getTime();
                    etDate.setText(dateFormat.format(selectedDate));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void setupActions() {
        btnAddCategoryInline.setOnClickListener(v -> openAddCategory());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void openAddCategory() {
        Intent intent = new Intent(this, AddCategoryActivity.class);
        startActivity(intent);
    }

    private void loadCategories() {
        categories.clear();
        categories.addAll(dataManager.getCategoriesByType(currentType));
        updateSpinner();
    }

    private void updateSpinner() {
        List<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.getName());
        }

        spinnerAdapter.clear();
        spinnerAdapter.addAll(names);
        spinnerAdapter.notifyDataSetChanged();

        if (!categories.isEmpty()) {
            spinnerCategory.setSelection(0);
        }
    }

    private void saveTransaction() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Please add a category first", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spinnerCategory.getSelectedItemPosition();
        if (pos < 0 || pos >= categories.size()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = categories.get(pos);

        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount == 0) {
            Toast.makeText(this, "Amount cannot be 0", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = noteInput.getText().toString().trim();
        if (TextUtils.isEmpty(note)) {
            note = selectedCategory.getName() + " transaction";
        }

        // Add transaction using DataManager
        dataManager.addTransaction(
                UUID.randomUUID().toString(),
                amount,
                selectedCategory.getId(),
                currentType,
                note,
                selectedDate,
                selectedCategory.getColor(),
                selectedCategory.getIconName()
        );

        Toast.makeText(this, "Saved ✅", Toast.LENGTH_SHORT).show();
        finish();
    }
}