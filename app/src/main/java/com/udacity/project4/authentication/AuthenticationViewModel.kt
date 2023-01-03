package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth


class AuthenticationViewModel : ViewModel() {
    private val _user = MutableLiveData<FirebaseAuth>()
    val user : LiveData<FirebaseAuth>
        get() = _user

    private val _navigateToReminders = MutableLiveData<Boolean>()
    val navigateToReminders : LiveData<Boolean>
        get() = _navigateToReminders


    init {
        _user.value = FirebaseAuth.getInstance()
        _navigateToReminders.value = false
    }

    fun checkUser() : Boolean {
       return if (_user.value?.currentUser != null) {
            _navigateToReminders.value = true
            true
        } else {
            _navigateToReminders.value = false
            false
        }
    }





}