package com.ianlibanio.npcplugin.controller;

import com.google.common.collect.Lists;
import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.data.npc.NPC;
import com.ianlibanio.npcplugin.service.NPCService;
import lombok.val;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class NPCController {

    private final List<NPC> npcList = Lists.newArrayList();

    private NPCService service;

    public CompletableFuture<Void> constructor(NPCPlugin plugin) {
        this.service = plugin.getNPCService();

        return this.service.load().thenAcceptAsync(find -> this.npcList.addAll(find), plugin.getExecutor());
    }

    public void destructor() {
        npcList.forEach(service::save);
    }

    public void create(String displayName, String skinName) {
        val npc = new NPC(displayName, skinName);
        npcList.add(npc);
    }

    public NPC find(String name) {
        return npcList.stream().filter(npc -> npc.getDisplayName().equals(name)).findFirst().orElse(null);
    }

    public NPC find(UUID uuid) {
        return npcList.stream().filter(npc -> npc.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public boolean delete(String name) {
        NPC npc = this.find(name);

        if (npc == null) return false;

        this.npcList.remove(npc);
        this.service.delete(name);

        return true;
    }

    public Stream<NPC> stream() {
        return this.npcList.stream();
    }

}
