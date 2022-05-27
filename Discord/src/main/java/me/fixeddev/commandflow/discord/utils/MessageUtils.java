package me.fixeddev.commandflow.discord.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

public class MessageUtils {

    public static String componentToString(Component component) {
        PlainComponentSerializer serializer = PlainComponentSerializer.plain();

        return serializer.serialize(component);
    }
}
