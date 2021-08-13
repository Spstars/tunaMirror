package com.example.aacrawler

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TunaData(var nickname : String,var aa:String):Parcelable {

}
