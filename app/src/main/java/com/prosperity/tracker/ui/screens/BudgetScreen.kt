package com.prosperity.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prosperity.tracker.data.AccountEntity
import com.prosperity.tracker.data.CategoryEntity
import com.prosperity.tracker.data.TransactionType
import com.prosperity.tracker.ui.theme.ChartPalette
import com.prosperity.tracker.ui.theme.OnSurface
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Primary
import com.prosperity.tracker.ui.theme.PrimaryDark
import com.prosperity.tracker.ui.theme.Tertiary
import com.prosperity.tracker.util.formatRupiah
import com.prosperity.tracker.util.monthLabel
import com.prosperity.tracker.viewmodel.FinanceViewModel

@Composable
fun BudgetScreen(vm: FinanceViewModel) {
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val budgets by vm.budgetsForSelectedMonth.collectAsStateWithLifecycle()
    val month by vm.selectedMonth.collectAsStateWithLifecycle()

    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
    val budgetByCategory = budgets.associate { it.categoryId to it.amount }
    val totalAssets = accounts.sumOf { it.balance }
    val totalAllocated = budgetByCategory.values.sum()
    val unallocated = totalAssets - totalAllocated

    // Dialog state
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var addingAccount by remember { mutableStateOf(false) }
    var allocCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Anggaran") }
        item { Text(monthLabel(month), color = OnSurfaceVariant, fontSize = 15.sp) }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryDark)))
                    .padding(24.dp)
            ) {
                Column {
                    Text("TOTAL ASET", color = Color(0xCCFFFFFF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(formatRupiah(totalAssets), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Dialokasikan", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                            Text(formatRupiah(totalAllocated), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Belum dialokasikan", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                            Text(
                                formatRupiah(unallocated),
                                color = if (unallocated < 0) Color(0xFFFFB3B6) else Color(0xFF85F8C4),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ---- Accounts ----
        item { SectionHeader("Daftar Akun") }
        items(accounts, key = { "acc-${it.id}" }) { account ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) { Text(account.emoji, fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(account.name, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    if (account.subtitle.isNotBlank()) {
                        Text(account.subtitle, color = OnSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Text(
                    formatRupiah(account.balance),
                    color = if (account.balance < 0) Tertiary else OnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { editingAccount = account }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { vm.deleteAccount(account) }) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus", tint = Tertiary, modifier = Modifier.size(20.dp))
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.06f))
                    .clickable { addingAccount = true }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+ Tambah Akun", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ---- Budget allocation (simple list) ----
        item { SectionHeader("Pembagian Anggaran") }
        item {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(8.dp)) {
                Column {
                    expenseCategories.forEachIndexed { index, category ->
                        val amount = budgetByCategory[category.id] ?: 0L
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { allocCategory = category }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ChartPalette[index % ChartPalette.size]))
                            Spacer(Modifier.width(12.dp))
                            Text("${category.emoji} ${category.name}", color = OnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text(formatRupiah(amount), color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Primary.copy(alpha = 0.10f))
                                    .clickable { allocCategory = category }.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) { Text("Atur", color = Primary, fontSize = 12.sp) }
                        }
                        if (index != expenseCategories.lastIndex) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFF1F5)))
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    // ---- Dialogs ----
    if (addingAccount) {
        AccountDialog(
            existing = null,
            onDismiss = { addingAccount = false },
            onConfirm = { name, sub, emoji, balance ->
                vm.addAccount(name, sub, emoji, balance)
                addingAccount = false
            }
        )
    }
    val editTarget = editingAccount
    if (editTarget != null) {
        AccountDialog(
            existing = editTarget,
            onDismiss = { editingAccount = null },
            onConfirm = { name, sub, emoji, balance ->
                vm.updateAccount(editTarget.copy(name = name, subtitle = sub, emoji = emoji, balance = balance))
                editingAccount = null
            }
        )
    }
    val allocTarget = allocCategory
    if (allocTarget != null) {
        AllocationDialog(
            category = allocTarget,
            current = budgetByCategory[allocTarget.id] ?: 0L,
            onDismiss = { allocCategory = null },
            onConfirm = { amount ->
                vm.setBudget(allocTarget.id, month, amount)
                allocCategory = null
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun AccountDialog(
    existing: AccountEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var sub by remember { mutableStateOf(existing?.subtitle ?: "") }
    var emoji by remember { mutableStateOf(existing?.emoji ?: "\uD83D\uDCB3") }
    var balanceText by remember { mutableStateOf(if (existing != null) existing.balance.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Tambah Akun" else "Edit Akun") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama akun") }, singleLine = true)
                OutlinedTextField(sub, { sub = it }, label = { Text("Keterangan") }, singleLine = true)
                OutlinedTextField(emoji, { emoji = it.take(2) }, label = { Text("Emoji") }, singleLine = true)
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        // Allow an optional leading minus, digits only after.
                        val neg = input.startsWith("-")
                        val digits = input.filter { it.isDigit() }
                        balanceText = if (neg) "-$digits" else digits
                    },
                    label = { Text("Saldo (Rp)") },
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalName = name.trim().ifBlank { "Akun" }
                    val finalEmoji = emoji.trim().ifBlank { "\uD83D\uDCB3" }
                    val balance = balanceText.toLongOrNull() ?: 0L
                    onConfirm(finalName, sub.trim(), finalEmoji, balance)
                }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AllocationDialog(
    category: CategoryEntity,
    current: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var text by remember { mutableStateOf(if (current > 0) current.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anggaran ${category.name}") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { ch -> ch.isDigit() } },
                prefix = { Text("Rp ") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text.toLongOrNull() ?: 0L) }) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
