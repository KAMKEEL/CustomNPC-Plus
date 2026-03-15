/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.client.renderer.entity
 */

/**
 * @javaFqn net.minecraft.client.renderer.entity.NPCRendererHelper
 */
export class NPCRendererHelper {
    getMainModel(render: import('./RendererLivingEntity').RendererLivingEntity): import('../../model/ModelBase').ModelBase;
    getTexture(render: import('./RendererLivingEntity').RendererLivingEntity, entity: import('../../../entity/Entity').Entity): String;
    shouldRenderPass(entity: import('../../../entity/EntityLivingBase').EntityLivingBase, par2: import('./int').int, par3: import('./float').float, renderEntity: import('./RendererLivingEntity').RendererLivingEntity): import('./int').int;
    preRenderCallback(entity: import('../../../entity/EntityLivingBase').EntityLivingBase, f: import('./float').float, renderEntity: import('./RendererLivingEntity').RendererLivingEntity): import('./void').void;
    getPassModel(render: import('./RendererLivingEntity').RendererLivingEntity): import('../../model/ModelBase').ModelBase;
    handleRotationFloat(entity: import('../../../entity/EntityLivingBase').EntityLivingBase, par2: import('./float').float, renderEntity: import('./RendererLivingEntity').RendererLivingEntity): import('./float').float;
    renderEquippedItems(entity: import('../../../entity/EntityLivingBase').EntityLivingBase, f: import('./float').float, renderEntity: import('./RendererLivingEntity').RendererLivingEntity): import('./void').void;
}
