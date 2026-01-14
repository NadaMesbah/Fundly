package com.ensias.fundlytest.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.fundlytest.R;
import com.ensias.fundlytest.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivProfileImage, btnPickPhoto;
    private TextInputEditText etName, etEmail;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private androidx.appcompat.widget.AppCompatButton btnSave, btnLogout;

    private SessionManager sessionManager;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private ImageButton btnToggleCurrent, btnToggleNew, btnToggleConfirm;
    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;


    private String userId;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileImage.setImageURI(uri);
                    ivProfileImage.setPadding(0, 0, 0, 0);
                    Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_details);

        // Cacher la ActionBar si présente
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sessionManager = new SessionManager(this);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Logs de diagnostic
        android.util.Log.d("LoginDetails", "=== DIAGNOSTIC ===");
        android.util.Log.d("LoginDetails", "sessionManager.isLoggedIn(): " + sessionManager.isLoggedIn());
        android.util.Log.d("LoginDetails", "firebaseUser: " + (firebaseUser != null ? firebaseUser.getEmail() : "NULL"));

        if (!sessionManager.isLoggedIn() || firebaseUser == null) {
            android.util.Log.d("LoginDetails", "❌ REDIRECTING TO LOGIN");
            goToLoginAndFinish();
            return;
        }

        android.util.Log.d("LoginDetails", "✅ USER IS LOGGED IN - LOADING DATA");
        userId = firebaseUser.getUid();

        bindViews();
        btnToggleCurrent.setOnClickListener(v -> {
            isCurrentVisible = togglePasswordVisibility(etCurrentPassword, btnToggleCurrent, isCurrentVisible);
        });

        btnToggleNew.setOnClickListener(v -> {
            isNewVisible = togglePasswordVisibility(etNewPassword, btnToggleNew, isNewVisible);
        });

        btnToggleConfirm.setOnClickListener(v -> {
            isConfirmVisible = togglePasswordVisibility(etConfirmPassword, btnToggleConfirm, isConfirmVisible);
        });

        loadUserFromFirestore();

        btnBack.setOnClickListener(v -> finish());
        btnPickPhoto.setOnClickListener(v -> openImagePicker());
        ivProfileImage.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> onSave());
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            sessionManager.logout();
            goToLoginAndFinish();
        });
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

//    private void bindViews() {
//        btnBack = findViewById(R.id.btn_back);
//        ivProfileImage = findViewById(R.id.iv_profile_image);
//        btnPickPhoto = findViewById(R.id.btn_pick_photo);
//        etName = findViewById(R.id.et_name);
//        etEmail = findViewById(R.id.et_email);
//        etCurrentPassword = findViewById(R.id.et_current_password);
//        etNewPassword = findViewById(R.id.et_new_password);
//        etConfirmPassword = findViewById(R.id.et_confirm_password);
//        btnSave = findViewById(R.id.btn_save);
//        btnLogout = findViewById(R.id.btn_logout_details);
//    }

    private boolean togglePasswordVisibility(TextInputEditText editText, ImageButton button, boolean isVisible) {
        if (isVisible) {
            // hide password -> show eye-off icon
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            button.setImageResource(R.drawable.ic_eye_off);
        } else {
            // show password -> show eye icon
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            button.setImageResource(R.drawable.ic_eye);
        }

        if (editText.getText() != null) {
            editText.setSelection(editText.getText().length());
        }

        return !isVisible;
    }


    private void bindViews() {
        btnBack = findViewById(R.id.btn_back);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        btnPickPhoto = findViewById(R.id.btn_pick_photo);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);

        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnSave = findViewById(R.id.btn_save);
        btnLogout = findViewById(R.id.btn_logout_details);

        btnToggleCurrent = findViewById(R.id.btn_toggle_current_password);
        btnToggleNew = findViewById(R.id.btn_toggle_new_password);
        btnToggleConfirm = findViewById(R.id.btn_toggle_confirm_password);
    }


    private void loadUserFromFirestore() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");

                        if (fullName != null) etName.setText(fullName);
                        if (email != null) etEmail.setText(email);

                        android.util.Log.d("LoginDetails", "✅ User data loaded: " + fullName);
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_LONG).show();
                        android.util.Log.e("LoginDetails", "❌ User document not found in Firestore");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    android.util.Log.e("LoginDetails", "❌ Error loading user: " + e.getMessage());
                });
    }

    private void onSave() {
        String fullName = safe(etName);
        String email = safe(etEmail);
        String currentPass = safe(etCurrentPassword);
        String newPass = safe(etNewPassword);
        String confirmPass = safe(etConfirmPassword);

        if (fullName.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        boolean wantsPasswordChange = !currentPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty();

        if (wantsPasswordChange) {
            if (currentPass.isEmpty()) {
                etCurrentPassword.setError("Current password is required");
                etCurrentPassword.requestFocus();
                return;
            }
            if (newPass.isEmpty()) {
                etNewPassword.setError("New password is required");
                etNewPassword.requestFocus();
                return;
            }
            if (confirmPass.isEmpty()) {
                etConfirmPassword.setError("Confirm password is required");
                etConfirmPassword.requestFocus();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
            if (newPass.length() < 6) {
                etNewPassword.setError("Password must be at least 6 characters");
                etNewPassword.requestFocus();
                return;
            }
        }

        setUiEnabled(false);

        db.collection("users").document(userId)
                .update("fullName", fullName, "email", email)
                .addOnSuccessListener(unused -> {
                    sessionManager.createLoginSession(userId, email, fullName);

                    String currentFirebaseEmail = firebaseUser.getEmail();
                    boolean emailChanged = currentFirebaseEmail == null || !currentFirebaseEmail.equals(email);

                    if (emailChanged) {
                        firebaseUser.updateEmail(email)
                                .addOnSuccessListener(unused2 -> {
                                    if (wantsPasswordChange) {
                                        changePassword(currentPass, newPass);
                                    } else {
                                        doneSuccess();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    setUiEnabled(true);
                                    Toast.makeText(this, "Email update failed: " + msg(e), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        if (wantsPasswordChange) {
                            changePassword(currentPass, newPass);
                        } else {
                            doneSuccess();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    setUiEnabled(true);
                    Toast.makeText(this, "Failed to save: " + msg(e), Toast.LENGTH_LONG).show();
                });
    }

    private void changePassword(String currentPass, String newPass) {
        String email = firebaseUser.getEmail();
        if (email == null || email.isEmpty()) {
            setUiEnabled(true);
            Toast.makeText(this, "No email found for this account", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPass);

        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(unused ->
                        firebaseUser.updatePassword(newPass)
                                .addOnSuccessListener(unused2 -> doneSuccess())
                                .addOnFailureListener(e -> {
                                    setUiEnabled(true);
                                    Toast.makeText(this, "Password update failed: " + msg(e), Toast.LENGTH_LONG).show();
                                })
                )
                .addOnFailureListener(e -> {
                    setUiEnabled(true);
                    Toast.makeText(this, "Current password incorrect", Toast.LENGTH_LONG).show();
                });
    }

    private void doneSuccess() {
        setUiEnabled(true);
        Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setUiEnabled(boolean enabled) {
        btnSave.setEnabled(enabled);
        btnLogout.setEnabled(enabled);
        etName.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etCurrentPassword.setEnabled(enabled);
        etNewPassword.setEnabled(enabled);
        etConfirmPassword.setEnabled(enabled);
        btnPickPhoto.setEnabled(enabled);
        ivProfileImage.setEnabled(enabled);
    }

    private void goToLoginAndFinish() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String msg(Exception e) {
        if (e == null) return "Unknown error";
        return e.getMessage() == null ? "Unknown error" : e.getMessage();
    }
}