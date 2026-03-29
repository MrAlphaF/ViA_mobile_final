package com.group3.financialapplication.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group3.financialapplication.data.TransactionDao

class FinanceViewModelFactory(
    private val dao: TransactionDao,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(dao, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}