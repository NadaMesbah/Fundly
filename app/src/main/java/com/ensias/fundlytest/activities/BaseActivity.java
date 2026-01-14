package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.fundlytest.R;
import android.widget.ImageView;
import androidx.core.widget.ImageViewCompat;
import android.content.res.ColorStateList;

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
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!(this instanceof HomeActivity)) {
                    Intent intent = new Intent(this, HomeActivity.class);
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

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!(this instanceof ProfileActivity)) {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    protected void highlightCurrentTab() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navTransactions = findViewById(R.id.nav_transactions);
        LinearLayout navReports = findViewById(R.id.nav_reports);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        resetTabColors(navHome);
        resetTabColors(navTransactions);
        resetTabColors(navReports);
        resetTabColors(navProfile);

        if (this instanceof HomeActivity && navHome != null) {
            highlightTab(navHome);
        } else if (this instanceof TransactionsActivity && navTransactions != null) {
            highlightTab(navTransactions);
        } else if (this instanceof ReportsActivity && navReports != null) {
            highlightTab(navReports);
        } else if (this instanceof ProfileActivity && navProfile != null) {
            highlightTab(navProfile);
        }
    }

    private void resetTabColors(LinearLayout tab) {
        if (tab != null && tab.getChildCount() > 1) {

            ImageView icon = (ImageView) tab.getChildAt(0);
            TextView textView = (TextView) tab.getChildAt(1);

            int gray = Color.parseColor("#757575");

            if (textView != null) textView.setTextColor(gray);
            if (icon != null) ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(gray));
        }
    }

    private void highlightTab(LinearLayout tab) {
        if (tab != null && tab.getChildCount() > 1) {

            ImageView icon = (ImageView) tab.getChildAt(0);
            TextView textView = (TextView) tab.getChildAt(1);

            int blue = Color.parseColor("#3869EA");

            if (textView != null) textView.setTextColor(blue);
            if (icon != null) ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(blue));
        }
    }

}