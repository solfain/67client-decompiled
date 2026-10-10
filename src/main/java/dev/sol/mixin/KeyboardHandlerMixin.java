package dev.sol.mixin;

import dev.sol.SolClient;
import dev.sol.gui.ClickGuiScreen;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_473;
import net.minecraft.class_7743;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_309.class})
public class KeyboardHandlerMixin {
   @Inject(
      method = {"method_1466(JILnet/minecraft/class_11908;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$dispatchModuleKeybinds(long window, int action, class_11908 keyEvent, CallbackInfo ci) {
      class_310 minecraft = class_310.method_1551();
      if (SolClient.modules() != null && action == 1) {
         if (minecraft.field_1755 == null && minecraft.field_1687 != null) {
            if (SolClient.modules().onKeyPressed(keyEvent.comp_4795())) {
               ci.cancel();
            }
         } else if ((keyEvent.comp_4795() == 340 || keyEvent.comp_4795() == 344) && minecraft.field_1755 != null && shiftOpensGui(minecraft.field_1755)) {
            minecraft.method_1507(new ClickGuiScreen(minecraft.field_1755));
            ci.cancel();
         }
      }

   }

   private static boolean shiftOpensGui(class_437 screen) {
      boolean var10000;
      if (!(screen instanceof ClickGuiScreen) && !(screen instanceof class_408) && !(screen instanceof class_465) && !(screen instanceof class_7743) && !(screen instanceof class_473)) {
         class_364 var2 = screen.method_25399();
         if (var2 instanceof class_342) {
            class_342 editBox = (class_342)var2;
            if (editBox.method_25370()) {
               var10000 = false;
               return var10000;
            }
         }

         var10000 = true;
      } else {
         var10000 = false;
      }

      return var10000;
   }
}
