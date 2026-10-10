package dev.sol.suschunk;

import dev.sol.SolClient;
import dev.sol.module.Modules;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_4076;

public class SusChunkScanner {
   public static final int POINTS_PER_SENSITIVITY = 5;
   private static final int GEODE_LINK_DISTANCE = 5;
   private static final boolean DEBUG_LOG = Boolean.getBoolean("sol.sus.debug");
   private Modules.SusChunkFinderModule module;
   private final Map<Long, ChunkScore> scores = new ConcurrentHashMap();
   private final Deque<Long> queue = new ArrayDeque();
   private final Set<Long> lightRescans = ConcurrentHashMap.newKeySet();
   private final Set<Long> alertedChunks = new HashSet();
   private volatile List<Flag> flags = List.of();
   private volatile List<Zone> zones = List.of();
   private class_1923 lastQueueCenter;
   private int tickCounter;

   public SusChunkScanner(Modules.SusChunkFinderModule module) {
      this.module = module;
      ServerLightCache var10000 = ServerLightCache.get();
      Set var10001 = this.lightRescans;
      Objects.requireNonNull(var10001);
      var10000.addDirtyListener(var10001::add);
   }

   public List<Flag> flags() {
      return this.flags;
   }

   public List<Zone> zones() {
      return this.zones;
   }

   public int threshold() {
      return this.module.sensitivity.getInt() * 5;
   }

   public void clear() {
      this.scores.clear();
      this.queue.clear();
      this.lightRescans.clear();
      this.alertedChunks.clear();
      this.flags = List.of();
      this.zones = List.of();
      this.lastQueueCenter = null;
   }

   public void tick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         try {
            this.refillQueueIfNeeded(mc);
            int budget = this.module.scanSpeed.getInt();
            long deadline = System.nanoTime() + 2000000L;
            int scanned = this.scanLightRescans(mc, budget, deadline);
            this.scanQueue(mc, budget - scanned, deadline);
            if (++this.tickCounter % 10 == 0) {
               this.rebuild(mc);
            }
         } catch (Exception var6) {
            SolClient.LOGGER.warn("SusChunkFinder scan error: {}", var6.toString());
         }
      }

   }

   private int scanLightRescans(class_310 mc, int budget, long deadline) {
      if (this.lightRescans.isEmpty()) {
         return 0;
      } else {
         int scanned = 0;
         Iterator<Long> iterator = this.lightRescans.iterator();

         while(iterator.hasNext() && scanned < budget && System.nanoTime() < deadline) {
            long key = (Long)iterator.next();
            iterator.remove();
            class_2818 chunk = mc.field_1687.method_2935().method_12126(class_1923.method_8325(key), class_1923.method_8332(key), false);
            if (chunk != null && this.scores.containsKey(key)) {
               this.scores.put(key, this.scanChunk(mc, chunk));
               ++scanned;
            }
         }

         return scanned;
      }
   }

   private void scanQueue(class_310 mc, int budget, long deadline) {
      int scanned = 0;
      int polled = 0;

      while(scanned < budget && polled < 128 && !this.queue.isEmpty() && System.nanoTime() < deadline) {
         ++polled;
         long key = (Long)this.queue.pollFirst();
         if (!this.scores.containsKey(key)) {
            class_2818 chunk = mc.field_1687.method_2935().method_12126(class_1923.method_8325(key), class_1923.method_8332(key), false);
            if (chunk != null) {
               this.scores.put(key, this.scanChunk(mc, chunk));
               ++scanned;
            }
         }
      }

   }

   private void refillQueueIfNeeded(class_310 mc) {
      class_1923 center = mc.field_1724.method_31476();
      if (this.queue.isEmpty() || this.lastQueueCenter == null || Math.max(Math.abs(center.field_9181 - this.lastQueueCenter.field_9181), Math.abs(center.field_9180 - this.lastQueueCenter.field_9180)) >= 3) {
         this.lastQueueCenter = center;
         this.queue.clear();
         int radius = Math.min((Integer)mc.field_1690.method_42503().method_41753() + 1, 16);
         List<Long> order = new ArrayList();

         for(int dx = -radius; dx <= radius; ++dx) {
            for(int dz = -radius; dz <= radius; ++dz) {
               long key = class_1923.method_8331(center.field_9181 + dx, center.field_9180 + dz);
               if (!this.scores.containsKey(key)) {
                  order.add(key);
               }
            }
         }

         order.sort(Comparator.comparingDouble((k) -> Math.hypot((double)(class_1923.method_8325(k) - center.field_9181), (double)(class_1923.method_8332(k) - center.field_9180))));
         this.queue.addAll(order);
      }

   }

   private static boolean isPlantTarget(class_2680 state) {
      class_2248 block = state.method_26204();
      return block == class_2246.field_9993 || block == class_2246.field_10463 || block == class_2246.field_10211 || block == class_2246.field_16999 || block == class_2246.field_10597 || block == class_2246.field_28048;
   }

   private static boolean isAmethystStructure(class_2680 state) {
      return state.method_27852(class_2246.field_27160) || state.method_27852(class_2246.field_27159);
   }

   private ChunkScore scanChunk(class_310 mc, class_2818 chunk) {
      ChunkScore result = new ChunkScore(chunk.method_12004().method_8324());
      if ((Boolean)this.module.amethyst.get()) {
         this.detectAmethyst(mc, chunk, result);
      }

      this.detectGrowth(mc, chunk, result);
      result.computeScore();
      if (DEBUG_LOG && result.score > (double)0.0F) {
         SolClient.LOGGER.info("[sus] chunk {} score={} hits={}", new Object[]{chunk.method_12004(), result.score, result.hits});
      }

      return result;
   }

   private void detectAmethyst(class_310 mc, class_2818 chunk, ChunkScore result) {
      List<class_2338> cells = ServerLightCache.get().light5Positions(chunk.method_12004().field_9181, chunk.method_12004().field_9180);
      if (!cells.isEmpty()) {
         class_2338.class_2339 cursor = new class_2338.class_2339();

         for(class_2338 cell : cells) {
            class_2680 state = mc.field_1687.method_8320(cell);
            if ((state.method_26215() || state.method_27852(class_2246.field_27161)) && hasAmethystNeighbour(mc, cell, cursor)) {
               result.amethystCells.add(cell.method_10062());
               if (DEBUG_LOG) {
                  SolClient.LOGGER.info("[sus] AMETHYST light-5 cell @{} ({})", cell, state.method_26215() ? "hidden" : "visible");
               }
            }
         }
      }

   }

   private static boolean hasAmethystNeighbour(class_310 mc, class_2338 cell, class_2338.class_2339 cursor) {
      for(int dx = -1; dx <= 1; ++dx) {
         for(int dy = -1; dy <= 1; ++dy) {
            for(int dz = -1; dz <= 1; ++dz) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  cursor.method_10103(cell.method_10263() + dx, cell.method_10264() + dy, cell.method_10260() + dz);
                  if (isAmethystStructure(mc.field_1687.method_8320(cursor))) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private void detectGrowth(class_310 mc, class_2818 chunk, ChunkScore result) {
      class_2826[] sections = chunk.method_12006();
      int minSectionY = chunk.method_32891();
      int loSection = class_4076.method_18675(-12);
      int hiSection = class_4076.method_18675(80);
      int baseX = chunk.method_12004().method_8326();
      int baseZ = chunk.method_12004().method_8328();
      class_2338.class_2339 cursor = new class_2338.class_2339();

      for(int s = 0; s < sections.length; ++s) {
         int sectionY = minSectionY + s;
         if (sectionY >= loSection && sectionY <= hiSection) {
            class_2826 section = sections[s];
            if (!section.method_38292() && section.method_12265().method_19526(SusChunkScanner::isPlantTarget)) {
               int baseY = sectionY << 4;

               for(int y = 0; y < 16; ++y) {
                  int worldY = baseY + y;
                  if (worldY >= -12 && worldY <= 80) {
                     for(int z = 0; z < 16; ++z) {
                        for(int x = 0; x < 16; ++x) {
                           class_2680 state = section.method_12254(x, y, z);
                           if (isPlantTarget(state)) {
                              cursor.method_10103(baseX + x, worldY, baseZ + z);
                              this.inspectPlant(mc, state, cursor, result);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private void inspectPlant(class_310 mc, class_2680 state, class_2338 pos, ChunkScore result) {
      class_2248 block = state.method_26204();
      if ((block == class_2246.field_9993 || block == class_2246.field_10463) && (Boolean)this.module.kelp.get()) {
         class_2680 above = mc.field_1687.method_8320(pos.method_10084());
         if (above.method_27852(class_2246.field_9993) || above.method_27852(class_2246.field_10463)) {
            return;
         }

         int height = columnLength(mc, pos, class_2350.field_11033, class_2246.field_9993, class_2246.field_10463);
         boolean maxAge = block == class_2246.field_9993 && state.method_28498(class_2741.field_12517) && (Integer)state.method_11654(class_2741.field_12517) == 25;
         if (maxAge && height >= 8 || height >= 14) {
            result.add(SusChunkScanner.SignalType.KELP, pos);
            if (DEBUG_LOG) {
               SolClient.LOGGER.info("[sus] KELP @{} height={} maxAge={}", new Object[]{pos, height, maxAge});
            }
         }
      } else if (block == class_2246.field_10211 && (Boolean)this.module.bamboo.get()) {
         if (mc.field_1687.method_8320(pos.method_10074()).method_27852(class_2246.field_10211)) {
            return;
         }

         int height = columnLength(mc, pos, class_2350.field_11036, class_2246.field_10211);
         if (height >= 12) {
            result.add(SusChunkScanner.SignalType.BAMBOO, pos);
            if (DEBUG_LOG) {
               SolClient.LOGGER.info("[sus] BAMBOO @{} height={}", pos, height);
            }
         }
      } else if (block == class_2246.field_16999 && (Boolean)this.module.berries.get()) {
         if (state.method_28498(class_2741.field_12497) && (Integer)state.method_11654(class_2741.field_12497) == 3) {
            result.add(SusChunkScanner.SignalType.BERRIES, pos);
            if (DEBUG_LOG) {
               SolClient.LOGGER.info("[sus] BERRIES max age @{}", pos);
            }
         }
      } else if (block == class_2246.field_10597 && (Boolean)this.module.vines.get()) {
         if (mc.field_1687.method_8320(pos.method_10084()).method_27852(class_2246.field_10597)) {
            return;
         }

         int length = columnLength(mc, pos, class_2350.field_11033, class_2246.field_10597);
         if (length >= 7) {
            result.add(SusChunkScanner.SignalType.VINES, pos);
            if (DEBUG_LOG) {
               SolClient.LOGGER.info("[sus] VINES @{} hang={}", pos, length);
            }
         }
      } else if (block == class_2246.field_28048 && (Boolean)this.module.dripstone.get()) {
         if (mc.field_1687.method_8320(pos.method_10084()).method_27852(class_2246.field_28048) || !mc.field_1687.method_8320(pos.method_10074()).method_27852(class_2246.field_28048)) {
            return;
         }

         int length = columnLength(mc, pos, class_2350.field_11033, class_2246.field_28048);
         if (length >= 5) {
            result.add(SusChunkScanner.SignalType.DRIPSTONE, pos);
            if (DEBUG_LOG) {
               SolClient.LOGGER.info("[sus] DRIPSTONE @{} length={}", pos, length);
            }
         }
      }

   }

   private static int columnLength(class_310 mc, class_2338 start, class_2350 dir, class_2248... blocks) {
      int length = 1;

      label23:
      for(class_2338.class_2339 cursor = start.method_25503(); length < 40; ++length) {
         cursor.method_10098(dir);
         class_2680 state = mc.field_1687.method_8320(cursor);

         for(class_2248 block : blocks) {
            if (state.method_27852(block)) {
               ;
            }
         }
         break;
      }

      return length;
   }

   private void rebuild(class_310 mc) {
      this.scores.keySet().removeIf((key) -> mc.field_1687.method_2935().method_12126(class_1923.method_8325(key), class_1923.method_8332(key), false) == null);
      int threshold = this.threshold();
      Map<Long, FlagAggregate> aggregates = new HashMap();

      for(ChunkScore score : this.scores.values()) {
         if (score.score >= (double)threshold) {
            FlagAggregate agg = (FlagAggregate)aggregates.computeIfAbsent(score.chunkKey, (x$0) -> new FlagAggregate(x$0));
            agg.score = Math.max(agg.score, score.score);
            agg.hitWeight += score.hitWeight;
            agg.hitX += score.hitX;
            agg.hitZ += score.hitZ;
         }
      }

      for(Geode geode : this.clusterGeodes()) {
         if (!(geode.score() < (double)threshold)) {
            for(long chunkKey : geode.chunks()) {
               FlagAggregate agg = (FlagAggregate)aggregates.computeIfAbsent(chunkKey, (x$0) -> new FlagAggregate(x$0));
               agg.score = Math.max(agg.score, geode.score());
            }

            double weight = SusChunkScanner.SignalType.AMETHYST.weight;

            for(class_2338 cell : geode.cells()) {
               FlagAggregate agg = (FlagAggregate)aggregates.get(class_1923.method_8331(cell.method_10263() >> 4, cell.method_10260() >> 4));
               if (agg != null) {
                  agg.hitWeight += weight;
                  agg.hitX += ((double)cell.method_10263() + (double)0.5F) * weight;
                  agg.hitZ += ((double)cell.method_10260() + (double)0.5F) * weight;
               }
            }

            if (DEBUG_LOG) {
               SolClient.LOGGER.info("[sus] GEODE {} cells / {} chunks, score {} -> FLAG", new Object[]{geode.cells().size(), geode.chunks().size(), geode.score()});
            }
         }
      }

      List<Flag> flagged = new ArrayList();

      for(FlagAggregate agg : aggregates.values()) {
         flagged.add(new Flag(agg.chunkKey, agg.score));
      }

      this.flags = List.copyOf(flagged);
      this.zones = this.buildZones(flagged, aggregates);
      this.fireAlerts(mc);
   }

   private List<Geode> clusterGeodes() {
      List<class_2338> cells = new ArrayList();

      for(ChunkScore score : this.scores.values()) {
         cells.addAll(score.amethystCells);
      }

      int n = cells.size();
      if (n == 0) {
         return List.of();
      } else {
         int[] parent = new int[n];

         for(int i = 0; i < n; parent[i] = i++) {
         }

         for(int ix = 0; ix < n; ++ix) {
            class_2338 a = (class_2338)cells.get(ix);

            for(int j = ix + 1; j < n; ++j) {
               class_2338 b = (class_2338)cells.get(j);
               int chebyshev = Math.max(Math.abs(a.method_10263() - b.method_10263()), Math.max(Math.abs(a.method_10264() - b.method_10264()), Math.abs(a.method_10260() - b.method_10260())));
               if (chebyshev <= 5) {
                  parent[find(parent, ix)] = find(parent, j);
               }
            }
         }

         Map<Integer, List<class_2338>> groups = new HashMap();

         for(int ix = 0; ix < n; ++ix) {
            ((List)groups.computeIfAbsent(find(parent, ix), (k) -> new ArrayList())).add((class_2338)cells.get(ix));
         }

         List<Geode> geodes = new ArrayList();

         for(List<class_2338> group : groups.values()) {
            Set<Long> chunks = new HashSet();
            double sx = (double)0.0F;
            double sz = (double)0.0F;

            for(class_2338 c : group) {
               chunks.add(class_1923.method_8331(c.method_10263() >> 4, c.method_10260() >> 4));
               sx += (double)c.method_10263() + (double)0.5F;
               sz += (double)c.method_10260() + (double)0.5F;
            }

            double score = SusChunkScanner.SignalType.AMETHYST.weight * (double)Math.min(group.size(), SusChunkScanner.SignalType.AMETHYST.cap);
            geodes.add(new Geode(List.copyOf(group), Set.copyOf(chunks), score, sx / (double)group.size(), sz / (double)group.size()));
         }

         return geodes;
      }
   }

   private static int find(int[] parent, int i) {
      while(parent[i] != i) {
         parent[i] = parent[parent[i]];
         i = parent[i];
      }

      return i;
   }

   private List<Zone> buildZones(List<Flag> flagged, Map<Long, FlagAggregate> aggregates) {
      int radius = Math.max(1, this.module.mergeRadius.getInt());
      Map<Long, Flag> byKey = new HashMap();

      for(Flag flag : flagged) {
         byKey.put(flag.chunkKey(), flag);
      }

      List<Zone> built = new ArrayList();
      Set<Long> visited = new HashSet();

      for(Flag seed : flagged) {
         if (visited.add(seed.chunkKey())) {
            List<Flag> group = new ArrayList();
            Deque<Flag> frontier = new ArrayDeque(List.of(seed));

            while(!frontier.isEmpty()) {
               Flag current = (Flag)frontier.poll();
               group.add(current);
               int cx = class_1923.method_8325(current.chunkKey());
               int cz = class_1923.method_8332(current.chunkKey());

               for(int dx = -radius; dx <= radius; ++dx) {
                  for(int dz = -radius; dz <= radius; ++dz) {
                     if (dx != 0 || dz != 0) {
                        Flag neighbour = (Flag)byKey.get(class_1923.method_8331(cx + dx, cz + dz));
                        if (neighbour != null && visited.add(neighbour.chunkKey())) {
                           frontier.add(neighbour);
                        }
                     }
                  }
               }
            }

            built.add(this.makeZone(group, aggregates));
         }
      }

      built.sort(Comparator.comparingDouble(Zone::totalScore).reversed());
      return built;
   }

   private Zone makeZone(List<Flag> group, Map<Long, FlagAggregate> aggregates) {
      Set<Long> members = new HashSet();
      double totalScore = (double)0.0F;
      double maxScore = (double)0.0F;
      double hitWeight = (double)0.0F;
      double hitX = (double)0.0F;
      double hitZ = (double)0.0F;
      double chunkX = (double)0.0F;
      double chunkZ = (double)0.0F;

      for(Flag flag : group) {
         members.add(flag.chunkKey());
         totalScore += flag.score();
         maxScore = Math.max(maxScore, flag.score());
         FlagAggregate agg = (FlagAggregate)aggregates.get(flag.chunkKey());
         if (agg != null && agg.hitWeight > (double)0.0F) {
            hitWeight += agg.hitWeight;
            hitX += agg.hitX;
            hitZ += agg.hitZ;
         }

         chunkX += (double)(class_1923.method_8325(flag.chunkKey()) * 16 + 8) * flag.score();
         chunkZ += (double)(class_1923.method_8332(flag.chunkKey()) * 16 + 8) * flag.score();
      }

      double cx = hitWeight > (double)0.0F ? hitX / hitWeight : chunkX / totalScore;
      double cz = hitWeight > (double)0.0F ? hitZ / hitWeight : chunkZ / totalScore;
      return new Zone(Set.copyOf(members), cx, cz, totalScore, maxScore);
   }

   private void fireAlerts(class_310 mc) {
      for(Zone zone : this.zones) {
         boolean isNew = true;

         for(long member : zone.members()) {
            if (this.alertedChunks.contains(member)) {
               isNew = false;
               break;
            }
         }

         if (!isNew) {
            this.alertedChunks.addAll(zone.members());
         } else {
            this.alertedChunks.addAll(zone.members());
            if (!this.module.notifications.check("Off")) {
               int bx = (int)Math.round(zone.centroidX());
               int bz = (int)Math.round(zone.centroidZ());
               int dist = (int)Math.hypot((double)bx - mc.field_1724.method_23317(), (double)bz - mc.field_1724.method_23321());
               String message = "Sus zone · " + dist + "m · " + bx + ", " + bz;
               if (this.module.notifications.check("Toast") && SolClient.notifications() != null) {
                  SolClient.notifications().pushInfo(message);
               } else if (this.module.notifications.check("Chat")) {
                  mc.field_1724.method_7353(class_2561.method_43470("§6[Sol] §fnull"), false);
               }
            }
         }
      }

   }

   public final class ChunkScore {
      public long chunkKey;
      public final EnumMap<SignalType, Integer> hits = new EnumMap(SignalType.class);
      public final List<class_2338> amethystCells = new ArrayList();
      public double score;
      double hitWeight;
      double hitX;
      double hitZ;

      ChunkScore(long chunkKey) {
         this.chunkKey = chunkKey;
      }

      void add(SignalType type, class_2338 pos) {
         this.hits.merge(type, 1, Integer::sum);
         this.hitWeight += type.weight;
         this.hitX += ((double)pos.method_10263() + (double)0.5F) * type.weight;
         this.hitZ += ((double)pos.method_10260() + (double)0.5F) * type.weight;
      }

      void computeScore() {
         double total = (double)0.0F;

         for(Map.Entry<SignalType, Integer> entry : this.hits.entrySet()) {
            total += ((SignalType)entry.getKey()).weight * (double)Math.min((Integer)entry.getValue(), ((SignalType)entry.getKey()).cap);
         }

         this.score = total;
      }
   }

   public final class Flag {
      private long chunkKey;
      private double score;

      public Flag(long chunkKey, double score) {
         this.chunkKey = chunkKey;
         this.score = score;
      }

      public long chunkKey() {
         return this.chunkKey;
      }

      public double score() {
         return this.score;
      }
   }

   final class FlagAggregate {
      final long chunkKey;
      double score;
      double hitWeight;
      double hitX;
      double hitZ;

      FlagAggregate(long chunkKey) {
         this.chunkKey = chunkKey;
      }
   }

   public final class Geode {
      private List<class_2338> cells;
      private Set<Long> chunks;
      private double score;
      private double centroidX;
      private double centroidZ;

      public Geode(List<class_2338> cells, Set<Long> chunks, double score, double centroidX, double centroidZ) {
         this.cells = cells;
         this.chunks = chunks;
         this.score = score;
         this.centroidX = centroidX;
         this.centroidZ = centroidZ;
      }

      public List<class_2338> cells() {
         return this.cells;
      }

      public Set<Long> chunks() {
         return this.chunks;
      }

      public double score() {
         return this.score;
      }

      public double centroidX() {
         return this.centroidX;
      }

      public double centroidZ() {
         return this.centroidZ;
      }
   }

   public static enum SignalType {
      AMETHYST((double)6.0F, 16),
      KELP((double)2.0F, 6),
      BAMBOO((double)2.0F, 6),
      BERRIES((double)2.0F, 5),
      VINES((double)2.0F, 6),
      DRIPSTONE((double)2.0F, 5);

      public double weight;
      public int cap;

      private SignalType(double weight, int cap) {
         this.weight = weight;
         this.cap = cap;
      }


   }

   public final class Zone {
      private Set<Long> members;
      private double centroidX;
      private double centroidZ;
      private double totalScore;
      private double maxScore;

      public Zone(Set<Long> members, double centroidX, double centroidZ, double totalScore, double maxScore) {
         this.members = members;
         this.centroidX = centroidX;
         this.centroidZ = centroidZ;
         this.totalScore = totalScore;
         this.maxScore = maxScore;
      }

      public Set<Long> members() {
         return this.members;
      }

      public double centroidX() {
         return this.centroidX;
      }

      public double centroidZ() {
         return this.centroidZ;
      }

      public double totalScore() {
         return this.totalScore;
      }

      public double maxScore() {
         return this.maxScore;
      }
   }
}
