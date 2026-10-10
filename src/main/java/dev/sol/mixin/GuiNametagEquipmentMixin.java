package dev.sol.mixin;

import dev.sol.render.WorldNametagRenderer;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public class GuiNametagEquipmentMixin {
   @Inject(
      method = {"method_1753(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"},
      at = {@At("TAIL")}
   )
   private void solclient$nametagEquipment(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo ci) {
      WorldNametagRenderer.renderEquipment(guiGraphics);
   }
}
