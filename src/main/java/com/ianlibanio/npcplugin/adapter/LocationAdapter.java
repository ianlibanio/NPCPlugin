package com.ianlibanio.npcplugin.adapter;

import com.google.common.collect.Lists;
import com.google.gson.*;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    @SneakyThrows
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        val serialized = jsonElement.getAsJsonObject().get("location").getAsString();
        val split = serialized.trim().split(":");

        val world = Bukkit.getWorld(split[0]);
        val x = Double.parseDouble(split[1]);
        val y = Double.parseDouble(split[2]);
        val z = Double.parseDouble(split[3]);
        val yaw = Float.parseFloat(split[4]);
        val pitch = Float.parseFloat(split[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        val jsonObject = new JsonObject();
        assert location != null;

        val worldName = location.getWorld().getName();
        val x = location.getX();
        val y = location.getY();
        val z = location.getZ();
        val yaw = location.getYaw();
        val pitch = location.getPitch();

        val serialized = StringUtils.join(Lists.newArrayList(worldName, x, y, z, yaw, pitch), ":");

        jsonObject.add("location", new JsonPrimitive(serialized));

        return jsonObject;
    }
}
