package com.janis_petrovs.financialapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.janis_petrovs.financialapplication.ui.viewmodel.ChartData
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import com.janis_petrovs.financialapplication.ui.viewmodel.MonthlySummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: FinanceViewModel) {
    // Collect the full monthly summary and the selected date from the ViewModel
    val summary by viewModel.monthlySummary.collectAsState(
        initial = MonthlySummary(0.0, 0.0, emptyList())
    )
    val selectedDate by viewModel.selectedDate.collectAsState()
    val savedAmount = summary.totalIncome - summary.totalExpenses

    val sliceColors = listOf(
        Color(0xff3F51B5), Color(0xffF44336), Color(0xff9C27B0),
        Color(0xff009688), Color(0xffFF9800), Color(0xff795548), Color(0xff607D8B)
    )

    // Check if the "Next" button should be enabled (true if not the current month)
    val isNextMonthButtonEnabled = !isSameMonth(selectedDate, Calendar.getInstance())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month navigation controls
        MonthSelector(
            calendar = selectedDate,
            onPrevious = { viewModel.goToPreviousMonth() },
            onNext = { viewModel.goToNextMonth() },
            isNextEnabled = isNextMonthButtonEnabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Income", summary.totalIncome, Color(0xFF008000))
                SummaryRow("Expenses", summary.totalExpenses, Color.Red)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SummaryRow("Saved", savedAmount, if (savedAmount >= 0) Color(0xFF008000) else Color.Red, isTotal = true)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pie Chart and Legend Section
        if (summary.chartData.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No expense data for this month.")
            }
        } else {
            CustomPieChart(
                data = summary.chartData,
                colors = sliceColors,
                modifier = Modifier.height(200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            ChartLegend(chartData = summary.chartData, colors = sliceColors)
        }
    }
}

@Composable
fun MonthSelector(
    calendar: Calendar,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isNextEnabled: Boolean
) {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }
        Text(
            text = formatter.format(calendar.time),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNext, enabled = isNextEnabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}

private fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

@Composable
fun SummaryRow(label: String, amount: Double, color: Color, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 18.sp else 16.sp
        )
        Text(
            text = "$${"%.2f".format(amount)}",
            color = color,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 18.sp else 16.sp
        )
    }
}

@Composable
fun CustomPieChart(
    data: List<ChartData>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val totalAmount = data.sumOf { it.amount.toDouble() }.toFloat()
    var startAngle = -90f

    Canvas(modifier = modifier.aspectRatio(1f)) {
        data.forEachIndexed { index, item ->
            val sweepAngle = (item.amount / totalAmount) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun ChartLegend(chartData: List<ChartData>, colors: List<Color>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        chartData.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colors[index % colors.size])
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.category,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$${"%.2f".format(item.amount)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            HorizontalDivider()
        }
    }
}