package dev.sol.render;

import dev.sol.hud.HudStyle;
import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import dev.sol.module.Modules;
import dev.sol.module.impl.NameProtectModule;
import dev.sol.module.impl.NameTagsModule;
import dev.sol.module.impl.SpawnerNametagsModule;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.util.Colors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_8961;
import org.joml.Matrix3x2fStack;

public final class WorldNametagRenderer {
   private static final int MAX_TAGS = 80;

   private WorldNametagRenderer() {
   }

   public static void render(NVGRenderer vg) {
      if (WorldProjection.isValid()) {
         ModuleManager modules = SolClient.modules();
         if (modules != null) {
            class_310 mc = class_310.method_1551();
            class_638 level = mc.field_1687;
            class_746 self = mc.field_1724;
            if (level != null && self != null) {
               NameTagsModule names = modules.nameTags;
               SpawnerNametagsModule spawners = modules.spawnerNametags;
               boolean doNames = names != null && names.isEnabled();
               boolean doSpawners = spawners != null && spawners.isEnabled() && (Boolean)spawners.nametag.get();
               if (doNames || doSpawners) {
                  float pt = WorldProjection.partialTick();
                  Theme theme = SolClient.themes().current();
                  Modules.HudModule hud = modules.hud;
                  int accent = hud != null && !(Boolean)hud.themeSync.get() ? (Integer)hud.listColor.get() : theme.accent();
                  List<Tag> tags = new ArrayList();
                  if (doNames) {
                     double range = (Double)names.range.get();
                     double rangeSq = range * range;
                     float nameScale = names.scale.getFloat();
                     float nameOpacity = (float)((Double)names.opacity.get() / (double)100.0F);
                     if ((Boolean)names.players.get()) {
                        NameProtectModule protect = modules.nameProtect;
                        boolean protecting = protect != null && protect.isEnabled();
                        boolean showSelf = (Boolean)names.self.get() && !mc.field_1690.method_31044().method_31034();

                        for(class_742 player : level.method_18456()) {
                           boolean isSelf = player == self;
                           if ((!isSelf || showSelf) && !player.method_7325() && player.method_5805()) {
                              double distSq = self.method_5858(player);
                              if (isSelf || !(distSq > rangeSq)) {
                                 String shown = player.method_7334().name();
                                 if (protecting) {
                                    String replaced = protect.replacementForDisplay(shown);
                                    if (replaced != null) {
                                       shown = replaced;
                                    }
                                 }

                                 float healthFrac = -1.0F;
                                 if ((Boolean)names.health.get()) {
                                    float max = player.method_6063();
                                    if (max > 0.0F) {
                                       healthFrac = class_3532.method_15363(player.method_6032() / max, 0.0F, 1.0F);
                                    }
                                 }

                                 String suffix = !isSelf && (Boolean)names.distance.get() ? (int)Math.sqrt(distSq) + "m" : null;
                                 if (!shown.isEmpty() || suffix != null || !(healthFrac < 0.0F)) {
                                    tags.add(entityTag(player, pt, Math.sqrt(distSq), shown, suffix, accent, healthFrac, nameScale, nameOpacity));
                                 }
                              }
                           }
                        }
                     }

                     if ((Boolean)names.items.get()) {
                        for(class_1297 entity : level.method_18112()) {
                           if (entity instanceof class_1542) {
                              class_1542 item = (class_1542)entity;
                              if (item.method_5805()) {
                                 double distSq = self.method_5858(item);
                                 if (!(distSq > rangeSq)) {
                                    class_1799 stack = item.method_6983();
                                    if (!stack.method_7960()) {
                                       String suffix = itemSuffix(stack.method_7947(), (Boolean)names.itemAmount.get(), (Boolean)names.distance.get() ? (int)Math.sqrt(distSq) + "m" : null);
                                       tags.add(entityTag(item, pt, Math.sqrt(distSq), stack.method_7964().getString(), suffix, accent, -1.0F, nameScale, nameOpacity));
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (doSpawners) {
                     boolean showDist = (Boolean)spawners.distance.get();
                     float spawnerOpacity = (float)((Double)spawners.opacity.get() / (double)100.0F);

                     for(class_2338 pos : spawners.scan.get()) {
                        double cx = (double)pos.method_10263() + (double)0.5F;
                        double cy = (double)pos.method_10264() + 1.35;
                        double cz = (double)pos.method_10260() + (double)0.5F;
                        double distSq = self.method_5649(cx, (double)pos.method_10264() + (double)0.5F, cz);
                        String suffix = showDist ? (int)Math.sqrt(distSq) + "m" : null;
                        tags.add(new Tag(Math.sqrt(distSq), cx, cy, cz, spawnerName(level, pos), suffix, SolClient.themes().current().accent() | -16777216, -1.0F, 1.0F, spawnerOpacity));
                     }
                  }

                  if (!tags.isEmpty()) {
                     tags.sort(Comparator.comparingDouble(Tag::dist));
                     int count = Math.min(tags.size(), 80);

                     for(int i = count - 1; i >= 0; --i) {
                        Tag tag = (Tag)tags.get(i);
                        float[] screen = WorldProjection.project(tag.doubleVal, tag.doubleVal2, tag.doubleVal3);
                        if (screen != null) {
                           drawTag(vg, theme, screen[0], screen[1], tag);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private static Tag entityTag(class_1297 e, float pt, double dist, String name, String suffix, int accent, float healthFrac, float scale, float opacity) {
      double x = class_3532.method_16436((double)pt, e.field_6038, e.method_23317());
      double y = class_3532.method_16436((double)pt, e.field_5971, e.method_23318()) + (double)e.method_17682() + (double)0.5F;
      double z = class_3532.method_16436((double)pt, e.field_5989, e.method_23321());
      return new Tag(dist, x, y, z, name, suffix, accent, healthFrac, scale, opacity);
   }

   private static String itemSuffix(int amount, boolean showAmount, String distance) {
      StringBuilder sb = new StringBuilder();
      if (showAmount && amount > 1) {
         sb.append('x').append(amount);
      }

      if (distance != null) {
         if (sb.length() > 0) {
            sb.append("  ");
         }

         sb.append(distance);
      }

      return sb.length() == 0 ? null : sb.toString();
   }

   private static String spawnerName(class_638 level, class_2338 pos) {
      try {
         class_2586 be = level.method_8321(pos);
         if (be instanceof class_2636 spawner) {
            class_1297 display = spawner.method_11390().method_8283(level, pos);
            return display != null ? display.method_5864().method_5897().getString() + " Spawner" : "Spawner";
         }

         if (be instanceof class_8961 trial) {
            class_1297 display = trial.method_55150().method_55174().method_55190(trial.method_55150(), level, trial.method_55151());
            return display != null ? "Trial: " + display.method_5864().method_5897().getString() : "Trial Spawner";
         }
      } catch (Exception var5) {
      }

      return "Spawner";
   }

   private static float pillHeight(float s, boolean hasHealth) {
      float textRowH = 12.5F * s + 3.5F * s * 2.0F;
      return textRowH + (hasHealth ? 3.0F * s + 3.5F * s : 0.0F);
   }

   public static void renderEquipment(class_332 gg) {
      if (WorldProjection.isValid()) {
         ModuleManager modules = SolClient.modules();
         if (modules != null) {
            NameTagsModule names = modules.nameTags;
            if (names != null && names.isEnabled() && (Boolean)names.players.get()) {
               boolean showArmor = (Boolean)names.armor.get();
               boolean showHand = (Boolean)names.heldItem.get();
               if (showArmor || showHand) {
                  class_310 mc = class_310.method_1551();
                  if (mc.field_1755 == null && !mc.field_1690.field_1842) {
                     class_638 level = mc.field_1687;
                     class_746 self = mc.field_1724;
                     if (level != null && self != null) {
                        float pt = WorldProjection.partialTick();
                        double range = (Double)names.range.get();
                        double rangeSq = range * range;
                        float s = names.scale.getFloat();
                        float pillH = pillHeight(s, (Boolean)names.health.get());
                        float uiScale = OverlayRenderer.uiScale();
                        double guiScale = (double)mc.method_22683().method_4495();
                        boolean showSelf = (Boolean)names.self.get() && !mc.field_1690.method_31044().method_31034();
                        List<class_1799> row = new ArrayList();

                        for(class_742 player : level.method_18456()) {
                           boolean isSelf = player == self;
                           if ((!isSelf || showSelf) && !player.method_7325() && player.method_5805() && (isSelf || !(self.method_5858(player) > rangeSq))) {
                              row.clear();
                              if (showArmor) {
                                 addItem(row, player.method_6118(class_1304.field_6169));
                                 addItem(row, player.method_6118(class_1304.field_6174));
                                 addItem(row, player.method_6118(class_1304.field_6172));
                                 addItem(row, player.method_6118(class_1304.field_6166));
                              }

                              if (showHand) {
                                 addItem(row, player.method_6047());
                                 addItem(row, player.method_6118(class_1304.field_6171));
                              }

                              if (!row.isEmpty()) {
                                 double wx = class_3532.method_16436((double)pt, player.field_6038, player.method_23317());
                                 double wy = class_3532.method_16436((double)pt, player.field_5971, player.method_23318()) + (double)player.method_17682() + (double)0.5F;
                                 double wz = class_3532.method_16436((double)pt, player.field_5989, player.method_23321());
                                 float[] px = WorldProjection.projectRaw(wx, wy, wz);
                                 if (px != null) {
                                    float guiX = (float)((double)px[0] / guiScale);
                                    float rowBottomY = (float)((double)(px[1] - pillH * uiScale) / guiScale) - 3.0F;
                                    float icon = 11.0F * s;
                                    float step = icon + 1.5F;
                                    float totalW = (float)row.size() * icon + (float)(row.size() - 1) * 1.5F;
                                    float startX = guiX - totalW / 2.0F;
                                    float rowTopY = rowBottomY - icon;
                                    Matrix3x2fStack pose = gg.method_51448();

                                    for(int i = 0; i < row.size(); ++i) {
                                       class_1799 stack = (class_1799)row.get(i);
                                       pose.pushMatrix();
                                       pose.translate(startX + (float)i * step, rowTopY);
                                       pose.scale(icon / 16.0F, icon / 16.0F);
                                       gg.method_51423(player, stack, 0, 0, 0);
                                       gg.method_51431(mc.field_1772, stack, 0, 0);
                                       pose.popMatrix();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private static void addItem(List<class_1799> list, class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         list.add(stack);
      }

   }

   private static void drawTag(NVGRenderer vg, Theme theme, float uiX, float uiY, Tag tag) {
      float s = tag.scale;
      int accentBright = Colors.lighten(tag.accent, 0.35F);
      float fontMain = 12.5F * s;
      float fontSub = 10.0F * s;
      float padX = 6.0F * s;
      float padY = 3.5F * s;
      float gap = 5.0F * s;
      boolean hasHealth = tag.healthFrac >= 0.0F;
      float barH = 3.0F * s;
      float nameW = vg.textWidthBold(tag.name, fontMain);
      float suffixW = tag.suffix != null ? gap + vg.textWidth(tag.suffix, fontSub) : 0.0F;
      float w = nameW + suffixW + padX * 2.0F;
      float textRowH = fontMain + padY * 2.0F;
      float h = textRowH + (hasHealth ? barH + padY : 0.0F);
      float x = uiX - w / 2.0F;
      float y = uiY - h;
      float radius = Math.min(2.0F * s, h / 2.0F);
      boolean fade = tag.opacity < 0.999F;
      if (fade) {
         vg.save();
         vg.alpha(tag.opacity);
      }

      vg.rect(x, y, w, h, radius, HudStyle.panelColor());
      vg.rect(x, y, w, 1.5F * s, 0.0F, tag.accent);
      float cy = y + padY + fontMain / 2.0F;
      float tx = x + padX;
      tx += vg.textBold(tag.name, tx, cy, fontMain, theme.textPrimary());
      if (tag.suffix != null) {
         tx += gap;
         vg.text(tag.suffix, tx, cy, fontSub, theme.textMuted());
      }

      if (hasHealth) {
         float barY = y + textRowH;
         float barX = x + padX;
         float barW = w - padX * 2.0F;
         vg.rect(barX, barY, barW, barH, 0.0F, Colors.withAlpha(-1, 0.10F));
         int hpColor = Colors.lerp(-2080450, -11671924, tag.healthFrac);
         vg.rect(barX, barY, barW * tag.healthFrac, barH, 0.0F, hpColor);
      }

      if (fade) {
         vg.restore();
      }

   }

   static final class Tag {
      private double dist;
      private double doubleVal;
      private double doubleVal2;
      private double doubleVal3;
      private String name;
      private String suffix;
      private int accent;
      private float healthFrac;
      private float scale;
      private float opacity;

      private Tag(double dist, double wx, double wy, double wz, String name, String suffix, int accent, float healthFrac, float scale, float opacity) {
         this.dist = dist;
         this.doubleVal = wx;
         this.doubleVal2 = wy;
         this.doubleVal3 = wz;
         this.name = name;
         this.suffix = suffix;
         this.accent = accent;
         this.healthFrac = healthFrac;
         this.scale = scale;
         this.opacity = opacity;
      }

      public double dist() {
         return this.dist;
      }

      public double getDouble() {
         return this.doubleVal;
      }

      public double getDouble2() {
         return this.doubleVal2;
      }

      public double getDouble3() {
         return this.doubleVal3;
      }

      public String name() {
         return this.name;
      }

      public String suffix() {
         return this.suffix;
      }

      public int accent() {
         return this.accent;
      }

      public float healthFrac() {
         return this.healthFrac;
      }

      public float scale() {
         return this.scale;
      }

      public float opacity() {
         return this.opacity;
      }
   }
}
