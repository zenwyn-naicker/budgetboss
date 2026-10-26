/*
 * Receipt photo helper functions for expense entries.
 * It creates secure file URIs so the camera can save receipt images through Android FileProvider.
 */
package com.example.budgetboss.feature.expenses

import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createReceiptCaptureUri(context: android.content.Context): Uri {
    val receiptDirectory = File(context.filesDir, "receipt_images").apply { mkdirs() }
    val imageFile = File.createTempFile(
        "receipt_${System.currentTimeMillis()}_",
        ".jpg",
        receiptDirectory,
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile,
    )
}

