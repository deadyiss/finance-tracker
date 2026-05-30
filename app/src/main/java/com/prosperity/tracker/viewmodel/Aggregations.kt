package com.prosperity.tracker.viewmodel

import com.prosperity.tracker.data.TransactionType
import com.prosperity.tracker.data.TransactionWithDetails
import com.prosperity.tracker.util.monthKeyOf

/** A category total used by charts, reports, and budget rows. */
data class CategoryAmount(
    val categoryId: Long,
    val name: String,
    val emoji: String,
    val amount: Long
)

/** Income and expense totals for one month, used by the reports bar chart. */
data class MonthlyTotal(
    val monthKey: Int,
    val income: Long,
    val expense: Long
)

fun List<TransactionWithDetails>.totalIncome(): Long =
    filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }

fun List<TransactionWithDetails>.totalExpense(): Long =
    filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

fun List<TransactionWithDetails>.forMonth(monthKey: Int): List<TransactionWithDetails> =
    filter { monthKeyOf(it.transaction.timestamp) == monthKey }

/** Filters transactions to those occurring in the given year. */
fun List<TransactionWithDetails>.forYear(year: Int): List<TransactionWithDetails> =
    filter { com.prosperity.tracker.util.yearOf(it.transaction.timestamp) == year }

/** Filters transactions to those occurring on the given day key. */
fun List<TransactionWithDetails>.forDay(dayKey: Int): List<TransactionWithDetails> =
    filter { com.prosperity.tracker.util.dayKeyOf(it.transaction.timestamp) == dayKey }

/** Sums expense amounts per category, largest first. */
fun List<TransactionWithDetails>.expenseByCategory(): List<CategoryAmount> =
    filter { it.transaction.type == TransactionType.EXPENSE }
        .groupBy { it.transaction.categoryId }
        .map { (id, items) ->
            val first = items.first()
            CategoryAmount(
                categoryId = id,
                name = first.categoryName ?: "?",
                emoji = first.categoryEmoji ?: "\uD83D\uDCB8",
                amount = items.sumOf { it.transaction.amount }
            )
        }
        .sortedByDescending { it.amount }

/** Sums amounts per category for the given type, largest first. */
fun List<TransactionWithDetails>.byCategory(type: TransactionType): List<CategoryAmount> =
    filter { it.transaction.type == type }
        .groupBy { it.transaction.categoryId }
        .map { (id, items) ->
            val first = items.first()
            CategoryAmount(
                categoryId = id,
                name = first.categoryName ?: "?",
                emoji = first.categoryEmoji ?: "\uD83D\uDCB8",
                amount = items.sumOf { it.transaction.amount }
            )
        }
        .sortedByDescending { it.amount }
