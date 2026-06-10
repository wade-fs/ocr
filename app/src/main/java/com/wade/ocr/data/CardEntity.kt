package com.wade.ocr.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Entity representing a business card stored locally.
 * Complex fields (phones, emails, bounding boxes) are serialized to JSON strings.
 */
@Entity(tableName = "business_cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "company") val company: String?,
    @ColumnInfo(name = "phones_json") val phonesJson: String?, // JSON array of PhoneEntry
    @ColumnInfo(name = "emails_json") val emailsJson: String?, // JSON array of String
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "website") val website: String?,
    @ColumnInfo(name = "wechat") val wechat: String?,
    @ColumnInfo(name = "category") val category: String = "personal", // 預設個人分類 // 用於分類、移動
    @ColumnInfo(name = "raw_text") val rawText: String?, // 完整 OCR 結果，供編輯時參考
    @ColumnInfo(name = "bbox_json") val bboxJson: String? // 每行文字的 Rect 序列化
)
