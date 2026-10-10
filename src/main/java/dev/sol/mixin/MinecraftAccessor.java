package dev.sol.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_310.class})
public interface MinecraftAccessor {
   @Accessor("field_1752")
   void solclient$setRightClickDelay(int var1);

   @Invoker("method_1536")
   boolean solclient$startAttack();

   @Invoker("method_1583")
   void solclient$startUseItem();
}
