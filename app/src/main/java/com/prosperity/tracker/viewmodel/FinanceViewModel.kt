package com.prosperity.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prosperity.tracker.data.AccountEntity
import com.prosperity.tracker.data.BudgetEntity
import com.prosperity.tracker.data.CategoryEntity
import com.prosperity.tracker.data.FinanceRepository
import com.prosperity.tracker.data.TransactionEntity
import com.prosperity.tracker.data.TransactionType
import com.prosperity.tracker.data.TransactionWithDetails
import com.prosperity.tracker.util.currentMonthKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModel(private val repo: FinanceRepository) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> =
        repo.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> =
        repo.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val transactions: StateFlow<List<TransactionWithDetails>> =
        repo.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedMonth = MutableStateFlow(currentMonthKey())
    val selectedMonth: StateFlow<Int> = _selectedMonth

    val budgetsForSelectedMonth: StateFlow<List<BudgetEntity>> =
        _selectedMonth
            .flatMapLatest { month -> repo.budgetsForMonth(month) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setMonth(monthKey: Int) { _selectedMonth.value = monthKey }

    // ---- Transactions ----
    fun addTransaction(amount: Long, type: TransactionType, categoryId: Long, accountId: Long, note: String) {
        viewModelScope.launch { repo.addTransaction(amount, type, categoryId, accountId, note) }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repo.deleteTransaction(transaction) }
    }

    // ---- Categories ----
    fun addCategory(name: String, type: TransactionType, emoji: String) {
        viewModelScope.launch { repo.addCategory(name, type, emoji) }
    }

    // ---- Accounts ----
    fun addAccount(name: String, subtitle: String, emoji: String, balance: Long) {
        viewModelScope.launch { repo.addAccount(name, subtitle, emoji, balance) }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch { repo.updateAccount(account) }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch { repo.deleteAccount(account) }
    }

    // ---- Budgets ----
    fun setBudget(categoryId: Long, month: Int, amount: Long) {
        viewModelScope.launch { repo.setBudget(categoryId, month, amount) }
    }
}

/** Manual factory so the ViewModel receives the repository without a DI framework. */
class FinanceViewModelFactory(private val repo: FinanceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            return FinanceViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
