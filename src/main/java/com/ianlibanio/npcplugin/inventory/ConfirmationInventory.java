package com.ianlibanio.npcplugin.inventory;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.utils.material.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import lombok.val;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class ConfirmationInventory implements InventoryProvider {

    private Consumer<Boolean> consumer;

    public final SmartInventory getInventory(boolean create, String npc, Consumer<Boolean> consumer) {
        this.consumer = consumer;

        return SmartInventory.builder()
                .id("confirmationInventory")
                .provider(this)
                .size(3, 9)
                .closeable(false)
                .title(create ? "§aCreate NPC " + npc : "§cDelete NPC " + npc)
                .manager(NPCPlugin.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        val confirm = XMaterial.GREEN_WOOL.parseItem();
        val confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§aConfirm");
        confirm.setItemMeta(confirmMeta);

        val cancel = XMaterial.RED_WOOL.parseItem();
        val cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cCancel");
        cancel.setItemMeta(cancelMeta);

        contents.set(1, 3, ClickableItem.of(confirm, e -> {
            consumer.accept(true);
            contents.inventory().close(player);
        }));

        contents.set(1, 5, ClickableItem.of(cancel, e -> {
            consumer.accept(false);
            contents.inventory().close(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}
