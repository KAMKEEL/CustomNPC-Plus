package kamkeel.npcs.editorgui;

/**
 * IPropertyEditorCallback is used by the property editor (SubGuiEditProperty)
 * to return the updated value.
 */
public interface IPropertyEditorCallback {
    void propertyUpdated(String newValue);
}
