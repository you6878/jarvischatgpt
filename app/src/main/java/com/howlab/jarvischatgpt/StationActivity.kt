package com.howlab.jarvischatgpt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.howlab.jarvischatgpt.databinding.ActivityStationBinding
import com.howlab.jarvischatgpt.db.*
import com.howlab.jarvischatgpt.network.StorageApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StationActivity : AppCompatActivity() {

    private val binding by lazy { ActivityStationBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val subwayAdapter = StationsAdapter {
            val intent = Intent()
            intent.putExtra("KEY_NAME", it.name)

            var line = ""
            it.connectedSubways.forEach {
                line += it.label + " "
            }
            intent.putExtra("KEY_LINE", line)
            setResult(500, intent)
            finish()
        }

        val storage = Firebase.storage

        val db = AppDatabase.build(baseContext)

        lifecycleScope.launch {
            val time =
                storage.reference.child("station_data.csv").metadata.await().updatedTimeMillis

            val pre = getPreferences(MODE_PRIVATE).edit().putLong("KEY_TIME", time).apply()

            val myTime = getPreferences(MODE_PRIVATE).getLong("KEY_TIME", 0L)
            Log.e("TESTTEST", "시간 체크 전")
            Log.e("TESTTEST", "시간 체크 전 $time")

            val downloadSizeBytes = storage.reference
                .child("station_data.csv")
                .metadata
                .await()
                .sizeBytes

            val byteArray = storage.reference
                .child("station_data.csv")
                .getBytes(downloadSizeBytes).await()

            val data = byteArray.decodeToString()
                .lines()
                .drop(1)
                .map { it.split(",") }
                .map { StationEntity(it[1]) to SubwayEntity(it[0].toInt()) }

            db.stationDao().insertStationSubways(data)
        }

        binding.recyclerView.adapter = subwayAdapter

        val stations = MutableStateFlow<List<Station>>(emptyList())

        lifecycleScope.launch {
            db.stationDao().getStationWithSubways().collectLatest { list ->
                stations.update { list.toStations() }
            }
        }

        val query = MutableStateFlow("")

        binding.searchEditText.addTextChangedListener {
            val edit = it.toString()
            query.update { edit }
        }

        lifecycleScope.launch {
            stations.combine(query) { list, ment ->
                list.filter { it.name.contains(ment) }
            }.collectLatest {
                subwayAdapter.setData(it)
            }
        }
    }
}