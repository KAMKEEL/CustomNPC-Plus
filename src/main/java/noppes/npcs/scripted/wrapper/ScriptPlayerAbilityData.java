package noppes.npcs.scripted.wrapper;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.api.ability.IPlayerAbilityData;
import noppes.npcs.controllers.data.PlayerAbilityData;
import noppes.npcs.controllers.data.PlayerData;

/**
 * Script wrapper for PlayerAbilityData providing API access to player abilities.
 */
public class ScriptPlayerAbilityData implements IPlayerAbilityData {

    private final PlayerAbilityData data;
    private final PlayerData playerData;

    public ScriptPlayerAbilityData(PlayerData playerData) {
        this.playerData = playerData;
        this.data = playerData.abilityData;
    }

    @Override
    public String[] getUnlockedAbilities() {
        return data.getUnlockedAbilities();
    }

    @Override
    public void unlockAbility(String key) {
        // Validate ability exists and allows players
        Ability ability = AbilityController.Instance.resolveAbility(key);
        if (ability == null) {
            return; // Ability doesn't exist
        }
        if (!ability.getAllowedBy().allowsPlayer()) {
            return; // Ability is NPC-only
        }
        // Use canonical key (registry key for built-in, UUID for custom)
        String canonicalKey = ability.getId() != null ? ability.getId() : key;
        data.unlockAbility(canonicalKey);
    }

    @Override
    public void lockAbility(String key) {
        data.lockAbility(key);
    }

    @Override
    public boolean hasUnlockedAbility(String key) {
        return data.hasUnlockedAbility(key);
    }

    @Override
    public int getSelectedIndex() {
        return data.getSelectedIndex();
    }

    @Override
    public void setSelectedIndex(int index) {
        data.setSelectedIndex(index);
    }

    @Override
    public String getSelectedAbilityKey() {
        return data.getSelectedAbilityKey();
    }

    @Override
    public void selectNext() {
        data.selectNext();
    }

    @Override
    public void selectPrevious() {
        data.selectPrevious();
    }

    @Override
    public boolean isExecutingAbility() {
        return data.isExecutingAbility();
    }

    @Override
    public IAbility getCurrentAbility() {
        return data.getCurrentAbility();
    }

    @Override
    public void interruptCurrentAbility() {
        data.interruptCurrentAbility();
    }

    @Override
    public boolean isOnCooldown() {
        return data.isOnCooldown();
    }

    @Override
    public boolean isOnCooldown(String key) {
        EntityPlayer player = playerData.player;
        if (player == null) return false;
        return data.isOnCooldown(key, player);
    }

    @Override
    public void resetCooldown() {
        data.resetCooldown();
    }

    @Override
    public void resetCooldown(String key) {
        data.resetCooldown(key);
    }

    @Override
    public void resetAllCooldowns() {
        data.resetAllCooldowns();
    }

    @Override
    public boolean activateAbility() {
        EntityPlayer player = playerData.player;
        if (player == null) return false;
        return data.activateAbility(player);
    }

    @Override
    public boolean activateAbility(String key) {
        EntityPlayer player = playerData.player;
        if (player == null) return false;
        return data.activateAbility(player, key);
    }
}
