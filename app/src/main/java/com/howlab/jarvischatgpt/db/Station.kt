package com.howlab.jarvischatgpt.db

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Station(
    val name: String,
    val connectedSubways: List<Subway>
) : Parcelable