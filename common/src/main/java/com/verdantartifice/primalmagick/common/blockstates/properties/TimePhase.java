package com.verdantartifice.primalmagick.common.blockstates.properties;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.timeline.Timelines;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Representation of the current phase of a block that phases in and out over time.
 * 
 * @author Daedalus4096
 */
public enum TimePhase implements StringRepresentable {
    FULL("full", 10),        // The block is fully phased in; use full light
    WAXING("waxing", 5),    // The block is mostly phased in; half light
    WANING("waning", 0),    // The block is mostly phased out; no light
    FADED("faded", 0);        // The block is fully phased out; no light
    
    private final String name;
    private final int light;
    
    TimePhase(String name, int light) {
        this.name = name;
        this.light = light;
    }
    
    public static TimePhase getSunPhase(@NotNull LevelReader world, @NotNull BlockPos pos) {
        return getPhaseFromFraction(getCelestialFraction(world, pos, EnvironmentAttributes.SUN_ANGLE, 0.0F));
    }
    
    public static TimePhase getMoonPhase(@NotNull LevelReader world, @NotNull BlockPos pos) {
        return getPhaseFromFraction(getCelestialFraction(world, pos, EnvironmentAttributes.MOON_ANGLE, 0.5F));
    }

    /**
     * Get how far a celestial body has turned past its zenith, as a fraction of a full turn.
     *
     * @param world the game world
     * @param pos the position being checked
     * @param angleAttribute the attribute holding the body's angle in dimensions with a day timeline
     * @param offsetFromSun the body's offset from the sun, used in dimensions with fixed time
     * @return the body's turn fraction; not reduced to the range [0, 1)
     */
    private static float getCelestialFraction(@NotNull LevelReader world, @NotNull BlockPos pos, @NotNull EnvironmentAttribute<Float> angleAttribute, float offsetFromSun) {
        if (world.dimensionType().hasFixedTime()) {
            return getFixedTimeSunFraction(world.environmentAttributes().getDimensionValue(EnvironmentAttributes.SKY_LIGHT_LEVEL)) + offsetFromSun;
        } else {
            // Celestial angles are in degrees, zero at the body's zenith, and the moon's runs from 180 to 540
            return world.environmentAttributes().getValue(angleAttribute, pos) / 360.0F;
        }
    }

    /**
     * Get the sun's turn fraction in a dimension with fixed time, such as the Nether or the End. These dimensions have
     * no day timeline, so their sun and moon angle attributes sit at the default of zero and would always read as
     * noon. In 1.21 they instead pinned the clock, the Nether at 18000 ticks (midnight) and the End at 6000 ticks
     * (noon), which the dimension's time-of-day curve turned into sun fractions of 0.5 and 0. 26.1 dropped the
     * pinned time and carries that choice in the dimension's sky light level instead: the Nether uses the night
     * level and the End keeps the day level. Read anything below the midpoint of the two as midnight and anything
     * else as noon.
     *
     * @param skyLightLevel the dimension's sky light level attribute
     * @return the sun's turn fraction, either 0.5 for midnight or 0 for noon
     */
    @VisibleForTesting
    public static float getFixedTimeSunFraction(float skyLightLevel) {
        float midpoint = (Timelines.DAY_SKY_LIGHT_LEVEL + Timelines.NIGHT_SKY_LIGHT_LEVEL) / 2.0F;
        return skyLightLevel < midpoint ? 0.5F : 0.0F;
    }

    /**
     * Get the phase of a block tied to a celestial body, given how far that body has turned past its zenith.
     *
     * @param fraction the body's turn fraction; values outside [0, 1) wrap around
     * @return the phase for that point in the body's turn
     */
    @VisibleForTesting
    public static TimePhase getPhaseFromFraction(float fraction) {
        float angle = Mth.positiveModulo(fraction, 1.0F);
        if (angle < 0.1875F) {
            return FULL;    // Afternoon
        } else if (angle < 0.25F) {
            return WAXING;  // Just before sunset
        } else if (angle < 0.3125F) {
            return WANING;  // Just after sunset
        } else if (angle < 0.6875F) {
            return FADED;   // Night
        } else if (angle < 0.75F) {
            return WANING;  // Just before sunrise
        } else if (angle < 0.8125F) {
            return WAXING;  // Just after sunrise
        } else {
            return FULL;    // Morning
        }
    }
    
    @Override
    public String toString() {
        return this.name;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return this.name;
    }
    
    public int getLightLevel() {
        return this.light;
    }
}
