package com.ensias.fundlytest.models;
public class ChartData {
    private String category;
    private float amount;
    private float percentage;
    private int color;

    public ChartData() {}

    public ChartData(String category, float amount, float percentage, int color) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}