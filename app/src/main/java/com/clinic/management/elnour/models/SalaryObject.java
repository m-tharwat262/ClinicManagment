package com.clinic.management.elnour.models;


public class SalaryObject {

    private String name;
    private String job;
    private double salary = 0.0;
    private double sessionsNumber = 0;
    private boolean isGotSalary = false;
    private long gotSalaryDate = -1;
    private String gotSalaryBy;


    public SalaryObject() {

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

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public double getSessionsNumber() {
        return sessionsNumber;
    }

    public void setSessionsNumber(double sessionsNumber) {
        this.sessionsNumber = sessionsNumber;
    }

    public boolean isGotSalary() {
        return isGotSalary;
    }

    public void setGotSalary(boolean gotSalary) {
        isGotSalary = gotSalary;
    }

    public long getGotSalaryDate() {
        return gotSalaryDate;
    }

    public void setGotSalaryDate(long gotSalaryDate) {
        this.gotSalaryDate = gotSalaryDate;
    }

    public String getGotSalaryBy() {
        return gotSalaryBy;
    }

    public void setGotSalaryBy(String gotSalaryBy) {
        this.gotSalaryBy = gotSalaryBy;
    }
}
