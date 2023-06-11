package com.uc.ccs.visuals.screens.admin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.settings.CsvData
import java.io.FileOutputStream

class CSVClient() {

    private var baseDocumentTreeUri: Uri? = null

    fun convertMarkerInfoToCSV(markerInfo: List<CsvData>): String {
        val data = markerInfo.map {
            listOf(
                it.id,
                it.code,
                it.title,
                it.position,
                it.iconImageUrl ?: "",
                it.description ?: "",
            )
        }

        return convertToCSV(data)
    }

    fun convertUserItemToCSV(userItems: List<UserItem>): String {
        val data = userItems.map {
            listOf(
                it.id,
                it.email,
                it.firstName,
                it.lastName,
                it.roles.toString()
            )
        }

        return convertToCSV(data)
    }

    private fun convertToCSV(data: List<List<String>>): String {
        val csvContent = StringBuilder()

        for (row in data) {
            for ((index, field) in row.withIndex()) {
                csvContent.append(field)

                if (index != row.lastIndex) {
                    csvContent.append(",")
                }
            }
            csvContent.append("\n")
        }

        return csvContent.toString()
    }

    fun exportToCSV(context: Context, fileName: String, csvData: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        try {
            baseDocumentTreeUri?.let {
                val directory = DocumentFile.fromTreeUri(context, it)
                val file = directory?.createFile("text/*", fileName)
                val pfd = context.contentResolver.openFileDescriptor(file?.uri!!, "w")
                val fos = FileOutputStream(pfd?.fileDescriptor)
                fos.write(csvData.toByteArray())
                fos.close()

                Log.d("ExportToCSV", "Data exported to $fileName")
                onSuccess.invoke()
            }
        } catch (e: Exception) {
            Log.e("ExportToCSV", "Error exporting data to CSV", e)
            onError.invoke(e)
        }
    }

    fun launchBaseDirectoryPicker(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        launcher.launch(intent)
    }

    fun onActivityResult(context: Context, result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            baseDocumentTreeUri = result.data?.data
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(baseDocumentTreeUri!!, takeFlags)
            val preferences = context.getSharedPreferences("com.example.app.fileutility", Context.MODE_PRIVATE)
            preferences.edit().putString("filestorageuri", baseDocumentTreeUri.toString()).apply()
        }
    }

    fun restoreSavedUri(context: Context) {
        val preferences = context.getSharedPreferences("com.example.app.fileutility", Context.MODE_PRIVATE)
        val uriString = preferences.getString("filestorageuri", null)

        if (uriString != null) {
            baseDocumentTreeUri = Uri.parse(uriString)
        }
    }
}

