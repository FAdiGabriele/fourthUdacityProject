package com.udacity.project4.authentication.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentStartBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.Constants.AUTENTICATION_CODE

class StartFragment : Fragment() {

    //todo: Manage registration
    lateinit var binding : FragmentStartBinding
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStartBinding.inflate(inflater,container,false)

        binding.buttonLogin.setOnClickListener {
            // TODO: a bonus is to customize the sign in flow to look nice using :
            //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                    providers
                ).build(), AUTENTICATION_CODE)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTENTICATION_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user
                Log.i(
                    Constants.FIREBASE_TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )

                val intent = Intent(requireActivity(), RemindersActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                // Sign in failed
                Log.i(Constants.FIREBASE_TAG, "Sign in unsuccessful ${response?.error?.errorCode}")

                Toast.makeText(requireContext(), resources.getText(R.string.login_failed), Toast.LENGTH_LONG).show()
            }
        }
    }


}