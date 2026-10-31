package de.begleiter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Merkt sich pro Spieler seinen Begleiter (nicht gespeichert: nach Neustart/Relog neu mit /begleiter spawn). */
public final class CompanionManager {
    private static final Map<UUID, UUID> COMPANIONS = new HashMap<>();

    private CompanionManager() {}

    public static BegleiterEntity get(ServerPlayer owner) {
        UUID id = COMPANIONS.get(owner.getUUID());
        if (id == null) return null;
        if (!(owner.level() instanceof ServerLevel level)) return null;
        Entity e = level.getEntity(id);
        return e instanceof BegleiterEntity b && b.isAlive() ? b : null;
    }

    public static BegleiterEntity spawn(ServerPlayer owner) {
        remove(owner.getUUID(), owner.level());
        if (!(owner.level() instanceof ServerLevel level)) return null;
        BegleiterEntity b = BegleiterMod.BEGLEITER.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (b == null) return null;
        b.setOwner(owner);
        b.teleportTo(owner.getX() + 1.5, owner.getY(), owner.getZ());
        level.addFreshEntity(b);
        COMPANIONS.put(owner.getUUID(), b.getUUID());
        return b;
    }

    public static void remove(UUID ownerId) {
        COMPANIONS.remove(ownerId);
    }

    private static void remove(UUID ownerId, net.minecraft.world.level.Level level) {
        UUID id = COMPANIONS.remove(ownerId);
        if (id != null && level instanceof ServerLevel sl) {
            Entity e = sl.getEntity(id);
            if (e != null) e.discard();
        }
    }

    public static void despawn(ServerPlayer owner) {
        remove(owner.getUUID(), owner.level());
    }

    /** Alle 2 Sekunden: Wenn der Begleiter weg ist (z. B. Dimensionswechsel), neu neben den Spieler setzen. */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 40 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (COMPANIONS.containsKey(p.getUUID()) && get(p) == null) {
                spawn(p);
            }
        }
    }
}
