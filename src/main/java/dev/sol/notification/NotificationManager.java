package dev.sol.notification;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
import dev.sol.hud.HudDragController;
import dev.sol.module.Modules;
import dev.sol.render.anim.Animation;
import dev.sol.render.anim.Easing;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationManager extends HudComponent {
   private static final float WIDTH_PAD = 12.0F;
   private static final float HEIGHT = 30.0F;
   private static final float HEIGHT_WEATHER = 42.0F;
   private static final float GAP = 6.0F;
   private static final float FONT = 13.5F;
   private static final float FONT_SUB = 11.0F;
   private static final float floatVal = 26.0F;
   private static final float ANCHOR_W = 168.0F;
   private static final float ANCHOR_H = 30.0F;
   private ThemeManager themes;
   private Modules.HudModule hud;
   private final List<Toast> toasts = new ArrayList();

   public NotificationManager(ThemeManager themes, Modules.HudModule hud) {
      super("notifications", 0.99F, 0.71F, () -> hud.isEnabled() && (Boolean)hud.notifications.get());
      this.themes = themes;
      this.hud = hud;
   }

   private boolean enabled() {
      return this.hud.isEnabled() && (Boolean)this.hud.notifications.get();
   }

   private float lifeSeconds() {
      return this.hud.notifyDuration.getFloat();
   }

   public void push(String moduleName, boolean enabled) {
      this.add(new Toast(moduleName + (enabled ? " enabled" : " disabled"), (String)null, (Weather)null, enabled));
   }

   public void pushInfo(String text) {
      this.add(new Toast(text, (String)null, (Weather)null, true));
   }

   public void pushWeather(String title, String subtitle, Weather weather, boolean starting) {
      this.add(new Toast(title, subtitle, weather, starting));
   }

   private void add(Toast toast) {
      if (this.enabled()) {
         synchronized(this.toasts) {
            this.toasts.add(toast);
            if (this.toasts.size() > 6) {
               this.toasts.removeFirst();
            }
         }
      }

   }

   public float measureWidth(NVGRenderer vg) {
      return 168.0F;
   }

   public float measureHeight(NVGRenderer vg) {
      return 30.0F;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
   }

   public void renderToasts(NVGRenderer vg, float uiWidth, float uiHeight) {
      Theme theme = this.themes.current();
      float life = this.lifeSeconds();
      float lifeNanos = life * 1.0E9F;
      synchronized(this.toasts) {
         Iterator<Toast> it = this.toasts.iterator();
         List<Toast> alive = new ArrayList();

         while(it.hasNext()) {
            Toast toast = (Toast)it.next();
            if ((float)(System.nanoTime() - toast.bornNanos) > lifeNanos + 3.0E8F) {
               it.remove();
            } else {
               alive.add(toast);
            }
         }

         boolean editing = HudDragController.isEditing();
         if (!alive.isEmpty() || editing) {
            float scale = this.getScale();
            float w = 168.0F * scale;
            float h = 30.0F * scale;
            float boxX = this.getFx() * (uiWidth - w);
            float boxY = this.getFy() * (uiHeight - h);
            vg.save();
            vg.translate((float)Math.round(boxX), (float)Math.round(boxY));
            vg.scale(scale);
            float yCursor = 30.0F;

            for(int i = alive.size() - 1; i >= 0; --i) {
               Toast toast = (Toast)alive.get(i);
               float age = (float)(System.nanoTime() - toast.bornNanos) / 1.0E9F;
               float fadeOut = Math.clamp((life + 0.3F - age) / 0.3F, 0.0F, 1.0F);
               float slide = toast.slide.value();
               float remaining = Math.clamp(1.0F - age / Math.max(0.1F, life), 0.0F, 1.0F);
               float toastH = this.drawToast(vg, theme, 168.0F, yCursor, slide, fadeOut, remaining, toast.title, toast.subtitle, toast.weather, toast.accent);
               yCursor -= toastH + 6.0F;
            }

            if (alive.isEmpty() && editing) {
               this.drawToast(vg, theme, 168.0F, yCursor, 1.0F, 0.5F, 0.6F, "Notification", (String)null, (Weather)null, true);
            }

            vg.restore();
         }

      }
   }

   private float drawToast(NVGRenderer vg, Theme theme, float rightEdge, float bottom, float slide, float fadeOut, float remaining, String title, String subtitle, Weather weather, boolean accent) {
      boolean twoLine = subtitle != null;
      float h = twoLine ? 40.0F : 28.0F;
      float chip = 16.0F;
      float textStart = 9.0F + chip + 9.0F;
      float textW = Math.max(vg.textWidthBold(title, 12.0F), twoLine ? vg.textWidth(subtitle, 10.5F) : 0.0F);
      float w = textStart + textW + 12.0F;
      float x = rightEdge - w + (1.0F - slide) * (w + 10.0F);
      float y = bottom - h;
      vg.save();
      vg.alpha(fadeOut);
      HudStyle.plain(vg, x, y, w, h);
      float chipX = x + 9.0F;
      float chipY = y + (h - chip) / 2.0F;
      if (weather != null) {
         vg.rect(chipX - 2.0F, chipY - 2.0F, chip + 4.0F, chip + 4.0F, 1.0F, Colors.withAlpha(theme.accent(), 0.14F));
         this.drawWeatherGlyph(vg, chipX + chip / 2.0F, chipY + chip / 2.0F - 1.0F, weather, theme.textMuted(), theme.accent());
      } else if (accent) {
         vg.rect(chipX, chipY, chip, chip, 1.0F, theme.accent());
         vg.checkmark(chipX + 3.5F, chipY + 3.5F, chip - 7.0F, 1.6F, theme.onAccent());
      } else {
         vg.rectOutline(chipX, chipY, chip, chip, 1.0F, 1.0F, Colors.withAlpha(-1, 0.25F));
         vg.cross(chipX + 4.5F, chipY + 4.5F, chip - 9.0F, 1.4F, theme.textMuted());
      }

      float tx = x + textStart;
      if (twoLine) {
         vg.textBold(title, tx, y + h / 2.0F - 7.0F, 12.0F, theme.textPrimary());
         vg.text(subtitle, tx, y + h / 2.0F + 7.5F, 10.5F, theme.textMuted());
      } else {
         vg.textBold(title, tx, y + h / 2.0F, 12.0F, accent ? theme.textPrimary() : theme.textMuted());
      }

      vg.rect(x, y + h - 1.5F, w * remaining, 1.5F, 0.0F, accent ? theme.accent() : Colors.withAlpha(-1, 0.3F));
      vg.restore();
      return h;
   }

   private void drawWeatherGlyph(NVGRenderer vg, float cx, float cy, Weather weather, int color, int boltColor) {
      switch (weather.ordinal()) {
         case 0:
            this.drawCloud(vg, cx, cy - 3.0F, color);

            for(int k = -1; k <= 1; ++k) {
               float dx = cx + (float)k * 4.0F;
               vg.line(dx + 1.0F, cy + 4.0F, dx - 1.0F, cy + 9.0F, 1.6F, color);
            }
            break;
         case 1:
            this.drawCloud(vg, cx, cy - 3.0F, color);
            vg.line(cx + 1.5F, cy + 2.0F, cx - 2.5F, cy + 6.0F, 1.9F, boltColor);
            vg.line(cx - 2.5F, cy + 6.0F, cx + 1.5F, cy + 6.0F, 1.9F, boltColor);
            vg.line(cx + 1.5F, cy + 6.0F, cx - 2.5F, cy + 11.0F, 1.9F, boltColor);
            break;
         case 2:
            vg.circle(cx, cy, 4.5F, boltColor);

            for(int k = 0; k < 8; ++k) {
               double ang = (double)k * Math.PI / (double)4.0F;
               float dxu = (float)Math.cos(ang);
               float dyu = (float)Math.sin(ang);
               vg.line(cx + dxu * 6.5F, cy + dyu * 6.5F, cx + dxu * 9.0F, cy + dyu * 9.0F, 1.6F, color);
            }
      }

   }

   private void drawCloud(NVGRenderer vg, float cx, float cy, int color) {
      vg.rect(cx - 7.0F, cy - 1.0F, 14.0F, 5.0F, 2.5F, color);
      vg.circle(cx - 4.5F, cy + 1.0F, 4.0F, color);
      vg.circle(cx + 4.5F, cy + 1.0F, 4.0F, color);
      vg.circle(cx, cy - 2.5F, 5.0F, color);
   }

   class Toast {
      final String title;
      final String subtitle;
      final Weather weather;
      final boolean accent;
      final long bornNanos = System.nanoTime();
      final Animation slide;

      Toast(String title, String subtitle, Weather weather, boolean accent) {
         this.slide = new Animation(220.0F, 0.0F, Easing.EASE_OUT_CUBIC);
         this.title = title;
         this.subtitle = subtitle;
         this.weather = weather;
         this.accent = accent;
         this.slide.setTarget(1.0F);
      }
   }

   public static enum Weather {
      RAIN,
      THUNDER,
      CLEAR;

   }
}
