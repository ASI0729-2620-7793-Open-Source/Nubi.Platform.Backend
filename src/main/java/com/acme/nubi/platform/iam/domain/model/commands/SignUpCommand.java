package com.acme.nubi.platform.iam.domain.model.commands;

public record SignUpCommand(String email, String fullName, String password) {
}
