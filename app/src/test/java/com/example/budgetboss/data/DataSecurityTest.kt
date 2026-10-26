package com.example.budgetboss.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataSecurityTest {

    @Test
    fun strongPassword_acceptsValidPassword() {
        assertTrue(isStrongPassword("Budget1234"))
    }

    @Test
    fun strongPassword_rejectsPasswordWithoutUppercase() {
        assertFalse(isStrongPassword("budget1234"))
    }

    @Test
    fun strongPassword_rejectsPasswordWithoutNumber() {
        assertFalse(isStrongPassword("Budgetpass"))
    }

    @Test
    fun strongPassword_rejectsShortPassword() {
        assertFalse(isStrongPassword("Bu1dg"))
    }
}

