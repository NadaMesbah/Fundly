package com.ensias.fundlytest.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import io.realm.annotations.Index;
import java.util.UUID;

public class Category extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    @Index
    private String userId;

    @Required
    private String name;

    @Required
    private String type; // "expense" or "income"

    private String iconName;
    private int color;
    private boolean isCustom;
    private int order;

    public Category() {
    }

    public Category(String name, String type, String iconName, int color) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.iconName = iconName;
        this.color = color;
        this.isCustom = true;
        this.order = 999;
    }

    public Category(String name, String type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.isCustom = true;
        this.order = 999;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}