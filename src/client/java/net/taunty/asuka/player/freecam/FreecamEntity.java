package net.taunty.asuka.player.freecam;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FreecamEntity extends Entity {

    public FreecamEntity(Level level) {
        super(EntityTypes.ARMOR_STAND, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {

    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {

    }
}
