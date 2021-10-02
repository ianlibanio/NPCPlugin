package com.ianlibanio.voidcommand;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ianlibanio.voidcommand.annotation.Command;
import com.ianlibanio.voidcommand.annotation.Sub;
import com.ianlibanio.voidcommand.data.Context;
import com.ianlibanio.voidcommand.data.Executor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class VoidCommand extends org.bukkit.command.Command {

    private Player player;
    private Command command;

    private final Map<String, Method> subMap = Maps.newHashMap();
    private final List<String> valid = Lists.newArrayList();

    public VoidCommand() {
        super("");

        for (Method method : this.getClass().getDeclaredMethods()) {
            val command = method.getAnnotation(Command.class);

            if (command != null && this.command == null) {
                this.command = command;
                setName(command.name());

                val aliases = command.aliases();

                if (!ArrayUtils.isEmpty(aliases)) setAliases(Arrays.asList(aliases));
            } else {
                val sub = method.getAnnotation(Sub.class);

                if (sub != null) subMap.put(sub.name(), method);
            }
        }
    }

    public abstract void command(Context context);

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        val executor = command.executor();

        if (!checkSender(executor, sender, command.permission())) return true;

        if (sender instanceof Player) this.player = (Player) sender;

        if (args.length == 0) {
            command(new Context(player, sender, label, args));
        } else {
            subMap.forEach((name, method) -> {
                val parameters = method.getParameterTypes();

                if (parameters.length == 1 && parameters[0].isAssignableFrom(Context.class)) {
                    val split = name.split(" ");

                    if (split.length == 0 || args.length < split.length) {
                        // this subcommand should not be used
                        return;
                    }

                    for (int i = 0; i < split.length; i++) {
                        if (!args[i].equalsIgnoreCase(split[i])) {
                            // this subcommand should not be used
                            return;
                        }
                    }

                    // it's valid! let's keep it on the list
                    valid.add(name);
                } else {
                    System.out.println("Invalid sub command executed: " + name + ".");
                }
            });

            if (valid.isEmpty()) {
                command(new Context(player, sender, label, args));
            } else {
                if (valid.size() != 1) {
                    AtomicReference<String> max = new AtomicReference<>("");

                    valid.forEach(name -> {
                        if (max.get().isEmpty() || name.split(" ").length > max.get().split(" ").length)
                            max.set(name);
                    });

                    valid.retainAll(Collections.singleton(max.get()));
                }

                val method = subMap.get(valid.get(0));
                invoke(method, new Context(player, sender, label, args), method.getAnnotation(Sub.class).executor());

                valid.clear();
            }
        }

        return false;
    }

    @SneakyThrows
    private void invoke(Method method, Context context, Executor executor) {
        if (checkSender(executor, context.sender(), method.getAnnotation(Sub.class).permission())) {
            method.invoke(this, context);
        }
    }

    private boolean checkSender(Executor executor, CommandSender sender, String permission) {
        val isPlayer = sender instanceof Player;

        if (executor.equals(Executor.PLAYER) && !isPlayer) {
            sender.sendMessage(ChatColor.RED + "You can't use this command into the " + ChatColor.BOLD + "CONSOLE" + ChatColor.RED + ".");
            return false;
        }

        if (executor.equals(Executor.CONSOLE) && isPlayer) {
            sender.sendMessage(ChatColor.RED + "You can't use this command as a " + ChatColor.BOLD + "PLAYER" + ChatColor.RED + ".");
            return false;
        }

        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have the permission '" + ChatColor.BOLD + permission + ChatColor.RED + "' to use this command!");
            return false;
        }

        return true;
    }

}
