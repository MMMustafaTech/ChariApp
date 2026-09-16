package com.chari.chariapp.account.application;

public record LoginCommand(String emailLookup, String rawPassword, String nationalIdLookup) {
    public LoginCommand(String emailLookup, String rawPassword) {
        this(emailLookup, rawPassword, null);
    }
}
