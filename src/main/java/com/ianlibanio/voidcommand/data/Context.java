package com.ianlibanio.voidcommand.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class Context {

    private final Player player;
    private final CommandSender sender;

    private final String label;
    private final String[] args;


}
