package dev.sol.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sol.SolClient;
import dev.sol.hud.HudDragController;
import dev.sol.module.ModuleManager;
import dev.sol.util.CpsTracker;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public class MouseHandlerMixin {
   @Shadow
   private double field_1789;
   @Shadow
   private double field_1787;

   @Inject(
      method = {"method_1606"},
      at = {@At("HEAD")}
   )
   private void solclient$aimAssist(double d, CallbackInfo ci) {
      ModuleManager modules = SolClient.modules();
      if (modules != null && modules.aimAssist != null && modules.aimAssist.isEnabled()) {
         double[] add = modules.aimAssist.computePixels(d, this.field_1789, this.field_1787);
         if (add != null) {
            this.field_1789 += add[0];
            this.field_1787 += add[1];
         }
      }

   }

   @Inject(
      method = {"method_1601(JLnet/minecraft/class_11910;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$onButton(long window, class_11910 buttonInfo, int action, CallbackInfo ci) {
      class_310 minecraft = class_310.method_1551();
      if (window == minecraft.method_22683().method_4490()) {
         if (action == 1 && minecraft.field_1755 == null) {
            CpsTracker.onClick(buttonInfo.comp_4801());
         }

         if (minecraft.field_1755 instanceof class_408 && buttonInfo.comp_4801() == 0 && SolClient.hud() != null) {
            if (action == 1) {
               if (HudDragController.tryStartDrag(SolClient.hud())) {
                  ci.cancel();
               }
            } else if (action == 0 && HudDragController.isDragging()) {
               HudDragController.stopDrag();
               SolClient.config().save();
               ci.cancel();
            }
         }
      }

   }

   @ModifyExpressionValue(
      method = {"method_1606"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_7172;method_41753()Ljava/lang/Object;"
)},
      slice = {@Slice(
   from = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_315;method_42495()Lnet/minecraft/class_7172;"
)
)}
   )
   private Object solclient$zoomSensitivity(Object original) {
      ModuleManager modules = SolClient.modules();
      if (modules != null && modules.zoom != null && original instanceof Double s) {
         double factor = modules.zoom.currentFactor();
         if (factor <= 1.0001) {
            return original;
         } else {
            double e = s * 0.6 + 0.2;
            double scaled = e / Math.cbrt(factor);
            double substitute = (scaled - 0.2) / 0.6;
            return Math.max((double)0.0F, substitute);
         }
      } else {
         return original;
      }
   }

   @Inject(
      method = {"method_1598(JDD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
      class_310 minecraft = class_310.method_1551();
      if (window == minecraft.method_22683().method_4490() && minecraft.field_1755 instanceof class_408 && SolClient.hud() != null && HudDragController.tryResize(SolClient.hud(), yOffset)) {
         ci.cancel();
      }

   }
}
