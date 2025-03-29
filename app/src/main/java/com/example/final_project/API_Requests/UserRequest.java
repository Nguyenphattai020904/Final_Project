package com.example.final_project.API_Requests;

public class UserRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String gender;
    private String dateOfBirth;
    private String otp;
    private String newPassword;

    // Default constructor
    public UserRequest() {}

    // Static factory method for registration
    public static UserRequest createRegistrationRequest(String name, String email, String phone, String password, String gender, String dateOfBirth) {
        UserRequest request = new UserRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);
        request.setPassword(password);
        request.setGender(gender);
        request.setDateOfBirth(dateOfBirth);
        return request;
    }

    // Static factory method for login
    public static UserRequest createLoginRequest(String email, String password) {
        UserRequest request = new UserRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    // Static factory method for OTP actions
    public static UserRequest createOtpRequest(String email, String otp) {
        UserRequest request = new UserRequest();
        request.setEmail(email);
        request.setOtp(otp);
        return request;
    }

    // Static factory method for password reset
    public static UserRequest createPasswordResetRequest(String email, String newPassword) {
        UserRequest request = new UserRequest();
        request.setEmail(email);
        request.setNewPassword(newPassword);
        return request;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}