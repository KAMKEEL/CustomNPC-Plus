package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelBigSign extends ModelBase{
    public ModelRenderer signBoard = new ModelRenderer(this, 0, 0);

    public ModelBigSign()
    {
        this.signBoard.addBox(-8.0F, -8F, -1.0F, 16, 16, 2, 0.0F);
    }

    /**
     * Renders the sign model through TileEntitySignRenderer
     */
    public void renderSign()
    {
        this.signBoard.render(0.0625F);
    }
}
