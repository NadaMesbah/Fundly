package com.ensias.fundlytest.database;

import com.ensias.fundlytest.models.Category;
import com.ensias.fundlytest.models.Transaction;
import com.ensias.fundlytest.models.User;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private static final String DEFAULT_USER_ID = "__DEFAULT__"; // optional, supports future default seeding
    private Realm realm;

    public DataManager() {
        realm = Realm.getDefaultInstance();
    }

    // ============ USER OPERATIONS ============

    public boolean registerUser(String id, String fullName, String email, String password) {
        User existingUser = realm.where(User.class)
                .equalTo("email", email)
                .findFirst();

        if (existingUser != null) return false;

        realm.executeTransaction(r -> {
            User user = r.createObject(User.class, id);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(password);
            user.setCreatedAt(System.currentTimeMillis());
        });

        return true;
    }

    public User loginUser(String email, String password) {
        return realm.where(User.class)
                .equalTo("email", email)
                .equalTo("password", password)
                .findFirst();
    }

    public User getUserByEmail(String email) {
        return realm.where(User.class)
                .equalTo("email", email)
                .findFirst();
    }

    public User getUserById(String userId) {
        return realm.where(User.class)
                .equalTo("id", userId)
                .findFirst();
    }

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

    // ============ CATEGORY OPERATIONS ============

    public void addCategory(String id, String userId, String name, String type,
                            String iconName, int color, boolean isCustom) {
        realm.executeTransaction(r -> {
            Category category = r.createObject(Category.class, id);
            category.setUserId(userId);
            category.setName(name);
            category.setType(type);
            category.setIconName(iconName);
            category.setColor(color);
            category.setCustom(isCustom);
            category.setOrder(999);
        });
    }

    /**
     * ✅ Returns:
     * - Defaults: userId == null OR userId == "__DEFAULT__"
     * - Plus user categories: userId == current userId
     */
    public List<Category> getCategoriesByType(String userId, String type) {
        RealmResults<Category> results = realm.where(Category.class)
                .equalTo("type", type)
                .beginGroup()
                .isNull("userId")                   // defaults
                .or()
                .equalTo("userId", DEFAULT_USER_ID)  // optional defaults
                .or()
                .equalTo("userId", userId)           // user categories
                .endGroup()
                .sort("order", Sort.ASCENDING)
                .findAll();

        // Dedup by (type + name), user version overrides default
        Map<String, Category> unique = new LinkedHashMap<>();

        for (Category c : results) {
            String key = (c.getType() + "||" + c.getName()).toLowerCase();

            boolean isUserCat = userId != null && userId.equals(c.getUserId());
            boolean isDefaultCat = (c.getUserId() == null) || DEFAULT_USER_ID.equals(c.getUserId());

            if (!unique.containsKey(key)) {
                unique.put(key, c);
            } else {
                Category existing = unique.get(key);
                boolean existingIsDefault =
                        (existing.getUserId() == null) || DEFAULT_USER_ID.equals(existing.getUserId());

                // If current is user and existing is default => replace
                if (isUserCat && existingIsDefault) {
                    unique.put(key, c);
                }

                // Otherwise keep existing (user already there, or both defaults)
            }
        }

        return new ArrayList<>(unique.values());
    }


    public List<Category> getAllCategories(String userId) {
        RealmResults<Category> results = realm.where(Category.class)
                .beginGroup()
                .isNull("userId")
                .or()
                .equalTo("userId", DEFAULT_USER_ID)
                .or()
                .equalTo("userId", userId)
                .endGroup()
                .sort("order", Sort.ASCENDING)
                .findAll();

        return realm.copyFromRealm(results);
    }

    public Category getCategoryByName(String userId, String name) {
        Category userCat = realm.where(Category.class)
                .equalTo("userId", userId)
                .equalTo("name", name)
                .findFirst();

        if (userCat != null) return userCat;

        // fallback to default
        return realm.where(Category.class)
                .equalTo("name", name)
                .beginGroup()
                .isNull("userId")
                .or()
                .equalTo("userId", DEFAULT_USER_ID)
                .endGroup()
                .findFirst();
    }

    public RealmResults<Category> getAllCategoriesRealm(String userId) {
        return realm.where(Category.class)
                .beginGroup()
                .isNull("userId")
                .or()
                .equalTo("userId", DEFAULT_USER_ID)
                .or()
                .equalTo("userId", userId)
                .endGroup()
                .sort("order", Sort.ASCENDING)
                .findAll();
    }

    public Category getCategoryById(String categoryId) {
        return realm.where(Category.class)
                .equalTo("id", categoryId)
                .findFirst();
    }

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

    public void deleteCategory(String categoryId) {
        realm.executeTransaction(r -> {
            Category category = r.where(Category.class)
                    .equalTo("id", categoryId)
                    .findFirst();
            if (category != null) category.deleteFromRealm();
        });
    }

    // ============ TRANSACTION OPERATIONS ============

    public void addTransaction(String id, String userId, double amount, String categoryId,
                               String type, String note, Date date, int color, String iconName) {
        realm.executeTransaction(r -> {
            Transaction transaction = r.createObject(Transaction.class, id);
            transaction.setUserId(userId);
            transaction.setAmount(amount);
            transaction.setCategoryId(categoryId);
            transaction.setType(type);
            transaction.setNote(note);
            transaction.setDate(date);
            transaction.setColor(color);
            transaction.setIconName(iconName);
        });
    }

    public Transaction getTransactionById(String transactionId) {
        return realm.where(Transaction.class)
                .equalTo("id", transactionId)
                .findFirst();
    }

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

    public List<Transaction> getAllTransactions(String userId) {
        return realm.where(Transaction.class)
                .equalTo("userId", userId)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    public List<Transaction> getTransactionsByType(String userId, String type) {
        return realm.where(Transaction.class)
                .equalTo("userId", userId)
                .equalTo("type", type)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    public List<Transaction> getTransactionsByDateRange(String userId, Date startDate, Date endDate) {
        return realm.where(Transaction.class)
                .equalTo("userId", userId)
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .sort("date", Sort.DESCENDING)
                .findAll();
    }

    public void deleteTransaction(String transactionId) {
        realm.executeTransaction(r -> {
            Transaction transaction = r.where(Transaction.class)
                    .equalTo("id", transactionId)
                    .findFirst();
            if (transaction != null) transaction.deleteFromRealm();
        });
    }

    // ============ REPORTS & STATISTICS ============

    public double getTotalExpenses(String userId, Date startDate, Date endDate) {
        RealmResults<Transaction> results = realm.where(Transaction.class)
                .equalTo("userId", userId)
                .equalTo("type", "expense")
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .findAll();

        double total = 0;
        for (Transaction t : results) total += t.getAmount();
        return total;
    }

    public double getTotalIncome(String userId, Date startDate, Date endDate) {
        RealmResults<Transaction> results = realm.where(Transaction.class)
                .equalTo("userId", userId)
                .equalTo("type", "income")
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .findAll();

        double total = 0;
        for (Transaction t : results) total += t.getAmount();
        return total;
    }

    public Map<String, Double> getCategoryBreakdown(String userId, Date startDate, Date endDate, String type) {
        RealmResults<Transaction> transactions = realm.where(Transaction.class)
                .equalTo("userId", userId)
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
        if (realm != null && !realm.isClosed()) realm.close();
    }
}
