package noppes.npcs.client.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.function.Consumer;

public class KeyPreset implements Comparable<KeyPreset> {
    /**
     * @field SINGLE_PRESS => on releasing before 5 ticks press time
     * @field PRESS_RELEASE => on pressing and releasing
     */
    /** Press types for key events. */
    public enum PressType { PRESS, HOLD, RELEASE, SINGLE_PRESS, PRESS_RELEASE }

    public KeyState defaultState = new KeyState();
    public KeyState currentState = new KeyState();

    public String name, description;
    public boolean shouldConflict = true;
    public Consumer<KeyEvent> task;

    private long pressStartMs = 0;
    public boolean isDown;


    public KeyPreset(String name) {
        this.name = name;
    }

    public KeyPreset setDefaultState(int keyCode, boolean hasCtrl, boolean hasAlt, boolean hasShift) {
        defaultState.setState(keyCode, hasCtrl, hasAlt, hasShift);
        currentState.setState(keyCode, hasCtrl, hasAlt, hasShift);
        return this;
    }

    public KeyPreset setDescription(String description) {
        this.description = description;
        return this;
    }

    public KeyPreset setTask(Consumer<KeyEvent> task) {
        this.task = task;
        return this;
    }

    public KeyPreset shouldConflict(boolean shouldConflict) {
        this.shouldConflict = shouldConflict;
        return this;
    }

    public void tick() {
        int keyCode = keyCode();
        if (keyCode == -1 || keyCode == 0)
            return;

        boolean isDown = isMouseKey() ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
        isDown = isDown && (hasCtrl() ? isCtrlKeyDown() : true) && (hasAlt() ? isAltKeyDown() : true) && (hasShift() ? isShiftKeyDown() : true);

        setDown(isDown);
    }

    private static final long SHORT_PRESS_MS = 250L;

    public void setDown(boolean down) {
        if (down && !isDown) { // just pressed
            pressStartMs = System.currentTimeMillis();
            onAction(PressType.PRESS, PressType.PRESS_RELEASE);
        }

        if (!down && isDown) { // just released
            if (getPressTime() <= SHORT_PRESS_MS) 
                onAction(PressType.SINGLE_PRESS);
            onAction(PressType.RELEASE, PressType.PRESS_RELEASE);
            pressStartMs = 0;
        }

        if (down) // while held
            onAction(PressType.HOLD);
        
        this.isDown = down;
    }
    
    public long getPressTime() {
        return pressStartMs > 0 ? (System.currentTimeMillis() - pressStartMs) : 0L;
    }

    public KeyPreset onAction(PressType... pressTypes) {
        if (task != null) {
            for (PressType pressType : pressTypes)
                this.task.accept(new KeyEvent(pressType, this));
        }
        return this;
    }

    public boolean isDefault() {
        return currentState.equals(defaultState);
    }

    public boolean isMouseKey() {
        return keyCode() < -1;
    }

    public int keyCode() {
        return currentState.keyCode;
    }

    public boolean hasCtrl() {
        return currentState.hasCtrl;
    }

    public boolean hasAlt() {
        return currentState.hasAlt;
    }

    public boolean hasShift() {
        return currentState.hasShift;
    }

    public String getKeyName() {
        return currentState.getName();
    }

    public void clear() {
        currentState.clear();
    }

    public void writeToNbt(NBTTagCompound c) {
        c.setTag(name, currentState.writeToNbt());
    }

    public void readFromNbt(NBTTagCompound compound) {
        currentState.readFromNbt(compound.getCompoundTag(name));
    }

    public boolean equals(Object preset) {
        if (preset == this)
            return true;

        if (preset instanceof KeyPreset) {
            KeyPreset key = (KeyPreset) preset;
            return key.currentState.equals(currentState);
        }

        return false;
    }

    @Override
    public int compareTo(KeyPreset o) {
        int mySpecialCount = getSpecialKeyCount(this);
        int otherSpecialCount = getSpecialKeyCount(o);

        return mySpecialCount - otherSpecialCount;
    }

    private static int getSpecialKeyCount(KeyPreset preset) {
        int res = 0;
        if (preset.hasAlt())
            res += 1;
        if (preset.hasCtrl())
            res += 1;
        if (preset.hasShift())
            res += 1;

        return res;
    }


    public static class KeyState {
        public int keyCode = -1;
        public boolean hasCtrl, hasAlt, hasShift;

        public void setState(int keyCode, boolean hasCtrl, boolean hasAlt, boolean hasShift) {
            this.keyCode = keyCode;
            this.hasCtrl = hasCtrl;
            this.hasAlt = hasAlt;
            this.hasShift = hasShift;
        }

        public void readFrom(KeyState state) {
            keyCode = state.keyCode;
            hasCtrl = state.hasCtrl;
            hasShift = state.hasShift;
            hasAlt = state.hasAlt;
        }

        public void writeTo(KeyState state) {
            state.setState(keyCode, hasCtrl, hasAlt, hasShift);
        }

        public boolean hasState() {
            return keyCode != -1;
        }

        public void clear() {
            this.keyCode = -1;
            hasCtrl = hasAlt = hasShift = false;
        }

        public boolean equals(Object preset) {
            if (preset == this)
                return true;

            if (preset instanceof KeyState) {
                KeyState state = (KeyState) preset;
                return state.keyCode == keyCode && state.hasCtrl == hasCtrl && state.hasAlt == hasAlt && state.hasShift == hasShift;
            }

            return false;
        }

        public NBTTagCompound writeToNbt() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("keyCode", keyCode);
            compound.setBoolean("hasCtrl", hasCtrl);
            compound.setBoolean("hasShift", hasShift);
            compound.setBoolean("hasAlt", hasAlt);
            return compound;
        }

        public void readFromNbt(NBTTagCompound compound) {
            this.keyCode = compound.getInteger("keyCode");
            this.hasCtrl = compound.getBoolean("hasCtrl");
            this.hasShift = compound.getBoolean("hasShift");
            this.hasAlt = compound.getBoolean("hasAlt");
        }

        public String getName() {
            int code = keyCode;
            String name = "";

            if (code == LEFT_MOUSE)
                name = "Left Mouse";
            else if (code == RIGHT_MOUSE)
                name = "Right Mouse";
            else if (code == MIDDLE_MOUSE)
                name = "Middle Mouse";
            else {
                name = code == -1 ? "" : GameSettings.getKeyDisplayString(code);
                if (name.contains("Button"))
                    name = name.replace("Button", "Mouse");
            }

            return (hasCtrl ? "CTRL " : "") + (hasAlt ? "ALT " : "") + (hasShift ? "SHIFT " : "") + name;
        }
    }

    public static final int LEFT_MOUSE = -100, RIGHT_MOUSE = -99, MIDDLE_MOUSE = -98, MOUSE_4 = -97, MOUSE_5 = -96;

    public static boolean isCtrlKeyDown() {
        return Minecraft.isRunningOnMac ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }

    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }

    public static boolean isNotCtrlAltShift(int key) {
        return key != 219 && key != 220 && key != 29 && key != 157 && key != 42 && key != 54 && key != 56 && key != 184;
    }
}
