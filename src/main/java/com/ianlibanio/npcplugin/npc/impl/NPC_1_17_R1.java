package com.ianlibanio.npcplugin.npc.impl;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.npc.NPCBase;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityPose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class NPC_1_17_R1 implements NPCBase<EntityPlayer> {

    @Override
    public EntityPlayer spawn(UUID uuid, String displayName, String skinName, Location spawnLocation) {
        final MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

        final WorldServer worldServer = ((CraftWorld) Objects.requireNonNull(spawnLocation.getWorld())).getHandle();
        final GameProfile gameProfile = new GameProfile(uuid, displayName);

        final Optional<String[]> skin = NPCPlugin.getInstance().getSkinFetcher().fetchSkin(skinName);

        if (skin.isPresent()) {
            final String[] properties = skin.get();

            gameProfile.getProperties().put("textures", new Property("textures", properties[0], properties[1]));
        }

        final EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile);
        entityPlayer.setLocation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        final PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entityPlayer);
        final PacketPlayOutPlayerInfo packetPlayOutPlayerInfoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.b, entityPlayer);
        final PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);

        Bukkit.getOnlinePlayers().forEach(player -> {
            final EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutPlayerInfoAdd);
            playerHandle.b.sendPacket(packetPlayOutNamedEntitySpawn);
            Bukkit.getScheduler().runTaskLaterAsynchronously(NPCPlugin.getInstance(), () -> playerHandle.b.sendPacket(packetPlayOutPlayerInfoRemove), 5L);
        });

        return entityPlayer;
    }

    @Override
    public void despawn(EntityPlayer entityPlayer) {
        final PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityPlayer.getId());

        Bukkit.getOnlinePlayers().forEach(player -> {
            final EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutEntityDestroy);
        });
    }

    @Override
    public void hit(EntityPlayer entityPlayer) {
        final PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(entityPlayer, 0);

        Bukkit.getOnlinePlayers().forEach(player -> {
            final EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutAnimation);
        });
    }

    @Override
    public void crouch(EntityPlayer entityPlayer, boolean crouch) {
        final DataWatcher dataWatcher = new DataWatcher(entityPlayer);
        dataWatcher.register(new DataWatcherObject<>(6, DataWatcherRegistry.s), (crouch ? EntityPose.f : EntityPose.a));

        final PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), dataWatcher, true);

        Bukkit.getOnlinePlayers().forEach(player -> {
            final EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutEntityMetadata);
        });
    }

    @Override
    public void walk(EntityPlayer entityPlayer, Location location, Location oldLocation) {
        short x = getCoordinate(location.getX(), oldLocation.getX());
        short y = getCoordinate(location.getY(), oldLocation.getY());
        short z = getCoordinate(location.getZ(), oldLocation.getZ());

        final PacketPlayOutEntity packetPlayOutRelEntityMoveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(entityPlayer.getId(), x, y, z, (byte) ((location.getYaw() * 256.0F) / 360.0F), (byte) location.getPitch(), true);
        final PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw() * 256.0F) / 360.0F));

        Bukkit.getOnlinePlayers().forEach(player -> {
            final EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();

            playerHandle.b.sendPacket(packetPlayOutRelEntityMoveLook);
            playerHandle.b.sendPacket(packetPlayOutEntityHeadRotation);
        });
    }

    private short getCoordinate(double newCoordinate, double oldCoordinate) {
        double coordinate = (newCoordinate * 32 - oldCoordinate * 32) * 128;

        return (short) coordinate;
    }

}

