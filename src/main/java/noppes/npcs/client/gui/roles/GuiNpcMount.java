package noppes.npcs.client.gui.roles;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.role.RoleSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleMount;

public class GuiNpcMount extends GuiNPCInterface2 implements ITextfieldListener {

    private final RoleMount role;

    public GuiNpcMount(EntityNPCInterface npc) {
        super(npc);
        this.role = npc.roleInterface instanceof RoleMount ? (RoleMount) npc.roleInterface : null;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (role == null) {
            close();
            return;
        }

        int leftLabelX = guiLeft + 4;
        int leftFieldX = guiLeft + 116;
        int leftY = guiTop + 6;

        leftY = addOffsetRow(0, 10, "role.mount.offsetx", role.getOffsetX(), leftLabelX, leftFieldX, leftY);
        leftY = addOffsetRow(1, 11, "role.mount.offsety", role.getOffsetY(), leftLabelX, leftFieldX, leftY);
        leftY = addOffsetRow(2, 12, "role.mount.offsetz", role.getOffsetZ(), leftLabelX, leftFieldX, leftY);
        leftY = addJumpRow(3, 13, "role.mount.jumpheight", role.getJumpStrength(), leftLabelX, leftFieldX, leftY);

        addLabel(new GuiNpcLabel(4, "role.mount.sprint", leftLabelX, leftY + 5));
        addButton(new GuiNpcButton(101, leftFieldX, leftY, 60, 20, new String[]{"gui.no", "gui.yes"}, role.isSprintAllowed() ? 1 : 0));
        leftY += 24;

        int rightLabelX = guiLeft + 214;
        int rightFieldX = guiLeft + 320;
        int rightY = guiTop + 6;

        addLabel(new GuiNpcLabel(20, "role.mount.fly", rightLabelX, rightY + 5));
        addButton(new GuiNpcButton(102, rightFieldX, rightY, 60, 20, new String[]{"gui.no", "gui.yes"}, role.isFlyingMountEnabled() ? 1 : 0));
        rightY += 24;

        if (role.isFlyingMountEnabled()) {
            addLabel(new GuiNpcLabel(21, "role.mount.hover", rightLabelX, rightY + 5));
            addButton(new GuiNpcButton(103, rightFieldX, rightY, 60, 20, new String[]{"gui.no", "gui.yes"}, role.isHoverModeEnabled() ? 1 : 0));
            rightY += 24;

            rightY = addSpeedRow(22, 14, "role.mount.ascendspeed", role.getFlyingAscendSpeed(), rightLabelX, rightFieldX, rightY, 0.1F, 3.0F, 0.60F);
            rightY = addSpeedRow(23, 15, "role.mount.descendspeed", role.getFlyingDescendSpeed(), rightLabelX, rightFieldX, rightY, 0.05F, 3.0F, 0.35F);
        }
    }

    private int addOffsetRow(int labelId, int fieldId, String translationKey, float value, int labelX, int fieldX, int y) {
        addLabel(new GuiNpcLabel(labelId, translationKey, labelX, y + 5));
        GuiNpcTextField field = new GuiNpcTextField(fieldId, this, fieldX, y, 80, 20, format(value));
        field.floatsOnly = true;
        field.setMinMaxDefaultFloat(-5.0F, 5.0F, 0.0F);
        addTextField(field);
        return y + 24;
    }

    private int addJumpRow(int labelId, int fieldId, String translationKey, float value, int labelX, int fieldX, int y) {
        addLabel(new GuiNpcLabel(labelId, translationKey, labelX, y + 5));
        GuiNpcTextField field = new GuiNpcTextField(fieldId, this, fieldX, y, 80, 20, format(value));
        field.floatsOnly = true;
        field.setMinMaxDefaultFloat(0.1F, 15.0F, 1.0F);
        addTextField(field);
        return y + 24;
    }

    private int addSpeedRow(int labelId, int fieldId, String translationKey, float value, int labelX, int fieldX, int y, float min, float max, float defaultValue) {
        addLabel(new GuiNpcLabel(labelId, translationKey, labelX, y + 5));
        GuiNpcTextField field = new GuiNpcTextField(fieldId, this, fieldX, y, 80, 20, format(value));
        field.floatsOnly = true;
        field.setMinMaxDefaultFloat(min, max, defaultValue);
        addTextField(field);
        return y + 24;
    }

    private String format(float value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (role == null)
            return;
        if (guibutton instanceof GuiNpcButton) {
            GuiNpcButton button = (GuiNpcButton) guibutton;
            if (guibutton.id == 101) {
                role.setSprintAllowed(button.getValue() == 1);
            } else if (guibutton.id == 102) {
                role.setFlyingMountEnabled(button.getValue() == 1);
                initGui();
            } else if (guibutton.id == 103) {
                role.setHoverModeEnabled(button.getValue() == 1);
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (role == null)
            return;
        float value = textfield.isFloat() ? textfield.getFloat() : 0.0F;
        switch (textfield.id) {
            case 10:
                role.setOffsetX(value);
                break;
            case 11:
                role.setOffsetY(value);
                break;
            case 12:
                role.setOffsetZ(value);
                break;
            case 13:
                role.setJumpStrength(value);
                break;
            case 14:
                role.setFlyingAscendSpeed(value);
                break;
            case 15:
                role.setFlyingDescendSpeed(value);
                break;
        }
        textfield.setText(format(valueForField(textfield.id)));
    }

    private float valueForField(int id) {
        switch (id) {
            case 10:
                return role.getOffsetX();
            case 11:
                return role.getOffsetY();
            case 12:
                return role.getOffsetZ();
            case 13:
                return role.getJumpStrength();
            case 14:
                return role.getFlyingAscendSpeed();
            case 15:
                return role.getFlyingDescendSpeed();
            default:
                return 0.0F;
        }
    }

    @Override
    public void save() {
        if (role != null) {
            PacketClient.sendClient(new RoleSavePacket(role.writeToNBT(new NBTTagCompound())));
        }
    }
}
