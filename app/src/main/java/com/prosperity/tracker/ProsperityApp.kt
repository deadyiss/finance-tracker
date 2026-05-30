package com.prosperity.tracker

import android.app.Application
import com.prosperity.tracker.data.AppDatabase
import com.prosperity.tracker.data.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ProsperityApp : Application() {

    val repository: FinanceRepository by lazy {
        val db = AppDatabase.get(this)
        FinanceRepository(db.accountDao(), db.categoryDao(), db.transactionDao(), db.budgetDao())
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.seedDefaultsIfEmpty() }
    }
}
