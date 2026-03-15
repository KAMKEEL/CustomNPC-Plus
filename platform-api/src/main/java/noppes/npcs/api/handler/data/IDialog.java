package noppes.npcs.api.handler.data;

import java.util.List;

public interface IDialog {
    /**
     * @return The unique ID of this dialog
     */
    int getId();

    /**
     * @return The title/name of this dialog
     */
    String getName();

    /**
     * Sets the title/name of this dialog.
     * @param var1 The new dialog title
     */
    void setName(String var1);

    /**
     * @return The dialog body text displayed to the player
     */
    String getText();

    /**
     * Sets the dialog body text displayed to the player.
     * @param var1 The new dialog text
     */
    void setText(String var1);

    /**
     * @return The quest associated with this dialog, or null if none
     */
    IQuest getQuest();

    /**
     * Sets the quest associated with this dialog.
     * @param var1 The quest to associate, or null to remove
     */
    void setQuest(IQuest var1);

    /**
     * @return The command executed when this dialog is shown
     */
    String getCommand();

    /**
     * Sets the command executed when this dialog is shown.
     * @param var1 The command string
     */
    void setCommand(String var1);

    /**
     * @return A list of all dialog options
     */
    List<IDialogOption> getOptions();

    /**
     * Gets a dialog option by its slot index.
     * @param var1 The option slot index
     * @return The dialog option at the given slot
     */
    IDialogOption getOption(int var1);

    /**
     * @return The availability settings for this dialog
     */
    IAvailability getAvailability();

    /**
     * @return The category this dialog belongs to
     */
    IDialogCategory getCategory();

    /**
     * Sets whether the screen darkens when this dialog is displayed.
     * @param darkenScreen True to darken the screen
     */
    void setDarkenScreen(boolean darkenScreen);

    /**
     * @return Whether the screen darkens when this dialog is displayed
     */
    boolean getDarkenScreen();

    /**
     * Sets whether the Escape key is disabled while this dialog is open.
     * @param disableEsc True to disable the Escape key
     */
    void setDisableEsc(boolean disableEsc);

    /**
     * @return Whether the Escape key is disabled while this dialog is open
     */
    boolean getDisableEsc();

    /**
     * Sets whether the dialog option wheel is shown.
     * @param showWheel True to show the option wheel
     */
    void setShowWheel(boolean showWheel);

    /**
     * @return Whether the dialog option wheel is shown
     */
    boolean getShowWheel();

    /**
     * Sets whether the NPC is hidden during this dialog.
     * @param hideNPC True to hide the NPC
     */
    void setHideNPC(boolean hideNPC);

    /**
     * @return Whether the NPC is hidden during this dialog
     */
    boolean getHideNPC();

    /**
     * Sets the sound played when this dialog opens.
     * @param sound The resource location of the sound
     */
    void setSound(String sound);

    /**
     * @return The resource location of the sound played when this dialog opens
     */
    String getSound();

    /**
     * Saves this dialog to the dialog controller.
     */
    void save();

    /**
     * Sets the text color of the dialog body.
     * @param color The color as an integer (e.g. 0xe0e0e0)
     */
    void setColor(int color);

    /**
     * @return The text color of the dialog body
     */
    int getColor();

    /**
     * Sets the color of the dialog title.
     * @param titleColor The color as an integer (e.g. 0xe0e0e0)
     */
    void setTitleColor(int titleColor);

    /**
     * @return The color of the dialog title
     */
    int getTitleColor();

    /**
     * Sets whether the dialog text renders character by character.
     * @param gradual True to enable gradual text rendering
     */
    void renderGradual(boolean gradual);

    /**
     * @return Whether the dialog text renders character by character
     */
    boolean renderGradual();

    /**
     * Sets whether previous dialog text blocks are shown.
     * @param show True to show previous dialog blocks
     */
    void showPreviousBlocks(boolean show);

    /**
     * @return Whether previous dialog text blocks are shown
     */
    boolean showPreviousBlocks();

    /**
     * Sets whether the separator line above dialog options is shown.
     * @param show True to show the option line
     */
    void showOptionLine(boolean show);

    /**
     * @return Whether the separator line above dialog options is shown
     */
    boolean showOptionLine();

    /**
     * Sets the sound played per character during gradual text rendering.
     * @param textSound The resource location of the text sound
     */
    void setTextSound(String textSound);

    /**
     * @return The resource location of the sound played per character during gradual text rendering
     */
    String getTextSound();

    /**
     * Sets the pitch of the per-character text sound.
     * @param textPitch The pitch value (default 1.0)
     */
    void setTextPitch(float textPitch);

    /**
     * @return The pitch of the per-character text sound
     */
    float getTextPitch();

    /**
     * Sets the position of the dialog title.
     * @param pos The title position value
     */
    void setTitlePos(int pos);

    /**
     * @return The position of the dialog title
     */
    int getTitlePos();

    /**
     * Sets the render scale of the NPC in the dialog.
     * @param scale The NPC scale (default 1.0)
     */
    void setNPCScale(float scale);

    /**
     * @return The render scale of the NPC in the dialog
     */
    float getNpcScale();

    /**
     * Sets the NPC render offset in the dialog.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    void setNpcOffset(int offsetX, int offsetY);

    /**
     * @return The horizontal offset of the NPC render in pixels
     */
    int getNpcOffsetX();

    /**
     * @return The vertical offset of the NPC render in pixels
     */
    int getNpcOffsetY();

    /**
     * Sets the width and height of the dialog text area.
     * @param textWidth The text area width in pixels
     * @param textHeight The text area height in pixels
     */
    void textWidthHeight(int textWidth, int textHeight);

    /**
     * @return The width of the dialog text area in pixels
     */
    int getTextWidth();

    /**
     * @return The height of the dialog text area in pixels
     */
    int setTextHeight();

    /**
     * Sets the offset of the dialog text area.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    void setTextOffset(int offsetX, int offsetY);

    /**
     * @return The horizontal offset of the dialog text area in pixels
     */
    int getTextOffsetX();

    /**
     * @return The vertical offset of the dialog text area in pixels
     */
    int getTextOffsetY();

    /**
     * Sets the offset of the dialog title.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    void setTitleOffset(int offsetX, int offsetY);

    /**
     * @return The horizontal offset of the dialog title in pixels
     */
    int getTitleOffsetX();

    /**
     * @return The vertical offset of the dialog title in pixels
     */
    int getTitleOffsetY();

    /**
     * Sets the offset of the dialog options area.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    void setOptionOffset(int offsetX, int offsetY);

    /**
     * @return The horizontal offset of the dialog options area in pixels
     */
    int getOptionOffsetX();

    /**
     * @return The vertical offset of the dialog options area in pixels
     */
    int getOptionOffsetY();

    /**
     * Sets the spacing between dialog options.
     * @param spaceX The horizontal spacing in pixels
     * @param spaceY The vertical spacing in pixels
     */
    void setOptionSpacing(int spaceX, int spaceY);

    /**
     * @return The horizontal spacing between dialog options in pixels
     */
    int getOptionSpaceX();

    /**
     * @return The vertical spacing between dialog options in pixels
     */
    int getOptionSpaceY();

    /**
     * Adds an image to the dialog at the given ID.
     * @param id The image ID
     * @param image The dialog image to add
     */
    void addImage(int id, IDialogImage image);

    /**
     * Gets a dialog image by its ID.
     * @param id The image ID
     * @return The dialog image, or null if not found
     */
    IDialogImage getImage(int id);

    /**
     * Creates a new empty dialog image instance.
     * @return A new {@link IDialogImage}
     */
    IDialogImage createImage();

    /**
     * @return An array of all dialog images
     */
    IDialogImage[] getImages();

    /**
     * Checks whether an image with the given ID exists.
     * @param id The image ID
     * @return True if the image exists
     */
    boolean hasImage(int id);

    /**
     * Removes the image with the given ID.
     * @param id The image ID to remove
     */
    void removeImage(int id);

    /**
     * Removes all images from this dialog.
     */
    void clearImages();
}
