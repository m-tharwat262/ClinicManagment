package com.clinic.management.elnour.models;

public class SessionObject {

    private String doctorId;
    private String doctorName;
    private double doctorSessionCost;
    private String patientId;
    private String patientName;
    private double patientSessionCost;
    private String disease;
    private double sessionsNumber;
    private String notes;
    private long addedTime;
    private String addedBy;

    public SessionObject() {
    }


    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public double getDoctorSessionCost() {
        return doctorSessionCost;
    }

    public void setDoctorSessionCost(double doctorSessionCost) {
        this.doctorSessionCost = doctorSessionCost;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public double getPatientSessionCost() {
        return patientSessionCost;
    }

    public void setPatientSessionCost(double patientSessionCost) {
        this.patientSessionCost = patientSessionCost;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public double getSessionsNumber() {
        return sessionsNumber;
    }

    public void setSessionsNumber(double sessionsNumber) {
        this.sessionsNumber = sessionsNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
