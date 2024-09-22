package com.exoleviathan.mychat.auth.models

enum class ValidationMessages(val msg: String, val msgDetails: String) {
    LOGIN_SUCCESSFUL(
        "Successful",
        "User login completed successfully."
    ),
    REGISTRATION_SUCCESSFUL(
        "Successful",
        "User registration completed successfully."
    ),
    EMAIL_SEND_SUCCESS(
        "Successful",
        "Reset password email sent successfully."
    ),
    INVALID_USERNAME(
        "Invalid username",
        "User name should only contain text and numbers.\n\n" +
                "Username must be more than 2 character and less than 21 character.\n" +
                "Username should not contain any special characters or space."
    ),
    INVALID_EMAIL_ADDRESS(
        "Invalid email address",
        "Please enter a valid email address (ex. abc@xyz)."
    ),
    INVALID_PASSWORD(
        "Invalid password",
        "Password is not valid.\n\n" +
                "Password must be at least 8 characters long.\n" +
                "It should not contain any space.\n" +
                "Password must contain at least 1 digit, 1 lower case letter, 1 upper case letter and 1 special character."
    ),
    PASSWORD_MISMATCHED(
        "Password mismatched",
        "Password does not match with the previous input."
    ),
    REGISTRATION_ERROR(
        "User registration failed",
        "Error while registering new user."
    ),
    LOGIN_ERROR(
        "User login failed",
        "Error while login with the given email and password."
    ),
    SEND_EMAIL_ERROR(
        "Reset password error",
        "Error while sending reset link to email."
    ),
    EMAIL_VERIFICATION_NEEDED(
        "Email verification needed",
        "An email has been sent to email address for verification. Follow the link to verify your email address."
    ),
    RELOAD_FAILED(
        "Reloading user information failed",
        "Failed to load user information due to some internal errors."
    )
}