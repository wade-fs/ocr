package com.example.businesscardscanner.util

import com.example.businesscardscanner.data.model.BusinessCard

sealed class UiState {
    object Idle : UiState()
    object Scanning : UiState()
    object Extracting : UiState()
    data class Done(val card: BusinessCard) : UiState()
    data class Error(val message: String) : UiState()
}
