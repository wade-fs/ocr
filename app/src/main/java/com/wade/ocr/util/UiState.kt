// util/UiState.kt
package com.wade.ocr.util

import com.wade.ocr.data.model.BusinessCard

sealed class UiState {
    object Idle : UiState()
    object Scanning : UiState()
    object Extracting : UiState()
    data class Done(val card: BusinessCard) : UiState()
    data class Error(val message: String) : UiState()
}
