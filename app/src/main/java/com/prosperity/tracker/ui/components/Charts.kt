package com.prosperity.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prosperity.tracker.ui.theme.IncomeGreen
import com.prosperity.tracker.ui.theme.OnSurface
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Primary

/** One slice of the donut chart. */
data class DonutSlice(val label: String, val value: Long, val color: Color)

/**
 * Donut chart with a centered caption. Slices are proportional to value; a
 * neutral full ring is drawn when every value is zero.
 *
 * Sized at 220dp with a 36dp stroke so the inner hole comfortably fits a full
 * Rupiah string (e.g. "Rp 15.452.253") on one line without truncation.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerTitle: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }
    Box(modifier = modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 36.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            if (total <= 0L) {
                drawArc(
                    color = Color(0xFFDCE9FF),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                return@Canvas
            }

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value.toFloat() / total) * 360f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = (sweep - 2f).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(centerTitle, color = OnSurfaceVariant, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                centerValue,
                color = Primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/** A legend row: colored dot, label, and right-aligned trailing text. */
@Composable
fun LegendRow(color: Color, label: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Text(label, color = OnSurface, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(trailing, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** One month's pair of income/expense values for the bar chart. */
data class BarGroup(val label: String, val income: Long, val expense: Long)

/**
 * Grouped vertical bars: income (green) and expense (primary) per month,
 * scaled to the largest value across all groups.
 */
@Composable
fun IncomeExpenseBarChart(
    groups: List<BarGroup>,
    modifier: Modifier = Modifier
) {
    val maxValue = (groups.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 0L).coerceAtLeast(1L)
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 8.dp)) {
            if (groups.isEmpty()) return@Canvas
            val groupWidth = size.width / groups.size
            val barWidth = groupWidth * 0.26f
            val gap = groupWidth * 0.10f
            val baseline = size.height

            groups.forEachIndexed { index, group ->
                val groupCenter = groupWidth * index + groupWidth / 2
                val incomeHeight = (group.income.toFloat() / maxValue) * baseline
                val expenseHeight = (group.expense.toFloat() / maxValue) * baseline
                drawRoundedBar(groupCenter - (barWidth / 2 + gap / 2), barWidth, incomeHeight, baseline, IncomeGreen)
                drawRoundedBar(groupCenter + (barWidth / 2 + gap / 2), barWidth, expenseHeight, baseline, Primary)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            groups.forEach { Text(it.label, color = OnSurfaceVariant, fontSize = 12.sp) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            LegendDot(IncomeGreen, "Pemasukan")
            Spacer(Modifier.width(20.dp))
            LegendDot(Primary, "Pengeluaran")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
    }
}

private fun DrawScope.drawRoundedBar(
    centerX: Float,
    barWidth: Float,
    barHeight: Float,
    baseline: Float,
    color: Color
) {
    val left = centerX - barWidth / 2
    val top = baseline - barHeight
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(barWidth, barHeight.coerceAtLeast(2f)),
        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
    )
}
