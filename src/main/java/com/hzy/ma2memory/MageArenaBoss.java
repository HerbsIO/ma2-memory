package com.hzy.ma2memory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

public class MageArenaBoss {
    @Expose
    @SerializedName("o")
    private final String owner;
    @Expose
    @SerializedName("n")
    private final String name;

    @Expose
    @SerializedName("w")
    private final int[] worldLocation;

    private final WorldPoint worldPoint;

    private final BufferedImage mapImage;
    private boolean drawn;

    MageArenaBoss(String owner, String name, WorldPoint worldPoint) {
        drawn = false;
        this.owner = owner;
        this.name = name;
        this.worldPoint = worldPoint;
        worldLocation = new int[] {worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane()};

        if (name.split(" ").length == 2) {
            mapImage = ImageUtil.loadImageResource(MageArena2MemoryPlugin.class, "zachariah.png");
        } else {
            mapImage = ImageUtil.loadImageResource(MageArena2MemoryPlugin.class, name.toLowerCase() + ".png");
        }
    }

    public String getOwner() {
        return owner;
    }
    public String getName() {
        return name;
    }

    public int[] getWorldLocation() {
        return worldLocation;
    }
    public WorldPoint getWorldPoint() {
        return worldPoint;
    }

    public BufferedImage getMapImage() {
        return mapImage;
    }

    public boolean hasDrawn() {
        return drawn;
    }

    public void draw() {
        this.drawn = true;
    }
}
