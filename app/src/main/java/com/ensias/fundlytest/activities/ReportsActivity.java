package com.ensias.fundlytest.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.models.Category;
import com.ensias.fundlytest.utils.SessionManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.components.Legend;
import com.google.android.material.tabs.TabLayout;
import java.text.*;
import java.util.*;

public class ReportsActivity extends BaseActivity {

    private static final String TAG = "ReportsActivity";

    private DataManager dataManager;
    private SessionManager sessionManager;
    private String currentUserId;

    private PieChart pieChart;
    private TextView totalAmountView;
    private TextView periodTextView;
    private TextView reportTypeTextView;
    private TextView expensesTextView;
    private TextView incomeTextView;
    private TextView balanceTextView;
    private LinearLayout categoryBreakdownLayout;
    private TabLayout periodTabLayout;
    private TabLayout dateTabLayout;

    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat weekFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");

    private Date startDate;
    private Date endDate;
    private String periodType = "month";
    private String reportType = "expense";

    private TabLayout.OnTabSelectedListener dateTabListener;
    private boolean suppressDateTabCallback = false;

    private Map<String, Category> categoryCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.fragment_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.activity_reports, container, true);

        // GET CURRENT USER
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "ReportsActivity started for user: " + currentUserId);

        dataManager = new DataManager();

        loadCategoryCache();
        setupViews();
        setupPieChart();
        setupPeriodTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategoryCache();
        if (startDate != null && endDate != null) {
            loadChartData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pieChart != null) pieChart.clear();
        if (dataManager != null) dataManager.close();
    }

    private void loadCategoryCache() {
        categoryCache.clear();

        try {
            // FILTER BY USER ID
            List<Category> expenseCategories = dataManager.getCategoriesByType(currentUserId, "expense");
            List<Category> incomeCategories = dataManager.getCategoriesByType(currentUserId, "income");

            for (Category cat : expenseCategories) {
                categoryCache.put(cat.getName(), cat);
            }
            for (Category cat : incomeCategories) {
                categoryCache.put(cat.getName(), cat);
            }

            Log.d(TAG, "Category cache loaded: " + categoryCache.size() + " categories");
        } catch (Exception e) {
            Log.e(TAG, "Error loading category cache", e);
        }
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

//        ImageButton btnCalendar = findViewById(R.id.btnCalendar);
//        if (btnCalendar != null) btnCalendar.setOnClickListener(v -> showDatePicker());

        reportTypeTextView = findViewById(R.id.reportTypeTextView);
        if (reportTypeTextView != null) {
            updateReportTypeText();
            reportTypeTextView.setOnClickListener(v -> toggleReportType());
        }

        pieChart = findViewById(R.id.pieChart);
        totalAmountView = findViewById(R.id.totalAmountView);
        periodTextView = findViewById(R.id.periodTextView);
        expensesTextView = findViewById(R.id.expensesTextView);
        incomeTextView = findViewById(R.id.incomeTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        categoryBreakdownLayout = findViewById(R.id.categoryBreakdownLayout);
        periodTabLayout = findViewById(R.id.periodTabLayout);
        dateTabLayout = findViewById(R.id.dateTabLayout);
    }

    private void toggleReportType() {
        reportType = reportType.equals("expense") ? "income" : "expense";
        updateReportTypeText();
        loadChartData();
    }

    private void updateReportTypeText() {
        if (reportTypeTextView != null) {
            String text = reportType.equals("expense") ? "Expenses ▼" : "Income ▼";
            reportTypeTextView.setText(text);

            int color = reportType.equals("expense")
                    ? Color.parseColor("#F44336")
                    : Color.parseColor("#4CAF50");
            reportTypeTextView.setTextColor(color);
        }
    }

    private void setupPeriodTabs() {
        periodTabLayout.removeAllTabs();
        periodTabLayout.addTab(periodTabLayout.newTab().setText("Week"));
        periodTabLayout.addTab(periodTabLayout.newTab().setText("Month"));
        periodTabLayout.addTab(periodTabLayout.newTab().setText("Year"));

        periodTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) periodType = "week";
                else if (tab.getPosition() == 1) periodType = "month";
                else periodType = "year";
                rebuildDateTabsAndLoad();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        TabLayout.Tab monthTab = periodTabLayout.getTabAt(1);
        if (monthTab != null) periodTabLayout.selectTab(monthTab);
    }

    private void rebuildDateTabsAndLoad() {
        setupDateTabs();

        if (dateTabLayout.getTabCount() > 0) {
            TabLayout.Tab first = dateTabLayout.getTabAt(0);
            if (first != null) {
                suppressDateTabCallback = true;
                dateTabLayout.selectTab(first);
                suppressDateTabCallback = false;

                updateDatesFromTab(first);
                loadChartData();
            }
        }
    }

    private void setupDateTabs() {
        dateTabLayout.removeAllTabs();

        if (dateTabListener != null) {
            dateTabLayout.removeOnTabSelectedListener(dateTabListener);
        }

        Date now = new Date();

        if ("week".equals(periodType)) {
            Date currentWeekStart = startOfWeek(now);
            for (int i = 0; i < 6; i++) {
                Date base = addToDate(currentWeekStart, Calendar.WEEK_OF_YEAR, -i);
                TabLayout.Tab tab = dateTabLayout.newTab();
                tab.setTag(base);
                tab.setText(getWeekRangeLabel(base));
                dateTabLayout.addTab(tab);
            }
        } else if ("month".equals(periodType)) {
            Date currentMonthStart = startOfMonth(now);
            for (int i = 0; i < 6; i++) {
                Date base = addToDate(currentMonthStart, Calendar.MONTH, -i);
                TabLayout.Tab tab = dateTabLayout.newTab();
                tab.setTag(base);
                tab.setText(monthFormat.format(base));
                dateTabLayout.addTab(tab);
            }
        } else {
            Date currentYearStart = startOfYear(now);
            for (int i = 0; i < 6; i++) {
                Date base = addToDate(currentYearStart, Calendar.YEAR, -i);
                TabLayout.Tab tab = dateTabLayout.newTab();
                tab.setTag(base);
                tab.setText(yearFormat.format(base));
                dateTabLayout.addTab(tab);
            }
        }

        dateTabListener = new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (suppressDateTabCallback) return;
                updateDatesFromTab(tab);
                loadChartData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        };

        dateTabLayout.addOnTabSelectedListener(dateTabListener);
    }

    private void updateDatesFromTab(TabLayout.Tab tab) {
        Object tag = tab.getTag();
        if (!(tag instanceof Date)) return;

        startDate = (Date) tag;

        if ("week".equals(periodType)) {
            endDate = addToDate(startDate, Calendar.DAY_OF_YEAR, 7);
        } else if ("month".equals(periodType)) {
            endDate = addToDate(startDate, Calendar.MONTH, 1);
        } else {
            endDate = addToDate(startDate, Calendar.YEAR, 1);
        }

        updatePeriodText();
    }

    private void updatePeriodText() {
        if (periodTextView == null || startDate == null || endDate == null) return;

        if ("week".equals(periodType)) {
            Date inclusiveEnd = addToDate(endDate, Calendar.DAY_OF_YEAR, -1);
            periodTextView.setText(weekFormat.format(startDate) + " - " + weekFormat.format(inclusiveEnd));
        } else if ("month".equals(periodType)) {
            periodTextView.setText(monthFormat.format(startDate));
        } else {
            periodTextView.setText(yearFormat.format(startDate));
        }
    }

    private String getWeekRangeLabel(Date weekStart) {
        Date inclusiveEnd = addToDate(weekStart, Calendar.DAY_OF_YEAR, 6);
        return weekFormat.format(weekStart) + " - " + weekFormat.format(inclusiveEnd);
    }

    private Date startOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }

    private Date startOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date startOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date addToDate(Date date, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        return cal.getTime();
    }

//    private void showDatePicker() {
//        Toast.makeText(this, "Date picker coming soon", Toast.LENGTH_SHORT).show();
//    }

    private void setupPieChart() {
        if (pieChart == null) return;

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(68f);
        pieChart.setTransparentCircleRadius(72f);
        pieChart.setNoDataText("");
        pieChart.setDrawCenterText(false);
        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
    }

    private void loadChartData() {
        if (startDate == null || endDate == null) return;

        showLoadingState();

        // FILTER BY USER ID
        double totalExpenses = dataManager.getTotalExpenses(currentUserId, startDate, endDate);
        double totalIncome = dataManager.getTotalIncome(currentUserId, startDate, endDate);
        Map<String, Double> breakdown = dataManager.getCategoryBreakdown(currentUserId, startDate, endDate, reportType);

        Log.d(TAG, "Loaded data for user " + currentUserId + " - Expenses: " + totalExpenses +
                ", Income: " + totalIncome + ", Breakdown items: " + (breakdown != null ? breakdown.size() : 0));

        updateSummary(totalExpenses, totalIncome);
        updatePieChart(breakdown);
        updateCategoryBreakdown(breakdown, reportType.equals("expense") ? totalExpenses : totalIncome);
    }

    private void updateSummary(double totalExpenses, double totalIncome) {
        double balance = totalIncome - totalExpenses;

        if (totalAmountView != null) {
            double displayTotal = reportType.equals("expense") ? totalExpenses : totalIncome;
            totalAmountView.setText(decimalFormat.format(displayTotal));
        }

        if (expensesTextView != null) expensesTextView.setText("-" + decimalFormat.format(totalExpenses));
        if (incomeTextView != null) incomeTextView.setText("+" + decimalFormat.format(totalIncome));

        if (balanceTextView != null) {
            balanceTextView.setText(decimalFormat.format(balance));
            balanceTextView.setTextColor(balance >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
        }
    }

    private void updatePieChart(Map<String, Double> breakdown) {
        if (pieChart == null) return;

        if (breakdown == null || breakdown.isEmpty()) {
            pieChart.setData(null);
            pieChart.invalidate();
            Log.d(TAG, "No breakdown data for pie chart");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Double> e : breakdown.entrySet()) {
            double amount = (e.getValue() == null) ? 0.0 : e.getValue();
            if (amount <= 0) continue;

            String categoryName = e.getKey();
            entries.add(new PieEntry((float) amount, categoryName));

            Category category = categoryCache.get(categoryName);
            int color = category != null ? category.getColor() : Color.parseColor("#607D8B");

            if (category != null) {
                Log.d(TAG, "Category: " + categoryName + " -> Color: #" + Integer.toHexString(color));
            } else {
                Log.w(TAG, "Category not found in cache: " + categoryName);
            }

            colors.add(color);
        }

        if (entries.isEmpty()) {
            pieChart.setData(null);
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2.5f);
        dataSet.setSelectionShift(6f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.invalidate();

        Log.d(TAG, "Pie chart updated with " + entries.size() + " slices");
    }

    private void updateCategoryBreakdown(Map<String, Double> breakdown, double totalForType) {
        if (categoryBreakdownLayout == null) return;
        categoryBreakdownLayout.removeAllViews();

        if (breakdown == null || breakdown.isEmpty() || totalForType <= 0) {
            TextView emptyText = new TextView(this);
            String emptyMessage = reportType.equals("expense")
                    ? "No expenses in this period"
                    : "No income in this period";
            emptyText.setText(emptyMessage);
            emptyText.setTextColor(Color.parseColor("#757575"));
            emptyText.setTextSize(14f);
            emptyText.setPadding(16, 24, 16, 24);
            categoryBreakdownLayout.addView(emptyText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(breakdown.entrySet());
        sorted.sort((a, b) -> Double.compare(
                b.getValue() == null ? 0 : b.getValue(),
                a.getValue() == null ? 0 : a.getValue()
        ));

        for (Map.Entry<String, Double> entry : sorted) {
            String categoryName = entry.getKey();
            double amount = entry.getValue() == null ? 0.0 : entry.getValue();
            float percentage = (float) (amount / totalForType * 100f);

            View row = inflater.inflate(R.layout.item_category_expense, categoryBreakdownLayout, false);

            TextView tvName = row.findViewById(R.id.categoryName);
            TextView tvPercent = row.findViewById(R.id.categoryPercent);
            TextView tvAmount = row.findViewById(R.id.categoryAmount);
            ImageView ivIcon = row.findViewById(R.id.categoryIcon);
            View progress = row.findViewById(R.id.progressBar);

            tvName.setText(categoryName);
            tvPercent.setText(String.format(Locale.getDefault(), "%.1f%%", percentage));
            tvAmount.setText(decimalFormat.format(amount) + " DH");

            Category category = categoryCache.get(categoryName);

            int color = Color.parseColor("#607D8B");
            int iconRes = R.drawable.ic_attach_money;

            if (category != null) {
                color = category.getColor();
                iconRes = getCategoryIconRes(category.getIconName());
                Log.d(TAG, "Breakdown row: " + categoryName + " -> Icon: " + category.getIconName() +
                        ", Color: #" + Integer.toHexString(color));
            } else {
                Log.w(TAG, "Category not found for breakdown: " + categoryName);
            }

            ivIcon.setImageResource(iconRes);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivIcon.setImageTintList(ColorStateList.valueOf(color));
            } else {
                ivIcon.setColorFilter(color);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                progress.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            progress.setPivotX(0f);
            progress.setScaleX(Math.max(0.02f, Math.min(1f, percentage / 100f)));

            categoryBreakdownLayout.addView(row);
        }
    }

    private int getCategoryIconRes(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_attach_money;
        }

        try {
            int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
            return (resId != 0) ? resId : R.drawable.ic_attach_money;
        } catch (Exception e) {
            Log.e(TAG, "Error getting icon resource for: " + iconName, e);
            return R.drawable.ic_attach_money;
        }
    }

    private void showLoadingState() {
        if (totalAmountView != null) totalAmountView.setText("--");

        if (pieChart != null) {
            pieChart.setData(null);
            pieChart.invalidate();
        }

        if (categoryBreakdownLayout != null) {
            categoryBreakdownLayout.removeAllViews();
        }
    }
}