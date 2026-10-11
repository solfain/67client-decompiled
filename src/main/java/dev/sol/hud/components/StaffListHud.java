package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
import dev.sol.module.impl.StaffListModule;
import dev.sol.render.anim.Animation;
import dev.sol.render.anim.Easing;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.staff.StaffEntry;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class StaffListHud extends HudComponent {
   private static final float PAD = 8.0F;
   private static final float HEADER_H = 15.0F;
   private static final float GAP_HEADER = 5.0F;
   private static final float ROW_H = 18.0F;
   private static final float EMPTY_H = 16.0F;
   private static final float OVERFLOW_H = 13.0F;
   private static final float MARK_W = 15.0F;
   private static final float PING_W = 11.0F;
   private static final float FONT_TITLE = 9.0F;
   private static final float FONT_NAME = 12.5F;
   private static final float FONT_RANK = 9.0F;
   private static final float FONT_EMPTY = 11.0F;
   private static final float MIN_CONTENT_W = 92.0F;
   private static final float MAX_NAME_W = 128.0F;
   private static final int GREEN = -11870592;
   private static final int YELLOW = -340971;
   private static final int RED = -495247;
   private StaffListModule module;
   private ThemeManager themes;
   private final Map<String, RowAnim> rows;

   public StaffListHud(StaffListModule module, ThemeManager themes) {
      super("staffList", 0.008F, 0.27F, module::isEnabled);
      this.rows = new LinkedHashMap();
      this.module = module;
      this.themes = themes;
   }

   private int shownCount(List<StaffEntry> staff) {
      return Math.min(staff.size(), Math.max(1, this.module.maxRows.getInt()));
   }

   private List<RowAnim> layoutRows(List<StaffEntry> staff) {
      int shown = this.shownCount(staff);
      Set<String> visible = new HashSet();

      for(int i = 0; i < shown && i < staff.size(); ++i) {
         StaffEntry e = (StaffEntry)staff.get(i);
         visible.add(e.name());
         RowAnim r = (RowAnim)this.rows.get(e.name());
         if (r == null) {
            r = new RowAnim(e);
            this.rows.put(e.name(), r);
         } else {
            r.entry = e;
         }

         r.anim.setTarget(1.0F);
      }

      List<RowAnim> out = new ArrayList();

      for(int i = 0; i < shown && i < staff.size(); ++i) {
         out.add((RowAnim)this.rows.get(((StaffEntry)staff.get(i)).name()));
      }

      Iterator<RowAnim> it = this.rows.values().iterator();

      while(it.hasNext()) {
         RowAnim r = (RowAnim)it.next();
         if (!visible.contains(r.entry.name())) {
            r.anim.setTarget(0.0F);
            if (r.anim.value() <= 0.01F) {
               it.remove();
            } else {
               out.add(r);
            }
         }
      }

      return out;
   }

   private float rowWidth(NVGRenderer vg, StaffEntry e) {
      float w = 15.0F + Math.min(vg.textWidth(e.name(), 12.5F), 128.0F);
      if ((Boolean)this.module.showRank.get() && !e.rankLabel().isEmpty()) {
         w += 6.0F + vg.textWidth(e.rankLabel(), 9.0F);
      }

      if ((Boolean)this.module.showPing.get()) {
         w += 19.0F;
      }

      return w;
   }

   public float measureWidth(NVGRenderer vg) {
      List<StaffEntry> staff = this.module.staff();
      float content = 92.0F;
      String count = Integer.toString(staff.size());
      content = Math.max(content, HudStyle.headerWidth(vg, "Staff", count) - 16.0F);

      for(RowAnim r : this.layoutRows(staff)) {
         content = Math.max(content, this.rowWidth(vg, r.entry));
      }

      if (staff.isEmpty()) {
         content = Math.max(content, 14.0F + vg.textWidth("No staff online", 11.0F));
      }

      return 16.0F + content;
   }

   public float measureHeight(NVGRenderer vg) {
      List<StaffEntry> staff = this.module.staff();
      float listH = 0.0F;

      for(RowAnim r : this.layoutRows(staff)) {
         listH += 18.0F * r.anim.value();
      }

      if (staff.isEmpty() && listH < 0.5F) {
         listH = 16.0F;
      }

      float h = HudStyle.RULE + HudStyle.HEADER_H + 4.0F + listH + 6.0F;
      if (staff.size() > this.shownCount(staff)) {
         h += 13.0F;
      }

      return h;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      List<StaffEntry> staff = this.module.staff();
      HudStyle.panel(vg, theme, x, y, w, h);
      float rowY = HudStyle.header(vg, theme, "Staff", Integer.toString(staff.size()), x, y, w);
      if (staff.isEmpty()) {
         float cy = rowY + 8.0F;
         vg.checkmark(x + 8.0F, cy - 4.0F, 8.0F, 1.7F, -11870592);
         vg.text("No staff online", x + 8.0F + 14.0F, cy, 11.0F, theme.textMuted());
      } else {
         for(RowAnim r : this.layoutRows(staff)) {
            float t = Math.clamp(r.anim.value(), 0.0F, 1.0F);
            if (!(t <= 0.01F)) {
               this.drawRow(vg, theme, r.entry, x, rowY, w, t);
               rowY += 18.0F * t;
            }
         }

         int overflow = staff.size() - this.shownCount(staff);
         if (overflow > 0) {
            vg.text("+" + overflow + " more", x + 8.0F + 15.0F, rowY + 6.5F, 9.0F, theme.textDisabled());
         }
      }

   }

   private void drawRow(NVGRenderer vg, Theme theme, StaffEntry e, float x, float rowY, float w, float t) {
      float cy = rowY + 9.0F;
      int color = e.hasColor() ? e.color() : theme.accent();
      vg.save();
      vg.alpha(t);
      vg.translate((1.0F - t) * -10.0F, 0.0F);
      this.drawStar(vg, x + 8.0F + 7.5F - 2.0F, cy, 4.2F, color, e.vanished());
      float nameX = x + 8.0F + 15.0F;
      int nameColor = e.vanished() ? theme.textMuted() : theme.textPrimary();
      float adv = vg.textTruncated(e.name(), nameX, cy, 12.5F, nameColor, 128.0F);
      if ((Boolean)this.module.showRank.get() && !e.rankLabel().isEmpty()) {
         int rankColor = e.hasColor() ? Colors.lighten(color, 0.25F) : theme.accent();
         vg.text(e.rankLabel(), nameX + adv + 6.0F, cy + 0.5F, 9.0F, Colors.withAlpha(rankColor, e.vanished() ? 0.6F : 0.95F));
      }

      if ((Boolean)this.module.showPing.get()) {
         this.drawPingBars(vg, x + w - 8.0F - 11.0F, cy, e.latency(), theme);
      }

      vg.restore();
   }

   private void drawStar(NVGRenderer vg, float cx, float cy, float r, int color, boolean vanished) {
      vg.save();
      vg.translate(cx, cy);
      vg.rotate(((float)Math.PI / 4F));
      if (vanished) {
         vg.rectOutline(-r * 0.55F, -r * 0.55F, r * 1.1F, r * 1.1F, 0.0F, 1.2F, Colors.withAlpha(color, 0.55F));
      } else {
         vg.rect(-r * 0.6F, -r * 0.6F, r * 1.2F, r * 1.2F, 0.0F, color);
      }

      vg.restore();
   }

   private void drawPingBars(NVGRenderer vg, float x, float cy, int latency, Theme theme) {
      int bars = latency < 0 ? 0 : (latency <= 80 ? 4 : (latency <= 150 ? 3 : (latency <= 300 ? 2 : (latency <= 600 ? 1 : 0))));
      int col = bars >= 3 ? -11870592 : (bars == 2 ? -340971 : -495247);

      for(int i = 0; i < 4; ++i) {
         float bh = 2.0F + (float)i * 2.0F;
         float bx = x + (float)i * 3.0F;
         float by = cy + 4.0F - bh;
         int c = i < bars ? col : Colors.withAlpha(theme.textDisabled(), 0.45F);
         vg.rect(bx, by, 2.0F, bh, 0.0F, c);
      }

   }

   final class RowAnim {
      StaffEntry entry;
      final Animation anim;

      RowAnim(StaffEntry entry) {
         this.anim = new Animation(220.0F, 0.0F, Easing.EASE_OUT_CUBIC);
         this.entry = entry;
      }
   }
}
