package com.verdantartifice.primalmagick.test;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A mock server player that records every system message sent to it, and can optionally be given a fixed random source
 * so that code which draws from the player's own random number generator is deterministic.
 */
public class RecordingServerPlayer extends ServerPlayer {
    private final List<Component> messages = new ArrayList<>();
    private RandomSource forcedRandom = null;

    private RecordingServerPlayer(ServerLevel level, CommonListenerCookie cookie) {
        super(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
    }

    /**
     * Creates a recording player with a working mock connection, optionally adding it to the test level.
     */
    public static RecordingServerPlayer create(GameTestHelper helper, boolean joinLevel) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        RecordingServerPlayer player = new RecordingServerPlayer(helper.getLevel(), cookie);
        AbstractBaseTest.attachMockConnection(helper, player, cookie, joinLevel);
        return player;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    public void sendSystemMessage(Component message, boolean overlay) {
        this.messages.add(message);
        super.sendSystemMessage(message, overlay);
    }

    @Override
    public RandomSource getRandom() {
        return this.forcedRandom != null ? this.forcedRandom : super.getRandom();
    }

    /**
     * Makes this player's {@link #getRandom()} return the given source, or its own again if given null.
     */
    public void setForcedRandom(RandomSource random) {
        this.forcedRandom = random;
    }

    /**
     * Returns whether a translatable message with the given key has been sent to this player.
     */
    public boolean hasMessage(String translationKey) {
        return this.messages.stream().anyMatch(m -> m.getContents() instanceof TranslatableContents contents && contents.getKey().equals(translationKey));
    }
}
