package dev.sol.render;

import com.mojang.blaze3d.textures.GpuTexture;
import dev.sol.SolClient;
import dev.sol.hud.HudDragController;
import dev.sol.hud.HudManager;
import dev.sol.module.impl.CustomCrosshairModule;
import dev.sol.module.impl.MotionBlurModule;
import dev.sol.notification.NotificationManager;
import dev.sol.render.nanovg.GlStateSnapshot;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.util.Colors;
import net.minecraft.class_10868;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_408;
import org.lwjgl.opengl.GL33C;

public final class OverlayRenderer {
   private static HudManager hudManager;
   private static NotificationManager notifications;
   private static int fbo = -1;
   private static boolean crashed;

   private OverlayRenderer() {
   }

   public static void init(HudManager hud, NotificationManager toasts) {
      hudManager = hud;
      notifications = toasts;
   }

   public static float uiScale() {
      class_310 mc = class_310.method_1551();
      return Math.max(1.0F, (float)mc.method_22683().method_4506() / 1080.0F);
   }

   public static void render() {
      try {
         GlintTextureTinter.tick();
      } catch (Throwable var19) {
      }

      if (!crashed && hudManager != null) {
         class_310 mc = class_310.method_1551();
         class_276 target = mc.method_1522();
         if (target != null) {
            GpuTexture var3 = target.method_30277();
            if (var3 instanceof class_10868) {
               class_10868 colorTexture = (class_10868)var3;
               boolean var22 = mc.field_1755 instanceof NvgDrawable;
               boolean chatEditing = mc.field_1755 instanceof class_408;
               boolean drawHud = !mc.field_1690.field_1842 && mc.field_1687 != null && !var22;
               boolean drawToasts = mc.field_1687 != null && !mc.field_1690.field_1842;
               MotionBlurModule mbModule = SolClient.modules() != null ? SolClient.modules().motionBlur : null;
               boolean motionBlur = mbModule != null && mbModule.isEnabled() && mc.field_1687 != null && !var22;
               if (var22 || drawHud || drawToasts || motionBlur) {
                  try {
                     GlStateSnapshot state = GlStateSnapshot.capture();

                     try {
                        if (bindOverlayFbo(colorTexture, target.field_1482, target.field_1481)) {
                           NVGRenderer vg = NVGRenderer.get();
                           if (motionBlur) {
                              MotionBlurRenderer.render(vg, target.field_1482, target.field_1481, mbModule);
                           }

                           vg.setFontMode((String)SolClient.modules().clickGui.font.get());
                           if (!vg.hasFont()) {
                              return;
                           }

                           float scale = uiScale();
                           float uiWidth = (float)target.field_1482 / scale;
                           float uiHeight = (float)target.field_1481 / scale;
                           vg.beginFrame((float)target.field_1482, (float)target.field_1481, 1.0F);
                           vg.save();
                           vg.scale(scale);
                           if (drawHud) {
                              if (chatEditing && HudDragController.isDragging()) {
                                 HudDragController.updateDrag(vg);
                              }

                              CustomCrosshairModule crosshair = SolClient.modules().customCrosshair;
                              if (crosshair.isEnabled() && mc.field_1755 == null) {
                                 crosshair.render(vg, uiWidth / 2.0F, uiHeight / 2.0F);
                              }

                              if (mc.field_1755 == null) {
                                 WorldNametagRenderer.render(vg);
                              }

                              hudManager.render(vg, uiWidth, uiHeight);
                              if (chatEditing) {
                                 renderChatEditOverlay(vg, uiWidth, uiHeight);
                              }
                           }

                           if (drawToasts) {
                              notifications.renderToasts(vg, uiWidth, uiHeight);
                           }

                           if (var22) {
                              ((NvgDrawable)mc.field_1755).renderNvg(vg, uiMouseX(), uiMouseY(), uiWidth, uiHeight);
                           }

                           vg.restore();
                           vg.endFrame();
                           return;
                        }
                     } finally {
                        state.restore();
                     }

                     return;
                  } catch (Throwable var21) {
                     crashed = true;
                     SolClient.LOGGER.error("Client overlay renderer crashed; disabling overlay", var21);
                  }
               }
            }
         }
      }

   }

   private static void renderChatEditOverlay(NVGRenderer vg, float uiWidth, float uiHeight) {
      Theme theme = SolClient.themes().current();
      float mx = uiMouseX();
      float my = uiMouseY();
      vg.text("Drag to move  ·  scroll to resize", (uiWidth - vg.textWidth("Drag to move  ·  scroll to resize", 14.0F)) / 2.0F, 22.0F, 14.0F, Colors.withAlpha(theme.textMuted(), 0.9F));

      for(HudManager.Placement p : hudManager.layout(vg, uiWidth, uiHeight, true)) {
         boolean hidden = !p.component().visible();
         boolean active = p.contains(mx, my) || p.component() == HudDragController.draggedComponent();
         if (hidden) {
            vg.save();
            vg.alpha(0.35F);
            hudManager.renderPlacement(vg, p);
            vg.restore();
         }

         int outline = active ? theme.accentBright() : Colors.withAlpha(theme.accent(), 0.5F);
         vg.rectOutline(p.getFloat() - 3.0F, p.getFloat2() - 3.0F, p.getFloat3() + 6.0F, p.getFloat4() + 6.0F, 6.0F, active ? 1.6F : 1.0F, outline);
         vg.rect(p.getFloat() + p.getFloat3() - 2.0F, p.getFloat2() + p.getFloat4() - 2.0F, 6.0F, 6.0F, 2.0F, outline);
         if (active) {
            float var10000 = p.component().getScale();
            String pct = Math.round(var10000 * 100.0F) + "%";
            vg.text(pct, p.getFloat() + p.getFloat3() + 6.0F, p.getFloat2() + p.getFloat4() / 2.0F, 11.0F, theme.accentBright());
         }
      }

   }

   private static boolean bindOverlayFbo(class_10868 colorTexture, int width, int height) {
      if (fbo == -1) {
         fbo = GL33C.glGenFramebuffers();
      }

      GL33C.glBindFramebuffer(36160, fbo);
      GL33C.glFramebufferTexture2D(36160, 36064, 3553, colorTexture.method_68427(), 0);
      if (GL33C.glCheckFramebufferStatus(36160) != 36053) {
         return false;
      } else {
         GL33C.glViewport(0, 0, width, height);
         GL33C.glDisable(3089);
         return true;
      }
   }

   public static float uiMouseX() {
      class_310 mc = class_310.method_1551();
      return (float)(mc.field_1729.method_1603() * (double)mc.method_22683().method_4489() / (double)Math.max(1, mc.method_22683().method_4480())) / uiScale();
   }

   public static float uiMouseY() {
      class_310 mc = class_310.method_1551();
      return (float)(mc.field_1729.method_1604() * (double)mc.method_22683().method_4506() / (double)Math.max(1, mc.method_22683().method_4507())) / uiScale();
   }

   public static float guiToUi(double guiCoord) {
      class_310 mc = class_310.method_1551();
      float pixels = (float)(guiCoord * (double)mc.method_22683().method_4495());
      return pixels / uiScale();
   }

   public static float uiWidth() {
      class_310 mc = class_310.method_1551();
      return (float)mc.method_22683().method_4489() / uiScale();
   }

   public static float uiHeight() {
      class_310 mc = class_310.method_1551();
      return (float)mc.method_22683().method_4506() / uiScale();
   }
}
