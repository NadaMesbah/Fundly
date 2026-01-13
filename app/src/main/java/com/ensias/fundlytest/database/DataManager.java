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

    // ============ USER OPERATIONS ============

    // Register a new user
    public boolean registerUser(String id, String fullName, String email, String password) {
        User existingUser = realm.where(User.class)
                .equalTo("email", email)
                .findFirst();

        if (existingUser != null) {
            return false;
        }

        realm.executeTransaction(r -> {
            User user = r.createObject(User.class, id);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(password);
            user.setCreatedAt(System.currentTimeMillis());
        });

        return true;
    }

    // Login user
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
                user.setPassword(newPassword);
            }
        });
    }

    // ============ CATEGORY OPERATIONS (USER-SPECIFIC) ============

    /**
     * Add a category for a specific user
     * IMPORTANT: Always pass userId
     */
    public void addCategory(String id, String userId, String name, String type,
                            String iconName, int color, boolean isCustom) {
        realm.executeTransaction(r -> {
            Category category = r.createObject(Category.class, id);
            category.setUserId(userId);  // CRITICAL: Set user ID
            category.setName(name);
            category.setType(type);
            category.setIconName(iconName);
            category.setColor(color);
            category.setCustom(isCustom);
            category.setOrder(999);
        });
    }

    /**
     * Get categories by type for a specific user
     * CRITICAL: Only returns categories belonging to this user
     */
    public List<Category> getCategoriesByType(String userId, String type) {
        return realm.where(Category.class)
                .equalTo("userId", userId)  // FILTER BY USER
                .equalTo("type", type)
                .sort("order", Sort.ASCENDING)
                .findAll();
    }

    /**
     * Get all categories for a specific user
     */
    public List<Category> getAllCategories(String userId) {
        RealmResults<Category> results = realm.where(Category.class)
                .equalTo("userId", userId)  // FILTER BY USER
                .findAll();
        return realm.copyFromRealm(results);
    }

    /**
     * Get category by name for a specific user
     */
    public Category getCategoryByName(String userId, String name) {
        return realm.where(Category.class)
                .equalTo("userId", userId)  // FILTER BY USER
                .equalTo("name", name)
                .findFirst();
    }

    /**
     * Get all categories for a specific user (RealmResults)
     */
    public RealmResults<Category> getAllCategoriesRealm(String userId) {
        return realm.where(Category.class)
                .equalTo("userId", userId)  // FILTER BY USER
                .findAll();
    }

    /**
     * Get category by ID (no user filter needed since ID is unique)
     */
    public Category getCategoryById(String categoryId) {
        return realm.where(Category.class)
                .equalTo("id", categoryId)
                .findFirst();
    }

    /**
     * Update a category
     */
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

    /**
     * Delete a category
     */
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

    // ============ TRANSACTION OPERATIONS (USER-SPECIFIC) ============

    /**
     * Add a transaction for a specific user
     * IMPORTANT: Always pass userId
     */
    public void addTransaction(String id, String userId, double amount, String categoryId,
                               String type, String note, Date date, int color, String iconName) {
        realm.executeTransaction(r -> {
            Transaction transaction = r.createObject(Transaction.class, id);
            transaction.setUserId(userId);  // CRITICAL: Set user ID
            transaction.setAmount(amount);
            transaction.setCategoryId(categoryId);
            transaction.setType(type);
            transaction.setNote(note);
            transaction.setDate(date);
            transaction.setColor(color);
            transaction.setIconName(iconName);
        });
    }

    /**
     * Get transaction by ID (no user filter needed since ID is unique)
     */
    public Transaction getTransactionById(String transactionId) {
        return realm.where(Transaction.class)
                .equalTo("id", transactionId)
                .findFirst();
    }

    /**
     * Update a transaction
     */
    public void updateTransaction(String transactionId, double amount, String categoryId,
                                  String type, String note, Date date, int color, String iconName) {
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

    /**
     * Get all transactions for a specific user
     * CRITICAL: Only returns transactions belonging to this user
     */
    public List<Transaction> getAllTransactions(String userId) {
        return realm.where(Transaction.class)
                .equalTo("userId", userId)  // FILTER BY USER
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    /**
     * Get transactions by type for a specific user
     */
    public List<Transaction> getTransactionsByType(String userId, String type) {
        return realm.where(Transaction.class)
                .equalTo("userId", userId)  // FILTER BY USER
                .equalTo("type", type)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    /**
     * Get transactions by date range for a specific user
     */
    public List<Transaction> getTransactionsByDateRange(String userId, Date startDate, Date endDate) {
        return realm.where(Transaction.class)
                .equalTo("userId", userId)  // FILTER BY USER
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    /**
     * Delete a transaction
     */
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

    // ============ REPORTS & STATISTICS (USER-SPECIFIC) ============

    /**
     * Get total expenses in date range for a specific user
     */
    public double getTotalExpenses(String userId, Date startDate, Date endDate) {
        RealmResults<Transaction> results = realm.where(Transaction.class)
                .equalTo("userId", userId)  // FILTER BY USER
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

    /**
     * Get total income in date range for a specific user
     */
    public double getTotalIncome(String userId, Date startDate, Date endDate) {
        RealmResults<Transaction> results = realm.where(Transaction.class)
                .equalTo("userId", userId)  // FILTER BY USER
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

    /**
     * Get category breakdown for a specific user
     */
    public Map<String, Double> getCategoryBreakdown(String userId, Date startDate, Date endDate, String type) {
        RealmResults<Transaction> transactions = realm.where(Transaction.class)
                .equalTo("userId", userId)  // FILTER BY USER
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