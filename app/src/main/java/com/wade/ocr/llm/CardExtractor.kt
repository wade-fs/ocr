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

    suspend fun extract(ocrText: String): BusinessCard? {
        return try {
            parseOcr(ocrText)
        } catch (e: Exception) {
            Log.e("Extractor", "Manual parsing failed", e)
            null
        }
    }

    private fun parseExchangeQr(text: String): BusinessCard {
        var name: String? = null
        var title: String? = null
        var company: String? = null
        val phones = mutableListOf<PhoneEntry>()
        val emails = mutableListOf<String>()
        var address: String? = null
        var website: String? = null
        var wechat: String? = null
        var lineInfo: String? = null

        text.lines().forEach { line ->
            when {
                line.startsWith("N:") -> name = line.substring(2)
                line.startsWith("T:") -> title = line.substring(2)
                line.startsWith("C:") -> company = line.substring(2)
                line.startsWith("P:") -> phones.add(PhoneEntry(null, line.substring(2)))
                line.startsWith("E:") -> emails.add(line.substring(2))
                line.startsWith("W:") -> website = line.substring(2)
                line.startsWith("WC:") -> wechat = line.substring(3)
                line.startsWith("L:") -> lineInfo = line.substring(2)
            }
        }

        return BusinessCard(
            name = name,
            title = title,
            company = company,
            phones = phones.ifEmpty { null },
            emails = emails.ifEmpty { null },
            address = address,
            website = website,
            wechat = wechat,
            line = lineInfo,
            note = "透過 QR Code 交換取得"
        )
    }

    private fun parseOcr(text: String): BusinessCard? {
        // Check for custom QR exchange format
        if (text.contains("OCR_CARD_V1")) {
            return parseExchangeQr(text)
        }

        // Filter out "---" which is used as block separator
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() && it != "---" }
        if (lines.isEmpty()) return null

        var name: String? = null
        var title: String? = null
        var company: String? = null
        val phones = mutableListOf<PhoneEntry>()
        val emails = mutableListOf<String>()
        var address: String? = null
        var website: String? = null
        var wechat: String? = null
        var lineInfo: String? = null
        val unclassified = mutableListOf<String>()

        for (line in lines) {
            // QR Code Data
            if (line.startsWith("[QR Code Data:")) {
                unclassified.add(line)
                continue
            }
            // Email
            if (line.contains("@")) {
                emails.add(line)
                continue
            }
            // Website
            if (website == null && Regex("(www\\.|\\.com|\\.net|\\.tw|http)", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                website = line
                continue
            }
            // Phone
            if (Regex("(\\+?886|0)[\\d\\- ()]{8,15}").containsMatchIn(line) ||
                Regex("(Tel|Mobile|Fax|Phone|電話|手機|傳真)[\\s:]*([\\d\\- ()+]+)", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                phones.add(PhoneEntry(type = null, number = line))
                continue
            }
            // Address (市, 縣, 區, 路, 街, 段, 巷, 弄, 號, 樓, F, Rm)
            if (address == null && Regex("(市|縣|區|路|街|段|巷|弄|號|樓|F\\b|Rm\\b)", RegexOption.IGNORE_CASE).containsMatchIn(line) && line.length >= 5 && !line.contains("@")) {
                address = line
                continue
            }
            // WeChat
            if (wechat == null && Regex("(WeChat|微信)[\\s:]*(.*)", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                wechat = line
                continue
            }
            // Line ID
            if (lineInfo == null && Regex("(Line)[\\s:]*(.*)", RegexOption.IGNORE_CASE).containsMatchIn(line) && !line.contains("---")) {
                lineInfo = line
                continue
            }
            // Title
            if (title == null && Regex("(經理|主管|工程師|專員|總監|總經理|董事|副總|顧問|協理|代表|主任|策劃|負責人|長|管理師|理財|Sales|Manager|Director|Engineer|CEO|CTO|Officer)", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                title = line
                continue
            }
            // Company
            if (company == null && Regex("(股份|有限|公司|集團|工作室|企業|實業|商行|大學|科技|工業|MEDIITEK|MEDITEK|MEDI|Inc\\.|Corp\\.|Co\\.,|LLC)", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                company = line
                continue
            }
            // Name (Usually short text without numbers)
            if (name == null && line.length in 2..15 && !Regex("\\d").containsMatchIn(line) && !Regex("(公司|地址|電話|手機|傳真)").containsMatchIn(line)) {
                name = line
                continue
            }
            
            unclassified.add(line)
        }

        // Apply heuristic: Unclassified text at the top is likely the company name
        if (company == null) {
            val potentialCompany = unclassified.firstOrNull { !it.startsWith("[QR") }
            if (potentialCompany != null) {
                company = potentialCompany
                unclassified.remove(potentialCompany)
            }
        }

        val note = if (unclassified.isNotEmpty()) unclassified.joinToString("\n") else null

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
