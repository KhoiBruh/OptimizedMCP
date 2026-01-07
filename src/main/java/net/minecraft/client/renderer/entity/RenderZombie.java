package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class RenderZombie extends RenderBiped<EntityZombie> {
    private static final ResourceLocation zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation zombieVillagerTextures = new ResourceLocation("textures/entity/zombie/zombie_villager.png");
    private final ModelBiped field_82434_o;
    private final ModelZombieVillager zombieVillagerModel;
    private final List<LayerRenderer<EntityZombie>> field_177121_n;
    private final List<LayerRenderer<EntityZombie>> field_177122_o;

    public RenderZombie(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelZombie(), 0.5F, 1.0F);
        LayerRenderer<?> layerrenderer = layerRenderers.getFirst();
        field_82434_o = modelBipedMain;
        zombieVillagerModel = new ModelZombieVillager();
        addLayer(new LayerHeldItem(this));
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            protected void initArmor() {
                modelLeggings = new ModelZombie(0.5F, true);
                modelArmor = new ModelZombie(1.0F, true);
            }
        };
        addLayer(layerbipedarmor);
        field_177122_o = Lists.newArrayList(layerRenderers);

        if (layerrenderer instanceof LayerCustomHead) {
            removeLayer(layerrenderer);
            addLayer(new LayerCustomHead(zombieVillagerModel.bipedHead));
        }

        removeLayer(layerbipedarmor);
        addLayer(new LayerVillagerArmor(this));
        field_177121_n = Lists.newArrayList(layerRenderers);
    }

    public void doRender(EntityZombie entity, double x, double y, double z, float entityYaw, float partialTicks) {
        func_82427_a(entity);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityZombie entity) {
        return entity.isVillager() ? zombieVillagerTextures : zombieTextures;
    }

    private void func_82427_a(EntityZombie zombie) {
        if (zombie.isVillager()) {
            mainModel = zombieVillagerModel;
            layerRenderers = field_177121_n;
        } else {
            mainModel = field_82434_o;
            layerRenderers = field_177122_o;
        }

        modelBipedMain = (ModelBiped) mainModel;
    }

    protected void rotateCorpse(EntityZombie bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        if (bat.isConverting()) {
            p_77043_3_ += (float) (Math.cos((double) bat.ticksExisted * 3.25D) * Math.PI * 0.25D);
        }

        super.rotateCorpse(bat, p_77043_2_, p_77043_3_, partialTicks);
    }
}
