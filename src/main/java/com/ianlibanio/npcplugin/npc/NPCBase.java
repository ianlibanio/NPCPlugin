package com.ianlibanio.npcplugin.npc;

import org.bukkit.Location;

import java.util.UUID;


public interface NPCBase<T> {

    T spawn(UUID uuid, String displayName, String skinName, Location spawnLocation);

    void despawn(T entityPlayer);

    void hit(T entityPlayer);

    void crouch(T entityPlayer, boolean crouch);

    void walk(T entityPlayer, Location location, Location oldLocation);

}
