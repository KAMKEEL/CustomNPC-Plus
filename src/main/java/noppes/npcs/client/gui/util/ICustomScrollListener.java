package noppes.npcs.client.gui.util;


public interface ICustomScrollListener {

    void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll);

    default void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }
}
