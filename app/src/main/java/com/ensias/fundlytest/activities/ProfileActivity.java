package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.ensias.fundlytest.R;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.utils.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends BaseActivity {

    private ImageButton btnBack, btnLogout;
    private ShapeableImageView ivProfileImage;
    private TextView tvUserName, tvUserRole, tvBudgetName;
    private TextView tvProfileIncome, tvProfileExpenses;
    private ImageButton btnEditBudget;
    private CardView cardBudget;
    private LinearLayout menuLoginDetails;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String currentUserId;
    private DataManager dataManager;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.fragment_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.activity_profile, container, true);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        currentUserId = sessionManager.getUserId();
        if (currentUserId == null && currentUser != null) currentUserId = currentUser.getUid();

        dataManager = new DataManager();

        initViews();

        // ✅ default avatar (no picker)
        ivProfileImage.setImageResource(R.drawable.ic_default_user); // change if needed
        ivProfileImage.setPadding(20, 20, 20, 20);

        loadUserData();
        loadBudgetData();
        setupListeners();

        highlightCurrentTab();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnLogout = findViewById(R.id.btn_logout);

        ivProfileImage = findViewById(R.id.iv_profile_image);

        tvUserName = findViewById(R.id.tv_user_name);
        tvUserRole = findViewById(R.id.tv_user_role);

        tvBudgetName = findViewById(R.id.tv_budget_name);
        tvProfileIncome = findViewById(R.id.tv_profile_income);
        tvProfileExpenses = findViewById(R.id.tv_profile_expenses);

        btnEditBudget = findViewById(R.id.btn_edit_budget);
        cardBudget = findViewById(R.id.card_budget);
        menuLoginDetails = findViewById(R.id.menu_login_details);
    }

    private void loadUserData() {
        if (currentUser == null) {
            loadUserDataFromSession();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("fullName");
                        String email = doc.getString("email");

                        //String firstName = getFirstName(fullName, email);

                        if (fullName != null) {
                            tvUserName.setText(fullName);
                            tvBudgetName.setText(fullName);
                        }

                        if (email != null) tvUserRole.setText(email);
                    } else {
                        loadUserDataFromSession();
                    }
                })
                .addOnFailureListener(e -> loadUserDataFromSession());
    }

    private void loadUserDataFromSession() {
        String fullName = sessionManager.getFullName();
        String email = sessionManager.getEmail();

        String firstName = getFirstName(fullName, email);

        if (firstName != null) {
            tvUserName.setText(firstName);
            tvBudgetName.setText(firstName);
        }

        if (email != null) tvUserRole.setText(email);
    }

    private String getFirstName(String fullName, String email) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String trimmed = fullName.trim();
            String[] parts = trimmed.split("\\s+");
            return parts.length > 0 ? parts[0] : trimmed;
        }

        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }

        return "User";
    }

    private void loadBudgetData() {
        if (currentUserId == null) return;

        Date[] dates = getCurrentMonthDates();
        double totalIncome = dataManager.getTotalIncome(currentUserId, dates[0], dates[1]);
        double totalExpenses = dataManager.getTotalExpenses(currentUserId, dates[0], dates[1]);

        tvProfileIncome.setText(formatAmount(totalIncome) + " DH");
        tvProfileExpenses.setText(formatAmount(totalExpenses) + " DH");
    }

    private Date[] getCurrentMonthDates() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        return new Date[]{startDate, calendar.getTime()};
    }

    private String formatAmount(double amount) {
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000);
        if (amount >= 1_000) return String.format("%.1fk", amount / 1_000);
        return decimalFormat.format(amount);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // ✅ no image picking
        ivProfileImage.setOnClickListener(null);

        btnEditBudget.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class))
        );

        cardBudget.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class))
        );

        menuLoginDetails.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, LoginDetailsActivity.class))
        );
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        sessionManager.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadBudgetData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) dataManager.close();
    }
}
