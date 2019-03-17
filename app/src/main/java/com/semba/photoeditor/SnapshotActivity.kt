//package com.semba.photoeditor
//
//import android.Manifest
//import android.app.Activity
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Matrix
//import android.graphics.drawable.BitmapDrawable
//import android.graphics.drawable.Drawable
//import android.hardware.Camera
//import android.hardware.camera2.CameraDevice
//import android.hardware.SensorManager
//import android.support.v7.app.AppCompatActivity
//import android.os.Bundle
//import android.os.Environment
//import android.os.Handler
//import android.util.Log
//import android.view.*
//import android.widget.SeekBar
//import android.widget.Toast
//import kotlinx.android.synthetic.main.activity_snapshot.*
//import java.io.*
//
//enum class CameraMode
//{
//    Capture,
//    Review
//}
//
//enum class GalleryMode
//{
//    Capture,
//    Review
//}
//
//class SnapshotActivity : AppCompatActivity() {
//
//    //Use Camera2 instead of Camera
//    var camera: CameraDevice? = null
//    var surfaceHolder: SurfaceHolder? = null
//    var rawCallback: Camera.PictureCallback? = null
//    var shutterCallback: Camera.ShutterCallback? = null
//    var jpegCallback: Camera.PictureCallback? = null
//    private var inPreview = false
//    private var cameraConfigured = false
//    private var galleryPath = ""
//    private var currentGalleryState = GalleryMode.Capture
//    private var flashWorks: Boolean = false
//    private var snapData: ByteArray? = null
//
//    private var mOrientationEventListener: OrientationEventListener? = null
//    private var mOrientation = -1
//    private var rotationFactor = 0f
//
//    private val ORIENTATION_PORTRAIT_NORMAL = 1
//    private val ORIENTATION_PORTRAIT_INVERTED = 2
//    private val ORIENTATION_LANDSCAPE_NORMAL = 3
//    private val ORIENTATION_LANDSCAPE_INVERTED = 4
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_snapshot)
//
//        initViews()
//        handleOptions()
//    }
//
//    private fun initViews() {
//        galleryPath = android.os.Environment.DIRECTORY_DCIM
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        try {
//            camera = Camera.open()
//            startPreview()
//        } catch (e: RuntimeException) {
//            Log.e("", "init_camera: " + e)
//            return
//        }
//
//        mOrientationEventListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
//            override fun enable() {
//                super.enable()
//            }
//
//            override fun canDetectOrientation(): Boolean {
//                return super.canDetectOrientation()
//            }
//
//            override fun disable() {
//                super.disable()
//            }
//
//            override fun onOrientationChanged(orientation: Int) {
//
//                val lastOrientation = mOrientation
//
//                val display = ((getSystemService(WINDOW_SERVICE)) as WindowManager).getDefaultDisplay()
//
//                if (display.getOrientation() == Surface.ROTATION_0) {   // landscape oriented devices
//                    if (orientation >= 315 || orientation < 45) {
//                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
//                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL
//                        }
//                    } else if (orientation < 315 && orientation >= 225) {
//                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
//                            mOrientation = ORIENTATION_PORTRAIT_INVERTED
//                        }
//                    } else if (orientation < 225 && orientation >= 135) {
//                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
//                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED
//                        }
//                    } else if (orientation < 135 && orientation > 45) {
//                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
//                            mOrientation = ORIENTATION_PORTRAIT_NORMAL
//                        }
//                    }
//                } else {  // portrait oriented devices
//                    if (orientation >= 315 || orientation < 45) {
//                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
//                            mOrientation = ORIENTATION_PORTRAIT_NORMAL
//                        }
//                    } else if (orientation < 315 && orientation >= 225) {
//                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
//                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL
//                        }
//                    } else if (orientation < 225 && orientation >= 135) {
//                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
//                            mOrientation = ORIENTATION_PORTRAIT_INVERTED
//                        }
//                    } else if (orientation < 135 && orientation > 45) {
//                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
//                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED
//                        }
//                    }
//                }
//
//                if (lastOrientation != mOrientation) {
//                    changeRotation(mOrientation, lastOrientation)
//                }
//            }
//        }
//
//        if (mOrientationEventListener?.canDetectOrientation() ?: false) {
//            mOrientationEventListener?.enable()
//        }
//
//    }
//
//    private fun changeRotation(orientation: Int, lastOrientation: Int) {
//        camera?.setDisplayOrientation(90)
//        when (orientation) {
//            ORIENTATION_PORTRAIT_NORMAL -> {
//                event_camera_capture.rotation = 270f
//                rotationFactor = 180f
//                Log.v("CameraActivity", "Orientation = 90")
//            }
//            ORIENTATION_LANDSCAPE_NORMAL -> {
//                event_camera_capture.rotation = 0f
//                rotationFactor = 90f
//                Log.v("CameraActivity", "Orientation = 0")
//            }
//            ORIENTATION_PORTRAIT_INVERTED -> {
//                event_camera_capture.rotation = 90f
//                rotationFactor = 0f
//                Log.v("CameraActivity", "Orientation = 270")
//            }
//            ORIENTATION_LANDSCAPE_INVERTED -> {
//                event_camera_capture.rotation = 180f
//                rotationFactor = -90f
//                Log.v("CameraActivity", "Orientation = 180")
//            }
//        }
//    }
//
//    private fun zoomCamera(progress: Int) {
//        if (camera != null) {
//            val parameter = camera?.parameters
//            val zoomIn = if (parameter?.zoom ?: 0 < progress * 4) true else false
//
//            if (parameter?.isZoomSupported ?: false) {
//                val MAX_ZOOM = parameter?.maxZoom ?: 0
//                val currentZoom = parameter?.zoom ?: 0
//                if (zoomIn && (currentZoom + 4 < MAX_ZOOM && currentZoom >= 0)) {
//                    parameter?.zoom = currentZoom + 4
//                } else if (!zoomIn && (currentZoom <= MAX_ZOOM && currentZoom - 4 > 0)) {
//                    parameter?.zoom = currentZoom - 4
//                }
//            } else
//                Toast.makeText(this, "Zoom Not Available", Toast.LENGTH_LONG).show()
//
//            camera?.parameters = parameter
//        }
//    }
//
//    private fun getRotatedImage(drawableId: Int, degrees: Int): Drawable {
//        val original = BitmapFactory.decodeResource(resources, drawableId)
//        val matrix = Matrix()
//        matrix.postRotate(degrees.toFloat())
//
//        val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true);
//        return BitmapDrawable(rotated)
//    }
//
//    override fun onPause() {
//        stop_camera()
//        super.onPause()
//
//        mOrientationEventListener?.disable();
//    }
//
//    private fun handleOptions() {
//        //updateGallery()
//        surfaceHolder = event_camera_surface.holder
//        surfaceHolder?.addCallback(surfaceCallback)
//        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
//
//        event_camera_zoom_slider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                zoomCamera(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                zoomCamera(seekBar?.progress ?: 0)
//            }
//        })
//
//        rawCallback = object : Camera.PictureCallback {
//
//            override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
//
//            }
//        };
//
//        shutterCallback = Camera.ShutterCallback {
//
//        };
//
//        jpegCallback = object : Camera.PictureCallback {
//            override fun onPictureTaken(data: ByteArray, camera: Camera) {
//
//                snapData = data
//                changeCameraMode(CameraMode.Review)
//
//            }
//        }
//
//        event_camera_flash_btn?.setOnClickListener {
//            val parameters = camera?.parameters
//            if (flashWorks) {
//                event_camera_flash_btn?.setImageResource(R.drawable.flash_off)
//                parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
//                flashWorks = false
//            } else {
//                event_camera_flash_btn?.setImageResource(R.drawable.flash)
//                parameters?.flashMode = Camera.Parameters.FLASH_MODE_ON
//                flashWorks = true
//            }
//            camera?.parameters = parameters
//        }
//
//        event_camera_capture.setOnClickListener {
//            event_camera_capture.visibility = View.GONE
//            captureImage()
//        }
//
//        event_camera_cancel.setOnClickListener {
//            if (currentGalleryState == GalleryMode.Capture) {
//                changeCameraMode(CameraMode.Capture)
//            } else {
//                currentGalleryState = GalleryMode.Capture
//            }
//        }
//
//        event_camera_save.setOnClickListener {
//            if (currentGalleryState == GalleryMode.Capture) {
//                tryToSaveSnapImage()
//            } else {
//                Shared.makeToast(R.string.failed_retry)
//                currentGalleryState = GalleryMode.Capture
//                changeCameraMode(CameraMode.Capture)
//            }
//        }
//    }
//
//    private fun tryToSaveSnapImage()
//    {
//        if (snapData != null) {
//            event_camera_save_layout?.visibility = View.GONE
//            event_camera_progress_layout?.visibility = View.VISIBLE
//
//            if (Shared.MediaPermission) {
//
//                Handler().post {
//                    performSavingSnapshot()
//                }
//            }
//            else
//            {
//                Shared.setPermissions(this@SnapshotActivity,arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),this@EventSnapshot.OnGalleryPermissionDone)
//            }
//        }
//        else
//        {
//            Shared.makeToast(R.string.failed_retry)
//            currentGalleryState = GalleryMode.Capture
//            changeCameraMode(CameraMode.Capture)
//        }
//    }
//
//    fun performSavingSnapshot() = runBlocking(Dispatchers.Default) {
//        async {
//            val outStream: FileOutputStream?
//            try {
//                val photo = File(Environment.getExternalStorageDirectory().absolutePath + "/Madinaty")
//
//                var success = true
//                if (!photo.exists()) {
//                    success = photo.mkdirs()
//                }
//
//                if (success) {
//                    outStream = FileOutputStream(photo.absolutePath + String.format("/Problem_%d.jpg", System.currentTimeMillis()))
//                    outStream.write(snapData)
//                    outStream.close()
//
//                    val matrix = Matrix()
//                    matrix.postRotate(rotationFactor)
//
//                    val out = ByteArrayOutputStream()
//                    val bitmap = BitmapFactory.decodeByteArray(snapData, 0, snapData!!.size)
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
//                    val decoded = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
//                    ImagesController.bitmaps.add(Bitmap.createBitmap(decoded, 0, 0, bitmap.width, bitmap.height, matrix, false))
//                    //updateGallery()
//                    snapData = null
//                    returnDone()
//                } else {
//
//                }
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } finally {
//
//            }
//        }
//    }
//
//    val OnGalleryPermissionDone = object : OnPermissionCompleted
//    {
//        override fun onPermissionCompleted(perms:Array<String>) {
//
//            if(perms.any{it.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)}) {
//                Shared.MediaPermission = true
//                tryToSaveSnapImage()
//            }
//        }
//    }
//
//    private fun changeCameraMode(cameraMode: CameraMode) {
//
//        if (cameraMode == CameraMode.Capture)
//        {
//            event_camera_save_layout.visibility = View.GONE
//            event_camera_capture.visibility = View.VISIBLE
//            event_camera_parameters_layout?.visibility = View.VISIBLE
//            camera?.startPreview()
//        }
//        else if (cameraMode == CameraMode.Review)
//        {
//            event_camera_capture.visibility = View.GONE
//            event_camera_save_layout.visibility = View.VISIBLE
//            event_camera_parameters_layout?.visibility = View.GONE
//            camera?.stopPreview()
//        }
//    }
//
//    private fun captureImage() {
//        camera?.takePicture(shutterCallback, rawCallback, jpegCallback)
//    }
//
//    private fun getBestPreviewSize(width: Int, height: Int,
//                                   parameters: Camera.Parameters): Camera.Size? {
//        val result: Camera.Size? = parameters.supportedPreviewSizes[0]
//        result?.width = 640
//        result?.height = 480
//
//        return result
//    }
//
//    private fun initPreview(width: Int, height: Int) {
//        if (camera != null && surfaceHolder?.surface != null) {
//            try {
//                camera?.setPreviewDisplay(surfaceHolder)
//            } catch (t: Throwable) {
//                //Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t)
//                Toast.makeText(this@SnapshotActivity, t.message, Toast.LENGTH_LONG).show()
//            }
//
//            if (!cameraConfigured) {
//                val parameters = camera?.parameters
//                event_camera_zoom_slider?.max = (parameters?.maxZoom ?: 0) / 4
//                event_camera_zoom_slider?.progress = parameters?.zoom ?: 0
//                if (parameters != null) {
//                    val size = getBestPreviewSize(width, height, parameters)
//
//                    if (size != null) {
//                        parameters.setPreviewSize(size.width, size.height)
//                        camera?.parameters = parameters
//                        cameraConfigured = true
//                    }
//                }
//            }
//        }
//    }
//
//    private fun startPreview() {
//        if (cameraConfigured && camera != null) {
//            camera?.startPreview()
//            inPreview = true
//        }
//    }
//
//    var surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
//        override fun surfaceCreated(holder: SurfaceHolder) {
//            // no-op -- wait until surfaceChanged()
//        }
//
//        override fun surfaceChanged(holder: SurfaceHolder,
//                                    format: Int, width: Int,
//                                    height: Int) {
//            initPreview(width, height)
//            startPreview()
//        }
//
//        override fun surfaceDestroyed(holder: SurfaceHolder) {
//            // no-op
//        }
//    }
//
//    private fun stop_camera() {
//        if (inPreview) {
//            camera?.stopPreview();
//        }
//
//        camera?.release();
//        camera=null;
//        inPreview=false;
//    }
//
//    private fun returnDone()
//    {
//        setResult(Activity.RESULT_OK)
//        finish()
//        overridePendingTransition(R.transition.fadeout,R.transition.fadein)
//    }
//
//    override fun onBackPressed() {
//        setResult(Activity.RESULT_CANCELED)
//        super.onBackPressed()
//        overridePendingTransition(R.transition.fadeout,R.transition.fadein)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//    }
//}
