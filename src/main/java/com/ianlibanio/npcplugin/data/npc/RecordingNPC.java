package com.ianlibanio.npcplugin.data.npc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RecordingNPC {

    private UUID uuid;
    private int tickTime;

}
