package noppes.npcs.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.controllers.data.Quest;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class OverlayQuestTracking extends Gui {
    private Minecraft mc;

    // Dimensions for overlay background
    private int overlayWidth = 200;
    private int overlayHeight = 120;

    // Text lists for each section
    private ArrayList<String> questTitleLines = new ArrayList<>();
    private ArrayList<String> questCategoryLines = new ArrayList<>();
    private ArrayList<String> objectiveLines = new ArrayList<>();
    private ArrayList<String> turnInLines = new ArrayList<>();

    public OverlayQuestTracking(Minecraft mc) {
        this.mc = mc;
    }

    /**
     * Updates overlay text based on quest data.
     * Converts "&" codes into Minecraft formatting codes.
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
     * Reads overlay data from an NBT compound and updates the overlay.
     */
    public void setOverlayData(NBTTagCompound compound) {
        Quest quest = new Quest();
        quest.readNBT(compound.getCompoundTag("Quest"));
        String category = compound.getString("CategoryName");

        ArrayList<String> objectives = new ArrayList<>();
        NBTTagList objectiveList = compound.getTagList("ObjectiveList", 8);
        for (int i = 0; i < objectiveList.tagCount(); i++) {
            String objective = objectiveList.getStringTagAt(i);
            String[] split = objective.split(":");
            split = split[split.length - 1].split("/");
            boolean completed = false;
            try {
                if (split.length < 2)
                    throw new NumberFormatException("Insufficient data");
                int killed = Integer.parseInt(split[0].trim());
                int total = Integer.parseInt(split[1].trim());
                if (killed / total == 1)
                    completed = true;
            } catch (NumberFormatException e) {
                if (objective.endsWith("(Done)") || objective.endsWith("(read)") ||
                    (objective.endsWith("Found") && !objective.endsWith("Not Found")))
                    completed = true;
            }
            if (completed) {
                objective = "&a&o&m" + objective;
            } else {
                objective = "&o" + objective;
            }
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
    }

    /**
     * Renders the overlay using the current configuration settings.
     * Here, posX and posY (from config, treated as percentages) are used relative to the current scaled resolution.
     */
    public void renderOverlay(float partialTicks) {
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int) ((float) ConfigClient.QuestOverlayX / 100F * res.getScaledWidth());
        int actualY = (int) ((float) ConfigClient.QuestOverlayY / 100F * res.getScaledHeight());
        float s = ConfigClient.QuestOverlayScale / 100.0F;
        int textAlign = ConfigClient.QuestOverlayTextAlign; // 0: left, 1: center, 2: right

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(s, s, s);

        // Draw a lighter, semi-transparent background
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

    private int renderTextBlock(List<String> lines, int startY, int align, int color) {
        int y = startY;
        FontRenderer font = mc.fontRenderer;
        for (String line : lines) {
            int strWidth = font.getStringWidth(line);
            int xOffset;
            if (align == 1)
                xOffset = (overlayWidth - strWidth) / 2;
            else if (align == 2)
                xOffset = overlayWidth - strWidth - 5;
            else
                xOffset = 5;
            font.drawStringWithShadow(line, xOffset, y, color);
            y += font.FONT_HEIGHT + 4;
        }
        return y;
    }

    private void drawDecorativeLine(int y) {
        int lineStart = 5;
        int lineEnd = overlayWidth - 5;
        drawHorizontalLine(lineStart, lineEnd, y, 0xFF777777);
        drawHorizontalLine(lineStart, lineEnd, y + 1, 0xFFA8A8A8);
        drawHorizontalLine(lineStart, lineEnd, y + 2, 0xFFFFFFFF);
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

    private String convertColorCodes(String text) {
        return text.replaceAll("&([0-9a-fk-or])", "§$1");
    }
}
