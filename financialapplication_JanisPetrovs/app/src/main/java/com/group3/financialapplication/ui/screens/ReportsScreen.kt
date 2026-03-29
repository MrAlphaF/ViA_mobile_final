package com.group3.financialapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group3.financialapplication.ui.viewmodel.BarChartData
import com.group3.financialapplication.ui.viewmodel.FinanceViewModel
import com.group3.financialapplication.ui.viewmodel.GaugeData
import com.group3.financialapplication.ui.viewmodel.ReportsData
import kotlin.math.roundToInt

@Composable
fun ReportsScreen(viewModel: FinanceViewModel) {
    val reportsData by viewModel.getReportsData().collectAsState(
        initial = ReportsData(emptyList(), emptyList())
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Top spending categories ──────────────────────────────────────
        Text(
            "Top Spending Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (reportsData.gaugeData.isEmpty()) {
            EmptyCard("No expense data for this month.")
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                reportsData.gaugeData.forEach { data ->
                    BudgetGauge(
                        label = data.category,
                        spentAmount = data.spent,
                        budgetAmount = data.budget,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        HorizontalDivider()

        // ── Daily spending bar chart ──────────────────────────────────────
        Text(
            "Spending by Day",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (reportsData.barChartData.isEmpty()) {
            EmptyCard("No expense data for this month.")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Bar chart with fixed height so it doesn't fight the scroll
                    SimpleBarChart(
                        data = reportsData.barChartData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    // X-axis day labels — show every other label if too many
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val step = if (reportsData.barChartData.size > 10) 2 else 1
                        reportsData.barChartData.forEachIndexed { i, item ->
                            if (i % step == 0) {
                                Text(
                                    text = item.day,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // ── Category breakdown list ───────────────────────────────────────
        Text(
            "Category Breakdown",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (reportsData.gaugeData.isEmpty()) {
            EmptyCard("Add some expenses to see a breakdown.")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val totalSpent = reportsData.gaugeData.sumOf { it.spent }
                    reportsData.gaugeData.forEachIndexed { index, data ->
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        CategoryRow(data = data, totalSpent = totalSpent)
                    }
                }
            }
        }

        // Bottom spacing
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun BudgetGauge(
    label: String,
    spentAmount: Double,
    budgetAmount: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (budgetAmount > 0) (spentAmount / budgetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val color = when {
        progress < 0.5f -> Color(0xFF2E7D32)
        progress < 0.8f -> Color(0xFFFF9800)
        else            -> Color.Red
    }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.LightGray,
                    startAngle = -215f, sweepAngle = 250f,
                    useCenter = false,
                    style = Stroke(width = 18f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -215f, sweepAngle = 250f * progress,
                    useCenter = false,
                    style = Stroke(width = 18f, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(progress * 100).roundToInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "€${"%.2f".format(spentAmount)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SimpleBarChart(data: List<BarChartData>, modifier: Modifier = Modifier) {
    val maxAmount = data.maxOfOrNull { it.amount } ?: 1f
    val barColor  = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val count        = data.size
        val totalWidth   = size.width
        val totalHeight  = size.height
        val barWidth     = (totalWidth / count) * 0.5f
        val gap          = (totalWidth / count) * 0.5f
        val slotWidth    = barWidth + gap

        data.forEachIndexed { i, item ->
            val barHeight = (item.amount / maxAmount) * totalHeight
            val left      = i * slotWidth + gap / 2f
            val top       = totalHeight - barHeight

            drawRect(
                color     = barColor,
                topLeft   = Offset(left, top),
                size      = Size(barWidth, barHeight)
            )
        }

        // Baseline
        drawLine(
            color       = Color.Gray,
            start       = Offset(0f, totalHeight),
            end         = Offset(totalWidth, totalHeight),
            strokeWidth = 2f
        )
    }
}

@Composable
private fun CategoryRow(data: GaugeData, totalSpent: Double) {
    val pct      = if (totalSpent > 0) (data.spent / totalSpent * 100).roundToInt() else 0
    val progress = if (totalSpent > 0) (data.spent / totalSpent).toFloat().coerceIn(0f, 1f) else 0f
    val color    = when {
        progress < 0.4f -> Color(0xFF2E7D32)
        progress < 0.7f -> Color(0xFFFF9800)
        else            -> Color.Red
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(data.category, fontWeight = FontWeight.Medium)
            Text(
                "€${"%.2f".format(data.spent)}  ($pct%)",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier  = Modifier.fillMaxWidth().height(6.dp),
            color     = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}