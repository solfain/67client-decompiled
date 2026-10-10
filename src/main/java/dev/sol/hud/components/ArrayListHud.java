package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
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

public class ArrayListHud extends HudComponent {
   private static final float ENTRY_HEIGHT = 20.0F;
   private static final float FONT_SIZE = 13.5F;
   private static final float PAD_X = 8.0F;
   private static final float STRIP_W = 2.5F;
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
            Animation anim = (Animation)this.slide.computeIfAbsent(module, (m) -> new Animation(240.0F, m.isEnabled() ? 1.0F : 0.0F, Easing.EASE_OUT_CUBIC));
            anim.setTarget(module.isEnabled() ? 1.0F : 0.0F);
            if (module.isEnabled() || anim.value() > 0.01F) {
               list.add(module);
            }
         }
      }

      list.sort(Comparator.comparingDouble((Module m) -> (double)vg.textWidthBold(m.getName(), 13.5F)).reversed());
      return list;
   }

   public float measureWidth(NVGRenderer vg) {
      float max = 40.0F;

      for(Module m : this.animatedEntries(vg)) {
         max = Math.max(max, vg.textWidthBold(m.getName(), 13.5F) + 16.0F + 2.5F + 2.0F);
      }

      return max;
   }

   public float measureHeight(NVGRenderer vg) {
      float h = 0.0F;

      for(Module m : this.animatedEntries(vg)) {
         h += 20.0F * ((Animation)this.slide.get(m)).value();
      }

      return Math.max(20.0F, h);
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      boolean sync = (Boolean)this.hudModule.themeSync.get();
      boolean right = this.rightAnchored();
      float rowY = y;

      for(Module m : this.animatedEntries(vg)) {
         float t = ((Animation)this.slide.get(m)).value();
         if (!(t <= 0.01F)) {
            int accent = sync ? theme.accent() : (Integer)this.hudModule.listColor.get();
            int accentBright = sync ? theme.accentBright() : Colors.lighten(accent, 0.35F);
            float textW = vg.textWidthBold(m.getName(), 13.5F);
            float barW = textW + 16.0F + 2.5F + 2.0F;
            float slideOff = (1.0F - t) * (barW + 12.0F) * (float)(right ? 1 : -1);
            float barX = (float)Math.round((right ? x + w - barW : x) + slideOff);
            float rowTop = (float)Math.round(rowY);
            vg.save();
            vg.alpha(t);
            vg.rect(barX, rowTop, barW, 20.0F, 0.0F, Colors.withAlpha(-15790317, 0.72F));
            float lineX = right ? barX + barW - 2.0F : barX;
            vg.rect(lineX, rowTop, 2.0F, 20.0F, 0.0F, accent);
            float textX = (float)Math.round(right ? barX + 8.0F : barX + 2.5F + 2.0F + 8.0F - 2.0F);
            vg.textBold(m.getName(), textX, rowTop + 10.0F, 13.5F, Colors.lerp(accent, accentBright, 0.25F));
            vg.restore();
            rowY += 20.0F * t;
         }
      }

   }
}
