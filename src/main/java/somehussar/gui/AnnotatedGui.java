package somehussar.gui;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import somehussar.gui.annotationHandling.GuiFieldHandler;
import somehussar.gui.annotationHandling.field.EditableField;

public class AnnotatedGui<T> extends SubGuiInterface {
    private final GuiFieldHandler.ClassMetadata metadata;

    public AnnotatedGui(T object, String[] groups, GuiScreen parent) {
        this(object, groups, parent, 256, 216);
    }

    public AnnotatedGui(T object, String[] groups, GuiScreen parent, int xSize, int ySize) {
        if (object != null)
            metadata = GuiFieldHandler.getMetadata(object.getClass());
        else
            metadata = null;
        setBackground("menubg.png");
        this.xSize = xSize;
        this.ySize = ySize;
        closeOnEsc = true;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (metadata == null)
            return;

        int i = 0;
        for (EditableField field : metadata.getParent().getDeclaredFields()) {
            addLabel(new GuiNpcLabel(i++, field.getName(), guiLeft+8, guiTop+i*15+5));
        }
    }

    @Override
    public void save() {

    }
}
