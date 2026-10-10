package dev.sol.mixin;

import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_338.class})
public class ChatComponentMixin {
   @ModifyVariable(
      method = {"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private class_2561 solclient$censorChat(class_2561 component) {
      ModuleManager modules = SolClient.modules();
      if (modules == null) {
         return component;
      } else {
         class_2561 result = component;
         if (modules.fakeRoles != null) {
            result = modules.fakeRoles.decorateChat(component);
         }

         if (modules.nameProtect != null && modules.nameProtect.isEnabled()) {
            result = modules.nameProtect.censorChat(result);
         }

         return result;
      }
   }
}
