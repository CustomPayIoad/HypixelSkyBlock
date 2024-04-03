package net.swofty.types.generic.event.actions.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.item.Material;
import net.swofty.types.generic.gui.inventory.inventories.GUIEnchantmentTable;
import net.swofty.types.generic.user.SkyBlockPlayer;
import net.swofty.types.generic.event.EventNodes;
import net.swofty.types.generic.event.EventParameters;
import net.swofty.types.generic.event.SkyBlockEvent;

@EventParameters(description = "Handles clicking on the enchantment table",
        node = EventNodes.PLAYER,
        requireDataLoaded = true)
public class ActionEnchantmentTableClick extends SkyBlockEvent {

    @Override
    public Class<? extends Event> getEvent() {
        return PlayerBlockInteractEvent.class;
    }

    @Override
    public void run(Event event) {
        PlayerBlockInteractEvent interactEvent = (PlayerBlockInteractEvent) event;
        final SkyBlockPlayer player = (SkyBlockPlayer) interactEvent.getPlayer();

        if (Material.fromNamespaceId(interactEvent.getBlock().namespace()) != Material.ENCHANTING_TABLE) {
            return;
        }

        interactEvent.setBlockingItemUse(true);

        new GUIEnchantmentTable(player.getInstance(), Pos.fromPoint(interactEvent.getBlockPosition())).open(player);
    }
}

