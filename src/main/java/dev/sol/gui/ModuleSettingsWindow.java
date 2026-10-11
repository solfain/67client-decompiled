package dev.sol.gui;

import dev.sol.gui.widget.BlockListWidget;
import dev.sol.gui.widget.BooleanWidget;
import dev.sol.gui.widget.ColorWidget;
import dev.sol.gui.widget.IconListWidget;
import dev.sol.gui.widget.KeybindWidget;
import dev.sol.gui.widget.ModeWidget;
import dev.sol.gui.widget.SettingWidget;
import dev.sol.gui.widget.SliderWidget;
import dev.sol.gui.widget.StringWidget;
import dev.sol.module.Module;
import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.BlockListSetting;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.IconListSetting;
import dev.sol.settings.KeybindSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.Setting;
import dev.sol.settings.SliderSetting;
import dev.sol.settings.StringSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.ArrayList;
import java.util.List;

public class ModuleSettingsWindow {
   public static final float WIDTH = 286.0F;
   private static final float HEADER_H = 28.0F;
   private static final float RADIUS = 4.0F;
   private static final float PAD = 10.0F;
   private static final float GAP = 4.0F;
   private static final float DESC_SIZE = 11.5F;
   private static final float DESC_LINE = 14.0F;
   private static final float TOGGLE_ROW_H = 26.0F;
   private final Module module;
   private final ThemeManager themes;
   private final String key;
   private final List<SettingWidget> widgets = new ArrayList();
   private final Animation open = new Animation(160.0F, 0.0F);
   private final Animation enable = new Animation(140.0F, 0.0F);
   private final Animation scroll = new Animation(160.0F, 0.0F);
   private float x = Float.NaN;
   private float y = Float.NaN;
   private float viewTop;
   private float viewH;
   private float maxScroll;
   private boolean dragging;
   private float dragDx;
   private float dragDy;
   private boolean closing;
   private List<String> descLines = List.of();
   private float descWrapWidth = -1.0F;

   public ModuleSettingsWindow(Module module, ThemeManager themes, String key) {
      this.module = module;
      this.themes = themes;
      this.key = key;
      this.enable.snapTo(module.isEnabled() ? 1.0F : 0.0F);
      this.widgets.addAll(createWidgets(themes, module));
      this.open.setTarget(1.0F);
   }

   public static List<SettingWidget> createWidgets(ThemeManager themes, Module module) {
      List<SettingWidget> out = new ArrayList();

      for(Setting<?> setting : module.getSettings()) {
         if (setting instanceof BooleanSetting b) {
            out.add(new BooleanWidget(themes, b));
         } else if (setting instanceof SliderSetting s) {
            out.add(new SliderWidget(themes, s));
         } else if (setting instanceof ModeSetting m) {
            out.add(new ModeWidget(themes, m));
         } else if (setting instanceof ColorSetting c) {
            out.add(new ColorWidget(themes, c));
         } else if (setting instanceof BlockListSetting bl) {
            out.add(new BlockListWidget(themes, bl));
         } else if (setting instanceof IconListSetting il) {
            out.add(new IconListWidget(themes, il));
         } else if (setting instanceof StringSetting str) {
            out.add(new StringWidget(themes, str));
         } else if (setting instanceof KeybindSetting k) {
            out.add(new KeybindWidget(themes, k));
         }
      }

      out.add(new KeybindWidget(themes, module.getKeybind()));
      return out;
   }

   public static String keyOf(Module module) {
      return module.getName() + "@" + module.getCategory().name();
   }

   public String key() {
      return this.key;
   }

   public Module module() {
      return this.module;
   }

   public void close() {
      if (!this.closing) {
         this.closing = true;
         this.open.setTarget(0.0F);
         this.dragging = false;
      }
   }

   public boolean isClosing() {
      return this.closing;
   }

   public boolean isGone() {
      return this.closing && this.open.isDone() && this.open.value() <= 0.01F;
   }

   private Theme theme() {
      return this.themes.current();
   }

   private List<String> wrap(NVGRenderer vg, String text, float maxW) {
      List<String> lines = new ArrayList();
      if (text == null || text.isBlank()) {
         return lines;
      } else {
         StringBuilder line = new StringBuilder();

         for(String word : text.trim().split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (vg.textWidth(candidate, DESC_SIZE) > maxW && !line.isEmpty()) {
               lines.add(line.toString());
               line.setLength(0);
               line.append(word);
            } else {
               line.setLength(0);
               line.append(candidate);
            }
         }

         if (!line.isEmpty()) {
            lines.add(line.toString());
         }

         return lines.size() > 4 ? lines.subList(0, 4) : lines;
      }
   }

   private float contentHeight(NVGRenderer vg) {
      float h = PAD;

      for(SettingWidget widget : this.widgets) {
         if (widget.isVisible()) {
            h += widget.height(vg) + GAP;
         }
      }

      return h + PAD - GAP;
   }

   private float topBlockHeight() {
      float desc = this.descLines.isEmpty() ? 0.0F : (float)this.descLines.size() * DESC_LINE + 6.0F;
      return 8.0F + desc + TOGGLE_ROW_H + 1.0F;
   }

   public void render(NVGRenderer vg, float mx, float my, float uiWidth, float uiHeight) {
      Theme theme = this.theme();
      float t = this.open.value();
      if (!(t <= 0.005F)) {
         float innerW = WIDTH - PAD * 2.0F;
         if (innerW != this.descWrapWidth) {
            this.descWrapWidth = innerW;
            this.descLines = this.wrap(vg, this.module.getDescription(), innerW);
         }

         float top = this.topBlockHeight();
         float content = this.contentHeight(vg);
         float maxBody = Math.max(120.0F, uiHeight * 0.72F - HEADER_H - top);
         this.viewH = Math.min(content, maxBody);
         this.maxScroll = Math.max(0.0F, content - this.viewH);
         this.scroll.setTarget(Math.clamp(this.scroll.getTarget(), 0.0F, this.maxScroll));
         float totalH = HEADER_H + top + this.viewH;
         if (Float.isNaN(this.x)) {
            this.x = (uiWidth - WIDTH) / 2.0F;
            this.y = Math.max(40.0F, (uiHeight - totalH) / 2.0F);
         }

         this.x = Math.clamp(this.x, 0.0F, Math.max(0.0F, uiWidth - WIDTH));
         this.y = Math.clamp(this.y, 0.0F, Math.max(0.0F, uiHeight - HEADER_H));
         float wx = this.x;
         float wy = this.y + (1.0F - t) * 10.0F;

         vg.rect(0.0F, 0.0F, uiWidth, uiHeight, 0.0F, Colors.withAlpha(0xFF000000, 0.28F * t));
         vg.save();
         vg.alpha(t);
         vg.glow(wx, wy, WIDTH, totalH, RADIUS, 10.0F, Colors.withAlpha(0xFF000000, 0.45F));
         vg.rectVaryingGradient(wx, wy + HEADER_H, WIDTH, totalH - HEADER_H, 0.0F, 0.0F, RADIUS, RADIUS, Colors.withAlpha(theme.background(), 0.97F), Colors.withAlpha(theme.background(), 0.97F));
         vg.rectOutline(wx, wy, WIDTH, totalH, RADIUS, 1.0F, theme.outline());

         vg.rectVaryingGradient(wx, wy, WIDTH, HEADER_H, RADIUS, RADIUS, 0.0F, 0.0F, theme.accent(), Colors.lerp(theme.accent(), theme.accentDark(), 0.18F));
         int onAccent = theme.onAccent();
         float hcy = wy + HEADER_H / 2.0F;
         vg.text(this.module.getName(), wx + PAD, hcy, 14.0F, onAccent);
         String cat = this.module.getCategory().getDisplayName().toUpperCase();
         float closeX = wx + WIDTH - 20.0F;
         vg.text(cat, closeX - 8.0F - vg.textWidth(cat, 9.5F), hcy, 9.5F, Colors.withAlpha(onAccent, 0.65F));
         boolean closeHover = mx >= closeX - 2.0F && mx <= closeX + 14.0F && my >= hcy - 8.0F && my <= hcy + 8.0F;
         if (closeHover) {
            vg.rect(closeX - 2.0F, hcy - 8.0F, 16.0F, 16.0F, 2.0F, Colors.withAlpha(0xFF000000, 0.18F));
         }

         vg.cross(closeX, hcy - 6.0F, 12.0F, 1.6F, onAccent);

         float cy = wy + HEADER_H + 8.0F;
         for(String line : this.descLines) {
            vg.text(line, wx + PAD, cy + DESC_LINE / 2.0F, DESC_SIZE, theme.textMuted());
            cy += DESC_LINE;
         }

         if (!this.descLines.isEmpty()) {
            cy += 6.0F;
         }

         this.enable.setTarget(this.module.isEnabled() ? 1.0F : 0.0F);
         float et = this.enable.value();
         float rowCy = cy + TOGGLE_ROW_H / 2.0F;
         boolean rowHover = mx >= wx && mx <= wx + WIDTH && my >= cy && my <= cy + TOGGLE_ROW_H;
         if (rowHover) {
            vg.rect(wx + 4.0F, cy, WIDTH - 8.0F, TOGGLE_ROW_H, 3.0F, theme.rowHover());
         }

         vg.text("Enabled", wx + PAD, rowCy, 12.5F, theme.textPrimary());
         float sw = 28.0F;
         float sh = 14.0F;
         float sx = wx + WIDTH - PAD - sw;
         vg.rect(sx, rowCy - sh / 2.0F, sw, sh, sh / 2.0F, Colors.lerp(theme.track(), theme.accent(), et));
         vg.circle(sx + sh / 2.0F + (sw - sh) * et, rowCy, sh / 2.0F - 2.0F, Colors.lerp(theme.textMuted(), 0xFFFFFFFF, et));
         cy += TOGGLE_ROW_H;
         vg.rect(wx + PAD, cy, WIDTH - PAD * 2.0F, 1.0F, 0.0F, theme.outline());
         cy += 1.0F;

         this.viewTop = cy;
         vg.save();
         vg.scissor(wx, this.viewTop, WIDTH, this.viewH);
         float wyCursor = this.viewTop + PAD - this.scroll.value();
         boolean inView = my >= this.viewTop && my <= this.viewTop + this.viewH;
         float wmx = inView ? mx : -10000.0F;
         float wmy = inView ? my : -10000.0F;
         for(SettingWidget widget : this.widgets) {
            if (widget.isVisible()) {
               widget.setBounds(wx + PAD, wyCursor, WIDTH - PAD * 2.0F);
               float h = widget.height(vg);
               if (wyCursor + h >= this.viewTop - 4.0F && wyCursor <= this.viewTop + this.viewH + 4.0F) {
                  widget.render(vg, wmx, wmy);
               }

               wyCursor += h + GAP;
            }
         }

         vg.restore();
         if (this.maxScroll > 0.5F) {
            float trackH = this.viewH - 8.0F;
            float thumbH = Math.max(16.0F, trackH * (this.viewH / (this.viewH + this.maxScroll)));
            float thumbY = this.viewTop + 4.0F + (trackH - thumbH) * (this.scroll.value() / this.maxScroll);
            vg.rect(wx + WIDTH - 4.0F, thumbY, 2.0F, thumbH, 1.0F, Colors.withAlpha(theme.accent(), 0.6F));
         }

         vg.restore();
      }
   }

   public boolean contains(float mx, float my) {
      if (Float.isNaN(this.x)) {
         return false;
      } else {
         float totalH = HEADER_H + this.topBlockHeight() + this.viewH;
         return mx >= this.x && mx <= this.x + WIDTH && my >= this.y && my <= this.y + totalH;
      }
   }

   private boolean headerHit(float mx, float my) {
      return mx >= this.x && mx <= this.x + WIDTH && my >= this.y && my <= this.y + HEADER_H;
   }

   private boolean closeHit(float mx, float my) {
      float closeX = this.x + WIDTH - 20.0F;
      float hcy = this.y + HEADER_H / 2.0F;
      return mx >= closeX - 2.0F && mx <= closeX + 14.0F && my >= hcy - 8.0F && my <= hcy + 8.0F;
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (this.closing) {
         return false;
      }

      for(SettingWidget widget : this.widgets) {
         if (widget.isListening() && widget instanceof KeybindWidget) {
            widget.mouseClicked(mx, my, button);
            return true;
         }
      }

      if (!this.contains(mx, my)) {
         return false;
      } else if (this.closeHit(mx, my)) {
         this.close();
         return true;
      } else if (this.headerHit(mx, my)) {
         if (button == 0) {
            this.dragging = true;
            this.dragDx = mx - this.x;
            this.dragDy = my - this.y;
         }

         return true;
      } else {
         float toggleTop = this.viewTop - 1.0F - TOGGLE_ROW_H;
         if (my >= toggleTop && my <= toggleTop + TOGGLE_ROW_H) {
            if (button == 0) {
               this.module.toggle();
            }

            return true;
         } else {
            boolean inView = my >= this.viewTop && my <= this.viewTop + this.viewH;
            for(SettingWidget widget : this.widgets) {
               if (widget.isVisible()) {
                  widget.mouseClicked(inView ? mx : -10000.0F, inView ? my : -10000.0F, button);
               }
            }

            return true;
         }
      }
   }

   public void mouseDragged(float mx, float my) {
      if (this.dragging) {
         this.x = mx - this.dragDx;
         this.y = my - this.dragDy;
      } else {
         for(SettingWidget widget : this.widgets) {
            widget.mouseDragged(mx, my);
         }
      }
   }

   public void mouseReleased() {
      this.dragging = false;

      for(SettingWidget widget : this.widgets) {
         widget.mouseReleased();
      }
   }

   public void onScroll(double amount) {
      if (this.maxScroll > 0.0F) {
         this.scroll.setTarget(Math.clamp(this.scroll.getTarget() - (float)amount * 30.0F, 0.0F, this.maxScroll));
      }
   }

   public boolean keyPressed(int keyCode) {
      for(SettingWidget widget : this.widgets) {
         if (widget.keyPressed(keyCode)) {
            return true;
         }
      }

      return false;
   }

   public boolean charTyped(int codepoint) {
      for(SettingWidget widget : this.widgets) {
         if (widget.charTyped(codepoint)) {
            return true;
         }
      }

      return false;
   }

   public boolean isListening() {
      for(SettingWidget widget : this.widgets) {
         if (widget.isListening()) {
            return true;
         }
      }

      return false;
   }
}
