package com.clinic.management.elnour.models;

public class PatientObject {

    private String name;
    private double age;
    private String disease;

    private double wallet = 0.0;
    private double dept = 0.0;
    private double sessionCost;
    private double allSessionsNumber = 0;
    private String phoneNumber;
    private long addedTime;
    private String addedBy;

    public PatientObject() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    public double getDept() {
        return dept;
    }

    public void setDept(double dept) {
        this.dept = dept;
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
