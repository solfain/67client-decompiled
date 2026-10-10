package dev.sol.mixin;

import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import dev.sol.module.impl.BlockEntityEspModule;
import dev.sol.module.impl.SpawnerProtectModule;
import dev.sol.util.TpsTracker;
import net.minecraft.class_2620;
import net.minecraft.class_2622;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2672;
import net.minecraft.class_2761;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_6603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_634.class})
public class ClientPacketListenerMixin {
   @Inject(
      method = {"method_11079"},
      at = {@At("HEAD")}
   )
   private void solclient$trackTps(class_2761 packet, CallbackInfo ci) {
      TpsTracker.onTimePacket();
   }

   @Inject(
      method = {"method_45730"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$fakeCommands(String command, CallbackInfo ci) {
      ModuleManager modules = SolClient.modules();
      if (modules != null) {
         try {
            if (modules.fakePay != null && modules.fakePay.tryIntercept(command)) {
               ci.cancel();
               return;
            }

            if (modules.fakeStats != null && modules.fakeStats.tryInterceptBalance(command)) {
               ci.cancel();
            }
         } catch (Exception var5) {
         }
      }

   }

   @Inject(
      method = {"method_11128"},
      at = {@At("TAIL")}
   )
   private void solclient$blockEntityChunk(class_2672 packet, CallbackInfo ci) {
      BlockEntityEspModule module = module();
      if (module != null && module.isEnabled() && module.chunkPacketsEnabled()) {
         try {
            packet.method_38598().method_38587(packet.method_11523(), packet.method_11524()).accept((class_6603.class_6605)(pos, type, tag) -> module.run(pos, type));
         } catch (Exception var5) {
         }
      }

   }

   @Inject(
      method = {"method_11094"},
      at = {@At("TAIL")}
   )
   private void solclient$blockEntityUpdate(class_2622 packet, CallbackInfo ci) {
      BlockEntityEspModule module = module();
      if (module != null && module.isEnabled() && module.beUpdatePacketsEnabled()) {
         try {
            module.run(packet.method_11293(), packet.method_11291());
         } catch (Exception var5) {
         }
      }

   }

   private static BlockEntityEspModule module() {
      ModuleManager modules = SolClient.modules();
      return modules != null ? modules.blockEntityEsp : null;
   }

   private static SpawnerProtectModule spawnerProtect() {
      ModuleManager modules = SolClient.modules();
      return modules != null ? modules.spawnerProtect : null;
   }

   @Inject(
      method = {"method_11116"},
      at = {@At("HEAD")}
   )
   private void solclient$spawnerProtectDestruction(class_2620 packet, CallbackInfo ci) {
      SpawnerProtectModule sp = spawnerProtect();
      if (sp != null && sp.isEnabled() && class_310.method_1551().method_18854()) {
         try {
            sp.onBlockDestructionPacket(packet.method_11280(), packet.method_11277());
         } catch (Exception var5) {
         }
      }

   }

   @Inject(
      method = {"method_11136"},
      at = {@At("HEAD")}
   )
   private void solclient$spawnerProtectBlockUpdate(class_2626 packet, CallbackInfo ci) {
      SpawnerProtectModule sp = spawnerProtect();
      if (sp != null && sp.isEnabled() && sp.detectBlockUpdatesEnabled() && class_310.method_1551().method_18854()) {
         try {
            sp.onServerBlockUpdate(packet.method_11309(), packet.method_11308(), false);
         } catch (Exception var5) {
         }
      }

   }

   @Inject(
      method = {"method_11100"},
      at = {@At("HEAD")}
   )
   private void solclient$spawnerProtectSectionUpdate(class_2637 packet, CallbackInfo ci) {
      SpawnerProtectModule sp = spawnerProtect();
      if (sp != null && sp.isEnabled() && sp.detectBlockUpdatesEnabled() && class_310.method_1551().method_18854()) {
         try {
            packet.method_30621((pos, state) -> sp.onServerBlockUpdate(pos, state, true));
         } catch (Exception var5) {
         }
      }

   }
}
