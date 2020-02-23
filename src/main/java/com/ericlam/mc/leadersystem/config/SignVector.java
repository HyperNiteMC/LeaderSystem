package com.ericlam.mc.leadersystem.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Map;

public final class SignVector implements ConfigurationSerializable {
    private final BlockVector vector;

    private SignVector(BlockVector vector) {
        this.vector = vector;
    }

    public static SignVector parse(Vector vector) {
        return new SignVector(vector.toBlockVector());
    }

    public static SignVector parse(BlockVector vector) {
        return new SignVector(vector);
    }

    public static SignVector deserialize(Map<String, Object> map) {
        return new SignVector(BlockVector.deserialize(map));
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        return vector.serialize();
    }

    public final BlockVector toBlockVector() {
        return vector;
    }

}
