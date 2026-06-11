package com.wade.ocr.ocr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class CardOcrProcessor {

    private val textRecognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )
    
    private val barcodeScannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .enableAllPotentialBarcodes() // Enable detection of multiple/dense barcodes
        .build()

    private val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

    fun recognize(bitmap: Bitmap, onResult: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // Step 1: Scan for Barcodes / QR Codes
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val qrData = StringBuilder()
                for (barcode in barcodes) {
                    barcode.rawValue?.let {
                        qrData.appendLine("[QR Code Data: $it]")
                    }
                }
                
                // Step 2: Recognize Text
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val structuredText = buildStructuredText(visionText)
                        val combinedResult = if (qrData.isNotEmpty()) {
                            "$structuredText\n\n$qrData"
                        } else {
                            structuredText
                        }
                        Log.d("OCR", "Recognized combined text:\n$combinedResult")
                        onResult(combinedResult)
                    }
                    .addOnFailureListener { e ->
                        Log.e("OCR", "Text Recognition Failed", e)
                        // Even if text fails, return QR data if any
                        onResult(qrData.toString())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Barcode Scanning Failed", e)
                // Fallback to text recognition only
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val structuredText = buildStructuredText(visionText)
                        Log.d("OCR", "Recognized text only:\n$structuredText")
                        onResult(structuredText)
                    }
                    .addOnFailureListener { e2 ->
                        Log.e("OCR", "Text Recognition Failed", e2)
                        onResult("")
                    }
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

