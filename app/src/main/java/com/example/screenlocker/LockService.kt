class LockService : Service() {
    private val vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LOCK -> activateLock()
            ACTION_UNLOCK -> deactivateLock()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun activateLock() {
        if (overlayView == null) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_lock, null).apply {
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        checkUnlockTap(event.x, event.y)
                    }
                    true
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply { alpha = 0.05f }

            windowManager.addView(overlayView, params)
            vibrate(300)
        }
    }

    private fun deactivateLock() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
            vibrate(300)
        }
    }

    private fun checkUnlockTap(x: Float, y: Float) {
        if (x < 100 && y < 100) { // Леый верхний угол 100x100
            tapCounter++
            if (tapCounter >= 4) {
                deactivateLock()
                tapCounter = 0
            }
        } else {
            tapCounter = 0
        }
    }

    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    override fun onDestroy() {
        deactivateLock()
        super.onDestroy()
    }

    companion object {
        const val ACTION_LOCK = "LOCK"
        const val ACTION_UNLOCK = "UNLOCK"
        const val ACTION_STOP = "STOP"
        var tapCounter = 0
    }
}