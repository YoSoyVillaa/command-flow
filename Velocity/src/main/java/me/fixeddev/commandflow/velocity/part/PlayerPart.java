package me.fixeddev.commandflow.velocity.part;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.fixeddev.commandflow.CommandContext;
import me.fixeddev.commandflow.exception.ArgumentParseException;
import me.fixeddev.commandflow.part.ArgumentPart;
import me.fixeddev.commandflow.part.CommandPart;
import me.fixeddev.commandflow.stack.ArgumentStack;
import me.fixeddev.commandflow.velocity.VelocityCommandManager;
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerPart implements ArgumentPart {

    private final ProxyServer  proxyServer;
    private final String name;
    private final boolean orSource;

    public PlayerPart(ProxyServer proxyServer, String name, boolean orSource) {
        this.proxyServer = proxyServer;
        this.name = name;
        this.orSource = orSource;
    }

    @Override
    public List<? extends Player> parseValue(CommandContext context, ArgumentStack stack, CommandPart parent) throws ArgumentParseException {
        Player player;

        if (!stack.hasNext()) {
            player = tryGetSender(context);
            if (orSource && player != null) {
                return Collections.singletonList(player);
            }
        }

        String target = stack.next();

        try {
            UUID uuid = UUID.fromString(target);

            player = proxyServer.getPlayer(uuid).orElse(null);
        } catch (IllegalArgumentException exception) {
            player = proxyServer.getPlayer(name).orElse(null);

            if (player == null) {
                throw new ArgumentParseException(TranslatableComponent.of("player.offline", TextComponent.of(target)))
                        .setArgument(this);
            }
        }

        if (player == null) {
            player = tryGetSender(context);
            if (orSource && player != null) {
                return Collections.singletonList(player);
            }
        }

        return Collections.singletonList(player);
    }

    @Override
    public List<String> getSuggestions(CommandContext commandContext, ArgumentStack stack) {
        return getStrings(stack);
    }

    private List<String> getStrings(ArgumentStack stack) {
        String last = stack.hasNext() ? stack.next() : null;

        List<String> names = new ArrayList<>();

        if (last == null) {
            for (Player player : proxyServer.getAllPlayers()) {
                names.add(player.getUsername());
            }
            return names;
        }

        if (proxyServer.getPlayer(last).isPresent()) {
            return Collections.emptyList();
        }

        for (Player player : proxyServer.matchPlayer(last)) {
            names.add(player.getUsername());
        }

        return names;
    }

    private Player tryGetSender(CommandContext context) {
        CommandSource sender = context.getObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE);

        if (sender instanceof Player) {
            return (Player) sender;
        }

        return null;
    }

    @Override
    public String getName() {
        return name;
    }
}
