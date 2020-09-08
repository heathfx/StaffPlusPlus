package net.shortninja.staffplus.player.ext.bukkit;

import net.shortninja.staffplus.player.OfflinePlayerProvider;
import net.shortninja.staffplus.player.ProvidedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public class BukkitOfflinePlayerProvider implements OfflinePlayerProvider {

    @Override
    public Optional<ProvidedPlayer> findUser(String username) {
        OfflinePlayer playerExact = Bukkit.getOfflinePlayer(username);
        if(!playerExact.hasPlayedBefore()) {
            return Optional.empty();
        }

        return Optional.of(new ProvidedPlayer(playerExact.getUniqueId(), playerExact.getName()));
    }
}