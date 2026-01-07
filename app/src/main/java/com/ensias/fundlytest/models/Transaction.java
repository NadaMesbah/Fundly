package com.ensias.fundlytest.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import java.util.Date;
import java.util.UUID;

public class Transaction extends RealmObject {

    @PrimaryKey
    private String id;

    private double amount;

    @Required
    private String categoryId; // Reference to category

    @Required
    private String type; // "income" or "expense"

    private Date date;
    private String note;

    // Denormalized fields for quick access (copied from category)
    private String iconName;
    private int color;

    // Realm requires empty constructor
    public Transaction() {
        // Don't generate UUID here - Realm handles it differently
    }

    // Constructor for creating new transactions
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
