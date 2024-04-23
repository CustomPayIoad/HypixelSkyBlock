package net.swofty.types.generic.item.items.armor.eleganttuxedo;

import net.minestom.server.color.Color;
import net.swofty.types.generic.item.SkyBlockItem;
import net.swofty.types.generic.item.impl.*;
import net.swofty.types.generic.user.statistics.ItemStatistic;
import net.swofty.types.generic.user.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public class ElegantTuxedoChestplate implements CustomSkyBlockItem, StandardItem, LeatherColour, NotFinishedYet, CustomDisplayName {
    @Override
    public String getDisplayName(@Nullable SkyBlockItem item) {
        return "Elegant Tuxedo Jacket";
    }

    @Override
    public Color getLeatherColour() {
        return new Color(25, 25, 25);
    }

    @Override
    public StandardItemType getStandardItemType() {
        return StandardItemType.CHESTPLATE;
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockItem instance) {
        return ItemStatistics.builder()
                .withAdditive(ItemStatistic.CRIT_DAMAGE, 100D)
                .withAdditive(ItemStatistic.INTELLIGENCE, 300D)
                .build();
    }
}
