package com.ianlibanio.npcplugin.utils.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ianlibanio.npcplugin.NPCPlugin;
import lombok.SneakyThrows;
import lombok.val;

import java.io.InputStreamReader;
import java.net.URL;

public class SkinFetcher {

    @SneakyThrows
    public String[] getSkinFromName(String name) {
        URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
        String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

        URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
        JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
        String texture = textureProperty.get("value").getAsString();
        String signature = textureProperty.get("signature").getAsString();

        return new String[] { texture, signature };
    }

    public String[] getDefaultSkin() {
        val config = NPCPlugin.getInstance().getConfig();

        val texture = config.getString("default-skin.texture");
        val signature = config.getString("default-skin.signature");

        return new String[] { texture, signature };
    }

}
