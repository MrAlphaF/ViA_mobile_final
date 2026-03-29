package com.group3.financialapplication.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.group3.financialapplication.data.LocationManager
import com.group3.financialapplication.data.TransactionLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/** Draws a teardrop pin bitmap — large enough to tap, visible when zoomed out. */
private fun makePinDrawable(color: Int, context: android.content.Context): BitmapDrawable {
    val sizePx = (48 * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = sizePx * 0.07f
    }

    val cx    = sizePx / 2f
    val r     = sizePx * 0.35f          // circle radius
    val cy    = r + sizePx * 0.05f      // circle centre y (near top)
    val tipY  = sizePx * 0.95f          // point of the pin

    // Teardrop path: circle top + triangle pointing down
    val path = Path().apply {
        // Arc for the round head
        addCircle(cx, cy, r, Path.Direction.CW)
        // Triangle tip
        moveTo(cx - r * 0.5f, cy + r * 0.7f)
        lineTo(cx, tipY)
        lineTo(cx + r * 0.5f, cy + r * 0.7f)
        close()
    }

    canvas.drawPath(path, paint)
    canvas.drawPath(path, strokePaint)

    // White dot in centre of circle
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, r * 0.3f, dotPaint)

    return BitmapDrawable(context.resources, bitmap)
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val locations = remember { locationManager.loadAll() }
    var selectedLocation by remember { mutableStateOf<TransactionLocation?>(null) }

    Configuration.getInstance().userAgentValue = context.packageName

    val defaultCenter = if (locations.isNotEmpty())
        GeoPoint(locations.first().latitude, locations.first().longitude)
    else
        GeoPoint(56.9460, 24.1059)

    // Pre-build drawables once
    val expensePin = remember { makePinDrawable(android.graphics.Color.parseColor("#D32F2F"), context) }
    val incomePin  = remember { makePinDrawable(android.graphics.Color.parseColor("#2E7D32"), context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isHorizontalMapRepetitionEnabled = false
                    isVerticalMapRepetitionEnabled   = false
                    minZoomLevel = 4.0
                    maxZoomLevel = 19.0
                    controller.setZoom(14.0)
                    controller.setCenter(defaultCenter)
                    setScrollableAreaLimitDouble(
                        BoundingBox(85.0, 180.0, -85.0, -180.0)
                    )

                    locations.forEach { loc ->
                        val marker = Marker(this).apply {
                            position = GeoPoint(loc.latitude, loc.longitude)
                            title    = loc.description
                            snippet  = "${if (loc.isExpense) "-" else "+"}€${"%.2f".format(loc.amount)} · ${loc.category}"
                            icon     = if (loc.isExpense) expensePin else incomePin
                            // Anchor: horizontal centre, vertical bottom (tip of pin)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedLocation = loc
                                true
                            }
                        }
                        overlays.add(marker)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (locations.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "No locations saved yet.\nToggle \"Save Location\" when adding a transaction.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    selectedLocation?.let { loc ->
        SpendingInfoDialog(location = loc, onDismiss = { selectedLocation = null })
    }
}

@Composable
fun SpendingInfoDialog(location: TransactionLocation, onDismiss: () -> Unit) {
    val amountColor = if (location.isExpense) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val sign = if (location.isExpense) "-" else "+"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(location.description, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoRow("Amount", "$sign€${"%.2f".format(location.amount)}", amountColor)
                InfoRow("Category", location.category)
                InfoRow("Type", if (location.isExpense) "Expense" else "Income")
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium)
    }
}