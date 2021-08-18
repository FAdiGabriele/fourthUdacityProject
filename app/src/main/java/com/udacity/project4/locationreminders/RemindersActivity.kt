package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.utils.Constants
import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRemindersBinding
    private val commonViewModel: CommonViewModel by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Constants.REQUEST_TURN_DEVICE_LOCATION_ON ->
                if(resultCode == RESULT_OK){
                    commonViewModel.locationRequestedAndApproved.value = true
                }
            }
        }
}
