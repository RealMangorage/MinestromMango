package org.mangorage.server.entities;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class Mine extends EntityCreature {
    private boolean handled = false;

    public Mine(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);
        addAIGroup(
                List.of(
                        new MeleeAttackGoal(this, 1.6, 20, TimeUnit.SERVER_TICK), // Attack the target
                        new RandomStrollGoal(this, 20) // Walk around
                ),
                List.of(
                        new LastEntityDamagerTarget(this, 32), // First target the last entity which attacked you
                        new ClosestEntityTarget(this, 32, entity -> entity instanceof Player) // If there is none, target the nearest player
                )
        );
    }

    public Mine(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (isDead() && !handled) {
            this.handled = true;
            scheduleRemove(10 * 50, ChronoUnit.MILLIS);
        }
    }
}
