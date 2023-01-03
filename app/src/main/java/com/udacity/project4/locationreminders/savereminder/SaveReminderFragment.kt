package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@SuppressLint("MissingPermission")
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geofenceClient: GeofencingClient
    private val geofenceIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_EVENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                requireActivity(), 0, intent,
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                requireActivity(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            val dataItem =
                ReminderDataItem(
                    title,
                    description.value,
                    location,
                    latitude,
                    longitude
                )
            _viewModel.validateAndSaveReminder(dataItem)

            if (_viewModel.validateEnteredData(dataItem)) {
                if (foregroundAndBackgroundLocationPermissionApproved()) {
                    checkLocationSettingsAndStartGeofence(dataItem)
                } else {
                    requestForegroundAndBackgroundLocationPermissions()
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Enter Valid data",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    // foreground and background location permission
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                )
        val backgroundPermissionApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                )
        return foregroundLocationApproved && backgroundPermissionApproved

    }

    // check location settings and start geofence
    private fun checkLocationSettingsAndStartGeofence(dataItem: ReminderDataItem) {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = com.google.android.gms.location.LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client =
            com.google.android.gms.location.LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())
        task.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startGeoFence(dataItem)
            } else {
                Snackbar.make(
                    requireView(),
                    "Location is not enabled on the device",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkLocationSettingsAndStartGeofence(dataItem)
                }.show()
            }
        }

    }

    // start geofence
    private fun startGeoFence(dataItem: ReminderDataItem) {
        val dataGeoFence = dataItem
        val geoFence = Geofence.Builder()
            .setRequestId(dataGeoFence.id)
            .setCircularRegion(
                dataGeoFence.latitude!!,
                dataGeoFence.longitude!!,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            .addGeofence(geoFence)
            .build()

        geofenceRequest.geofences
        geofenceClient.addGeofences(geofenceRequest, geofenceIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Geofence Added",
                    Toast.LENGTH_SHORT
                ).show()
            }
            addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Geofence Failed to Add",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    // request foreground and background location permissions
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        const val ACTION_EVENT = "ACTION_EVENT"
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33

    }


}
