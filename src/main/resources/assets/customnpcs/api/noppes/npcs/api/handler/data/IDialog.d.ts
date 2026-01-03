/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IDialog {

    // Methods
    getId(): number;
    getName(): string;
    setName(var1: string): void;
    getText(): string;
    setText(var1: string): void;
    getQuest(): import('./IQuest').IQuest;
    setQuest(var1: import('./IQuest').IQuest): void;
    getCommand(): string;
    setCommand(var1: string): void;
    getOptions(): Array<IDialogOption;
    getOption(var1: number): import('./IDialogOption').IDialogOption;
    getAvailability(): import('./IAvailability').IAvailability;
    getCategory(): import('./IDialogCategory').IDialogCategory;
    setDarkenScreen(darkenScreen: boolean): void;
    getDarkenScreen(): boolean;
    setDisableEsc(disableEsc: boolean): void;
    getDisableEsc(): boolean;
    setShowWheel(showWheel: boolean): void;
    getShowWheel(): boolean;
    setHideNPC(hideNPC: boolean): void;
    getHideNPC(): boolean;
    setSound(sound: string): void;
    getSound(): string;
    save(): void;
    setColor(color: number): void;
    getColor(): number;
    setTitleColor(titleColor: number): void;
    getTitleColor(): number;
    renderGradual(gradual: boolean): void;
    renderGradual(): boolean;
    showPreviousBlocks(show: boolean): void;
    showPreviousBlocks(): boolean;
    showOptionLine(show: boolean): void;
    showOptionLine(): boolean;
    setTextSound(textSound: string): void;
    getTextSound(): string;
    setTextPitch(textPitch: number): void;
    getTextPitch(): number;
    setTitlePos(pos: number): void;
    getTitlePos(): number;
    setNPCScale(scale: number): void;
    getNpcScale(): number;
    setNpcOffset(offsetX: number, offsetY: number): void;
    getNpcOffsetX(): number;
    getNpcOffsetY(): number;
    textWidthHeight(textWidth: number, textHeight: number): void;
    getTextWidth(): number;
    setTextHeight(): number;
    setTextOffset(offsetX: number, offsetY: number): void;
    getTextOffsetX(): number;
    getTextOffsetY(): number;
    setTitleOffset(offsetX: number, offsetY: number): void;
    getTitleOffsetX(): number;
    getTitleOffsetY(): number;
    setOptionOffset(offsetX: number, offsetY: number): void;
    getOptionOffsetX(): number;
    getOptionOffsetY(): number;
    setOptionSpacing(spaceX: number, spaceY: number): void;
    getOptionSpaceX(): number;
    getOptionSpaceY(): number;
    addImage(id: number, image: import('./IDialogImage').IDialogImage): void;
    getImage(id: number): import('./IDialogImage').IDialogImage;
    createImage(): import('./IDialogImage').IDialogImage;
    getImages(): import('./IDialogImage').IDialogImage[];
    hasImage(id: number): boolean;
    removeImage(id: number): void;
    clearImages(): void;

}
