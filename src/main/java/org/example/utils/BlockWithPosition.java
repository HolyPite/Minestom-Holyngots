package org.example.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public record BlockWithPosition(Block block, int x, int y, int z) {
    public Pos pos() {
        return new Pos(x,y,z);
    }
}
