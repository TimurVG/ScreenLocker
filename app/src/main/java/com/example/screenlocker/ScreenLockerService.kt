class ScreenLockerService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var isLocked = false
    private val gestureDetector: GestureDetectorCompat
    private val circleGestureListener = CircleGestureListener()

    init {
        gestureDetector = GestureDetectorCompat(applicationContext, object : GestureDetector.SimpleOnGestureListener())
    }

    override fun onCreate() {
        super.onCreate()
        setupOverlay()
        createNotificationChannel()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LockOverlayView(this).apply {
            setOnTouchListener { _, event ->
                if (gestureDetector.onTouchEvent(event)) {
                    return@setOnTouchListener true
                }
                circleGestureListener.handleEvent(event)
                true
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) TYPE_APPLICATION_OVERLAY else TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayView, params)
    }

    inner class CircleGestureListener {
        private val circles = mutableListOf<Circle>()
        private var lastCircleTime = 0L

        fun handleEvent(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    circles.clear()
                    circles.add(Circle(event.x, event.y))
                }
                MotionEvent.ACTION_MOVE -> {
                    circles.last().addPoint(event.x, event.y)
                    checkForUnlockGesture()
                }
                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - lastCircleTime < 500) return
                    if (circles.size == 1 && circles[0].isComplete() && !isLocked) {
                        lockScreen()
                    }
                }
            }
        }

        private fun checkForUnlockGesture() {
            if (circles.size >= 2 && circles.all { it.isComplete() } && isLocked) {
                unlockScreen()
            }
        }

        private fun lockScreen() {
            isLocked = true
            (overlayView as LockOverlayView).setLockState(true)
        }

        private fun unlockScreen() {
            isLocked = false
            (overlayView as LockOverlayView).setLockState(false)
            circles.clear()
        }
    }

    // ... остальные методы сервиса (onDestroy, onBind и т.д.)
}