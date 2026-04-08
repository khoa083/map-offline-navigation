package com.kblack.offlinemap.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kblack.offlinemap.BuildConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BugHandlerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val exceptionMessage = intent.getStringExtra("exception_message") ?: "Unknown error"
        val threadName = intent.getStringExtra("thread") ?: "Unknown"

        val combinedText = buildCombinedText(exceptionMessage, threadName)

        setContent {
            BugHandlerScreen(
                combinedText = combinedText,
                onBackPressed = { finish() },
                onCopy = { copyToClipboard(it) },
                onShare = { text -> shareErrorLog(text) }
            )
        }

        copyToClipboard(combinedText)
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("error msg", text)
        allowDiskAccessInStrictMode {
            clipboard.setPrimaryClip(clip)
        }
        if (!hasOsClipboardDialog()) {
            Toast.makeText(this, "Copied crash log to clipboard", Toast.LENGTH_LONG).show()
        }
    }

    private fun readBuildConfigString(fieldName: String, fallback: String): String {
        return runCatching {
            BuildConfig::class.java.getField(fieldName)[null] as? String
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun buildCombinedText(exceptionMessage: String, threadName: String): String {
        val deviceBrand = Build.BRAND
        val deviceModel = Build.MODEL
        val sdkLevel = Build.VERSION.SDK_INT
        val currentDateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDateTime = formatter.format(currentDateTime)
        val versionName = readBuildConfigString("MY_VERSION_NAME", BuildConfig.VERSION_NAME)
        val commitName = readBuildConfigString("MY_COMMIT_NAME", "unknown")

        return buildString {
            append("Version: $versionName\n\n")
            append("Commit: $commitName\n\n")
            append("Brand:      $deviceBrand\n")
            append("Model:      $deviceModel\n")
            append("SDK level: $sdkLevel\n")
            append("Thread:    $threadName\n\n\n")
            append("Crash Time: $formattedDateTime\n\n")
            append("--------- Beginning of crash ---------\n\n")
            append(exceptionMessage)
        }
    }

    private fun shareErrorLog(text: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "Kblack Logs")
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugHandlerScreen(
    combinedText: String,
    onBackPressed: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Bug Report") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onCopy(combinedText) }) {
                        Text("Copy")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onShare(combinedText) },
                icon = { Icon(Icons.Filled.Share, contentDescription = "Share") },
                text = { Text("Share") },
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SelectionContainer {
                Text(
                    text = combinedText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(62.dp))
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun hasOsClipboardDialog(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

inline fun <reified T> allowDiskAccessInStrictMode(relax: Boolean = false, doIt: () -> T): T {
    return if (BuildConfig.DEBUG) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            if (relax) doIt() else
                throw IllegalStateException("allowDiskAccessInStrictMode(false) on wrong thread")
        } else {
            val policy = StrictMode.allowThreadDiskReads()
            try {
                StrictMode.allowThreadDiskWrites()
                doIt()
            } finally {
                StrictMode.setThreadPolicy(policy)
            }
        }
    } else doIt()
}