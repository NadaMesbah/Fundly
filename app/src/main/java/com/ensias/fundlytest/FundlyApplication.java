package com.ensias.fundlytest;

import android.app.Application;
import android.util.Log;

import com.ensias.fundlytest.models.Category;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class FundlyApplication extends Application {

    private static final String TAG = "FundlyApplication";
    public static final String DEFAULT_USER_ID = "__DEFAULT__";

    private static FundlyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("fundly.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .allowWritesOnUiThread(true)
                .build();
        Realm.setDefaultConfiguration(config);

        ensureDefaultCategoriesExist();
        normalizeCategories();
    }

    public static FundlyApplication getInstance() {
        return instance;
    }

    /**
     * Seed default categories ONCE (shared for all users)
     */
    private void ensureDefaultCategoriesExist() {
        Realm realm = Realm.getDefaultInstance();

        long defaultsCount = realm.where(Category.class)
                .equalTo("userId", DEFAULT_USER_ID)
                .count();

        if (defaultsCount > 0) {
            realm.close();
            return;
        }

        realm.executeTransaction(r -> {
            int order = 0;

            // Expense defaults
            order = createDefaultCategory(r, "Food", "expense", "ic_restaurant", "#4CAF50", order);
            order = createDefaultCategory(r, "Transport", "expense", "ic_directions_car", "#2196F3", order);
            order = createDefaultCategory(r, "Shopping", "expense", "ic_shopping_cart", "#FF9800", order);
            order = createDefaultCategory(r, "Health", "expense", "ic_health", "#9C27B0", order);
            order = createDefaultCategory(r, "Entertainment", "expense", "ic_movie", "#E91E63", order);
            order = createDefaultCategory(r, "Education", "expense", "ic_school", "#00BCD4", order);
            order = createDefaultCategory(r, "Home", "expense", "ic_home", "#795548", order);
            order = createDefaultCategory(r, "Phone", "expense", "ic_phone_android", "#607D8B", order);

            // Income defaults
            order = createDefaultCategory(r, "Salary", "income", "ic_work", "#4CAF50", order);
            order = createDefaultCategory(r, "Freelance", "income", "ic_computer", "#2196F3", order);
            order = createDefaultCategory(r, "Investment", "income", "ic_trending_up", "#FF9800", order);
            createDefaultCategory(r, "Gift", "income", "ic_card_giftcard", "#9C27B0", order);
        });

        realm.close();
    }

    private int createDefaultCategory(Realm realm, String name, String type, String icon, String colorHex, int order) {
        Category category = realm.createObject(Category.class, UUID.randomUUID().toString());
        category.setUserId(DEFAULT_USER_ID);         // ✅ global default
        category.setName(name);
        category.setType(type);
        category.setIconName(icon);
        category.setColor(android.graphics.Color.parseColor(colorHex));
        category.setCustom(false);                   // ✅ not customized
        category.setOrder(order);
        return order + 1;
    }

    /**
     * Fix old data:
     * - Old defaults that had userId null -> set to DEFAULT_USER_ID and custom=false
     * - User categories that accidentally have custom=false -> set to true
     */
    private void normalizeCategories() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            RealmResults<Category> all = r.where(Category.class).findAll();

            for (Category c : all) {
                String uid = c.getUserId();

                // If userId is null/empty, it was probably an old default -> mark as default
                if (uid == null || uid.trim().isEmpty()) {
                    c.setUserId(DEFAULT_USER_ID);
                    c.setCustom(false);
                } else {
                    // Any non-default user category should be custom
                    if (!DEFAULT_USER_ID.equals(uid) && !c.isCustom()) {
                        c.setCustom(true);
                    }
                }
            }
        });
        realm.close();

        Log.d(TAG, "Categories normalized");
    }
}
