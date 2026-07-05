package de.bemmeutils.holostats;

import de.bemmeutils.holostats.listener.MessageSendListener;
import de.bemmeutils.holostats.listener.PrivateMessageListener;
import de.bemmeutils.holostats.listener.PurchaseListener;
import de.bemmeutils.holostats.listener.SellListener;
import de.bemmeutils.holostats.messages.MessageTarget;
import de.bemmeutils.holostats.utils.FileUtil;
import de.bemmeutils.holostats.utils.JsonUtil;
import de.byteandbit.velociraptor.api.VelociraptorAPI;
import de.byteandbit.velociraptor.api.chat.ChatMessage;
import de.byteandbit.velociraptor.api.chat.ChatPriority;
import lombok.Getter;
import lombok.Setter;
import net.labymod.api.LabyModAddon;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.settings.elements.*;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Addon extends LabyModAddon {
    @Getter
    private static List<String> commandQueue = new ArrayList<>();
    private ScheduledExecutorService scheduler;

    @Getter
    private static final String ADDON_FILE_PATH = String.format("%s/BemmeUtils/Holostats/data.json", Minecraft.getMinecraft().mcDataDir).replace("/", File.separator);
    @Getter
    @Setter
    private static JsonUtil jsonUtil;
    @Getter
    @Setter
    private static VelociraptorAPI velociraptorAPI;

    @Getter
    @Setter
    private static String jackpotHoloText, wagerHoloText, jackpotChatText, jackpotDiscordText, discordWebhookUrl;
    @Getter
    @Setter
    private static boolean sendJackpotChatMessage, sendJackpotDiscordMessage;
    @Getter
    @Setter
    private static Integer minimumJackpotBroadcastValue, minimumDiscordBroadcastValue;
    @Getter
    @Setter
    private static MessageTarget jackpotMessageTarget;

    @Override
    public void onEnable() {
        initVelociraptorAPI();
        FileUtil.setupAddonFiles(Arrays.asList("jackpots", "wagers", "holos"));
        //JsonUtil depends on the addon files, so create the JSON file first
        try {
            jsonUtil = new JsonUtil(JsonUtil.readJsonFromFile(ADDON_FILE_PATH));
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }
        this.getApi().getEventManager().register(new MessageSendListener());
        this.getApi().getEventManager().register(new PrivateMessageListener());
        runCommandWorker();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void loadConfig() {
        setJackpotHoloText(getConfig().has("jackpotHoloText") ? getConfig().get("jackpotHoloText").getAsString() : "");
        setWagerHoloText(getConfig().has("wagerHoloText") ? getConfig().get("wagerHoloText").getAsString() : "");

        setJackpotChatText(getConfig().has("jackpotChatText") ? getConfig().get("jackpotChatText").getAsString() : "Jemand hat einen Jackpot gewonnen, aber ich habe vergessen das Nachricht zu ändern. Womp Womp");
        setJackpotDiscordText(getConfig().has("jackpotDiscordText") ? getConfig().get("jackpotDiscordText").getAsString() : "Jemand hat einen Jackpot gewonnen, aber ich habe vergessen das Nachricht zu ändern. Womp Womp");

        setSendJackpotChatMessage(getConfig().has("sendJackpotChatMessage") && getConfig().get("sendJackpotChatMessage").getAsBoolean());
        setSendJackpotDiscordMessage(getConfig().has("sendJackpotDiscordMessage") && getConfig().get("sendJackpotDiscordMessage").getAsBoolean());
        setDiscordWebhookUrl(getConfig().has("discordWebhookUrl") ? getConfig().get("discordWebhookUrl").getAsString() : "");
        setMinimumJackpotBroadcastValue(getConfig().has("minimumJackpotBroadcastValue") ? getConfig().get("minimumJackpotBroadcastValue").getAsInt() : 0);
        setMinimumDiscordBroadcastValue(getConfig().has("minimumDiscordBroadcastValue") ? getConfig().get("minimumDiscordBroadcastValue").getAsInt() : 0);

        setJackpotMessageTarget(getConfig().has("jackpotMessageTarget")
                ? parseMessageTarget(getConfig().get("jackpotMessageTarget").getAsString())
                : MessageTarget.CHAT);
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        ListContainerElement holoCategory = new ListContainerElement("Hologram Texte", new ControlElement.IconData(Material.BOOK));
        list.add(holoCategory);
        holoCategory.getSubSettings().add(new StringElement("Jackpot Hologram", new ControlElement.IconData(Material.PAPER), getJackpotHoloText(), (value -> {
            setJackpotHoloText(value);
            this.getConfig().addProperty("jackpotHoloText", value);
            this.saveConfig();
        })));
        holoCategory.getSubSettings().add(new StringElement("Umsatz Hologram", new ControlElement.IconData(Material.PAPER), getWagerHoloText(), (value -> {
            setWagerHoloText(value);
            this.getConfig().addProperty("wagerHoloText", value);
            this.saveConfig();
        })));

        ListContainerElement messageCategory = new ListContainerElement("Nachrichten", new ControlElement.IconData(Material.BOOK));
        list.add(messageCategory);
        messageCategory.getSubSettings().add(new StringElement("Jackpot Chat", new ControlElement.IconData(Material.PAPER), getJackpotChatText(), (value -> {
            setJackpotChatText(value);
            this.getConfig().addProperty("jackpotChatText", value);
            this.saveConfig();
        })));
        messageCategory.getSubSettings().add(new StringElement("Jackpot Discord", new ControlElement.IconData(Material.PAPER), getJackpotDiscordText(), (value -> {
            setJackpotChatText(value);
            this.getConfig().addProperty("jackpotDiscordText", value);
            this.saveConfig();
        })));
        DropDownMenu<MessageTarget> targetMenu =
                new DropDownMenu<MessageTarget>("Chat", 0, 0, 0, 0)
                        .fill(MessageTarget.values());
        targetMenu.setSelected(getJackpotMessageTarget());

        DropDownElement<MessageTarget> targetDropDown =
                new DropDownElement<>("Chat", targetMenu);
        targetDropDown.setChangeListener(target -> {
            setJackpotMessageTarget(target);
            getConfig().addProperty("jackpotMessageTarget", target.name());
            saveConfig();
        });
        messageCategory.getSubSettings().add(targetDropDown);

        ListContainerElement settingsCategory = new ListContainerElement("Einstellungen", new ControlElement.IconData(Material.REDSTONE_COMPARATOR_ON));
        list.add(settingsCategory);
        settingsCategory.getSubSettings().add(new BooleanElement("Jackpot Chat Nachricht", new ControlElement.IconData(Material.LEVER), (Consumer<Boolean>) value -> {
            setSendJackpotChatMessage(value);
            getConfig().addProperty("sendJackpotChatMessage", value);
            saveConfig();
        }, isSendJackpotChatMessage()));
        settingsCategory.getSubSettings().add(new BooleanElement("Discord Chat Nachricht", new ControlElement.IconData(Material.LEVER), (Consumer<Boolean>) value -> {
            setSendJackpotDiscordMessage(value);
            getConfig().addProperty("sendJackpotDiscordMessage", value);
            saveConfig();
        }, isSendJackpotDiscordMessage()));
        settingsCategory.getSubSettings().add(new StringElement("Discord Webhook", new ControlElement.IconData(Material.WEB), getDiscordWebhookUrl(), (value -> {
            setDiscordWebhookUrl(value);
            this.getConfig().addProperty("discordWebhookUrl", value);
            this.saveConfig();
        })));
        settingsCategory.getSubSettings().add(new NumberElement("Jackpot Chat Minimum Summe", new ControlElement.IconData(Material.GOLD_INGOT), getMinimumJackpotBroadcastValue()).setMinValue(0).setSteps(1).addCallback(value -> {
            setMinimumJackpotBroadcastValue(value);
            this.getConfig().addProperty("minimumJackpotBroadcastValue", value);
            this.saveConfig();
        }));
        settingsCategory.getSubSettings().add(new NumberElement("Discord Chat Minimum Summe", new ControlElement.IconData(Material.GOLD_INGOT), getMinimumDiscordBroadcastValue()).setMinValue(0).setSteps(1).addCallback(value -> {
            setMinimumDiscordBroadcastValue(value);
            this.getConfig().addProperty("minimumDiscordBroadcastValue", value);
            this.saveConfig();
        }));
    }

    private void initVelociraptorAPI() {
        new Thread(() -> {
            try {
                Thread.sleep(10000);
                boolean foundApi = false;
                while (!foundApi) {
                    System.out.println("SUCHE VELOCIRAPTOR API.....");
                    try {
                        Class.forName("de.byteandbit.velociraptor.api.VelociraptorAPI");
                        foundApi = true;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    Thread.sleep(1000L);
                }
                velociraptorAPI = new VelociraptorAPI();
                System.out.println("Velociraptor NG API wurde erfolgreich eingebunden!");
                VelociraptorAPI.EVENT_BUS.register(new PurchaseListener());
                VelociraptorAPI.EVENT_BUS.register(new SellListener());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void runCommandWorker() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (commandQueue.isEmpty()) return;
            String command = commandQueue.get(0);
            commandQueue.remove(0);
            Addon.getVelociraptorAPI().getChatAPI().send(ChatMessage.command().text(command).priority(ChatPriority.HIGH)); //reserve the highest priority for really important commands e.g., /p kick
        }, 0, 5, TimeUnit.SECONDS);
    }

    private static MessageTarget parseMessageTarget(String value) {
        try {
            return MessageTarget.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return MessageTarget.CHAT;
        }
    }

}

