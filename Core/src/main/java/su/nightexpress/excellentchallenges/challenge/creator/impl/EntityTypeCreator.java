package su.nightexpress.excellentchallenges.challenge.creator.impl;

import com.google.common.collect.Sets;
import su.nightexpress.excellentchallenges.ChallengesPlugin;
import su.nightexpress.excellentchallenges.Placeholders;
import su.nightexpress.excellentchallenges.challenge.action.ActionType;
import su.nightexpress.excellentchallenges.challenge.action.ActionTypes;
import su.nightexpress.excellentchallenges.challenge.creator.CreatorManager;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.wrapper.UniInt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityTypeCreator extends AbstractCreator<EntityType> {

    public EntityTypeCreator(@NotNull ChallengesPlugin plugin) {
        super(plugin);
    }

    @Override
    public void create() {

        this.createEntityBreedGenerators();
        this.createEntityKillGenerators();
        this.createEntityTameGenerators();
        this.createProjectileLaunchGenerators();
    }

    @NotNull
    @Override
    public Set<String> getConditions(@NotNull ActionType<?, EntityType> actionType) {
        if (actionType == ActionTypes.ENTITY_KILL) {
            return Sets.newHashSet(
                CreatorManager.CONDITIONS_SERVER_TIME,
                CreatorManager.CONDITIONS_ARMOR,
                CreatorManager.CONDITIONS_WEAPON,
                CreatorManager.CONDITIONS_PLAYER,
                CreatorManager.CONDITIONS_WORLD
            );
        }
        return Sets.newHashSet(
            CreatorManager.CONDITIONS_SERVER_TIME,
            CreatorManager.CONDITIONS_ARMOR,
            CreatorManager.CONDITIONS_PLAYER,
            CreatorManager.CONDITIONS_WORLD
        );
    }

    @NotNull
    @Override
    public Set<String> getRewards(@NotNull ActionType<?, EntityType> actionType) {
        return Sets.newHashSet(CreatorManager.REWARDS_MONEY, CreatorManager.REWARDS_ITEMS);
    }

    @NotNull
    @Override
    public UniInt getMinProgress(@NotNull ActionType<?, EntityType> actionType) {
        if (actionType == ActionTypes.ENTITY_BREED || actionType == ActionTypes.ENTITY_TAME) {
            return UniInt.of(2, 5);
        }
        return UniInt.of(12, 20);
    }

    @NotNull
    @Override
    public UniInt getMaxProgress(@NotNull ActionType<?, EntityType> actionType) {
        if (actionType == ActionTypes.ENTITY_BREED || actionType == ActionTypes.ENTITY_TAME) {
            return UniInt.of(6, 12);
        }
        return UniInt.of(20, 34);
    }

    private void createEntityKillGenerators() {
        Map<String, Set<EntityType>> map = new HashMap<>();

        Set<EntityType> spawnable = Stream.of(EntityType.values()).filter(e -> e.isAlive() && e.isSpawnable()).collect(Collectors.toSet());
        spawnable.forEach(type -> {
            if (type == EntityType.GIANT || type == EntityType.ARMOR_STAND) return;
            if (type == EntityType.WITHER || type == EntityType.ENDER_DRAGON || type == EntityType.ELDER_GUARDIAN) return;
            if (Version.isAbove(Version.V1_19_R3) && type == EntityType.ALLAY) return;

            Class<? extends Entity> clazz = type.getEntityClass();
            if (clazz == null) return;

            String group;
            if (Animals.class.isAssignableFrom(clazz)) {
                group = "animal";
            }
            else if (Raider.class.isAssignableFrom(clazz)) {
                group = "raider";
            }
            else if (Monster.class.isAssignableFrom(clazz)) {
                group = "monster";
            }
            else if (Fish.class.isAssignableFrom(clazz)) {
                group = "fish";
            }
            else group = "creature";

            map.computeIfAbsent(group, k -> new HashSet<>()).add(type);
        });

        this.createGenerator(ActionTypes.ENTITY_KILL, map);
        this.createGenerator(ActionTypes.ENTITY_SHOOT, map);
    }

    private void createEntityBreedGenerators() {
        Map<String, Set<EntityType>> map = new HashMap<>();

        Stream.of(EntityType.values()).filter(e -> e.isAlive() && e.isSpawnable()).forEach(type -> {
            Class<? extends Entity> clazz = type.getEntityClass();
            if (clazz != null && Breedable.class.isAssignableFrom(clazz)) {
                if (type == EntityType.WANDERING_TRADER) return;

                map.computeIfAbsent("animal", k -> new HashSet<>()).add(type);
            }
        });

        this.createGenerator(ActionTypes.ENTITY_BREED, map);
    }

    private void createEntityTameGenerators() {
        Map<String, Set<EntityType>> map = new HashMap<>();

        Stream.of(EntityType.values()).filter(e -> e.isAlive() && e.isSpawnable()).forEach(type -> {
            Class<? extends Entity> clazz = type.getEntityClass();
            if (clazz != null && Tameable.class.isAssignableFrom(clazz)) {
                if (type == EntityType.ZOMBIE_HORSE) return;
                if (type == EntityType.SKELETON_HORSE) return;

                map.computeIfAbsent("animal", k -> new HashSet<>()).add(type);
            }
        });

        this.createGenerator(ActionTypes.ENTITY_TAME, map);
    }

    private void createProjectileLaunchGenerators() {
        Map<String, Set<EntityType>> map = new HashMap<>();

        Stream.of(EntityType.values()).filter(e -> !e.isAlive()).forEach(type -> {
            Class<? extends Entity> clazz = type.getEntityClass();
            if (clazz != null && (ThrowableProjectile.class.isAssignableFrom(clazz) || clazz.equals(Arrow.class))) {
                map.computeIfAbsent(Placeholders.DEFAULT, k -> new HashSet<>()).add(type);
            }
        });

        this.createGenerator(ActionTypes.PROJECTILE_LAUNCH, map);
    }
}
