package io.github.secretx33.mobstatuschange.entity;

import io.github.secretx33.mobstatuschange.config.Const;
import io.github.secretx33.mobstatuschange.Main;
import io.github.secretx33.mobstatuschange.utils.Utils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNullableByDefault;
import java.util.*;

public class EntityAttributes {
    private static Main plugin = null;
    private static final UUID hpUID = UUID.fromString("664ab926-13df-4305-b497-5a1119ae403e");
    private static final UUID followRangeUID = UUID.fromString("cc67f9d5-02d2-4cb8-aac7-2463b45a5f6e");
    private static final UUID atkKnockBackUID = UUID.fromString("82587ea5-db4f-4605-bbd0-4e1c5b9b7d9c");
    private static final UUID knockBackResistUID = UUID.fromString("340dd7c8-9f82-454c-8274-963da7c91518");
    private static final UUID movSpeedUID = UUID.fromString("21484559-81e1-4463-88a0-df8c09f5eb83");
    private static final UUID arg1UID = UUID.fromString("692ab151-8a95-45db-bc1b-04c1e0aecdb4");
    private static final UUID arg2UID = UUID.fromString("bc82bef9-3424-436c-9eac-26117e875656");

    private static final List<EntityAttributes> entityArray = new ArrayList<>();

    private final EntityType entityType;
    private final boolean isBaby;
    private AttributeModifier hpMod;
    private AttributeModifier followRangeMod;
    private Double atkDamageMod;
    private AttributeModifier atkKnockBackMod;
    private AttributeModifier knockBackResistMod;
    private AttributeModifier movSpeedMod;
    private AttributeModifier arg1; // First exclusive attribute of entity
    private AttributeModifier arg2; // Second exclusive attribute of entity
    private Double arg3;            // First exclusive attribute of entity
    private Double arg4;            // Second exclusive attribute of entity


    @ParametersAreNullableByDefault
    public EntityAttributes(@NotNull EntityType entityType, boolean isBaby, Double hpMod, Double followRangeMod, Double atkDamageMod, Double atkKnockBackMod, Double knockBackResistMod, Double movSpeedMod, Double arg1, AttributeModifier.Operation opArg1, Double arg2, AttributeModifier.Operation opArg2, Double arg3, Double arg4) {
        // Initializing variables with null
        this.hpMod = null;
        this.followRangeMod = null;
        this.atkDamageMod = null;
        this.atkKnockBackMod = null;
        this.knockBackResistMod = null;
        this.movSpeedMod = null;
        this.arg1 = null;
        this.arg2 = null;
        this.arg3 = null;
        this.arg4 = null;

        // Assigning values specified on constructor
        this.entityType = entityType;
        this.isBaby = isBaby;
        if(hpMod != null) this.hpMod = new AttributeModifier(hpUID, "", convertMod(hpMod), AttributeModifier.Operation.ADD_SCALAR);
        if(followRangeMod != null) this.followRangeMod = new AttributeModifier(followRangeUID, "", convertMod(followRangeMod), AttributeModifier.Operation.ADD_SCALAR);
        if(atkDamageMod != null) this.atkDamageMod = atkDamageMod;
        if(atkKnockBackMod != null) this.atkKnockBackMod = new AttributeModifier(atkKnockBackUID, "", atkKnockBackMod, AttributeModifier.Operation.ADD_NUMBER);
        if(knockBackResistMod != null) this.knockBackResistMod = new AttributeModifier(knockBackResistUID, "", knockBackResistMod, AttributeModifier.Operation.ADD_NUMBER);
        if(movSpeedMod != null) this.movSpeedMod = new AttributeModifier(movSpeedUID, "", convertMod(movSpeedMod), AttributeModifier.Operation.ADD_SCALAR);
        if(arg1 != null && opArg1 != null) {
            if(opArg1 == AttributeModifier.Operation.ADD_NUMBER) this.arg1 = new AttributeModifier(arg1UID, "", arg1, opArg1);
            else this.arg1 = new AttributeModifier(arg1UID, "", convertMod(arg1), opArg1);
        }
        if(arg2 != null && opArg2 != null){
            if(opArg1 == AttributeModifier.Operation.ADD_NUMBER) this.arg2 = new AttributeModifier(arg2UID, "", arg2, opArg2);
            else this.arg2 = new AttributeModifier(arg1UID, "", convertMod(arg2), opArg2);
        }
        if(arg3 != null) this.arg3 = arg3;
        if(arg4 != null) this.arg4 = arg4;
    }

    @ParametersAreNullableByDefault
    public EntityAttributes(@NotNull EntityType entityType, boolean isBaby, Double hpMod, Double followRangeMod, Double atkDamageMod, Double atkKnockBackMod, Double knockBackResistMod, Double movSpeedMod) {
        this(entityType, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, null, null, null, null, null, null);
    }

    public static void populateEntityArray() {
        if(plugin == null) {
            throw new NullPointerException("Plugin variable was not set yet.");
        }
        final FileConfiguration config = plugin.getConfig();
        entityArray.clear();

        String field = "entities";
        ConfigurationSection mobConfig = null;
        if (config.isSet(field)) mobConfig = config.getConfigurationSection(field);
        if (mobConfig == null) {
            Utils.messageConsole(String.format(Const.SECTION_NOT_FOUND, field));
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

    public static void setPlugin(@NotNull Main plugin) {
        EntityAttributes.plugin = plugin;
        refreshConfig();
    }

    public static void refreshConfig(){
        populateEntityArray();
        reloadEntityAttributes();
    }

    private static void reloadEntityAttributes(){
        if(plugin == null) {
            throw new NullPointerException("Plugin variable was not set yet.");
        }
        // Removing old and applying new value to entities
        plugin.getServer().getWorlds().forEach(world -> world.getLivingEntities().forEach(EntityAttributes::setAttributesFor));
    }

    public static void setAttributesFor(@NotNull LivingEntity entity) {
        final boolean isBaby = (Ageable.class.isAssignableFrom(entity.getClass()) && !((Ageable)entity).isAdult());

        final EntityAttributes e = entityArray.stream().filter(attrib -> entity.getType() == attrib.entityType && isBaby == attrib.isBaby).findAny().orElse(null);

        if(e != null){
            AttributeInstance hp = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            AttributeInstance followRange = entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
            AttributeInstance atkKnockBack = entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
            AttributeInstance knockBackResist = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            AttributeInstance movSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

            if(hp != null && e.hpMod != null) {
                double currentHP = entity.getHealth();
                double maxHP = hp.getValue();
                double percentHP = currentHP / maxHP;
                hp.removeModifier(e.hpMod);
                hp.addModifier(e.hpMod);
                entity.setHealth(hp.getValue() * percentHP); // Preserve the entity HP percentage when modifying HP
            }
            if(followRange != null && e.followRangeMod != null){
                followRange.removeModifier(e.followRangeMod);
                followRange.addModifier(e.followRangeMod);
            }
            if(atkKnockBack != null && e.atkKnockBackMod != null){
                atkKnockBack.removeModifier(e.atkKnockBackMod);
                atkKnockBack.addModifier(e.atkKnockBackMod);
            }
            if(knockBackResist != null && e.knockBackResistMod != null){
                knockBackResist.removeModifier(e.knockBackResistMod);
                knockBackResist.addModifier(e.knockBackResistMod);
            }
            if(movSpeed != null && e.movSpeedMod != null){
                movSpeed.removeModifier(e.movSpeedMod);
                movSpeed.addModifier(e.movSpeedMod);
            }

            // Special living entities
            if(entity.getType() == EntityType.PLAYER) {
                AttributeInstance arg1 = entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                AttributeInstance arg2 = entity.getAttribute(Attribute.GENERIC_LUCK);

                // Attack speed
                if(arg1 != null && e.arg1 != null){
                    arg1.removeModifier(e.arg1);
                    arg1.addModifier(e.arg1);
                }
                // Luck
                if(arg2 != null && e.arg2 != null){
                    arg2.removeModifier(e.arg2);
                    arg2.addModifier(e.arg2);
                }
            } else if(entity.getType() == EntityType.ZOMBIE) {
                AttributeInstance arg1 = entity.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);

                // Spawn Reinforcements
                if (arg1 != null && e.arg1 != null) {
                    arg1.removeModifier(e.arg1);
                    arg1.addModifier(e.arg1);
                }

            } else if (entity.getType() == EntityType.BEE || entity.getType() == EntityType.PARROT) {
                AttributeInstance arg1 = entity.getAttribute(Attribute.GENERIC_FLYING_SPEED);

                // Spawn Reinforcements
                if (arg1 != null && e.arg1 != null) {
                    arg1.removeModifier(e.arg1);
                    arg1.addModifier(e.arg1);
                }
            } else if(entity.getType() == EntityType.CREEPER) {
                if(e.arg3 != null) ((Creeper)entity).setMaxFuseTicks((int)Math.floor(e.arg3));
                if(e.arg4 != null) ((Creeper)entity).setExplosionRadius((int)Math.floor(e.arg4));
            }
        }
    }

    @Nullable
    public Double getAtkDamageMod() {
        return atkDamageMod;
    }

    @Nullable
    public static EntityAttributes getEntityAttributes(@NotNull Entity entity) {
        // orElse will create the object inside it no matter what, on the other hand, orElseGet won't compute unless findAny() returns empty (don't find anything), here though the object is null so I don't think I'll see any replacing it with orElseGet
        final boolean isBaby = (Ageable.class.isAssignableFrom(entity.getClass()) && !((Ageable)entity).isAdult());
        return entityArray.stream().filter(e -> e.entityType == entity.getType() && e.isBaby == isBaby).findAny().orElse(null);
    }

    private static double convertMod(double mod){
        return mod - 1;
    }
}