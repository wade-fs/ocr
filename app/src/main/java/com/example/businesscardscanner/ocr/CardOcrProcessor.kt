package com.example.businesscardscanner.ocr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class CardOcrProcessor {

    private val recognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )

    fun recognize(bitmap: Bitmap, onResult: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val structured = buildStructuredText(visionText)
                onResult(structured)
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Failed", e)
                onResult("")
            }
    }

    // 保留換行與區塊資訊，讓 LLM 更容易理解佈局
    private fun buildStructuredText(text: com.google.mlkit.vision.text.Text): String {
        val sb = StringBuilder()
        for (block in text.textBlocks) {
            for (line in block.lines) {
                sb.appendLine(line.text)
            }
            sb.appendLine("---") // 區塊分隔符
        }
        return sb.toString().trim()
    }
}
