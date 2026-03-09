package com.group3.financialapplication.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.group3.financialapplication.MainActivity
import com.group3.financialapplication.data.DatabaseProvider
import com.group3.financialapplication.data.SettingsManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar

class BudgetWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch latest data every time the widget is asked to render
        val data = fetchWidgetData(context)
        provideContent {
            WidgetContent(data)
        }
    }
}

private data class WidgetData(
    val currency: String,
    val monthlyLimit: Double,
    val totalSpent: Double
)

private suspend fun fetchWidgetData(context: Context): WidgetData {
    val db = DatabaseProvider.getDatabase(context)
    val settingsManager = SettingsManager(context)
    val settings = settingsManager.settingsFlow.first()

    val cal = Calendar.getInstance()
    val start = (cal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val end = (cal.clone() as Calendar).apply {
        add(Calendar.MONTH, 1); set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.DAY_OF_MONTH, -1)
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
    }.timeInMillis

    val transactions = db.transactionDao().getTransactionsForMonth(start, end).first()
    val totalSpent = transactions.filter { it.isExpense }.sumOf { it.amount }

    return WidgetData(
        currency = settings.currency,
        monthlyLimit = settings.monthlyLimit,
        totalSpent = totalSpent
    )
}

@Composable
private fun WidgetContent(data: WidgetData) {
    val remaining = data.monthlyLimit - data.totalSpent
    val progress = if (data.monthlyLimit > 0)
        (data.totalSpent / data.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()

    val isOver = remaining < 0
    val fillColor = when {
        progress < 0.5f -> Color(0xFF2E7D32)
        progress < 0.8f -> Color(0xFFE65100)
        else -> Color(0xFFD32F2F)
    }
    val bgColor = if (isOver) Color(0xFFFFCDD2) else Color(0xFFF1F8E9)
    val trackColor = Color(0xFFE0E0E0)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Monthly Budget",
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
        )

        Spacer(GlanceModifier.height(6.dp))

        Text(
            text = if (isOver) "-${data.currency}${"%.2f".format(-remaining)}"
            else "${data.currency}${"%.2f".format(remaining)}",
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
        )

        Text(
            text = if (isOver) "OVER LIMIT" else "remaining",
            style = TextStyle(fontSize = 10.sp)
        )

        Spacer(GlanceModifier.height(8.dp))

        // Progress bar
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(10.dp)
                .background(trackColor),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                if (progressPercent > 0) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .background(fillColor),
                        contentAlignment = Alignment.Center
                    ) {}
                }
                if (progressPercent < 100) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .background(trackColor),
                        contentAlignment = Alignment.Center
                    ) {}
                }
            }
        }

        Spacer(GlanceModifier.height(4.dp))

        Text(
            text = "${data.currency}${"%.2f".format(data.totalSpent)} / ${data.currency}${"%.2f".format(data.monthlyLimit)}  •  $progressPercent%",
            style = TextStyle(fontSize = 10.sp)
        )
    }
}

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}