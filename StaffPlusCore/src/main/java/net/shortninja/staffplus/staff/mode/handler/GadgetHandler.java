package net.shortninja.staffplus.staff.mode.handler;

import be.garagepoort.staffplusplus.craftbukkit.common.IProtocol;
import net.shortninja.staffplus.IocContainer;
import net.shortninja.staffplus.StaffPlus;
import net.shortninja.staffplus.player.SppPlayer;
import net.shortninja.staffplus.player.attribute.gui.CounterGui;
import net.shortninja.staffplus.player.attribute.gui.hub.HubGui;
import net.shortninja.staffplus.server.data.config.Messages;
import net.shortninja.staffplus.server.data.config.Options;
import net.shortninja.staffplus.session.PlayerSession;
import net.shortninja.staffplus.session.SessionManager;
import net.shortninja.staffplus.staff.examine.ExamineGui;
import net.shortninja.staffplus.staff.mode.item.CustomModuleConfiguration;
import net.shortninja.staffplus.staff.vanish.VanishService;
import net.shortninja.staffplus.util.MessageCoordinator;
import net.shortninja.staffplus.util.PermissionHandler;
import net.shortninja.staffplus.util.lib.JavaUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class GadgetHandler {
    private final static Map<UUID, Integer> lastRandomTeleport = new HashMap<UUID, Integer>();
    private final IProtocol versionProtocol = StaffPlus.get().versionProtocol;
    private final PermissionHandler permission = IocContainer.getPermissionHandler();
    private final MessageCoordinator message = IocContainer.getMessage();
    private final Options options = IocContainer.getOptions();
    private final Messages messages = IocContainer.getMessages();
    private final SessionManager sessionManager = IocContainer.getSessionManager();
    private final CpsHandler cpsHandler = StaffPlus.get().cpsHandler;
    private final VanishService vanishService = IocContainer.getVanishHandler();

    public GadgetType getGadgetType(ItemStack item, String value) {
        if (options.modeConfiguration.getCompassModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.COMPASS;
        }
        if (options.modeConfiguration.getRandomTeleportModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.RANDOM_TELEPORT;
        }
        if (options.modeConfiguration.getVanishModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.VANISH;
        }
        if (options.modeConfiguration.getGuiModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.GUI_HUB;
        }
        if (options.modeConfiguration.getCounterModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.COUNTER;
        }
        if (options.modeConfiguration.getFreezeModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.FREEZE;
        }
        if (options.modeConfiguration.getCpsModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.CPS;
        }
        if (options.modeConfiguration.getExamineModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.EXAMINE;
        }
        if (options.modeConfiguration.getFollowModeConfiguration().getIdentifier().equals(value)) {
            return GadgetType.FOLLOW;
        }

        return GadgetType.CUSTOM;
    }

    public Optional<CustomModuleConfiguration> getModule(ItemStack item) {
        String identifier = versionProtocol.getNbtString(item);
        return options.customModuleConfigurations
            .stream()
            .filter(m -> m.getIdentifier().equals(identifier))
            .findFirst();
    }

    public void onCompass(Player player) {
        Vector vector = player.getLocation().getDirection();


        player.setVelocity(JavaUtils.makeVelocitySafe(vector.multiply(options.modeConfiguration.getCompassModeConfiguration().getVelocity())));
    }

    public void onRandomTeleport(Player player, int count) {
        List<Player> onlinePlayers = JavaUtils.getOnlinePlayers();
        Player currentPlayer = null;

        if (onlinePlayers.size() == 1) {
            message.send(player, messages.modeNotEnoughPlayers, messages.prefixGeneral);
            return;
        }

        if (options.modeConfiguration.getRandomTeleportModeConfiguration().isRandom()) {
            Random random = new Random();

            do {
                currentPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
            }
            while (player.getName().equals(currentPlayer.getName()) || permission.has(currentPlayer, options.permissionMember));
        } else {
            UUID uuid = player.getUniqueId();
            int lastIndex = lastRandomTeleport.get(uuid) == null ? 0 : lastRandomTeleport.get(uuid);

            if ((lastIndex + 1) < onlinePlayers.size()) {
                lastIndex++;
            } else lastIndex = 0;

            currentPlayer = onlinePlayers.get(lastIndex);

            if (count >= onlinePlayers.size()) {
                message.send(player, messages.modeNotEnoughPlayers, messages.prefixGeneral);
                return;
            } else if (player.getName().equals(currentPlayer.getName()) || permission.has(currentPlayer, options.permissionMember)) {
                lastRandomTeleport.put(uuid, lastIndex);
                onRandomTeleport(player, count + 1);
                return;
            }

            lastRandomTeleport.put(uuid, lastIndex);
        }

        message.send(player, messages.modeRandomTeleport, messages.prefixGeneral);
        player.teleport(currentPlayer);
    }

    public void onVanish(Player player, boolean shouldUpdateItem) {
        ItemStack item = player.getItemInHand();
        int slot = JavaUtils.getItemSlot(player.getInventory(), item);

        PlayerSession session = sessionManager.get(player.getUniqueId());
        if (session.getVanishType() == options.modeConfiguration.getModeVanish()) {
            vanishService.removeVanish(player);
        } else {
            vanishService.addVanish(player, options.modeConfiguration.getModeVanish());
        }

        if (shouldUpdateItem && item != null) {
            player.getInventory().remove(item);
            player.getInventory().setItem(slot, options.modeConfiguration.getVanishModeConfiguration().getModeVanishItem(session, options.modeConfiguration.getModeVanish()));
        }
    }

    public void onGuiHub(Player player) {
        new HubGui(player, options.modeConfiguration.getGuiModeConfiguration().getItem().getItemMeta().getDisplayName());
    }

    public void onCounter(Player player) {
        new CounterGui(player, options.modeConfiguration.getCounterModeConfiguration().getTitle(), 0);
    }

    public void onCps(CommandSender sender, Player targetPlayer) {
        if (targetPlayer == null) {
            return;
        }

        cpsHandler.startTest(sender, targetPlayer);
    }

    public void onExamine(Player player, SppPlayer targetPlayer) {
        if (targetPlayer == null) {
            return;
        }

        new ExamineGui(player, targetPlayer, options.modeConfiguration.getExamineModeConfiguration().getModeExamineTitle());
    }

    public void onFollow(Player player, Player targetPlayer) {
        if (targetPlayer == null || player.getName().equals(targetPlayer.getName())) {
            return;
        }

        if (player.getVehicle() != null) {
            player.getVehicle().eject();
            return;
        }

        targetPlayer.setPassenger(player);
    }

    public void onCustom(Player player, Player targetPlayer, CustomModuleConfiguration customModuleConfiguration) {
        switch (customModuleConfiguration.getType()) {
            case COMMAND_STATIC:
                Bukkit.dispatchCommand(player, customModuleConfiguration.getAction());
                break;
            case COMMAND_DYNAMIC:
                if (targetPlayer != null) {
                    Bukkit.dispatchCommand(player, customModuleConfiguration.getAction().replace("%clicker%", player.getName()).replace("%clicked%", targetPlayer.getName()));
                }
                break;
            case COMMAND_CONSOLE:
                if (targetPlayer != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), customModuleConfiguration.getAction().replace("%clicker%", player.getName()).replace("%clicked%", targetPlayer.getName()));
                } else
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), customModuleConfiguration.getAction().replace("%clicker%", player.getName()));
                break;
            default:
                break;
        }
    }

    public void updateGadgets() {
        Set<UUID> modeUsers = IocContainer.getModeCoordinator().getModeUsers();

        for (UUID uuid : modeUsers) {
            Optional<Player> player = sessionManager.get(uuid).getPlayer();

            if (!player.isPresent()) {
                continue;
            }

            for (ItemStack item : player.get().getInventory().getContents()) {
                if (item == null) {
                    continue;
                }

                if (getGadgetType(item, versionProtocol.getNbtString(item)) == GadgetType.COUNTER) {
                    item.setAmount(options.modeConfiguration.getCounterModeConfiguration().isModeCounterShowStaffMode() ? modeUsers.size() : permission.getStaffCount());
                    break;
                }
            }
        }
    }

    public enum GadgetType {
        COMPASS, RANDOM_TELEPORT, VANISH, GUI_HUB, COUNTER, FREEZE, CPS, EXAMINE,
        FOLLOW, CUSTOM, NO_GADGET;
    }
}