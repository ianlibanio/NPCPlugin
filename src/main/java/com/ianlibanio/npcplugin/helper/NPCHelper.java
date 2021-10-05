package com.ianlibanio.npcplugin.helper;

import com.ianlibanio.npcplugin.npc.NPCBase;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_12_R1;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_15_R1;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_17_R1;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_8_R3;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class NPCHelper extends JavaPlugin {

    public abstract void load();

    public abstract void enable();

    public abstract void disable();

    @Override
    public void onLoad() {
        load();
    }

    @Override
    public void onEnable() {
        enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    @SneakyThrows
    public NPCBase getBaseVersion() {
        val packageName = this.getServer().getClass().getPackage().getName();
        val version = packageName.substring(packageName.lastIndexOf('.') + 1);

        NPCBase npcBase;

        switch (version) {
            case "v1_8_R3":
                npcBase = new NPC_1_8_R3();
                break;
            case "v1_12_R1":
                npcBase = new NPC_1_12_R1();
                break;
            case "v1_15_R1":
                npcBase = new NPC_1_15_R1();
                break;
            case "v1_17_R1":
                npcBase = new NPC_1_17_R1();
                break;
            default:
                throw new Exception("The version " + version + " is not supported.");
        }

        return npcBase;
    }

    public void listener(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

}