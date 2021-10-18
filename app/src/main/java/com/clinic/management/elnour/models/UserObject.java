package com.clinic.management.elnour.models;

public class UserObject {

    private String userRealName;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String unixTime;
    private boolean hasVerifiedNumber;
    private boolean isDisable;
    private boolean isAdmin;


    public UserObject () {

    }

    public UserObject (String userRealName, String userName, String userEmail, String userPhone,
                       String unixTime, boolean hasVerifiedNumber, boolean isDisable, boolean isAdmin) {

        this.userRealName = userRealName;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.unixTime = unixTime;
        this.hasVerifiedNumber = hasVerifiedNumber;
        this.isDisable = isDisable;
        this.isAdmin = isAdmin;

    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(String unixTime) {
        this.unixTime = unixTime;
    }

    public boolean isHasVerifiedNumber() {
        return hasVerifiedNumber;
    }

    public void setHasVerifiedNumber(boolean hasVerifiedNumber) {
        this.hasVerifiedNumber = hasVerifiedNumber;
    }

    public boolean isDisable() {
        return isDisable;
    }

    public void setDisable(boolean disable) {
        isDisable = disable;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
