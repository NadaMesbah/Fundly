package com.ensias.fundlytest.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "FundlySession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "fullName";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Créer une session après login
    public void createLoginSession(String userId, String email, String fullName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.commit();
    }

    // Vérifier si l'utilisateur est connecté
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Obtenir l'ID de l'utilisateur connecté
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    // Obtenir l'email de l'utilisateur connecté
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Obtenir le nom de l'utilisateur connecté
    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    // Déconnexion
    public void logout() {
        editor.clear();
        editor.commit();
    }
}