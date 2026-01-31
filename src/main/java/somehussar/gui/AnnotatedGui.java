package somehussar.gui;

import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import somehussar.gui.annotationHandling.GuiFieldHandler;
import somehussar.gui.annotationHandling.field.EditableField;

public class AnnotatedGui extends GuiNPCInterface {
    private final GuiFieldHandler.ClassMetadata metadata;

    public AnnotatedGui(Class<?> type) {
        metadata = GuiFieldHandler.getMetadata(type);
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (metadata == null)
            return;

        int i = 0;
        for (EditableField field : metadata.getDeclaredFields()) {
            addLabel(new GuiNpcLabel(i++, field.getName(), guiLeft+8, guiTop+i*15+5));
        }
    }

    @Override
    public void save() {

    }
}
