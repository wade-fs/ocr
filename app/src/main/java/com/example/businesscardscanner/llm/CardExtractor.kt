package com.example.businesscardscanner.llm

import android.util.Log
import com.example.businesscardscanner.BuildConfig
import com.example.businesscardscanner.data.model.BusinessCard
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class CardExtractor {

    private val model = GenerativeModel(
        modelName = "models/gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun extract(ocrText: String): BusinessCard? {
        return try {
            val prompt = PromptBuilder.buildPrompt(ocrText)
            val response = model.generateContent(prompt)
            val json = response.text?.trim() ?: return null
            parseJson(json)
        } catch (e: Exception) {
            Log.e("Extractor", "LLM call failed", e)
            null
        }
    }

    private fun parseJson(json: String): BusinessCard? {
        val cleaned = json
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return try {
            Gson().fromJson(cleaned, BusinessCard::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("Extractor", "JSON parse error: $cleaned", e)
            null
        }
    }
}
