package com.example.mobivideoplayer.activities

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobivideoplayer.R
import com.example.mobivideoplayer.adapters.IconsAdapter
import com.example.mobivideoplayer.databinding.ActivityVideoPlayerBinding
import com.example.mobivideoplayer.models.IconData
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.PositionInfo
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File

class MobiVideoPlayer: AppCompatActivity(), View.OnClickListener {
    var mVideoFiles: ArrayList<String>? = null
    var player: SimpleExoPlayer? = null
    var position = 0
    var title: TextView? = null

    var videoBack: ImageView? = null
    var lock: ImageView? = null
    var unlock:ImageView? = null
    var scaling:ImageView? = null
    var root: RelativeLayout? = null
    var concatenatingMediaSource: ConcatenatingMediaSource? = null
    var nextButton: ImageView? = null
    var previousButton:ImageView? = null
    private val iconModelArrayList: ArrayList<IconData> = ArrayList<IconData>()
    var playbackIconsAdapter: IconsAdapter? = null
    var recyclerViewIcons: RecyclerView? = null
    var mute = false
    var loop = true
    var parameters: PlaybackParameters? = null
    var speed = 1f
    var pictureInPicture: PictureInPictureParams.Builder? = null
    var isCrossChecked = false

    var vol_text: TextView? = null
    var brt_text:TextView? = null
    var total_duration:TextView? = null
    var audioManager: AudioManager? = null
    private val contentResolver: ContentResolver? = null
    private val window: Window? = null
    var singleTap = false

    var zoomLayout: RelativeLayout? = null
    var zoomContainer: RelativeLayout? = null
    var zoom_perc: TextView? = null
    var scaleGestureDetector: ScaleGestureDetector? = null
    private var scale_factor = 1.0f
    var double_tap = false

    //swipe and zoom variables
    private var startX = 0f
    private var startY = 0f
    private var isSeeking = false
    private lateinit var binding: ActivityVideoPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setFullScreen()
        hideBottomBar()
        supportActionBar?.hide()
        setContentView(binding.root)


        position = intent.getIntExtra("position", 0);
        mVideoFiles = intent.getStringArrayListExtra("videoArrayList")

        initViews()
        playVideo()
        binding.exoplayerView.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        if (event.pointerCount == 1) {
                            // Record the start position of the touch event
                            startX = event.getX(0)
                            startY = event.getY(0)
                            isSeeking = true
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isSeeking) {
                            if (event.pointerCount == 1) {
                                // Calculate the distance moved horizontally and vertically
                                val distanceX = event.getX(0) - startX
                                val distanceY = event.getY(0) - startY
                                // Check if the horizontal movement is greater than vertical movement and greater than a threshold
                                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > 10f) {
                                    // Calculate the seek position based on the distance moved
                                    var seekPosition = player!!.currentPosition + (distanceX * 7f).toLong()
                                    seekPosition = Math.max(0, Math.min(seekPosition, player!!.duration))
                                    // Seek to the calculated position
                                    player!!.seekTo(seekPosition)
                                }
                            }
                        } else {
                            // Scale when two fingers are down
                            scaleGestureDetector!!.onTouchEvent(event)
                        }
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (event.pointerCount == 2) {
                            // Start scaling when two fingers are down
                            isSeeking = false
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isSeeking = false
                    }
                }
                return gestureDetector.onTouchEvent(event)
            }

            private val gestureDetector = GestureDetector(binding.exoplayerView.getContext(), object : SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (double_tap) {
                            player!!.playWhenReady = true
                            double_tap = false
                        } else {
                            player!!.playWhenReady = false
                            double_tap = true
                        }
                        return super.onDoubleTap(e)
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        singleTap = if (singleTap) {
                            binding.exoplayerView.showController()
                            false
                        } else {
                            binding.exoplayerView.hideController()
                            true
                        }
                        return super.onSingleTapConfirmed(e)
                    }
                })
        })
        horizontalIconList()
    }

    fun isPiPSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

    private fun horizontalIconList() {
        if (isPiPSupported()) {
            iconModelArrayList.add(IconData(R.drawable.ic_pip_mode, "Popup"))
        }
        iconModelArrayList.add(IconData(R.drawable.ic_rotate, "Rotate"))
        iconModelArrayList.add(IconData(R.drawable.ic_loop_all, "Loop"))
        iconModelArrayList.add(IconData(R.drawable.ic_volume_off, "Mute"))
        iconModelArrayList.add(IconData(R.drawable.ic_speed, "Speed"))
        playbackIconsAdapter = IconsAdapter(this,iconModelArrayList)
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, true)

        recyclerViewIcons!!.layoutManager = layoutManager
        recyclerViewIcons!!.adapter = playbackIconsAdapter
        playbackIconsAdapter?.setOnItemClickListener(object : IconsAdapter.OnItemClickListener {
            @SuppressLint("Range")
            override fun onItemClick(position: Int) {
                if (position == 0) {
                    //popup
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        binding.exoplayerView!!.controllerAutoShow = false
                        binding.exoplayerView!!.hideController()
                        val aspectRatio = Rational(9, 16)
                        pictureInPicture!!.setAspectRatio(aspectRatio)
                        enterPictureInPictureMode(pictureInPicture!!.build())


                        // Create a new intent to bring the app to the background
                        val homeIntent = Intent(Intent.ACTION_MAIN)
                        homeIntent.addCategory(Intent.CATEGORY_HOME)
                        startActivity(homeIntent)
                    } else {
                        Log.wtf("not oreo", "yes")
                    }
                }
                if (position == 1) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        playbackIconsAdapter?.notifyDataSetChanged()
                    } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        playbackIconsAdapter?.notifyDataSetChanged()
                    }
                }
                if (position == 2) {
                    if (loop) {
                        iconModelArrayList[position] = IconData(R.drawable.ic_loop_one, "Loop")
                        playbackIconsAdapter?.notifyDataSetChanged()
                        loop = false
                    } else {
                        iconModelArrayList[position] = IconData(R.drawable.ic_loop_all, "Loop")
                        playbackIconsAdapter?.notifyDataSetChanged()
                        loop = true
                    }
                }
                if (position == 3) {
                    //mute
                    if (mute) {
                        player!!.volume = 100F
                        iconModelArrayList[position] = IconData(R.drawable.ic_volume_off, "Mute")
                        playbackIconsAdapter?.notifyDataSetChanged()
                        mute = false
                    } else {
                        player!!.volume = 0F
                        iconModelArrayList[position] = IconData(R.drawable.ic_volume, "unMute")
                        playbackIconsAdapter?.notifyDataSetChanged()
                        mute = true
                    }
                }
                if (position == 4) {
                    //speed
                    val alertDialog = AlertDialog.Builder(this@MobiVideoPlayer)
                    alertDialog.setTitle("Select Playback Speed")
                    val items = arrayOf("0.5x", "1x Normal Speed", "1.25x", "1.5x", "2x")
                    val checkedItem = -1
                    alertDialog.setSingleChoiceItems(
                        items, checkedItem
                    ) { dialog, which ->
                        when (which) {
                            0 -> {
                                speed = 0.5f
                                parameters = PlaybackParameters(speed)
                                player?.playbackParameters = parameters!!
                            }

                            1 -> {
                                speed = 1f
                                parameters = PlaybackParameters(speed)
                                player?.playbackParameters = parameters!!
                            }

                            2 -> {
                                speed = 1.25f
                                parameters = PlaybackParameters(speed)
                                player?.playbackParameters = parameters!!
                            }

                            3 -> {
                                speed = 1.5f
                                parameters = PlaybackParameters(speed)
                                player?.playbackParameters = parameters!!
                            }

                            4 -> {
                                speed = 2f
                                parameters = PlaybackParameters(speed)
                                player?.playbackParameters = parameters!!
                            }
                        }
                        dialog.dismiss() // Dismiss the dialog after the item is clicked
                    }
                    val dialog = alertDialog.create()
                    dialog.show()
                }
            }
        })
    }

    private fun initViews() {
        nextButton = findViewById(R.id.exo_play_next)
        previousButton = findViewById(R.id.exo_play_previous)
        total_duration = findViewById(R.id.exo_duration)
        title = findViewById(R.id.video_title)
        videoBack = findViewById(R.id.video_back)
        lock = findViewById(R.id.lock)
        unlock = findViewById(R.id.unlock)
        scaling = findViewById(R.id.scaling)
        root = findViewById(R.id.root_layout)
        recyclerViewIcons = findViewById(R.id.recyclerview_icon)
        vol_text = findViewById(R.id.vol_text)
        brt_text = findViewById(R.id.brt_text)
        zoomLayout = findViewById(R.id.zoom_layout)
        zoom_perc = findViewById(R.id.zoom_percentage)
        zoomContainer = findViewById(R.id.zoom_container)
        scaleGestureDetector = ScaleGestureDetector(this, ScaleDetector())
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        title?.text = File(mVideoFiles!![position]).name
        nextButton?.setOnClickListener(this)
        previousButton?.setOnClickListener(this)
        videoBack?.setOnClickListener(this)
        lock?.setOnClickListener(this)
        unlock?.setOnClickListener(this)
        scaling?.setOnClickListener(firstListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPicture = PictureInPictureParams.Builder()
        }
    }

    private fun playVideo() {
        player = SimpleExoPlayer.Builder(this).setSeekBackIncrementMs(10000)
        .setSeekForwardIncrementMs(10000).build()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"))
        concatenatingMediaSource = ConcatenatingMediaSource()
        for (i in 0 until mVideoFiles!!.size) {
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(Uri.parse(mVideoFiles!![i])))
            concatenatingMediaSource!!.addMediaSource(mediaSource)
        }
        binding.exoplayerView?.player = player
        binding.exoplayerView?.keepScreenOn = true
//        parameters = PlaybackParameters(speed)
//        player?.playbackParameters = parameters!!
        player?.repeatMode = Player.REPEAT_MODE_OFF
        player?.prepare(concatenatingMediaSource!!)
        player!!.seekTo(position, (mVideoFiles!!.size - 1).toLong())
        playError()
        player?.addListener(object : Player.Listener {
            // when video complete it moves to next one
            override fun onPositionDiscontinuity(
                oldPosition: PositionInfo,
                newPosition: PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    if (!loop) {
                        player?.stop()
                        playVideo()
                        return
                    }
                    position++
                }
                Log.d("vhdf", reason.toString())
                if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                    if (position == mVideoFiles!!.size - 1) {
                        if (!loop) {
                            player?.stop()
                            playVideo()
                            return
                        }
                        player?.stop()
                        position = 0
                        playVideo()
                    }
                }
            }
        })
        binding.exoplayerView?.setControllerVisibilityListener { visibility ->
            if (visibility == View.GONE) {
                singleTap = false
            }
            if (visibility == View.VISIBLE) {
                singleTap = true
            }
        }
        playError()
    }

    private fun playError() {

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@MobiVideoPlayer, "Video Playing Error", Toast.LENGTH_SHORT).show()
            }
        })
        player?.playWhenReady = true
    }

    override fun onBackPressed() {
        if (player != null) {
            player?.release()
        }
        super.onBackPressed()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
        player?.playbackState
        if (isInPictureInPictureMode) {
            player?.playWhenReady = true
        } else {
            player?.playWhenReady = false
            player?.playbackState
        }
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
        player?.playbackState
    }

    override fun onRestart() {
        super.onRestart()
        player?.playWhenReady = true
        player?.playbackState
    }

    private fun setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun hideBottomBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            val v = getWindow().decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            val decodeView = getWindow().decorView
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decodeView.systemUiVisibility = uiOptions
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.video_back -> {
                if (player != null) {
                    player?.release()
                }
                finish()
            }

            R.id.lock -> {
                root?.visibility = View.VISIBLE
                lock?.visibility = View.INVISIBLE
                Toast.makeText(this, "unLocked", Toast.LENGTH_SHORT).show()
            }

            R.id.unlock -> {
                root?.visibility = View.INVISIBLE
                lock?.visibility = View.VISIBLE
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show()
            }

            R.id.exo_play_next -> try {
                if (position == mVideoFiles!!.size - 1) {
                    position = -1
                }
                player?.stop()
                position++
                playVideo()
                title?.text = File(mVideoFiles!![position]).name
            } catch (e: Exception) {
                Toast.makeText(this, "no Next Video", Toast.LENGTH_SHORT).show()
                finish()
            }

            R.id.exo_play_previous -> try {
                if (position == 0) return
                player?.stop()
                position--
                playVideo()
                title?.text = File(mVideoFiles!![position]).name
            } catch (e: Exception) {
                Toast.makeText(this, "no Previous Video", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private var firstListener : View.OnClickListener = View.OnClickListener {
        binding.exoplayerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
        scaling?.setImageResource(R.drawable.ic_fill)
        Toast.makeText(this@MobiVideoPlayer, "Full Screen", Toast.LENGTH_SHORT).show()
        scaling?.setOnClickListener(secondListener)
    }
    private var secondListener : View.OnClickListener = View.OnClickListener {
        binding.exoplayerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
        scaling?.setImageResource(R.drawable.ic_zoom)
        Toast.makeText(this@MobiVideoPlayer, "Zoom", Toast.LENGTH_SHORT).show()
        scaling?.setOnClickListener(thirdListener)
    }
    private var thirdListener : View.OnClickListener = View.OnClickListener {
        binding.exoplayerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
        scaling?.setImageResource(R.drawable.ic_fit)
        Toast.makeText(this@MobiVideoPlayer, "Fit", Toast.LENGTH_SHORT).show()
        scaling?.setOnClickListener(firstListener)
    }



    override fun onStop() {
        super.onStop()
        if (isCrossChecked) {
            player?.release()
            finish()
        }
    }

    inner class ScaleDetector : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale_factor *= detector.scaleFactor
            scale_factor = Math.max(0.5f, Math.min(scale_factor, 6.0f))
            zoomLayout?.setScaleX(scale_factor)
            zoomLayout?.setScaleY(scale_factor)
            val percentage = (scale_factor * 100)
            zoom_perc?.setText(" ${percentage.toInt()}%")
            zoomContainer?.setVisibility(View.VISIBLE)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            zoomContainer?.setVisibility(View.GONE)
            super.onScaleEnd(detector)
        }
    }
}