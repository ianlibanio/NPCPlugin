package com.ianlibanio.npcplugin.npc.impl;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.npc.NPCBase;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class NPC_1_8_R3 implements NPCBase<EntityPlayer> {

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
        dataWatcher.a(0, (byte) (crouch ? 0x02 : 0));

        final PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), dataWatcher, true);

        Bukkit.getOnlinePlayers().forEach(player -> {
            final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

            playerConnection.sendPacket(packetPlayOutEntityMetadata);
        });
    }

    @Override
    public void walk(EntityPlayer entityPlayer, Location location, Location oldLocation) {
        final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook packetPlayOutRelEntityMoveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                entityPlayer.getId(),
                (byte) MathHelper.floor((location.getX() - oldLocation.getX()) * 32.0D),
                (byte) MathHelper.floor((location.getY() - oldLocation.getY()) * 32.0D),
                (byte) MathHelper.floor((location.getZ() - oldLocation.getZ()) * 32.0D),
                (byte) ((int) (location.getYaw() * 256.0F / 360.0F)),
                (byte) ((int) (location.getPitch() * 256.0F / 360.0F)),
                true
        );

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

    @SneakyThrows
    private void setPrivateField(Class type, Object object, String name, Object value) {
        Field field = type.getDeclaredField(name);

        field.setAccessible(true);
        field.set(object, value);
        field.setAccessible(false);
    }

}
