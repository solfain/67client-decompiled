package dev.sol.render.anim;

public enum Easing {
   LINEAR {
      public float apply(float t) {
         return t;
      }
   },
   EASE_OUT_CUBIC {
      public float apply(float t) {
         float inv = 1.0F - t;
         return 1.0F - inv * inv * inv;
      }
   },
   EASE_IN_OUT_QUAD {
      public float apply(float t) {
         return t < 0.5F ? 2.0F * t * t : 1.0F - (float)Math.pow((double)(-2.0F * t + 2.0F), (double)2.0F) / 2.0F;
      }
   };

   public abstract float apply(float var1);


}
