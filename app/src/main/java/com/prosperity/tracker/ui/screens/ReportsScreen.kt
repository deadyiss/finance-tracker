package com.prosperity.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prosperity.tracker.data.TransactionType
import com.prosperity.tracker.data.TransactionWithDetails
import com.prosperity.tracker.ui.components.DonutChart
import com.prosperity.tracker.ui.components.DonutSlice
import com.prosperity.tracker.ui.components.LegendRow
import com.prosperity.tracker.ui.theme.ChartPalette
import com.prosperity.tracker.ui.theme.IncomeGreen
import com.prosperity.tracker.ui.theme.OnSurface
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Primary
import com.prosperity.tracker.ui.theme.SurfaceContainer
import com.prosperity.tracker.ui.theme.Tertiary
import com.prosperity.tracker.util.currentYear
import com.prosperity.tracker.util.currentMonthKey
import com.prosperity.tracker.util.dayKeyOf
import com.prosperity.tracker.util.dayKeyToLocalDate
import com.prosperity.tracker.util.dayLabel
import com.prosperity.tracker.util.daysInMonth
import com.prosperity.tracker.util.firstWeekdayOffsetMondayFirst
import com.prosperity.tracker.util.formatRupiah
import com.prosperity.tracker.util.formatRupiahShort
import com.prosperity.tracker.util.monthKeyFromYearMonth
import com.prosperity.tracker.util.monthLabel
import com.prosperity.tracker.util.monthLabelShort
import com.prosperity.tracker.util.todayDayKey
import com.prosperity.tracker.util.weekdayHeadersIdShort
import com.prosperity.tracker.viewmodel.FinanceViewModel
import com.prosperity.tracker.viewmodel.byCategory
import com.prosperity.tracker.viewmodel.forDay
import com.prosperity.tracker.viewmodel.forMonth
import com.prosperity.tracker.viewmodel.forYear
import com.prosperity.tracker.viewmodel.totalExpense
import com.prosperity.tracker.viewmodel.totalIncome
import java.time.LocalDate

/** What time window the report is showing. */
private enum class Granularity { YEAR, MONTH, DAY }

@Composable
fun ReportsScreen(vm: FinanceViewModel) {
    val transactions by vm.transactions.collectAsStateWithLifecycle()

    // ---- State: granularity + the three independent cursors ----
    var granularity by remember { mutableStateOf(Granularity.MONTH) }
    var selectedYear by remember { mutableStateOf(currentYear()) }
    var selectedMonthKey by remember { mutableStateOf(currentMonthKey()) }
    var selectedDayKey by remember { mutableStateOf(todayDayKey()) }
    var typeFilter by remember { mutableStateOf(TransactionType.EXPENSE) }

    // ---- Derived: the transactions visible for the selected period ----
    val periodTx: List<TransactionWithDetails> = when (granularity) {
        Granularity.YEAR -> transactions.forYear(selectedYear)
        Granularity.MONTH -> transactions.forMonth(selectedMonthKey)
        Granularity.DAY -> transactions.forDay(selectedDayKey)
    }
    val income = periodTx.totalIncome()
    val expense = periodTx.totalExpense()
    val net = income - expense

    // ---- Derived: category breakdown for the toggled type ----
    val byCategory = periodTx.byCategory(typeFilter)
    val totalByType = byCategory.sumOf { it.amount }
    val slices = byCategory.mapIndexed { index, ca ->
        DonutSlice(ca.name, ca.amount, ChartPalette[index % ChartPalette.size])
    }

    // ---- Period label for the header ----
    val periodLabel = when (granularity) {
        Granularity.YEAR -> selectedYear.toString()
        Granularity.MONTH -> monthLabel(selectedMonthKey)
        Granularity.DAY -> dayLabel(selectedDayKey)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Laporan") }

        // ---- Granularity selector ----
        item {
            GranularitySelector(
                current = granularity,
                onSelect = { granularity = it }
            )
        }

        // ---- Period stepper (prev / label / next) ----
        item {
            PeriodStepper(
                label = periodLabel,
                onPrev = {
                    when (granularity) {
                        Granularity.YEAR -> selectedYear -= 1
                        Granularity.MONTH -> selectedMonthKey = shiftMonth(selectedMonthKey, -1)
                        Granularity.DAY -> selectedDayKey =
                            dayKeyOf(dayKeyToLocalDate(selectedDayKey).minusDays(1))
                    }
                },
                onNext = {
                    when (granularity) {
                        Granularity.YEAR -> selectedYear += 1
                        Granularity.MONTH -> selectedMonthKey = shiftMonth(selectedMonthKey, 1)
                        Granularity.DAY -> selectedDayKey =
                            dayKeyOf(dayKeyToLocalDate(selectedDayKey).plusDays(1))
                    }
                }
            )
        }

        // ---- Stats card: net + income + expense (both mini-stats, as requested) ----
        item {
            StatsCard(
                net = net,
                income = income,
                expense = expense
            )
        }

        // ---- Type toggle controls donut + calendar + categories list ----
        item {
            TypeToggle(
                current = typeFilter,
                onSelect = { typeFilter = it }
            )
        }

        // ---- Donut chart for the chosen type ----
        item {
            DonutCard(
                slices = slices,
                totalAmount = totalByType,
                typeFilter = typeFilter
            )
        }

        // ---- Calendar ----
        item {
            CalendarCard(
                granularity = granularity,
                selectedYear = selectedYear,
                selectedMonthKey = selectedMonthKey,
                selectedDayKey = selectedDayKey,
                transactions = transactions,
                typeFilter = typeFilter,
                onMonthTapped = { monthKey ->
                    // Drill from Year view -> Month view.
                    selectedMonthKey = monthKey
                    granularity = Granularity.MONTH
                },
                onDayTapped = { dayKey ->
                    // Drill from Month/Day view -> Day view.
                    selectedDayKey = dayKey
                    granularity = Granularity.DAY
                }
            )
        }

        // ---- All categories with non-zero amount ----
        item { CategoryList(byCategory, totalByType) }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

/** Adds [delta] months (positive or negative) to a yyyymm key. */
private fun shiftMonth(monthKey: Int, delta: Int): Int {
    val totalMonths = (monthKey / 100) * 12 + (monthKey % 100) - 1 + delta
    val year = totalMonths / 12
    val month = totalMonths % 12 + 1
    return year * 100 + month
}

// ---- Sub-components ----

@Composable
private fun GranularitySelector(current: Granularity, onSelect: (Granularity) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceContainer).padding(4.dp)
    ) {
        listOf(
            Granularity.YEAR to "Tahun",
            Granularity.MONTH to "Bulan",
            Granularity.DAY to "Hari"
        ).forEach { (g, label) ->
            val selected = current == g
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable { onSelect(g) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) OnSurface else OnSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun PeriodStepper(label: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Sebelumnya", tint = Primary)
        }
        Text(
            label,
            color = OnSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Selanjutnya", tint = Primary)
        }
    }
}

@Composable
private fun StatsCard(net: Long, income: Long, expense: Long) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp)
    ) {
        Column {
            Text("SALDO BERSIH", color = OnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(formatRupiah(net), color = OnSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStat("Pemasukan", formatRupiah(income), IncomeGreen, Modifier.weight(1f))
                MiniStat("Pengeluaran", formatRupiah(expense), Primary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, accent: Color, modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceContainer.copy(alpha = 0.5f)).padding(12.dp)) {
        Column {
            Text(label, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TypeToggle(current: TransactionType, onSelect: (TransactionType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceContainer).padding(4.dp)
    ) {
        listOf(
            TransactionType.EXPENSE to "Pengeluaran",
            TransactionType.INCOME to "Pemasukan"
        ).forEach { (t, label) ->
            val selected = current == t
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable { onSelect(t) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected) OnSurface else OnSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DonutCard(slices: List<DonutSlice>, totalAmount: Long, typeFilter: TransactionType) {
    val title = if (typeFilter == TransactionType.EXPENSE) "Distribusi Pengeluaran" else "Distribusi Pemasukan"
    val centerLabel = if (typeFilter == TransactionType.EXPENSE) "Total" else "Total"
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp)
    ) {
        Column {
            Text(title, color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutChart(slices = slices, centerTitle = centerLabel, centerValue = formatRupiah(totalAmount))
            }
            Spacer(Modifier.height(12.dp))
            if (slices.isEmpty()) {
                Text(
                    if (typeFilter == TransactionType.EXPENSE) "Tidak ada pengeluaran di periode ini."
                    else "Tidak ada pemasukan di periode ini.",
                    color = OnSurfaceVariant,
                    fontSize = 14.sp
                )
            } else {
                slices.forEach { slice ->
                    val percent = if (totalAmount > 0) (slice.value * 100 / totalAmount) else 0L
                    LegendRow(slice.color, slice.label, "$percent%")
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(
    granularity: Granularity,
    selectedYear: Int,
    selectedMonthKey: Int,
    selectedDayKey: Int,
    transactions: List<TransactionWithDetails>,
    typeFilter: TransactionType,
    onMonthTapped: (Int) -> Unit,
    onDayTapped: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp)) {
        Column {
            val accent = if (typeFilter == TransactionType.EXPENSE) Tertiary else IncomeGreen
            val headerLabel = when (granularity) {
                Granularity.YEAR -> "Per Bulan di $selectedYear"
                Granularity.MONTH, Granularity.DAY -> "Kalender"
            }
            Text(headerLabel, color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            when (granularity) {
                Granularity.YEAR -> YearMonthGrid(
                    year = selectedYear,
                    transactions = transactions,
                    typeFilter = typeFilter,
                    accent = accent,
                    onMonthTapped = onMonthTapped
                )
                Granularity.MONTH -> MonthDayGrid(
                    monthKey = selectedMonthKey,
                    selectedDayKey = null,
                    transactions = transactions,
                    typeFilter = typeFilter,
                    accent = accent,
                    onDayTapped = onDayTapped
                )
                Granularity.DAY -> {
                    val monthOfSelected = (selectedDayKey / 10000) * 100 + ((selectedDayKey / 100) % 100)
                    MonthDayGrid(
                        monthKey = monthOfSelected,
                        selectedDayKey = selectedDayKey,
                        transactions = transactions,
                        typeFilter = typeFilter,
                        accent = accent,
                        onDayTapped = onDayTapped
                    )
                }
            }
        }
    }
}

@Composable
private fun YearMonthGrid(
    year: Int,
    transactions: List<TransactionWithDetails>,
    typeFilter: TransactionType,
    accent: Color,
    onMonthTapped: (Int) -> Unit
) {
    // Compute per-month totals once.
    val monthTotals: Map<Int, Long> = (1..12).associate { m ->
        val key = monthKeyFromYearMonth(year, m)
        val total = transactions.forMonth(key)
            .filter { it.transaction.type == typeFilter }
            .sumOf { it.transaction.amount }
        key to total
    }
    // 4 rows x 3 cols.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 3) {
                    val month = row * 3 + col + 1
                    val key = monthKeyFromYearMonth(year, month)
                    val total = monthTotals[key] ?: 0L
                    MonthTile(
                        label = monthLabelShort(key),
                        total = total,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                        onClick = { onMonthTapped(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthTile(
    label: String,
    total: Long,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (total > 0) accent.copy(alpha = 0.10f) else SurfaceContainer.copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (total > 0) formatRupiahShort(total) else "\u2014",
                color = if (total > 0) accent else OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MonthDayGrid(
    monthKey: Int,
    selectedDayKey: Int?,
    transactions: List<TransactionWithDetails>,
    typeFilter: TransactionType,
    accent: Color,
    onDayTapped: (Int) -> Unit
) {
    val offset = firstWeekdayOffsetMondayFirst(monthKey)
    val daysCount = daysInMonth(monthKey)
    val year = monthKey / 100
    val month = monthKey % 100
    val today = LocalDate.now()

    // Per-day totals for this month, filtered by type.
    val dayTotals: Map<Int, Long> = transactions
        .filter { it.transaction.type == typeFilter }
        .filter {
            val d = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(it.transaction.timestamp),
                java.time.ZoneId.systemDefault()
            )
            d.year == year && d.monthValue == month
        }
        .groupBy {
            LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(it.transaction.timestamp),
                java.time.ZoneId.systemDefault()
            ).dayOfMonth
        }
        .mapValues { (_, items) -> items.sumOf { it.transaction.amount } }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayHeadersIdShort.forEach { header ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(header, color = OnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        // Calendar cells: 6 rows x 7 columns is enough for any month.
        val totalCells = offset + daysCount
        val rows = (totalCells + 6) / 7
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - offset + 1
                    if (dayNum in 1..daysCount) {
                        val date = LocalDate.of(year, month, dayNum)
                        val dayKey = dayKeyOf(date)
                        val total = dayTotals[dayNum] ?: 0L
                        val isSelected = selectedDayKey == dayKey
                        val isToday = date == today
                        DayCell(
                            day = dayNum,
                            total = total,
                            accent = accent,
                            selected = isSelected,
                            isToday = isToday,
                            modifier = Modifier.weight(1f),
                            onClick = { onDayTapped(dayKey) }
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(0.85f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    total: Long,
    accent: Color,
    selected: Boolean,
    isToday: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bg = when {
        selected -> Primary
        total > 0 -> accent.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    val dayColor = when {
        selected -> Color.White
        isToday -> Primary
        else -> OnSurface
    }
    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                color = dayColor,
                fontSize = 13.sp,
                fontWeight = if (isToday || selected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            if (total > 0) {
                Text(
                    formatRupiahShort(total).removePrefix("Rp "),
                    color = if (selected) Color.White else accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CategoryList(items: List<com.prosperity.tracker.viewmodel.CategoryAmount>, total: Long) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp)
    ) {
        Column {
            Text("Kategori Teratas", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (items.isEmpty()) {
                Text("Tidak ada data di periode ini.", color = OnSurfaceVariant, fontSize = 14.sp)
            } else {
                items.forEachIndexed { index, ca ->
                    val percent = if (total > 0) (ca.amount * 100 / total).toInt() else 0
                    TopCategoryRow(ca.emoji, ca.name, ca.amount, percent, ChartPalette[index % ChartPalette.size])
                    if (index != items.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TopCategoryRow(emoji: String, name: String, amount: Long, percent: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 18.sp) }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("${formatRupiah(amount)} ($percent%)", color = OnSurfaceVariant, fontSize = 13.sp)
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(SurfaceContainer)) {
                Box(
                    modifier = Modifier.fillMaxWidth((percent / 100f).coerceIn(0f, 1f)).height(6.dp)
                        .clip(RoundedCornerShape(3.dp)).background(color)
                )
            }
        }
    }
}
