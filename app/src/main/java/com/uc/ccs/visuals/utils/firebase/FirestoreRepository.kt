package com.uc.ccs.visuals.utils.firebase

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.main.models.TravelHistory
import com.uc.ccs.visuals.screens.settings.CsvData
import java.text.SimpleDateFormat
import java.util.Locale

class FirestoreRepository: IFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override fun saveMultipleData(
        collectionPath: String,
        data: List<CsvData>,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val batch = firestore.batch()

        data.forEach { csvData ->
            if (csvData.id.isBlank() || csvData.code.isBlank() || csvData.title.isBlank() || csvData.description.isBlank()) {
                onFailure(Exception("Incorrect data"))
                return@forEach
            }

            val csvDataMap = hashMapOf<String, Any>()
            csvDataMap["id"] = csvData.id
            csvDataMap["code"] = csvData.code
            csvDataMap["title"] = csvData.title
            csvDataMap["description"] = csvData.description

            // Validate and add position data
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
                    } else {
                        // Invalid latitude or longitude, skip this data
                        return@forEach
                    }
                } else {
                    // Invalid position format, skip this data
                    return@forEach
                }
            }

            csvDataMap["iconImageUrl"] = csvData.iconImageUrl ?: ""
            csvDataMap["vehicleType"] = csvData.vehicleType ?: ""

            val documentReference = firestore.collection(collectionPath)
                .document(csvData.id)
            batch.set(documentReference, csvDataMap)
        }

        batch.commit()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }


    override fun getCsvData(collectionPath: String, onSuccess: (List<CsvData>) -> Unit, onFailure: (e: Exception) -> Unit) {
        firestore.collection(collectionPath)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val csvDataList = mutableListOf<CsvData>()

                for (document in querySnapshot) {
                    val csvDataMap = document.data
                    val id = csvDataMap["id"] as? String
                    val code = csvDataMap["code"] as? String
                    val title = csvDataMap["title"] as? String
                    val description = csvDataMap["description"] as? String
                    val positionMap = csvDataMap["position"] as? Map<String, Any>
                    val iconImageUrl = csvDataMap["iconImageUrl"] as? String
                    val vehicleType = csvDataMap["vehicleType"] as? String

                    if (id != null && title != null && description != null && positionMap != null
                        && code != null && vehicleType != null) {
                        val latitude = positionMap["latitude"] as? Double
                        val longitude = positionMap["longitude"] as? Double

                        if (latitude != null && longitude != null) {
                            val position = "$latitude-$longitude"
                            val csvData = CsvData(
                                id = id,
                                code = code,
                                title = title,
                                description = description,
                                position = position,
                                iconImageUrl = iconImageUrl,
                                vehicleType = vehicleType
                            )
                            csvDataList.add(csvData)
                        }
                    }
                }

                onSuccess(csvDataList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun getUsers(collectionPath: String, onSuccess: (List<UserItem>) -> Unit, onFailure: (e: Exception) -> Unit) {
        firestore.collection(collectionPath)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = mutableListOf<UserItem>()

                for (document in querySnapshot) {
                    val userDataMap = document.data
                    val id = document.id as? String
                    val firstname = userDataMap["firstName"] as? String
                    val lastname = userDataMap["lastName"] as? String
                    val email = userDataMap["email"] as? String

                    if (id != null && firstname != null && lastname != null && email != null) {
                        val user = UserItem(
                            id = id,
                            firstName = firstname,
                            lastName = lastname,
                            email = email
                        )
                        userList.add(user)
                    }
                }

                onSuccess(userList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun getUserByEmail(collectionPath: String, email: String, onSuccess: (UserItem?) -> Unit, onFailure: (e: Exception) -> Unit) {
        firestore.collection(collectionPath)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val userDataMap = documentSnapshot.data
                    val id = documentSnapshot.id as? String
                    val firstname = userDataMap?.get("firstName") as? String
                    val lastname = userDataMap?.get("lastName") as? String
                    val userEmail = userDataMap?.get("email") as? String
                    val roles = userDataMap?.get("roles") as? Long

                    if (id != null && firstname != null && lastname != null && userEmail != null
                        && roles != null) {
                        val user = UserItem(
                            id = id,
                            firstName = firstname,
                            lastName = lastname,
                            email = userEmail,
                            roles = roles.toInt()
                        )
                        onSuccess(user)
                    } else {
                        onSuccess(null) // User data is incomplete
                    }
                } else {
                    onSuccess(null) // User not found
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun saveTravelRideHistory(
        collectionPath: String,
        userEmail: String,
        startDestinationName: String,
        endDestinationName: String,
        startDestinationLatLng: LatLng,
        endDestinationLatLng: LatLng,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val rideHistoryData = hashMapOf(
            "email" to userEmail,
            "startDestinationName" to startDestinationName,
            "endDestinationName" to endDestinationName,
            "startDestinationLatLng" to startDestinationLatLng,
            "endDestinationLatLng" to endDestinationLatLng,
            "timestamp" to Timestamp.now()
        )

        firestore.collection(collectionPath)
            .add(rideHistoryData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    override fun getTravelRideHistory(
        collectionPath: String,
        email: String,
        onSuccess: (List<TravelHistory>) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        firestore.collection(collectionPath)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val rideHistoryList = mutableListOf<TravelHistory>()

                for (document in querySnapshot) {
                    val rideHistoryData = document.data
                    val id = document.id
                    val email = rideHistoryData["email"] as? String
                    val startDestinationName = rideHistoryData["startDestinationName"] as? String
                    val endDestinationName = rideHistoryData["endDestinationName"] as? String
                    val startLatLngMap = rideHistoryData["startDestinationLatLng"] as? Map<String, Double>
                    val endLatLngMap = rideHistoryData["endDestinationLatLng"] as? Map<String, Double>
                    val timestamp = rideHistoryData["timestamp"] as? Timestamp // Retrieve the timestamp value

                    val startDestinationLatLng: LatLng? = startLatLngMap?.let { latLngMap ->
                        val latitude = latLngMap["latitude"]
                        val longitude = latLngMap["longitude"]
                        if (latitude != null && longitude != null) {
                            LatLng(latitude, longitude)
                        } else {
                            null
                        }
                    }

                    val endDestinationLatLng: LatLng? = endLatLngMap?.let { latLngMap ->
                        val latitude = latLngMap["latitude"]
                        val longitude = latLngMap["longitude"]
                        if (latitude != null && longitude != null) {
                            LatLng(latitude, longitude)
                        } else {
                            null
                        }
                    }

                    if (email != null && startDestinationName != null && endDestinationName != null &&
                        startDestinationLatLng != null && endDestinationLatLng != null && timestamp != null
                    ) {
                        val rideHistory = TravelHistory(
                            id = id,
                            email = email,
                            startDestinationName = startDestinationName,
                            endDestinationName = endDestinationName,
                            startDestinationLatLng = startDestinationLatLng,
                            endDestinationLatLng = endDestinationLatLng,
                            timestamp = timestamp.toFormattedString()
                        )
                        rideHistoryList.add(rideHistory)
                    }
                }

                onSuccess(rideHistoryList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    override fun deleteTravelRideHistory(
        collectionPath: String,
        documentId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        firestore.collection(collectionPath)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

}

fun Timestamp.toFormattedString(): String {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    return dateFormat.format(this.toDate())
}