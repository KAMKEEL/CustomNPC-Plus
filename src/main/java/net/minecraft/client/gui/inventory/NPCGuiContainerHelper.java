package net.minecraft.client.gui.inventory;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;

public class NPCGuiContainerHelper {

	public static int getLeft(GuiContainer gui) {
		return gui.guiLeft;
	}

	public static int getTop(GuiContainer gui) {
		return gui.guiTop;
	}
}
