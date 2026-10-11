package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.module.ModuleManager;
import dev.sol.module.Modules;
import dev.sol.render.anim.Animation;
import dev.sol.render.anim.Easing;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Enabled-module list. No row backgrounds: bold text with a soft shadow and a thin accent
 * tick on the outer edge. Rows ease in/out horizontally and fade toward a lighter tint
 * further down the list.
 */
public class ArrayListHud extends HudComponent {
   private static final float ROW = 15.0F;
   private static final float FONT = 12.0F;
   private static final float TICK_W = 1.5F;
   private static final float TICK_GAP = 5.0F;
   private ModuleManager modules;
   private Modules.HudModule hudModule;
   private ThemeManager themes;
   private final Map<Module, Animation> slide = new HashMap();

   public ArrayListHud(ModuleManager modules, Modules.HudModule hudModule, ThemeManager themes, BooleanSupplier visible) {
      super("arraylist", 1.0F, 0.008F, visible);
      this.modules = modules;
      this.hudModule = hudModule;
      this.themes = themes;
   }

   private List<Module> animatedEntries(NVGRenderer vg) {
      List<Module> list = new ArrayList();

      for(Module module : this.modules.all()) {
         if (module.getCategory() != Category.CLIENT) {
            Animation anim = (Animation)this.slide.computeIfAbsent(module, (m) -> new Animation(220.0F, m.isEnabled() ? 1.0F : 0.0F, Easing.EASE_OUT_CUBIC));
            anim.setTarget(module.isEnabled() ? 1.0F : 0.0F);
            if (module.isEnabled() || anim.value() > 0.01F) {
               list.add(module);
            }
         }
      }

      list.sort(Comparator.comparingDouble((Module m) -> (double)vg.textWidthBold(m.getName(), FONT)).reversed());
      return list;
   }

   public float measureWidth(NVGRenderer vg) {
      float max = 40.0F;

      for(Module m : this.animatedEntries(vg)) {
         max = Math.max(max, vg.textWidthBold(m.getName(), FONT) + TICK_GAP + TICK_W + 2.0F);
      }

      return max;
   }

   public float measureHeight(NVGRenderer vg) {
      float h = 0.0F;

      for(Module m : this.animatedEntries(vg)) {
         h += ROW * ((Animation)this.slide.get(m)).value();
      }

      return Math.max(ROW, h);
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      boolean sync = (Boolean)this.hudModule.themeSync.get();
      boolean right = this.rightAnchored();
      int base = sync ? theme.accent() : (Integer)this.hudModule.listColor.get();
      List<Module> entries = this.animatedEntries(vg);
      int count = Math.max(1, entries.size() - 1);
      float rowY = y;

      for(int i = 0; i < entries.size(); ++i) {
         Module m = (Module)entries.get(i);
         float t = ((Animation)this.slide.get(m)).value();
         if (!(t <= 0.01F)) {
            int color = Colors.lerp(base, Colors.lighten(base, 0.45F), (float)i / (float)count);
            float textW = vg.textWidthBold(m.getName(), FONT);
            float slideOff = (1.0F - t) * (textW + 20.0F) * (float)(right ? 1 : -1);
            float cy = (float)Math.round(rowY + ROW / 2.0F);
            vg.save();
            vg.alpha(t);
            if (right) {
               float tickX = x + w - TICK_W + slideOff;
               vg.rect(tickX, cy - 4.5F, TICK_W, 9.0F, 0.0F, color);
               HudStyle.shadowed(vg, m.getName(), (float)Math.round(tickX - TICK_GAP - textW), cy, FONT, color, true);
            } else {
               float tickX = x + slideOff;
               vg.rect(tickX, cy - 4.5F, TICK_W, 9.0F, 0.0F, color);
               HudStyle.shadowed(vg, m.getName(), (float)Math.round(tickX + TICK_W + TICK_GAP), cy, FONT, color, true);
            }

            vg.restore();
            rowY += ROW * t;
         }
      }

   }
}
