/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package foxz.commandhelper.permissions;

import foxz.commandhelper.AbstractCommandHelper;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * @author foxz
 */
public class PlayerOnly extends AbstractPermission {

    @Override
    public String errorMsg() {
        return "Player Only";
    }

    @Override
    public boolean delegate(AbstractCommandHelper parent, String[] args) {
        return parent.pcParam instanceof EntityPlayer;
    }

}
