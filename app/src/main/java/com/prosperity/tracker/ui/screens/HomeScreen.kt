package com.prosperity.tracker.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prosperity.tracker.data.TransactionType
import com.prosperity.tracker.ui.components.DonutChart
import com.prosperity.tracker.ui.components.DonutSlice
import com.prosperity.tracker.ui.components.LegendRow
import com.prosperity.tracker.ui.theme.ChartPalette
import com.prosperity.tracker.ui.theme.IncomeGreen
import com.prosperity.tracker.ui.theme.OnSurface
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Primary
import com.prosperity.tracker.ui.theme.PrimaryDark
import com.prosperity.tracker.ui.theme.Tertiary
import com.prosperity.tracker.util.currentMonthKey
import com.prosperity.tracker.util.formatRupiah
import com.prosperity.tracker.viewmodel.FinanceViewModel
import com.prosperity.tracker.viewmodel.expenseByCategory
import com.prosperity.tracker.viewmodel.forMonth
import com.prosperity.tracker.viewmodel.totalExpense
import com.prosperity.tracker.viewmodel.totalIncome

@Composable
fun HomeScreen(vm: FinanceViewModel) {
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val month = currentMonthKey()
    val monthTx = transactions.forMonth(month)

    val totalAssets = accounts.sumOf { it.balance }
    val monthIncome = monthTx.totalIncome()
    val monthExpense = monthTx.totalExpense()

    val byCategory = monthTx.expenseByCategory()
    val totalExpenseMonth = byCategory.sumOf { it.amount }
    val slices = byCategory.mapIndexed { index, ca ->
        DonutSlice(ca.name, ca.amount, ChartPalette[index % ChartPalette.size])
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(2.dp)) }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryDark)))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("TOTAL SALDO", color = Color(0xCCFFFFFF), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text(formatRupiah(totalAssets), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard(Modifier.weight(1f), "Pemasukan", formatRupiah(monthIncome), IncomeGreen, "\u2193")
                SummaryCard(Modifier.weight(1f), "Pengeluaran", formatRupiah(monthExpense), Tertiary, "\u2191")
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Column {
                    Text("Ringkasan Pengeluaran", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        DonutChart(slices = slices, centerTitle = "Terpakai", centerValue = formatRupiah(totalExpenseMonth))
                    }
                    Spacer(Modifier.height(16.dp))
                    if (slices.isEmpty()) {
                        Text("Belum ada pengeluaran bulan ini.", color = OnSurfaceVariant, fontSize = 14.sp)
                    } else {
                        slices.forEach { slice ->
                            val percent = if (totalExpenseMonth > 0) (slice.value * 100 / totalExpenseMonth) else 0
                            LegendRow(slice.color, slice.label, "$percent%")
                        }
                    }
                }
            }
        }

        item { Text("Transaksi Terbaru", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        if (transactions.isEmpty()) {
            item { Text("Belum ada transaksi.", color = OnSurfaceVariant, fontSize = 14.sp) }
        } else {
            items(transactions.take(5), key = { it.transaction.id }) { item ->
                TransactionRow(
                    emoji = item.categoryEmoji ?: "\uD83D\uDCB8",
                    title = item.categoryName ?: "?",
                    subtitle = listOfNotNull(item.accountName, item.transaction.note.ifBlank { null }).joinToString(" \u00B7 "),
                    amount = item.transaction.amount,
                    isIncome = item.transaction.type == TransactionType.INCOME
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, label: String, value: String, accent: Color, glyph: String) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(glyph, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(label, color = OnSurfaceVariant, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = OnSurface, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}
