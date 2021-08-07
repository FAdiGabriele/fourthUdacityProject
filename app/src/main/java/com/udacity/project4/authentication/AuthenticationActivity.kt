package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.databinding.FragmentStartBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.Constants

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    lateinit var binding : ActivityAuthenticationBinding
    private val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
    )

    var customLayout = AuthMethodPickerLayout.Builder(R.layout.firebase_autenticator_layout)
            .setGoogleButtonId(R.id.google_button)
            .setEmailButtonId(R.id.email_button)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                            .setAuthMethodPickerLayout(customLayout)
                            .setIsSmartLockEnabled(false).build(), Constants.AUTENTICATION_CODE)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.AUTENTICATION_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user
                Log.i(
                        Constants.FIREBASE_TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )

                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Sign in failed
                Log.i(Constants.FIREBASE_TAG, "Sign in unsuccessful ${response?.error?.errorCode}")

                Toast.makeText(this, resources.getText(R.string.login_failed), Toast.LENGTH_LONG).show()
            }
        }

    }
}
