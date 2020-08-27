package com.demosample.videosong

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var name: String
    private val REQUEST_CAMERA_PERMISSION_RESULT = 0
    private var cameraDsiplayer: TextureView? = null

    private var i: Int = 1
    private val mSurfaceTextureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
        ) {
            setupCamera(width, height)
            connectCamera()
        }

        override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
        ) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            //detectFace(i)
            i++
        }
    }
    private var mCameraDevice: CameraDevice? = null
    private val mCameraDeviceStateCallback: CameraDevice.StateCallback =
            object : CameraDevice.StateCallback() {
                override fun onOpened(p0: CameraDevice) {
                    mCameraDevice = p0
                    mMediaRecorder = MediaRecorder()
                    if (mIsRecording) {
                        startRecord()
                        mMediaRecorder!!.start()

                    } else {
                        startPreview()
                    }

                }

                override fun onDisconnected(p0: CameraDevice) {
                    p0.close()
                    mCameraDevice = null
                }

                override fun onError(p0: CameraDevice, p1: Int) {
                    p0.close()
                    mCameraDevice = null
                }

            }
    private var mBackgroundHandlerThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private lateinit var mCameraId: String
    private var mMediaRecorder: MediaRecorder? = null
    private var mRecordCaptureSession: CameraCaptureSession? = null
    private lateinit var mCaptureRequestBuilder: CaptureRequest.Builder
    private lateinit var clickme: AppCompatButton

    private var mIsRecording = false
    var a = 0
    var k = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        a = intent.getIntExtra("song", 0)
        cameraDsiplayer = findViewById(R.id.view_finder)
        checkGivenPermission()
        clickme = findViewById(R.id.click)
        clickme.setOnClickListener {
            if (k == 0) {
                val mediaPlayer = MediaPlayer()
                var afd: AssetFileDescriptor
                try {
                    if (a == 1)
                        afd = applicationContext.assets.openFd("a.mp3")
                    else
                        afd = applicationContext.assets.openFd("b.mp3")
                    mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    Toast.makeText(this, "Playing" + "a",
                            Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                startRecord()
                mMediaRecorder?.start()
                k++
                clickme.text = "Done"
            } else {
                k = 0
                stopRecord()
            }


        }
    }


    fun stopRecord() {
        mRecordCaptureSession?.close()
        mIsRecording = false
        mMediaRecorder?.stop()
        mMediaRecorder?.reset()
        Log.d("Successful", getOutputDirectory(this).toString())
    }


    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (cameraDsiplayer?.isAvailable!!) {
            setupCamera(cameraDsiplayer!!.width, cameraDsiplayer!!.height)
            connectCamera()
        } else {
            cameraDsiplayer!!.surfaceTextureListener = mSurfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
        mMediaRecorder?.release()

    }


    private fun startBackgroundThread() {
        mBackgroundHandlerThread = HandlerThread("Camera2VideoImage")
        mBackgroundHandlerThread!!.start()
        mBackgroundHandler = Handler(mBackgroundHandlerThread!!.looper)
    }

    private fun stopBackgroundThread() {
        mBackgroundHandlerThread?.quitSafely()
        try {
            mBackgroundHandlerThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    private fun setupCamera(width: Int, height: Int) {
        val cameraManager: CameraManager =
                getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val cameraCharacteristics: CameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId)
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ===
                        CameraCharacteristics.LENS_FACING_BACK
                ) {
                    continue
                }

                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun connectCamera() {
        val cameraManager =
                getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ===
                        PackageManager.PERMISSION_GRANTED
                ) {
                    cameraManager.openCamera(
                            mCameraId,
                            mCameraDeviceStateCallback,
                            mBackgroundHandler
                    )
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(
                                this,
                                "Video app required access to camera", Toast.LENGTH_SHORT
                        ).show()
                    }
                    requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION_RESULT
                    )
                }
            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun startPreview() {
        val surfaceTexture = cameraDsiplayer?.surfaceTexture
        val previewSurface = Surface(surfaceTexture)
        try {
            mCaptureRequestBuilder =
                    mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mCaptureRequestBuilder.addTarget(previewSurface)
            mCameraDevice!!.createCaptureSession(
                    Arrays.asList(previewSurface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(p0: CameraCaptureSession) {
                            Toast.makeText(
                                    applicationContext,
                                    "Unable to setup camera preview", Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onConfigured(p0: CameraCaptureSession) {
                            try {
                                p0.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(),
                                        null, mBackgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                    }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    @Throws(IOException::class)
    private fun setupMediaRecorder() {
        val cpHigh: CamcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder!!.setOrientationHint(0)
        mMediaRecorder!!.setOutputFile(getOutputDirectory(this))
        mMediaRecorder!!.setVideoEncodingBitRate(cpHigh.videoBitRate)
        mMediaRecorder!!.setVideoFrameRate(cpHigh.videoFrameRate)
        mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)


        //  mMediaRecorder!!.setAudioSource(MediaRecorder.AudioEncoder.AAC)
        mMediaRecorder!!.prepare()
    }


    private fun startRecord() {
        try {
            setupMediaRecorder()
            val surfaceTexture = cameraDsiplayer!!.surfaceTexture
            val previewSurface = Surface(surfaceTexture)
            val recordSurface = mMediaRecorder!!.surface
            mCaptureRequestBuilder =
                    mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            mCaptureRequestBuilder.addTarget(previewSurface)
            mCaptureRequestBuilder.addTarget(recordSurface)
            mCameraDevice!!.createCaptureSession(listOf(previewSurface, recordSurface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            mRecordCaptureSession = session
                            try {
                                mRecordCaptureSession!!.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getOutputDirectory(context: Context): File {
        return File(
                externalMediaDirs.first(),
                "abc.mp4"
        )
    }

    private fun checkGivenPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ===
                        PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                ) ===
                        PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) ===
                        PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) ===
                        PackageManager.PERMISSION_GRANTED)
        ) {


        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) &&
                    shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) &&
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                Toast.makeText(
                        this,
                        "Please Give All Permission", Toast.LENGTH_SHORT
                ).show()
            }
            requestPermissions(
                    arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
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


        }
    }

}

