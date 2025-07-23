package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.DataStats;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumParticleType;
import noppes.npcs.constants.EnumPotionType;

public class SubGuiNpcProjectiles extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {
    private DataStats stats;
    private String[] potionNames = new String[]{"gui.none", "tile.fire.name", "potion.poison", "potion.hunger", "potion.weakness", "potion.moveSlowdown", "potion.confusion", "potion.blindness", "potion.wither"};
    private String[] trailNames = new String[]{"gui.none", "trail.smoke", "trail.portal", "trail.redstone",
        "trail.lightning", "trail.largesmoke", "trail.magic", "trail.enchant", "trail.crit",
        "trail.explode", "trail.music", "trail.flame", "trail.lava", "trail.splash",
        "trail.slime", "trail.heart", "trail.angryvillager", "trail.happyvillager", "trail.custom"
    };

    public SubGuiNpcProjectiles(DataStats stats) {
        this.stats = stats;
        setBackground("menubg.png");
        xSize = 390;
        ySize = 216;
        closeOnEsc = true;
    }

    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(1, "enchantment.arrowDamage", guiLeft + 5, guiTop + 15));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 45, guiTop + 10, 50, 20, String.format("%.0f", stats.pDamage) + ""));
        getTextField(1).setFloatsOnly();
        getTextField(1).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 5);

        int y = guiTop + 10;
        int second = guiLeft + 110;

        addLabel(new GuiNpcLabel(2, "enchantment.arrowKnockback", second, y + 5));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, second + 40, y, 50, 20, stats.pImpact + ""));
        getTextField(2).integersOnly = true;
        getTextField(2).setMinMaxDefault(0, Integer.MAX_VALUE, 0);

        addButton(new GuiNpcButton(6, guiLeft + 220, guiTop + 10, 60, 20, new String[]{"stats.noglow", "stats.glows"}, stats.pGlows ? 1 : 0));

        y += 30;

        addLabel(new GuiNpcLabel(3, "stats.size", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 45, y, 50, 20, stats.pSize + ""));
        getTextField(3).integersOnly = true;
        getTextField(3).setMinMaxDefault(1, Integer.MAX_VALUE, 10);

        addLabel(new GuiNpcLabel(4, "stats.speed", second, y + 5));
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, second + 40, y, 50, 20, stats.pSpeed + ""));
        getTextField(4).integersOnly = true;
        getTextField(4).setMinMaxDefault(1, Integer.MAX_VALUE, 10);

        y += 30;

        addLabel(new GuiNpcLabel(5, "stats.hasgravity", guiLeft + 5, y + 5));
        addButton(new GuiNpcButton(0, guiLeft + 80, y, 40, 20, new String[]{"gui.no", "gui.yes"}, stats.pPhysics ? 1 : 0));
        if (!stats.pPhysics) {
            addButton(new GuiNpcButton(1, guiLeft + 125, y, 60, 20, new String[]{"gui.constant", "gui.accelerate"}, stats.pXlr8 ? 1 : 0));
        }

        y += 30;

        addLabel(new GuiNpcLabel(6, "stats.explosive", guiLeft + 5, y + 5));
        addButton(new GuiNpcButton(2, guiLeft + 80, y, 40, 20, new String[]{"gui.no", "gui.yes"}, stats.pExplode ? 1 : 0));
        if (stats.pExplode) {
            addButton(new GuiNpcButton(3, guiLeft + 125, y, 60, 20, new String[]{"gui.none", "gui.small", "gui.medium", "gui.large"}, stats.pArea));
        }

        addLabel(new GuiNpcLabel(7, "stats.rangedeffect", guiLeft + 210, y + 5));
        addButton(new GuiButtonBiDirectional(4, guiLeft + 280, y, 100, 20, potionNames, stats.pEffect.ordinal()));

        if (stats.pEffect != EnumPotionType.None) {
            int internalY = y + 30;
            addLabel(new GuiNpcLabel(50, "gui.time", guiLeft + 210, internalY + 5));
            addTextField(new GuiNpcTextField(5, this, fontRendererObj, guiLeft + 330, internalY, 52, 20, stats.pDur + ""));
            getTextField(5).integersOnly = true;
            getTextField(5).setMinMaxDefault(1, Integer.MAX_VALUE, 5);
            if (stats.pEffect != EnumPotionType.Fire) {
                internalY += 30;
                addLabel(new GuiNpcLabel(70, "stats.amplify", guiLeft + 210, internalY + 5));
                addButton(new GuiButtonBiDirectional(10, guiLeft + 280, internalY, 52, 20, new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, stats.pEffAmp));
            } else {
                internalY += 30;
                addLabel(new GuiNpcLabel(100, "stats.burnItem", guiLeft + 210, internalY + 5));
                addButton(new GuiNpcButtonYesNo(100, guiLeft + 330, internalY, 52, 20, stats.pBurnItem));
            }
        }

        y += 30;

        addLabel(new GuiNpcLabel(8, "stats.trail", guiLeft + 5, y + 5));
        addButton(new GuiNpcButton(5, guiLeft + 80, y, 75, 20, trailNames, stats.pTrail.ordinal()));
        if (stats.pTrail == EnumParticleType.Custom) {
            int internalY = y + 30;
            addLabel(new GuiNpcLabel(90, "trail.custom", guiLeft + 5, internalY + 5));
            addButton(new GuiNpcButton(90, guiLeft + 80, internalY, 75, 20, "gui.edit"));
        }

        addButton(new GuiNpcButton(7, guiLeft + xSize - 40, guiTop + 10, 30, 20, new String[]{"2D", "3D"}, stats.pRender3D ? 1 : 0));
        if (stats.pRender3D) {
            addLabel(new GuiNpcLabel(10, "stats.spin", guiLeft + xSize - 80, guiTop + 45));
            addButton(new GuiNpcButton(8, guiLeft + xSize - 40, guiTop + 40, 30, 20, new String[]{"gui.no", "gui.yes"}, stats.pSpin ? 1 : 0));
            addLabel(new GuiNpcLabel(11, "stats.stick", guiLeft + xSize - 80, guiTop + 75));
            addButton(new GuiNpcButton(9, guiLeft + xSize - 40, guiTop + 70, 30, 20, new String[]{"gui.no", "gui.yes"}, stats.pStick ? 1 : 0));
        }
        addButton(new GuiNpcButton(66, guiLeft + xSize - 50, guiTop + 190, 40, 20, "gui.done"));
    }

    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 1) {
            stats.pDamage = (float) (Math.floor(Float.parseFloat(textfield.getText())));
        } else if (textfield.id == 2) {
            stats.pImpact = textfield.getInteger();
        } else if (textfield.id == 3) {
            stats.pSize = textfield.getInteger();
        } else if (textfield.id == 4) {
            stats.pSpeed = textfield.getInteger();
        } else if (textfield.id == 5) {
            stats.pDur = textfield.getInteger();
        }
    }

    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 0) {
            stats.pPhysics = (button.getValue() == 1);
            initGui();
        }
        if (button.id == 1) {
            stats.pXlr8 = (button.getValue() == 1);
        }
        if (button.id == 2) {
            stats.pExplode = (button.getValue() == 1);
            initGui();
        }
        if (button.id == 3) {
            stats.pArea = button.getValue();
        }
        if (button.id == 4) {
            stats.pEffect = EnumPotionType.values()[button.getValue()];
            initGui();
        }
        if (button.id == 5) {
            stats.pTrail = EnumParticleType.values()[button.getValue()];
            initGui();
        }
        if (button.id == 90) {
            setSubGui(new SubGuiScriptParticle(stats.pCustom));
        }
        if (button.id == 6) {
            stats.pGlows = (button.getValue() == 1);
        }
        if (button.id == 7) {
            stats.pRender3D = (button.getValue() == 1);
            initGui();
        }
        if (button.id == 8) {
            stats.pSpin = (button.getValue() == 1);
        }
        if (button.id == 9) {
            stats.pStick = (button.getValue() == 1);
        }
        if (button.id == 10) {
            stats.pEffAmp = button.getValue();
        }
        if (button.id == 100) {
            stats.pBurnItem = (button.getValue() == 1);
            initGui();
        }
        if (button.id == 66) {
            close();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {

    }
}
