package com.uc.ccs.visuals.utils.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.uc.ccs.visuals.screens.settings.CsvData

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()


    fun saveMultipleData(collectionPath: String, data: List<CsvData>, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit) {
        val batch = firestore.batch()

        data.map { csvData ->
            val csvDataMap = hashMapOf<String, Any>()
            csvDataMap["id"] = csvData.id
            csvDataMap["title"] = csvData.title
            csvDataMap["description"] = csvData.description
            csvData.position.let { position ->
                val latLong = position.split("-")
                if (latLong.size == 2) {
                    val latitude = latLong[0].toDoubleOrNull()
                    val longitude = latLong[1].toDoubleOrNull()
                    if (latitude != null && longitude != null) {
                        val positionMap = hashMapOf<String, Any>(
                            "latitude" to latitude,
                            "longitude" to longitude
                        )
                        csvDataMap["position"] = positionMap
                    }
                }
            }
            csvDataMap["distance"] = csvData.distance
            csvDataMap["iconImageUrl"] = csvData.iconImageUrl ?: ""

            val documentReference = firestore.collection(collectionPath)
                .document(csvData.id)
            batch.set(documentReference,csvDataMap)
        }
        batch.commit()
            .addOnSuccessListener { documentReference ->
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}