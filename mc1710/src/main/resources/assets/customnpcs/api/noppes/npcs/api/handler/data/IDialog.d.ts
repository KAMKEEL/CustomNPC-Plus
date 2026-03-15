/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IDialog
 */
export interface IDialog {
    /**
     * @return The unique ID of this dialog
     */
    getId(): import('./int').int;
    /**
     * @return The title/name of this dialog
     */
    getName(): String;
    /**
     * Sets the title/name of this dialog.
     * @param var1 The new dialog title
     */
    setName(var1: String): import('./void').void;
    /**
     * @return The dialog body text displayed to the player
     */
    getText(): String;
    /**
     * Sets the dialog body text displayed to the player.
     * @param var1 The new dialog text
     */
    setText(var1: String): import('./void').void;
    /**
     * @return The quest associated with this dialog, or null if none
     */
    getQuest(): import('./IQuest').IQuest;
    /**
     * Sets the quest associated with this dialog.
     * @param var1 The quest to associate, or null to remove
     */
    setQuest(var1: import('./IQuest').IQuest): import('./void').void;
    /**
     * @return The command executed when this dialog is shown
     */
    getCommand(): String;
    /**
     * Sets the command executed when this dialog is shown.
     * @param var1 The command string
     */
    setCommand(var1: String): import('./void').void;
    /**
     * @return A list of all dialog options
     */
    getOptions(): import('./IDialogOption').IDialogOption[];
    /**
     * Gets a dialog option by its slot index.
     * @param var1 The option slot index
     * @return The dialog option at the given slot
     */
    getOption(var1: import('./int').int): import('./IDialogOption').IDialogOption;
    /**
     * @return The availability settings for this dialog
     */
    getAvailability(): import('./IAvailability').IAvailability;
    /**
     * @return The category this dialog belongs to
     */
    getCategory(): import('./IDialogCategory').IDialogCategory;
    /**
     * Sets whether the screen darkens when this dialog is displayed.
     * @param darkenScreen True to darken the screen
     */
    setDarkenScreen(darkenScreen: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the screen darkens when this dialog is displayed
     */
    getDarkenScreen(): import('./boolean').boolean;
    /**
     * Sets whether the Escape key is disabled while this dialog is open.
     * @param disableEsc True to disable the Escape key
     */
    setDisableEsc(disableEsc: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the Escape key is disabled while this dialog is open
     */
    getDisableEsc(): import('./boolean').boolean;
    /**
     * Sets whether the dialog option wheel is shown.
     * @param showWheel True to show the option wheel
     */
    setShowWheel(showWheel: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the dialog option wheel is shown
     */
    getShowWheel(): import('./boolean').boolean;
    /**
     * Sets whether the NPC is hidden during this dialog.
     * @param hideNPC True to hide the NPC
     */
    setHideNPC(hideNPC: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the NPC is hidden during this dialog
     */
    getHideNPC(): import('./boolean').boolean;
    /**
     * Sets the sound played when this dialog opens.
     * @param sound The resource location of the sound
     */
    setSound(sound: String): import('./void').void;
    /**
     * @return The resource location of the sound played when this dialog opens
     */
    getSound(): String;
    /**
     * Saves this dialog to the dialog controller.
     */
    save(): import('./void').void;
    /**
     * Sets the text color of the dialog body.
     * @param color The color as an integer (e.g. 0xe0e0e0)
     */
    setColor(color: import('./int').int): import('./void').void;
    /**
     * @return The text color of the dialog body
     */
    getColor(): import('./int').int;
    /**
     * Sets the color of the dialog title.
     * @param titleColor The color as an integer (e.g. 0xe0e0e0)
     */
    setTitleColor(titleColor: import('./int').int): import('./void').void;
    /**
     * @return The color of the dialog title
     */
    getTitleColor(): import('./int').int;
    /**
     * Sets whether the dialog text renders character by character.
     * @param gradual True to enable gradual text rendering
     */
    renderGradual(gradual: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the dialog text renders character by character
     */
    renderGradual(): import('./boolean').boolean;
    /**
     * Sets whether previous dialog text blocks are shown.
     * @param show True to show previous dialog blocks
     */
    showPreviousBlocks(show: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether previous dialog text blocks are shown
     */
    showPreviousBlocks(): import('./boolean').boolean;
    /**
     * Sets whether the separator line above dialog options is shown.
     * @param show True to show the option line
     */
    showOptionLine(show: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the separator line above dialog options is shown
     */
    showOptionLine(): import('./boolean').boolean;
    /**
     * Sets the sound played per character during gradual text rendering.
     * @param textSound The resource location of the text sound
     */
    setTextSound(textSound: String): import('./void').void;
    /**
     * @return The resource location of the sound played per character during gradual text rendering
     */
    getTextSound(): String;
    /**
     * Sets the pitch of the per-character text sound.
     * @param textPitch The pitch value (default 1.0)
     */
    setTextPitch(textPitch: import('./float').float): import('./void').void;
    /**
     * @return The pitch of the per-character text sound
     */
    getTextPitch(): import('./float').float;
    /**
     * Sets the position of the dialog title.
     * @param pos The title position value
     */
    setTitlePos(pos: import('./int').int): import('./void').void;
    /**
     * @return The position of the dialog title
     */
    getTitlePos(): import('./int').int;
    /**
     * Sets the render scale of the NPC in the dialog.
     * @param scale The NPC scale (default 1.0)
     */
    setNPCScale(scale: import('./float').float): import('./void').void;
    /**
     * @return The render scale of the NPC in the dialog
     */
    getNpcScale(): import('./float').float;
    /**
     * Sets the NPC render offset in the dialog.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    setNpcOffset(offsetX: import('./int').int, offsetY: import('./int').int): import('./void').void;
    /**
     * @return The horizontal offset of the NPC render in pixels
     */
    getNpcOffsetX(): import('./int').int;
    /**
     * @return The vertical offset of the NPC render in pixels
     */
    getNpcOffsetY(): import('./int').int;
    /**
     * Sets the width and height of the dialog text area.
     * @param textWidth The text area width in pixels
     * @param textHeight The text area height in pixels
     */
    textWidthHeight(textWidth: import('./int').int, textHeight: import('./int').int): import('./void').void;
    /**
     * @return The width of the dialog text area in pixels
     */
    getTextWidth(): import('./int').int;
    /**
     * @return The height of the dialog text area in pixels
     */
    setTextHeight(): import('./int').int;
    /**
     * Sets the offset of the dialog text area.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    setTextOffset(offsetX: import('./int').int, offsetY: import('./int').int): import('./void').void;
    /**
     * @return The horizontal offset of the dialog text area in pixels
     */
    getTextOffsetX(): import('./int').int;
    /**
     * @return The vertical offset of the dialog text area in pixels
     */
    getTextOffsetY(): import('./int').int;
    /**
     * Sets the offset of the dialog title.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    setTitleOffset(offsetX: import('./int').int, offsetY: import('./int').int): import('./void').void;
    /**
     * @return The horizontal offset of the dialog title in pixels
     */
    getTitleOffsetX(): import('./int').int;
    /**
     * @return The vertical offset of the dialog title in pixels
     */
    getTitleOffsetY(): import('./int').int;
    /**
     * Sets the offset of the dialog options area.
     * @param offsetX The horizontal offset in pixels
     * @param offsetY The vertical offset in pixels
     */
    setOptionOffset(offsetX: import('./int').int, offsetY: import('./int').int): import('./void').void;
    /**
     * @return The horizontal offset of the dialog options area in pixels
     */
    getOptionOffsetX(): import('./int').int;
    /**
     * @return The vertical offset of the dialog options area in pixels
     */
    getOptionOffsetY(): import('./int').int;
    /**
     * Sets the spacing between dialog options.
     * @param spaceX The horizontal spacing in pixels
     * @param spaceY The vertical spacing in pixels
     */
    setOptionSpacing(spaceX: import('./int').int, spaceY: import('./int').int): import('./void').void;
    /**
     * @return The horizontal spacing between dialog options in pixels
     */
    getOptionSpaceX(): import('./int').int;
    /**
     * @return The vertical spacing between dialog options in pixels
     */
    getOptionSpaceY(): import('./int').int;
    /**
     * Adds an image to the dialog at the given ID.
     * @param id The image ID
     * @param image The dialog image to add
     */
    addImage(id: import('./int').int, image: import('./IDialogImage').IDialogImage): import('./void').void;
    /**
     * Gets a dialog image by its ID.
     * @param id The image ID
     * @return The dialog image, or null if not found
     */
    getImage(id: import('./int').int): import('./IDialogImage').IDialogImage;
    /**
     * Creates a new empty dialog image instance.
     * @return A new {@link IDialogImage}
     */
    createImage(): import('./IDialogImage').IDialogImage;
    /**
     * @return An array of all dialog images
     */
    getImages(): import('./IDialogImage').IDialogImage[];
    /**
     * Checks whether an image with the given ID exists.
     * @param id The image ID
     * @return True if the image exists
     */
    hasImage(id: import('./int').int): import('./boolean').boolean;
    /**
     * Removes the image with the given ID.
     * @param id The image ID to remove
     */
    removeImage(id: import('./int').int): import('./void').void;
    /**
     * Removes all images from this dialog.
     */
    clearImages(): import('./void').void;
    version: import('./int').int;
    id: import('./int').int;
    title: String;
    text: String;
    quest: import('./int').int;
    category: import('./DialogCategory').DialogCategory;
    colorData: import('./DialogColorData').DialogColorData;
    options: Java.java.util.HashMap<Integer, import('./DialogOption').DialogOption>;
    availability: import('./Availability').Availability;
    factionOptions: import('./FactionOptions').FactionOptions;
    sound: String;
    command: String;
    mail: import('./PlayerMail').PlayerMail;
    color: import('./int').int;
    titleColor: import('./int').int;
    hideNPC: import('./boolean').boolean;
    showWheel: import('./boolean').boolean;
    disableEsc: import('./boolean').boolean;
    darkenScreen: import('./boolean').boolean;
    showOptionLine: import('./boolean').boolean;
    alignment: import('./byte').byte;
    renderGradual: import('./boolean').boolean;
    showPreviousBlocks: import('./boolean').boolean;
    textSound: String;
    textPitch: import('./float').float;
    textWidth: import('./int').int;
    textHeight: import('./int').int;
    titlePos: import('./int').int;
    textOffsetY: import('./int textOffsetX,').int textOffsetX,;
    titleOffsetY: import('./int titleOffsetX,').int titleOffsetX,;
    optionSpaceY: import('./int optionSpaceX,').int optionSpaceX,;
    optionOffsetY: import('./int optionOffsetX,').int optionOffsetX,;
    npcScale: import('./float').float;
    npcOffsetY: import('./int npcOffsetX,').int npcOffsetX,;
    dialogImages: Java.java.util.HashMap<Integer, import('./IDialogImage').IDialogImage>;
}
