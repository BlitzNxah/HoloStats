package de.bemmeutils.holostats.listener;

import de.bemmeutils.holostats.Addon;
import de.bemmeutils.holostats.api.Hologram;
import de.bemmeutils.holostats.api.Wager;
import de.byteandbit.velociraptor.api.events.EventHandler;
import de.byteandbit.velociraptor.api.events.sell.AfterSellEvent;

import java.util.List;

public class SellListener {
    @EventHandler
    public void onAfterSell(AfterSellEvent event) {
        String playerName = event.getPlayerName();
        String playerUuid = event.getPlayerUUID();
        double payAmount = event.getPayAmount();

        List<Wager> oldTop3 = Addon.getJsonUtil().getTop3Wagers();

        Wager wager = Addon.getJsonUtil().getWager(playerUuid);
        if (wager == null) {
            Addon.getJsonUtil().addWager(playerUuid, playerName, payAmount);
        } else {
            wager.setWager(wager.getWager() + payAmount);
            Addon.getJsonUtil().saveWager(wager);
        }

        Addon.getJsonUtil().getAllHolograms().stream()
                .filter(holo -> holo.getType() == Hologram.HologramType.WAGER)
                .forEach(holo -> {
                    holo.update(oldTop3);
                });
    }
}
