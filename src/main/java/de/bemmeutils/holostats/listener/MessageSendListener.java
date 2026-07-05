package de.bemmeutils.holostats.listener;

import de.bemmeutils.holostats.Addon;
import de.bemmeutils.holostats.api.Hologram;
import de.bemmeutils.holostats.api.Jackpot;
import de.bemmeutils.holostats.api.Wager;
import de.bemmeutils.holostats.utils.Helper;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.main.LabyMod;

import java.util.Comparator;
import java.util.List;

public class MessageSendListener implements MessageSendEvent {
    @Override
    public boolean onSend(String message) {
        String[] args = message.trim().split(" ");
        if (!args[0].equalsIgnoreCase("/ggstats")) return false;
        if (args.length < 2) {
            sendUsageMessage();
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "jackpot":
                if (args.length == 3 && args[2].equalsIgnoreCase("list")) {
                    List<Jackpot> jackpots = Addon.getJsonUtil().getAllJackpots();
                    if (jackpots.isEmpty()) {
                        LabyMod.getInstance().displayMessageInChat("§cEs sind keine Jackpots vorhanden!");
                        return true;
                    }
                    LabyMod.getInstance().displayMessageInChat("§e--- Jackpot Liste ---");

                    jackpots.stream().sorted(Comparator.comparing(Jackpot::getPrice).reversed()).forEach(jackpot -> {
                        LabyMod.getInstance().displayMessageInChat(String.format("§a%s$ §e| Gekauft: §a%dx §e| Letzter Käufer: §a%s", Helper.getNUMBER_FORMAT().format(jackpot.getPrice()), jackpot.getTimesPurchased(), jackpot.getLastUsername().isEmpty() ? "Niemand" : jackpot.getLastUsername()));
                    });
                    return true;
                } else if (args.length == 4 && args[2].equalsIgnoreCase("add")) {
                    double price;
                    try {
                        price = Double.parseDouble(args[3]);
                    } catch (NumberFormatException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cDer Jackpot muss eine Zahl sein!");
                        return true;
                    }
                    if (Addon.getJsonUtil().getJackpot(price) != null) {
                        LabyMod.getInstance().displayMessageInChat("§cDieser Jackpot wurde bereits erstellt!");
                        return true;
                    }
                    Addon.getJsonUtil().addJackpot(price);
                    LabyMod.getInstance().displayMessageInChat("§aDer Jackpot wurde erstellt!");
                    return true;
                } else if (args.length == 6 && args[2].equalsIgnoreCase("edit")) {
                    double price;
                    int timesPurchased;
                    try {
                        price = Double.parseDouble(args[3]);
                        timesPurchased = Integer.parseInt(args[4]);
                    } catch (NumberFormatException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cDer Jackpot und die Anzahl müssen eine Zahl sein!");
                        return true;
                    }
                    Jackpot jackpot = Addon.getJsonUtil().getJackpot(price);
                    if (jackpot == null) {
                        LabyMod.getInstance().displayMessageInChat("§cDieser Jackpot existiert nicht!");
                        return true;
                    }
                    jackpot.setTimesPurchased(timesPurchased);
                    jackpot.setLastUsername(args[5]);
                    Addon.getJsonUtil().saveJackpot(jackpot);
                    LabyMod.getInstance().displayMessageInChat("§aDer Jackpot wurde gespeichert!");
                    return true;
                } else if (args.length == 4 && args[2].equalsIgnoreCase("remove")) {
                    double price;
                    try {
                        price = Double.parseDouble(args[3]);
                    } catch (NumberFormatException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cDer Jackpot muss eine Zahl sein!");
                        return true;
                    }
                    if (Addon.getJsonUtil().getJackpot(price) == null) {
                        LabyMod.getInstance().displayMessageInChat("§cDieser Jackpot existiert nicht!");
                        return true;
                    }
                    Addon.getJsonUtil().removeJackpot(price);
                    LabyMod.getInstance().displayMessageInChat("§aDer Jackpot wurde entfernt!");
                    return true;
                }
                break;
            case "umsatz":
                if (args.length == 4 && args[2].equalsIgnoreCase("info")) {
                    String playerName = args[3];
                    Helper.getPlayerUuidAsync(playerName, playerUuid -> {
                        if (playerUuid == null) {
                            LabyMod.getInstance().displayMessageInChat("§cDer Spieler existiert nicht!");
                            return;
                        }
                        String uuid = playerUuid.toString();
                        Wager wager = Addon.getJsonUtil().getWager(uuid);
                        if (wager == null) {
                            LabyMod.getInstance().displayMessageInChat("§cDieser Spieler hat noch keinen Umsatz!");
                            return;
                        }
                        LabyMod.getInstance().displayMessageInChat(String.format("§a%s §ehat einen Umsatz von §a%s$§e.", playerName, Helper.getNUMBER_FORMAT().format(wager.getWager())));
                    }, exception -> {
                        LabyMod.getInstance().displayMessageInChat("§cFehler beim Abrufen der UUID: " + exception.getMessage());
                    });
                    return true;
                } else if (args.length == 5 && args[2].equalsIgnoreCase("edit")) {
                    String playerName = args[3];
                    double wager;
                    try {
                        wager = Double.parseDouble(args[4]);
                    } catch (NumberFormatException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cDer Umsatz muss eine Zahl sein!");
                        return true;
                    }
                    Helper.getPlayerUuidAsync(playerName, playerUuid -> {
                        if (playerUuid == null) {
                            LabyMod.getInstance().displayMessageInChat("§cDer Spieler existiert nicht!");
                            return;
                        }
                        String uuid = playerUuid.toString();
                        if (Addon.getJsonUtil().getWager(uuid) == null) {
                            Addon.getJsonUtil().addWager(uuid, playerName, wager);
                        } else {
                            Addon.getJsonUtil().saveWager(new Wager(uuid, playerName, wager));
                        }
                        LabyMod.getInstance().displayMessageInChat("§aDer Umsatz wurde gespeichert!");
                    }, exception -> {
                        LabyMod.getInstance().displayMessageInChat("§cFehler beim Abrufen der UUID: " + exception.getMessage());
                    });
                    return true;
                }
                break;
            case "holo":
                if (args.length == 5 && args[2].equalsIgnoreCase("create")) {
                    Hologram.HologramType holoType;
                    int holoName;
                    try {
                        holoType = Hologram.HologramType.valueOf(args[3].toUpperCase());
                        holoName = Integer.parseInt(args[4]);
                    } catch (IllegalArgumentException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cMögliche Hologram Typen: jackpot/wager");
                        LabyMod.getInstance().displayMessageInChat("§cDer Holo-Name muss eine Zahl sein!");
                        return true;
                    }
                    if (Addon.getJsonUtil().getHologram(holoName) != null) {
                        LabyMod.getInstance().displayMessageInChat("§cEin Holo mit diesem Namen existiert bereits!");
                        return true;
                    }
                    Addon.getJsonUtil().addHolo(holoName, holoType);
                    LabyMod.getInstance().displayMessageInChat("§aDas Holo wurde erstellt!");
                    return true;
                } else if (args.length == 4 && args[2].equalsIgnoreCase("reload")) {
                    int holoName;
                    try {
                        holoName = Integer.parseInt(args[3]);
                    } catch (NumberFormatException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cDer Holo-Name muss eine Zahl sein!");
                        return true;
                    }
                    Hologram holo = Addon.getJsonUtil().getHologram(holoName);
                    if (holo == null) {
                        LabyMod.getInstance().displayMessageInChat("§cEin Holo mit diesem Namen existiert nicht!");
                        return true;
                    }
                    holo.reload();
                    LabyMod.getInstance().displayMessageInChat("§aDas Holo wurde neu geladen!");
                    return true;
                } else if (args.length == 4 && args[2].equalsIgnoreCase("remove")) {
                    int holoName;
                    try {
                        holoName = Integer.parseInt(args[3]);
                    } catch (NumberFormatException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cDer Holo-Name muss eine Zahl sein!");
                        return true;
                    }
                    if (Addon.getJsonUtil().getHologram(holoName) == null) {
                        LabyMod.getInstance().displayMessageInChat("§cEin Holo mit diesem Namen existiert nicht!");
                        return true;
                    }
                    Addon.getJsonUtil().removeHolo(holoName);
                    LabyMod.getInstance().displayMessageInChat("§aDas Holo wurde entfernt!");
                    return true;
                } else if (args.length == 6 && args[2].equalsIgnoreCase("add")) {
                    int holoName;
                    int holoLine;
                    double jackpotValue;
                    try {
                        holoName = Integer.parseInt(args[3]);
                        holoLine = Integer.parseInt(args[4]);
                        jackpotValue = Double.parseDouble(args[5]);
                    } catch (NumberFormatException exception) {
                        LabyMod.getInstance().displayMessageInChat("§cHolo-Name, Zeile und Jackpot müssen eine Zahl sein!");
                        return true;
                    }
                    Jackpot jackpot = Addon.getJsonUtil().getJackpot(jackpotValue);
                    if (jackpot == null) {
                        LabyMod.getInstance().displayMessageInChat("§cDieser Jackpot existiert nicht!");
                        return true;
                    }
                    Hologram holo = Addon.getJsonUtil().getHologram(holoName);
                    if (holo == null) {
                        LabyMod.getInstance().displayMessageInChat("§cDieses Holo existiert nicht!");
                        return true;
                    }
                    if (holo.getType() != Hologram.HologramType.JACKPOT) {
                        LabyMod.getInstance().displayMessageInChat("§cDieser Hologram-Typ kann keine Jackpots anzeigen.");
                        return true;
                    }
                    holo.addLine(holoLine, jackpot.getPrice());
                    LabyMod.getInstance().displayMessageInChat("§aDer Jackpot wurde dem Holo hinzugefügt!");
                    Addon.getJsonUtil().saveHologram(holo);
                    return true;
                }
                break;
        }
        sendUsageMessage();
        return true;
    }

    private void sendUsageMessage() {
        LabyMod.getInstance().displayMessageInChat("§1");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats jackpot add <Preis>");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats jackpot edit <Preis> <Anzahl> <Spieler>");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats jackpot remove <Preis>");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats jackpot list");
        LabyMod.getInstance().displayMessageInChat("§2");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats umsatz info <Spieler>");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats umsatz edit <Spieler> <Summe>");
        LabyMod.getInstance().displayMessageInChat("§3");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats holo create <Typ> <Holo-Name>");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats holo add <Holo-Name> <Zeile> <Jackpot>");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats holo reload <Holo-Name>");
        LabyMod.getInstance().displayMessageInChat("§e/ggstats holo remove <Holo-Name>");
        LabyMod.getInstance().displayMessageInChat("§4");
    }
}
