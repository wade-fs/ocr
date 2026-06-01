package com.example.businesscardscanner.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhoneEntry(
    val type: String?, // "mobile", "office", "fax"
    val number: String?
) : Parcelable
