package dev.sol.render;

import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public final class WorldProjection {
   private static final Matrix4f mvp = new Matrix4f();
   private static final Vector4f scratch = new Vector4f();
   private static final Matrix4f inverse = new Matrix4f();
   private static final Vector3f centerOrigin = new Vector3f();
   private static final Vector3f centerDir = new Vector3f();
   private static boolean centerValid;
   private static class_243 camPos;
   private static int fbWidth;
   private static int fbHeight;
   private static float partialTick;
   private static boolean valid;

   private WorldProjection() {
   }

   public static void capture(Matrix4f projection, float partialTick) {
      class_310 mc = class_310.method_1551();
      class_4184 camera = mc.field_1773.method_19418();
      if (camera == null) {
         valid = false;
      } else {
         Quaternionf view = camera.method_23767().conjugate(new Quaternionf());
         mvp.set(projection).rotate(view);
         camPos = camera.method_71156();
         class_276 target = mc.method_1522();
         fbWidth = target.field_1482;
         fbHeight = target.field_1481;
         WorldProjection.partialTick = partialTick;
         valid = true;
         updateCenter();
      }

   }

   private static void updateCenter() {
      centerValid = false;
      inverse.set(mvp);
      if (Math.abs(inverse.determinant()) < 1.0E-12F) {
         return;
      }

      inverse.invert();
      Vector4f near = new Vector4f(0.0F, 0.0F, -1.0F, 1.0F);
      Vector4f mid = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
      inverse.transform(near);
      inverse.transform(mid);
      if (Math.abs(near.w) < 1.0E-6F || Math.abs(mid.w) < 1.0E-6F) {
         return;
      }

      Vector3f a = new Vector3f(near.x / near.w, near.y / near.w, near.z / near.w);
      Vector3f b = new Vector3f(mid.x / mid.w, mid.y / mid.w, mid.z / mid.w);
      Vector3f dir = b.sub(a, new Vector3f());
      if (dir.lengthSquared() < 1.0E-12F || !Float.isFinite(dir.x) || !Float.isFinite(dir.y) || !Float.isFinite(dir.z)) {
         return;
      }

      dir.normalize();
      float along = -a.dot(dir);
      centerOrigin.set(a).fma(along, dir);
      centerDir.set(dir);
      centerValid = true;
   }

   public static boolean screenCenter(float distance, Vector3fc forward, Vector3f out) {
      if (!valid || !centerValid) {
         return false;
      } else {
         float sign = centerDir.dot(forward.x(), forward.y(), forward.z()) < 0.0F ? -1.0F : 1.0F;
         out.set(centerDir).mul(sign * distance).add(centerOrigin);
         return true;
      }
   }

   public static void invalidate() {
      valid = false;
      centerValid = false;
   }

   public static boolean isValid() {
      return valid;
   }

   public static float partialTick() {
      return partialTick;
   }

   public static float[] project(double wx, double wy, double wz) {
      float[] px = projectRaw(wx, wy, wz);
      if (px == null) {
         return null;
      } else {
         float scale = OverlayRenderer.uiScale();
         return new float[]{px[0] / scale, px[1] / scale};
      }
   }

   public static float[] projectRaw(double wx, double wy, double wz) {
      if (!valid) {
         return null;
      } else {
         scratch.set((float)(wx - camPos.field_1352), (float)(wy - camPos.field_1351), (float)(wz - camPos.field_1350), 1.0F);
         mvp.transform(scratch);
         if (scratch.w <= 1.0E-4F) {
            return null;
         } else {
            float ndcX = scratch.x / scratch.w;
            float ndcY = scratch.y / scratch.w;
            float px = (ndcX * 0.5F + 0.5F) * (float)fbWidth;
            float py = (1.0F - (ndcY * 0.5F + 0.5F)) * (float)fbHeight;
            return new float[]{px, py};
         }
      }
   }

   static {
      camPos = class_243.field_1353;
   }
}
