package dev.sol.mixin;

import dev.sol.module.impl.FreecamModule;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_746.class})
public abstract class LocalPlayerFreecamMixin {
   @Inject(
      method = {"method_6007"},
      at = {@At("HEAD")}
   )
   private void solclient$freecamReapplyBeforeAiStep(CallbackInfo ci) {
      this.solclient$reapply();
   }

   @Inject(
      method = {"method_3136"},
      at = {@At("HEAD")}
   )
   private void solclient$freecamReapplyBeforeSendPosition(CallbackInfo ci) {
      this.solclient$reapply();
   }

   private void solclient$reapply() {
      class_746 self = (class_746)this;
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == self) {
         FreecamModule f = FreecamModule.get();
         if (f != null && f.isActive()) {
            FreecamModule.reapplyBodyInput(mc);
         }
      }

   }
}
