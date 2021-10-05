package com.ianlibanio.npcplugin.commands;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.utils.messages.Messages;
import com.ianlibanio.npcplugin.utils.messages.key.ReplacementKey;
import com.ianlibanio.voidcommand.VoidCommand;
import com.ianlibanio.voidcommand.annotation.Command;
import com.ianlibanio.voidcommand.annotation.Sub;
import com.ianlibanio.voidcommand.data.Context;
import com.ianlibanio.voidcommand.data.Executor;
import lombok.val;

public class NPCCommand extends VoidCommand {

    @Command(
            name = "npc",
            executor = Executor.PLAYER
    )
    public void command(Context context) {
        context.player().sendMessage(Messages.getMessage("command-help"));
    }

    @Sub(
            name = "create",
            permission = "npc.admin.create"
    )
    public void create(Context context) {
        val args = context.args();
        val player = context.player();

        if (args.length < 2) {
            player.sendMessage(Messages.getMessage("create-help"));
            return;
        }

        val name = args[1];
        val skin = args.length > 2 ? args[2] : "Steve";

        NPCPlugin.getInstance().getConfirmationInventory().getInventory(true, name, (accepted) -> {
            if (accepted) {
                val npcController = NPCPlugin.getInstance().getNpcController();

                if (npcController.getByName(name) != null)
                    player.sendMessage(Messages.getMessage("npc-already-exists"));

                npcController.createNPC(name, skin);
                player.sendMessage(Messages.getMessage(
                        "create-success",
                        new ReplacementKey("name", name),
                        new ReplacementKey("skin", skin)
                ));
            } else {
                player.sendMessage(Messages.getMessage(
                        "create-cancelled",
                        new ReplacementKey("name", name)
                ));
            }
        }).open(player);
    }

    @Sub(
            name = "rec",
            permission = "npc.admin.rec"
    )
    public void rec(Context context) {
        val args = context.args();
        val player = context.player();

        if (args.length < 3) {
            player.sendMessage(Messages.getMessage("rec-help"));
            return;
        }

        val name = args[1];
        int seconds;

        try {
            seconds = Integer.parseInt(args[2]);
        } catch (Exception exception) {
            player.sendMessage(Messages.getMessage("rec-invalid-seconds"));
            return;
        }

        val npcController = NPCPlugin.getInstance().getNpcController();
        val npc = npcController.getByName(name);

        if (npc == null) {
            player.sendMessage(Messages.getMessage("npc-does-not-exists"));
            return;
        }

        player.sendMessage(Messages.getMessage(
                "rec-starting",
                new ReplacementKey("name", name)
        ));

        npcController.startRecording(player.getUniqueId(), npc, player, seconds);
    }

    @Sub(
            name = "delete",
            permission = "npc.admin.delete"
    )
    public void delete(Context context) {
        val args = context.args();
        val player = context.player();

        if (args.length < 2) {
            player.sendMessage(Messages.getMessage("delete-help"));
            return;
        }

        val name = args[1];

        NPCPlugin.getInstance().getConfirmationInventory().getInventory(false, name, (accepted) -> {
            if (accepted) {
                val npcController = NPCPlugin.getInstance().getNpcController();

                boolean deleted = npcController.deleteNPC(name);

                if (deleted) {
                    player.sendMessage(Messages.getMessage(
                            "delete-success",
                            new ReplacementKey("name", name)
                    ));
                } else {
                    player.sendMessage(Messages.getMessage("npc-does-not-exists"));
                }
            } else {
                player.sendMessage(Messages.getMessage(
                        "delete-canceled",
                        new ReplacementKey("name", name)
                ));
            }
        }).open(player);
    }

    @Sub(
            name = "play",
            permission = "npc.admin.play"
    )
    public void play(Context context) {
        val args = context.args();
        val player = context.player();

        if (args.length < 2) {
            player.sendMessage(Messages.getMessage("play-help"));
            return;
        }

        val name = args[1];

        val npcController = NPCPlugin.getInstance().getNpcController();
        val npc = npcController.getByName(name);

        if (npc == null) {
            player.sendMessage(Messages.getMessage("npc-does-not-exists"));
            return;
        }

        npc.spawn();
        npc.exec();
        player.sendMessage(Messages.getMessage(
                "play-playing",
                new ReplacementKey("name", name)
        ));
    }

}
