package com.ianlibanio.npcplugin.controller;

import com.google.common.collect.Maps;
import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.data.frame.Frame;
import com.ianlibanio.npcplugin.data.frame.FrameAction;
import com.ianlibanio.npcplugin.data.npc.NPC;
import com.ianlibanio.npcplugin.data.npc.RecordingNPC;
import com.ianlibanio.npcplugin.service.NPCService;
import com.ianlibanio.npcplugin.utils.messages.Messages;
import com.ianlibanio.npcplugin.utils.messages.key.ReplacementKey;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class RecordingController {

    private final Map<UUID, RecordingNPC> recording = Maps.newHashMap();

    private final NPCService npcService = NPCPlugin.getInstance().getNPCService();
    private final NPCController npcController = NPCPlugin.getInstance().getNpcController();

    public void startRecording(UUID uuid, NPC npc, Player player, int seconds) {
        this.recording.put(uuid, new RecordingNPC(npc.getUuid(), 0));

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i % 20 == 0) {
                    if (i == 0) {
                        player.sendMessage(Messages.getMessage("recording-now"));
                    } else {
                        if (i / 20 >= seconds) {
                            stopRecording(uuid);
                            player.sendMessage(Messages.getMessage("recording-ended"));
                            this.cancel();

                            return;
                        }

                        player.sendMessage(Messages.getMessage(
                                "recording-time",
                                new ReplacementKey("time", i / 20)
                        ));
                    }
                }

                if (isRecording(player.getUniqueId())) {
                    val currentNPC = getNPC(player.getUniqueId());
                    val frameList = currentNPC.getFrameList();

                    val recording = getRecordingNPC(player.getUniqueId());

                    if (frameList.size() == 0 || frameList.get(frameList.size() - 1).getCurrentTime() != i) {
                        currentNPC.addFrame(new Frame(player.getLocation(), i, FrameAction.NONE));
                        recording.setTickTime(i);
                    }
                }

                i++;
            }
        }.runTaskTimerAsynchronously(NPCPlugin.getInstance(), 60L, 1L);
    }

    public void stopRecording(UUID uuid) {
        val npc = this.getNPC(uuid);

        val frameList = npc.getFrameList();
        npc.setLocation(frameList.get(0).getLocation());

        npcService.save(npc);

        this.recording.remove(uuid);
    }

    public RecordingNPC getRecordingNPC(UUID playerId) {
        return this.recording.get(playerId);
    }

    public NPC getNPC(UUID playerId) {
        return npcController.find(getRecordingNPC(playerId).getUuid());
    }

    public boolean isRecording(UUID uuid) {
        return recording.containsKey(uuid);
    }

}
