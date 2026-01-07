package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.fundlytest.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
    }

    private void setupNavigation() {
        Button btnAddTransaction = findViewById(R.id.btnAddTransaction);
        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        Button btnReports = findViewById(R.id.btnReports);

        if (btnAddTransaction != null) {
            btnAddTransaction.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                startActivity(intent);
            });
        }

        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddCategoryActivity.class);
                startActivity(intent);
            });
        }

        if (btnReports != null) {
            btnReports.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ReportsActivity.class);
                startActivity(intent);
            });
        }
    }
}