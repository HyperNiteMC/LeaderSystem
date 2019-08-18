package com.ericlam.mc.leadersystem.sign;

import org.bukkit.World;
import org.bukkit.util.BlockVector;

import javax.annotation.Nullable;

public class SignData {
    private final String item;
    private final String uid;
    private final int rank;
    private final World world;
    private final BlockVector headLocation;

    public SignData(String item, String uid, int rank, World world, @Nullable BlockVector headLocation) {
        this.item = item;
        this.uid = uid;
        this.rank = rank;
        this.world = world;
        this.headLocation = headLocation;
    }

    public String getUid() {
        return uid;
    }

    public String getItem() {
        return item;
    }

    public int getRank() {
        return rank;
    }

    public World getWorld() {
        return world;
    }

    @Nullable
    public BlockVector getHeadLocation() {
        return headLocation;
    }
}
