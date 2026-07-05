package de.bemmeutils.holostats.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MessageTemplate {
    private final String template;
    private final Map<String, String> placeholders = new HashMap<>();
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public MessageTemplate(String template) {
        this.template = template;
    }

    public MessageTemplate with(String key, Object value) {
        placeholders.put(key, String.valueOf(value));
        return this;
    }

    public String build() {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public boolean hasPlaceholders() {
        return PLACEHOLDER_PATTERN.matcher(template).find();
    }
}

