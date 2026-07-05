package de.bemmeutils.holostats.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class Wager {
    @Getter
    @Setter
    private String uuid;
    @Getter
    @Setter
    private String playerName;
    @Getter
    @Setter
    private double wager;
}