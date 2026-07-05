package de.bemmeutils.holostats.listener;

import de.bemmeutils.holostats.Addon;
import de.bemmeutils.holostats.api.Hologram;
import de.bemmeutils.holostats.api.Jackpot;
import de.bemmeutils.holostats.messages.DiscordJackpotMessage;
import de.bemmeutils.holostats.messages.MessageTarget;
import de.bemmeutils.holostats.messages.Messages;
import de.bemmeutils.holostats.utils.Helper;
import de.byteandbit.velociraptor.api.chat.ChatMessage;
import de.byteandbit.velociraptor.api.data.item.ItemType;
import de.byteandbit.velociraptor.api.events.EventHandler;
import de.byteandbit.velociraptor.api.events.purchase.AfterPurchaseEvent;

import java.awt.*;
import java.util.Arrays;

public class PurchaseListener {
    @EventHandler
    public void onAfterPurchase(AfterPurchaseEvent event) {
        String playerName = event.getPlayerName();
        String playerUuid = event.getPlayerUUID();
        event.getAcceptedItems().forEach(item -> {
            Arrays.stream(item.getPrices())
                    .filter(price -> price.getAmount() == 1)
                    .filter(price -> price.getType() == ItemType.BUY)
                    .forEach(price -> {
                        System.out.println(price);
                        Jackpot jackpot = Addon.getJsonUtil().getJackpot(price.getPrice());
                        if (jackpot == null) return;
                        int amount = event.getAmountOf(item);
                        jackpot.setTimesPurchased(jackpot.getTimesPurchased() + amount);
                        jackpot.setLastUsername(playerName);
                        Addon.getJsonUtil().saveJackpot(jackpot);

                        double total = jackpot.getPrice() * amount;

                        String discordMessage = Messages.DISCORD_JACKPOT.template()
                                .with("price", Helper.getNUMBER_FORMAT().format(jackpot.getPrice()))
                                .with("amount", jackpot.getTimesPurchased())
                                .with("player", playerName)
                                .with("purchase_amount", amount)
                                .with("total", Helper.getNUMBER_FORMAT().format(total))
                                .build();

                        String chatMessage = Messages.CHAT_JACKPOT.template()
                                .with("price", Helper.getNUMBER_FORMAT().format(jackpot.getPrice()))
                                .with("amount", jackpot.getTimesPurchased())
                                .with("player", playerName)
                                .with("purchase_amount", amount)
                                .with("total", Helper.getNUMBER_FORMAT().format(total))
                                .build();

                        if (Addon.isSendJackpotChatMessage() && price.getPrice() >= Addon.getMinimumJackpotBroadcastValue()) {
                            if (chatMessage.startsWith("/")) {
                                Addon.getVelociraptorAPI().getChatAPI().send(ChatMessage.command().text(chatMessage.substring(1)));
                            } else {
                                switch (Addon.getJackpotMessageTarget()) {
                                    case CHAT:
                                        Addon.getVelociraptorAPI().getChatAPI().send(ChatMessage.chat().text(chatMessage));
                                        break;
                                    case PLOT_CHAT:
                                        Addon.getVelociraptorAPI().getChatAPI().send(ChatMessage.plotChat().text(chatMessage));
                                        break;
                                }
                            }
                        }

                        Addon.getJsonUtil().getAllHolograms().stream()
                                .filter(holo -> holo.getType() == Hologram.HologramType.JACKPOT)
                                .filter(holo -> holo.getLines().containsValue(jackpot.getPrice()))
                                .forEach(holo -> {
                                    holo.update(jackpot);
                                });

                        if (!Addon.isSendJackpotDiscordMessage() || Addon.getDiscordWebhookUrl().equalsIgnoreCase(""))
                            return;
                        if (price.getPrice() < Addon.getMinimumDiscordBroadcastValue()) return;
                        try {
                            new DiscordJackpotMessage(Addon.getDiscordWebhookUrl(), Color.GREEN, discordMessage, playerUuid).getWebhook().execute();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    });
        });
    }
}
