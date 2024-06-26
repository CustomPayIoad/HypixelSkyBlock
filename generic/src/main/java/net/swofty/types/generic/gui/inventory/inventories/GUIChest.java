package net.swofty.types.generic.gui.inventory.inventories;


import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import net.swofty.types.generic.block.blocks.BlockChest;
import net.swofty.types.generic.chest.Chest;
import net.swofty.types.generic.gui.inventory.ItemStackCreator;
import net.swofty.types.generic.gui.inventory.SkyBlockInventoryGUI;
import net.swofty.types.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.types.generic.user.SkyBlockPlayer;

public class GUIChest extends SkyBlockInventoryGUI {

    private final Chest chest;

    public GUIChest(Chest chest) {
        super(chest.getName(), chest.getSize());
        this.chest = chest;
    }

    @Override
    public void setItems(InventoryGUIOpenEvent e) {
        for (int counter = 0; counter < chest.getItems().size(); counter++) {
            int finalCounter = counter;
            set(new GUIClickableItem(finalCounter) {
                @Override
                public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                    ItemStack stack = e.getCursorItem();
                    chest.setItem(finalCounter, stack);
                }

                @Override
                public ItemStack.Builder getItem(SkyBlockPlayer player) {
                    return ItemStackCreator.getFromStack(chest.getItem(finalCounter));
                }

                @Override
                public boolean canPickup() {
                    return true;
                }
            });
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        e.getPlayer().playSound(Sound.sound(SoundEvent.BLOCK_CHEST_CLOSE, Sound.Source.RECORD, 1f, 1f));
        chest.playAnimation(chest.getInstance(), chest.getPosition(), BlockChest.ChestAnimation.CLOSE);
    }

    /*
    * Not supported yet.
    * cause item to disappear
     */
    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
