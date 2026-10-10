package dev.sol.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import dev.sol.module.impl.FreecamModule;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public class GuiCrosshairMixin {
   @Inject(
      method = {"method_1736"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$hideVanillaCrosshair(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo ci) {
      ModuleManager modules = SolClient.modules();
      if (modules != null && modules.customCrosshair != null && modules.customCrosshair.shouldHideVanilla()) {
         ci.cancel();
      }

   }

   @ModifyExpressionValue(
      method = {"method_1736"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_5498;method_31034()Z"
)}
   )
   private boolean solclient$freecamCrosshairInThirdPerson(boolean isFirstPerson) {
      FreecamModule freecam = FreecamModule.get();
      return freecam != null && freecam.isActive() && freecam.isShowPlayerModel() && !isFirstPerson ? true : isFirstPerson;
   }
}
