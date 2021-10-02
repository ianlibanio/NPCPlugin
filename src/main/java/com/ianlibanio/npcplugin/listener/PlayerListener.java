package com.ianlibanio.npcplugin.listener;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.controller.NPCController;
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

    private final NPCController npcController = NPCPlugin.getInstance().getNpcController();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        val player = event.getPlayer();

        if (npcController.isRecording(player.getUniqueId())) {
            npcController.getRecordingNPC(player.getUniqueId()).addFrame(new Frame(event.getTo(), System.currentTimeMillis(), FrameAction.NONE));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        val player = event.getPlayer();

        if (npcController.isRecording(player.getUniqueId())) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                val frameList = npcController.getRecordingNPC(player.getUniqueId()).getFrameList();
                val lastFrame = frameList.get(frameList.size() - 1);

                if (lastFrame.getFrameAction() == FrameAction.NONE && lastFrame.getCurrentTime() == System.currentTimeMillis()) frameList.remove(lastFrame);

                npcController.getRecordingNPC(player.getUniqueId()).addFrame(new Frame(player.getLocation(), System.currentTimeMillis(),  FrameAction.HIT));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        val player = event.getPlayer();

        if (npcController.isRecording(player.getUniqueId())) {
            val frameList = npcController.getRecordingNPC(player.getUniqueId()).getFrameList();
            val lastFrame = frameList.get(frameList.size() - 1);

            if (lastFrame.getFrameAction() == FrameAction.NONE && lastFrame.getCurrentTime() == System.currentTimeMillis()) frameList.remove(lastFrame);

            if (event.isSneaking()) {
                npcController.getRecordingNPC(player.getUniqueId()).addFrame(new Frame(player.getLocation(), System.currentTimeMillis(), FrameAction.CROUCH));
            } else {
                npcController.getRecordingNPC(player.getUniqueId()).addFrame(new Frame(player.getLocation(), System.currentTimeMillis(), FrameAction.UNCROUCH));
            }
        }
    }

}
