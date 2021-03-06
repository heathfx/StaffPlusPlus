package net.shortninja.staffplus.server.command.arguments;

import org.bukkit.command.CommandSender;

public interface ArgumentExecutor extends ArgumentTabCompletion {

    boolean execute(CommandSender commandSender, String playerName, String value);

    ArgumentType getType();
}
