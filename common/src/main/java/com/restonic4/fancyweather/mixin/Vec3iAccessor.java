package com.restonic4.fancyweather.mixin;

import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Vec3i.class)
public interface Vec3iAccessor {
    @Invoker("setX")
    Vec3i invokeSetX(int pX);

    @Invoker("setY")
    Vec3i invokeSetY(int pY);

    @Invoker("setZ")
    Vec3i invokeSetZ(int pZ);
}
