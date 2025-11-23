package com.example.myapplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: ExpenseDbHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = ExpenseDbHelper(this)

        setContent {
            ExpenseTrackerApp(dbHelper)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
