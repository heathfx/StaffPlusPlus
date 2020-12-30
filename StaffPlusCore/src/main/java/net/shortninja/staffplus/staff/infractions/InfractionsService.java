package net.shortninja.staffplus.staff.infractions;

import net.shortninja.staffplus.util.lib.JavaUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class InfractionsService {

    private final List<InfractionProvider> infractionProviders;

    public InfractionsService(List<InfractionProvider> infractionProviders) {
        this.infractionProviders = infractionProviders;
    }

    public List<Infraction> getAllInfractions(Player executor, UUID playerUuid, int page, int pageSize) {
        List<Infraction> infractions = new ArrayList<>();
        for (InfractionProvider infractionProvider : infractionProviders) {
            infractions.addAll(infractionProvider.getInfractions(executor, playerUuid));
        }
        infractions.sort(Comparator.comparingLong(Infraction::getCreationTimestamp).reversed());
        return JavaUtils.getPageOfList(infractions, page, pageSize);
    }
}
