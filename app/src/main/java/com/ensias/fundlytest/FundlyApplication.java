package com.ensias.fundlytest;

import android.app.Application;

import com.ensias.fundlytest.models.Category;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class FundlyApplication extends Application {

    private static FundlyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("fundly.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .allowWritesOnUiThread(true)
                .build();
        Realm.setDefaultConfiguration(config);

        addDefaultCategoriesIfNeeded();
    }

    public static FundlyApplication getInstance() {
        return instance;
    }

    private void addDefaultCategoriesIfNeeded() {
        Realm realm = Realm.getDefaultInstance();

        long count = realm.where(Category.class).count();
        if (count > 0) {
            realm.close();
            return;
        }

        realm.executeTransaction(r -> {
            // Expense categories
            createCategory(r, "Food", "expense", "ic_restaurant", "#4CAF50");
            createCategory(r, "Transport", "expense", "ic_directions_car", "#2196F3");
            createCategory(r, "Shopping", "expense", "ic_shopping_cart", "#FF9800");
            createCategory(r, "Health", "expense", "ic_health", "#9C27B0");
            createCategory(r, "Entertainment", "expense", "ic_movie", "#E91E63");
            createCategory(r, "Education", "expense", "ic_school", "#00BCD4");
            createCategory(r, "Home", "expense", "ic_home", "#795548");
            createCategory(r, "Phone", "expense", "ic_phone_android", "#607D8B");

            // Income categories
            createCategory(r, "Salary", "income", "ic_work", "#4CAF50");
            createCategory(r, "Freelance", "income", "ic_computer", "#2196F3");
            createCategory(r, "Investment", "income", "ic_trending_up", "#FF9800");
            createCategory(r, "Gift", "income", "ic_card_giftcard", "#9C27B0");
        });

        realm.close();
    }

    private void createCategory(Realm realm, String name, String type, String icon, String colorHex) {
        Category category = realm.createObject(Category.class, java.util.UUID.randomUUID().toString());
        category.setName(name);
        category.setType(type);
        category.setIconName(icon);
        category.setColor(android.graphics.Color.parseColor(colorHex));
        category.setCustom(false);
    }
}
