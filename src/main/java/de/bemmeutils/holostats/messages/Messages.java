package de.bemmeutils.holostats.messages;

import de.bemmeutils.holostats.Addon;

import java.util.function.Supplier;

public enum Messages {
    HOLO_JACKPOT(Addon::getJackpotHoloText),
    HOLO_WAGER(Addon::getWagerHoloText),
    CHAT_JACKPOT(Addon::getJackpotChatText),
    DISCORD_JACKPOT(Addon::getJackpotDiscordText);

    private final Supplier<String> supplier;

    Messages(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    public MessageTemplate template() {
        return new MessageTemplate(supplier.get());
    }
}



