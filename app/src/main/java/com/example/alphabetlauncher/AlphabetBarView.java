package com.example.alphabetlauncher;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.HashSet;
import java.util.Set;


/**
 * Custom view responsible for drawing and controlling
 * the A-Z alphabet bar.
 */
public final class AlphabetBarView extends View {


    /*
     * Listener used to communicate with MainActivity.
     */
    public interface Listener {

        /*
         * Called whenever the selected letter changes.
         */
        void onLetterChanged(char letter);


        /*
         * Called when the user starts touching the bar.
         */
        void onDragStarted();


        /*
         * Called when the user releases the finger.
         */
        void onDragFinished();


        /*
         * Called when the top star icon is clicked.
         */
        void onStarClicked();
    }


    /*
     * Paint object used for all drawing.
     */
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);


    /*
     * The alphabet.
     */
    private final String letters =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    /*
     * Activity/listener.
     */
    private final Listener listener;


    /*
     * Screen density.
     *
     * Android phones have different screen densities.
     * We convert dp to pixels using this value.
     */
    private final float density;


    /*
     * Android vibrator.
     */
    private final Vibrator vibrator;


    /*
     * Current finger Y position.
     *
     * -1 means there is currently
     * no finger on the bar.
     */
    private float fingerY = -1f;


    /*
     * Current bending amount.
     */
    private float bend = 0f;


    /*
     * Target bending amount.
     */
    private float targetBend = 0f;


    /*
     * Currently selected letter.
     */
    private char selectedLetter = 'A';


    /*
     * Is the user currently dragging?
     */
    private boolean dragging = false;


    /*
     * Animator used when the user releases
     * the alphabet bar.
     */
    private ValueAnimator releaseAnimator;


    /*
     * Used to prevent excessive vibration.
     */
    private long lastHapticTime = 0L;


    /*
     * Dark mode flag.
     */
    private boolean isDarkMode = false;


    /*
     * Letters that contain at least one installed app.
     */
    private Set<Character> activeLetters = new HashSet<>();


    /**
     * Configure dark mode colors.
     */
    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        invalidate();
    }


    /**
     * Update active letters with installed apps.
     */
    public void setActiveLetters(Set<Character> active) {
        if (active != null) {
            this.activeLetters = active;
            invalidate();
        }
    }


    /**
     * Constructor.
     */
    public AlphabetBarView(
            Context context,
            Listener listener
    ) {

        super(context);


        /*
         * Save listener.
         */
        this.listener = listener;


        /*
         * Get screen density.
         */
        density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        /*
         * We draw everything ourselves.
         */
        setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
        );


        /*
         * Get Android's vibrator service.
         */
        if (Build.VERSION.SDK_INT >= 31) {

            VibratorManager vibratorManager =
                    (VibratorManager)
                            context.getSystemService(
                                    Context.VIBRATOR_MANAGER_SERVICE
                            );


            if (vibratorManager != null) {

                vibrator =
                        vibratorManager
                                .getDefaultVibrator();

            } else {

                vibrator = null;
            }

        } else {

            vibrator =
                    (Vibrator)
                            context.getSystemService(
                                    Context.VIBRATOR_SERVICE
                            );
        }
    }


    /**
     * Convert dp to pixels.
     */
    private float dp(float value) {

        return value * density;
    }


    /**
     * Draw the complete alphabet bar.
     */
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);


        /*
         * Top position of A.
         */
        float top =
                dp(52);


        /*
         * Bottom position of Z.
         */
        float bottom =
                getHeight() - dp(36);


        /*
         * X coordinate of the normal
         * straight alphabet bar.
         */
        float centerX =
                getWidth() - dp(24);


        /*
         * Total vertical distance.
         */
        float usableHeight =
                Math.max(
                        dp(1),
                        bottom - top
                );


        /*
         * There are 26 letters.
         *
         * 25 intervals exist between
         * A and Z.
         */
        float step =
                usableHeight / 25f;


        // --------------------------------------------------
        // STAR
        // --------------------------------------------------

        paint.setTypeface(
                Typeface.DEFAULT_BOLD
        );

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        paint.setTextSize(
                dp(18)
        );

        paint.setColor(
                isDarkMode ? 0xFFFFFFFF : 0xFF18181C
        );


        /*
         * Draw star.
         */
        canvas.drawText(
                "★",
                centerX,
                dp(29),
                paint
        );


        // --------------------------------------------------
        // A-Z LETTERS
        // --------------------------------------------------

        for (int i = 0;
             i < letters.length();
             i++) {


            /*
             * Current letter.
             */
            char letter =
                    letters.charAt(i);


            /*
             * Normal Y position.
             */
            float baseY =
                    top + i * step;


            /*
             * Horizontal movement.
             *
             * Normally this is zero.
             */
            float shift = 0f;


            /*
             * Only bend when the user
             * is touching the bar.
             */
            if (dragging &&
                    fingerY >= 0) {


                /*
                 * Calculate vertical distance
                 * between this letter and finger.
                 */
                float distance =
                        (baseY - fingerY)
                                / dp(115);


                /*
                 * Gaussian falloff.
                 *
                 * Letters close to the finger
                 * move a lot.
                 *
                 * Letters far away move very little.
                 */
                float influence =
                        (float)
                                Math.exp(
                                        -(distance * distance)
                                );


                /*
                 * Move letters toward
                 * the left side.
                 */
                shift =
                        -targetBend
                                * influence;
            }


            /*
             * Final X position.
             */
            float x =
                    centerX + shift;


            /*
             * Bold font for active/selected letter.
             */
            boolean isActive = activeLetters.contains(letter);

            paint.setTypeface(
                    isActive ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT
            );


            /*
             * Letter size.
             */
            paint.setTextSize(
                    dp(11)
            );


            /*
             * Color based on state and dark mode.
             */
            if (letter == selectedLetter && dragging) {

                paint.setColor(
                        isDarkMode ? 0xFFFFFFFF : 0xFF000000
                );

            } else if (isActive) {

                paint.setColor(
                        isDarkMode ? 0xFFDDDDDF : 0xFF333338
                );

            } else {

                paint.setColor(
                        isDarkMode ? 0x40888899 : 0x35AAAAAA
                );
            }


            /*
             * Draw letter.
             */
            canvas.drawText(
                    String.valueOf(letter),
                    x,
                    baseY,
                    paint
            );
        }


        // --------------------------------------------------
        // BOTTOM DOT
        // --------------------------------------------------

        paint.setColor(
                isDarkMode ? 0xFF666677 : 0xFFAAAAAA
        );


        canvas.drawCircle(
                centerX,
                getHeight() - dp(16),
                dp(2.5f),
                paint
        );


        // --------------------------------------------------
        // LETTER BUBBLE
        // --------------------------------------------------

        if (dragging &&
                fingerY >= 0) {


            /*
             * Bubble X coordinate.
             *
             * It appears to the left
             * of the finger/bar.
             */
            float bubbleX =
                    Math.max(
                            dp(45),
                            centerX - dp(72) - bend
                    );


            /*
             * Keep bubble inside screen.
             */
            float bubbleY =
                    Math.max(
                            dp(42),
                            Math.min(
                                    getHeight() - dp(42),
                                    fingerY
                            )
                    );


            /*
             * Bubble background color.
             */
            paint.setColor(
                    isDarkMode ? 0xFFF0F0F5 : 0xFF18181C
            );


            /*
             * Small shadow.
             */
            paint.setShadowLayer(
                    dp(10),
                    0,
                    dp(4),
                    isDarkMode ? 0x60000000 : 0x30000000
            );


            /*
             * Draw circular bubble.
             */
            canvas.drawCircle(
                    bubbleX,
                    bubbleY,
                    dp(28),
                    paint
            );


            /*
             * Remove shadow.
             */
            paint.clearShadowLayer();


            /*
             * Bubble letter color.
             */
            paint.setColor(
                    isDarkMode ? 0xFF121216 : 0xFFFFFFFF
            );


            paint.setTypeface(
                    Typeface.DEFAULT_BOLD
            );


            paint.setTextSize(
                    dp(22)
            );


            /*
             * Draw selected letter
             * inside the bubble.
             */
            canvas.drawText(
                    String.valueOf(selectedLetter),
                    bubbleX,
                    bubbleY + dp(8),
                    paint
            );
        }
    }


    /**
     * Receive touch events.
     */
    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {


        switch (event.getActionMasked()) {


            // ------------------------------------------
            // FINGER DOWN
            // ------------------------------------------

            case MotionEvent.ACTION_DOWN:

                /*
                 * Only start dragging if the finger
                 * is close to the alphabet bar.
                 */
                if (isNearBar(event.getX())) {

                    if (event.getY() < dp(42)) {
                        hapticTick();
                        listener.onStarClicked();
                        return true;
                    }

                    startDrag(
                            event.getY()
                    );

                    return true;
                }

                return false;


            // ------------------------------------------
            // FINGER MOVING
            // ------------------------------------------

            case MotionEvent.ACTION_MOVE:

                if (dragging) {

                    updateFinger(
                            event.getY()
                    );

                    return true;
                }

                return false;


            // ------------------------------------------
            // FINGER RELEASE
            // ------------------------------------------

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:

                if (dragging) {

                    finishDrag();

                    return true;
                }

                return false;
        }


        return true;
    }


    /**
     * Checks whether the touch occurred
     * near the alphabet bar.
     */
    private boolean isNearBar(float x) {

        return x >
                getWidth() - dp(72);
    }


    /**
     * Start dragging.
     */
    private void startDrag(float y) {


        /*
         * Set dragging state.
         */
        dragging = true;


        /*
         * Save finger position.
         */
        fingerY =
                clamp(
                        y,
                        dp(42),
                        getHeight() - dp(28)
                );


        /*
         * Amount of horizontal bending.
         */
        targetBend =
                dp(82);


        /*
         * Apply immediately.
         */
        bend =
                targetBend;


        /*
         * Find selected letter.
         */
        selectedLetter =
                letterForY(
                        fingerY
                );


        /*
         * Tell MainActivity that
         * dragging has started.
         */
        listener.onDragStarted();


        /*
         * Tell MainActivity which letter
         * was selected.
         */
        listener.onLetterChanged(
                selectedLetter
        );


        /*
         * Redraw immediately.
         */
        invalidate();
    }


    /**
     * Update finger position while dragging.
     */
    private void updateFinger(float y) {


        /*
         * Keep finger inside the usable
         * alphabet area.
         */
        fingerY =
                clamp(
                        y,
                        dp(42),
                        getHeight() - dp(28)
                );


        /*
         * Calculate the letter under
         * the finger.
         */
        char newLetter =
                letterForY(
                        fingerY
                );


        /*
         * Only update if the letter changed.
         */
        if (newLetter != selectedLetter) {


            /*
             * Save new letter.
             */
            selectedLetter =
                    newLetter;


            /*
             * Tell MainActivity.
             */
            listener.onLetterChanged(
                    selectedLetter
            );


            /*
             * Small haptic tick.
             */
            hapticTick();
        }


        /*
         * Redraw the curve.
         */
        invalidate();
    }


    /**
     * Finish dragging and animate
     * the alphabet back to straight.
     */
    private void finishDrag() {


        /*
         * Disable dragging.
         */
        dragging = false;


        /*
         * Remove finger.
         */
        fingerY = -1f;


        /*
         * Stop previous animator
         * if one exists.
         */
        if (releaseAnimator != null) {

            releaseAnimator.cancel();
        }


        /*
         * Remember starting bend.
         */
        final float start =
                bend;


        /*
         * Animate:
         *
         * current bend -> 0
         */
        releaseAnimator =
                ValueAnimator.ofFloat(
                        start,
                        0f
                );


        /*
         * Animation duration.
         */
        releaseAnimator.setDuration(
                240
        );


        /*
         * Smooth deceleration.
         */
        releaseAnimator.setInterpolator(
                new DecelerateInterpolator()
        );


        /*
         * Update bend during animation.
         */
        releaseAnimator.addUpdateListener(
                animation -> {

                    bend =
                            (float)
                                    animation
                                            .getAnimatedValue();


                    /*
                     * Redraw.
                     */
                    invalidate();
                }
        );


        /*
         * Start animation.
         */
        releaseAnimator.start();


        /*
         * Tell MainActivity that
         * dragging has ended.
         */
        listener.onDragFinished();


        /*
         * Redraw.
         */
        invalidate();
    }


    /**
     * Convert Y position to A-Z letter.
     */
    private char letterForY(float y) {


        /*
         * Position where A starts.
         */
        float top =
                dp(52);


        /*
         * Position where Z ends.
         */
        float bottom =
                getHeight() - dp(36);


        /*
         * Convert Y position into
         * a 0.0 -> 1.0 ratio.
         */
        float ratio =
                (y - top)
                        / Math.max(
                        dp(1),
                        bottom - top
                );


        /*
         * Convert ratio into
         * alphabet index 0 -> 25.
         */
        int index =
                Math.round(
                        ratio * 25f
                );


        /*
         * Prevent going outside A-Z.
         */
        index =
                Math.max(
                        0,
                        Math.min(
                                25,
                                index
                        )
                );


        /*
         * Return corresponding letter.
         */
        return letters.charAt(
                index
        );
    }


    /**
     * Trigger a small vibration when
     * selected letter changes.
     */
    private void hapticTick() {


        /*
         * Current time.
         */
        long now =
                System.currentTimeMillis();


        /*
         * Don't vibrate too frequently.
         */
        if (vibrator == null ||
                now - lastHapticTime < 35) {

            return;
        }


        /*
         * Save vibration time.
         */
        lastHapticTime =
                now;


        /*
         * Android 10+ predefined tick.
         */
        if (Build.VERSION.SDK_INT >= 29) {

            vibrator.vibrate(
                    VibrationEffect
                            .createPredefined(
                                    VibrationEffect.EFFECT_TICK
                            )
            );


            /*
             * Android 8+ vibration.
             */
        } else if (Build.VERSION.SDK_INT >= 26) {

            vibrator.vibrate(
                    VibrationEffect
                            .createOneShot(
                                    10,
                                    VibrationEffect
                                            .DEFAULT_AMPLITUDE
                            )
            );


            /*
             * Older Android.
             */
        } else {

            vibrator.vibrate(
                    10
            );
        }
    }


    /**
     * Keep a value between minimum and maximum.
     */
    private float clamp(
            float value,
            float min,
            float max
    ) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }
}
