package com.verdantartifice.primalmagick.test.util;

import com.verdantartifice.primalmagick.common.util.ProgressUtils;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Tests for the progress scaling used by GUI meters such as the grimoire's expertise progress bar. Progress past the
 * maximum must fill the meter exactly rather than overflowing it, and a zero maximum must yield an empty meter rather
 * than dividing by zero.
 */
public class ProgressUtilsTests extends AbstractBaseTest {
    // Width of the grimoire's requirement progress bars, in pixels
    protected static final int BAR_WIDTH = 16;

    public static void scaled_progress(GameTestHelper helper, int current, int max, int expected) {
        scaled_progress(helper, current, max, BAR_WIDTH, expected);
    }

    public static void scaled_progress(GameTestHelper helper, int current, int max, int size, int expected) {
        int actual = ProgressUtils.scaledProgress(current, max, size);
        assertValueEqual(helper, expected, actual, "Unexpected scaled progress for " + current + "/" + max + " at size " + size);
        helper.succeed();
    }
}
