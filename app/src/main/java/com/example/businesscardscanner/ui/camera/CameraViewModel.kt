package com.example.businesscardscanner.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businesscardscanner.llm.CardExtractor
import com.example.businesscardscanner.ocr.CardOcrProcessor
import com.example.businesscardscanner.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraViewModel : ViewModel() {

    private val ocrProcessor = CardOcrProcessor()
    private val extractor = CardExtractor()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = UiState.Scanning

            // Step 1: 離線 OCR
            val ocrText = suspendCoroutine<String> { cont ->
                ocrProcessor.recognize(bitmap) { text ->
                    cont.resume(text)
                }
            }

            if (ocrText.isEmpty()) {
                _uiState.value = UiState.Error("OCR 辨識失敗或無文字")
                return@launch
            }

            _uiState.value = UiState.Extracting

            // Step 2: LLM 萃取
            val card = extractor.extract(ocrText)

            _uiState.value = if (card != null) {
                UiState.Done(card)
            } else {
                UiState.Error("萃取失敗，請重試")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
