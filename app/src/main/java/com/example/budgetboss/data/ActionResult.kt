/*
 * Simple result wrapper for repository actions.
 * It allows functions to return either a successful value or an error message for the UI to display.
 */
package com.example.budgetboss.data

data class ActionResult<T>(
    val value: T? = null,
    val errorMessage: String? = null,
) {
    val isSuccess: Boolean = errorMessage == null
}


