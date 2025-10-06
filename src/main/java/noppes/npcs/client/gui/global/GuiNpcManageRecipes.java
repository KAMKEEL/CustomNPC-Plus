package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.recipe.RecipeGetPacket;
import kamkeel.npcs.network.packets.request.recipe.RecipeRemovePacket;
import kamkeel.npcs.network.packets.request.recipe.RecipeSavePacket;
import kamkeel.npcs.network.packets.request.recipe.RecipesGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.script.GuiScriptRecipe;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNpcManageRecipes extends GuiContainerNPCInterface2 implements IScrollData, IGuiData, ICustomScrollListener, ITextfieldListener, ISubGuiListener {
    public static int tab = 1;
    public GuiCustomScroll scroll;
    public HashMap<String, Integer> data = new HashMap<String, Integer>();
    private ContainerManageRecipes container;
    private String selected = null;
    private ResourceLocation slot;
    private String search = "";

    public GuiNpcManageRecipes(EntityNPCInterface npc, ContainerManageRecipes container) {
        super(npc, container);
        this.container = container;
        drawDefaultBackground = false;
        PacketClient.sendClient(new RecipesGetPacket(container.width));
        setBackground("inventorymenu.png");
        slot = getResource("slot.png");
        ySize = 200;

        if (container.width == 1) {
            tab = 2;
        } else if (container.width == 3) {
            tab = 0;
        } else {
            tab = 1;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        if (scroll == null)
            scroll = new GuiCustomScroll(this, 0, 0);
        scroll.setSize(130, 150);
        scroll.guiLeft = guiLeft + 280;
        scroll.guiTop = guiTop + 38;
        addScroll(scroll);

        int y = guiTop + 10;

        this.addButton(new GuiButtonBiDirectional(0, guiLeft + 280, y, 130, 20, new String[]{"menu.global", "tile.npcCarpentyBench.name", "tile.anvil.name"}, tab));

        y += 106;
        y += 44;

        this.addButton(new GuiNpcButton(3, guiLeft + 172, y, 50, 20, "gui.add"));
        this.addButton(new GuiNpcButton(4, guiLeft + 226, y, 50, 20, "gui.remove"));
        this.addButton(new GuiNpcButton(10, guiLeft + 226, y += 30, 50, 20, "gui.copy"));

        if (container.width != 1) {
            int buttonPos = guiTop + 72;
            this.addLabel(new GuiNpcLabel(0, "gui.ignoreDamage", guiLeft + 131, buttonPos));
            this.addButton(new GuiNpcButtonYesNo(5, guiLeft + 235, buttonPos - 5, 40, 20, container.recipe.ignoreDamage));
            this.addLabel(new GuiNpcLabel(1, "gui.ignoreNBT", guiLeft + 131, buttonPos += 22));
            this.addButton(new GuiNpcButtonYesNo(6, guiLeft + 235, buttonPos - 5, 40, 20, container.recipe.ignoreNBT));

            buttonPos += 22;
            this.addButton(new GuiNpcButton(15, guiLeft + 172, buttonPos - 5, 103, 20, "availability.options"));
            this.addButton(new GuiNpcButton(16, guiLeft + 172, buttonPos + 17, 103, 20, "script.scripts"));

            this.getButton(5).setEnabled(false);
            this.getButton(6).setEnabled(false);
            this.getButton(15).setEnabled(false);
            this.getButton(16).setEnabled(false);

            this.addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 8, guiTop + 8, 160, 20, container.recipe.name));
            this.getTextField(0).enabled = false;
        } else {
            int buttonPos = guiTop + 50;
            this.addLabel(new GuiNpcLabel(0, "gui.ignoreMatDamage", guiLeft + 131, buttonPos));
            this.addButton(new GuiNpcButtonYesNo(7, guiLeft + 235, buttonPos - 5, 40, 20, container.recipeAnvil.ignoreRepairMaterialDamage));


            this.addLabel(new GuiNpcLabel(1, "gui.ignoreMatNBT", guiLeft + 131, buttonPos += 22));
            this.addButton(new GuiNpcButtonYesNo(8, guiLeft + 235, buttonPos - 5, 40, 20, container.recipeAnvil.ignoreRepairMaterialNBT));

            this.addLabel(new GuiNpcLabel(11, "gui.repairPercent", guiLeft + 8, buttonPos));
            this.addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 90, buttonPos - 5, 35, 20, container.recipeAnvil.getRepairPercentage() + ""));
            this.getTextField(1).floatsOnly = true;
            this.getTextField(1).setMinMaxDefaultFloat(1, 100, container.recipeAnvil.getRepairPercentage());
            this.getTextField(1).enabled = false;

            this.addLabel(new GuiNpcLabel(2, "gui.ignoreItemNBT", guiLeft + 131, buttonPos += 22));
            this.addButton(new GuiNpcButtonYesNo(9, guiLeft + 235, buttonPos - 5, 40, 20, container.recipeAnvil.ignoreRepairItemNBT));

            this.addLabel(new GuiNpcLabel(12, "gui.xpCost", guiLeft + 8, buttonPos));
            this.addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 70, buttonPos - 5, 55, 20, container.recipeAnvil.getXpCost() + ""));
            this.getTextField(2).integersOnly = true;
            this.getTextField(2).setMinMaxDefault(0, Integer.MAX_VALUE, container.recipeAnvil.getXpCost());
            this.getTextField(2).enabled = false;

            buttonPos += 22;
            this.addButton(new GuiNpcButton(15, guiLeft + 172, buttonPos - 5, 103, 20, "availability.options"));
            this.addButton(new GuiNpcButton(16, guiLeft + 172, buttonPos + 17, 103, 20, "script.scripts"));

            this.getButton(7).setEnabled(false);
            this.getButton(8).setEnabled(false);
            this.getButton(9).setEnabled(false);
            this.getButton(15).setEnabled(false);
            this.getButton(16).setEnabled(false);

            this.addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 8, guiTop + 8, 160, 20, container.recipeAnvil.name));
            this.getTextField(0).enabled = false;
        }
        this.addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 280, guiTop + 8 + 3 + 180, 130, 20, search));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 0) {
            GuiButtonBiDirectional buttonBiDirectional = (GuiButtonBiDirectional) button;
            tab = buttonBiDirectional.getValue();
            if (tab == 0) {
                getTextField(55).setText("");
                search = "";
                scroll.clear();
                save();
                NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 3, 0, 0);
            } else if (tab == 1) {
                getTextField(55).setText("");
                search = "";
                scroll.clear();
                save();
                NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 4, 0, 0);
            } else {
                getTextField(55).setText("");
                search = "";
                scroll.clear();
                save();
                NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 1, 0, 0);
            }
        }
        if (button.id == 3) {
            save();
            scroll.clear();
            String name = "New";
            while (data.containsKey(name))
                name += "_";

            if (container.width == 1) {
                RecipeAnvil recipe = new RecipeAnvil();
                recipe.name = name;
                PacketClient.sendClient(new RecipeSavePacket(recipe.writeNBT()));
            } else {
                RecipeCarpentry recipe = new RecipeCarpentry(name);
                recipe.isGlobal = container.width == 3;
                PacketClient.sendClient(new RecipeSavePacket(recipe.writeNBT()));
            }
        }
        if (button.id == 4) {
            if (data.containsKey(scroll.getSelected())) {
                PacketClient.sendClient(new RecipeRemovePacket(data.get(scroll.getSelected()), container.width == 1));
                scroll.clear();
            }
        }
        if (button.id == 5) {
            container.recipe.ignoreDamage = button.getValue() == 1;
        }
        if (button.id == 6) {
            container.recipe.ignoreNBT = button.getValue() == 1;
        }
        if (button.id == 7) {
            container.recipeAnvil.ignoreRepairMaterialDamage = button.getValue() == 1;
        }
        if (button.id == 8) {
            container.recipeAnvil.ignoreRepairMaterialNBT = button.getValue() == 1;
        }
        if (button.id == 9) {
            container.recipeAnvil.ignoreRepairItemNBT = button.getValue() == 1;
        }
        if (button.id == 15) {
            save();
            if (container.width == 1) {
                setSubGui(new SubGuiNpcAvailability(container.recipeAnvil.availability));
            } else {
                setSubGui(new SubGuiNpcAvailability(container.recipe.availability));
            }
        }

        if (button.id == 16) {
            save();
            if (container.width == 1) {
                GuiScriptRecipe gui = new GuiScriptRecipe(this, container.recipeAnvil);
                gui.setWorldAndResolution(mc, width, height);
                gui.initGui();
                mc.currentScreen = gui;
            } else if (!container.recipe.isGlobal) {
                GuiScriptRecipe gui = new GuiScriptRecipe(this, container.recipe);
                gui.setWorldAndResolution(mc, width, height);
                gui.initGui();
                mc.currentScreen = gui;
            }
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scroll.resetScroll();
                scroll.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<String>(this.data.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : this.data.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("IsAnvil")) {
            RecipeAnvil recipe = new RecipeAnvil();
            recipe.readNBT(compound);
            container.setRecipe(recipe);
            container.width = 1;
            tab = 2;
            fixButtons();
        } else {
            RecipeCarpentry recipe = RecipeCarpentry.create(compound);
            recipe.readNBT(compound);
            container.setRecipe(recipe);
            fixButtons();
        }
    }

    private void fixButtons() {
        if (tab == 2) {
            getTextField(0).setText(container.recipeAnvil.name);
            getTextField(1).setText(container.recipeAnvil.getRepairPercentage() + "");
            getTextField(2).setText(container.recipeAnvil.getXpCost() + "");

            setSelected(container.recipeAnvil.name);
            getTextField(0).enabled = true;
            getTextField(1).enabled = true;
            getTextField(2).enabled = true;

            GuiNpcButtonYesNo btnMatDamage = (GuiNpcButtonYesNo) this.getButton(7);
            GuiNpcButtonYesNo btnMatNBT = (GuiNpcButtonYesNo) this.getButton(8);
            GuiNpcButtonYesNo btnItemNBT = (GuiNpcButtonYesNo) this.getButton(9);
            if (btnMatDamage != null) {
                btnMatDamage.setDisplay(container.recipeAnvil.ignoreRepairMaterialDamage ? 1 : 0);
                btnMatDamage.setEnabled(true);
            }
            if (btnMatNBT != null) {
                btnMatNBT.setDisplay(container.recipeAnvil.ignoreRepairMaterialNBT ? 1 : 0);
                btnMatNBT.setEnabled(true);
            }
            if (btnItemNBT != null) {
                btnItemNBT.setDisplay(container.recipeAnvil.ignoreRepairItemNBT ? 1 : 0);
                btnItemNBT.setEnabled(true);
            }

            GuiNpcButton avail = this.getButton(15);
            if (avail != null)
                avail.setEnabled(true);

            GuiNpcButton script = this.getButton(16);
            if (script != null)
                script.setEnabled(true);
        } else {
            getTextField(0).setText(container.recipe.name);

            this.getTextField(0).enabled = true;
            this.getButton(5).setEnabled(true);
            this.getButton(5).setDisplay(container.recipe.ignoreDamage ? 1 : 0);
            this.getButton(6).setEnabled(true);
            this.getButton(6).setDisplay(container.recipe.ignoreNBT ? 1 : 0);
            setSelected(container.recipe.name);

            if (!container.recipe.isGlobal) {
                GuiNpcButton avail = this.getButton(15);
                if (avail != null)
                    avail.setEnabled(true);

                GuiNpcButton script = this.getButton(16);
                if (script != null)
                    script.setEnabled(true);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(slot);

        for (int i = 0; i < container.width; i++) {
            for (int j = 0; j < container.width; j++) {
                drawTexturedModalRect(guiLeft + i * 18 + 7, guiTop + j * 18 + 34, 0, 0, 18, 18);
            }
        }

        if (container.width == 1) {
            drawTexturedModalRect(guiLeft + 101, guiTop + 34, 0, 0, 18, 18);
            this.fontRendererObj.drawString(StatCollector.translateToLocal("gui.material"), guiLeft + 28, guiTop + 38, CustomNpcResourceListener.DefaultTextColor);
        } else {
            drawTexturedModalRect(guiLeft + 86, guiTop + 60, 0, 0, 18, 18);
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = scroll.getSelected();
        this.data = data;
        scroll.setList(getSearchList());
        this.getTextField(0).enabled = name != null;

        if (container.width == 1) {
            this.getButton(7).setEnabled(name != null);
            this.getButton(8).setEnabled(name != null);
            this.getButton(9).setEnabled(name != null);
        } else {
            this.getButton(5).setEnabled(name != null);
            this.getButton(6).setEnabled(name != null);
        }

        if (name != null)
            scroll.setSelected(name);
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        scroll.setSelected(selected);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        save();
        selected = scroll.getSelected();
        if (container.width == 1) {
            PacketClient.sendClient(new RecipeGetPacket(data.get(selected), true));
        } else {
            PacketClient.sendClient(new RecipeGetPacket(data.get(selected), false));
        }
    }

    @Override
    public void save() {
        GuiNpcTextField.unfocus();
        if (selected != null && data.containsKey(selected)) {
            container.saveRecipe();
            if (container.width == 1) {
                PacketClient.sendClient(new RecipeSavePacket(container.recipeAnvil.writeNBT()));
            } else {
                PacketClient.sendClient(new RecipeSavePacket(container.recipe.writeNBT()));
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        if (guiNpcTextField.id == 0) {
            String name = guiNpcTextField.getText();
            if (!name.isEmpty() && !data.containsKey(name)) {
                if (container.width == 1) {
                    String old = container.recipeAnvil.name;
                    data.remove(container.recipeAnvil.name);
                    container.recipeAnvil.name = name;
                    data.put(container.recipeAnvil.name, container.recipeAnvil.id);
                    selected = name;
                    scroll.replace(old, container.recipeAnvil.name);
                } else {
                    String old = container.recipe.name;
                    data.remove(container.recipe.name);
                    container.recipe.name = name;
                    data.put(container.recipe.name, container.recipe.id);
                    selected = name;
                    scroll.replace(old, container.recipe.name);
                }
            }
        }
        if (guiNpcTextField.id == 1) {
            float percent = guiNpcTextField.getFloat();
            if (container.width == 1) {
                container.recipeAnvil.repairPercentage = percent;
            }
        }
        if (guiNpcTextField.id == 2) {
            int xpCost = guiNpcTextField.getInteger();
            if (container.width == 1) {
                container.recipeAnvil.xpCost = xpCost;
            }
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiNpcAvailability)
            fixButtons();
    }
}
