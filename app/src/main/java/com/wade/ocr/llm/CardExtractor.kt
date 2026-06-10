package com.wade.ocr.llm

import android.util.Log
import com.wade.ocr.data.model.BusinessCard
import com.wade.ocr.data.model.PhoneEntry

/**
 * Simple extractor that parses the raw OCR string returned by ML Kit.
 * It does not call any external LLM service – all information is extracted
 * locally by analysing the lines of text.
 */
class CardExtractor {

    /**
     * Extract a BusinessCard from the raw OCR text.
     * The algorithm is heuristics‑based and aims to work for typical Chinese
     * business cards (name, title, company, phone numbers, e‑mail, address,
     * website, etc.).
     */
    suspend fun extract(ocrText: String): BusinessCard? {
        return try {
            parseOcr(ocrText)
        } catch (e: Exception) {
            Log.e("Extractor", "Manual parsing failed", e)
            null
        }
    }

    /**
     * Parse the OCR text line‑by‑line and fill a BusinessCard.
     */
    private fun parseOcr(text: String): BusinessCard? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return null

        var name: String? = null
        var title: String? = null
        var company: String? = null
        val phones = mutableListOf<PhoneEntry>()
        val emails = mutableListOf<String>()
        var address: String? = null
        var website: String? = null
        var wechat: String? = null
        var note: String? = null
        var lineInfo: String? = null

        // Simple heuristics
        for (line in lines) {
            // Name – usually the first line containing Chinese characters and ending with "博士" or similar
            if (name == null && (line.contains("博士") || line.contains("先生") || line.contains("女士"))) {
                name = line
                continue
            }
            // Company – contains "股份有限公司" or "有限公司" or known brand names
            if (company == null && (line.contains("股份有限公司") || line.contains("有限公司") || line.matches(Regex(".*(MEDIITEK|MEDITEK|MEDI).*", RegexOption.IGNORE_CASE))) {
                company = line
                continue
            }
            // Title – contains typical titles like "資深經理", "主管", "工程師" etc.
            if (title == null && line.matches(Regex(".*(經理|主管|工程師|專員|總監|總經理|董事|副總|總辦公|顧問).*"))) {
                title = line
                continue
            }
            // Phone – contains Taiwan mobile pattern
            if (line.matches(Regex(".*(\\+?886|0)\\d{1,4}[ -]?\\d{3,4}[ -]?\\d{3,4}.*"))) {
                phones.add(PhoneEntry(type = null, number = line))
                continue
            }
            // Email – contains @
            if (line.contains("@")) {
                emails.add(line)
                continue
            }
            // Website – contains "www" or ".com"
            if (website == null && (line.contains("www") || line.contains(".com"))) {
                website = line
                continue
            }
            // Address – contains typical address keywords
            if (address == null && line.matches(Regex(".*(新竹|台北|台中|台南|高雄).*"))) {
                address = line
                continue
            }
            // WeChat – often starts with "WeChat" or contains "微信"
            if (wechat == null && (line.contains("WeChat") || line.contains("微信"))) {
                wechat = line
                continue
            }
            // Anything else we treat as a note/line
            if (lineInfo == null) {
                lineInfo = line
            } else {
                note = (note ?: "") + "\n" + line
            }
        }

        return BusinessCard(
            name = name,
            title = title,
            company = company,
            phones = if (phones.isNotEmpty()) phones else null,
            emails = if (emails.isNotEmpty()) emails else null,
            address = address,
            website = website,
            wechat = wechat,
            line = lineInfo,
            note = note?.trim()
        )
    }
}
