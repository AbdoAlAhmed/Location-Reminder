package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        // permissions check and request
//        checkPermissionsAndStartGeofence()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
                startActivity(Intent(requireContext(), AuthenticationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }
//
//    private fun checkPermissionsAndStartGeofence() {
//          if (foregroundAndBackgroundLocationPermissionApproved()) {
//            } else {
//                requestForegroundAndBackgroundLocationPermissions()
//            }
//    }
//    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
//        val foregroundLocationApproved = (
//                PackageManager.PERMISSION_GRANTED ==
//                        ContextCompat.checkSelfPermission(
//                                requireContext(),
//                                Manifest.permission.ACCESS_FINE_LOCATION
//                        )
//                )
//        val backgroundPermissionApproved = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            PackageManager.PERMISSION_GRANTED ==
//                    ContextCompat.checkSelfPermission(
//                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                    )
//        } else {
//            true
//        }
//        return foregroundLocationApproved && backgroundPermissionApproved
//    }
//    private fun requestForegroundAndBackgroundLocationPermissions() {
//        if (foregroundAndBackgroundLocationPermissionApproved()) {
//            return
//        }
//        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        val resultCode = when {
//            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q -> {
//                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//            }
//            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//        }
//        requestPermissions(
//                permissionsArray,
//                resultCode
//        )
//    }
//
//
//    companion object {
//        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
//        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
//    }

}
