package dev.sol.render.nanovg;

import dev.sol.SolClient;
import dev.sol.module.Category;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryUtil;

public final class NVGIcons {
   private static final int RASTER_SIZE = 64;
   private static final Map<String, Integer> ICONS = new HashMap();
   private static boolean loaded;

   private NVGIcons() {
   }

   public static int get(Category category) {
      ensureLoaded();
      return (Integer)ICONS.getOrDefault(category.getIconId(), -1);
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;

         for(Category category : Category.values()) {
            int handle = loadSvg("assets/solclient/icons/" + category.getIconId() + ".svg");
            if (handle > 0) {
               ICONS.put(category.getIconId(), handle);
            }
         }
      }

   }

   private static int loadSvg(String resourcePath) {
      long ctx = NVGRenderer.get().ctx();

      try {
         InputStream in = NVGIcons.class.getClassLoader().getResourceAsStream(resourcePath);

         byte var25;
         label254: {
            int var12;
            label255: {
               byte var27;
               try {
                  if (in == null) {
                     SolClient.LOGGER.warn("Icon resource missing: {}", resourcePath);
                     var25 = -1;
                     break label254;
                  }

                  String svg = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                  svg = svg.replaceAll("#[0-9a-fA-F]{6}", "#ffffff").replaceAll("#[0-9a-fA-F]{3}\\b", "#fff").replace("currentColor", "#ffffff").replace("\"black\"", "\"white\"");
                  ByteBuffer svgData = MemoryUtil.memUTF8(svg, true);
                  ByteBuffer units = MemoryUtil.memASCII("px");
                  NSVGImage image = null;
                  long rasterizer = 0L;
                  ByteBuffer pixels = null;
                  boolean var18 = false;

                  byte scale;
                  label257: {
                     label258: {
                        try {
                           var18 = true;
                           image = NanoSVG.nsvgParse(svgData, units, 96.0F);
                           if (image != null) {
                              rasterizer = NanoSVG.nsvgCreateRasterizer();
                              float scalex = 64.0F / Math.max(image.width(), image.height());
                              pixels = MemoryUtil.memAlloc(16384);
                              NanoSVG.nsvgRasterize(rasterizer, image, 0.0F, 0.0F, scalex, pixels, 64, 64, 256);
                              var12 = NanoVG.nvgCreateImageRGBA(ctx, 64, 64, 0, pixels);
                              var18 = false;
                              break label258;
                           }

                           scale = -1;
                           var18 = false;
                        } finally {
                           if (var18) {
                              if (pixels != null) {
                                 MemoryUtil.memFree(pixels);
                              }

                              if (rasterizer != 0L) {
                                 NanoSVG.nsvgDeleteRasterizer(rasterizer);
                              }

                              if (image != null) {
                                 NanoSVG.nsvgDelete(image);
                              }

                              MemoryUtil.memFree(svgData);
                              MemoryUtil.memFree(units);
                           }

                        }

                        if (image != null) {
                           NanoSVG.nsvgDelete(image);
                        }
                        break label257;
                     }

                     if (pixels != null) {
                        MemoryUtil.memFree(pixels);
                     }

                     if (rasterizer != 0L) {
                        NanoSVG.nsvgDeleteRasterizer(rasterizer);
                     }

                     if (image != null) {
                        NanoSVG.nsvgDelete(image);
                     }

                     MemoryUtil.memFree(svgData);
                     MemoryUtil.memFree(units);
                     break label255;
                  }

                  MemoryUtil.memFree(svgData);
                  MemoryUtil.memFree(units);
                  var27 = scale;
               } catch (Throwable var221) {
                  if (in != null) {
                     try {
                        in.close();
                     } catch (Throwable var20) {
                        var221.addSuppressed(var20);
                     }
                  }

                  throw var221;
               }

               if (in != null) {
                  in.close();
               }

               return var27;
            }

            if (in != null) {
               in.close();
            }

            return var12;
         }

         if (in != null) {
            in.close();
         }

         return var25;
      } catch (Exception var22) {
         SolClient.LOGGER.error("Failed to rasterize icon {}", resourcePath, var22);
         return -1;
      }
   }
}
