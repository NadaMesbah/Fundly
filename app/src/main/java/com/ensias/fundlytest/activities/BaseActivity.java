package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.fundlytest.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_bar_container);

        setupNavigation();
        highlightCurrentTab();
    }

    private void setupNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navTransactions = findViewById(R.id.nav_transactions);
        LinearLayout navReports = findViewById(R.id.nav_reports);
        LinearLayout navSettings = findViewById(R.id.nav_settings);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!(this instanceof MainActivity)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }

        if (navTransactions != null) {
            navTransactions.setOnClickListener(v -> {
                if (!(this instanceof TransactionsActivity)) {
                    Intent intent = new Intent(this, TransactionsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }

        if (navReports != null) {
            navReports.setOnClickListener(v -> {
                if (!(this instanceof ReportsActivity)) {
                    Intent intent = new Intent(this, ReportsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                // Currently does nothing, can implement settings later
            });
        }
    }

    protected void highlightCurrentTab() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navTransactions = findViewById(R.id.nav_transactions);
        LinearLayout navReports = findViewById(R.id.nav_reports);
        LinearLayout navSettings = findViewById(R.id.nav_settings);

        resetTabColors(navHome);
        resetTabColors(navTransactions);
        resetTabColors(navReports);
        resetTabColors(navSettings);

        if (this instanceof MainActivity && navHome != null) {
            highlightTab(navHome);
        } else if (this instanceof TransactionsActivity && navTransactions != null) {
            highlightTab(navTransactions);
        } else if (this instanceof ReportsActivity && navReports != null) {
            highlightTab(navReports);
        }
    }

    private void resetTabColors(LinearLayout tab) {
        if (tab != null && tab.getChildCount() > 1) {
            TextView textView = (TextView) tab.getChildAt(1);
            if (textView != null) {
                textView.setTextColor(Color.parseColor("#757575"));
            }
        }
    }

    private void highlightTab(LinearLayout tab) {
        if (tab != null && tab.getChildCount() > 1) {
            TextView textView = (TextView) tab.getChildAt(1);
            if (textView != null) {
                textView.setTextColor(Color.parseColor("#3869EA"));
            }
        }
    }
}