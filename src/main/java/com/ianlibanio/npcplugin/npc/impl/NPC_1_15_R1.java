package com.ianlibanio.npcplugin.npc.impl;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.npc.NPCBase;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class NPC_1_15_R1 implements NPCBase<EntityPlayer> {

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

        final EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));
        entityPlayer.setLocation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        final PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        final PacketPlayOutPlayerInfo packetPlayOutPlayerInfoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        final PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);

        Bukkit.getOnlinePlayers().forEach(player -> {
            final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
            playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
            Bukkit.getScheduler().runTaskLaterAsynchronously(NPCPlugin.getInstance(), () -> playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 5L);
        });

        return entityPlayer;
    }

    @Override
    public void despawn(EntityPlayer entityPlayer) {
        final PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityPlayer.getId());

        Bukkit.getOnlinePlayers().forEach(player -> {
            final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutEntityDestroy);
        });
    }

    @Override
    public void hit(EntityPlayer entityPlayer) {
        final PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(entityPlayer, 0);

        Bukkit.getOnlinePlayers().forEach(player -> {
            final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutAnimation);
        });
    }

    @Override
    public void crouch(EntityPlayer entityPlayer, boolean crouch) {
        final DataWatcher dataWatcher = new DataWatcher(entityPlayer);
        dataWatcher.register(new DataWatcherObject<>(6, DataWatcherRegistry.s), (crouch ? EntityPose.CROUCHING : EntityPose.STANDING));

        final PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), dataWatcher, true);

        Bukkit.getOnlinePlayers().forEach(player -> {
            final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutEntityMetadata);
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
            final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutRelEntityMoveLook);
            playerConnection.sendPacket(packetPlayOutEntityHeadRotation);
        });
    }

    private short getCoordinate(double newCoordinate, double oldCoordinate) {
        double coordinate = (newCoordinate * 32 - oldCoordinate * 32) * 128;

        return (short) coordinate;
    }
}
