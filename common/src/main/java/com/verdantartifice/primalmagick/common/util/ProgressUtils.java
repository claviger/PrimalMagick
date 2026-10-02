package com.verdantartifice.primalmagick.common.util;

/**
 * Collection of utility methods for scaling progress values to the size of a GUI meter.
 */
public class ProgressUtils {
    /**
     * Scale a progress value to the size of a meter, clamped so that the result never exceeds the meter's full size.
     * Overflowing progress (e.g. 200 of 50) yields a full meter, and a zero or negative maximum yields an empty one
     * rather than dividing by zero.
     *
     * @param current the current progress value
     * @param max the progress value at which the meter is full
     * @param size the full size of the meter, in pixels
     * @return the number of pixels of the meter to fill, between zero and the given size inclusive
     */
    public static int scaledProgress(long current, long max, int size) {
        if (max <= 0L || current <= 0L || size <= 0) {
            return 0;
        } else if (current >= max) {
            return size;
        } else {
            return (int)(current * size / max);
        }
    }
}
