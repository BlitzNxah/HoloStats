package de.bemmeutils.holostats.api;

import de.bemmeutils.holostats.Addon;
import de.bemmeutils.holostats.messages.Messages;
import de.bemmeutils.holostats.utils.Helper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Hologram {
    @Getter
    @Setter
    private int name;
    @Getter
    @Setter
    private HologramType type;
    @Getter
    @Setter
    private Map<Integer, Double> lines = new HashMap<>();

    public Hologram(int holoName, HologramType hologramType) {
        this.name = holoName;
        this.type = hologramType;
        this.lines = new HashMap<>();
    }

    public void addLine(int lineNumber, double jackpotPrice) {
        if (lineNumber < 1 || lineNumber > 3) {
            throw new IllegalArgumentException("Zeile muss zwischen 1 und 3 liegen!");
        }
        lines.put(lineNumber, jackpotPrice);
    }

    public void removeLine(int lineNumber) {
        lines.remove(lineNumber);
    }

    public Double getJackpotForLine(int lineNumber) {
        return lines.get(lineNumber);
    }

    public boolean hasLine(int lineNumber) {
        return lines.containsKey(lineNumber);
    }

    public enum HologramType {
        WAGER,
        JACKPOT,
    }

    //TODO: Maybe make this an abstract class and make individual classes for each type of hologram?

    public void update(Jackpot jackpot) {
        String holoText = Messages.HOLO_JACKPOT.template()
                .with("price", Helper.getNUMBER_FORMAT().format(jackpot.getPrice()))
                .with("amount", jackpot.getTimesPurchased())
                .with("player", jackpot.getLastUsername())
                .build();

        for (Map.Entry<Integer, Double> entry : this.getLines().entrySet()) {
            int lineNumber = entry.getKey();
            double jackpotPrice = entry.getValue();
            if (jackpotPrice == jackpot.getPrice()) {
                Addon.getCommandQueue().add(String.format("pholo edit %s %s %s", this.getName(), lineNumber, holoText));
            }
        }
    }

    public void update(List<Wager> oldTop3) {
        List<Wager> newTop3 = Addon.getJsonUtil().getTop3Wagers();
        for (int i = 0; i < 3; i++) {
            Wager oldWager = i < oldTop3.size() ? oldTop3.get(i) : new Wager("none", "none", -1);
            Wager newWager = i < newTop3.size() ? newTop3.get(i) : new Wager("none", "none", -1);
            if (newWager.getWager() == -1) continue;
            if (oldWager.getWager() == newWager.getWager() && oldWager.getPlayerName().equalsIgnoreCase(newWager.getPlayerName()) && oldWager.getUuid().equalsIgnoreCase(newWager.getUuid()))
                continue;

            String holoText = Messages.HOLO_WAGER.template()
                    .with("wager", Helper.getNUMBER_FORMAT().format(newWager.getWager()))
                    .with("position", i + 1)
                    .with("player", newWager.getPlayerName())
                    .build();
            Addon.getCommandQueue().add(String.format("pholo edit %s %s %s", this.getName(), i + 1, holoText));
        }
    }


    public void reload() {
        if (this.getType() == HologramType.JACKPOT) {
            for (Map.Entry<Integer, Double> entry : this.getLines().entrySet()) {
                int lineNumber = entry.getKey();
                double jackpotPrice = entry.getValue();
                Jackpot jackpot = Addon.getJsonUtil().getJackpot(jackpotPrice);
                if (jackpot == null) return;
                String holoText = Messages.HOLO_JACKPOT.template()
                        .with("price", Helper.getNUMBER_FORMAT().format(jackpot.getPrice()))
                        .with("amount", jackpot.getTimesPurchased())
                        .with("player", jackpot.getLastUsername())
                        .build();
                Addon.getCommandQueue().add(String.format("pholo edit %s %s %s", this.getName(), lineNumber, holoText));
            }
        } else if (this.getType() == HologramType.WAGER) {
            List<Wager> top3 = Addon.getJsonUtil().getTop3Wagers();
            for (int i = 0; i < 3; i++) {
                Wager newWager = i < top3.size() ? top3.get(i) : new Wager("none", "none", -1);
                if (newWager.getWager() == -1) continue;
                String holoText = Messages.HOLO_WAGER.template()
                        .with("wager", Helper.getNUMBER_FORMAT().format(newWager.getWager()))
                        .with("position", i + 1)
                        .with("player", newWager.getPlayerName())
                        .build();
                Addon.getCommandQueue().add(String.format("pholo edit %s %s %s", this.getName(), i + 1, holoText));
            }
        }
    }
}