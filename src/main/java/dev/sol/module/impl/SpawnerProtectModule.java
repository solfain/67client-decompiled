package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.settings.StringSetting;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_746;

public class SpawnerProtectModule extends Module {
   public final SliderSetting targetStackCount = (SliderSetting)this.addSetting(new SliderSetting("Stacks To Deposit", "How many full stacks of spawners to mine before stashing.", (double)3.0F, (double)1.0F, (double)30.0F, (double)1.0F));
   public final SliderSetting scanRange = (SliderSetting)this.addSetting(new SliderSetting("Trigger Range", "Horizontal distance a stranger triggers the routine.", (double)64.0F, (double)16.0F, (double)128.0F, (double)1.0F, "m"));
   public final SliderSetting rotationSpeed = (SliderSetting)this.addSetting(new SliderSetting("Rotation Speed", "Degrees per tick the look direction is nudged toward its target.", (double)15.0F, (double)1.0F, (double)30.0F, (double)1.0F));
   public final BooleanSetting detectBlockUpdates = (BooleanSetting)this.addSetting(new BooleanSetting("Detect Block Updates", "Detects distant players by their out-of-render block break/place packets.", true));
   public final StringSetting whitelist = (StringSetting)this.addSetting(new StringSetting("Whitelist", "Comma-separated names to ignore.", "", 256, "Steve, Alex"));
   public final SliderSetting breakRange = (SliderSetting)this.addSetting(new SliderSetting("Break Range", "How close a spawner must be before it is broken.", (double)5.5F, (double)1.0F, (double)8.0F, (double)0.5F, "m"));
   public final BooleanSetting doubleCheck = (BooleanSetting)this.addSetting(new BooleanSetting("Double Check", "Requires two distant breaks within the window before triggering.", true));
   public final SliderSetting doubleCheckWindow = (SliderSetting)this.addSetting(new SliderSetting("Double Check Window", "Time window for the double check.", (double)60.0F, (double)1.0F, (double)600.0F, (double)1.0F, "s"));
   public final StringSetting webhookUrl = (StringSetting)this.addSetting(new StringSetting("Webhook URL", "Optional Discord webhook for alerts.", "", 256, "https://discord.com/api/webhooks/..."));
   private final class_310 mc = class_310.method_1551();
   private State currentState;
   private class_2338 targetBlock;
   private class_2338 targetChest;
   private int chestTick;
   private int breakCooldown;
   private int lagWaitTicks;
   private boolean strangerDetected;
   private final Set<Integer> verifiedPlayers;
   private int blockBreakCounter;
   private long lastBreakTimestamp;
   private int shopSequence;
   private int shopTick;
   private int miningTicks;

   public SpawnerProtectModule() {
      super("SpawnerProtect", "Auto-salvages your spawners when a stranger approaches, then logs out.", Category.WORLD);
      this.currentState = SpawnerProtectModule.State.WAITING_FOR_STRANGER;
      this.targetBlock = null;
      this.targetChest = null;
      this.chestTick = 0;
      this.breakCooldown = 0;
      this.lagWaitTicks = 0;
      this.strangerDetected = false;
      this.verifiedPlayers = new HashSet();
      this.blockBreakCounter = 0;
      this.lastBreakTimestamp = 0L;
      this.shopSequence = 0;
      this.shopTick = 0;
      this.miningTicks = 0;
      SliderSetting var10000 = this.doubleCheckWindow;
      BooleanSetting var10001 = this.doubleCheck;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::get);
   }

   protected void onEnable() {
      this.resetModule();
   }

   protected void onDisable() {
      this.stopMovement();
      this.updateSneak(false);
   }

   private void resetModule() {
      this.currentState = SpawnerProtectModule.State.WAITING_FOR_STRANGER;
      this.strangerDetected = false;
      this.lagWaitTicks = 0;
      this.verifiedPlayers.clear();
      this.blockBreakCounter = 0;
      this.lastBreakTimestamp = 0L;
      this.resetState();
   }

   private void resetState() {
      this.targetChest = null;
      this.targetBlock = null;
      this.chestTick = 0;
      this.breakCooldown = 0;
      this.miningTicks = 0;
   }

   public boolean isTriggered() {
      return this.strangerDetected;
   }

   public String phase() {
      return this.currentState.name();
   }

   public boolean detectBlockUpdatesEnabled() {
      return (Boolean)this.detectBlockUpdates.get();
   }

   public void onBlockDestructionPacket(int breakerId, class_2338 pos) {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (!this.strangerDetected && player != null && level != null && !this.isNearSpawn() && breakerId != player.method_5628()) {
         class_1297 breaker = level.method_8469(breakerId);
         if (breaker instanceof class_1657) {
            class_1657 p = (class_1657)breaker;
            if (this.isWhitelisted(p.method_5477().getString())) {
               return;
            }
         } else if (breaker == null) {
            for(class_1657 px : level.method_18456()) {
               if (px.method_33571().method_1022(class_243.method_24953(pos)) < (double)8.0F && this.isWhitelisted(px.method_5477().getString())) {
                  return;
               }
            }
         }

         double dx = player.method_23317() - (double)pos.method_10263();
         double dz = player.method_23321() - (double)pos.method_10260();
         double horizontalDist = Math.sqrt(dx * dx + dz * dz);
         if (horizontalDist <= (Double)this.scanRange.get()) {
            this.strangerDetected = true;
            this.currentState = SpawnerProtectModule.State.WORKING;
            String name = breaker != null ? breaker.method_5477().getString() : "Invisible/Anti-ESP";
            this.warn("\ud83d\udea8 PACKET DETECT: " + name + " started breaking! (Horizontal: " + (int)horizontalDist + "m)");
            this.sendWebhook("\ud83d\udea8 **PACKET DETECT:** `" + name + "` started breaking! (Horizontal: " + (int)horizontalDist + "m, Y: " + pos.method_10264() + ").");
         }
      }

   }

   public void onServerBlockUpdate(class_2338 pos, class_2680 newState, boolean multi) {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (!this.strangerDetected && player != null && level != null && !this.isNearSpawn()) {
         for(class_1657 p : level.method_18456()) {
            if (p.method_33571().method_1022(class_243.method_24953(pos)) < (double)8.0F && this.isWhitelisted(p.method_5477().getString())) {
               return;
            }
         }

         if (pos.method_10264() > -64 && newState.method_26215()) {
            class_2680 oldState = level.method_8320(pos);
            if (!oldState.method_26215() && !(oldState.method_26204() instanceof class_2480)) {
               double distToPlayer = player.method_33571().method_1022(class_243.method_24953(pos));
               if (!(distToPlayer < (double)6.0F)) {
                  double dx = player.method_23317() - (double)pos.method_10263();
                  double dz = player.method_23321() - (double)pos.method_10260();
                  double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                  if (!(horizontalDist > (Double)this.scanRange.get())) {
                     if ((Boolean)this.doubleCheck.get()) {
                        long now = System.currentTimeMillis();
                        if (now - this.lastBreakTimestamp > (long)this.doubleCheckWindow.getInt() * 1000L) {
                           this.blockBreakCounter = 0;
                        }

                        ++this.blockBreakCounter;
                        this.lastBreakTimestamp = now;
                        if (this.blockBreakCounter < 2) {
                           return;
                        }
                     }

                     this.strangerDetected = true;
                     this.currentState = SpawnerProtectModule.State.WORKING;
                     String reason = multi ? "multi-block break" : "block break";
                     this.warn("\ud83d\udea8 REMOTE DETECT: a block broke nearby! (Horizontal: " + (int)horizontalDist + "m, Y: " + pos.method_10264() + ")");
                     this.sendWebhook("\ud83d\udea8 **REMOTE DETECT (" + reason + "):** an invisible or far-away player broke a block! (Horizontal: " + (int)horizontalDist + "m, Y: " + pos.method_10264() + ")");
                  }
               }
            }
         }
      }

   }

   public void onTick() {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null) {
         if (this.isNearSpawn()) {
            if (this.strangerDetected) {
               this.resetModule();
            }
         } else {
            if (!this.strangerDetected) {
               for(class_1657 p : level.method_18456()) {
                  if (p != player && !this.isWhitelisted(p.method_5477().getString())) {
                     double dx = player.method_23317() - p.method_23317();
                     double dz = player.method_23321() - p.method_23321();
                     double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                     if (!(horizontalDist > (Double)this.scanRange.get())) {
                        boolean verified = this.verifiedPlayers.contains(p.method_5628());
                        if (!verified && p.method_6115()) {
                           verified = true;
                           this.verifiedPlayers.add(p.method_5628());
                        }

                        if (verified) {
                           this.strangerDetected = true;
                           this.currentState = SpawnerProtectModule.State.WORKING;
                           String var10001 = p.method_5477().getString();
                           this.warn("⚠ PLAYER SPOTTED (verified): " + var10001 + " (Horizontal: " + (int)horizontalDist + "m)");
                           var10001 = p.method_5477().getString();
                           this.sendWebhook("⚠ **PLAYER SPOTTED (verified):** `" + var10001 + "` (Horizontal: " + (int)horizontalDist + "m, Y: " + p.method_31478() + ")!");
                           break;
                        }
                     }
                  }
               }
            }

            if (!this.strangerDetected) {
               for(class_1297 entity : level.method_18112()) {
                  if (entity.method_5864() == class_1299.field_6082) {
                     if (entity instanceof class_1676) {
                        class_1676 proj = (class_1676)entity;
                        class_1297 dx = proj.method_24921();
                        if (dx instanceof class_1657) {
                           class_1657 owner = (class_1657)dx;
                           if (this.isWhitelisted(owner.method_5477().getString())) {
                              continue;
                           }
                        }
                     }

                     double dx = player.method_23317() - entity.method_23317();
                     double dz = player.method_23321() - entity.method_23321();
                     double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                     if (horizontalDist <= (Double)this.scanRange.get()) {
                        this.strangerDetected = true;
                        this.currentState = SpawnerProtectModule.State.WORKING;
                        this.warn("⚠ ENDER PEARL SPOTTED! (Horizontal: " + (int)horizontalDist + "m)");
                        this.sendWebhook("⚠ **ENDER PEARL SPOTTED!** Someone threw a pearl. (Horizontal: " + (int)horizontalDist + "m)!");
                        break;
                     }
                  }
               }
            }

            if (this.currentState != SpawnerProtectModule.State.WAITING_FOR_STRANGER) {
               if (this.currentState != SpawnerProtectModule.State.OPENING_CHEST && this.currentState != SpawnerProtectModule.State.DEPOSITING_ITEMS && this.currentState != SpawnerProtectModule.State.BUYING_ECHEST) {
                  this.updateSneak(true);
               }

               this.handleRotation();
               switch (this.currentState.ordinal()) {
                  case 1 -> this.handleWorking();
                  case 2 -> this.handleGoingToChest();
                  case 3 -> this.handleOpeningChest();
                  case 4 -> this.handleDepositing();
                  case 5 -> this.handleFinalExit();
                  case 6 -> this.handleBuyingEChest();
                  case 7 -> this.handlePlacingEChest();
               }
            }
         }
      }

   }

   private void handleRotation() {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null) {
         class_243 targetPos = null;
         if (this.currentState == SpawnerProtectModule.State.WORKING) {
            class_1542 dropped = this.findDroppedSpawner();
            if (dropped != null) {
               targetPos = dropped.method_73189();
            } else {
               if (this.targetBlock == null || level.method_8320(this.targetBlock).method_26204() != class_2246.field_10260 || player.method_24515().method_10262(this.targetBlock) > (double)256.0F) {
                  this.targetBlock = this.findRandomBlock(class_2246.field_10260, 16);
                  this.miningTicks = 0;
               }

               if (this.targetBlock != null) {
                  targetPos = class_243.method_24953(this.targetBlock);
               }
            }
         } else if ((this.currentState == SpawnerProtectModule.State.GOING_TO_CHEST || this.currentState == SpawnerProtectModule.State.OPENING_CHEST) && this.targetChest != null) {
            targetPos = class_243.method_24953(this.targetChest);
         }

         if (targetPos != null) {
            this.smoothLook(targetPos);
         }
      }

   }

   private void smoothLook(class_243 target) {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         class_243 eyes = player.method_33571();
         double dx = target.field_1352 - eyes.field_1352;
         double dy = target.field_1351 - eyes.field_1351;
         double dz = target.field_1350 - eyes.field_1350;
         double dist = Math.sqrt(dx * dx + dz * dz);
         float targetYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
         float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
         float step = this.rotationSpeed.getFloat();
         player.method_36456(player.method_36454() + class_3532.method_15363(class_3532.method_15393(targetYaw - player.method_36454()), -step, step));
         player.method_36457(player.method_36455() + class_3532.method_15363(class_3532.method_15393(targetPitch - player.method_36455()), -step, step));
      }

   }

   private void handleWorking() {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null) {
         if (player.field_7512 != player.field_7498) {
            player.method_7346();
         }

         class_1542 dropped = this.findDroppedSpawner();
         if (dropped != null) {
            this.lagWaitTicks = 0;
            this.stopBreaking();
            this.mc.field_1690.field_1894.method_23481(true);
         } else if (this.getSpawnerCount() >= this.targetStackCount.getInt() * 64) {
            this.goToChest();
         } else {
            if (this.targetBlock == null || level.method_8320(this.targetBlock).method_26204() != class_2246.field_10260 || player.method_24515().method_10262(this.targetBlock) > (double)256.0F) {
               this.targetBlock = this.findRandomBlock(class_2246.field_10260, 16);
               this.miningTicks = 0;
            }

            if (this.targetBlock == null) {
               this.stopMovement();
               if (this.lagWaitTicks < 40) {
                  ++this.lagWaitTicks;
               } else if (this.getSpawnerCount() > 0) {
                  this.goToChest();
               } else {
                  this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
                  this.lagWaitTicks = 0;
               }
            } else {
               this.lagWaitTicks = 0;
               double dist = player.method_33571().method_1022(class_243.method_24953(this.targetBlock));
               if (dist <= (Double)this.breakRange.get()) {
                  this.mc.field_1690.field_1894.method_23481(false);
                  ++this.miningTicks;
                  if (this.miningTicks > 60) {
                     this.mc.field_1761.method_2896(player, class_1268.field_5808, new class_3965(class_243.method_24953(this.targetBlock), class_2350.field_11036, this.targetBlock, false));
                     player.method_6104(class_1268.field_5808);
                     this.miningTicks = 0;
                  }

                  if (this.breakCooldown <= 0) {
                     this.mc.field_1761.method_2902(this.targetBlock, class_2350.field_11036);
                     player.method_6104(class_1268.field_5808);
                     this.mc.field_1690.field_1886.method_23481(true);
                     this.breakCooldown = 6;
                  } else {
                     --this.breakCooldown;
                  }
               } else {
                  this.stopBreaking();
                  this.mc.field_1690.field_1894.method_23481(true);
               }
            }
         }
      }

   }

   private void goToChest() {
      this.stopMovement();
      this.targetChest = this.findNearestBlock(class_2246.field_10443, 4);
      if (this.targetChest != null) {
         this.currentState = SpawnerProtectModule.State.GOING_TO_CHEST;
      } else if (this.getEnderChestCount() > 0) {
         this.currentState = SpawnerProtectModule.State.PLACING_ECHEST;
         this.shopTick = 0;
      } else {
         this.currentState = SpawnerProtectModule.State.BUYING_ECHEST;
         this.shopSequence = 0;
         this.shopTick = 0;
      }

   }

   private void handleGoingToChest() {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         if (this.targetChest == null) {
            this.targetChest = this.findNearestBlock(class_2246.field_10443, 4);
         }

         if (this.targetChest == null) {
            this.currentState = SpawnerProtectModule.State.WORKING;
         } else {
            this.mc.field_1690.field_1894.method_23481(true);
            if (player.method_24515().method_19771(this.targetChest, (double)4.0F)) {
               this.stopMovement();
               this.currentState = SpawnerProtectModule.State.OPENING_CHEST;
            }
         }
      }

   }

   private void handleOpeningChest() {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         this.updateSneak(false);
         if (this.chestTick % 12 == 0 && this.targetChest != null) {
            this.mc.field_1761.method_2896(player, class_1268.field_5808, new class_3965(class_243.method_24953(this.targetChest), class_2350.field_11036, this.targetChest, false));
         }

         ++this.chestTick;
         if (player.field_7512 instanceof class_1707) {
            this.chestTick = 0;
            this.currentState = SpawnerProtectModule.State.DEPOSITING_ITEMS;
            this.lagWaitTicks = 0;
         }
      }

   }

   private void handleBuyingEChest() {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         ++this.shopTick;
         if (this.shopSequence > 0 && this.mc.field_1755 == null && this.shopTick > 60) {
            this.say("Shop screen closed, retrying...");
            this.shopSequence = 0;
            this.shopTick = 0;
         } else if (this.shopTick >= 30) {
            switch (this.shopSequence) {
               case 0:
                  this.say("Opening shop: /shop");
                  if (this.mc.method_1562() != null) {
                     this.mc.method_1562().method_45730("shop");
                  }

                  this.shopSequence = 1;
                  this.shopTick = 0;
                  break;
               case 1:
                  if (this.mc.field_1755 != null && this.screenTitle().contains("SHOP")) {
                     this.say("Shop: selecting the End category...");
                     this.clickSlot(11, class_1713.field_7790);
                     this.shopSequence = 2;
                     this.shopTick = 0;
                  }
                  break;
               case 2:
                  if (this.mc.field_1755 != null && this.screenTitle().contains("END")) {
                     this.say("Shop: selecting Ender Chest...");
                     this.clickSlot(9, class_1713.field_7790);
                     this.shopSequence = 3;
                     this.shopTick = 0;
                  }
                  break;
               case 3:
                  if (this.mc.field_1755 != null && this.screenTitle().contains("ENDER CHEST")) {
                     this.say("Shop: confirming purchase...");
                     this.clickSlot(25, class_1713.field_7790);
                     this.shopSequence = 4;
                     this.shopTick = 0;
                  }
                  break;
               case 4:
                  if (this.getEnderChestCount() > 0) {
                     this.say("Ender Chest purchased.");
                     player.method_7346();
                     this.currentState = SpawnerProtectModule.State.PLACING_ECHEST;
                     this.shopTick = 0;
                  } else if (this.shopTick > 100) {
                     this.say("Purchase failed (timeout).");
                     player.method_7346();
                     this.shopSequence = 0;
                     this.shopTick = 0;
                  }
            }
         }
      }

   }

   private void handlePlacingEChest() {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null) {
         ++this.shopTick;
         if (this.shopTick >= 10) {
            int slot = -1;

            for(int i = 0; i < 9; ++i) {
               if (player.method_31548().method_5438(i).method_7909() == class_2246.field_10443.method_8389()) {
                  slot = i;
                  break;
               }
            }

            if (slot == -1) {
               for(int ix = 9; ix < 36; ++ix) {
                  if (player.method_31548().method_5438(ix).method_7909() == class_2246.field_10443.method_8389()) {
                     this.mc.field_1761.method_2906(player.field_7512.field_7763, ix, 0, class_1713.field_7794, player);
                     this.shopTick = 0;
                     return;
                  }
               }

               this.currentState = SpawnerProtectModule.State.BUYING_ECHEST;
               this.shopSequence = 0;
            } else {
               player.method_31548().method_61496(slot);
               class_2338 p = player.method_24515();
               class_2338 placePos = null;

               for(class_2350 d : class_2350.values()) {
                  if (d != class_2350.field_11036 && d != class_2350.field_11033) {
                     class_2338 bp = p.method_10093(d);
                     if (level.method_8320(bp).method_45474()) {
                        placePos = bp;
                        break;
                     }
                  }
               }

               if (placePos != null) {
                  this.mc.field_1761.method_2896(player, class_1268.field_5808, new class_3965(class_243.method_24953(placePos), class_2350.field_11036, placePos, false));
                  player.method_6104(class_1268.field_5808);
                  this.targetChest = placePos;
                  this.currentState = SpawnerProtectModule.State.GOING_TO_CHEST;
               } else {
                  this.warn("No spot to place the Ender Chest!");
                  this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
                  this.lagWaitTicks = 0;
               }
            }
         }
      }

   }

   private void handleDepositing() {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         if (this.lagWaitTicks < 15) {
            ++this.lagWaitTicks;
         } else {
            class_1703 var3 = player.field_7512;
            if (var3 instanceof class_1707) {
               class_1707 handler = (class_1707)var3;
               int var8 = handler.field_7761.size() - 36;
               boolean hasSpace = false;

               for(int invStart = 0; invStart < var8; ++invStart) {
                  class_1799 stack = handler.method_7611(invStart).method_7677();
                  if (stack.method_7960() || stack.method_7909() == class_2246.field_10260.method_8389() && stack.method_7947() < stack.method_7914()) {
                     hasSpace = true;
                     break;
                  }
               }

               int invStartx = var8;

               for(int i = 0; i < 36; ++i) {
                  int slotId = invStartx + i;
                  if (handler.method_7611(slotId).method_7677().method_7909() == class_2246.field_10260.method_8389()) {
                     if (!hasSpace) {
                        this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
                        this.lagWaitTicks = 0;
                        return;
                     }

                     this.mc.field_1761.method_2906(handler.field_7763, slotId, 0, class_1713.field_7794, player);
                     return;
                  }
               }

               player.method_7346();
               if (this.findNearestBlock(class_2246.field_10260, 16) != null) {
                  this.currentState = SpawnerProtectModule.State.WORKING;
               } else {
                  this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
                  this.lagWaitTicks = 0;
               }
            }
         }
      }

   }

   private void handleFinalExit() {
      if (this.lagWaitTicks == 0) {
         this.sendWebhook("\ud83c\udfc1 **DONE.** Waiting for items to save...");
         this.say("Task done — waiting 2s for the server to save, then disconnecting...");
      }

      ++this.lagWaitTicks;
      if (this.lagWaitTicks > 40) {
         class_634 conn = this.mc.method_1562();
         if (conn != null) {
            conn.method_48296().method_10747(class_2561.method_43470("[SpawnerProtect] Task complete, safe disconnect."));
         }

         this.resetModule();
      }

   }

   private void updateSneak(boolean sneak) {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         player.method_5660(sneak);
      }

      this.mc.field_1690.field_1832.method_23481(sneak);
   }

   private boolean isNearSpawn() {
      class_746 player = this.mc.field_1724;
      return player != null && Math.abs(player.method_23317()) < (double)100.0F && Math.abs(player.method_23321()) < (double)100.0F;
   }

   private boolean isWhitelisted(String name) {
      if (name != null && !name.isEmpty()) {
         for(String n : ((String)this.whitelist.get()).split(",")) {
            if (n.trim().equalsIgnoreCase(name)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private String screenTitle() {
      return this.mc.field_1755 == null ? "" : this.mc.field_1755.method_25440().getString().toUpperCase(Locale.ROOT);
   }

   private void clickSlot(int slot, class_1713 type) {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         this.mc.field_1761.method_2906(player.field_7512.field_7763, slot, 0, type, player);
      }

   }

   private class_2338 findNearestBlock(class_2248 block, int r) {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null) {
         class_2338 p = player.method_24515();
         class_2338 nearest = null;
         double minDist = Double.MAX_VALUE;

         for(int x = -r; x <= r; ++x) {
            for(int y = -r; y <= r; ++y) {
               for(int z = -r; z <= r; ++z) {
                  class_2338 bp = p.method_10069(x, y, z);
                  if (level.method_8320(bp).method_26204() == block) {
                     double d = p.method_10262(bp);
                     if (d < minDist) {
                        minDist = d;
                        nearest = bp;
                     }
                  }
               }
            }
         }

         return nearest;
      } else {
         return null;
      }
   }

   private class_2338 findRandomBlock(class_2248 block, int r) {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null) {
         class_2338 p = player.method_24515();
         List<class_2338> list = new ArrayList();
         double maxDistSq = (double)r * (double)r;

         for(int x = -r; x <= r; ++x) {
            for(int y = -r; y <= r; ++y) {
               for(int z = -r; z <= r; ++z) {
                  class_2338 bp = p.method_10069(x, y, z);
                  if (p.method_10262(bp) <= maxDistSq && level.method_8320(bp).method_26204() == block) {
                     list.add(bp);
                  }
               }
            }
         }

         return list.isEmpty() ? null : (class_2338)list.get((new Random()).nextInt(list.size()));
      } else {
         return null;
      }
   }

   private int getSpawnerCount() {
      return this.countItem(class_2246.field_10260.method_8389(), 36);
   }

   private int getEnderChestCount() {
      return this.countItem(class_2246.field_10443.method_8389(), 45);
   }

   private int countItem(class_1792 item, int slots) {
      class_746 player = this.mc.field_1724;
      if (player == null) {
         return 0;
      } else {
         int count = 0;

         for(int i = 0; i < slots; ++i) {
            class_1799 stack = player.method_31548().method_5438(i);
            if (stack.method_7909() == item) {
               count += stack.method_7947();
            }
         }

         return count;
      }
   }

   private class_1542 findDroppedSpawner() {
      class_746 player = this.mc.field_1724;
      class_638 level = this.mc.field_1687;
      if (player != null && level != null) {
         class_1542 best = null;
         double bestDist = Double.MAX_VALUE;

         for(class_1297 e : level.method_18112()) {
            if (e instanceof class_1542) {
               class_1542 item = (class_1542)e;
               if (item.method_6983().method_7909() == class_2246.field_10260.method_8389()) {
                  double d = (double)player.method_5739(item);
                  if (d < (double)16.0F && d < bestDist) {
                     bestDist = d;
                     best = item;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private void stopBreaking() {
      this.mc.field_1690.field_1886.method_23481(false);
      this.breakCooldown = 0;
   }

   private void stopMovement() {
      this.stopBreaking();
      this.mc.field_1690.field_1894.method_23481(false);
   }

   private void warn(String msg) {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         player.method_7353(class_2561.method_43470("§c[SpawnerProtect] §fnull"), false);
      }

      try {
         SolClient.notifications().pushInfo("SpawnerProtect · " + msg.replaceAll("§.", ""));
      } catch (Exception var4) {
      }

   }

   private void say(String msg) {
      class_746 player = this.mc.field_1724;
      if (player != null) {
         player.method_7353(class_2561.method_43470("§7[SpawnerProtect] null"), false);
      }

   }

   private void sendWebhook(String message) {
      String urlStr = "";
      if (!urlStr.isEmpty()) {
         String var10000 = message.replace("\\", "\\\\");
         String json = "{\"content\": \"" + var10000.replace("\"", "\\\"") + "\"}";
         (new Thread(() -> {
            try {
               HttpURLConnection conn = (HttpURLConnection)URI.create(urlStr).toURL().openConnection();
               conn.setRequestMethod("POST");
               conn.setRequestProperty("Content-Type", "application/json");
               conn.setDoOutput(true);
               OutputStream os = conn.getOutputStream();

               try {
                  os.write(json.getBytes(StandardCharsets.UTF_8));
               } catch (Throwable var7) {
                  if (os != null) {
                     try {
                        os.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (os != null) {
                  os.close();
               }

               conn.getInputStream().close();
            } catch (Exception var8) {
            }

         }, "SpawnerProtect-Webhook")).start();
      }

   }

   static enum State {
      WAITING_FOR_STRANGER,
      WORKING,
      GOING_TO_CHEST,
      OPENING_CHEST,
      DEPOSITING_ITEMS,
      FINAL_EXIT,
      BUYING_ECHEST,
      PLACING_ECHEST;


   }
}
