package dev.sol.mixin;

import dev.sol.SolClient;
import dev.sol.module.ModuleManager;
import dev.sol.render.BlockEntityEspRenderer;
import dev.sol.render.BlockEspRenderer;
import dev.sol.render.BlockOutlineRenderer;
import dev.sol.render.ChunkFinderRenderer;
import dev.sol.render.EntityEspRenderer;
import dev.sol.render.HitParticleRenderer;
import dev.sol.render.HoleEspRenderer;
import dev.sol.render.JumpCircleRenderer;
import dev.sol.render.MiscBlockEspRenderer;
import dev.sol.render.StorageEspRenderer;
import dev.sol.render.SusChunkRenderer;
import net.minecraft.class_11658;
import net.minecraft.class_12074;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_761.class})
public class LevelRendererMixin {
   @Inject(
      method = {"method_62210(Lnet/minecraft/class_4597$class_4598;Lnet/minecraft/class_4587;ZLnet/minecraft/class_11658;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void solclient$customBlockOutline(class_4597.class_4598 bufferSource, class_4587 poseStack, boolean translucentPass, class_11658 levelRenderState, CallbackInfo ci) {
      ModuleManager modules = SolClient.modules();
      if (modules != null) {
         if (translucentPass && modules.susChunkFinder.isEnabled()) {
            SusChunkRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.susChunkFinder);
         }

         if (translucentPass && modules.chunkFinder != null && modules.chunkFinder.isEnabled()) {
            ChunkFinderRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.chunkFinder);
         }

         if (translucentPass && modules.storageEsp != null && modules.storageEsp.isEnabled()) {
            StorageEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.storageEsp);
         }

         if (translucentPass && modules.blockEsp != null && modules.blockEsp.isEnabled()) {
            BlockEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.blockEsp);
         }

         if (translucentPass && modules.blockEntityEsp != null && modules.blockEntityEsp.isEnabled()) {
            BlockEntityEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.blockEntityEsp);
         }

         if (translucentPass && modules.spawnerNametags != null && modules.spawnerNametags.isEnabled()) {
            MiscBlockEspRenderer.renderSpawners(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.spawnerNametags);
         }

         if (translucentPass && modules.debugHoleEsp != null && modules.debugHoleEsp.isEnabled()) {
            HoleEspRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.debugHoleEsp);
         }

         if (translucentPass && modules.playerEsp != null && modules.playerEsp.isEnabled()) {
            EntityEspRenderer.renderPlayers(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.playerEsp);
         }

         if (translucentPass && modules.mobEsp != null && modules.mobEsp.isEnabled()) {
            EntityEspRenderer.renderMobs(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.mobEsp);
         }

         if (translucentPass && modules.jumpCircles != null && modules.jumpCircles.isEnabled()) {
            JumpCircleRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.jumpCircles);
         }

         if (translucentPass && modules.hitParticles != null && modules.hitParticles.isEnabled()) {
            HitParticleRenderer.render(bufferSource, poseStack, levelRenderState.field_63082.field_63078, modules.hitParticles);
         }

         if (modules.freecam != null && modules.freecam.isActive()) {
            ci.cancel();
         } else if (modules.blockOutline.isEnabled()) {
            ci.cancel();
            class_12074 state = levelRenderState.field_63083;
            if (state != null && state.comp_4933() == translucentPass) {
               BlockOutlineRenderer.render(bufferSource, poseStack, state, levelRenderState.field_63082.field_63078, modules.blockOutline);
            }
         }
      }

   }
}
