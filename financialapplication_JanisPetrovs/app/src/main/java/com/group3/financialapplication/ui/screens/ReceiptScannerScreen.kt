package com.group3.financialapplication.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.group3.financialapplication.data.Transaction
import com.group3.financialapplication.ui.viewmodel.FinanceViewModel
import java.io.File
import java.util.Date
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerScreen(navController: NavController, viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedAmount by remember { mutableStateOf<Double?>(null) }
    var scannedDescription by remember { mutableStateOf("Receipt") }
    var isScanning by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isScanning = true
            statusMessage = "Scanning..."
            processReceiptImage(context, it) { amount, message ->
                isScanning = false
                scannedAmount = amount
                statusMessage = message
                if (amount != null) showSaveDialog = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (showSaveDialog && scannedAmount != null) {
        SaveReceiptDialog(
            amount = scannedAmount!!,
            description = scannedDescription,
            onDescriptionChange = { scannedDescription = it },
            onConfirm = {
                viewModel.addTransaction(
                    Transaction(
                        description = scannedDescription,
                        amount = scannedAmount!!,
                        date = Date().time,
                        isExpense = true,
                        category = "Other"
                    )
                )
                showSaveDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showSaveDialog = false
                scannedAmount = null
                statusMessage = "Cancelled."
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera preview
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val future = ProcessCameraProvider.getInstance(ctx)
                            future.addListener({
                                val provider = future.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                try {
                                    provider.unbindAll()
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageCapture
                                    )
                                } catch (e: Exception) { e.printStackTrace() }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Camera permission required")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
                if (isScanning) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Status
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(8.dp),
                    color = if (scannedAmount != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            // Hint text
            Text(
                text = "Point at the TOTAL line on your receipt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery")
                }
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            isScanning = true
                            statusMessage = "Scanning..."
                            val file = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                            val options = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture.takePicture(
                                options, cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(out: ImageCapture.OutputFileResults) {
                                        processReceiptImage(context, Uri.fromFile(file)) { amount, message ->
                                            isScanning = false
                                            scannedAmount = amount
                                            statusMessage = message
                                            if (amount != null) showSaveDialog = true
                                        }
                                    }
                                    override fun onError(e: ImageCaptureException) {
                                        isScanning = false
                                        statusMessage = "Failed to capture image."
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = hasCameraPermission && !isScanning
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Capture")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// OCR processing
// ---------------------------------------------------------------------------

private fun processReceiptImage(
    context: android.content.Context,
    uri: Uri,
    onResult: (Double?, String) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val result = extractTotal(visionText.text)
                if (result != null) {
                    onResult(result, "Found total: ${"%.2f".format(result)}")
                } else {
                    onResult(
                        null,
                        "No total found. Make sure the receipt shows a word like \"Total\" or \"Kopā\"."
                    )
                }
            }
            .addOnFailureListener {
                onResult(null, "OCR failed: ${it.message}")
            }
    } catch (e: Exception) {
        onResult(null, "Error reading image.")
    }
}

/**
 * Keyword patterns that indicate a "total" line on a receipt.
 *
 * Supported languages / variants:
 *   English : total, grand total, amount due, amount to pay, subtotal,
 *             balance due, net total, to pay
 *   Latvian : kopā, kopa, summa, maksājums, apmaksāt, maksāt, pavisam,
 *             kopējā summa
 *   Lithuanian: iš viso, viso, mokėti
 *   Estonian : kokku, tasuda
 *   Russian  : итого, сумма, к оплате
 */
private val TOTAL_KEYWORDS = listOf(
    // English
    "grand total", "amount due", "amount to pay", "balance due",
    "net total", "total due", "to pay", "subtotal", "total",
    // Latvian (all common variants including diacritics and ASCII fallbacks)
    "kopējā summa", "kopeja summa",
    "kopā", "kopa", "kopaa",          // "kopā" with and without diacritics
    "summa", "maksājums", "maksajums",
    "apmaksāt", "apmaksat",
    "maksāt", "maksat",
    "pavisam",
    // Lithuanian
    "iš viso", "mokėti", "viso",
    // Estonian
    "tasuda", "kokku",
    // Russian
    "к оплате", "итого", "сумма"
)

// Matches: 1234.56  |  1234,56  |  1.234,56  |  1,234.56  |  1234
private val NUMBER_REGEX = Regex("""(\d{1,3}(?:[.,\s]\d{3})*[.,]\d{2}|\d+[.,]\d{1,2}|\d+)""")

/**
 * Returns the amount found next to a total keyword, or null if none found.
 * Does NOT fall back to the largest number — if there's no keyword match
 * the user is prompted to try again.
 */
fun extractTotal(rawText: String): Double? {
    val lines = rawText.lines()

    // Sort keywords longest-first so "grand total" matches before "total"
    val sortedKeywords = TOTAL_KEYWORDS.sortedByDescending { it.length }

    for (i in lines.indices) {
        val lineLower = lines[i].lowercase().trim()

        val matchedKeyword = sortedKeywords.firstOrNull { lineLower.contains(it) }
            ?: continue  // no keyword on this line → skip

        // 1. Try numbers on the SAME line (after the keyword)
        val keywordEnd = lineLower.indexOf(matchedKeyword) + matchedKeyword.length
        val afterKeyword = lines[i].drop(keywordEnd)
        val sameLineNumber = extractLargestNumber(afterKeyword)
        if (sameLineNumber != null && sameLineNumber > 0) return sameLineNumber

        // 2. Try the NEXT line (some receipts put amount on its own line)
        if (i + 1 < lines.size) {
            val nextLineNumber = extractLargestNumber(lines[i + 1])
            if (nextLineNumber != null && nextLineNumber > 0) return nextLineNumber
        }

        // 3. Try the PREVIOUS line (rare, but some receipt formats)
        if (i > 0) {
            val prevLineNumber = extractLargestNumber(lines[i - 1])
            if (prevLineNumber != null && prevLineNumber > 0) return prevLineNumber
        }
    }

    return null  // no keyword found → return null, don't guess
}

/** Pull out the largest valid currency number from a string. */
private fun extractLargestNumber(text: String): Double? =
    NUMBER_REGEX.findAll(text)
        .mapNotNull { it.value.replace(",", ".").replace(" ", "").toDoubleOrNull() }
        // Ignore implausible values: must be > 0 and < 100 000
        .filter { it > 0.0 && it < 10_000.0 }
        .maxOrNull()

// ---------------------------------------------------------------------------
// Save dialog
// ---------------------------------------------------------------------------

@Composable
private fun SaveReceiptDialog(
    amount: Double,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Receipt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Detected total: ${"%.2f".format(amount)}")
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Save as Expense") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}