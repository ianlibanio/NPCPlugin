package com.ianlibanio.npcplugin.data.npc;

import com.google.common.collect.Lists;
import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.data.frame.Frame;
import lombok.Data;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

@Data
public class NPC {

    private Location location;
    private transient Object entityPlayer;

    private UUID uuid;
    private String skinName;
    private String displayName;

    private List<Frame> frameList;
    private transient int taskId = -1;

    public NPC(String displayName, String skinName) {
        this.displayName = displayName;
        this.skinName = skinName;

        this.uuid = UUID.randomUUID();
        this.frameList = Lists.newArrayList();
    }

    public void addFrame(Frame frame) {
        frameList.add(frame);
    }

    public void spawn() {
        this.entityPlayer = NPCPlugin.getInstance().getNpcBase().spawn(uuid, displayName, skinName, location);
    }

    public void exec() {
        val npcBase = NPCPlugin.getInstance().getNpcBase();

        BukkitTask task = new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i >= frameList.size() - 1) {
                    npcBase.despawn(entityPlayer);
                    this.cancel();

                    return;
                }

                val currentFrame = frameList.get(i);

                if (i >= 2) {
                    Frame oldFrame = frameList.get(i - 1);

                    npcBase.walk(entityPlayer, currentFrame.getLocation(), oldFrame.getLocation());
                }

                switch (currentFrame.getFrameAction()) {
                    case HIT:
                        npcBase.hit(entityPlayer);
                        break;
                    case CROUCH:
                        npcBase.crouch(entityPlayer, true);
                        break;
                    case UNCROUCH:
                        npcBase.crouch(entityPlayer, false);
                        break;
                }

                i++;
            }
        }.runTaskTimerAsynchronously(NPCPlugin.getInstance(), 0L, 1L);

        this.taskId = task.getTaskId();
    }

    public void delete() {
        val npcBase = NPCPlugin.getInstance().getNpcBase();

        npcBase.despawn(entityPlayer);
    }

}
