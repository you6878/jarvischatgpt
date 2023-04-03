package com.howlab.jarvischatgpt.network

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.howlab.jarvischatgpt.db.StationEntity
import com.howlab.jarvischatgpt.db.SubwayEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class StorageApi {

    private val firebaseStorage = FirebaseStorage.getInstance()

    private val sheetReference = firebaseStorage.reference.child(STATION_DATA_FILE_NAME)

    suspend fun getStationDataUpdatedTimeMillis(): Long =
        sheetReference.metadata.await().updatedTimeMillis

    suspend fun getStationSubways(): List<Pair<StationEntity, SubwayEntity>> {
        val downloadSizeBytes = sheetReference.metadata.await().sizeBytes
        val byteArray = sheetReference.getBytes(downloadSizeBytes).await()

        return byteArray.decodeToString()
            .lines()
            .drop(1)
            .map { it.split(",") }
            .map { StationEntity(it[1]) to SubwayEntity(it[0].toInt()) }
    }

    suspend fun uploadImagesAsync(mediaList: List<Uri>): Deferred<List<String>> =
        CoroutineScope(Dispatchers.IO).async {
            val images = mutableListOf<String>()
            val uploadDeferred: List<Deferred<Any>> =
                mediaList.mapIndexed { index, media ->
                    async {
                        try {
                            val userId = 123123
                            val fileName =
                                "${userId}-${123123}-image${index}.png"
                            firebaseStorage.reference.child("product/${userId}").child(fileName)
                                .putFile(media)
                                .await()
                                .storage
                                .downloadUrl
                                .await()
                                .toString().also {
                                    images.add(it)
                                }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Pair(media, e)
                        }
                    }
                }
            uploadDeferred.awaitAll()
            images
        }

    companion object {
        private const val STATION_DATA_FILE_NAME = "station_data.csv"
    }
}