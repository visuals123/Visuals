package com.uc.ccs.visuals.utils.extensions

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

fun Fragment.checkLocationPermissions(requestCode: Int): Boolean {
    val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

    if (ActivityCompat.checkSelfPermission(requireContext(), fineLocationPermission) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(requireContext(), coarseLocationPermission) != PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissions(arrayOf(fineLocationPermission, coarseLocationPermission), requestCode)
        return false
    }

    return true
}

fun Fragment.requestLocationPermissions(requestCode: Int) {
    ActivityCompat.requestPermissions(
        requireActivity(),
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        requestCode
    )
}

fun Fragment.showConfirmationDialog(
    title: String,
    message: String,
    positiveText: String,
    negativeText: String,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText) { _, _ -> onPositiveClick() }
        .setNegativeButton(negativeText) { _, _ -> onNegativeClick() }
        .setCancelable(false)
        .show()
}
