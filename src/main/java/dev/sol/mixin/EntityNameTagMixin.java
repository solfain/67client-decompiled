package dev.sol.mixin;

import com.mojang.authlib.GameProfile;
import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import dev.sol.module.impl.FakeRolesModule;
import dev.sol.module.impl.NameTagsModule;
import net.minecraft.class_10017;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_640;
import net.minecraft.class_746;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_897.class})
public class EntityNameTagMixin {
   @Inject(
      method = {"method_3926"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$nameTag(class_10017 state, class_4587 poseStack, class_11659 collector, class_12075 cameraRenderState, CallbackInfo ci) {
      ModuleManager modules = SolClient.modules();
      if (modules != null && state.field_53337 != null) {
         NameTagsModule nameTags = modules.nameTags;
         boolean nameTagsOn = nameTags != null && nameTags.isEnabled();
         if (nameTagsOn) {
            String display = state.field_53337.getString();
            if (isLocalPlayer(display)) {
               if ((Boolean)nameTags.hideOwnTag.get() || (Boolean)nameTags.players.get() && (Boolean)nameTags.self.get()) {
                  ci.cancel();
                  return;
               }
            } else if (isOnlinePlayer(display)) {
               if ((Boolean)nameTags.players.get() || (Boolean)nameTags.hidePlayerTags.get()) {
                  ci.cancel();
                  return;
               }
            } else if ((Boolean)nameTags.hideOtherTags.get()) {
               ci.cancel();
               return;
            }
         }

         if (modules.nameProtect != null && modules.nameProtect.isEnabled()) {
            String replacement = modules.nameProtect.replacementForDisplay(state.field_53337.getString());
            if (replacement != null) {
               state.field_53337 = class_2561.method_43470(replacement);
            }
         }

         FakeRolesModule fakeRoles = modules.fakeRoles;
         if (fakeRoles != null && isLocalPlayer(state.field_53337.getString())) {
            state.field_53337 = fakeRoles.decorateNametag(state.field_53337);
         }
      }

   }

   private static boolean isLocalPlayer(String display) {
      if (display != null && !display.isEmpty()) {
         class_746 self = class_310.method_1551().field_1724;
         if (self == null) {
            return false;
         } else {
            String name = self.method_7334().name();
            return name != null && !name.isEmpty() && display.contains(name);
         }
      } else {
         return false;
      }
   }

   private static boolean isOnlinePlayer(String display) {
      if (display != null && !display.isEmpty()) {
         class_310 mc = class_310.method_1551();
         if (mc.method_1562() == null) {
            return false;
         } else {
            for(class_640 info : mc.method_1562().method_2880()) {
               GameProfile profile = info.method_2966();
               String name = profile == null ? null : profile.name();
               if (name != null && !name.isEmpty() && display.contains(name)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }
}
