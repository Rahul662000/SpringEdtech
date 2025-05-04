package com.edtech.auth.DTO;

public class OTPresponseDTO {
    private boolean success;
    private String message;
    private String otp;

    public OTPresponseDTO(boolean success, String message, String otp) {
        this.success = success;
        this.message = message;
        this.otp = otp;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}