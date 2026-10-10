package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.notification.NotificationManager;
import dev.sol.settings.SliderSetting;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;

public final class ChunkFinderModule extends Module {
   private static final int MIN_CHUNK_AGE_TICKS = 200;
   private static final int CHUNKS_PER_TICK = 8;
   public final SliderSetting mergeRadius = (SliderSetting)this.addSetting(new SliderSetting("Merge Radius", "Hives within this many chunks of each other merge to one middle flag", (double)4.0F, (double)1.0F, (double)8.0F, (double)1.0F, " ch"));
   private final Set<Long> beehiveChunks = ConcurrentHashMap.newKeySet();
   private final Set<Long> notifiedChunks = ConcurrentHashMap.newKeySet();
   private final Map<Long, Integer> firstLoadedTicks = new ConcurrentHashMap();
   private volatile Set<Long> displayChunks = Set.of();
   private int scanCursor;
   private int tickCounter;
   private boolean displayDirty;
   private int lastMergeRadius;

   public ChunkFinderModule() {
      super("Chunk Finder", "Highlights Donut SMP chunks that match a rare world signal worth checking.", Category.WORLD);
   }

   protected void onEnable() {
      this.clear();
   }

   protected void onDisable() {
      this.clear();
   }

   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1687 != null && client.field_1724 != null) {
         ++this.tickCounter;
         int radius = (Integer)client.field_1690.method_42503().method_41753();
         class_1923 playerChunk = client.field_1724.method_31476();
         int side = radius * 2 + 1;
         int total = side * side;
         if (total > 0) {
            for(int i = 0; i < 8; ++i) {
               int index = this.scanCursor % total;
               this.scanCursor = (this.scanCursor + 1) % total;
               int dx = index % side - radius;
               int dz = index / side - radius;
               int cx = playerChunk.field_9181 + dx;
               int cz = playerChunk.field_9180 + dz;
               class_2818 chunk = client.field_1687.method_2935().method_12126(cx, cz, false);
               if (chunk != null && !chunk.method_12223()) {
                  long key = class_1923.method_8331(cx, cz);
                  this.firstLoadedTicks.putIfAbsent(key, this.tickCounter);
                  boolean longLoaded = this.tickCounter - (Integer)this.firstLoadedTicks.get(key) >= 200;
                  if (longLoaded && hasTargetSignal(chunk)) {
                     if (this.beehiveChunks.add(key)) {
                        this.displayDirty = true;
                     }

                     if (this.notifiedChunks.add(key)) {
                        this.showToast(new class_1923(cx, cz));
                     }
                  } else if (this.beehiveChunks.remove(key)) {
                     this.displayDirty = true;
                  }
               }
            }

            if (this.beehiveChunks.removeIf((keyx) -> outOfRange(keyx, playerChunk, radius))) {
               this.displayDirty = true;
            }

            this.firstLoadedTicks.keySet().removeIf((keyx) -> outOfRange(keyx, playerChunk, radius));
            if (this.displayDirty || this.mergeRadius.getInt() != this.lastMergeRadius) {
               this.rebuildDisplay();
            }
         }
      }

   }

   private void rebuildDisplay() {
      this.displayDirty = false;
      this.lastMergeRadius = this.mergeRadius.getInt();
      Set<Long> raw = new HashSet(this.beehiveChunks);
      if (raw.size() < 2) {
         this.displayChunks = Set.copyOf(raw);
      } else {
         int radius = Math.max(1, this.lastMergeRadius);
         Set<Long> display = new HashSet();
         Set<Long> visited = new HashSet();

         for(long seed : raw) {
            if (visited.add(seed)) {
               List<Long> cluster = new ArrayList();
               Deque<Long> frontier = new ArrayDeque();
               frontier.add(seed);

               while(!frontier.isEmpty()) {
                  long current = (Long)frontier.poll();
                  cluster.add(current);
                  int cx = class_1923.method_8325(current);
                  int cz = class_1923.method_8332(current);

                  for(int dx = -radius; dx <= radius; ++dx) {
                     for(int dz = -radius; dz <= radius; ++dz) {
                        if (dx != 0 || dz != 0) {
                           long neighbour = class_1923.method_8331(cx + dx, cz + dz);
                           if (raw.contains(neighbour) && visited.add(neighbour)) {
                              frontier.add(neighbour);
                           }
                        }
                     }
                  }
               }

               display.add(cluster.size() == 1 ? (Long)cluster.get(0) : middleChunk(cluster));
            }
         }

         this.displayChunks = Set.copyOf(display);
      }

   }

   private static long middleChunk(List<Long> cluster) {
      long sumX = 0L;
      long sumZ = 0L;

      for(long key : cluster) {
         sumX += (long)class_1923.method_8325(key);
         sumZ += (long)class_1923.method_8332(key);
      }

      int mx = (int)Math.round((double)sumX / (double)cluster.size());
      int mz = (int)Math.round((double)sumZ / (double)cluster.size());
      return class_1923.method_8331(mx, mz);
   }

   private static boolean hasTargetSignal(class_2818 chunk) {
      for(class_2826 section : chunk.method_12006()) {
         if (section != null && !section.method_38292() && section.method_12265().method_19526(ChunkFinderModule::isFullHoney)) {
            for(int lx = 0; lx < 16; ++lx) {
               for(int lz = 0; lz < 16; ++lz) {
                  for(int ly = 0; ly < 16; ++ly) {
                     if (isFullHoney(section.method_12254(lx, ly, lz))) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean isFullHoney(class_2680 state) {
      return (state.method_27852(class_2246.field_20422) || state.method_27852(class_2246.field_20421)) && state.method_28498(class_2741.field_20432) && (Integer)state.method_11654(class_2741.field_20432) == 5;
   }

   private static boolean outOfRange(long key, class_1923 player, int radius) {
      return Math.abs(class_1923.method_8325(key) - player.field_9181) > radius || Math.abs(class_1923.method_8332(key) - player.field_9180) > radius;
   }

   private void showToast(class_1923 chunk) {
      if (SolClient.notifications() != null) {
         NotificationManager var10000 = SolClient.notifications();
         int var10001 = chunk.method_33940();
         var10000.pushInfo("Chunk Finder · X " + var10001 + " Z " + chunk.method_33942());
      }

   }

   public Set<Long> flaggedChunks() {
      return this.displayChunks;
   }

   public void clear() {
      this.beehiveChunks.clear();
      this.notifiedChunks.clear();
      this.firstLoadedTicks.clear();
      this.displayChunks = Set.of();
      this.scanCursor = 0;
      this.tickCounter = 0;
      this.displayDirty = false;
      this.lastMergeRadius = this.mergeRadius.getInt();
   }
}
