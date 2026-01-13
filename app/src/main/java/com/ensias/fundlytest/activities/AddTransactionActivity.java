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
import com.ensias.fundlytest.models.Transaction;
import com.ensias.fundlytest.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddTransactionActivity extends AppCompatActivity {

    private DataManager dataManager;
    private SessionManager sessionManager;
    private String currentUserId;

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

    // Edit mode variables
    private String transactionIdToEdit = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // GET CURRENT USER
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dataManager = new DataManager();

        // Check if we're editing an existing transaction
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("transaction_id") && intent.getBooleanExtra("is_edit", false)) {
            transactionIdToEdit = intent.getStringExtra("transaction_id");
            isEditMode = true;
        }

        setupViews();
        setupTabs();
        setupDatePicker();
        setupActions();

        // If editing, load transaction data
        if (isEditMode && transactionIdToEdit != null) {
            loadTransactionForEditing();
        }
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

    private void loadTransactionForEditing() {
        Transaction transactionToEdit = dataManager.getTransactionById(transactionIdToEdit);

        if (transactionToEdit != null) {
            // Verify this transaction belongs to current user
            if (!currentUserId.equals(transactionToEdit.getUserId())) {
                Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentType = transactionToEdit.getType();

            int tabPosition = currentType.equals("expense") ? 0 : 1;
            TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
            if (tab != null) {
                tabLayout.selectTab(tab);
            }

            loadCategories();

            spinnerCategory.post(() -> {
                int categoryPosition = -1;
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId().equals(transactionToEdit.getCategoryId())) {
                        categoryPosition = i;
                        break;
                    }
                }

                if (categoryPosition >= 0) {
                    spinnerCategory.setSelection(categoryPosition);
                }

                etAmount.setText(String.valueOf(transactionToEdit.getAmount()));

                if (transactionToEdit.getDate() != null) {
                    selectedDate = transactionToEdit.getDate();
                    etDate.setText(dateFormat.format(selectedDate));
                }

                if (transactionToEdit.getNote() != null) {
                    noteInput.setText(transactionToEdit.getNote());
                }

                btnSave.setText("Update Transaction");
            });
        } else {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
        }
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
        // FILTER BY USER ID
        categories.addAll(dataManager.getCategoriesByType(currentUserId, currentType));
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

        if (!categories.isEmpty() && !isEditMode) {
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

        if (isEditMode && transactionIdToEdit != null) {
            // Update existing transaction
            dataManager.updateTransaction(
                    transactionIdToEdit,
                    amount,
                    selectedCategory.getId(),
                    currentType,
                    note,
                    selectedDate,
                    selectedCategory.getColor(),
                    selectedCategory.getIconName()
            );
        } else {
            // Add new transaction WITH USER ID
            dataManager.addTransaction(
                    UUID.randomUUID().toString(),
                    currentUserId,  // USER ID
                    amount,
                    selectedCategory.getId(),
                    currentType,
                    note,
                    selectedDate,
                    selectedCategory.getColor(),
                    selectedCategory.getIconName()
            );
        }

        Toast.makeText(this, isEditMode ? "Updated ✅" : "Saved ✅", Toast.LENGTH_SHORT).show();
        finish();
    }
}