package com.ensias.fundlytest.database;

import com.ensias.fundlytest.models.Category;
import com.ensias.fundlytest.models.Transaction;
import com.ensias.fundlytest.models.User;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.*;

public class DataManager {

    private Realm realm;

    public DataManager() {
        realm = Realm.getDefaultInstance();
    }

    // Register un nouvel utilisateur
    public boolean registerUser(String id, String fullName, String email, String password) {
        // Vérifier si l'email existe déjà
        User existingUser = realm.where(User.class)
                .equalTo("email", email)
                .findFirst();

        if (existingUser != null) {
            return false; // Email déjà utilisé
        }

        // Créer le nouvel utilisateur
        realm.executeTransaction(r -> {
            User user = r.createObject(User.class, id);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(password); // En production, hasher le mot de passe !
            user.setCreatedAt(System.currentTimeMillis());
        });

        return true;
    }

    // Login utilisateur
    public User loginUser(String email, String password) {
        return realm.where(User.class)
                .equalTo("email", email)
                .equalTo("password", password)
                .findFirst();
    }

    // Get user by email
    public User getUserByEmail(String email) {
        return realm.where(User.class)
                .equalTo("email", email)
                .findFirst();
    }

    // Get user by ID
    public User getUserById(String userId) {
        return realm.where(User.class)
                .equalTo("id", userId)
                .findFirst();
    }

    // Update user
    public void updateUser(String userId, String fullName, String email) {
        realm.executeTransaction(r -> {
            User user = r.where(User.class)
                    .equalTo("id", userId)
                    .findFirst();
            if (user != null) {
                user.setFullName(fullName);
                user.setEmail(email);
            }
        });
    }

    // Update password
    public void updatePassword(String userId, String newPassword) {
        realm.executeTransaction(r -> {
            User user = r.where(User.class)
                    .equalTo("id", userId)
                    .findFirst();
            if (user != null) {
                user.setPassword(newPassword); // En production, hasher !
            }
        });
    }

    // ============ CATEGORY OPERATIONS ============

    // Add a category
    public void addCategory(String id, String name, String type, String iconName, int color, boolean isCustom) {
        realm.executeTransaction(r -> {
            Category category = r.createObject(Category.class, id);
            category.setName(name);
            category.setType(type);
            category.setIconName(iconName);
            category.setColor(color);
            category.setCustom(isCustom);
            category.setOrder(999);
        });
    }

    // Get all categories by type
    public List<Category> getCategoriesByType(String type) {
        return realm.where(Category.class)
                .equalTo("type", type)
                .sort("order", Sort.ASCENDING)
                .findAll();
    }

    // Get all categories (both types)
    public List<Category> getAllCategories() {
        RealmResults<Category> results = realm.where(Category.class).findAll();
        return realm.copyFromRealm(results);
    }

    // Get category by name
    public Category getCategoryByName(String name) {
        return realm.where(Category.class)
                .equalTo("name", name)
                .findFirst();
    }
    public RealmResults<Category> getAllCategoriesRealm() {
        return realm.where(Category.class).findAll();
    }

    // Get category by ID
    public Category getCategoryById(String categoryId) {
        return realm.where(Category.class)
                .equalTo("id", categoryId)
                .findFirst();
    }

    // Update a category
    public void updateCategory(String categoryId, String name, String iconName, int color) {
        realm.executeTransaction(r -> {
            Category category = r.where(Category.class)
                    .equalTo("id", categoryId)
                    .findFirst();
            if (category != null) {
                category.setName(name);
                category.setIconName(iconName);
                category.setColor(color);
            }
        });
    }

    // Delete a category
    public void deleteCategory(String categoryId) {
        realm.executeTransaction(r -> {
            Category category = r.where(Category.class)
                    .equalTo("id", categoryId)
                    .findFirst();
            if (category != null) {
                category.deleteFromRealm();
            }
        });
    }

    // ============ TRANSACTION OPERATIONS ============

    // Add a transaction
    public void addTransaction(String id, double amount, String categoryId, String type,
                               String note, Date date, int color, String iconName) {
        realm.executeTransaction(r -> {
            Transaction transaction = r.createObject(Transaction.class, id);
            transaction.setAmount(amount);
            transaction.setCategoryId(categoryId);
            transaction.setType(type);
            transaction.setNote(note);
            transaction.setDate(date);
            transaction.setColor(color);
            transaction.setIconName(iconName);
        });
    }

    // Get transaction by ID
    public Transaction getTransactionById(String transactionId) {
        return realm.where(Transaction.class)
                .equalTo("id", transactionId)
                .findFirst();
    }

    // Update a transaction
    public void updateTransaction(String transactionId, double amount, String categoryId, String type,
                                  String note, Date date, int color, String iconName) {
        realm.executeTransaction(r -> {
            Transaction transaction = r.where(Transaction.class)
                    .equalTo("id", transactionId)
                    .findFirst();
            if (transaction != null) {
                transaction.setAmount(amount);
                transaction.setCategoryId(categoryId);
                transaction.setType(type);
                transaction.setNote(note);
                transaction.setDate(date);
                transaction.setColor(color);
                transaction.setIconName(iconName);
            }
        });
    }

    // Get all transactions
    public List<Transaction> getAllTransactions() {
        return realm.where(Transaction.class)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    // Get transactions by type
    public List<Transaction> getTransactionsByType(String type) {
        return realm.where(Transaction.class)
                .equalTo("type", type)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    // Get transactions by date range
    public List<Transaction> getTransactionsByDateRange(Date startDate, Date endDate) {
        return realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    // Delete a transaction
    public void deleteTransaction(String transactionId) {
        realm.executeTransaction(r -> {
            Transaction transaction = r.where(Transaction.class)
                    .equalTo("id", transactionId)
                    .findFirst();
            if (transaction != null) {
                transaction.deleteFromRealm();
            }
        });
    }

    // ============ REPORTS & STATISTICS ============

    // Get total expenses in date range
    public double getTotalExpenses(Date startDate, Date endDate) {
        RealmResults<Transaction> results = realm.where(Transaction.class)
                .equalTo("type", "expense")
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .findAll();

        double total = 0;
        for (Transaction t : results) {
            total += t.getAmount();
        }
        return total;
    }

    // Get total income in date range
    public double getTotalIncome(Date startDate, Date endDate) {
        RealmResults<Transaction> results = realm.where(Transaction.class)
                .equalTo("type", "income")
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .findAll();

        double total = 0;
        for (Transaction t : results) {
            total += t.getAmount();
        }
        return total;
    }

    // Get category breakdown
    public Map<String, Double> getCategoryBreakdown(Date startDate, Date endDate, String type) {
        RealmResults<Transaction> transactions = realm.where(Transaction.class)
                .equalTo("type", type)
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .findAll();

        Map<String, Double> breakdown = new HashMap<>();
        for (Transaction t : transactions) {
            Category category = getCategoryById(t.getCategoryId());
            String categoryName = category != null ? category.getName() : "Other";

            breakdown.put(categoryName, breakdown.getOrDefault(categoryName, 0.0) + t.getAmount());
        }

        return breakdown;
    }

    public void close() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}