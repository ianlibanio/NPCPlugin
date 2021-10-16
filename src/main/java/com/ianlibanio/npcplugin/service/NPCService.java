package com.ianlibanio.npcplugin.service;

import com.ianlibanio.npcplugin.data.npc.NPC;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NPCService {

    CompletableFuture<List<NPC>> load();

    void save(NPC... npcList);

    void delete(String name);

}
