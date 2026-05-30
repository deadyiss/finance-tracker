package com.prosperity.tracker.data

import kotlinx.coroutines.flow.Flow

/**
 * Single access point to the data layer. Reads are exposed as Flows so the UI
 * updates automatically. Writes that touch account balances adjust them here.
 */
class FinanceRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val accounts: Flow<List<AccountEntity>> = accountDao.getAll()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAll()
    val transactions: Flow<List<TransactionWithDetails>> = transactionDao.getAllWithDetails()

    fun budgetsForMonth(month: Int): Flow<List<BudgetEntity>> = budgetDao.getForMonth(month)

    // ---- Accounts ----
    suspend fun addAccount(name: String, subtitle: String, emoji: String, balance: Long) =
        accountDao.insert(AccountEntity(name = name, subtitle = subtitle, emoji = emoji, balance = balance))

    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.delete(account)

    // ---- Categories ----
    suspend fun addCategory(name: String, type: TransactionType, emoji: String) =
        categoryDao.insert(CategoryEntity(name = name, type = type, emoji = emoji))

    // ---- Transactions (also adjust the linked account balance) ----
    suspend fun addTransaction(amount: Long, type: TransactionType, categoryId: Long, accountId: Long, note: String) {
        transactionDao.insert(
            TransactionEntity(
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = accountId,
                note = note,
                timestamp = System.currentTimeMillis()
            )
        )
        adjustBalance(accountId, if (type == TransactionType.INCOME) amount else -amount)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
        // Reverse the balance effect this transaction had.
        val reverse = if (transaction.type == TransactionType.INCOME) -transaction.amount else transaction.amount
        adjustBalance(transaction.accountId, reverse)
    }

    private suspend fun adjustBalance(accountId: Long, delta: Long) {
        val account = accountDao.getById(accountId) ?: return
        accountDao.update(account.copy(balance = account.balance + delta))
    }

    // ---- Budgets ----
    suspend fun setBudget(categoryId: Long, month: Int, amount: Long) =
        budgetDao.upsert(BudgetEntity(categoryId = categoryId, month = month, amount = amount))

    // ---- Seeding ----
    suspend fun seedDefaultsIfEmpty() {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(defaultCategories())
        }
        if (accountDao.count() == 0) {
            accountDao.insertAll(defaultAccounts())
        }
    }

    private fun defaultAccounts(): List<AccountEntity> = listOf(
        AccountEntity(name = "Tunai", subtitle = "Dompet Fisik", emoji = "\uD83D\uDCB5", balance = 0),
        AccountEntity(name = "Bank", subtitle = "Rekening", emoji = "\uD83C\uDFE6", balance = 0),
        AccountEntity(name = "E-Wallet", subtitle = "Dompet Digital", emoji = "\uD83D\uDCF1", balance = 0)
    )

    private fun defaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Makan", type = TransactionType.EXPENSE, emoji = "\uD83C\uDF7D\uFE0F"),
        CategoryEntity(name = "Transportasi", type = TransactionType.EXPENSE, emoji = "\uD83D\uDE97"),
        CategoryEntity(name = "Kesehatan", type = TransactionType.EXPENSE, emoji = "\uD83C\uDFE5"),
        CategoryEntity(name = "Belanja", type = TransactionType.EXPENSE, emoji = "\uD83D\uDECD\uFE0F"),
        CategoryEntity(name = "Tagihan", type = TransactionType.EXPENSE, emoji = "\u26A1"),
        CategoryEntity(name = "Hadiah", type = TransactionType.EXPENSE, emoji = "\uD83C\uDF81"),
        CategoryEntity(name = "Gaji", type = TransactionType.INCOME, emoji = "\uD83D\uDCB0"),
        CategoryEntity(name = "Freelance", type = TransactionType.INCOME, emoji = "\uD83D\uDCBB"),
        CategoryEntity(name = "Investasi", type = TransactionType.INCOME, emoji = "\uD83D\uDCC8"),
        CategoryEntity(name = "Lainnya", type = TransactionType.INCOME, emoji = "\uD83D\uDCB5")
    )
}
