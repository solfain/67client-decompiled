package dev.sol.mixin;

import dev.sol.module.impl.FreecamModule;
import net.minecraft.class_310;
import net.minecraft.class_743;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_743.class})
public class KeyboardInputFreecamMixin {
   @Inject(
      method = {"method_3129"},
      at = {@At("TAIL")}
   )
   private void solclient$freecamReapplyCachedBodyInput(CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1724.field_3913 == (Object)this) {
         FreecamModule f = FreecamModule.get();
         if (f != null && f.isActive()) {
            FreecamModule.reapplyBodyInput(mc);
         }
      }

   }
}
