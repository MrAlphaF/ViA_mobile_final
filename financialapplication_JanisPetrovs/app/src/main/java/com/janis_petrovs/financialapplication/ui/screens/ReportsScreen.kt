package com.janis_petrovs.financialapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.janis_petrovs.financialapplication.ui.viewmodel.BarChartData
import com.janis_petrovs.financialapplication.ui.viewmodel.FinanceViewModel
import com.janis_petrovs.financialapplication.ui.viewmodel.ReportsData
import kotlin.math.roundToInt // <-- THE NEW, NECESSARY IMPORT

@Composable
fun ReportsScreen(viewModel: FinanceViewModel) {
    val reportsData by viewModel.getReportsData().collectAsState(
        initial = ReportsData(emptyList(), emptyList())
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Spending Breakdown", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            reportsData.gaugeData.forEach { data ->
                BudgetGauge(
                    label = data.category,
                    spentAmount = data.spent,
                    budgetAmount = data.budget
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        Text("Spend by Days", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (reportsData.barChartData.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No expense data for this month.")
            }
        } else {
            CustomBarChart(data = reportsData.barChartData, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun BudgetGauge(
    label: String,
    spentAmount: Double,
    budgetAmount: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (budgetAmount > 0) (spentAmount / budgetAmount).toFloat() else 0f
    val color = if (progress < 0.25) Color(0xFF008000) else if (progress < 0.5) Color(0xFFFF9800) else Color.Red

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.LightGray,
                    startAngle = -215f,
                    sweepAngle = 250f,
                    useCenter = false,
                    style = Stroke(width = 20f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -215f,
                    sweepAngle = 250f * progress,
                    useCenter = false,
                    style = Stroke(width = 20f, cap = StrokeCap.Round)
                )
            }

            Text(text = "${(progress * 100).roundToInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CustomBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier
) {
    val maxAmount = data.maxOfOrNull { it.amount } ?: 1f
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxWidth()) {
        val barWidth = size.width / (data.size * 2)
        val spaceBetween = barWidth

        data.forEachIndexed { index, item ->
            val barHeight = (item.amount / maxAmount) * size.height
            val left = (index * (barWidth + spaceBetween)) + spaceBetween / 2
            drawRect(
                color = barColor,
                topLeft = Offset(x = left, y = size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}