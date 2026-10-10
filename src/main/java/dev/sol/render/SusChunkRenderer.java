package dev.sol.render;

import dev.sol.SolClient;
import dev.sol.module.Modules;
import dev.sol.suschunk.SusChunkScanner;
import dev.sol.util.Colors;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597;

public final class SusChunkRenderer {
   private static final float FADE_IN_SECONDS = 0.3F;
   private static final float FADE_OUT_SECONDS = 0.45F;
   private static final float MOVE_RATE = 7.0F;
   private static final Map<Long, ChunkFade> chunkFades = new HashMap();
   private static final Map<Long, ZoneFade> zoneFades = new HashMap();
   private static long lastFrameNanos;

   private SusChunkRenderer() {
   }

   public static void reset() {
      chunkFades.clear();
      zoneFades.clear();
   }

   public static String debugState() {
      StringBuilder sb = new StringBuilder();

      for(Map.Entry<Long, ChunkFade> entry : chunkFades.entrySet()) {
         ChunkFade fade = (ChunkFade)entry.getValue();
         sb.append(new class_1923((Long)entry.getKey())).append("{a=").append(fade.alpha).append(",t=").append(fade.tier).append(",f=").append(fade.flagged).append("} ");
      }

      for(Map.Entry<Long, ZoneFade> entry : zoneFades.entrySet()) {
         ZoneFade zone = (ZoneFade)entry.getValue();
         sb.append("zone").append(new class_1923((Long)entry.getKey())).append("{a=").append(zone.alpha).append(",s=").append(zone.size).append(",f=").append(zone.flagged).append("} ");
      }

      return sb.toString();
   }

   public static void render(class_4597.class_4598 bufferSource, class_4587 poseStack, class_243 camera, Modules.SusChunkFinderModule module) {
      SusChunkScanner scanner = module.scanner;
      double y = (Double)module.renderY.get();
      int accent = SolClient.themes().current().accent();
      float fillAlpha = module.fillOpacity.getFloat() / 255.0F;
      float outlineAlpha = module.outlineOpacity.getFloat() / 255.0F;
      boolean outline = (Boolean)module.outline.get();
      boolean smart = (Boolean)module.smartMode.get();
      int threshold = scanner.threshold();
      updateTargets(scanner, smart, threshold);
      float dt = frameDelta();
      float move = 1.0F - (float)Math.exp((double)(-dt * 7.0F));
      Iterator<Map.Entry<Long, ChunkFade>> chunks = chunkFades.entrySet().iterator();

      while(chunks.hasNext()) {
         Map.Entry<Long, ChunkFade> entry = (Map.Entry)chunks.next();
         ChunkFade fade = (ChunkFade)entry.getValue();
         fade.alpha += fade.flagged ? dt / 0.3F : -dt / 0.45F;
         fade.alpha = Math.clamp(fade.alpha, 0.0F, 1.0F);
         if (!fade.flagged && fade.alpha <= 0.0F) {
            chunks.remove();
         } else {
            double x0 = (double)class_1923.method_8325((Long)entry.getKey()) * (double)16.0F;
            double z0 = (double)class_1923.method_8332((Long)entry.getKey()) * (double)16.0F;
            drawQuad(bufferSource, poseStack, camera, x0, z0, x0 + (double)16.0F, z0 + (double)16.0F, y, accent, fillAlpha * fade.tier * fade.alpha, outline ? outlineAlpha * fade.alpha : 0.0F);
         }
      }

      Iterator<Map.Entry<Long, ZoneFade>> zones = zoneFades.entrySet().iterator();

      while(zones.hasNext()) {
         ZoneFade zone = (ZoneFade)((Map.Entry)zones.next()).getValue();
         zone.alpha += zone.flagged ? dt / 0.3F : -dt / 0.45F;
         zone.alpha = Math.clamp(zone.alpha, 0.0F, 1.0F);
         if (!zone.flagged && zone.alpha <= 0.0F) {
            zones.remove();
         } else {
            zone.centerX += (zone.targetX - zone.centerX) * (double)move;
            zone.centerZ += (zone.targetZ - zone.centerZ) * (double)move;
            zone.size += (zone.targetSize - zone.size) * move;
            double half = (double)zone.size / (double)2.0F;
            drawQuad(bufferSource, poseStack, camera, zone.centerX - half, zone.centerZ - half, zone.centerX + half, zone.centerZ + half, y, accent, fillAlpha * zone.tier * zone.alpha, outline ? outlineAlpha * zone.alpha : 0.0F);
            if ((Boolean)module.centroidMarker.get()) {
               FlatOverlay.marker(bufferSource, poseStack, camera, zone.centerX, zone.centerZ, y + 0.05, (double)2.0F, Colors.withAlpha(accent, 0.95F * zone.alpha));
            }
         }
      }

      FlatOverlay.flush(bufferSource);
   }

   private static void drawQuad(class_4597.class_4598 bufferSource, class_4587 poseStack, class_243 camera, double x0, double z0, double x1, double z1, double y, int accent, float fill, float border) {
      FlatOverlay.fillQuad(bufferSource, poseStack, camera, x0, z0, x1, z1, y, Colors.withAlpha(accent, fill));
      if (border > 0.004F) {
         int color = Colors.withAlpha(accent, border);
         FlatOverlay.edge(bufferSource, poseStack, camera, x0, z0, x1, z0, y, color, 2.5F);
         FlatOverlay.edge(bufferSource, poseStack, camera, x0, z1, x1, z1, y, color, 2.5F);
         FlatOverlay.edge(bufferSource, poseStack, camera, x0, z0, x0, z1, y, color, 2.5F);
         FlatOverlay.edge(bufferSource, poseStack, camera, x1, z0, x1, z1, y, color, 2.5F);
      }

   }

   private static void updateTargets(SusChunkScanner scanner, boolean smart, int threshold) {
      for(ChunkFade fade : chunkFades.values()) {
         fade.flagged = false;
      }

      for(ZoneFade zone : zoneFades.values()) {
         zone.flagged = false;
      }

      if (smart) {
         for(SusChunkScanner.Zone zone : scanner.zones()) {
            if (zone.members().size() == 1) {
               long key = (Long)zone.members().iterator().next();
               ChunkFade fade = (ChunkFade)chunkFades.computeIfAbsent(key, (k) -> new ChunkFade());
               fade.flagged = true;
               fade.tier = confidence(zone.maxScore(), threshold);
            } else {
               long key = Long.MAX_VALUE;

               for(long member : zone.members()) {
                  key = Math.min(key, member);
               }

               ZoneFade fade = (ZoneFade)zoneFades.get(key);
               if (fade == null) {
                  fade = new ZoneFade(zone.centroidX(), zone.centroidZ());
                  zoneFades.put(key, fade);
               }

               fade.flagged = true;
               fade.targetX = zone.centroidX();
               fade.targetZ = zone.centroidZ();
               fade.targetSize = Math.min(48.0F, 16.0F + (float)(zone.members().size() - 1) * 8.0F);
               fade.tier = confidence(zone.maxScore(), threshold);
            }
         }
      } else {
         for(SusChunkScanner.Flag flag : scanner.flags()) {
            ChunkFade fade = (ChunkFade)chunkFades.computeIfAbsent(flag.chunkKey(), (k) -> new ChunkFade());
            fade.flagged = true;
            fade.tier = 1.0F;
         }
      }

   }

   private static float confidence(double score, int threshold) {
      if (threshold <= 0) {
         return 1.0F;
      } else {
         float over = (float)((score - (double)threshold) / ((double)threshold * (double)2.0F));
         return 0.55F + 0.45F * Math.clamp(over, 0.0F, 1.0F);
      }
   }

   private static float frameDelta() {
      long now = System.nanoTime();
      float dt = lastFrameNanos == 0L ? 0.016F : (float)(now - lastFrameNanos) / 1.0E9F;
      lastFrameNanos = now;
      return Math.min(dt, 0.1F);
   }

   static final class ChunkFade {
      float alpha;
      float tier = 1.0F;
      boolean flagged;

      private ChunkFade() {
      }
   }

   static final class ZoneFade {
      double centerX;
      double centerZ;
      double targetX;
      double targetZ;
      float size = 16.0F;
      float targetSize = 16.0F;
      float alpha;
      float tier = 1.0F;
      boolean flagged;

      ZoneFade(double x, double z) {
         this.centerX = this.targetX = x;
         this.centerZ = this.targetZ = z;
      }
   }
}
