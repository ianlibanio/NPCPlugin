package com.ianlibanio.npcplugin.adapter;

import com.google.gson.*;
import com.ianlibanio.npcplugin.utils.location.LocationSerializer;
import lombok.val;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        return LocationSerializer.deserialize(jsonElement.getAsJsonObject().get("location").getAsString());
    }

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        val jsonObject = new JsonObject();
        assert location != null;

        val serialized = LocationSerializer.serialize(location);

        jsonObject.add("location", new JsonPrimitive(serialized));

        return jsonObject;
    }
}
