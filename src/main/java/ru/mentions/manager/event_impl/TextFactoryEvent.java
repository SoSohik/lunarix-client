package ru.mentions.manager.event_impl;

import java.util.regex.Pattern;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.manager.events.Event;

@Environment(EnvType.CLIENT)
public class TextFactoryEvent extends Event {
   private String text;

   public void replaceText(String protect, String replaced) {
      if (this.text != null && !this.text.isEmpty() && protect != null && !protect.isEmpty()) {
         if (this.text.toLowerCase().contains(protect.toLowerCase())) {
            this.text = this.text.replaceAll("(?ui)" + Pattern.quote(protect), replaced);
         }
      }
   }

   public void replaceRegex(String regex, String replaced) {
      if (this.text != null && !this.text.isEmpty()) {
         this.text = this.text.replaceAll(regex, replaced);
      }
   }

   @Generated
   public void setText(String text) {
      this.text = text;
   }

   @Generated
   public String getText() {
      return this.text;
   }

   @Generated
   public TextFactoryEvent(String text) {
      this.text = text;
   }
}
