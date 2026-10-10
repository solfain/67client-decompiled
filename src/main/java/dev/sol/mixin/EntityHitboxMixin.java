package dev.sol.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_1297.class})
public class EntityHitboxMixin {
   @ModifyReturnValue(
      method = {"method_5829"},
      at = {@At("RETURN")}
   )
   private class_238 solclient$expandHitbox(class_238 original) {
      return original;
   }
}
