package com.example.msdpaint.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.msdpaint.storage.DoodleRepository


class StorageViewModelFactory(
    private val repository: DoodleRepository
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(StorageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StorageViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")

    }


}