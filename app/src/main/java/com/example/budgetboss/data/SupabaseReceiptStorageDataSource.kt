package com.example.budgetboss.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.budgetboss.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseReceiptStorageDataSource(
    private val context: Context,
) {
    fun shouldUpload(receiptUri: String?): Boolean {
        if (receiptUri.isNullOrBlank()) return false
        return !receiptUri.startsWith("http://") && !receiptUri.startsWith("https://")
    }

    suspend fun uploadReceipt(
        user: UserEntity,
        expense: ExpenseEntity,
    ): ActionResult<String> = withContext(Dispatchers.IO) {
        val sourceUri = expense.receiptUri
        if (!shouldUpload(sourceUri)) {
            return@withContext ActionResult(value = sourceUri)
        }
        if (!isConfigured() || user.remoteId.isBlank()) {
            return@withContext ActionResult(value = sourceUri)
        }

        runCatching {
            val parsedUri = Uri.parse(sourceUri)
            val mimeType = context.contentResolver.getType(parsedUri) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(parsedUri)?.use { it.readBytes() }
                ?: error("Could not read the selected receipt image.")
            val objectPath = buildObjectPath(user, expense, mimeType)
            val endpoint = "${baseUrl()}/storage/v1/object/${bucketName()}/$objectPath"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                setRequestProperty("Content-Type", mimeType)
            }

            connection.outputStream.use { it.write(bytes) }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                error("Supabase upload failed ($responseCode). $error")
            }
            "${baseUrl()}/storage/v1/object/public/${bucketName()}/$objectPath"
        }.fold(
            onSuccess = {
                Log.d(TAG, "Receipt uploaded to Supabase.")
                ActionResult(value = it)
            },
            onFailure = { error ->
                Log.w(TAG, "Could not upload receipt to Supabase.", error)
                ActionResult(errorMessage = error.localizedMessage ?: "Could not upload the receipt image.")
            },
        )
    }

    private fun isConfigured(): Boolean =
        BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private fun buildObjectPath(
        user: UserEntity,
        expense: ExpenseEntity,
        mimeType: String,
    ): String {
        val extension = when (mimeType.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        return "${user.remoteId}/${expense.id}-${System.currentTimeMillis()}.$extension"
    }

    private fun baseUrl(): String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')

    private fun bucketName(): String = BuildConfig.SUPABASE_RECEIPT_BUCKET.ifBlank { "receipts" }

    private companion object {
        const val TAG = "SupabaseReceipts"
    }
}

