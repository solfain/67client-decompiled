package dev.sol.mixin;

import dev.sol.module.impl.FreeLookModule;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1297.class})
public class EntityFreeLookMixin {
   @Inject(
      method = {"method_5872(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$freeLookTurn(double d, double e, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if ((Object)this == mc.field_1724) {
         FreeLookModule freeLook = FreeLookModule.get();
         if (freeLook != null && freeLook.cameraMode()) {
            freeLook.addCameraLook(d, e);
            ci.cancel();
         }
      }

   }
}
