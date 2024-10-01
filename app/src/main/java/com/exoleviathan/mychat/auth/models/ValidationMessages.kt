package com.exoleviathan.mychat.auth.models

enum class ValidationMessages(val msg: String, val msgDetails: String) {
    LOGIN_SUCCESSFUL(
        "Successful",
        "User login is completed successfully."
    ),
    REGISTRATION_SUCCESSFUL(
        "Successful",
        "User registration is completed successfully."
    ),
    EMAIL_SEND_SUCCESSFUL(
        "Successful",
        "Reset password email is sent successfully."
    ),
    INVALID_USERNAME(
        "Invalid username",
        """User name can contain text and numbers.
            Username must be more than 2 character and less than 21 character
            Username can only contain underscore(_), dash(-), space( ) or dot(.)."""
    ),
    CORRECT_EMAIL_FORMAT_PROVIDED(
        "Successful",
        "Correct email format is provided."
    ),
    INVALID_EMAIL_ADDRESS(
        "Invalid email address",
        "Please enter a valid email address (ex. abc@xyz)."
    ),
    INVALID_PASSWORD(
        "Invalid password",
        """Password is not valid.
            Password must be at least 8 characters long
            It should not contain any space.
            Password must contain at least 1 digit, 1 lower case letter, 1 upper case letter and 1 special character."""
    ),
    PASSWORD_MISMATCHED(
        "Password mismatched",
        "The password entered does not match with the previous input."
    ),
    REGISTRATION_ERROR(
        "User registration failed",
        "Error while registering the new user."
    ),
    LOGIN_ERROR(
        "User login failed",
        "Error while login with the given email and password."
    ),
    SEND_EMAIL_ERROR(
        "Reset password error",
        "Error while sending reset link to email."
    ),
}