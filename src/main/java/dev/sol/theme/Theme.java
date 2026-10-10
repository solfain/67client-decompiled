package dev.sol.theme;

import dev.sol.util.Colors;

/**
 * Sol Client palette.
 *
 * Flat, neutral greys with a single accent colour (Meteor-style): no tinted
 * gradients, no heavy glows. Every GUI surface pulls its colours from here so
 * swapping the accent re-skins the whole client.
 */
public class Theme {
   // Base greys (ARGB, fully opaque — alpha is applied per surface below)
   private static final int BASE_0 = 0xFF0E0E10; // deepest (window body)
   private static final int BASE_1 = 0xFF151518; // header / inputs
   private static final int BASE_2 = 0xFF1C1C20; // raised rows / chips
   private static final int BASE_3 = 0xFF2A2A30; // outlines / tracks

   private String name;
   private int accent;
   private boolean custom;

   public Theme(String name, int accent, boolean custom) {
      this.name = name;
      this.accent = accent;
      this.custom = custom;
   }

   public String getName() {
      return this.name;
   }

   public int accent() {
      return this.accent;
   }

   public void setAccent(int accent) {
      this.accent = accent;
   }

   public boolean isCustom() {
      return this.custom;
   }

   public int accentBright() {
      return Colors.lighten(this.accent(), 0.25F);
   }

   public int accentDark() {
      return Colors.darken(this.accent(), 0.35F);
   }

   public int accentHover() {
      return Colors.withAlpha(this.accent(), 0.30F);
   }

   /** Text drawn on top of a solid accent fill (headers, active tabs). */
   public int onAccent() {
      int a = this.accent();
      float lum = (0.299F * (float)Colors.red(a) + 0.587F * (float)Colors.green(a) + 0.114F * (float)Colors.blue(a)) / 255.0F;
      return lum > 0.62F ? 0xFF101012 : 0xFFFFFFFF;
   }

   // ---- surfaces -------------------------------------------------------

   public int background() {
      return Colors.withAlpha(BASE_0, 0.86F);
   }

   public int backgroundTo() {
      return Colors.withAlpha(BASE_0, 0.86F);
   }

   public int headerTop() {
      return Colors.withAlpha(BASE_1, 0.96F);
   }

   public int headerBottom() {
      return Colors.withAlpha(BASE_1, 0.96F);
   }

   public int surface() {
      return Colors.withAlpha(BASE_2, 0.92F);
   }

   public int outline() {
      return Colors.withAlpha(BASE_3, 0.90F);
   }

   public int track() {
      return Colors.withAlpha(BASE_3, 0.95F);
   }

   public int rowHover() {
      return Colors.withAlpha(0xFFFFFFFF, 0.05F);
   }

   public int moduleActiveFill() {
      return Colors.withAlpha(this.accent(), 0.16F);
   }

   // ---- text -----------------------------------------------------------

   public int textPrimary() {
      return 0xFFEDEDED;
   }

   public int textMuted() {
      return 0xFFA3A3AA;
   }

   public int textDisabled() {
      return 0xFF66666D;
   }

   public int statusEnabled() {
      return this.accent();
   }

   public int statusDisabled() {
      return 0xFF3A3A40;
   }
}
