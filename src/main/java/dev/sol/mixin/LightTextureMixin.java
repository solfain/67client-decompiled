package dev.sol.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin({class_765.class})
public class LightTextureMixin {
   @ModifyExpressionValue(
      method = {"method_3313"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_7172;method_41753()Ljava/lang/Object;"
)},
      slice = {@Slice(
   from = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_315;method_42473()Lnet/minecraft/class_7172;"
)
)}
   )
   private Object solclient$fullbrightGamma(Object original) {
      ModuleManager modules = SolClient.modules();
      return modules != null && modules.fullbright != null && modules.fullbright.isEnabled() ? (double)modules.fullbright.gamma.getFloat() : original;
   }
}
