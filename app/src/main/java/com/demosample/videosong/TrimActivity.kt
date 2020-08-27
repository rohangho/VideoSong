package com.demosample.videosong

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

class TrimActivity : AppCompatActivity() {

    private var abc: VideoView? = null
    private var selectedImageUri: Uri? = null
    private var mediaMetadataRetriever: MediaMetadataRetriever? = null
    private lateinit var recyclerView: RecyclerView
    private var seek1: SeekBar? = null
    private var downLoadButton: Button? = null
    private var filterOne: Button? = null
    private var filterTwo: Button? = null
    private var filterThree: Button? = null
    private var seek2: SeekBar? = null
    private var allAdapter: RecyclerAdapter? = null
    var bitmapper1 = ArrayList<Bitmap>()
    private var downloadCounter: Int = 0
    private var a: Int = 0
    private var b: Int = 100
    private var starter: Int = 0
    private var ender: Int = 100
    val uiScope = CoroutineScope(Dispatchers.Main)
    private var filter: Int = 0
    lateinit var filterType: Filter
    private lateinit var mNotifyManager: NotificationManagerCompat

    val CHANNEL_ID = "1001"
    val notificationId = 1001
    val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
        setContentTitle("Picture Download")
        setContentText("Download in progress")
        setSmallIcon(R.drawable.ic_launcher_foreground)
        priority = NotificationCompat.PRIORITY_MAX
    }

    init {
        System.loadLibrary("NativeImageProcessor")
    }

    private lateinit var uri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim)
        uri = intent.getStringExtra("pathname")

        mNotifyManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificatonChannel()
        seek1 = findViewById(R.id.firstProgress)
        seek2 = findViewById(R.id.secondProgress)
        abc = findViewById(R.id.surfaceView)
        filterOne = findViewById(R.id.filter1)
        filterTwo = findViewById(R.id.filter2)
        filterThree = findViewById(R.id.filter3)
        downLoadButton = findViewById(R.id.button)
        downLoadButton?.setOnClickListener {

            Toast.makeText(this, "Downloading Started .. you can put the app in background", Toast.LENGTH_SHORT)
            sendWorkMAnager(bitmapper1)

        }
        recyclerView = findViewById(R.id.recicler)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        filterOne?.setOnClickListener {
            filter = 1
            filterType = FilterPack.getClarendon(this)
            startRetriverWork()
            Toast.makeText(this, "Filter Applied ... Press download to download frames", Toast.LENGTH_SHORT)
        }
        filterTwo?.setOnClickListener {
            filter = 2
            filterType = FilterPack.getAmazonFilter(this)
            startRetriverWork()
            Toast.makeText(this, "Filter Applied ... Press download to download frames", Toast.LENGTH_SHORT)
        }
        filterThree?.setOnClickListener {
            filter = 3
            filterType = FilterPack.getBlueMessFilter(this)
            startRetriverWork()
            Toast.makeText(this, "Filter Applied ... Press download to download frames", Toast.LENGTH_SHORT)
        }

        seek1?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                a = seekBar?.progress!!
                changeInFrames()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        seek2?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                b = seekBar?.progress!!
                changeInFrames()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })


        abc!!.setVideoURI(Uri.parse(uri))
        abc!!.start()
        abc!!.setOnPreparedListener { mp -> mp.isLooping = true }
        startRetriverWork()
    }

    private fun sendWorkMAnager(bitmapper: ArrayList<Bitmap>) {
        var abc = arrayOfNulls<String>(bitmapper.size)
        val data = Data.Builder()
        var i = 0
        builder.setProgress(100, 0, false)
        mNotifyManager.notify(notificationId, builder.build())
        while (i < bitmapper.size) {
//            createDirectoryAndSaveFile(bitmapper.get(i),Integer.toString(i))
            abc[i] = BitMapToString(bitmapper.get(i))
            data.putString("array", abc[i])
            val workRequest = OneTimeWorkRequestBuilder<MyWorker>().setInputData(data.build())
                    .build()
            val workMAgerDemo = WorkManager.getInstance(this).enqueue(workRequest)
            if (workMAgerDemo.result.isDone) {
                downloadCounter++
                builder.setProgress(100, downloadCounter * 10, false)
                mNotifyManager.notify(notificationId, builder.build())

            }

            i++
        }
        builder.setContentText("Download complete")
                .setProgress(0, 0, false)
        mNotifyManager.notify(notificationId, builder.build())


    }

    private fun createNotificatonChannel() {
        val id = CHANNEL_ID
        val notificationChannel = NotificationChannel(id, "Progress", NotificationManager.IMPORTANCE_LOW)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNotifyManager.createNotificationChannel(notificationChannel)
    }

    fun changeInFrames() {
        if (a > b) {
            starter = b
            ender = a
        } else if (b > a) {
            starter = a
            ender = b
        } else {
            starter = 0
            ender = 100
        }

        startRetriverWork()
    }


    private fun startRetriverWork() {
        val tRetriever = MediaMetadataRetriever()


        tRetriever.setDataSource(baseContext, Uri.parse(uri))
        mediaMetadataRetriever = tRetriever
        uiScope.launch {
            processBitmap(mediaMetadataRetriever!!)
        }

    }

    private suspend fun processBitmap(mediaMetadataRetriever: MediaMetadataRetriever) {
        withContext(Dispatchers.Default) {
            val bitmapper = ArrayList<Bitmap>()
            val DURATION = mediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION)

            var maxDur = (1000 * DURATION.toDouble()).toLong()
            maxDur = ((ender * maxDur) / 100)

            var i: Long = (starter * maxDur) / 100

            while (i < maxDur) {
                if (filter != 0)
                    bitmapper.add(filterType.processFilter(getResizedBitmap(mediaMetadataRetriever.getFrameAtTime(i), 50)))
                else
                    bitmapper.add(getResizedBitmap(mediaMetadataRetriever.getFrameAtTime(i), 50))


                i = i + maxDur / 10
            }

            withContext(Dispatchers.Main) {
                bitmapper1 = bitmapper
                allAdapter = RecyclerAdapter(bitmapper, applicationContext)
                recyclerView.adapter = allAdapter
                mediaMetadataRetriever.release()
            }
        }


    }


    fun BitMapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

}