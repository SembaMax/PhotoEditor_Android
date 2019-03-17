package com.semba.photoeditor

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleOptions()
    }

    private fun handleOptions() {
        create_report_take_snapshot?.setOnClickListener {
            val snapshotIntent = Intent(this@MainActivity, SnapshotActivityUpgraded::class.java)
            startActivity(snapshotIntent)
        }
    }
}
