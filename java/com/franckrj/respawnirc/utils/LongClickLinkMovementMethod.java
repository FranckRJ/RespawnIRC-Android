package com.franckrj.respawnirc.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;


public class LongClickLinkMovementMethod extends LinkMovementMethod {
    private static final int LONG_CLICK_TIME = 600;
    private static final long[] LONG_PRESS_VIBRATE_PATTERN = {0, 1, 20, 21};

    private static LongClickLinkMovementMethod instance;

    private Handler longClickHandler;
    private boolean itsLongPress = false;

    @Override
    public boolean onTouchEvent(final TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL) {
            if (longClickHandler != null) {
                longClickHandler.removeCallbacksAndMessages(null);
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            final LongClickableSpan[] link = buffer.getSpans(off, off, LongClickableSpan.class);

            if (link.length > 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if (longClickHandler != null) {
                        longClickHandler.removeCallbacksAndMessages(null);
                    }
                    if (!itsLongPress) {
                        link[0].onClick(widget);
                    }
                    itsLongPress = false;
                } else {
                    longClickHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibratorService = (Vibrator) widget.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                            Undeprecator.vibratorVibrate(vibratorService, LONG_PRESS_VIBRATE_PATTERN, -1);
                            link[0].onLongClick(widget);
                            itsLongPress = true;
                        }
                    }, LONG_CLICK_TIME);
                }
                return true;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        if (instance == null) {
            instance = new LongClickLinkMovementMethod();
            instance.longClickHandler = new Handler();
        }

        return instance;
    }
}
