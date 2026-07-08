package somehussar.gui.guides;


import tconstruct.client.tabs.InventoryTabGuides;
import tconstruct.client.tabs.TabRegistry;

// TODO: THIS IS JUST THE CLIENT SIDE CONTROLLER
// NEED SERVER SIDE
public class GuideController {
    private static boolean isGuideLoaded = true;

    public static boolean isGuideLoaded() {
        return isGuideLoaded;
    }

    public static void setGuideLoaded(boolean isGuideLoaded) {
        GuideController.isGuideLoaded = isGuideLoaded;
    }

    public static void forceGuideButtonToBeFirst() {
        TabRegistry.getTabList().add(0, new InventoryTabGuides());
    }
}
