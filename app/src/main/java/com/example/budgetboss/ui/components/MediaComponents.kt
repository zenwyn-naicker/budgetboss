/*
 * Shared receipt image components for BudgetBoss.
 * It displays receipt previews and opens saved receipt images in a viewer dialog.
 */
package com.example.budgetboss.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReceiptPreview(
    receiptUri: String?,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No receipt attached yet.",
    onPreviewClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, receiptUri) {
        value = loadBitmap(context, receiptUri)
    }
    Card(
        modifier = modifier.then(
            if (imageBitmap != null && onPreviewClick != null) {
                Modifier.clickable(onClick = onPreviewClick)
            } else {
                Modifier
            },
        ),
        shape = ItemCardShape,
        border = SubtleBorder(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (receiptUri != null && imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = "Receipt preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(emptyMessage)
            }
        }
    }
}

@Composable
fun ReceiptViewerDialog(
    receiptUri: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, receiptUri) {
        value = loadBitmap(context, receiptUri)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f)),
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Receipt image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Unable to load receipt image.",
                        color = Color.White,
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close receipt viewer",
                    tint = Color.White,
                )
            }
        }
    }
}

suspend fun loadBitmap(context: Context, receiptUri: String?): ImageBitmap? = withContext(Dispatchers.IO) {
    if (receiptUri.isNullOrBlank()) {
        null
    } else {
        runCatching {
        val stream = if (receiptUri.startsWith("http://") || receiptUri.startsWith("https://")) {
            URL(receiptUri).openStream()
        } else {
            context.contentResolver.openInputStream(Uri.parse(receiptUri))
        }
        stream?.use { input ->
            BitmapFactory.decodeStream(input)?.asImageBitmap()
        }
        }.getOrNull()
    }
}

