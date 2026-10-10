package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.mixin.ClientInputAccessor;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import net.minecraft.class_10185;
import net.minecraft.class_1923;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3532;
import net.minecraft.class_5498;
import net.minecraft.class_631;
import net.minecraft.class_746;

public class FreecamModule extends Module {
   private static FreecamModule instance;
   public final SliderSetting speed = (SliderSetting)this.addSetting(new SliderSetting("Speed", "Camera fly speed", (double)1.0F, 0.1, (double)50.0F, 0.1, "x"));
   public final SliderSetting verticalMultiplier = (SliderSetting)this.addSetting(new SliderSetting("Vertical speed", "Up/down speed multiplier", (double)1.0F, 0.2, (double)5.0F, 0.05, "x"));
   public final BooleanSetting smoothing = (BooleanSetting)this.addSetting(new BooleanSetting("Smoothing", "Ease camera movement", true));
   public final BooleanSetting showPlayerModel = (BooleanSetting)this.addSetting(new BooleanSetting("Show own body", "Render your body while detached", true));
   public final BooleanSetting showHands = (BooleanSetting)this.addSetting(new BooleanSetting("Show hands", "Keep first-person hands visible", true));
   public final BooleanSetting bodyFollowsKeys = (BooleanSetting)this.addSetting(new BooleanSetting("Body uses movement keys", "Physical WASD also move the body", false));
   public final SliderSetting lookSensitivity = (SliderSetting)this.addSetting(new SliderSetting("Look Sensitivity", "Camera look sensitivity", (double)0.5F, 0.1, (double)2.0F, 0.05));
   public final SliderSetting freecamChunkDistance = (SliderSetting)this.addSetting(new SliderSetting("Chunk distance", "Chunks to load around the camera", (double)12.0F, (double)2.0F, (double)32.0F, (double)1.0F));
   private double currentX;
   private double currentY;
   private double currentZ;
   private double prevX;
   private double prevY;
   private double prevZ;
   private float currentYaw;
   private float currentPitch;
   private float prevYaw;
   private float prevPitch;
   private double savedX;
   private double savedY;
   private double savedZ;
   private float savedYaw;
   private float savedPitch;
   private boolean savedAbilitiesFlying;
   private boolean savedSmartCull;
   private class_5498 perspectiveBeforeFreecam;
   private boolean switchedPerspectiveForBody;
   private class_1923 lastSyncedCamChunk;
   private int lastSyncedLoadDistance = Integer.MIN_VALUE;
   private boolean activationPending;
   private boolean active = false;
   private boolean latchedForward;
   private boolean latchedBack;
   private boolean latchedLeft;
   private boolean latchedRight;
   private boolean latchedJump;
   private boolean latchedSneak;
   private boolean latchedSprint;
   private int autopilotWarmupTicks;

   public FreecamModule() {
      super("Freecam", "Detached camera (WASD = fly). Body can keep walking; mining uses real aim.", Category.MOVEMENT);
      instance = this;
   }

   public static FreecamModule get() {
      return instance;
   }

   protected void onEnable() {
      this.activationPending = true;
      this.active = false;
   }

   protected void onDisable() {
      class_310 mc = class_310.method_1551();
      this.clearMovementLatches();
      this.activationPending = false;
      this.active = false;
      if (this.switchedPerspectiveForBody) {
         mc.field_1690.method_31043(this.perspectiveBeforeFreecam);
         this.switchedPerspectiveForBody = false;
      }

      this.restoreViewOnlyClientState(mc);
      this.restoreVanillaChunkLoading(mc);
      if (mc.field_1724 != null) {
         mc.field_1724.method_31549().field_7479 = this.savedAbilitiesFlying;
      }

   }

   public void tryCompleteActivation(class_310 mc) {
      if (this.isEnabled() && this.activationPending && mc.field_1724 != null && mc.field_1687 != null) {
         this.savedX = mc.field_1724.method_23317();
         this.savedY = mc.field_1724.method_23318();
         this.savedZ = mc.field_1724.method_23321();
         this.savedYaw = mc.field_1724.method_36454();
         this.savedPitch = mc.field_1724.method_36455();
         this.currentX = this.prevX = this.savedX;
         this.currentY = this.prevY = this.savedY + (double)mc.field_1724.method_5751();
         this.currentZ = this.prevZ = this.savedZ;
         this.currentYaw = this.prevYaw = this.savedYaw;
         this.currentPitch = this.prevPitch = this.savedPitch;
         this.savedAbilitiesFlying = mc.field_1724.method_31549().field_7479;
         this.switchedPerspectiveForBody = false;
         if ((Boolean)this.showPlayerModel.get()) {
            this.perspectiveBeforeFreecam = mc.field_1690.method_31044();
            if (this.perspectiveBeforeFreecam.method_31034()) {
               mc.field_1690.method_31043(class_5498.field_26665);
               this.switchedPerspectiveForBody = true;
            }
         }

         this.activationPending = false;
         this.active = true;
         this.captureMovementLatches(mc);
         this.maybeLatchWalkFromVelocity(mc);
         this.autopilotWarmupTicks = 40;
         this.lastSyncedCamChunk = null;
         this.lastSyncedLoadDistance = Integer.MIN_VALUE;
         this.applyViewOnlyClientState(mc);
         this.syncFreecamChunkLoading(mc);
         reapplyBodyInput(mc);
      }

   }

   private void captureMovementLatches(class_310 mc) {
      class_315 o = mc.field_1690;
      class_746 player = mc.field_1724;
      class_10185 ki = player != null ? player.field_3913.field_54155 : class_10185.field_54098;
      this.latchedForward = o.field_1894.method_1434() || ki.comp_3159();
      this.latchedBack = o.field_1881.method_1434() || ki.comp_3160();
      this.latchedLeft = o.field_1913.method_1434() || ki.comp_3161();
      this.latchedRight = o.field_1849.method_1434() || ki.comp_3162();
      this.latchedJump = o.field_1903.method_1434() || ki.comp_3163();
      this.latchedSneak = o.field_1832.method_1434() || ki.comp_3164() || player != null && player.method_5715();
      this.latchedSprint = o.field_1867.method_1434() || ki.comp_3165();
      this.mergeLatchFromAutoWalk();
      this.mergeLatchFromLivingSpeed(mc.field_1724);
   }

   private void mergeLatchFromAutoWalk() {
      AutoWalkModule aw = SolClient.modules() != null ? SolClient.modules().autoWalk : null;
      if (aw != null && aw.isEnabled()) {
         this.latchedForward = true;
      }

   }

   public void mergeAutopilotFromCurrentState(class_310 mc) {
      if (mc.field_1724 != null) {
         this.mergeLatchFromAutoWalk();
         if (!(Boolean)this.bodyFollowsKeys.get()) {
            this.latchedSneak |= mc.field_1724.method_5715();
         } else {
            class_315 o = mc.field_1690;
            class_10185 ki = mc.field_1724.field_3913.field_54155;
            this.latchedForward |= o.field_1894.method_1434() || ki.comp_3159();
            this.latchedBack |= o.field_1881.method_1434() || ki.comp_3160();
            this.latchedLeft |= o.field_1913.method_1434() || ki.comp_3161();
            this.latchedRight |= o.field_1849.method_1434() || ki.comp_3162();
            this.latchedJump |= o.field_1903.method_1434() || ki.comp_3163();
            this.latchedSneak |= o.field_1832.method_1434() || ki.comp_3164();
            this.latchedSprint |= o.field_1867.method_1434() || ki.comp_3165();
         }

         this.mergeLatchFromLivingSpeed(mc.field_1724);
         this.mergeVelocityIntoLatch(mc.field_1724);
      }

   }

   private void mergeLatchFromLivingSpeed(class_746 player) {
      if (player != null) {
         float fs = player.field_6250;
         float ss = player.field_6212;
         if (fs > 0.015F) {
            this.latchedForward = true;
         }

         if (fs < -0.015F) {
            this.latchedBack = true;
         }

         if (ss > 0.015F) {
            this.latchedLeft = true;
         }

         if (ss < -0.015F) {
            this.latchedRight = true;
         }
      }

   }

   private void mergeVelocityIntoLatch(class_746 player) {
      class_243 vel = player.method_18798();
      double vx = vel.field_1352;
      double vz = vel.field_1350;
      if (!(vx * vx + vz * vz < 1.0E-10)) {
         class_243 flatLook = flatLook(player.method_36454());
         double dot = vx * flatLook.field_1352 + vz * flatLook.field_1350;
         double perp = vx * -flatLook.field_1350 + vz * flatLook.field_1352;
         if (Math.abs(dot) >= Math.abs(perp)) {
            if (dot > 0.008) {
               this.latchedForward = true;
            } else if (dot < -0.008) {
               this.latchedBack = true;
            }
         } else if (perp > 0.008) {
            this.latchedLeft = true;
         } else if (perp < -0.008) {
            this.latchedRight = true;
         }
      }

   }

   private void maybeLatchWalkFromVelocity(class_310 mc) {
      if (mc.field_1724 != null && !this.latchedForward && !this.latchedBack && !this.latchedLeft && !this.latchedRight) {
         class_243 vel = mc.field_1724.method_18798();
         double vx = vel.field_1352;
         double vz = vel.field_1350;
         if (!(vx * vx + vz * vz < 1.0E-8)) {
            class_243 flatLook = flatLook(mc.field_1724.method_36454());
            double dot = vx * flatLook.field_1352 + vz * flatLook.field_1350;
            double perp = vx * -flatLook.field_1350 + vz * flatLook.field_1352;
            if (Math.abs(dot) > Math.abs(perp)) {
               if (dot > 0.008) {
                  this.latchedForward = true;
               } else if (dot < -0.008) {
                  this.latchedBack = true;
               }
            } else if (perp > 0.008) {
               this.latchedLeft = true;
            } else if (perp < -0.008) {
               this.latchedRight = true;
            }
         }
      }

   }

   private static class_243 flatLook(float yawDeg) {
      double yawRad = Math.toRadians((double)yawDeg);
      return new class_243(-Math.sin(yawRad), (double)0.0F, Math.cos(yawRad));
   }

   private void clearMovementLatches() {
      this.latchedForward = this.latchedBack = this.latchedLeft = this.latchedRight = false;
      this.latchedJump = this.latchedSneak = this.latchedSprint = false;
      this.autopilotWarmupTicks = 0;
   }

   public void onTick() {
      class_310 client = class_310.method_1551();
      if (this.isEnabled()) {
         this.tryCompleteActivation(client);
         if (this.active && client.field_1724 != null) {
            if (this.autopilotWarmupTicks > 0) {
               --this.autopilotWarmupTicks;
               this.mergeAutopilotFromCurrentState(client);
            }

            this.prevX = this.currentX;
            this.prevY = this.currentY;
            this.prevZ = this.currentZ;
            this.prevYaw = this.currentYaw;
            this.prevPitch = this.currentPitch;
            float spd = this.speed.getFloat();
            float vMul = this.verticalMultiplier.getFloat();
            float moveFactor = (Boolean)this.smoothing.get() ? 0.5F : 1.0F;
            class_315 o = client.field_1690;
            double forward = (double)0.0F;
            double strafe = (double)0.0F;
            double vertical = (double)0.0F;
            if (o.field_1894.method_1434()) {
               ++forward;
            }

            if (o.field_1881.method_1434()) {
               --forward;
            }

            if (o.field_1913.method_1434()) {
               ++strafe;
            }

            if (o.field_1849.method_1434()) {
               --strafe;
            }

            if (o.field_1903.method_1434()) {
               ++vertical;
            }

            if (o.field_1832.method_1434()) {
               --vertical;
            }

            class_746 p = client.field_1724;
            if (p.method_31549().field_7477) {
               p.method_31549().field_7479 = false;
            }

            double yawRad = Math.toRadians((double)this.currentYaw);
            double mx = -Math.sin(yawRad) * forward * (double)spd + Math.cos(yawRad) * strafe * (double)spd;
            double mz = Math.cos(yawRad) * forward * (double)spd + Math.sin(yawRad) * strafe * (double)spd;
            double my = vertical * (double)spd * (double)vMul;
            this.currentX += mx * (double)moveFactor;
            this.currentY += my * (double)moveFactor;
            this.currentZ += mz * (double)moveFactor;
            this.syncFreecamChunkLoading(client);
         }
      }

   }

   public static void reapplyBodyInput(class_310 client) {
      FreecamModule f = instance;
      if (f != null && f.isActive() && client.field_1724 != null) {
         class_746 player = client.field_1724;
         class_315 o = client.field_1690;
         boolean mergePhysical = (Boolean)f.bodyFollowsKeys.get();
         boolean fwd = f.latchedForward || mergePhysical && o.field_1894.method_1434();
         boolean back = f.latchedBack || mergePhysical && o.field_1881.method_1434();
         boolean left = f.latchedLeft || mergePhysical && o.field_1913.method_1434();
         boolean right = f.latchedRight || mergePhysical && o.field_1849.method_1434();
         boolean jump = f.latchedJump;
         boolean sneak = f.latchedSneak || mergePhysical && o.field_1832.method_1434();
         boolean sprint = f.latchedSprint || mergePhysical && o.field_1867.method_1434();
         if (mergePhysical) {
            if (o.field_1881.method_1434()) {
               f.latchedForward = false;
            }

            if (o.field_1894.method_1434()) {
               f.latchedBack = false;
            }

            if (o.field_1849.method_1434()) {
               f.latchedLeft = false;
            }

            if (o.field_1913.method_1434()) {
               f.latchedRight = false;
            }
         }

         player.field_3913.field_54155 = new class_10185(fwd, back, left, right, jump, sneak, sprint);
         float sx = (left ? 1.0F : 0.0F) - (right ? 1.0F : 0.0F);
         float sz = (fwd ? 1.0F : 0.0F) - (back ? 1.0F : 0.0F);
         class_241 moveVector = (new class_241(sx, sz)).method_35581();
         ((ClientInputAccessor)player.field_3913).solclient$setMoveVector(moveVector);
         player.method_5660(sneak);
      }

   }

   public boolean hasLatchedLocomotion() {
      return this.latchedForward || this.latchedBack || this.latchedLeft || this.latchedRight || this.latchedJump || this.latchedSneak;
   }

   private void syncFreecamChunkLoading(class_310 client) {
      if (this.active && client.field_1687 != null && client.field_1724 != null) {
         class_631 ccm = client.field_1687.method_2935();
         class_1923 camChunk = new class_1923((int)Math.floor(this.currentX) >> 4, (int)Math.floor(this.currentZ) >> 4);
         class_1923 bodyChunk = client.field_1724.method_31476();
         class_1923 centerChunk = camChunk;
         int sepCamBody = Math.max(Math.abs(camChunk.field_9181 - bodyChunk.field_9181), Math.abs(camChunk.field_9180 - bodyChunk.field_9180));
         if (sepCamBody + 4 > 32) {
            int mx = (camChunk.field_9181 + bodyChunk.field_9181) / 2;
            int mz = (camChunk.field_9180 + bodyChunk.field_9180) / 2;
            centerChunk = new class_1923(mx, mz);
         }

         int spread = Math.max(Math.max(Math.abs(centerChunk.field_9181 - camChunk.field_9181), Math.abs(centerChunk.field_9180 - camChunk.field_9180)), Math.max(Math.abs(centerChunk.field_9181 - bodyChunk.field_9181), Math.abs(centerChunk.field_9180 - bodyChunk.field_9180)));
         int userDist = Math.min(32, Math.max(2, Math.round(this.freecamChunkDistance.getFloat())));
         int dist = Math.min(32, Math.max(userDist, spread + 4));
         if (dist != this.lastSyncedLoadDistance) {
            ccm.method_20180(dist);
            this.lastSyncedLoadDistance = dist;
         }

         if (this.lastSyncedCamChunk == null || centerChunk.field_9181 != this.lastSyncedCamChunk.field_9181 || centerChunk.field_9180 != this.lastSyncedCamChunk.field_9180) {
            ccm.method_20317(centerChunk.field_9181, centerChunk.field_9180);
            this.lastSyncedCamChunk = centerChunk;
            if (client.field_1769 != null) {
               client.field_1769.method_3292();
            }
         }
      }

   }

   private void restoreVanillaChunkLoading(class_310 client) {
      this.lastSyncedCamChunk = null;
      this.lastSyncedLoadDistance = Integer.MIN_VALUE;
      if (client.field_1687 != null) {
         class_631 ccm = client.field_1687.method_2935();
         if (client.field_1724 != null) {
            class_1923 p = client.field_1724.method_31476();
            ccm.method_20317(p.field_9181, p.field_9180);
         }

         ccm.method_20180(client.field_1690.method_38521());
         if (client.field_1769 != null) {
            client.field_1769.method_3292();
         }
      }

   }

   private void applyViewOnlyClientState(class_310 mc) {
      this.savedSmartCull = mc.field_1730;
      mc.field_1730 = false;
   }

   private void restoreViewOnlyClientState(class_310 mc) {
      mc.field_1730 = this.savedSmartCull;
      if (mc.field_1761 != null) {
         mc.field_1761.method_2925();
      }

   }

   public boolean isActive() {
      return this.isEnabled() && this.active;
   }

   public boolean isShowPlayerModel() {
      return (Boolean)this.showPlayerModel.get();
   }

   public boolean isShowHands() {
      return (Boolean)this.showHands.get();
   }

   public boolean renderHands() {
      return !this.isActive() || this.isShowHands();
   }

   public boolean wasHoldingSneak() {
      return this.latchedSneak;
   }

   public double getInterpolatedX(float tickDelta) {
      return class_3532.method_16436((double)tickDelta, this.prevX, this.currentX);
   }

   public double getInterpolatedY(float tickDelta) {
      return class_3532.method_16436((double)tickDelta, this.prevY, this.currentY);
   }

   public double getInterpolatedZ(float tickDelta) {
      return class_3532.method_16436((double)tickDelta, this.prevZ, this.currentZ);
   }

   public float getInterpolatedYaw(float tickDelta) {
      return class_3532.method_16439(tickDelta, this.prevYaw, this.currentYaw);
   }

   public float getInterpolatedPitch(float tickDelta) {
      return class_3532.method_16439(tickDelta, this.prevPitch, this.currentPitch);
   }

   public class_243 getInterpolatedPos(float tickDelta) {
      return new class_243(this.getInterpolatedX(tickDelta), this.getInterpolatedY(tickDelta), this.getInterpolatedZ(tickDelta));
   }

   public void setRotation(float yaw, float pitch) {
      this.currentYaw = yaw;
      this.currentPitch = class_3532.method_15363(pitch, -90.0F, 90.0F);
   }

   public float getCurrentYaw() {
      return this.currentYaw;
   }

   public float getCurrentPitch() {
      return this.currentPitch;
   }

   public float getLookSensitivity() {
      return this.lookSensitivity.getFloat();
   }
}
