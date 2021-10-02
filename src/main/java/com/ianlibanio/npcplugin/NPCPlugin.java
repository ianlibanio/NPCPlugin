package com.ianlibanio.npcplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ianlibanio.npcplugin.adapter.LocationAdapter;
import com.ianlibanio.npcplugin.commands.NPCCommand;
import com.ianlibanio.npcplugin.controller.NPCController;
import com.ianlibanio.npcplugin.helper.NPCHelper;
import com.ianlibanio.npcplugin.inventory.ConfirmationInventory;
import com.ianlibanio.npcplugin.listener.PlayerListener;
import com.ianlibanio.npcplugin.npc.NPCBase;
import com.ianlibanio.npcplugin.utils.skin.SkinFetcher;
import com.ianlibanio.voidcommand.register.VoidRegister;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public final class NPCPlugin extends NPCHelper {

    private NPCBase npcBase;
    private NPCController npcController;
    private InventoryManager inventoryManager;

    private SkinFetcher skinFetcher;
    private ConfirmationInventory confirmationInventory;

    private Gson gson;

    @Override
    public void load() {
        gson = new GsonBuilder().registerTypeAdapter(Location.class, new LocationAdapter()).create();
    }

    @Override
    public void enable() {
        saveDefaultConfig();
        this.getDataFolder().mkdirs();

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        skinFetcher = new SkinFetcher();
        confirmationInventory = new ConfirmationInventory();

        npcBase = getBaseVersion();
        npcController = new NPCController();

        listener(new PlayerListener());

        this.npcController.load();

        final VoidRegister register = new VoidRegister(this);
        register.add(new NPCCommand());
    }

    @Override
    public void disable() {
        this.npcController.save();
    }

    public static NPCPlugin getInstance() {
        return getPlugin(NPCPlugin.class);
    }

}
