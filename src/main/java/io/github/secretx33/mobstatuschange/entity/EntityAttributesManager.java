package io.github.secretx33.mobstatuschange.entity;

import io.github.secretx33.mobstatuschange.config.Messages;
import javafx.util.Pair;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.floor;

@ParametersAreNonnullByDefault
public class EntityAttributesManager {

    private final Map<Pair<EntityType, Boolean>, EntityAttributes> entityAttributesMap = new HashMap<>();
    private final Plugin plugin;
    private final Logger logger;

    public EntityAttributesManager(final Plugin plugin, final Logger logger) {
        checkNotNull(plugin, "plugin cannot be null");
        checkNotNull(logger, "logger cannot be null");
        this.plugin = plugin;
        this.logger = logger;
        refreshConfig();
    }

    public void refreshConfig(){
        populateEntityArray();
        reloadEntityAttributes();
    }

    private void reloadEntityAttributes(){
        // Removing old and applying new value to entities
        plugin.getServer().getWorlds().forEach(world -> world.getLivingEntities().forEach(this::setAttributesFor));
    }

    private void populateEntityArray() {
        entityAttributesMap.clear();

        ConfigurationSection entitiesConfig = plugin.getConfig().getConfigurationSection("entities");
        // If "entities" configuration section is not found
        if (entitiesConfig == null) {
            logger.severe(String.format(Messages.SECTION_NOT_FOUND, "entities"));
            return;
        }

        Arrays.stream(EntityType.values())
            .filter(type -> type.getEntityClass() != null)
            .forEach(type -> addAttributeEntryFor(entitiesConfig, type, false));
    }

    private void addAttributeEntryFor(final ConfigurationSection entitiesConfig, final EntityType entityType, final boolean isBaby) {
        String name = String.format("%s%s", (isBaby) ? "baby_" : "", entityType.toString().toLowerCase(Locale.US));
        ConfigurationSection sect = entitiesConfig.getConfigurationSection(name);
        if(sect == null) return;

        double hpMod              = sect.getDouble("hp"              , Double.MIN_VALUE);
        double followRangeMod     = sect.getDouble("follow-range"    , Double.MIN_VALUE);
        double atkDamageMod       = sect.getDouble("atk-damage"      , Double.MIN_VALUE);
        double atkKnockBackMod    = sect.getDouble("atk-knockback"   , Double.MIN_VALUE);
        double knockBackResistMod = sect.getDouble("knockback-resist", Double.MIN_VALUE);
        double movSpeedMod        = sect.getDouble("move-speed"      , Double.MIN_VALUE);

        final EntityAttributes attrib;
        final Double arg1;
        final Double arg2;

        switch(entityType) {
            case PLAYER:
                arg1 = (sect.isSet("atk-speed")) ? sect.getDouble("atk-speed") : null;
                arg2 = (sect.isSet("luck"))      ? sect.getDouble("luck") : null;
                attrib = new EntityAttributes(entityType, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, arg1, AttributeModifier.Operation.ADD_SCALAR, arg2, AttributeModifier.Operation.ADD_NUMBER, null, null);
                break;
            case ZOMBIE:
                arg1 = (sect.isSet("spawn-reinforcements")) ? sect.getDouble("spawn-reinforcements") : null;
                attrib = new EntityAttributes(entityType, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, arg1, AttributeModifier.Operation.ADD_NUMBER, null, null, null, null);
                break;
            case BEE:
            case PARROT:
                arg1 = (sect.isSet("flying-speed")) ? sect.getDouble("flying-speed") : null;
                attrib = new EntityAttributes(entityType, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, arg1, AttributeModifier.Operation.ADD_SCALAR, null, null, null, null);
                break;
            case CREEPER:
                arg1 = (sect.isSet("max-fuse-time"))    ? (double)sect.getInt("max-fuse-time") : null;
                arg2 = (sect.isSet("explosion-radius")) ? (double)sect.getInt("explosion-radius") : null;
                attrib = new EntityAttributes(entityType, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, null, null, null, null, arg1, arg2);
                break;
            default:
                attrib = new EntityAttributes(entityType, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod);
                break;
        }
        entityAttributesMap.put(new Pair<>(entityType, isBaby), attrib);

        if(!isBaby && Ageable.class.isAssignableFrom(entityType.getEntityClass())) addAttributeEntryFor(entitiesConfig, entityType, true);
    }

    public void setAttributesFor(final LivingEntity entity) {
        final EntityAttributes attribs = getAttributesOf(entity);
        if(attribs == null) return;

        final AttributeInstance hp = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        final AttributeInstance followRange = entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
        final AttributeInstance atkKnockBack = entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
        final AttributeInstance knockBackResist = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        final AttributeInstance movSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        if(hp != null && attribs.hpMod != null) {
            double currentHP = entity.getHealth();
            double maxHP = hp.getValue();
            double percentHP = currentHP / maxHP;
            hp.removeModifier(attribs.hpMod);
            hp.addModifier(attribs.hpMod);
            entity.setHealth(hp.getValue() * percentHP); // Preserve the entity HP percentage when modifying HP
        }
        if(followRange != null && attribs.followRangeMod != null){
            followRange.removeModifier(attribs.followRangeMod);
            followRange.addModifier(attribs.followRangeMod);
        }
        if(atkKnockBack != null && attribs.atkKnockBackMod != null){
            atkKnockBack.removeModifier(attribs.atkKnockBackMod);
            atkKnockBack.addModifier(attribs.atkKnockBackMod);
        }
        if(knockBackResist != null && attribs.knockBackResistMod != null){
            knockBackResist.removeModifier(attribs.knockBackResistMod);
            knockBackResist.addModifier(attribs.knockBackResistMod);
        }
        if(movSpeed != null && attribs.movSpeedMod != null){
            movSpeed.removeModifier(attribs.movSpeedMod);
            movSpeed.addModifier(attribs.movSpeedMod);
        }

        final AttributeInstance arg1;
        final AttributeInstance arg2;

        // Special living entities
        switch(entity.getType()) {
            case PLAYER:
                arg1 = entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                arg2 = entity.getAttribute(Attribute.GENERIC_LUCK);

                // Attack speed
                if(arg1 != null && attribs.arg1 != null){
                    arg1.removeModifier(attribs.arg1);
                    arg1.addModifier(attribs.arg1);
                }
                // Luck
                if(arg2 != null && attribs.arg2 != null){
                    arg2.removeModifier(attribs.arg2);
                    arg2.addModifier(attribs.arg2);
                }
                break;
            case ZOMBIE:
                arg1 = entity.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);
                // Spawn Reinforcements
                if (arg1 != null && attribs.arg1 != null) {
                    arg1.removeModifier(attribs.arg1);
                    arg1.addModifier(attribs.arg1);
                }
                break;
            case BEE:
            case PARROT:
                arg1 = entity.getAttribute(Attribute.GENERIC_FLYING_SPEED);
                // Spawn Reinforcements
                if (arg1 != null && attribs.arg1 != null) {
                    arg1.removeModifier(attribs.arg1);
                    arg1.addModifier(attribs.arg1);
                }
                break;
            case CREEPER:
                if(attribs.arg3 != null) ((Creeper)entity).setMaxFuseTicks((int) floor(attribs.arg3));
                if(attribs.arg4 != null) ((Creeper)entity).setExplosionRadius((int) floor(attribs.arg4));
                break;
            default:
                break;
        }
    }

    @Nullable
    public EntityAttributes getAttributesOf(final Entity entity) {
        checkNotNull(entity, "entity cannot be null");
        return entityAttributesMap.get(new Pair<>(entity.getType(), isBaby(entity)));
    }

    private boolean isBaby(final Entity entity) {
        return entity instanceof Ageable && !((Ageable) entity).isAdult();
    }
}
