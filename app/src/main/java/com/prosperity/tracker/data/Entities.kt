package com.prosperity.tracker.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/** Whether a transaction or category represents money coming in or going out. */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/** Room converter so the enum can be stored as TEXT and read back. */
class Converters {
    @TypeConverter
    fun fromType(type: TransactionType): String = type.name

    @TypeConverter
    fun toType(value: String): TransactionType = TransactionType.valueOf(value)
}

/**
 * A place where money is held (cash, bank, e-wallet, ...). `balance` is in whole
 * Rupiah and may be negative (e.g. a credit card). `emoji` is shown as the icon.
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subtitle: String,
    val emoji: String,
    val balance: Long
)

/**
 * A spending/earning category. `emoji` is shown as the icon. Chart colors are
 * assigned at render time from a fixed palette, so no color is stored here.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val emoji: String
)

/**
 * A single money movement. `amount` is whole Rupiah (always positive); direction
 * comes from `type`. `accountId` is the account paid from (expense) or saved to
 * (income). No hard foreign keys are declared, on purpose: it keeps account and
 * category deletion from crashing on constraint violations. Integrity is handled
 * in the repository. `timestamp` is epoch milliseconds.
 */
@Entity(
    tableName = "transactions",
    indices = [Index("categoryId"), Index("accountId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    val timestamp: Long
)

/**
 * A monthly budget allocation for a category. `month` is year*100 + month
 * (e.g. May 2026 -> 202605). The unique index prevents duplicates.
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId", "month"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val month: Int,
    val amount: Long
)

/** Join result: a transaction plus its category and account display fields. */
data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @ColumnInfo(name = "cat_name") val categoryName: String?,
    @ColumnInfo(name = "cat_emoji") val categoryEmoji: String?,
    @ColumnInfo(name = "acc_name") val accountName: String?
)
