/*
This file is part of MobStatusChange.

MobStatusChange is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MobStatusChange is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MobStatusChange.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.secretx33.mobstatuschange.entity;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNullableByDefault;
import java.util.UUID;

public class EntityAttributes {
    private static final UUID hpUID = UUID.fromString("664ab926-13df-4305-b497-5a1119ae403e");
    private static final UUID followRangeUID = UUID.fromString("cc67f9d5-02d2-4cb8-aac7-2463b45a5f6e");
    private static final UUID atkKnockBackUID = UUID.fromString("82587ea5-db4f-4605-bbd0-4e1c5b9b7d9c");
    private static final UUID knockBackResistUID = UUID.fromString("340dd7c8-9f82-454c-8274-963da7c91518");
    private static final UUID movSpeedUID = UUID.fromString("21484559-81e1-4463-88a0-df8c09f5eb83");
    private static final UUID arg1UID = UUID.fromString("692ab151-8a95-45db-bc1b-04c1e0aecdb4");
    private static final UUID arg2UID = UUID.fromString("bc82bef9-3424-436c-9eac-26117e875656");

    public final EntityType entityType;
    public final boolean isBaby;
    public final AttributeModifier hpMod;
    public final AttributeModifier followRangeMod;
    public final Double atkDamageMod;
    public final AttributeModifier atkKnockBackMod;
    public final AttributeModifier knockBackResistMod;
    public final AttributeModifier movSpeedMod;
    public final AttributeModifier arg1; // First exclusive attribute of entity
    public final AttributeModifier arg2; // Second exclusive attribute of entity
    public final Double arg3;            // First exclusive attribute of entity
    public final Double arg4;            // Second exclusive attribute of entity

    @ParametersAreNullableByDefault
    public EntityAttributes (
        @NotNull final EntityType entityType,
        final boolean isBaby,
        final Double hpMod,
        final Double followRangeMod,
        final Double atkDamageMod,
        final Double atkKnockBackMod,
        final Double knockBackResistMod,
        final Double movSpeedMod,
        final Double arg1,
        final AttributeModifier.Operation opArg1,
        final Double arg2,
        final AttributeModifier.Operation opArg2,
        final Double arg3,
        final Double arg4
    ) {
        this.entityType = entityType;
        this.isBaby = isBaby;

        if(hpMod != null && hpMod > Double.MIN_VALUE) this.hpMod = new AttributeModifier(hpUID, "", convertMod(hpMod), AttributeModifier.Operation.ADD_SCALAR);
        else this.hpMod = null;

        if(followRangeMod != null && followRangeMod > Double.MIN_VALUE) this.followRangeMod = new AttributeModifier(followRangeUID, "", convertMod(followRangeMod), AttributeModifier.Operation.ADD_SCALAR);
        else this.followRangeMod = null;

        if(atkDamageMod != null && atkDamageMod > Double.MIN_VALUE) this.atkDamageMod = atkDamageMod;
        else this.atkDamageMod = null;

        if(atkKnockBackMod != null && atkKnockBackMod > Double.MIN_VALUE) this.atkKnockBackMod = new AttributeModifier(atkKnockBackUID, "", atkKnockBackMod, AttributeModifier.Operation.ADD_NUMBER);
        else this.atkKnockBackMod = null;

        if(knockBackResistMod != null && knockBackResistMod > Double.MIN_VALUE) this.knockBackResistMod = new AttributeModifier(knockBackResistUID, "", knockBackResistMod, AttributeModifier.Operation.ADD_NUMBER);
        else this.knockBackResistMod = null;

        if(movSpeedMod != null && movSpeedMod > Double.MIN_VALUE) this.movSpeedMod = new AttributeModifier(movSpeedUID, "", convertMod(movSpeedMod), AttributeModifier.Operation.ADD_SCALAR);
        else this.movSpeedMod = null;

        if(arg1 != null && arg1 > Double.MIN_VALUE && opArg1 != null) {
            if(opArg1 == AttributeModifier.Operation.ADD_NUMBER) this.arg1 = new AttributeModifier(arg1UID, "", arg1, opArg1);
            else this.arg1 = new AttributeModifier(arg1UID, "", convertMod(arg1), opArg1);
        }
        else this.arg1 = null;

        if(arg2 != null && arg2 > Double.MIN_VALUE && opArg2 != null){
            if(opArg1 == AttributeModifier.Operation.ADD_NUMBER) this.arg2 = new AttributeModifier(arg2UID, "", arg2, opArg2);
            else this.arg2 = new AttributeModifier(arg1UID, "", convertMod(arg2), opArg2);
        }
        else this.arg2 = null;

        this.arg3 = arg3;
        this.arg4 = arg4;
    }

    @ParametersAreNullableByDefault
    public EntityAttributes(@NotNull EntityType entityType, boolean isBaby, Double hpMod, Double followRangeMod, Double atkDamageMod, Double atkKnockBackMod, Double knockBackResistMod, Double movSpeedMod) {
        this(entityType, isBaby, hpMod, followRangeMod, atkDamageMod, atkKnockBackMod, knockBackResistMod, movSpeedMod, null, null, null, null, null, null);
    }

    @Nullable
    public Double getAtkDamageMod() {
        return atkDamageMod;
    }

    private static double convertMod(double mod){
        return mod - 1;
    }
}
