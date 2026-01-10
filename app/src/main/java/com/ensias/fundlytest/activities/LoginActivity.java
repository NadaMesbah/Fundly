package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEmail;
    private EditText etPassword;
    private ImageButton btnTogglePassword;
    private TextView tvForgotPassword;
    private AppCompatButton btnLogin;
    private TextView tvSignUp;

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vérifier si l'utilisateur est déjà connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Utilisateur déjà connecté → aller à HomeActivity
            goToHome();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignUp = findViewById(R.id.tv_sign_up);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
            isPasswordVisible = false;
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        etEmail.setError(null);
        etPassword.setError(null);

        boolean isValid = true;

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            isValid = false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            if (isValid) {
                etPassword.requestFocus();
            }
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            if (isValid) {
                etPassword.requestFocus();
            }
            isValid = false;
        }

        if (isValid) {
            performLogin(email, password);
        }
    }

    private void performLogin(String email, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // ✅ Récupérer le nom depuis Firestore
                        db.collection("users").document(user.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String fullName = "User";
                                    if (documentSnapshot.exists()) {
                                        String name = documentSnapshot.getString("fullName");
                                        if (name != null && !name.isEmpty()) {
                                            fullName = name;
                                        }
                                    }

                                    // Sauvegarder la session avec le vrai nom
                                    SessionManager sessionManager = new SessionManager(LoginActivity.this);
                                    sessionManager.createLoginSession(user.getUid(), user.getEmail(), fullName);

                                    Toast.makeText(LoginActivity.this,
                                            "Welcome " + fullName,
                                            Toast.LENGTH_SHORT).show();
                                    goToHome();
                                })
                                .addOnFailureListener(e -> {
                                    // Si échec de chargement Firestore, utiliser "User" par défaut
                                    SessionManager sessionManager = new SessionManager(LoginActivity.this);
                                    sessionManager.createLoginSession(user.getUid(), user.getEmail(), "User");

                                    Toast.makeText(LoginActivity.this,
                                            "Welcome!",
                                            Toast.LENGTH_SHORT).show();
                                    goToHome();
                                });

                    } else {
                        String errorMessage = "Authentication failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}