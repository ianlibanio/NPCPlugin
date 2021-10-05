package com.ianlibanio.npcplugin.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.data.frame.Frame;
import com.ianlibanio.npcplugin.data.frame.FrameAction;
import com.ianlibanio.npcplugin.data.npc.NPC;
import com.ianlibanio.npcplugin.utils.messages.Messages;
import com.ianlibanio.npcplugin.utils.messages.key.ReplacementKey;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NPCController {

    private List<NPC> npcList = Lists.newArrayList();
    private final Map<UUID, UUID> recording = Maps.newHashMap();

    @SneakyThrows
    public void load() {
        val gson = NPCPlugin.getInstance().getGson();
        val dataFile = new File(NPCPlugin.getInstance().getDataFolder(), "data.json");

        if (!dataFile.exists()) dataFile.createNewFile();

        val type = new TypeToken<ArrayList<NPC>>() {
        }.getType();
        val reader = new JsonReader(new FileReader(dataFile));

        List<NPC> list = gson.fromJson(reader, type);
        reader.close();

        if (list != null && !list.isEmpty()) this.npcList = list;
    }

    @SneakyThrows
    public void save() {
        val gson = NPCPlugin.getInstance().getGson();
        val dataFile = new File(NPCPlugin.getInstance().getDataFolder(), "data.json");
        val writer = new FileWriter(dataFile);

        gson.toJson(npcList, writer);

        writer.close();
    }

    public void createNPC(String displayName, String skinName) {
        val npc = new NPC(displayName, skinName);
        npcList.add(npc);
    }

    public boolean deleteNPC(String displayName) {
        val npc = this.getByName(displayName);

        if (npc == null) return false;

        if (npc.getTaskId() != -1) Bukkit.getScheduler().cancelTask(npc.getTaskId());
        npc.delete();

        this.save();
        return true;
    }

    public NPC getByName(String name) {
        return npcList.stream().filter(npc -> npc.getDisplayName().equals(name)).findFirst().orElse(null);
    }

    public NPC get(UUID uuid) {
        return npcList.stream().filter(npc -> npc.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void startRecording(UUID uuid, NPC npc, Player player, int seconds) {
        this.recording.put(uuid, npc.getUuid());

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i % 20 == 0) {
                    if (i == 0) {
                        player.sendMessage(Messages.getMessage("recording-now"));
                    } else {
                        if (i / 20 > seconds) {
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
                    val currentNPC = getRecordingNPC(player.getUniqueId());
                    val frameList = currentNPC.getFrameList();

                    if (frameList.size() == 0 || frameList.get(frameList.size() - 1).getCurrentTime() != System.currentTimeMillis()) {
                        currentNPC.addFrame(new Frame(player.getLocation(), System.currentTimeMillis(), FrameAction.NONE));
                    }
                }

                i++;
            }
        }.runTaskTimerAsynchronously(NPCPlugin.getInstance(), 60L, 1L);
    }

    public void stopRecording(UUID uuid) {
        val recordingNPC = getRecordingNPC(uuid);
        val npc = this.get(recordingNPC.getUuid());

        val frameList = recordingNPC.getFrameList();

        npc.setFrameList(frameList);
        npc.setLocation(frameList.get(0).getLocation());
        this.recording.remove(uuid);

        this.save();
    }

    public NPC getRecordingNPC(UUID playerId) {
        return this.get(this.recording.get(playerId));
    }

    public boolean isRecording(UUID uuid) {
        return recording.containsKey(uuid);
    }

}
