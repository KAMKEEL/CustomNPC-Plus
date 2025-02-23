package noppes.npcs.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import noppes.npcs.controllers.data.Quest;
import java.util.ArrayList;
import java.util.List;

public class QuestTrackingComponent extends HudComponent {
    private Minecraft mc;
    private ArrayList<String> questTitleLines = new ArrayList<>();
    private ArrayList<String> questCategoryLines = new ArrayList<>();
    private ArrayList<String> objectiveLines = new ArrayList<>();
    private ArrayList<String> turnInLines = new ArrayList<>();

    public QuestTrackingComponent(Minecraft mc) {
        this.mc = mc;
        load();
    }

    /**
     * Sets quest data similar to the original OverlayQuestTracking.
     */
    public void setQuestData(Quest quest, String category, List<String> objectives, String turnIn) {
        questTitleLines = splitLines(convertColorCodes(quest.getName()), overlayWidth - 10);
        questCategoryLines = splitLines(convertColorCodes(category), overlayWidth - 10);
        objectiveLines.clear();
        for (String obj : objectives) {
            objectiveLines.add(convertColorCodes(obj));
        }
        turnInLines.clear();
        if (turnIn != null && !turnIn.isEmpty()) {
            turnInLines.add(convertColorCodes(turnIn));
        }
    }

    /**
     * Loads overlay data from an NBT compound.
     */
    public void setOverlayData(NBTTagCompound compound) {
        Quest quest = new Quest();
        quest.readNBT(compound.getCompoundTag("Quest"));
        String category = compound.getString("CategoryName");

        ArrayList<String> objectives = new ArrayList<>();
        NBTTagList objectiveList = compound.getTagList("ObjectiveList", 8);
        for (int i = 0; i < objectiveList.tagCount(); i++) {
            String objective = objectiveList.getStringTagAt(i);
            objectives.add(objective);
        }
        String turnIn = "";
        boolean instantComplete = compound.getBoolean("Instant");
        if (instantComplete) {
            turnIn = "Completed automatically";
        } else {
            String npcName = compound.getString("TurnInNPC");
            if (!npcName.isEmpty()) {
                turnIn = "Complete with " + npcName;
            }
        }
        setQuestData(quest, category, objectives, turnIn);
        hasData = true;
    }

    @Override
    public void loadData(NBTTagCompound compound) {
        setOverlayData(compound);
    }

    @Override
    public void load() {
        posX = ConfigClient.QuestOverlayX;
        posY = ConfigClient.QuestOverlayY;
        scale = ConfigClient.QuestOverlayScale;
        textAlign = ConfigClient.QuestOverlayTextAlign;
    }

    @Override
    public void save() {
        ConfigClient.QuestOverlayX = posX;
        ConfigClient.QuestOverlayXProperty.set(ConfigClient.QuestOverlayX);

        ConfigClient.QuestOverlayY = posY;
        ConfigClient.QuestOverlayYProperty.set(ConfigClient.QuestOverlayY);

        ConfigClient.QuestOverlayScale = scale;
        ConfigClient.QuestOverlayScaleProperty.set(ConfigClient.QuestOverlayScale);

        ConfigClient.QuestOverlayTextAlign = textAlign;
        ConfigClient.QuestOverlayTextAlignProperty.set(ConfigClient.QuestOverlayTextAlign);

        if (ConfigClient.config.hasChanged()) {
            ConfigClient.config.save();
        }
    }

    @Override
    public void renderOnScreen(float partialTicks) {
        if (!hasData || isEditting)
            return;

        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int)(posX / 100F * res.getScaledWidth());
        int actualY = (int)(posY / 100F * res.getScaledHeight());
        float effectiveScale = getEffectiveScale(res);

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(effectiveScale, effectiveScale, effectiveScale);

        // Draw background (semi-transparent).
        drawRect(0, 0, overlayWidth, overlayHeight, 0x40FFFFFF);

        int currentY = 5;
        currentY = renderTextBlock(questTitleLines, currentY, textAlign, 0xFFFFFF);
        drawDecorativeLine(currentY - 1);
        currentY += 8;

        currentY = renderTextBlock(questCategoryLines, currentY, textAlign, 0xCCCCCC);
        drawDecorativeLine(currentY - 1);
        currentY += 8;

        currentY = renderTextBlock(objectiveLines, currentY, textAlign, 0xAAAAAA);
        if (!turnInLines.isEmpty()) {
            drawDecorativeLine(currentY - 1);
            currentY += 8;
        }
        renderTextBlock(turnInLines, currentY, textAlign, 0xAAAAAA);

        GL11.glPopMatrix();
    }

    @Override
    public void renderEditing() {
        isEditting = true;
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int)(posX / 100F * res.getScaledWidth());
        int actualY = (int)(posY / 100F * res.getScaledHeight());
        float effectiveScale = getEffectiveScale(res);

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(effectiveScale, effectiveScale, effectiveScale);

        // Draw editing background, border, and resize handle.
        drawRect(0, 0, overlayWidth, overlayHeight, 0x40FFFFFF);
        drawRectOutline(0, 0, overlayWidth, overlayHeight, 0xFFAAAAAA);
        drawRect(overlayWidth - 10, overlayHeight - 10, overlayWidth, overlayHeight, 0xFFCCCCCC);

        int currentY = 5;
        currentY = renderDemoTextBlock(new String[] { "Dummy Quest Title" }, currentY, textAlign, 0xFFFFFF);
        drawDecorativeLine(currentY - 1);
        currentY += 8;
        currentY = renderDemoTextBlock(new String[] { "Dummy Category" }, currentY, textAlign, 0xCCCCCC);
        drawDecorativeLine(currentY - 1);
        currentY += 8;
        currentY = renderDemoTextBlock(new String[] { "Objective: Kill 10 mobs", "Objective: Collect 5 items" }, currentY, textAlign, 0xAAAAAA);
        drawDecorativeLine(currentY - 1);
        currentY += 8;
        renderDemoTextBlock(new String[] { "Turn in with NPC" }, currentY, textAlign, 0xAAAAAA);

        GL11.glPopMatrix();
    }

    @Override
    public void addEditorButtons(List<GuiButton> buttonList) {
        // First add the default toggle button.
        super.addEditorButtons(buttonList);
        // Then add other custom buttons.
        buttonList.add(new GuiButton(1, 100, 0, 120, 20, getAlignText()));
        buttonList.add(new GuiButton(3, 100, 0, 120, 20, "Reset to Center"));
    }

    @Override
    public void onEditorButtonPressed(GuiButton button) {
        if (button.id == 1) { // Cycle alignment.
            textAlign = (textAlign + 1) % 3;
            button.displayString = getAlignText();
        } else if (button.id == 3) { // Reset to center.
            posX = 50;
            posY = 50;
        } else {
            super.onEditorButtonPressed(button);
        }
    }

    private String getAlignText() {
        switch (textAlign) {
            case 0: return "Align: Left";
            case 1: return "Align: Center";
            case 2: return "Align: Right";
            default: return "Align: Unknown";
        }
    }

    // ---------- Helper rendering methods -----------
    private int renderTextBlock(List<String> lines, int startY, int align, int color) {
        int y = startY;
        FontRenderer font = mc.fontRenderer;
        for (String line : lines) {
            int strWidth = font.getStringWidth(line);
            int xOffset = (align == 1) ? (overlayWidth - strWidth) / 2
                : (align == 2) ? overlayWidth - strWidth - 5 : 5;
            font.drawStringWithShadow(line, xOffset, y, color);
            y += font.FONT_HEIGHT + 4;
        }
        return y;
    }

    private int renderDemoTextBlock(String[] lines, int startY, int align, int color) {
        int y = startY;
        FontRenderer font = mc.fontRenderer;
        for (String line : lines) {
            int strWidth = font.getStringWidth(line);
            int xOffset = (align == 1) ? (overlayWidth - strWidth) / 2
                : (align == 2) ? overlayWidth - strWidth - 5 : 5;
            font.drawStringWithShadow(line, xOffset, y, color);
            y += font.FONT_HEIGHT + 4;
        }
        return y;
    }

    private void drawDecorativeLine(int y) {
        drawHorizontalLine(5, overlayWidth - 5, y, 0xFF777777);
        drawHorizontalLine(5, overlayWidth - 5, y + 1, 0xFFA8A8A8);
        drawHorizontalLine(5, overlayWidth - 5, y + 2, 0xFFFFFFFF);
    }

    // Simple wrappers for drawing.
    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    private void drawHorizontalLine(int left, int right, int y, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, y, right, y + 1, color);
    }

    private void drawRectOutline(int left, int top, int right, int bottom, int color) {
        drawHorizontalLine(left, right, top, color);
        drawHorizontalLine(left, right, bottom - 1, color);
        net.minecraft.client.gui.Gui.drawRect(left, top, left + 1, bottom, color);
        net.minecraft.client.gui.Gui.drawRect(right - 1, top, right, bottom, color);
    }

    private String convertColorCodes(String text) {
        return text.replaceAll("&([0-9a-fk-or])", "§$1");
    }

    private ArrayList<String> splitLines(String text, int maxWidth) {
        ArrayList<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        FontRenderer font = mc.fontRenderer;
        for (String word : words) {
            String test = current.length() == 0 ? word : current.toString() + " " + word;
            if (font.getStringWidth(test) > maxWidth) {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            } else {
                if (current.length() > 0)
                    current.append(" ").append(word);
                else
                    current.append(word);
            }
        }
        if (current.length() > 0)
            lines.add(current.toString());
        return lines;
    }
}
