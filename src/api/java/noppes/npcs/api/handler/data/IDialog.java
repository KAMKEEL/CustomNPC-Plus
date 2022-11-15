//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler.data;
import java.util.List;

public interface IDialog {
    int getId();

    String getName();

    void setName(String var1);

    String getText();

    void setText(String var1);

    IQuest getQuest();

    void setQuest(IQuest var1);

    String getCommand();

    void setCommand(String var1);

    List<IDialogOption> getOptions();

    IDialogOption getOption(int var1);

    IAvailability getAvailability();

    IDialogCategory getCategory();

    void setDarkenScreen(boolean darkenScreen);
    boolean getDarkenScreen();

    void setDisableEsc(boolean disableEsc);
    boolean getDisableEsc();

    void setShowWheel(boolean showWheel);
    boolean getShowWheel();

    void setHideNPC(boolean hideNPC);
    boolean getHideNPC();

    void setSound(String sound);
    String getSound();

    void save();

    void setColor(int color);

    int getColor();

    void setTitleColor(int titleColor);

    int getTitleColor();

    void renderGradual(boolean gradual);

    boolean renderGradual();

    void showPreviousBlocks(boolean show);

    boolean showPreviousBlocks();

    void showOptionLine(boolean show);

    boolean showOptionLine();

    void setTextSound(String textSound);

    String getTextSound();

    void setTextPitch(float textPitch);

    float getTextPitch();

    void setTitlePos(int pos);

    int getTitlePos();

    void setNPCScale(float scale);

    float getNpcScale();

    void setNpcOffset(int offsetX, int offsetY);

    int getNpcOffsetX();

    int getNpcOffsetY();

    void textWidthHeight(int textWidth, int textHeight);

    int getTextWidth();

    int setTextHeight();

    void setTextOffset(int offsetX, int offsetY);

    int getTextOffsetX();

    int getTextOffsetY();

    void setTitleOffset(int offsetX, int offsetY);

    int getTitleOffsetX();

    int getTitleOffsetY();

    void setOptionOffset(int offsetX, int offsetY);

    int getOptionOffsetX();

    int getOptionOffsetY();

    void setOptionSpacing(int spaceX, int spaceY);

    int getOptionSpaceX();

    int getOptionSpaceY();

    void addImage(int id, IDialogImage image);

    IDialogImage getImage(int id);

    IDialogImage createImage();

    IDialogImage[] getImages();

    boolean hasImage(int id);

    void removeImage(int id);

    void clearImages();
}
