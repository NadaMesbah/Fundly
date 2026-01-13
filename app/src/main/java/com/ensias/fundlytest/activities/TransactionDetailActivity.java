package com.ensias.fundlytest.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.models.Category;
import com.ensias.fundlytest.models.Transaction;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {

    private DataManager dataManager;
    private TextView transactionName;
    private TextView transactionType;
    private TextView transactionAmount;
    private TextView transactionDate;
    private TextView transactionNote;
    private ImageView iconImage;
    private View iconBackground;
    private String transactionId;
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_detail);

        transactionId = getIntent().getStringExtra("transaction_id");
        if (transactionId == null) {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dataManager = new DataManager();
        setupViews();
        loadTransactionDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactionDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.close();
        }
    }

    private void setupViews() {
        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Edit button
        findViewById(R.id.editButton).setOnClickListener(v -> {
            Intent intent = new Intent(TransactionDetailActivity.this, AddTransactionActivity.class);
            intent.putExtra("transaction_id", transactionId);
            intent.putExtra("is_edit", true);
            startActivity(intent);
        });

        // Delete button
        findViewById(R.id.deleteButton).setOnClickListener(v -> showDeleteConfirmationDialog());

        // Initialize views
        transactionName = findViewById(R.id.transactionName);
        transactionType = findViewById(R.id.transactionType);
        transactionAmount = findViewById(R.id.transactionAmount);
        transactionDate = findViewById(R.id.transactionDate);
        transactionNote = findViewById(R.id.transactionNote);
        iconImage = findViewById(R.id.iconImage);
        iconBackground = findViewById(R.id.iconBackground);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadTransactionDetails() {
        Transaction transaction = dataManager.getAllTransactions().stream()
                .filter(t -> t.getId().equals(transactionId))
                .findFirst()
                .orElse(null);

        if (transaction != null) {
            updateUI(transaction);
            loadCategoryDetails(transaction.getCategoryId());
        } else {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateUI(Transaction transaction) {
        // Set amount
        String amountText = "DH" + decimalFormat.format(transaction.getAmount());
        transactionAmount.setText(amountText);

        // Set date
        if (transaction.getDate() != null) {
            transactionDate.setText(dateFormat.format(transaction.getDate()));
        }

        // Set type
        String type = transaction.getType();
        transactionType.setText(type.substring(0, 1).toUpperCase() + type.substring(1));

        // Set note
        String note = transaction.getNote();
        if (note != null && !note.trim().isEmpty()) {
            transactionNote.setText(note);
            transactionNote.setVisibility(View.VISIBLE);
        } else {
            transactionNote.setVisibility(View.GONE);
        }

        // Set icon if available
        if (transaction.getIconName() != null) {
            try {
                int resId = getResources().getIdentifier(
                        transaction.getIconName(), "drawable", getPackageName());
                if (resId != 0) {
                    iconImage.setImageResource(resId);
                }
            } catch (Exception e) {
                iconImage.setImageResource(R.drawable.ic_attach_money);
            }
        }

        // Set color if available
        if (transaction.getColor() != 0) {
            iconBackground.setBackgroundColor(transaction.getColor());
        }
    }

    private void loadCategoryDetails(String categoryId) {
        Category category = dataManager.getCategoryById(categoryId);
        if (category != null) {
            transactionName.setText(category.getName());
        } else {
            transactionName.setText("Transaction");
        }
    }

    private void deleteTransaction() {
        dataManager.deleteTransaction(transactionId);
        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
        finish();
    }
}