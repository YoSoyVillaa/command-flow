package me.fixeddev.commandflow.velocity;

import java.util.ArrayList;
import java.util.List;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;

import me.fixeddev.commandflow.CommandManager;
import me.fixeddev.commandflow.Namespace;
import me.fixeddev.commandflow.NamespaceImpl;
import me.fixeddev.commandflow.command.Command;
import me.fixeddev.commandflow.exception.CommandException;
import me.fixeddev.commandflow.translator.Translator;
import net.kyori.adventure.text.Component;

public class VelocityCommandWrapper implements RawCommand {

    protected final CommandManager commandManager;
    protected final Translator translator;

    protected final String[] aliases;
    protected final String permission;
    protected final String command;

    public VelocityCommandWrapper(Command command, CommandManager commandManager,
                                  Translator translator) {

        this.command = command.getName();
        this.commandManager = commandManager;
        this.translator = translator;

        this.aliases = command.getAliases().toArray(new String[0]);
        this.permission = command.getPermission();
    }


    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        String argumentLine = invocation.alias() + " " + invocation.arguments();

        Namespace namespace = new NamespaceImpl();
        namespace.setObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE, commandSource);
        namespace.setObject(String.class, "label", invocation.alias());

        try {
            commandManager.execute(namespace, argumentLine);
        } catch (CommandException e) {
            CommandException exceptionToSend = e;

            if (e.getCause() instanceof CommandException) {
                exceptionToSend = (CommandException) e.getCause();
            }

            sendMessageToSender(e, namespace);

            throw new CommandException("An unexpected exception occurred while executing the command " + this.command, exceptionToSend);
        }
    }

    public String getPermission() {
        return permission;
    }

    public String[] getAliases() {
        return aliases;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        List<String> arguments = new ArrayList<>();
        arguments.add(invocation.alias());
        if (!invocation.arguments().isEmpty()) arguments.addAll(List.of(invocation.arguments().split(" ")));
        if (invocation.arguments().endsWith(" ")) arguments.add("");

        Namespace namespace = new NamespaceImpl();
        namespace.setObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE, commandSource);

        return commandManager.getSuggestions(namespace, arguments);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        CommandSource commandSource = invocation.source();

        Namespace namespace = new NamespaceImpl();
        namespace.setObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE, commandSource);

        return commandManager.getAuthorizer().isAuthorized(namespace, getPermission());
    }

    protected static void sendMessageToSender(CommandException exception, Namespace namespace) {
        CommandManager commandManager = namespace.getObject(CommandManager.class, "commandManager");
        CommandSource sender = namespace.getObject(CommandSource.class, VelocityCommandManager.SENDER_NAMESPACE);

        Component component = exception.getMessageComponent();
        Component translatedComponent = commandManager.getTranslator().translate(component, namespace);

        sender.sendMessage(translatedComponent);
    }

}
