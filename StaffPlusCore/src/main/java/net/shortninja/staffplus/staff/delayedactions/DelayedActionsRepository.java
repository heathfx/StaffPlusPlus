package net.shortninja.staffplus.staff.delayedactions;

import java.util.List;
import java.util.UUID;

public interface DelayedActionsRepository {

    void saveDelayedAction(UUID uuid, String command);

    List<String> getDelayedActions(UUID uuid);

    void clearDelayedActions(UUID uuid);
}
