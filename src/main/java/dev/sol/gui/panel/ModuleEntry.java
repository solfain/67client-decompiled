package dev.sol.gui.panel;

import dev.sol.gui.ClickGuiState;
import dev.sol.module.Module;
import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;

/**
 * One compact module row inside a category window.
 *
 * Left-click toggles the module, right-click opens its settings window
 * (see {@link dev.sol.gui.ModuleSettingsWindow}). Enabled modules get an
 * accent bar on the left edge and a faint accent wash.
 */
public class ModuleEntry {
   public static final float ROW_H = 20.0F;
   private static final float TEXT_SIZE = 12.5F;
   private Module module;
   private ThemeManager themes;
   private ClickGuiState state;
   private String key;
   private final Animation hover = new Animation(110.0F, 0.0F);
   private final Animation enable = new Animation(150.0F, 0.0F);
   private float floatVal;
   private float floatVal2;
   private float width;

   public ModuleEntry(Module module, ThemeManager themes, ClickGuiState state) {
      this.module = module;
      this.themes = themes;
      this.state = state;
      this.key = keyOf(module);
      this.enable.snapTo(module.isEnabled() ? 1.0F : 0.0F);
   }

   public static String keyOf(Module module) {
      return module.getName() + "@" + module.getCategory().name();
   }

   public Module getModule() {
      return this.module;
   }

   public void setBounds(float x, float y, float width) {
      this.floatVal = x;
      this.floatVal2 = y;
      this.width = width;
   }

   public float height(NVGRenderer vg) {
      return ROW_H;
   }

   private boolean hit(float mx, float my) {
      return mx >= this.floatVal && mx <= this.floatVal + this.width && my >= this.floatVal2 && my <= this.floatVal2 + ROW_H;
   }

   public void render(NVGRenderer vg, float mx, float my, float fadeAlpha) {
      Theme theme = this.theme();
      boolean hovered = this.hit(mx, my);
      if (hovered && this.hover.getTarget() < 0.5F) {
         UiSounds.hover();
      }

      this.hover.setTarget(hovered ? 1.0F : 0.0F);
      this.enable.setTarget(this.module.isEnabled() ? 1.0F : 0.0F);
      float hoverT = this.hover.value();
      float enableT = this.enable.value();
      boolean open = this.state.isExpanded(this.key);
      float x = this.floatVal;
      float y = this.floatVal2;
      float cy = y + ROW_H / 2.0F;
      vg.save();
      vg.alpha(fadeAlpha);
      if (enableT > 0.01F) {
         vg.rect(x, y, this.width, ROW_H, 0.0F, Colors.withAlpha(theme.moduleActiveFill(), Colors.alpha(theme.moduleActiveFill()) / 255.0F * enableT));
         vg.rect(x, y + 2.0F, 2.0F * enableT, ROW_H - 4.0F, 1.0F, theme.accent());
      }

      if (hoverT > 0.01F) {
         vg.rect(x, y, this.width, ROW_H, 0.0F, Colors.withAlpha(theme.rowHover(), Colors.alpha(theme.rowHover()) / 255.0F * hoverT));
      }

      int idle = Colors.lerp(theme.textMuted(), theme.textPrimary(), hoverT);
      int on = Colors.lerp(theme.textPrimary(), theme.accentBright(), 0.35F);
      int textColor = Colors.lerp(idle, on, enableT);
      vg.textTruncated(this.module.getName(), x + 8.0F + 2.0F * enableT, cy, TEXT_SIZE, textColor, this.width - 30.0F);

      // settings affordance: three small dots, lit while this module's settings window is open
      if (!this.module.getSettings().isEmpty() || hovered) {
         int dots = open ? theme.accent() : Colors.withAlpha(theme.textDisabled(), 0.6F + 0.4F * hoverT);
         float dx = x + this.width - 10.0F;
         for(int i = -1; i <= 1; ++i) {
            vg.circle(dx, cy + (float)i * 3.5F, 1.1F, dots);
         }
      }

      vg.restore();
   }

   private Theme theme() {
      return this.themes.current();
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (!this.hit(mx, my)) {
         return false;
      } else {
         if (button == 0) {
            this.module.toggle();
            UiSounds.toggle(this.module.isEnabled());
         } else if (button == 1) {
            boolean nowOpen = !this.state.isExpanded(this.key);
            this.state.setExpanded(this.key, nowOpen);
            UiSounds.select();
         }

         return true;
      }
   }

   public void mouseDragged(float mx, float my) {
   }

   public void mouseReleased() {
   }

   public boolean keyPressed(int keyCode) {
      return false;
   }

   public boolean charTyped(int codepoint) {
      return false;
   }

   public boolean isListening() {
      return false;
   }
}
