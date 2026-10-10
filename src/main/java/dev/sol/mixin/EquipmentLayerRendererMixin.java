package dev.sol.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import dev.sol.module.impl.ArmorTrimHiderModule;
import net.minecraft.class_10055;
import net.minecraft.class_10197;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_8053;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_10197.class})
public class EquipmentLayerRendererMixin {
   @ModifyExpressionValue(
      method = {"method_64078(Lnet/minecraft/class_10186$class_10190;Lnet/minecraft/class_5321;Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_1799;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_2960;II)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_1799;method_58694(Lnet/minecraft/class_9331;)Ljava/lang/Object;"
)}
   )
   private Object solclient$armorTrim(Object original, @Local(argsOnly = true) class_1799 itemStack, @Local(argsOnly = true) Object renderState) {
      ModuleManager modules = SolClient.modules();
      if (modules == null) {
         return original;
      } else {
         ArmorTrimHiderModule module = modules.armorTrimHider;
         if (module != null && module.isEnabled()) {
            return !module.affectsOwn() && solclient$isLocalPlayer(renderState) ? original : module.mapTrim(itemStack, (class_8053)original);
         } else {
            return original;
         }
      }
   }

   private static boolean solclient$isLocalPlayer(Object renderState) {
      if (!(renderState instanceof class_10055 avatar)) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         return mc.field_1724 != null && avatar.field_53528 == mc.field_1724.method_5628();
      }
   }
}
