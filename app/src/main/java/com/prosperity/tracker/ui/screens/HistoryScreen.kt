package com.prosperity.tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prosperity.tracker.data.TransactionType
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Tertiary
import com.prosperity.tracker.viewmodel.FinanceViewModel

@Composable
fun HistoryScreen(vm: FinanceViewModel) {
    val transactions by vm.transactions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("Riwayat") }

        if (transactions.isEmpty()) {
            item { Text("Belum ada transaksi.", color = OnSurfaceVariant, fontSize = 14.sp) }
        } else {
            items(transactions, key = { it.transaction.id }) { item ->
                TransactionRow(
                    emoji = item.categoryEmoji ?: "\uD83D\uDCB8",
                    title = item.categoryName ?: "?",
                    subtitle = listOfNotNull(item.accountName, item.transaction.note.ifBlank { null }).joinToString(" \u00B7 "),
                    amount = item.transaction.amount,
                    isIncome = item.transaction.type == TransactionType.INCOME,
                    trailing = {
                        IconButton(onClick = { vm.deleteTransaction(item.transaction) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus", tint = Tertiary)
                        }
                    }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
