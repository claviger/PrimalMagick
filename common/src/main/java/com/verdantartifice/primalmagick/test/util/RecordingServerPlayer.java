package com.verdantartifice.primalmagick.test.util;

import com.mojang.authlib.GameProfile;
import com.verdantartifice.primalmagick.test.AbstractBaseTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A mock server player, set up like the ones made by {@link AbstractBaseTest}, that records every system and overlay
 * message sent to it so tests can confirm which feedback the player was given.
 */
public class RecordingServerPlayer extends ServerPlayer {
    private final List<Component> systemMessages = new ArrayList<>();
    private final List<Component> overlayMessages = new ArrayList<>();

    private RecordingServerPlayer(ServerLevel level, CommonListenerCookie cookie) {
        super(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
    }

    /**
     * Creates a recording mock player with a working mock connection, optionally adding it to the test level.
     */
    public static RecordingServerPlayer create(GameTestHelper helper, boolean joinLevel) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        RecordingServerPlayer player = new RecordingServerPlayer(helper.getLevel(), cookie);
        Connector.connect(helper, player, cookie, joinLevel);
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
        (overlay ? this.overlayMessages : this.systemMessages).add(message);
        super.sendSystemMessage(message, overlay);
    }

    public boolean hasSystemMessage(String translationKey) {
        return hasKey(this.systemMessages, translationKey);
    }

    public boolean hasOverlayMessage(String translationKey) {
        return hasKey(this.overlayMessages, translationKey);
    }

    private static boolean hasKey(List<Component> messages, String translationKey) {
        return messages.stream().anyMatch(m -> m.getContents() instanceof TranslatableContents contents && contents.getKey().equals(translationKey));
    }

    /**
     * Gives access to the protected mock connection setup in the base test class.
     */
    private static class Connector extends AbstractBaseTest {
        static void connect(GameTestHelper helper, ServerPlayer player, CommonListenerCookie cookie, boolean joinLevel) {
            attachMockConnection(helper, player, cookie, joinLevel);
        }
    }
}
