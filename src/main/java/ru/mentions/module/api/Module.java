package ru.mentions.module.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.NotImplementedException;
import ru.mentions.Client;
import ru.mentions.module.api.settings.Setting;
import ru.mentions.utils.RussianUi;
import ru.mentions.utils.animation.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.other.NotificationSound;

@Environment(EnvType.CLIENT)
public abstract class Module implements IMinecraft {
   private final List<Setting<?>> settings = new ObjectArrayList();
   private ModuleInfo moduleInfo;
   private String name;
   private String bigName;
   private String desc;
   private Category category;
   private boolean enabled;
   private boolean autoEnabled;
   private boolean allowDisable;
   private boolean hidden;
   private int key;
   public final Animation animation_new = new Animation();
   public final Animation animation_new_1 = new Animation();
   public final Animation toggleAnimation = new Animation();
   public final Animation hoverModule = new Animation();
   public final Animation expandedAnimation = new Animation();
   public final Animation animtoP7 = new Animation();
   public final Animation animtoP8 = new Animation();
   public ru.mentions.utils.animation.satoshi.Animation animation = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.satoshi.Animation animation1 = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.satoshi.Animation animation2 = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.satoshi.Animation animation3 = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.satoshi.Animation animation12 = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.satoshi.Animation animation14 = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.satoshi.Animation animation15 = new EaseInOutQuad(300, 1.0);
   public ru.mentions.utils.animation.satoshi.Animation animation16 = new EaseInOutQuad(300, 1.0);
   private boolean expanded;

   public Module() {
      Class<?> clazz = this.getClass();
      ModuleInfo moduleInfo = clazz.getAnnotation(ModuleInfo.class);
      if (moduleInfo == null) {
         throw new NotImplementedException("@ModuleInfo annotation not found on " + clazz.getSimpleName());
      } else {
         this.moduleInfo = moduleInfo;
         this.name = moduleInfo.name().trim().replaceAll(" ", "");
         this.bigName = moduleInfo.name();
         this.desc = moduleInfo.desc();
         this.category = moduleInfo.category();
         this.autoEnabled = moduleInfo.autoEnabled();
         this.allowDisable = moduleInfo.allowDisable();
         this.hidden = moduleInfo.hidden();
         this.key = moduleInfo.key();
         this.setup();
      }
   }

   public void toggle() {
      this.setEnabled(!this.enabled);
   }

   public void setEnabled(boolean enabled) {
      this.setEnabled(enabled, true);
   }

   public void setEnabled(boolean enabled, boolean notification) {
      if (this.enabled != enabled && (this.allowDisable || enabled)) {
         this.enabled = enabled;
         if (enabled) {
            this.superEnable();
            if (mc.field_1724 != null && notification) {
               NotificationSound.playToggle(true);
            }
         } else {
            this.superDisable();
            if (mc.field_1724 != null && notification) {
               NotificationSound.playToggle(false);
            }
         }
      }
   }

   private void eventEnable() {
      Client.eventHandler().subscribe(this);
   }

   private void eventDisable() {
      Client.eventHandler().unsubscribe(this);
   }

   private void superEnable() {
      if (mc.field_1724 != null) {
         this.onEnable();
      }

      this.eventEnable();
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public void setExpanded(boolean expanded) {
      this.expanded = expanded;
   }

   private void superDisable() {
      if (mc.field_1724 != null) {
         this.onDisable();
      }

      this.eventDisable();
   }

   public void setup() {
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   @Generated
   public List<Setting<?>> getSettings() {
      return this.settings;
   }

   @Generated
   public ModuleInfo getModuleInfo() {
      return this.moduleInfo;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getBigName() {
      return this.bigName;
   }

   @Generated
   public String getDesc() {
      try {
         return RussianUi.text(this.desc);
      } catch (Throwable var2) {
         return this.desc != null ? this.desc : "";
      }
   }

   @Generated
   public Category getCategory() {
      return this.category;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean isAutoEnabled() {
      return this.autoEnabled;
   }

   @Generated
   public boolean isAllowDisable() {
      return this.allowDisable;
   }

   @Generated
   public boolean isHidden() {
      return this.hidden;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public Animation getAnimation_new() {
      return this.animation_new;
   }

   @Generated
   public Animation getAnimation_new_1() {
      return this.animation_new_1;
   }

   @Generated
   public Animation getToggleAnimation() {
      return this.toggleAnimation;
   }

   @Generated
   public Animation getHoverModule() {
      return this.hoverModule;
   }

   @Generated
   public Animation getExpandedAnimation() {
      return this.expandedAnimation;
   }

   @Generated
   public Animation getAnimtoP7() {
      return this.animtoP7;
   }

   @Generated
   public Animation getAnimtoP8() {
      return this.animtoP8;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation1() {
      return this.animation1;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation2() {
      return this.animation2;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation3() {
      return this.animation3;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation12() {
      return this.animation12;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation14() {
      return this.animation14;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation15() {
      return this.animation15;
   }

   @Generated
   public ru.mentions.utils.animation.satoshi.Animation getAnimation16() {
      return this.animation16;
   }

   @Generated
   public void setModuleInfo(ModuleInfo moduleInfo) {
      this.moduleInfo = moduleInfo;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setBigName(String bigName) {
      this.bigName = bigName;
   }

   @Generated
   public void setDesc(String desc) {
      this.desc = desc;
   }

   @Generated
   public void setCategory(Category category) {
      this.category = category;
   }

   @Generated
   public void setAutoEnabled(boolean autoEnabled) {
      this.autoEnabled = autoEnabled;
   }

   @Generated
   public void setAllowDisable(boolean allowDisable) {
      this.allowDisable = allowDisable;
   }

   @Generated
   public void setHidden(boolean hidden) {
      this.hidden = hidden;
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }

   @Generated
   public void setAnimation(ru.mentions.utils.animation.satoshi.Animation animation) {
      this.animation = animation;
   }

   @Generated
   public void setAnimation1(ru.mentions.utils.animation.satoshi.Animation animation1) {
      this.animation1 = animation1;
   }

   @Generated
   public void setAnimation2(ru.mentions.utils.animation.satoshi.Animation animation2) {
      this.animation2 = animation2;
   }

   @Generated
   public void setAnimation3(ru.mentions.utils.animation.satoshi.Animation animation3) {
      this.animation3 = animation3;
   }

   @Generated
   public void setAnimation12(ru.mentions.utils.animation.satoshi.Animation animation12) {
      this.animation12 = animation12;
   }

   @Generated
   public void setAnimation14(ru.mentions.utils.animation.satoshi.Animation animation14) {
      this.animation14 = animation14;
   }

   @Generated
   public void setAnimation15(ru.mentions.utils.animation.satoshi.Animation animation15) {
      this.animation15 = animation15;
   }

   @Generated
   public void setAnimation16(ru.mentions.utils.animation.satoshi.Animation animation16) {
      this.animation16 = animation16;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Module other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.isEnabled() != other.isEnabled()) {
         return false;
      } else if (this.isAutoEnabled() != other.isAutoEnabled()) {
         return false;
      } else if (this.isAllowDisable() != other.isAllowDisable()) {
         return false;
      } else if (this.isHidden() != other.isHidden()) {
         return false;
      } else if (this.getKey() != other.getKey()) {
         return false;
      } else if (this.isExpanded() != other.isExpanded()) {
         return false;
      } else {
         List<Setting<?>> this$settings = this.getSettings();
         List<Setting<?>> other$settings = other.getSettings();
         if (this$settings == null ? other$settings == null : this$settings.equals(other$settings)) {
            ModuleInfo this$moduleInfo = this.getModuleInfo();
            ModuleInfo other$moduleInfo = other.getModuleInfo();
            if (this$moduleInfo == null ? other$moduleInfo == null : this$moduleInfo.equals(other$moduleInfo)) {
               String this$name = this.getName();
               String other$name = other.getName();
               if (this$name == null ? other$name == null : this$name.equals(other$name)) {
                  String this$bigName = this.getBigName();
                  String other$bigName = other.getBigName();
                  if (this$bigName == null ? other$bigName == null : this$bigName.equals(other$bigName)) {
                     String this$desc = this.getDesc();
                     String other$desc = other.getDesc();
                     if (this$desc == null ? other$desc == null : this$desc.equals(other$desc)) {
                        Category this$category = this.getCategory();
                        Category other$category = other.getCategory();
                        if (this$category == null ? other$category == null : this$category.equals(other$category)) {
                           Animation this$animation_new = this.getAnimation_new();
                           Animation other$animation_new = other.getAnimation_new();
                           if (this$animation_new == null ? other$animation_new == null : this$animation_new.equals(other$animation_new)) {
                              Animation this$animation_new_1 = this.getAnimation_new_1();
                              Animation other$animation_new_1 = other.getAnimation_new_1();
                              if (this$animation_new_1 == null ? other$animation_new_1 == null : this$animation_new_1.equals(other$animation_new_1)) {
                                 Animation this$toggleAnimation = this.getToggleAnimation();
                                 Animation other$toggleAnimation = other.getToggleAnimation();
                                 if (this$toggleAnimation == null ? other$toggleAnimation == null : this$toggleAnimation.equals(other$toggleAnimation)) {
                                    Animation this$hoverModule = this.getHoverModule();
                                    Animation other$hoverModule = other.getHoverModule();
                                    if (this$hoverModule == null ? other$hoverModule == null : this$hoverModule.equals(other$hoverModule)) {
                                       Animation this$expandedAnimation = this.getExpandedAnimation();
                                       Animation other$expandedAnimation = other.getExpandedAnimation();
                                       if (this$expandedAnimation == null
                                          ? other$expandedAnimation == null
                                          : this$expandedAnimation.equals(other$expandedAnimation)) {
                                          Animation this$animtoP7 = this.getAnimtoP7();
                                          Animation other$animtoP7 = other.getAnimtoP7();
                                          if (this$animtoP7 == null ? other$animtoP7 == null : this$animtoP7.equals(other$animtoP7)) {
                                             Animation this$animtoP8 = this.getAnimtoP8();
                                             Animation other$animtoP8 = other.getAnimtoP8();
                                             if (this$animtoP8 == null ? other$animtoP8 == null : this$animtoP8.equals(other$animtoP8)) {
                                                ru.mentions.utils.animation.satoshi.Animation this$animation = this.getAnimation();
                                                ru.mentions.utils.animation.satoshi.Animation other$animation = other.getAnimation();
                                                if (this$animation == null ? other$animation == null : this$animation.equals(other$animation)) {
                                                   ru.mentions.utils.animation.satoshi.Animation this$animation1 = this.getAnimation1();
                                                   ru.mentions.utils.animation.satoshi.Animation other$animation1 = other.getAnimation1();
                                                   if (this$animation1 == null ? other$animation1 == null : this$animation1.equals(other$animation1)) {
                                                      ru.mentions.utils.animation.satoshi.Animation this$animation2 = this.getAnimation2();
                                                      ru.mentions.utils.animation.satoshi.Animation other$animation2 = other.getAnimation2();
                                                      if (this$animation2 == null ? other$animation2 == null : this$animation2.equals(other$animation2)) {
                                                         ru.mentions.utils.animation.satoshi.Animation this$animation3 = this.getAnimation3();
                                                         ru.mentions.utils.animation.satoshi.Animation other$animation3 = other.getAnimation3();
                                                         if (this$animation3 == null ? other$animation3 == null : this$animation3.equals(other$animation3)) {
                                                            ru.mentions.utils.animation.satoshi.Animation this$animation12 = this.getAnimation12();
                                                            ru.mentions.utils.animation.satoshi.Animation other$animation12 = other.getAnimation12();
                                                            if (this$animation12 == null
                                                               ? other$animation12 == null
                                                               : this$animation12.equals(other$animation12)) {
                                                               ru.mentions.utils.animation.satoshi.Animation this$animation14 = this.getAnimation14();
                                                               ru.mentions.utils.animation.satoshi.Animation other$animation14 = other.getAnimation14();
                                                               if (this$animation14 == null
                                                                  ? other$animation14 == null
                                                                  : this$animation14.equals(other$animation14)) {
                                                                  ru.mentions.utils.animation.satoshi.Animation this$animation15 = this.getAnimation15();
                                                                  ru.mentions.utils.animation.satoshi.Animation other$animation15 = other.getAnimation15();
                                                                  if (this$animation15 == null
                                                                     ? other$animation15 == null
                                                                     : this$animation15.equals(other$animation15)) {
                                                                     ru.mentions.utils.animation.satoshi.Animation this$animation16 = this.getAnimation16();
                                                                     ru.mentions.utils.animation.satoshi.Animation other$animation16 = other.getAnimation16();
                                                                     return this$animation16 == null
                                                                        ? other$animation16 == null
                                                                        : this$animation16.equals(other$animation16);
                                                                  } else {
                                                                     return false;
                                                                  }
                                                               } else {
                                                                  return false;
                                                               }
                                                            } else {
                                                               return false;
                                                            }
                                                         } else {
                                                            return false;
                                                         }
                                                      } else {
                                                         return false;
                                                      }
                                                   } else {
                                                      return false;
                                                   }
                                                } else {
                                                   return false;
                                                }
                                             } else {
                                                return false;
                                             }
                                          } else {
                                             return false;
                                          }
                                       } else {
                                          return false;
                                       }
                                    } else {
                                       return false;
                                    }
                                 } else {
                                    return false;
                                 }
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Module;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + (this.isEnabled() ? 79 : 97);
      result = result * 59 + (this.isAutoEnabled() ? 79 : 97);
      result = result * 59 + (this.isAllowDisable() ? 79 : 97);
      result = result * 59 + (this.isHidden() ? 79 : 97);
      result = result * 59 + this.getKey();
      result = result * 59 + (this.isExpanded() ? 79 : 97);
      List<Setting<?>> $settings = this.getSettings();
      result = result * 59 + ($settings == null ? 43 : $settings.hashCode());
      ModuleInfo $moduleInfo = this.getModuleInfo();
      result = result * 59 + ($moduleInfo == null ? 43 : $moduleInfo.hashCode());
      String $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      String $bigName = this.getBigName();
      result = result * 59 + ($bigName == null ? 43 : $bigName.hashCode());
      String $desc = this.getDesc();
      result = result * 59 + ($desc == null ? 43 : $desc.hashCode());
      Category $category = this.getCategory();
      result = result * 59 + ($category == null ? 43 : $category.hashCode());
      Animation $animation_new = this.getAnimation_new();
      result = result * 59 + ($animation_new == null ? 43 : $animation_new.hashCode());
      Animation $animation_new_1 = this.getAnimation_new_1();
      result = result * 59 + ($animation_new_1 == null ? 43 : $animation_new_1.hashCode());
      Animation $toggleAnimation = this.getToggleAnimation();
      result = result * 59 + ($toggleAnimation == null ? 43 : $toggleAnimation.hashCode());
      Animation $hoverModule = this.getHoverModule();
      result = result * 59 + ($hoverModule == null ? 43 : $hoverModule.hashCode());
      Animation $expandedAnimation = this.getExpandedAnimation();
      result = result * 59 + ($expandedAnimation == null ? 43 : $expandedAnimation.hashCode());
      Animation $animtoP7 = this.getAnimtoP7();
      result = result * 59 + ($animtoP7 == null ? 43 : $animtoP7.hashCode());
      Animation $animtoP8 = this.getAnimtoP8();
      result = result * 59 + ($animtoP8 == null ? 43 : $animtoP8.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation = this.getAnimation();
      result = result * 59 + ($animation == null ? 43 : $animation.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation1 = this.getAnimation1();
      result = result * 59 + ($animation1 == null ? 43 : $animation1.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation2 = this.getAnimation2();
      result = result * 59 + ($animation2 == null ? 43 : $animation2.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation3 = this.getAnimation3();
      result = result * 59 + ($animation3 == null ? 43 : $animation3.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation12 = this.getAnimation12();
      result = result * 59 + ($animation12 == null ? 43 : $animation12.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation14 = this.getAnimation14();
      result = result * 59 + ($animation14 == null ? 43 : $animation14.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation15 = this.getAnimation15();
      result = result * 59 + ($animation15 == null ? 43 : $animation15.hashCode());
      ru.mentions.utils.animation.satoshi.Animation $animation16 = this.getAnimation16();
      return result * 59 + ($animation16 == null ? 43 : $animation16.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "Module(settings="
         + this.getSettings()
         + ", moduleInfo="
         + this.getModuleInfo()
         + ", name="
         + this.getName()
         + ", bigName="
         + this.getBigName()
         + ", desc="
         + this.getDesc()
         + ", category="
         + this.getCategory()
         + ", enabled="
         + this.isEnabled()
         + ", autoEnabled="
         + this.isAutoEnabled()
         + ", allowDisable="
         + this.isAllowDisable()
         + ", hidden="
         + this.isHidden()
         + ", key="
         + this.getKey()
         + ", animation_new="
         + this.getAnimation_new()
         + ", animation_new_1="
         + this.getAnimation_new_1()
         + ", toggleAnimation="
         + this.getToggleAnimation()
         + ", hoverModule="
         + this.getHoverModule()
         + ", expandedAnimation="
         + this.getExpandedAnimation()
         + ", animtoP7="
         + this.getAnimtoP7()
         + ", animtoP8="
         + this.getAnimtoP8()
         + ", animation="
         + this.getAnimation()
         + ", animation1="
         + this.getAnimation1()
         + ", animation2="
         + this.getAnimation2()
         + ", animation3="
         + this.getAnimation3()
         + ", animation12="
         + this.getAnimation12()
         + ", animation14="
         + this.getAnimation14()
         + ", animation15="
         + this.getAnimation15()
         + ", animation16="
         + this.getAnimation16()
         + ", expanded="
         + this.isExpanded()
         + ")";
   }
}
