package ru.mentions.utils.render;

import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class Scissor {
   private static final class_310 mc = class_310.method_1551();
   private static final Deque<int[]> scissorStack = new ArrayDeque<>();

   public static void enable(float x, float y, float width, float height, float guiScale) {
      DrawBatcher.flushPending();
      int windowHeight = mc.method_22683().method_4507();
      int scissorX = (int)(x * guiScale);
      int scissorY = (int)((float)windowHeight - (y + height) * guiScale);
      int scissorWidth = (int)(width * guiScale);
      int scissorHeight = (int)(height * guiScale);
      if (!scissorStack.isEmpty()) {
         int[] parent = scissorStack.peek();
         int parentX = parent[0];
         int parentY = parent[1];
         int parentX2 = parentX + parent[2];
         int parentY2 = parentY + parent[3];
         int newX2 = scissorX + scissorWidth;
         int newY2 = scissorY + scissorHeight;
         scissorX = Math.max(scissorX, parentX);
         scissorY = Math.max(scissorY, parentY);
         newX2 = Math.min(newX2, parentX2);
         newY2 = Math.min(newY2, parentY2);
         scissorWidth = Math.max(0, newX2 - scissorX);
         scissorHeight = Math.max(0, newY2 - scissorY);
      }

      scissorStack.push(new int[]{scissorX, scissorY, scissorWidth, scissorHeight});
      GL11.glEnable(3089);
      GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
   }

   public static void enable(float x, float y, float width, float height) {
      int currentGuiScale = mc.method_22683().method_4476((Integer)mc.field_1690.method_42474().method_41753(), mc.method_1573());
      enable(x, y, width, height, (float)currentGuiScale);
   }

   public static void disable() {
      DrawBatcher.flushPending();
      if (!scissorStack.isEmpty()) {
         scissorStack.pop();
      }

      if (scissorStack.isEmpty()) {
         GL11.glDisable(3089);
      } else {
         int[] parent = scissorStack.peek();
         GL11.glScissor(parent[0], parent[1], parent[2], parent[3]);
      }
   }

   public static void refresh() {
      DrawBatcher.flushPending();
      if (scissorStack.isEmpty()) {
         GL11.glDisable(3089);
      } else {
         int[] current = scissorStack.peek();
         GL11.glEnable(3089);
         GL11.glScissor(current[0], current[1], current[2], current[3]);
      }
   }

   public static void reset() {
      DrawBatcher.flushPending();
      scissorStack.clear();
      GL11.glDisable(3089);
   }
}
