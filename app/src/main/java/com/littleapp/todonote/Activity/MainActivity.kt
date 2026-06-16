package com.littleapp.todonote.Activity

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.littleapp.todonote.R
import com.littleapp.todonote.Unit.THEME
import com.littleapp.todonote.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    var context = this@MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        //THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostFragment.findNavController()

        binding.bottomNavigation.setupWithNavController(navController)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.tasksFragment, R.id.notesFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

const val ADD_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1