package com.clinic.management.elnour.models;

public class EmployeeObject {

    private String name;
    private String job;
    private double balance = 0.0;
    private double sessionCost;
    private double allSessionsNumber = 0;
    private String phoneNumber;
    private long addedTime;
    private String addedBy;

    public EmployeeObject() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getSessionCost() {
        return sessionCost;
    }

    public void setSessionCost(double sessionCost) {
        this.sessionCost = sessionCost;
    }

    public double getAllSessionsNumber() {
        return allSessionsNumber;
    }

    public void setAllSessionsNumber(double allSessionsNumber) {
        this.allSessionsNumber = allSessionsNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
}
