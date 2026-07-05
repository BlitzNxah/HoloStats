package de.bemmeutils.holostats.utils;

import de.bemmeutils.holostats.Addon;
import lombok.Getter;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public class Helper {
    @Getter
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.GERMANY);

    public static void getPlayerUuidAsync(String playerName, Consumer<UUID> callback, Consumer<Throwable> onError) {
        new Thread(() -> {
            try {
                UUID uuid = Addon.getVelociraptorAPI().getPlayerAPI().getUUIDByPlayerName(playerName);
                callback.accept(uuid);
            } catch (Exception exception) {
                onError.accept(exception);
            }
        }).start();
    }

}
