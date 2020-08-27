package com.demosample.videosong

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CameraActivity : AppCompatActivity() {
    var a = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        a = intent.getIntExtra("song", 0)

        checkGivenPermission()
    }

    private fun checkGivenPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ===
                        PackageManager.PERMISSION_GRANTED)
        ) {


        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(
                        this,
                        "Please Give All Permission", Toast.LENGTH_SHORT
                ).show()
            }
            requestPermissions(
                    arrayOf(
                            Manifest.permission.CAMERA,

                            ),
                    1001
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            Toast.makeText(
                    this,
                    "Permission Given", Toast.LENGTH_SHORT
            ).show()

            openCamera()

        }
    }

    private fun openCamera() {

    }
}