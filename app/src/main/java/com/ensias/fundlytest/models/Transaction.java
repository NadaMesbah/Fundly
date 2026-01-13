package com.ensias.fundlytest.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import io.realm.annotations.Index;
import java.util.Date;
import java.util.UUID;

public class Transaction extends RealmObject {

    @PrimaryKey
    private String id;
    @Required
    @Index
    private String userId;

    private double amount;

    @Required
    private String categoryId;

    @Required
    private String type; // "income" or "expense"

    private Date date;
    private String note;
    private String iconName;
    private int color;

    public Transaction() {
    }

    public Transaction(double amount, String categoryId, String type, String note) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.categoryId = categoryId;
        this.type = type;
        this.note = note;
        this.date = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}