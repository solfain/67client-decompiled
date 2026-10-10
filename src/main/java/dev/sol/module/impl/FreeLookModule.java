package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5498;
import org.lwjgl.glfw.GLFW;

public class FreeLookModule extends Module {
   private static FreeLookModule instance;
   public final ModeSetting mode = (ModeSetting)this.addSetting(new ModeSetting("Mode", "Which entity the mouse rotates.", "Player", new String[]{"Player", "Camera"}));
   public final BooleanSetting togglePerspective = (BooleanSetting)this.addSetting(new BooleanSetting("Toggle Perspective", "Switch to third person on toggle.", true));
   public final BooleanSetting throughWalls = (BooleanSetting)this.addSetting(new BooleanSetting("Through Walls", "See through walls — the third-person camera ignores wall collision.", false));
   public final SliderSetting sensitivity = (SliderSetting)this.addSetting(new SliderSetting("Camera Sensitivity", "How fast the camera moves in Camera mode.", (double)8.0F, (double)0.0F, (double)10.0F, 0.1));
   public final BooleanSetting arrows = (BooleanSetting)this.addSetting(new BooleanSetting("Arrows Control Opposite", "Control the other entity's rotation with the arrow keys.", true));
   public final SliderSetting arrowSpeed = (SliderSetting)this.addSetting(new SliderSetting("Arrow Speed", "Rotation speed with the arrow keys.", (double)4.0F, (double)0.0F, (double)10.0F, (double)0.5F));
   private float cameraYaw;
   private float cameraPitch;
   private class_5498 prePers;

   public FreeLookModule() {
      super("FreeLook", "Allows more rotation options in third person.", Category.MISC);
      instance = this;
   }

   public static FreeLookModule get() {
      return instance;
   }

   protected void onEnable() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         this.cameraYaw = mc.field_1724.method_36454();
         this.cameraPitch = mc.field_1724.method_36455();
         this.prePers = mc.field_1690.method_31044();
         if (this.prePers != class_5498.field_26665 && (Boolean)this.togglePerspective.get()) {
            mc.field_1690.method_31043(class_5498.field_26665);
         }
      }

   }

   protected void onDisable() {
      class_310 mc = class_310.method_1551();
      if (this.prePers != null && mc.field_1690.method_31044() != this.prePers && (Boolean)this.togglePerspective.get()) {
         mc.field_1690.method_31043(this.prePers);
      }

   }

   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         if ((Boolean)this.arrows.get()) {
            long win = mc.method_22683().method_4490();
            boolean left = GLFW.glfwGetKey(win, 263) == 1;
            boolean right = GLFW.glfwGetKey(win, 262) == 1;
            boolean up = GLFW.glfwGetKey(win, 265) == 1;
            boolean down = GLFW.glfwGetKey(win, 264) == 1;
            int iterations = (int)((Double)this.arrowSpeed.get() * (double)2.0F);

            for(int i = 0; i < iterations; ++i) {
               if (this.mode.check("Player")) {
                  if (left) {
                     this.cameraYaw -= 0.5F;
                  }

                  if (right) {
                     this.cameraYaw += 0.5F;
                  }

                  if (up) {
                     this.cameraPitch -= 0.5F;
                  }

                  if (down) {
                     this.cameraPitch += 0.5F;
                  }
               } else {
                  float yaw = mc.field_1724.method_36454();
                  float pitch = mc.field_1724.method_36455();
                  if (left) {
                     yaw -= 0.5F;
                  }

                  if (right) {
                     yaw += 0.5F;
                  }

                  if (up) {
                     pitch -= 0.5F;
                  }

                  if (down) {
                     pitch += 0.5F;
                  }

                  mc.field_1724.method_36456(yaw);
                  mc.field_1724.method_36457(pitch);
               }
            }
         }

         mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455(), -90.0F, 90.0F));
         this.cameraPitch = class_3532.method_15363(this.cameraPitch, -90.0F, 90.0F);
      }

   }

   public boolean isActive() {
      return this.isEnabled() && class_310.method_1551().field_1724 != null;
   }

   public boolean seeThroughWalls() {
      return this.isActive() && (Boolean)this.throughWalls.get();
   }

   public boolean cameraMode() {
      return this.isActive() && this.mode.check("Camera");
   }

   public boolean playerMode() {
      return this.isActive() && class_310.method_1551().field_1690.method_31044() == class_5498.field_26665 && this.mode.check("Player");
   }

   public void addCameraLook(double deltaX, double deltaY) {
      float sens = this.sensitivity.getFloat();
      if (sens <= 0.0F) {
         sens = 1.0F;
      }

      this.cameraYaw += (float)(deltaX / (double)sens);
      this.cameraPitch += (float)(deltaY / (double)sens);
      if (Math.abs(this.cameraPitch) > 90.0F) {
         this.cameraPitch = this.cameraPitch > 0.0F ? 90.0F : -90.0F;
      }

   }

   public float getCameraYaw() {
      return this.cameraYaw;
   }

   public float getCameraPitch() {
      return this.cameraPitch;
   }
}
