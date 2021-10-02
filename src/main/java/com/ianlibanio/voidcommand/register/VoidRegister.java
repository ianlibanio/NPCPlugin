package com.ianlibanio.voidcommand.register;

import com.ianlibanio.voidcommand.VoidCommand;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

@AllArgsConstructor
public class VoidRegister {

    private final Plugin plugin;

    @SneakyThrows
    public void add(VoidCommand... commands) {
        val bukkitCommandMap = plugin.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);

        val commandMap = (CommandMap) bukkitCommandMap.get(plugin.getServer());

        for (VoidCommand command : commands) {
            commandMap.register(plugin.getName(), command);
        }
    }

}
