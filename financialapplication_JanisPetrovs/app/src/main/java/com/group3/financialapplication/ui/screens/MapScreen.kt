package com.group3.financialapplication.ui.screens

import android.graphics.drawable.Drawable
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
import androidx.core.content.ContextCompat
import com.group3.financialapplication.R
import com.group3.financialapplication.data.LocationManager
import com.group3.financialapplication.data.TransactionLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val locations = remember { locationManager.loadAll() }
    var selectedLocation by remember { mutableStateOf<TransactionLocation?>(null) }

    // osmdroid requires this before use
    Configuration.getInstance().userAgentValue = context.packageName

    val defaultCenter = if (locations.isNotEmpty())
        GeoPoint(locations.first().latitude, locations.first().longitude)
    else
        GeoPoint(56.9460, 24.1059) // Riga

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    controller.setCenter(defaultCenter)

                    locations.forEach { loc ->
                        val marker = Marker(this).apply {
                            position = GeoPoint(loc.latitude, loc.longitude)
                            title = loc.description
                            snippet = "${if (loc.isExpense) "-" else "+"}€${"%.2f".format(loc.amount)} · ${loc.category}"
                            // Red pin for expense, green for income
                            icon = ContextCompat.getDrawable(
                                ctx,
                                if (loc.isExpense) android.R.drawable.presence_busy
                                else android.R.drawable.presence_online
                            )
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
        SpendingInfoDialog(
            location = loc,
            onDismiss = { selectedLocation = null }
        )
    }
}

@Composable
fun SpendingInfoDialog(
    location: TransactionLocation,
    onDismiss: () -> Unit
) {
    val amountColor = if (location.isExpense) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val sign = if (location.isExpense) "-" else "+"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(location.description, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoRow(
                    label = "Amount",
                    value = "$sign€${"%.2f".format(location.amount)}",
                    valueColor = amountColor
                )
                InfoRow(label = "Category", value = location.category)
                InfoRow(
                    label = "Type",
                    value = if (location.isExpense) "Expense" else "Income"
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}