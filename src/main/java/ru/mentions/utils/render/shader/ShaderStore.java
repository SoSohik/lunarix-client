package ru.mentions.utils.render.shader;

import com.mojang.blaze3d.shaders.ShaderType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

@Environment(EnvType.CLIENT)
public final class ShaderStore {
   private static final byte[] KEY = "Nightix//shader-veil//2026".getBytes(StandardCharsets.UTF_8);
   private static final String NAMESPACE = "client";
   private static final String INCLUDE_DIR = "shaders/include/";

   private ShaderStore() {
   }

   public static String getSource(class_2960 id, ShaderType type) {
      if (id != null && "client".equals(id.method_12836())) {
         String ext = type == ShaderType.VERTEX ? "vsh" : "fsh";
         String key = id.method_12832() + "|" + ext;
         String enc = ShaderData.SOURCES.get(key);
         if (enc == null) {
            return null;
         } else {
            String raw = decrypt(enc);
            class_2960 owner = class_2960.method_60655(id.method_12836(), "shaders/" + id.method_12832() + "." + ext);
            String resolved = resolveImports(raw, owner, new HashSet<>());
            return dedupeVersion(resolved);
         }
      } else {
         return null;
      }
   }

   private static String dedupeVersion(String source) {
      StringBuilder out = new StringBuilder(source.length());
      boolean versionSeen = false;

      for (String line : source.split("\n", -1)) {
         if (line.trim().startsWith("#version")) {
            if (versionSeen) {
               continue;
            }

            versionSeen = true;
         }

         out.append(line).append('\n');
      }

      return out.toString();
   }

   private static String decrypt(String base64) {
      byte[] data = Base64.getDecoder().decode(base64);
      byte[] out = new byte[data.length];

      for (int i = 0; i < data.length; i++) {
         out[i] = (byte)(data[i] ^ KEY[i % KEY.length]);
      }

      return new String(out, StandardCharsets.UTF_8);
   }

   private static String resolveImports(String source, class_2960 owner, Set<String> seen) {
      StringBuilder out = new StringBuilder(source.length() + 256);

      for (String line : source.split("\n", -1)) {
         String trimmed = line.trim();
         class_2960 inc;
         if (!trimmed.startsWith("#moj_import") || (inc = parseImport(trimmed, owner)) == null) {
            out.append(line).append('\n');
         } else {
            String incSrc;
            if (seen.add(inc.toString()) && (incSrc = readResource(inc)) != null) {
               out.append(resolveImports(incSrc, inc, seen));
               if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') {
                  out.append('\n');
               }
            }
         }
      }

      return out.toString();
   }

   private static class_2960 parseImport(String line, class_2960 owner) {
      int lt = line.indexOf(60);
      int gt = line.indexOf(62);
      if (lt >= 0 && gt > lt) {
         String ref = line.substring(lt + 1, gt).trim();
         class_2960 id = class_2960.method_60654(ref);
         return class_2960.method_60655(id.method_12836(), "shaders/include/" + id.method_12832());
      } else {
         int q1 = line.indexOf(34);
         int q2 = line.lastIndexOf(34);
         if (q1 >= 0 && q2 > q1) {
            String ref = line.substring(q1 + 1, q2).trim();
            String ownerPath = owner.method_12832();
            int slash = ownerPath.lastIndexOf(47);
            String dir = slash >= 0 ? ownerPath.substring(0, slash + 1) : "";
            return class_2960.method_60655(owner.method_12836(), dir + ref);
         } else {
            return null;
         }
      }
   }

   private static String readResource(class_2960 id) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.method_1478() != null) {
         Optional<?> res = mc.method_1478().method_14486(id);
         if (res.isEmpty()) {
            return null;
         } else {
            try {
               String var4;
               try (InputStream in = ((class_3298)res.get()).method_14482()) {
                  var4 = new String(in.readAllBytes(), StandardCharsets.UTF_8);
               }

               return var4;
            } catch (RuntimeException | IOException var8) {
               return null;
            }
         }
      } else {
         return null;
      }
   }
}
