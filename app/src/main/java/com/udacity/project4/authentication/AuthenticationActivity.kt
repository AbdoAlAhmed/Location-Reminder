package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private var REQUEST_CODE_SIGN_IN = 1001
    private var viewModel = AuthenticationViewModel()
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->

        viewModel.checkUser()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.checkUser()
        viewModel.navigateToReminders.observe(
            this
        ) {
            if (it) {
                startActivity(Intent(this, RemindersActivity::class.java))
                finish()
            }
        }

        with(binding) {
            login.setOnClickListener {
                createAnAccount()
            }

            register.setOnClickListener {
                createAnAccount()
            }
        }


//         DONE: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
//          DONE: If the user was authenticated, send him to RemindersActivity
//          DONE: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun createAnAccount() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        // custom layout
        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.activity_authentication)
            .setGoogleButtonId(R.id.google)
            .setEmailButtonId(R.id.email)
            .build()

        // Create and launch sign-in intent
//        startActivityForResult(
//            AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .setAuthMethodPickerLayout(customLayout)
//                .build(),
//            REQUEST_CODE_SIGN_IN
//        )
        val singIn = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(customLayout)
            .build()
        signInLauncher.launch(singIn)

    }


}
