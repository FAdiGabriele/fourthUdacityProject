package com.udacity.project4.authentication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.databinding.FragmentStartBinding
import com.udacity.project4.utils.Constants.AUTENTICATION_CODE

class StartFragment : Fragment() {


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


}