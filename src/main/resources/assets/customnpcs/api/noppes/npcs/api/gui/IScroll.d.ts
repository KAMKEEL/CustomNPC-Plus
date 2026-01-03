/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

export interface IScroll extends ICustomGuiComponent {

    // Methods
    getWidth(): number;
    getHeight(): number;
    setSize(width: number, height: number): import('./IScroll').IScroll;
    getList(): string[];
    setList(textList: string[]): import('./IScroll').IScroll;
    getDefaultSelection(): number;
    setDefaultSelection(defaultSelection: number): import('./IScroll').IScroll;
    isMultiSelect(): boolean;
    setMultiSelect(selectMultiple: boolean): import('./IScroll').IScroll;

}
