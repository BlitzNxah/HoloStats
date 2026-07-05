package de.bemmeutils.holostats.listener;

import de.bemmeutils.holostats.Addon;
import de.bemmeutils.holostats.api.Wager;
import de.bemmeutils.holostats.utils.Helper;
import net.labymod.api.events.MessageReceiveEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivateMessageListener implements MessageReceiveEvent {

        private static final Pattern PM_PATTERN = Pattern.compile("^\\[.*? (?<name>\\S+) -> mir\\] (?<message>.*)$");

        @Override
        public boolean onReceive(String message, String formattedMessage) {

                if (formattedMessage == null) return false;

                Matcher matcher = PM_PATTERN.matcher(formattedMessage);

                if (!matcher.matches()) return false;

                String playerName = matcher.group("name");
                String playerMessage = matcher.group("message");
                if (playerName == null || playerMessage == null) return false;
                if (!playerMessage.trim().equalsIgnoreCase("#umsatz")) return false;

                System.out.println("[Holostats] #umsatz erkannt von: " + playerName);

                Helper.getPlayerUuidAsync(playerName, playerUuid -> {
                        System.out.println("[Holostats] UUID abgerufen: " + playerUuid);
                        if (playerUuid == null) {
                                Addon.getCommandQueue().add("msg " + playerName + " Fehler: Spieler nicht gefunden.");
                                return;
                        }
                        Wager wager = Addon.getJsonUtil().getWager(playerUuid.toString());

                        if (wager == null || wager.getWager() == 0) {
                                Addon.getCommandQueue().add("msg " + playerName + " Du hast noch keinen Umsatz.");
                        } else {
                                Addon.getCommandQueue().add("msg " + playerName + " Dein Umsatz ist: " + formatWager(wager.getWager()));
                        }
                }, exception -> {
                        Addon.getCommandQueue().add("msg " + playerName + " Fehler beim Abrufen deiner Daten.");
                });

                return false;
        }

        private static String formatWager(double wager) {
                if (wager >= 1_000_000_000) {
                        double mrd = wager / 1_000_000_000.0;
                        return Helper.getNUMBER_FORMAT().format(mrd) + " Mrd";
                }
                return Helper.getNUMBER_FORMAT().format(wager) + "$";
        }
}