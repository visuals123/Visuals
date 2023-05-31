package com.uc.ccs.visuals.utils.extensions

import com.google.android.gms.maps.model.LatLng
import com.uc.ccs.visuals.data.LocalCsvData
import com.uc.ccs.visuals.screens.main.models.MarkerInfo
import com.uc.ccs.visuals.screens.settings.CsvData

fun List<CsvData>.toLocalCsvDataList(): List<LocalCsvData> {
    return map { csvData ->
        LocalCsvData(
            id = csvData.id,
            code = csvData.code,
            title = csvData.title,
            description = csvData.description,
            position = csvData.position,
            iconImageUrl = csvData.iconImageUrl
        )
    }
}

fun List<CsvData>.toMarkerInfoList(): List<MarkerInfo> {
    return map { item ->
        val latLng = item.position.split("-")
        MarkerInfo(
            id = item.id,
            code = item.code,
            title = item.title,
            position = LatLng(latLng[0].toDouble(), latLng[1].toDouble()),
            iconImageUrl = item.iconImageUrl,
            description = item.description,
            isWithinRadius = false,
        )
    }
}