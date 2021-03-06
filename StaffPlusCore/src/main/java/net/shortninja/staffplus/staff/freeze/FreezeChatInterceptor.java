package net.shortninja.staffplus.staff.freeze;

import net.shortninja.staffplus.server.chat.ChatInterceptor;
import net.shortninja.staffplus.server.data.config.Messages;
import net.shortninja.staffplus.server.data.config.Options;
import net.shortninja.staffplus.util.MessageCoordinator;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class FreezeChatInterceptor implements ChatInterceptor {

    private final FreezeHandler freezeHandler;
    private final Options options;
    private final Messages messages;
    private final MessageCoordinator message;

    public FreezeChatInterceptor(FreezeHandler freezeHandler, Options options, Messages messages, MessageCoordinator message) {
        this.freezeHandler = freezeHandler;
        this.options = options;
        this.messages = messages;
        this.message = message;
    }

    @Override
    public boolean intercept(AsyncPlayerChatEvent event) {
        if (freezeHandler.isFrozen(event.getPlayer().getUniqueId()) && !options.modeConfiguration.getFreezeModeConfiguration().isModeFreezeChat()) {
            this.message.send(event.getPlayer(), messages.chatPrevented, messages.prefixGeneral);
            return true;
        }
        return false;
    }
}
