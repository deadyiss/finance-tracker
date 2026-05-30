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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prosperity.tracker.data.AccountEntity
import com.prosperity.tracker.data.CategoryEntity
import com.prosperity.tracker.data.TransactionType
import com.prosperity.tracker.ui.theme.OnSurface
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Primary
import com.prosperity.tracker.ui.theme.SurfaceContainer
import com.prosperity.tracker.viewmodel.FinanceViewModel

@Composable
fun AddScreen(vm: FinanceViewModel, onSaved: () -> Unit) {
    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()

    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val visibleCategories = categories.filter { it.type == type }
    if (selectedCategoryId != null && visibleCategories.none { it.id == selectedCategoryId }) {
        selectedCategoryId = null
    }
    if (selectedAccountId != null && accounts.none { it.id == selectedAccountId }) {
        selectedAccountId = null
    }

    val amount = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val canSave = amount > 0L && selectedCategoryId != null && selectedAccountId != null

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenTitle("Tambah Transaksi") }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceContainer).padding(4.dp)
            ) {
                TypeTab("Pengeluaran", type == TransactionType.EXPENSE, Modifier.weight(1f)) { type = TransactionType.EXPENSE }
                TypeTab("Pemasukan", type == TransactionType.INCOME, Modifier.weight(1f)) { type = TransactionType.INCOME }
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("JUMLAH", color = OnSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    placeholder = { Text("0") },
                    prefix = { Text("Rp ", color = Primary, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Primary,
                        unfocusedIndicatorColor = SurfaceContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { Text("Kategori", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        item {
            CategoryGrid(
                categories = visibleCategories,
                selectedId = selectedCategoryId,
                onSelect = { selectedCategoryId = it },
                onAddCustom = { showCategoryDialog = true }
            )
        }

        item {
            Text(
                if (type == TransactionType.EXPENSE) "Bayar dari" else "Simpan ke",
                color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
            )
        }
        item {
            AccountChips(
                accounts = accounts,
                selectedId = selectedAccountId,
                onSelect = { selectedAccountId = it }
            )
        }

        item { Text("Catatan (Opsional)", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        item {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Untuk apa transaksi ini?") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedIndicatorColor = Primary,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(96.dp)
            )
        }

        item {
            Button(
                onClick = {
                    val catId = selectedCategoryId
                    val accId = selectedAccountId
                    if (canSave && catId != null && accId != null) {
                        vm.addTransaction(amount, type, catId, accId, note.trim())
                        amountText = ""; note = ""; selectedCategoryId = null; selectedAccountId = null
                        onSaved()
                    }
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Simpan Transaksi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showCategoryDialog) {
        AddCategoryDialog(
            type = type,
            onDismiss = { showCategoryDialog = false },
            onConfirm = { name, emoji ->
                vm.addCategory(name, type, emoji)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
private fun TypeTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) OnSurface else OnSurfaceVariant,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onAddCustom: () -> Unit
) {
    // Build a grid of category chips plus a trailing "Tambah" tile, 4 per row.
    val tiles = categories.size + 1
    val rows = (tiles + 3) / 4
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until 4) {
                    val index = r * 4 + c
                    when {
                        index < categories.size -> {
                            val category = categories[index]
                            CategoryChip(category, category.id == selectedId, Modifier.weight(1f)) { onSelect(category.id) }
                        }
                        index == categories.size -> AddTile(Modifier.weight(1f), onAddCustom)
                        else -> Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: CategoryEntity, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Primary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(if (selected) Primary else SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(category.emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            category.name,
            color = if (selected) Primary else OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun AddTile(modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }.padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = OnSurfaceVariant, fontSize = 26.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text("Tambah", color = OnSurfaceVariant, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun AccountChips(accounts: List<AccountEntity>, selectedId: Long?, onSelect: (Long) -> Unit) {
    // Simple wrapping row of selectable account chips.
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        accounts.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { account ->
                    val selected = account.id == selectedId
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) Color.White else SurfaceContainer)
                            .clickable { onSelect(account.id) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(account.emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            account.name,
                            color = if (selected) Primary else OnSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
                repeat(2 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    val typeLabel = if (type == TransactionType.EXPENSE) "Pengeluaran" else "Pemasukan"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategori $typeLabel Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama kategori") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it.take(2) },
                    label = { Text("Emoji (cth: \uD83C\uDF7D\uFE0F)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalName = name.trim()
                    val finalEmoji = emoji.trim().ifBlank { "\uD83D\uDCB8" }
                    if (finalName.isNotEmpty()) onConfirm(finalName, finalEmoji)
                },
                enabled = name.trim().isNotEmpty()
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
