package dev.sol.license;

public class LicenseException extends RuntimeException {
   public LicenseException(String message) {
      super("Client license: " + message);
   }

   public LicenseException(String message, Throwable cause) {
      super("Client license: " + message, cause);
   }
}
