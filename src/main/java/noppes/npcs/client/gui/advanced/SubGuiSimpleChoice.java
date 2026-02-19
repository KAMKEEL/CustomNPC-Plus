package noppes.npcs.client.gui.advanced;

import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Base class for simple choice dialogs that present a few buttons and return an int result.
 * Subclasses define their own layout in {@code initGui()} and call {@code setResult()} from {@code buttonEvent()}.
 * The parent reads the result via {@code getResult()} in {@code subGuiClosed()}.
 *
 * <p>Result is -1 by default (cancelled / no selection).</p>
 */
public abstract class SubGuiSimpleChoice extends SubGuiInterface {

    private int result = -1;

    protected final void setResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }
}
