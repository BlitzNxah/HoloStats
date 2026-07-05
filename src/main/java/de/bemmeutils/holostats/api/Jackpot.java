package de.bemmeutils.holostats.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class Jackpot {
    @Getter
    @Setter
    private double price;
    @Getter
    @Setter
    private int timesPurchased;
    @Getter
    @Setter
    private String lastUsername;
}
