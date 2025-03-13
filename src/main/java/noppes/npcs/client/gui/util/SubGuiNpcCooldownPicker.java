package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;

public class SubGuiNpcCooldownPicker extends SubGuiInterface implements ITextfieldListener {

    private final boolean isMCCustom;
    public long cooldownValue;

    private GuiNpcTextField daysField;
    private GuiNpcTextField hoursField;
    private GuiNpcTextField minutesField;
    private GuiNpcTextField secondsField;

    public SubGuiNpcCooldownPicker(boolean isMCCustom, long currentCooldown) {
        this.isMCCustom = isMCCustom;
        this.cooldownValue = currentCooldown;
        setBackground("smallbg.png");
        xSize = 220;
        ySize = 222;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int days = 0, hours = 0, minutes = 0, seconds = 0;
        if (isMCCustom) {
            long currentMillis = cooldownValue * 50;
            days = (int) (currentMillis / 86400000L);
            long remainder = currentMillis % 86400000L;
            hours = (int) (remainder / 3600000L);
            remainder %= 3600000L;
            minutes = (int) (remainder / 60000L);
            remainder %= 60000L;
            seconds = (int) (remainder / 1000L);
        } else {
            // For RL custom cooldown, currentCooldown is stored in milliseconds.
            days = (int) (cooldownValue / 86400000L);
            long remainder = cooldownValue % 86400000L;
            hours = (int) (remainder / 3600000L);
            remainder %= 3600000L;
            minutes = (int) (remainder / 60000L);
            remainder %= 60000L;
            seconds = (int) (remainder / 1000L);
        }

        int y = guiTop + 20;
        addLabel(new GuiNpcLabel(1, "mailbox.days", guiLeft + 10, y));
        daysField = new GuiNpcTextField(1, this, fontRendererObj, guiLeft + xSize - 110, y - 5, 40, 20, "" + days);
        daysField.setIntegersOnly();
        daysField.setMinMaxDefault(0, Integer.MAX_VALUE, 0);
        addTextField(daysField);

        y += 30;

        addLabel(new GuiNpcLabel(2, "mailbox.hours", guiLeft + 10, y));
        hoursField = new GuiNpcTextField(2, this, fontRendererObj, guiLeft + xSize - 110, y - 5, 40, 20, "" + hours);
        hoursField.setIntegersOnly();
        hoursField.setMinMaxDefault(0, 24, 0);
        addTextField(hoursField);

        y += 30;

        addLabel(new GuiNpcLabel(3, "mailbox.minutes", guiLeft + 10, y));
        minutesField = new GuiNpcTextField(3, this, fontRendererObj, guiLeft + xSize - 110, y - 5, 40, 20, "" + minutes);
        minutesField.setIntegersOnly();
        minutesField.setMinMaxDefault(0, 60, 0);
        addTextField(minutesField);

        y += 30;

        addLabel(new GuiNpcLabel(4, "mailbox.seconds", guiLeft + 10, y));
        secondsField = new GuiNpcTextField(4, this, fontRendererObj, guiLeft + xSize - 110, y - 5, 40, 20, "" + seconds);
        secondsField.setIntegersOnly();
        secondsField.setMinMaxDefault(0, 60, 0);
        addTextField(secondsField);

        addButton(new GuiNpcButton(0, guiLeft + 10, guiTop + ySize - 30, 80, 20, "gui.done"));
    }

    @Override
    public void buttonEvent(GuiButton button) {
        if (button.id == 0) {
            close();
        }
    }

    public void close() {
        int inputDays = parseInt(daysField.getText());
        int inputHours = parseInt(hoursField.getText());
        int inputMinutes = parseInt(minutesField.getText());
        int inputSeconds = parseInt(secondsField.getText());
        // Calculate total time in milliseconds.
        long totalMillis = inputDays * 86400000L + inputHours * 3600000L + inputMinutes * 60000L + inputSeconds * 1000L;
        if (isMCCustom) {
            // Convert real milliseconds to ticks.
            cooldownValue = totalMillis / 50;
        } else {
            cooldownValue = totalMillis;
        }
        super.close();
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        // No immediate action needed on unfocus.
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            return 0;
        }
    }
}
