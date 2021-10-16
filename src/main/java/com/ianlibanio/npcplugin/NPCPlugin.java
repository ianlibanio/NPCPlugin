package com.ianlibanio.npcplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ianlibanio.npcplugin.adapter.LocationAdapter;
import com.ianlibanio.npcplugin.commands.NPCCommand;
import com.ianlibanio.npcplugin.controller.NPCController;
import com.ianlibanio.npcplugin.controller.RecordingController;
import com.ianlibanio.npcplugin.helper.NPCHelper;
import com.ianlibanio.npcplugin.inventory.ConfirmationInventory;
import com.ianlibanio.npcplugin.listener.PlayerListener;
import com.ianlibanio.npcplugin.npc.NPCBase;
import com.ianlibanio.npcplugin.service.NPCService;
import com.ianlibanio.npcplugin.service.NPCServiceImpl;
import com.ianlibanio.npcplugin.utils.skin.SkinFetcher;
import com.ianlibanio.voidcommand.register.VoidRegister;
import com.zaxxer.hikari.HikariDataSource;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import org.bukkit.Location;

import java.util.concurrent.ForkJoinPool;

@Getter
public final class NPCPlugin extends NPCHelper {

    private final ForkJoinPool executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    private NPCBase npcBase;
    private InventoryManager inventoryManager;

    private NPCController npcController;
    private RecordingController recordingController;

    private SkinFetcher skinFetcher;
    private ConfirmationInventory confirmationInventory;

    private Gson gson;
    private HikariDataSource hikariDataSource;

    public static NPCPlugin getInstance() {
        return getPlugin(NPCPlugin.class);
    }

    @Override
    public void load() {
        saveDefaultConfig();
        this.getDataFolder().mkdirs();

        this.hikariDataSource = this.getDataSourceFromConfig();
        gson = new GsonBuilder().registerTypeAdapter(Location.class, new LocationAdapter()).create();

        this.provideService(NPCService.class, new NPCServiceImpl(this));
    }

    @Override
    public void enable() {
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        skinFetcher = new SkinFetcher();
        confirmationInventory = new ConfirmationInventory();

        npcBase = getBaseVersion();

        npcController = new NPCController();
        recordingController = new RecordingController();

        npcController.constructor(this);

        listener(new PlayerListener());

        final VoidRegister register = new VoidRegister(this);
        register.add(new NPCCommand());
    }

    @Override
    public void disable() {
        npcController.destructor();
    }

    public NPCService getNPCService() {
        return this.getService(NPCService.class);
    }

}
