package com.wade.ocr.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusinessCard(
    val name: String?,
    val title: String?,
    val company: String?,
    val phones: List<PhoneEntry>?,
    val emails: List<String>?,
    val address: String?,
    val website: String?,
    val wechat: String?,
    val line: String?,
    val note: String?,
    val customFields: Map<String, String>? = null
) : Parcelable
