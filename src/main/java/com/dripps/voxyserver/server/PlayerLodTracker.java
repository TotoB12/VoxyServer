package com.dripps.voxyserver.server;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.level.ServerPlayer;

// tracks which lod sections have already been sent to a specific player
public class PlayerLodTracker {
    private final LongOpenHashSet sentSections = new LongOpenHashSet();
    private volatile boolean ready = false;
    private int lastChunkX;
    private int lastChunkZ;

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public synchronized boolean hasSent(long sectionKey) {
        return sentSections.contains(sectionKey);
    }

    public synchronized void markSent(long sectionKey) {
        sentSections.add(sectionKey);
    }

    public synchronized void reset() {
        sentSections.clear();
    }

    public synchronized void invalidate(long sectionKey) {
        sentSections.remove(sectionKey);
    }

    public int getLastChunkX() {
        return lastChunkX;
    }

    public int getLastChunkZ() {
        return lastChunkZ;
    }

    public void updatePosition(ServerPlayer player) {
        this.lastChunkX = player.getBlockX() >> 4;
        this.lastChunkZ = player.getBlockZ() >> 4;
    }

    public synchronized int sentCount() {
        return sentSections.size();
    }
}
