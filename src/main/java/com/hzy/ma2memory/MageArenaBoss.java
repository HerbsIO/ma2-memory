package com.hzy.ma2memory;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

public class MageArenaBoss {
    private String owner;
    private String name;
    private WorldPoint worldPoint;
    private BufferedImage mapImage;
    private boolean drawn;

    MageArenaBoss(String owner, String name, WorldPoint worldPoint) {
        drawn = false;
        this.owner = owner;
        this.name = name;
        this.worldPoint = worldPoint;
        switch(name.charAt(0)) {
            case 'P':
                mapImage = ImageUtil.loadImageResource(getClass(), "porazdir.png");
                break;
            case 'J':
                mapImage = ImageUtil.loadImageResource(getClass(), "zachariah.png");
                break;
            case 'D':
                mapImage = ImageUtil.loadImageResource(getClass(), "derwen.png");
                break;
        }
    }

    public String getOwner() {
        return owner;
    }
    public String getName() {
        return name;
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
        this.drawn = drawn;
    }
}
