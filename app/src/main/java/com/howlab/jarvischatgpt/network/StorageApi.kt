package com.howlab.jarvischatgpt.network

import com.google.firebase.storage.FirebaseStorage
import com.howlab.jarvischatgpt.db.StationEntity
import com.howlab.jarvischatgpt.db.SubwayEntity
import kotlinx.coroutines.tasks.await

class StorageApi {

    private val firebaseStorage = FirebaseStorage.getInstance()

    private val sheetReference = firebaseStorage.reference.child(STATION_DATA_FILE_NAME)

    suspend fun getStationDataUpdatedTimeMillis(): Long
            = sheetReference.metadata.await().updatedTimeMillis

    suspend fun getStationSubways(): List<Pair<StationEntity, SubwayEntity>> {
        val downloadSizeBytes = sheetReference.metadata.await().sizeBytes
        val byteArray = sheetReference.getBytes(downloadSizeBytes).await()

        return byteArray.decodeToString()
            .lines()
            .drop(1)
            .map { it.split(",") }
            .map { StationEntity(it[1]) to SubwayEntity(it[0].toInt()) }
    }

    companion object {
        private const val STATION_DATA_FILE_NAME = "station_data.csv"
    }
}