package com.ianlibanio.npcplugin.data.frame;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@Getter
@AllArgsConstructor
public class Frame {

    private Location location;

    private transient long currentTime;
    private FrameAction frameAction;

}
