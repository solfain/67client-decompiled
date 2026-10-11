package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.IconListSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2343;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4970;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_7923;

public class BlockEntityEspModule extends Module {
   private static final String OTHER = "other";
   private static final int MAX_ENTRIES = 8192;
   public final IconListSetting blockEntities = (IconListSetting)this.addSetting(new IconListSetting("Block Entities", "Which block-entity types to highlight"));
   public final ModeSetting mode = (ModeSetting)this.addSetting(new ModeSetting("Mode", "Box style: outline or see-through fill", "Full", new String[]{"Full", "Outline"}));
   public final SliderSetting range = (SliderSetting)this.addSetting(new SliderSetting("Range", "Max distance a block entity is highlighted", (double)128.0F, (double)16.0F, (double)512.0F, (double)8.0F));
   public final SliderSetting highlightAlpha = (SliderSetting)this.addSetting(new SliderSetting("Highlight Alpha", "Box opacity (0-255)", (double)180.0F, (double)0.0F, (double)255.0F, (double)1.0F));
   public final BooleanSetting tracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each block entity", false));
   public final BooleanSetting showGhosts = (BooleanSetting)this.addSetting(new BooleanSetting("Show Ghosts", "Keep players the server listed who are not here anymore", true));
   public final ColorSetting ghostTint = (ColorSetting)this.addSetting(new ColorSetting("Ghost Tint", "Color blended into ghost entries", -922791856));
   public final BooleanSetting chunkPackets = (BooleanSetting)this.addSetting(new BooleanSetting("Chunk Packets", "Read block entities from chunk-data packets", true));
   public final BooleanSetting beUpdatePackets = (BooleanSetting)this.addSetting(new BooleanSetting("BE Update Packets", "Read block entities from block-entity update packets", true));
   public final BooleanSetting worldRescan = (BooleanSetting)this.addSetting(new BooleanSetting("World Rescan", "Also scan chunks that are already loaded when turned on", true));
   private final Map<String, String> aliases = new HashMap();
   private final Map<Long, Cached> cache = new ConcurrentHashMap();

   public BlockEntityEspModule() {
      super("BlockEntityESP", "Highlights block entities from raw packets", Category.RENDER);
      ColorSetting var10000 = this.ghostTint;
      BooleanSetting var10001 = this.showGhosts;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::get);
      this.type("minecraft:chest", "Chest", class_1802.field_8106, -22016, true);
      this.type("minecraft:trapped_chest", "Trapped Chest", class_1802.field_8247, -65536, true);
      this.type("minecraft:ender_chest", "Ender Chest", class_1802.field_8466, -8912641, true);
      this.type("minecraft:shulker_box", "Shulker Box", class_1802.field_8545, -47873, true);
      this.type("minecraft:barrel", "Barrel", class_1802.field_16307, -7842560, true);
      this.type("minecraft:mob_spawner", "Spawner", class_1802.field_8849, -16711936, true);
      this.type("minecraft:hopper", "Hopper", class_1802.field_8239, -7829368, false);
      this.type("minecraft:furnace", "Furnace", class_1802.field_8732, -7566196, false);
      this.alias("minecraft:blast_furnace", "minecraft:furnace");
      this.alias("minecraft:smoker", "minecraft:furnace");
      this.type("minecraft:dispenser", "Dispenser", class_1802.field_8357, -10066330, false);
      this.alias("minecraft:dropper", "minecraft:dispenser");
      this.type("minecraft:brewing_stand", "Brewing Stand", class_1802.field_8740, -3372801, false);
      this.type("minecraft:beehive", "Beehive", class_1802.field_20416, -13312, false);
      this.type("minecraft:enchanting_table", "Enchanting Table", class_1802.field_8657, -7864065, false);
      this.type("minecraft:sign", "Sign", class_1802.field_8788, -3355444, false);
      this.alias("minecraft:hanging_sign", "minecraft:sign");
      this.type("minecraft:bed", "Bed", class_1802.field_8789, -30584, false);
      this.type("minecraft:skull", "Skull", class_1802.field_8398, -2236963, false);
      this.type("minecraft:banner", "Banner", class_1802.field_8539, -1118482, false);
      this.type("minecraft:crafter", "Crafter", class_1802.field_46791, -12276993, false);
      this.type("minecraft:vault", "Vault", class_1802.field_48847, -10496, false);
      this.type("minecraft:trial_spawner", "Trial Spawner", class_1802.field_47314, -16711766, false);
      this.type("other", "Other", class_1802.field_8542, -5592406, true);
   }

   private void type(String id, String label, class_1792 icon, int color, boolean on) {
      this.blockEntities.add(id, label, icon, on, color);
   }

   private void alias(String from, String to) {
      this.aliases.put(from, to);
   }

   private String canonicalKey(String typeId) {
      if (this.blockEntities.get(typeId) != null) {
         return typeId;
      } else {
         String aliased = (String)this.aliases.get(typeId);
         if (aliased != null) {
            return aliased;
         } else {
            int slash = typeId.indexOf(58);
            if (slash >= 0) {
               String plain = "minecraft:" + typeId.substring(slash + 1);
               if (this.blockEntities.get(plain) != null) {
                  return plain;
               }

               if (this.aliases.containsKey(plain)) {
                  return (String)this.aliases.get(plain);
               }
            }

            return "other";
         }
      }
   }

   public boolean chunkPacketsEnabled() {
      return (Boolean)this.chunkPackets.get();
   }

   public boolean beUpdatePacketsEnabled() {
      return (Boolean)this.beUpdatePackets.get();
   }

   public void run(class_2338 pos, class_2591<?> type) {
      if (pos != null && type != null) {
         class_2960 id = class_7923.field_41181.method_10221(type);
         String key = this.canonicalKey(id != null ? id.toString() : String.valueOf(type));
         this.cache.put(pos.method_10063(), new Cached(pos.method_10062(), key, System.currentTimeMillis()));
         if (this.cache.size() > 8192) {
            this.pruneOldest();
         }
      }

   }

   private void pruneOldest() {
      long oldestTime = Long.MAX_VALUE;
      Long oldestKey = null;

      for(Map.Entry<Long, Cached> e : this.cache.entrySet()) {
         if (((Cached)e.getValue()).lastSeenMs() < oldestTime) {
            oldestTime = ((Cached)e.getValue()).lastSeenMs();
            oldestKey = (Long)e.getKey();
         }
      }

      if (oldestKey != null) {
         this.cache.remove(oldestKey);
      }

   }

   public Collection<Cached> entries() {
      return this.cache.values();
   }

   public void clear() {
      this.cache.clear();
   }

   public int cachedCount() {
      return this.cache.size();
   }

   protected void onEnable() {
      this.cache.clear();
      if ((Boolean)this.worldRescan.get()) {
         this.rescanLoadedChunks();
      }

   }

   protected void onDisable() {
      this.cache.clear();
   }

   public void onTick() {
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (mc.field_1687 != null && player != null) {
         double r = (Double)this.range.get();
         double maxSq = r * r;
         double px = player.method_23317();
         double py = player.method_23318();
         double pz = player.method_23321();
         this.cache.values().removeIf((c) -> {
            class_2338 p = c.pos();
            double dx = (double)p.method_10263() + (double)0.5F - px;
            double dy = (double)p.method_10264() + (double)0.5F - py;
            double dz = (double)p.method_10260() + (double)0.5F - pz;
            return dx * dx + dy * dy + dz * dz > maxSq;
         });
      }

   }

   private void rescanLoadedChunks() {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      class_746 player = mc.field_1724;
      if (level != null && player != null) {
         int radius = Math.min(32, (int)Math.ceil((Double)this.range.get() / (double)16.0F) + 2);
         int pcx = player.method_31476().field_9181;
         int pcz = player.method_31476().field_9180;
         class_2338.class_2339 p = new class_2338.class_2339();

         for(int cx = pcx - radius; cx <= pcx + radius; ++cx) {
            for(int cz = pcz - radius; cz <= pcz + radius; ++cz) {
               if (level.method_2935().method_12123(cx, cz)) {
                  class_2818 chunk = level.method_8497(cx, cz);
                  class_2826[] sections = chunk.method_12006();
                  int minSectionY = chunk.method_32891();
                  int baseX = chunk.method_12004().method_8326();
                  int baseZ = chunk.method_12004().method_8328();

                  for(int s = 0; s < sections.length; ++s) {
                     class_2826 section = sections[s];
                     if (!section.method_38292() && section.method_19523(class_4970.class_4971::method_31709)) {
                        int baseY = minSectionY + s << 4;

                        for(int y = 0; y < 16; ++y) {
                           for(int z = 0; z < 16; ++z) {
                              for(int x = 0; x < 16; ++x) {
                                 class_2680 state = section.method_12254(x, y, z);
                                 if (state.method_31709()) {
                                    class_2248 var23 = state.method_26204();
                                    if (var23 instanceof class_2343) {
                                       class_2343 eb = (class_2343)var23;
                                       p.method_10103(baseX + x, baseY + y, baseZ + z);

                                       try {
                                          class_2586 be = eb.method_10123(p.method_10062(), state);
                                          if (be != null) {
                                             this.run(p, be.method_11017());
                                          }
                                       } catch (Exception var24) {
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

   }

   public final class Cached {
      private class_2338 pos;
      private String typeKey;
      private long lastSeenMs;

      public Cached(class_2338 pos, String typeKey, long lastSeenMs) {
         this.pos = pos;
         this.typeKey = typeKey;
         this.lastSeenMs = lastSeenMs;
      }

      public class_2338 pos() {
         return this.pos;
      }

      public String typeKey() {
         return this.typeKey;
      }

      public long lastSeenMs() {
         return this.lastSeenMs;
      }
   }
}
