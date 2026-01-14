package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.adapters.TransactionAdapter;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.models.Transaction;
import com.google.android.material.imageview.ShapeableImageView;
import com.ensias.fundlytest.utils.SessionManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeActivity extends BaseActivity {

    // Views
    private ShapeableImageView ivProfile;
    private TextView tvBalanceAmount;
    private TextView tvIncomeAmount;
    private TextView tvExpenseAmount;
    private TextView tvViewAll;
    private RecyclerView rvTransactions;
    // Data
    private DataManager dataManager;
    private TransactionAdapter adapter;
    private List<Transaction> recentTransactions;
    private SessionManager sessionManager;
    private String currentUserId;
    private TextView tvWelcomeTitle;

    // Formatage
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate home layout into BaseActivity's fragment container
        FrameLayout container = findViewById(R.id.fragment_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.activity_home, container, true);

        // Initialiser DataManager (Realm)
        dataManager = new DataManager();
        recentTransactions = new ArrayList<>();
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        if (currentUserId == null) {
            // you can redirect to LoginActivity instead if you want
            finish();
            return;
        }
        // Initialiser les vues
        initViews();
        updateWelcomeTitle();

        // Configurer RecyclerView
        setupRecyclerView();

        // Charger les données
        loadData();

        // Configurer les listeners
        setupListeners();

        // Mettre en surbrillance l'onglet Home
        highlightCurrentTab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données quand on revient sur la page
        updateWelcomeTitle();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.close();
        }
        if (adapter != null) {
            adapter.closeDataManager();
        }
    }

    private void initViews() {

        tvBalanceAmount = findViewById(R.id.tv_balance_amount);
        tvIncomeAmount = findViewById(R.id.tv_income_amount);
        tvExpenseAmount = findViewById(R.id.tv_expense_amount);
        tvViewAll = findViewById(R.id.tv_view_all);
        rvTransactions = findViewById(R.id.rv_transactions);
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
    }

    private void setupRecyclerView() {
        // Initialiser l'adaptateur avec la liste vide et un listener
        adapter = new TransactionAdapter(recentTransactions, new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                // Ouvrir les détails de la transaction
                Intent intent = new Intent(HomeActivity.this, TransactionDetailActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }
        });

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
        rvTransactions.setNestedScrollingEnabled(false);
    }

    private void updateWelcomeTitle() {
        if (tvWelcomeTitle == null || sessionManager == null) return;

        // Prefer fullName but display only first name
        String fullName = sessionManager.getFullName();

        String firstName = null;

        if (fullName != null) {
            fullName = fullName.trim();
            if (!fullName.isEmpty()) {
                // split by spaces and take the first part
                String[] parts = fullName.split("\\s+");
                if (parts.length > 0) firstName = parts[0];
            }
        }

        // fallback: email prefix
        if (firstName == null || firstName.isEmpty()) {
            String email = sessionManager.getEmail();
            if (email != null && email.contains("@")) {
                firstName = email.split("@")[0];
            }
        }

        // last fallback
        if (firstName == null || firstName.isEmpty()) {
            firstName = "User";
        }

        tvWelcomeTitle.setText("Welcome, " + firstName + "!");
    }


    private void setupListeners() {
        // Clic sur la photo de profil → ProfileActivity
        if (ivProfile != null) {
            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Créer ProfileActivity
                    // Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    // startActivity(intent);
                }
            });
        }

        // Clic sur "View all" → TransactionsActivity
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, TransactionsActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadData() {
        Date[] currentMonthDates = getCurrentMonthDates();
        Date startDate = currentMonthDates[0];
        Date endDate = currentMonthDates[1];

        double totalIncome = dataManager.getTotalIncome(currentUserId, startDate, endDate);
        double totalExpenses = dataManager.getTotalExpenses(currentUserId, startDate, endDate);
        double balance = totalIncome - totalExpenses;

        updateBalanceCard(balance, totalIncome, totalExpenses);

        loadRecentTransactions(startDate, endDate);
    }


    private Date[] getCurrentMonthDates() {
        Calendar cal = Calendar.getInstance();

        // Début du mois (1er jour à 00:00:00)
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        // Fin du mois (1er jour du mois suivant)
        cal.add(Calendar.MONTH, 1);
        Date endDate = cal.getTime();

        return new Date[]{startDate, endDate};
    }

    private void updateBalanceCard(double balance, double income, double expense) {
        // Balance totale (Income - Expense)
        if (tvBalanceAmount != null) {
            tvBalanceAmount.setText(decimalFormat.format(balance) + " DH");
        }

        // Revenus (vert avec flèche ↑)
        if (tvIncomeAmount != null) {
            tvIncomeAmount.setText("↑ " + formatShortAmount(income) + " DH");
        }

        // Dépenses (rouge avec flèche ↓)
        if (tvExpenseAmount != null) {
            tvExpenseAmount.setText("↓ " + formatShortAmount(expense) + " DH");
        }
    }

    /**
     * Formater les montants en version courte
     * Exemples:
     * - 3900 → $3.9k
     * - 1190000 → $1.2M
     * - 250 → $250.00
     */
    private String formatShortAmount(double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.1fk", amount / 1000);
        } else {
            return decimalFormat.format(amount);
        }
    }

    private void loadRecentTransactions(Date startDate, Date endDate) {
        List<Transaction> allTransactions =
                dataManager.getTransactionsByDateRange(currentUserId, startDate, endDate);

        recentTransactions.clear();

        int count = Math.min(5, allTransactions.size());
        for (int i = 0; i < count; i++) {
            recentTransactions.add(allTransactions.get(i));
        }

        if (adapter != null) {
            adapter.updateTransactions(recentTransactions);
        }
    }

}