package com.ianlibanio.npcplugin.utils.location;

import com.google.common.collect.Lists;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerializer {

    public static String serialize(Location location) {
        String worldName = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        return StringUtils.join(Lists.newArrayList(worldName, x, y, z, yaw, pitch), ":");
    }

    public static Location deserialize(String serialized) {
        val split = serialized.trim().split(":");

        val world = Bukkit.getWorld(split[0]);
        val x = Double.parseDouble(split[1]);
        val y = Double.parseDouble(split[2]);
        val z = Double.parseDouble(split[3]);
        val yaw = Float.parseFloat(split[4]);
        val pitch = Float.parseFloat(split[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

}
