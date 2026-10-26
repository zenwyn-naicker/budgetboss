package com.example.budgetboss.data

internal fun isStrongPassword(password: String): Boolean {
    return password.length >= 8 &&
        password.any { it.isUpperCase() } &&
        password.any { it.isLowerCase() } &&
        password.any { it.isDigit() }
}

