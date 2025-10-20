// New content for FinanceViewModel.kt
package com.janis_petrovs.financialapplication.ui.viewmodel // Will be updated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.janis_petrovs.financialapplication.data.Transaction
import com.janis_petrovs.financialapplication.data.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FinanceViewModel(private val dao: TransactionDao) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.insert(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.delete(transaction)
        }
    }
}