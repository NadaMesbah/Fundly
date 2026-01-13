package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.utils.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends BaseActivity {

    private ImageButton btnBack;
    private ImageButton btnLogout;
    private ShapeableImageView ivProfileImage;
    private TextView tvUserName;
    private TextView tvUserRole;
    private TextView tvBudgetName;
    private TextView tvProfileIncome;
    private TextView tvProfileExpenses;
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
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = findViewById(R.id.fragment_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.activity_profile, container, true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        if (currentUserId == null && currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        dataManager = new DataManager();

        initializeImagePicker();
        initViews();
        loadUserData(); // Charge depuis Firestore
        loadBudgetData();
        setupListeners();

        highlightCurrentTab();
    }

    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), selectedImageUri);
                            ivProfileImage.setImageBitmap(bitmap);
                            ivProfileImage.setPadding(0, 0, 0, 0);
                            Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
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
            return;
        }

        String userId = currentUser.getUid();


        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");

                        // Afficher le nom complet
                        if (fullName != null && !fullName.isEmpty()) {
                            tvUserName.setText(fullName);
                            tvBudgetName.setText(fullName);
                        } else if (email != null) {
                            tvUserName.setText(email.split("@")[0]);
                            tvBudgetName.setText(email.split("@")[0]);
                        }

                        // Afficher l'email
                        if (email != null) {
                            tvUserRole.setText(email);
                        }
                    } else {
                        // Fallback si pas de données Firestore
                        loadUserDataFromSession();
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback en cas d'erreur
                    loadUserDataFromSession();
                });
    }


    private void loadUserDataFromSession() {
        String fullName = sessionManager.getFullName();
        String email = sessionManager.getEmail();

        if (fullName != null && !fullName.isEmpty()) {
            tvUserName.setText(fullName);
            tvBudgetName.setText(fullName);
        } else if (email != null) {
            tvUserName.setText(email.split("@")[0]);
            tvBudgetName.setText(email.split("@")[0]);
        }

        if (email != null) {
            tvUserRole.setText(email);
        }
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
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("%.1fk", amount / 1_000);
        } else {
            return decimalFormat.format(amount);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        ivProfileImage.setOnClickListener(v -> openImagePicker());

        btnEditBudget.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTransactionActivity.class));
        });

        cardBudget.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTransactionActivity.class));
        });

        menuLoginDetails.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginDetailsActivity.class);
            startActivity(intent);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
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
        if (dataManager != null) {
            dataManager.close();
        }
    }
}