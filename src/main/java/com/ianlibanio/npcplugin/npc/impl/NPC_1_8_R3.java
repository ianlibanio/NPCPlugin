package com.ianlibanio.npcplugin.npc.impl;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.npc.NPCBase;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;

public class NPC_1_8_R3 implements NPCBase<EntityPlayer> {

    @Override
    public EntityPlayer spawn(UUID uuid, String displayName, String skinName, Location spawnLocation) {
        val minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

        val worldServer = ((CraftWorld) Objects.requireNonNull(spawnLocation.getWorld())).getHandle();
        val gameProfile = new GameProfile(uuid, displayName);

        val fetcher = NPCPlugin.getInstance().getSkinFetcher();

        val skin = Objects.equals(skinName, "Steve") ? fetcher.getDefaultSkin() : fetcher.getSkinFromName(skinName);
        gameProfile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));

        val entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));
        entityPlayer.setLocation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        val packetPlayOutPlayerInfoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        val packetPlayOutPlayerInfoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        val packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
            playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
            Bukkit.getScheduler().runTaskLaterAsynchronously(NPCPlugin.getInstance(), () -> playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 5L);
        });

        return entityPlayer;
    }

    @Override
    public void despawn(EntityPlayer entityPlayer) {
        val packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityPlayer.getId());

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutEntityDestroy);
        });
    }

    @Override
    public void hit(EntityPlayer entityPlayer) {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(entityPlayer, 0);

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutAnimation);
        });
    }

    @Override
    public void crouch(EntityPlayer entityPlayer, boolean crouch) {
        val dataWatcher = new DataWatcher(entityPlayer);
        dataWatcher.a(0, (byte) (crouch ? 0x02 : 0));

        val packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), dataWatcher, true);

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutEntityMetadata);
        });
    }

    @Override
    public void walk(EntityPlayer entityPlayer, Location location, Location oldLocation) {
        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook packetPlayOutRelEntityMoveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                entityPlayer.getId(),
                (byte) MathHelper.floor((location.getX() - oldLocation.getX()) * 32.0D),
                (byte) MathHelper.floor((location.getY() - oldLocation.getY()) * 32.0D),
                (byte) MathHelper.floor((location.getZ() - oldLocation.getZ()) * 32.0D),
                (byte) ((int) (location.getYaw() * 256.0F / 360.0F)),
                (byte) ((int) (location.getPitch() * 256.0F / 360.0F)),
                true
        );

        val packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw() * 256.0F) / 360.0F));

        Bukkit.getOnlinePlayers().forEach(player -> {
            val playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutRelEntityMoveLook);
            playerConnection.sendPacket(packetPlayOutEntityHeadRotation);
        });
    }

    private short getCoordinate(double newCoordinate, double oldCoordinate) {
        val coordinate = (newCoordinate * 32 - oldCoordinate * 32) * 128;

        return (short) coordinate;
    }

    @SneakyThrows
    private void setPrivateField(Class type, Object object, String name, Object value) {
        Field field = type.getDeclaredField(name);

        field.setAccessible(true);
        field.set(object, value);
        field.setAccessible(false);
    }

}
