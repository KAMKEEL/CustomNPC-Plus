package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.*;

public class SubGuiNpcTextArea extends SubGuiInterface implements ITextChangeListener {
    public String text;
    public String originalText;
    private GuiScriptTextArea textarea;
    private boolean highlighting = false;

    public SubGuiNpcTextArea(String text) {
        this.text = text;
        setBackground("bgfilled.png");
        xSize = 256;
        ySize = 256;
        closeOnEsc = true;
    }

    public SubGuiNpcTextArea(String originalText, String text){
        this(text);
        this.originalText = originalText;
    }

    @Override
    public void initGui() {
        xSize = (int) (width * 0.88);
        ySize = (int) (xSize * 0.56);
        bgScale = xSize / 440f;
        super.initGui();
        if (textarea != null)
            this.text = textarea.getText();
        if(textarea != null)
            this.text = textarea.getText();
        int yoffset = (int) (ySize * 0.02);

        textarea = new GuiScriptTextArea(this, 2, guiLeft + 1 + yoffset, guiTop + yoffset, xSize - 100 - yoffset, (int) (ySize) - yoffset * 2, text);
        textarea.setListener(this);
        if(highlighting)
            textarea.enableCodeHighlighting();
        addTextField(textarea);

        this.buttonList.add(new GuiNpcButton(102, guiLeft + xSize - 90 - yoffset, guiTop + 20, 56, 20, "gui.clear"));
        this.buttonList.add(new GuiNpcButton(101, guiLeft + xSize - 90 - yoffset, guiTop + 43, 56, 20, "gui.paste"));
        this.buttonList.add(new GuiNpcButton(100, guiLeft + xSize - 90 - yoffset, guiTop + 66, 56, 20, "gui.copy"));
        this.buttonList.add(new GuiNpcButton(103, guiLeft + xSize - 90 - yoffset, guiTop + 89, 56, 20, "remote.reset"));

        this.buttonList.add(new GuiNpcButton(0, guiLeft + xSize - 90 - yoffset, guiTop + 160, 56, 20, "gui.close"));

        xSize = 420;
        ySize = 256;
    }


    @Override
    public void close() {
        text = getTextField(2).getText();
        super.close();
    }

    public SubGuiNpcTextArea enableHighlighting() {
        highlighting = true;
        return this;
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 100) {
            NoppesStringUtils.setClipboardContents(textarea.getText());
        }
        if (id == 101) {
            textarea.setText(NoppesStringUtils.getClipboardContents());
        }
        if (id == 102) {
            textarea.setText("");
        }
        if (id == 103) {
            textarea.setText(originalText);
        }
        if(id == 0){
            close();
        }
    }

    @Override
    public void textUpdate(String text) {
        this.text = text;
    }
}
