package dev.sol.gui.panel;

import dev.sol.gui.ClickGuiState;
import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;

/**
 * A draggable Sol window: solid accent title bar, flat translucent body,
 * small corner radius. Left-drag the title bar to move, click it to
 * collapse / expand.
 */
public abstract class Panel {
   public static final float WIDTH = 172.0F;
   public static final float HEADER_H = 26.0F;
   public static final float RADIUS = 3.0F;
   protected static final float CONTENT_PAD = 3.0F;
   private static final float FADE_ZONE = 10.0F;
   private static final float TITLE_SIZE = 13.5F;
   protected ThemeManager themes;
   protected ClickGuiState.PanelState clickGuiStatePanelState;
   private Animation open;
   private final Animation scroll = new Animation(160.0F, 0.0F);
   private final Animation headerHover = new Animation(120.0F, 0.0F);
   private float maxScroll;
   private boolean headerHovered;

   protected Panel(ThemeManager themes, ClickGuiState.PanelState ps) {
      this.themes = themes;
      this.clickGuiStatePanelState = ps;
      this.open = new Animation(170.0F, ps.collapsed ? 0.0F : 1.0F);
   }

   protected Theme theme() {
      return this.themes.current();
   }

   public float getFloat() {
      return this.clickGuiStatePanelState.floatVal;
   }

   public float getFloat2() {
      return this.clickGuiStatePanelState.floatVal2;
   }

   protected float x() {
      return this.clickGuiStatePanelState.floatVal;
   }

   protected float y() {
      return this.clickGuiStatePanelState.floatVal2;
   }

   public void moveTo(float x, float y) {
      this.clickGuiStatePanelState.floatVal = x;
      this.clickGuiStatePanelState.floatVal2 = y;
   }

   public void toggleCollapsed() {
      this.clickGuiStatePanelState.collapsed = !this.clickGuiStatePanelState.collapsed;
      this.open.setTarget(this.clickGuiStatePanelState.collapsed ? 0.0F : 1.0F);
   }

   protected abstract String title();

   protected int icon() {
      return -1;
   }

   /** Optional small counter shown on the right of the title bar (e.g. "3/12"). */
   protected String badge() {
      return null;
   }

   protected abstract float contentHeight(NVGRenderer var1);

   protected abstract void renderContent(NVGRenderer var1, float var2, float var3, float var4, float var5, float var6);

   protected float maxViewHeight(float uiHeight) {
      return Math.max(60.0F, uiHeight - this.y() - HEADER_H - 16.0F);
   }

   protected float viewHeight(NVGRenderer vg, float uiHeight) {
      return Math.min(this.contentHeight(vg), this.maxViewHeight(uiHeight)) * this.open.value();
   }

   public float totalHeight(NVGRenderer vg, float uiHeight) {
      return HEADER_H + this.viewHeight(vg, uiHeight);
   }

   protected float edgeFade(float rowTop, float rowBottom, float viewTop, float viewBottom) {
      float fadeTop = Math.clamp((rowBottom - viewTop) / FADE_ZONE, 0.0F, 1.0F);
      float fadeBottom = Math.clamp((viewBottom - rowTop) / FADE_ZONE, 0.0F, 1.0F);
      return Math.min(fadeTop, fadeBottom);
   }

   public void render(NVGRenderer vg, float mx, float my, float uiWidth, float uiHeight) {
      this.clickGuiStatePanelState.floatVal = Math.clamp(this.x(), -WIDTH + 40.0F, uiWidth - 40.0F);
      this.clickGuiStatePanelState.floatVal2 = Math.clamp(this.y(), 0.0F, uiHeight - HEADER_H);
      Theme theme = this.theme();
      float x = this.x();
      float y = this.y();
      float viewH = this.viewHeight(vg, uiHeight);
      boolean hasBody = viewH > 0.5F;
      boolean hoveredNow = this.headerHit(mx, my);
      if (hoveredNow && !this.headerHovered) {
         UiSounds.hover();
      }

      this.headerHovered = hoveredNow;
      this.headerHover.setTarget(hoveredNow ? 1.0F : 0.0F);
      float hoverT = this.headerHover.value();

      // soft drop shadow
      vg.glow(x, y, WIDTH, HEADER_H + viewH, RADIUS, 6.0F, Colors.withAlpha(0xFF000000, 0.30F));

      // body
      if (hasBody) {
         vg.rectVaryingGradient(x, y + HEADER_H, WIDTH, viewH, 0.0F, 0.0F, RADIUS, RADIUS, theme.background(), theme.backgroundTo());
      }

      // title bar: solid accent, slightly lifted on hover
      float bottomRadius = hasBody ? 0.0F : RADIUS;
      int bar = Colors.lerp(theme.accent(), theme.accentBright(), 0.25F * hoverT);
      vg.rectVaryingGradient(x, y, WIDTH, HEADER_H, RADIUS, RADIUS, bottomRadius, bottomRadius, bar, Colors.lerp(bar, theme.accentDark(), 0.18F));
      int onAccent = theme.onAccent();
      float cy = y + HEADER_H / 2.0F;
      int iconHandle = this.icon();
      if (iconHandle > 0) {
         vg.image(iconHandle, x + 7.0F, cy - 7.0F, 14.0F, 14.0F, onAccent);
      }

      String title = this.title();
      float tw = vg.textWidth(title, TITLE_SIZE);
      vg.text(title, x + (WIDTH - tw) / 2.0F, cy, TITLE_SIZE, onAccent);

      // right side: badge or +/- collapse glyph
      float gx = x + WIDTH - 12.0F;
      int glyph = Colors.withAlpha(onAccent, 0.85F);
      vg.line(gx - 3.5F, cy, gx + 3.5F, cy, 1.5F, glyph);
      float vert = 1.0F - this.open.value();
      if (vert > 0.01F) {
         vg.line(gx, cy - 3.5F * vert, gx, cy + 3.5F * vert, 1.5F, glyph);
      }

      String badge = this.badge();
      if (badge != null && !badge.isEmpty()) {
         float bw = vg.textWidth(badge, 10.5F);
         vg.text(badge, gx - 10.0F - bw, cy, 10.5F, Colors.withAlpha(onAccent, 0.7F));
      }

      if (hasBody) {
         float viewTop = y + HEADER_H;
         float viewBottom = viewTop + viewH;
         this.maxScroll = Math.max(0.0F, this.contentHeight(vg) - this.maxViewHeight(uiHeight));
         this.scroll.setTarget(Math.clamp(this.scroll.getTarget(), 0.0F, this.maxScroll));
         vg.save();
         vg.scissor(x, viewTop, WIDTH, viewH);
         this.renderContent(vg, viewTop + CONTENT_PAD - this.scroll.value(), mx, my, viewTop, viewBottom);
         vg.restore();
         if (this.maxScroll > 0.5F) {
            float trackH = viewH - 6.0F;
            float thumbH = Math.max(14.0F, trackH * (viewH / (viewH + this.maxScroll)));
            float thumbY = viewTop + 3.0F + (trackH - thumbH) * (this.scroll.value() / this.maxScroll);
            vg.rect(x + WIDTH - 3.0F, thumbY, 2.0F, thumbH, 1.0F, Colors.withAlpha(theme.accent(), 0.55F));
         }
      }
   }

   public boolean headerHit(float mx, float my) {
      return mx >= this.x() && mx <= this.x() + WIDTH && my >= this.y() && my <= this.y() + HEADER_H;
   }

   public boolean bodyHit(NVGRenderer vg, float mx, float my, float uiHeight) {
      float viewH = this.viewHeight(vg, uiHeight);
      return mx >= this.x() && mx <= this.x() + WIDTH && my >= this.y() + HEADER_H && my <= this.y() + HEADER_H + viewH;
   }

   public void onScroll(double amount) {
      if (!(this.maxScroll <= 0.0F)) {
         this.scroll.setTarget(Math.clamp(this.scroll.getTarget() - (float)amount * 30.0F, 0.0F, this.maxScroll));
      }
   }

   public boolean mouseClicked(float mx, float my, int button) {
      return false;
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
