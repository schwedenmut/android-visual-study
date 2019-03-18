package de.schwedenmut.mobileinformationstudy

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.Menu
import android.view.MenuItem
import de.schwedenmut.mobileinformationstudy.R.menu.menu_main
import de.schwedenmut.mobileinformationstudy.misc.Const
import de.schwedenmut.mobileinformationstudy.misc.ExportTask
import de.schwedenmut.mobileinformationstudy.misc.Utilities
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val wasWizardShown: Boolean = getDefaultSharedPreferences(this).getBoolean(Const.WAS_WIZARD_SHOWN, false)
        if (!wasWizardShown) {
            startActivity(Intent(this, PagerActivity::class.java))
            finish()
        } else {
            if (!Utilities.isPermissionGranted(this)) startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_export -> {
                if (!ExportTask.exporting) ExportTask(this).execute()
                return true
            }
            R.id.action_information -> {
                startActivity(Intent(this, PagerActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
