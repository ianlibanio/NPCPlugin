package com.ianlibanio.npcplugin.listener;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.controller.RecordingController;
import com.ianlibanio.npcplugin.data.frame.Frame;
import com.ianlibanio.npcplugin.data.frame.FrameAction;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerListener implements Listener {

    private final RecordingController recordingController = NPCPlugin.getInstance().getRecordingController();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        val player = event.getPlayer();

        if (recordingController.isRecording(player.getUniqueId())) {
            val npc = recordingController.getNPC(player.getUniqueId());
            val recordingNPC = recordingController.getRecordingNPC(player.getUniqueId());

            val tickTime = recordingNPC.getTickTime();

            npc.addFrame(new Frame(player.getLocation(), tickTime + 1, FrameAction.NONE));
            recordingNPC.setTickTime(tickTime + 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        val player = event.getPlayer();

        if (recordingController.isRecording(player.getUniqueId())) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                val npc = recordingController.getNPC(player.getUniqueId());
                val recordingNPC = recordingController.getRecordingNPC(player.getUniqueId());

                val tickTime = recordingNPC.getTickTime();

                val frameList = npc.getFrameList();
                val lastFrame = frameList.get(frameList.size() - 1);

                if (lastFrame.getFrameAction() == FrameAction.NONE && lastFrame.getCurrentTime() == tickTime)
                    frameList.remove(lastFrame);

                npc.addFrame(new Frame(player.getLocation(), tickTime + 1, FrameAction.HIT));
                recordingNPC.setTickTime(tickTime + 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        val player = event.getPlayer();

        if (recordingController.isRecording(player.getUniqueId())) {
            val npc = recordingController.getNPC(player.getUniqueId());
            val recordingNPC = recordingController.getRecordingNPC(player.getUniqueId());

            val tickTime = recordingNPC.getTickTime();

            val frameList = npc.getFrameList();
            val lastFrame = frameList.get(frameList.size() - 1);

            if (lastFrame.getFrameAction() == FrameAction.NONE && lastFrame.getCurrentTime() == tickTime)
                frameList.remove(lastFrame);

            if (event.isSneaking()) {
                npc.addFrame(new Frame(player.getLocation(), tickTime + 1, FrameAction.CROUCH));
            } else {
                npc.addFrame(new Frame(player.getLocation(), tickTime + 1, FrameAction.UNCROUCH));
            }

            recordingNPC.setTickTime(tickTime + 1);
        }
    }

}
