package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    private val commonViewModel: CommonViewModel by inject()
    private var showedReminderDataItem : ReminderDataItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_reminder_description
        )

        if(isPresentAnErrorInIntent()){
            returnToRemindersActivity()
        }else{
            binding.reminderDataItem = showedReminderDataItem

            commonViewModel.removeReminderData(showedReminderDataItem!!)
        }

        binding.buttonConfirm.setOnClickListener {
            returnToRemindersActivity()
        }

    }


    private fun returnToRemindersActivity(){
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isPresentAnErrorInIntent() : Boolean{
        return if(intent != null && intent.hasExtra(EXTRA_ReminderDataItem)){
            showedReminderDataItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem?
            if(showedReminderDataItem == null){
                Toast.makeText(this, "error null", Toast.LENGTH_SHORT).show()
                true
            }else{
                false
            }
        }else{
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
            true
        }
    }
}
