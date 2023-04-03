package com.howlab.jarvischatgpt.db

import androidx.room.Entity

@Entity(primaryKeys = ["stationName", "subwayId"])
data class StationsSubwayCrossRefEntity(
    val stationName: String,
    val subwayId: Int
)