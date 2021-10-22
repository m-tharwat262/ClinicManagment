package com.clinic.management.elnour.models;


public class PaymentObject {

    private String name;
    private double itemsNumber = 0.0;
    private double cost = 0.0;
    private long addedDate;
    private String addedBy;



    public PaymentObject() {

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getItemsNumber() {
        return itemsNumber;
    }

    public void setItemsNumber(double itemsNumber) {
        this.itemsNumber = itemsNumber;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public long getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(long addedDate) {
        this.addedDate = addedDate;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
}
