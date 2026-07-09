package somehussar.gui.guides;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.stats.AchievementList;

// TODO: THIS IS JUST THE CLIENT SIDE CONTROLLER
//       NEED SERVER SIDE
public class GuideController {
    private static boolean glint = true;
    private static boolean isGuideLoaded = true;

    public static boolean isGuideLoaded() {
        return isGuideLoaded;
    }

    public static void setGuideLoaded(boolean isGuideLoaded) {
        GuideController.isGuideLoaded = isGuideLoaded;
    }

    public static boolean glint() {
        return glint;
    }

    public static void handleWorldJoin() {
        if (glint()){
            GuiAchievement achievementGui = Minecraft.getMinecraft().guiAchievement;
            achievementGui.func_146255_b(AchievementList.openInventory);
            achievementGui.field_146265_j = "Check out guides in your inventory!";
            achievementGui.field_146268_i = "CNPC Guides!";
        }
    }

    public static void disableGlint() {
        glint = false;
        final long time = Minecraft.getSystemTime();
        GuiAchievement achievementGui = Minecraft.getMinecraft().guiAchievement;
        achievementGui.field_146263_l = (achievementGui.field_146263_l <= time - 3000 && achievementGui.field_146262_n ? time : Minecraft.getSystemTime() - (long) (3000.0D * 0.5D));
        achievementGui.field_146262_n = false;
        achievementGui.field_146265_j = "Guides explored!";
    }

    public static void resetGlint() {
        glint = true;
    }
}
