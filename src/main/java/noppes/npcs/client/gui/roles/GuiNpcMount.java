package noppes.npcs.client.gui.roles;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.role.RoleSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleMount;

import java.util.Locale;

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

        int y = guiTop + 6;
        addOffsetRow(0, "role.mount.offsetx", role.getOffsetX(), y);
        y += 24;
        addOffsetRow(1, "role.mount.offsety", role.getOffsetY(), y);
        y += 24;
        addOffsetRow(2, "role.mount.offsetz", role.getOffsetZ(), y);

        addButton(new GuiNpcButton(3, guiLeft + 4, y + 26, 80, 20, "role.mount.reset"));

        GuiNpcLabel speedLabel = new GuiNpcLabel(4, "", guiLeft + 4, y + 6);
        speedLabel.label = StatCollector.translateToLocalFormatted("role.mount.walkspeed", npc.ais.getWalkingSpeed());
        addLabel(speedLabel);
    }

    private void addOffsetRow(int id, String translationKey, float value, int y) {
        addLabel(new GuiNpcLabel(id, translationKey, guiLeft + 4, y + 5));
        GuiNpcTextField field = new GuiNpcTextField(id, this, guiLeft + 140, y, 80, 20, format(value));
        field.floatsOnly = true;
        field.setMinMaxDefaultFloat(-5.0F, 5.0F, 0.0F);
        addTextField(field);
    }

    private String format(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (role == null)
            return;
        if (guibutton.id == 3) {
            role.resetOffsets();
            initGui();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (role == null)
            return;
        float value = textfield.isFloat() ? textfield.getFloat() : 0.0F;
        switch (textfield.id) {
            case 0:
                role.setOffsetX(value);
                break;
            case 1:
                role.setOffsetY(value);
                break;
            case 2:
                role.setOffsetZ(value);
                break;
        }
        textfield.setText(format(valueForField(textfield.id)));
    }

    private float valueForField(int id) {
        switch (id) {
            case 0:
                return role.getOffsetX();
            case 1:
                return role.getOffsetY();
            case 2:
                return role.getOffsetZ();
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
