package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.adapters.TransactionAdapter;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.models.Transaction;
import com.ensias.fundlytest.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionsActivity extends BaseActivity {

    private DataManager dataManager;
    private SessionManager sessionManager;
    private String currentUserId;
    private RecyclerView transactionsList;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();
    private TextView totalLabel;
    private TextView weekSelector;
    private Button btnAddTransaction;
    private TabLayout tabLayout;
    private String currentType = "expense";
    private Date startDate;
    private Date endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.fragment_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.transaction, container, true);

        // GET CURRENT USER
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dataManager = new DataManager();
        setupViews();
        setupTabs();
        setupDateRange();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.close();
        }
    }

    private void setupViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadTransactions());

        totalLabel = findViewById(R.id.totalLabel);
        weekSelector = findViewById(R.id.weekSelector);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        tabLayout = findViewById(R.id.tabLayout);

        transactionsList = findViewById(R.id.transactionsList);
        transactionsList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(filteredTransactions, transaction -> {
            Intent intent = new Intent(TransactionsActivity.this, TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transaction.getId());
            startActivity(intent);
        });
        transactionsList.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("EXPENSE"), true);
        tabLayout.addTab(tabLayout.newTab().setText("INCOME"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentType = tab.getPosition() == 0 ? "expense" : "income";
                filterAndDisplayTransactions();
                updateTotalLabel();
                updateTotal();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupDateRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTime();

        cal.add(Calendar.WEEK_OF_YEAR, 1);
        endDate = cal.getTime();

        updateWeekSelectorText();

        weekSelector.setOnClickListener(v -> {
            if (weekSelector.getText().toString().contains("week")) {
                Calendar monthCal = Calendar.getInstance();
                monthCal.set(Calendar.DAY_OF_MONTH, 1);
                monthCal.set(Calendar.HOUR_OF_DAY, 0);
                monthCal.set(Calendar.MINUTE, 0);
                monthCal.set(Calendar.SECOND, 0);
                monthCal.set(Calendar.MILLISECOND, 0);
                startDate = monthCal.getTime();

                monthCal.add(Calendar.MONTH, 1);
                endDate = monthCal.getTime();

                weekSelector.setText("This month");
            } else {
                Calendar weekCal = Calendar.getInstance();
                weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                weekCal.set(Calendar.HOUR_OF_DAY, 0);
                weekCal.set(Calendar.MINUTE, 0);
                weekCal.set(Calendar.SECOND, 0);
                weekCal.set(Calendar.MILLISECOND, 0);
                startDate = weekCal.getTime();

                weekCal.add(Calendar.WEEK_OF_YEAR, 1);
                endDate = weekCal.getTime();

                weekSelector.setText("This week");
            }

            filterAndDisplayTransactions();
            updateTotal();
        });
    }

    private void updateWeekSelectorText() {
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.add(Calendar.DAY_OF_YEAR, -1);

        String text = dateFormat.format(startDate) + " - " + dateFormat.format(endCal.getTime());
        weekSelector.setText(text);
    }

    private void loadTransactions() {
        // FILTER BY USER ID
        allTransactions = dataManager.getAllTransactions(currentUserId);
        filterAndDisplayTransactions();
        updateTotal();
    }

    private void filterAndDisplayTransactions() {
        filteredTransactions.clear();

        for (Transaction transaction : allTransactions) {
            boolean typeMatches = transaction.getType().equals(currentType);
            boolean dateMatches = transaction.getDate() != null &&
                    !transaction.getDate().before(startDate) &&
                    transaction.getDate().before(endDate);

            if (typeMatches && dateMatches) {
                filteredTransactions.add(transaction);
            }
        }

        adapter.updateTransactions(filteredTransactions);
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        String typeText = currentType.equals("expense") ? "expenses" : "income";
        totalLabel.setText("Total " + typeText);
    }

    private void updateTotal() {
        // FILTER BY USER ID
        List<Transaction> transactions = dataManager.getTransactionsByDateRange(currentUserId, startDate, endDate);
        double total = 0;

        for (Transaction t : transactions) {
            if (t.getType().equals(currentType)) {
                total += t.getAmount();
            }
        }

        String totalText = decimalFormat.format(total) + " DH";
        totalLabel.setText("Total " + (currentType.equals("expense") ? "expenses" : "income") + ": " + totalText);
    }

    private void setupClickListeners() {
        btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionsActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }
}