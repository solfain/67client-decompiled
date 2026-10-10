package dev.sol.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.sol.SolClient;
import dev.sol.module.impl.CustomGlintModule;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1011;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_918;
import net.minecraft.class_9848;

public final class GlintTextureTinter {
   private static final class_2960[] TEXTURES;
   private static final class_1011[] originals;
   private static final class_1011[] scratch;
   private static final GpuTexture[] lastUploaded;
   private static final Map<String, class_1011> customCache;
   private static boolean loaded;
   private static boolean written;
   private static String lastKey;

   private GlintTextureTinter() {
   }

   public static void tick() {
      CustomGlintModule module = SolClient.modules() == null ? null : SolClient.modules().customGlint;
      if (module != null) {
         if (module.isActive()) {
            if (module.usesTexture()) {
               applyTexture(module.textureName(), module.strengthUnit());
            } else {
               applyTint(module.glintColor());
            }
         } else if (written) {
            restore();
         }
      }

   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;
         class_310 mc = class_310.method_1551();

         for(int i = 0; i < TEXTURES.length; ++i) {
            Optional<class_3298> res = mc.method_1478().method_14486(TEXTURES[i]);
            if (!res.isEmpty()) {
               try {
                  InputStream in = ((class_3298)res.get()).method_14482();

                  try {
                     originals[i] = class_1011.method_4309(in);
                  } catch (Throwable var7) {
                     if (in != null) {
                        try {
                           in.close();
                        } catch (Throwable var6) {
                           var7.addSuppressed(var6);
                        }
                     }

                     throw var7;
                  }

                  if (in != null) {
                     in.close();
                  }
               } catch (Exception var8) {
                  originals[i] = null;
               }
            }
         }
      }

   }

   private static void applyTint(int color) {
      ensureLoaded();
      upload("tint:" + color, (i) -> tintInto(i, originals[i], color));
   }

   private static void applyTexture(String name, float strength) {
      ensureLoaded();
      class_1011 custom = loadCustom(name);
      if (custom != null) {
         int strq = Math.round(strength * 255.0F);
         upload("tex:" + name + ":" + strq, (i) -> sampleInto(i, custom, strength));
      }

   }

   private static void upload(String key, Source source) {
      class_1060 tm = class_310.method_1551().method_1531();
      boolean texChanged = false;

      for(int i = 0; i < TEXTURES.length; ++i) {
         class_1044 t = tm.method_4619(TEXTURES[i]);
         if (t != null && t.method_68004() != lastUploaded[i]) {
            texChanged = true;
         }
      }

      if (!written || !key.equals(lastKey) || texChanged) {
         for(int ix = 0; ix < TEXTURES.length; ++ix) {
            if (originals[ix] != null) {
               class_1044 t = tm.method_4619(TEXTURES[ix]);
               GpuTexture gpu = t == null ? null : t.method_68004();
               if (gpu != null) {
                  class_1011 img = source.build(ix);
                  if (img != null) {
                     RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpu, img);
                     lastUploaded[ix] = gpu;
                  }
               }
            }
         }

         written = true;
         lastKey = key;
      }

   }

   private static void restore() {
      class_1060 tm = class_310.method_1551().method_1531();

      for(int i = 0; i < TEXTURES.length; ++i) {
         class_1011 src = originals[i];
         if (src != null) {
            class_1044 t = tm.method_4619(TEXTURES[i]);
            GpuTexture gpu = t == null ? null : t.method_68004();
            if (gpu != null) {
               RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpu, src);
               lastUploaded[i] = gpu;
            }
         }
      }

      written = false;
      lastKey = null;
   }

   private static class_1011 tintInto(int i, class_1011 src, int color) {
      int w = src.method_4307();
      int h = src.method_4323();
      class_1011 dst = ensureScratch(i, w, h);
      int tr = class_9848.method_61327(color);
      int tg = class_9848.method_61329(color);
      int tb = class_9848.method_61331(color);

      for(int y = 0; y < h; ++y) {
         for(int x = 0; x < w; ++x) {
            int p = src.method_61940(x, y);
            int v = Math.max(class_9848.method_61327(p), Math.max(class_9848.method_61329(p), class_9848.method_61331(p)));
            dst.method_61941(x, y, class_9848.method_61324(class_9848.method_61320(p), tr * v / 255, tg * v / 255, tb * v / 255));
         }
      }

      return dst;
   }

   private static class_1011 sampleInto(int i, class_1011 src, float strength) {
      int w = originals[i].method_4307();
      int h = originals[i].method_4323();
      int sw = src.method_4307();
      int sh = src.method_4323();
      class_1011 dst = ensureScratch(i, w, h);

      for(int y = 0; y < h; ++y) {
         float fy = ((float)y + 0.5F) * (float)sh / (float)h - 0.5F;

         for(int x = 0; x < w; ++x) {
            float fx = ((float)x + 0.5F) * (float)sw / (float)w - 0.5F;
            int p = bilinearWrapped(src, fx, fy, sw, sh);
            int r = Math.round((float)class_9848.method_61327(p) * strength);
            int g = Math.round((float)class_9848.method_61329(p) * strength);
            int b = Math.round((float)class_9848.method_61331(p) * strength);
            dst.method_61941(x, y, class_9848.method_61324(class_9848.method_61320(p), r, g, b));
         }
      }

      return dst;
   }

   private static class_1011 ensureScratch(int i, int w, int h) {
      class_1011 dst = scratch[i];
      if (dst == null || dst.method_4307() != w || dst.method_4323() != h) {
         if (dst != null) {
            dst.close();
         }

         dst = new class_1011(w, h, false);
         scratch[i] = dst;
      }

      return dst;
   }

   private static int bilinearWrapped(class_1011 img, float fx, float fy, int sw, int sh) {
      int x0 = Math.floorMod((int)Math.floor((double)fx), sw);
      int y0 = Math.floorMod((int)Math.floor((double)fy), sh);
      int x1 = (x0 + 1) % sw;
      int y1 = (y0 + 1) % sh;
      float dx = fx - (float)Math.floor((double)fx);
      float dy = fy - (float)Math.floor((double)fy);
      int p00 = img.method_61940(x0, y0);
      int p10 = img.method_61940(x1, y0);
      int p01 = img.method_61940(x0, y1);
      int p11 = img.method_61940(x1, y1);
      int a = mix(class_9848.method_61320(p00), class_9848.method_61320(p10), class_9848.method_61320(p01), class_9848.method_61320(p11), dx, dy);
      int r = mix(class_9848.method_61327(p00), class_9848.method_61327(p10), class_9848.method_61327(p01), class_9848.method_61327(p11), dx, dy);
      int g = mix(class_9848.method_61329(p00), class_9848.method_61329(p10), class_9848.method_61329(p01), class_9848.method_61329(p11), dx, dy);
      int b = mix(class_9848.method_61331(p00), class_9848.method_61331(p10), class_9848.method_61331(p01), class_9848.method_61331(p11), dx, dy);
      return class_9848.method_61324(a, r, g, b);
   }

   private static int mix(int c00, int c10, int c01, int c11, float dx, float dy) {
      float top = (float)c00 + (float)(c10 - c00) * dx;
      float bot = (float)c01 + (float)(c11 - c01) * dx;
      return Math.round(top + (bot - top) * dy);
   }

   private static class_1011 loadCustom(String name) {
      if (customCache.containsKey(name)) {
         return (class_1011)customCache.get(name);
      } else {
         class_1011 img = null;
         class_2960 id = class_2960.method_60655("solclient", "textures/misc/glints/null.png");
         Optional<class_3298> res = class_310.method_1551().method_1478().method_14486(id);
         if (res.isPresent()) {
            try {
               InputStream in = ((class_3298)res.get()).method_14482();

               try {
                  img = class_1011.method_4309(in);
               } catch (Throwable var8) {
                  if (in != null) {
                     try {
                        in.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (in != null) {
                  in.close();
               }
            } catch (Exception var9) {
               img = null;
            }
         }

         customCache.put(name, img);
         return img;
      }
   }

   static {
      TEXTURES = new class_2960[]{class_918.field_43087, class_918.field_43086};
      originals = new class_1011[TEXTURES.length];
      scratch = new class_1011[TEXTURES.length];
      lastUploaded = new GpuTexture[TEXTURES.length];
      customCache = new HashMap();
   }

   interface Source {
      class_1011 build(int var1);
   }
}
