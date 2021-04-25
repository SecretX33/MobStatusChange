package io.github.secretx33.mobstatuschange.entity;

import io.github.secretx33.mobstatuschange.config.Const;
import io.github.secretx33.mobstatuschange.utils.Utils;
import javafx.util.Pair;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.floor;

public class EntityAttributeManager {

    private final Map<Pair<EntityType, Boolean>, EntityAttributes> entityAttributesMap = new HashMap<>();
    private final Plugin plugin;

    public EntityAttributeManager(final Plugin plugin) {
        checkNotNull(plugin, "plugin cannot be null");
        this.plugin = plugin;
        refreshConfig();
    }

    public void refreshConfig(){
        populateEntityArray();
        reloadEntityAttributes();
    }

    public void populateEntityArray() {
        final FileConfiguration config = plugin.getConfig();
        entityAttributesMap.clear();

        String field = "entities";
        ConfigurationSection mobConfig = null;
        if (config.isSet(field)) mobConfig = config.getConfigurationSection(field);
        if (mobConfig == null) {
            Utils.consoleMessage(String.format(Const.SECTION_NOT_FOUND, field));
            return;
        }

        String name;
        boolean isBaby = false;
        ConfigurationSection sect;
        Double hpMod;
        Double followRangeMod;
        Double atkDamageMod;
        Double atkKnockBackMod;
        Double knockBackResistMod;
        Double movSpeedMod;
        Double arg1;
        Double arg2;

        for(int i=0; i<2; i++){
            for (EntityType e: EntityType.values()) {
                if (!isBaby || (e.getEntityClass() != null && Ageable.class.isAssignableFrom(e.getEntityClass()))) {
                    name = e.toString().toLowerCase(Locale.US);
                    if (isBaby) name = "baby_" + name;
                    sect = mobConfig.getConfigurationSection(name);

                    if (sect != null) {
                        hpMod = null;
                        followRangeMod = null;
                        atkDamageMod = null;
                        atkKnockBackMod = null;
                        knockBackResistMod = null;
                        movSpeedMod = null;
                        arg1 = null;
                        arg2 = null;
                        try {
                            if (sect.isSet("hp")) hpMod = sect.getDouble("hp");
                            if (sect.isSet("follow-range")) followRangeMod = sect.getDouble("follow-range");
                            if (sect.isSet("atk-damage")) atkDamageMod = sect.getDouble("atk-damage");
                            if (sect.isSet("atk-knockback")) atkKnockBackMod = sect.getDouble("atk-knockback");
                            if (sect.isSet("knockback-resist")) knockBackResistMod = sect.getDouble("knockback-resist");
                            if (sect.isSet("move-speed")) movSpeedMod = sect.getDouble("move-speed");

                            if (e == EntityType.PLAYER) {
                                if (sect.isSet("atk-speed")) arg1 = sect.getDouble("atk-speed");
                                if (sect.isSet("luck")) arg2 = sect.getDouble("luck");
                                entityArray.add(new EntityAttributes(e, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, arg1, AttributeModifier.Operation.ADD_SCALAR, arg2, AttributeModifier.Operation.ADD_NUMBER, null, null));
                            } else if (e == EntityType.ZOMBIE) {
                                if (sect.isSet("spawn-reinforcements")) arg1 = sect.getDouble("spawn-reinforcements");
                                entityArray.add(new EntityAttributes(e, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, arg1, AttributeModifier.Operation.ADD_NUMBER, null, null, null, null));
                            } else if (e == EntityType.BEE || e == EntityType.PARROT) {
                                if (sect.isSet("flying-speed")) arg1 = sect.getDouble("flying-speed");
                                entityArray.add(new EntityAttributes(e, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, arg1, AttributeModifier.Operation.ADD_SCALAR, null, null, null, null));
                            } else if (e == EntityType.CREEPER) {
                                if (sect.isSet("max-fuse-time")) arg1 = (double) sect.getInt("max-fuse-time");
                                if (sect.isSet("explosion-radius")) arg2 = (double) sect.getInt("explosion-radius");
                                entityArray.add(new EntityAttributes(e, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, null, null, null, null, arg1, arg2));
                            } else {
                                entityArray.add(new EntityAttributes(e, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod));
                            }
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            isBaby = true;
        }
    }

    private void reloadEntityAttributes(){
        // Removing old and applying new value to entities
        plugin.getServer().getWorlds().forEach(world -> world.getLivingEntities().forEach(this::setAttributesFor));
    }

    public void setAttributesFor(@NotNull final LivingEntity entity) {
        final EntityAttributes attribs = getEntityAttributes(entity);
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

        // Special living entities
        if(entity.getType() == EntityType.PLAYER) {
            AttributeInstance arg1 = entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            AttributeInstance arg2 = entity.getAttribute(Attribute.GENERIC_LUCK);
            
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
            return;
        }

        if(entity.getType() == EntityType.ZOMBIE) {
            AttributeInstance arg1 = entity.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);
            // Spawn Reinforcements
            if (arg1 != null && attribs.arg1 != null) {
                arg1.removeModifier(attribs.arg1);
                arg1.addModifier(attribs.arg1);
            }
            return;
        }

        if (entity.getType() == EntityType.BEE || entity.getType() == EntityType.PARROT) {
            AttributeInstance arg1 = entity.getAttribute(Attribute.GENERIC_FLYING_SPEED);
            // Spawn Reinforcements
            if (arg1 != null && attribs.arg1 != null) {
                arg1.removeModifier(attribs.arg1);
                arg1.addModifier(attribs.arg1);
            }
            return;
        }

        if(entity.getType() == EntityType.CREEPER) {
            if(attribs.arg3 != null) ((Creeper)entity).setMaxFuseTicks((int) floor(attribs.arg3));
            if(attribs.arg4 != null) ((Creeper)entity).setExplosionRadius((int) floor(attribs.arg4));
        }
    }

    @Nullable
    public EntityAttributes getEntityAttributes(@NotNull final Entity entity) {
        return entityAttributesMap.get(new Pair<>(entity.getType(), isBaby(entity)));
    }

    private boolean isBaby(@NotNull final Entity entity) {
        return entity instanceof Ageable && !((Ageable) entity).isAdult();
    }
}
