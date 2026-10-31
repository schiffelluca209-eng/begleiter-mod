package de.begleiter;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BegleiterEntity extends PathfinderMob {
    private UUID ownerId;
    private boolean sleeping;
    private int fearCooldown;
    private int calmTicks;

    private static final EntityDataAccessor<String> SKIN =
            SynchedEntityData.defineId(BegleiterEntity.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, "default");
    }

    public String getSkin() {
        return entityData.get(SKIN);
    }

    public void setSkin(String name) {
        entityData.set(SKIN, name);
    }

    public BegleiterEntity(EntityType<? extends BegleiterEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public void setOwner(ServerPlayer p) {
        this.ownerId = p.getUUID();
    }

    public ServerPlayer getOwnerPlayer() {
        if (ownerId == null || !(level() instanceof ServerLevel sl)) return null;
        return sl.getServer().getPlayerList().getPlayer(ownerId);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new FollowOwnerGoal(this));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public boolean isSleepingNow() {
        return sleeping;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel sl)) return;
        ServerPlayer owner = getOwnerPlayer();
        if (owner == null) {
            discard();
            return;
        }
        if (fearCooldown > 0) fearCooldown--;

        // --- Schlafen: legt sich hin, solange der Spieler im Bett schläft ---
        if (owner.isSleeping() && !isPassenger()) {
            if (!sleeping) {
                sleeping = true;
                getNavigation().stop();
                say(owner, "Gute Nacht... *gähn*");
            }
            setPose(Pose.SLEEPING);
            return;
        } else if (sleeping) {
            sleeping = false;
            setPose(Pose.STANDING);
            say(owner, "Guten Morgen!");
        }

        // --- Gefahr: Monster in der Nähe von mir oder dem Spieler ---
        List<Monster> monsters = sl.getEntitiesOfClass(Monster.class, owner.getBoundingBox().inflate(14.0));
        Monster nearest = null;
        double best = Double.MAX_VALUE;
        for (Monster m : monsters) {
            double d = m.distanceToSqr(this);
            if (d < best) {
                best = d;
                nearest = m;
            }
        }

        if (nearest != null) {
            calmTicks = 0;
            if (fearCooldown == 0) {
                say(owner, best < 49 ? "Hilfe, ein " + nearest.getName().getString() + "! Ich habe Angst!"
                        : "Da hinten ist ein " + nearest.getName().getString() + "... mir ist unheimlich.");
                fearCooldown = 20 * 15;
            }
            if (best < 49 && !isPassenger()) { // näher als 7 Blöcke: auf den Spieler klettern
                startRiding(owner, true);
            }
        } else if (isPassenger()) {
            calmTicks++;
            if (calmTicks > 20 * 5) {
                stopRiding();
                calmTicks = 0;
                say(owner, "Puh... ich glaube, es ist vorbei.");
            }
        }
    }

    private void say(ServerPlayer owner, String text) {
        owner.sendSystemMessage(Component.literal("<Begleiter> " + text));
    }

    /** Einfache Antworten nach Stichwort (keine KI, kann später an ein Sprachmodell angebunden werden). */
    public String replyTo(String input) {
        String t = input.toLowerCase(Locale.ROOT);
        if (t.contains("hallo") || t.contains("hi") || t.contains("hey")) return "Hallo! Schön, dass du da bist.";
        if (t.contains("wie geht")) return isPassenger() ? "Ich sitze auf dir und zittere ein bisschen..." : "Mir geht's gut, solange keine Monster da sind.";
        if (t.contains("müde") || t.contains("schlaf")) return "Wenn du dich ins Bett legst, lege ich mich auch hin.";
        if (t.contains("angst")) return "Ja, bei Zombies und Skeletten werde ich nervös. Bleib bei mir!";
        if (t.contains("name")) return "Ich bin dein Begleiter.";
        if (t.contains("danke")) return "Immer gern!";
        return "Hm, ich bin nicht sicher, was ich dazu sagen soll. Aber ich bleibe bei dir.";
    }

    private static class FollowOwnerGoal extends Goal {
        private final BegleiterEntity mob;
        private int recalc;

        FollowOwnerGoal(BegleiterEntity mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            ServerPlayer o = mob.getOwnerPlayer();
            return o != null && !mob.sleeping && !mob.isPassenger() && mob.distanceToSqr(o) > 9.0;
        }

        @Override
        public boolean canContinueToUse() {
            ServerPlayer o = mob.getOwnerPlayer();
            return o != null && !mob.sleeping && !mob.isPassenger() && mob.distanceToSqr(o) > 4.0;
        }

        @Override
        public void tick() {
            ServerPlayer o = mob.getOwnerPlayer();
            if (o == null) return;
            if (mob.distanceToSqr(o) > 24.0 * 24.0) {
                mob.teleportTo(o.getX(), o.getY(), o.getZ());
                return;
            }
            if (--recalc <= 0) {
                recalc = 10;
                mob.getNavigation().moveTo(o, 1.2);
            }
        }
    }
}
