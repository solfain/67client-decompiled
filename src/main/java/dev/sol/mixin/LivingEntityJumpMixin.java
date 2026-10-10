package dev.sol.mixin;

import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1309.class})
public class LivingEntityJumpMixin {
   @Inject(
      method = {"method_6043"},
      at = {@At("HEAD")}
   )
   private void solclient$onJump(CallbackInfo ci) {
      if ((Object)this instanceof class_746 player) {
         if (player == class_310.method_1551().field_1724) {
            ModuleManager modules = SolClient.modules();
            if (modules != null && modules.jumpCircles != null && modules.jumpCircles.isEnabled()) {
               modules.jumpCircles.onPlayerJump(player);
            }
         }
      }

   }
}
