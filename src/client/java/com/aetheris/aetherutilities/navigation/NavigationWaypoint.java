
package com.aetheris.aetherutilities.navigation;

import java.util.UUID;

public final class NavigationWaypoint {

    private final String id;

    private String name;
    private int x;
    private int y;
    private int z;
    private String dimension;
    private int color;
    private boolean enabled;

    public NavigationWaypoint(
            String name,
            int x,
            int y,
            int z,
            String dimension,
            int color
    ) {
        this(
                UUID.randomUUID().toString(),
                name,
                x,
                y,
                z,
                dimension,
                color,
                true
        );
    }

    public NavigationWaypoint(
            String id,
            String name,
            int x,
            int y,
            int z,
            String dimension,
            int color,
            boolean enabled
    ) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.color = color;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getDimension() {
        return dimension;
    }

    public int getColor() {
        return color;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
