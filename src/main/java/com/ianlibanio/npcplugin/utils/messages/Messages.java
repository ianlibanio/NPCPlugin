package com.ianlibanio.npcplugin.utils.messages;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.utils.messages.key.ReplacementKey;
import lombok.val;
import org.bukkit.ChatColor;

public class Messages {

    public static String getMessage(String message, ReplacementKey... keys) {
        String msg = ChatColor.translateAlternateColorCodes(
                '&',
                NPCPlugin.getInstance().getConfig().getString("messages." + message, "Message not found (" + message + ")")
        );

        for (val key : keys) {
            msg = msg.replace(key.getTarget(), key.getReplacement());
        }

        return msg;
    }

}
