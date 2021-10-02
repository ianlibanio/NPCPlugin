package com.ianlibanio.npcplugin.npc.impl;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.npc.NPCBase;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.val;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityPose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

import java.util.Objects;
import java.util.UUID;

public class NPC_1_17_R1 implements NPCBase<EntityPlayer> {

    @Override
    public EntityPlayer spawn(UUID uuid, String displayName, String skinName, Location spawnLocation) {
        val minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

        val worldServer = ((CraftWorld) Objects.requireNonNull(spawnLocation.getWorld())).getHandle();
        val gameProfile = new GameProfile(uuid, displayName);

        val fetcher = NPCPlugin.getInstance().getSkinFetcher();

        val skin = Objects.equals(skinName, "Steve") ? fetcher.getDefaultSkin() : fetcher.getSkinFromName(skinName);
        gameProfile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));

        val entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile);
        entityPlayer.setLocation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        val packetPlayOutPlayerInfoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entityPlayer);
        val packetPlayOutPlayerInfoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.b, entityPlayer);
        val packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutPlayerInfoAdd);
            playerHandle.b.sendPacket(packetPlayOutNamedEntitySpawn);
            Bukkit.getScheduler().runTaskLaterAsynchronously(NPCPlugin.getInstance(), () -> playerHandle.b.sendPacket(packetPlayOutPlayerInfoRemove), 5L);
        });

        return entityPlayer;
    }

    @Override
    public void despawn(EntityPlayer entityPlayer) {
        val packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityPlayer.getId());

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutEntityDestroy);
        });
    }

    @Override
    public void hit(EntityPlayer entityPlayer) {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(entityPlayer, 0);

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutAnimation);
        });
    }

    @Override
    public void crouch(EntityPlayer entityPlayer, boolean crouch) {
        val dataWatcher = new DataWatcher(entityPlayer);
        dataWatcher.register(new DataWatcherObject<>(6, DataWatcherRegistry.s), (crouch ? EntityPose.f : EntityPose.a));

        val packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), dataWatcher, true);

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutEntityMetadata);
        });
    }

    @Override
    public void walk(EntityPlayer entityPlayer, Location location, Location oldLocation) {
        val x = getCoordinate(location.getX(), oldLocation.getX());
        val y = getCoordinate(location.getY(), oldLocation.getY());
        val z = getCoordinate(location.getZ(), oldLocation.getZ());

        val packetPlayOutRelEntityMoveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(entityPlayer.getId(), x, y, z, (byte) ((location.getYaw() * 256.0F) / 360.0F), (byte) location.getPitch(), true);
        val packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw() * 256.0F) / 360.0F));

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutRelEntityMoveLook);
            playerHandle.b.sendPacket(packetPlayOutEntityHeadRotation);
        });
    }

    private short getCoordinate(double newCoordinate, double oldCoordinate) {
        val coordinate = (newCoordinate * 32 - oldCoordinate * 32) * 128;

        return (short) coordinate;
    }

}

