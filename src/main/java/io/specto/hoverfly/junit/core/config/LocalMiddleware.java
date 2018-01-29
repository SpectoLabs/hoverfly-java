package io.specto.hoverfly.junit.core.config;

public class LocalMiddleware {
   private final String binary;
   private final String path;

   LocalMiddleware(String binary, String path) {
      this.binary = binary;
      this.path = path;
   }

   public String getPath() {
      return path;
   }

   public String getBinary() {
      return binary;
   }


}
