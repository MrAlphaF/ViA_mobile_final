// New content for FinanceViewModelFactory.kt
package com.janis_petrovs.financialapplication.ui.viewmodel // Will be updated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.janis_petrovs.financialapplication.data.TransactionDao

class FinanceViewModelFactory(private val dao: TransactionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}