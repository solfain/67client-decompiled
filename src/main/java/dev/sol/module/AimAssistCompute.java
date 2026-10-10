package dev.sol.module;

import dev.sol.module.impl.AimAssistModule;

public interface AimAssistCompute {
   double[] computePixels(AimAssistModule var1, double var2, double var4, double var6);

   void reset();
}
