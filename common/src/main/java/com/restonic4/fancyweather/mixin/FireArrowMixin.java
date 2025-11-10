package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class FireArrowMixin {
    @Inject(at = @At("TAIL"), method = "onHitBlock")
    public void onHitBlock(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (!FancyWeatherMidnightConfig.enableFireSpreadingArrows) return;
        if (blockHitResult.getType() == HitResult.Type.MISS) return;

        AbstractArrow self = (AbstractArrow) (Object) this;

        if (!self.isOnFire()) return;

        Level level = self.level();
        if (level.isClientSide()) return;

        BlockPos desiredPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        BlockState currentState = level.getBlockState(desiredPos);

        // Only set fire if the block can be replaced
        if (currentState.canBeReplaced()) {
            level.destroyBlock(desiredPos, true); // Drops items
            level.setBlock(desiredPos, BaseFireBlock.getState(level, desiredPos), 11);
        }
    }
}
