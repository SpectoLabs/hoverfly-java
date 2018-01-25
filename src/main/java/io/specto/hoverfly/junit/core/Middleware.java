package io.specto.hoverfly.junit.core;

public class Middleware {
   private final String binary;
   private final String path;

   public Middleware(String binary, String path) {
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
