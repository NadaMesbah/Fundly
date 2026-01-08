package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.fundlytest.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.fragment_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.activity_main, container, true);

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