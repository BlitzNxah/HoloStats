package de.bemmeutils.holostats.messages;

import de.bemmeutils.holostats.utils.DiscordWebhook;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.awt.*;

@NoArgsConstructor
@AllArgsConstructor
public class DiscordJackpotMessage {
    String webhookUrl;
    Color color;
    String message;
    String playerUuid;

    public DiscordWebhook getWebhook() {
        DiscordWebhook discordWebhook = new DiscordWebhook(this.webhookUrl);
        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();

        embedObject.setTitle("Hauptgewinn");
        embedObject.setColor(this.color);
        embedObject.setThumbnail(String.format("https://visage.surgeplay.com/bust/256/%s?y=-40", this.playerUuid));
        embedObject.addField("", this.message, false);
        discordWebhook.addEmbed(embedObject);
        return discordWebhook;
    }

}
