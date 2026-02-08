package net.minecraft.client.resources.model;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.IIconCreator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IRegistry;
import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomItems;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class ModelBakery {
    protected static final ModelResourceLocation MODEL_MISSING = new ModelResourceLocation("builtin/missing",
            "missing");
    private static final Set<ResourceLocation> LOCATIONS_BUILTIN_TEXTURES = Sets.newHashSet(new ResourceLocation("blocks/water_flow"), new ResourceLocation("blocks/water_still"),
            new ResourceLocation("blocks/lava_flow"), new ResourceLocation("blocks/lava_still"),
            new ResourceLocation("blocks/destroy_stage_0"), new ResourceLocation("blocks/destroy_stage_1"),
            new ResourceLocation("blocks/destroy_stage_2"), new ResourceLocation("blocks/destroy_stage_3"),
            new ResourceLocation("blocks/destroy_stage_4"), new ResourceLocation("blocks/destroy_stage_5"),
            new ResourceLocation("blocks/destroy_stage_6"), new ResourceLocation("blocks/destroy_stage_7"),
            new ResourceLocation("blocks/destroy_stage_8"), new ResourceLocation("blocks/destroy_stage_9"),
            new ResourceLocation("items/empty_armor_slot_helmet"),
            new ResourceLocation("items/empty_armor_slot_chestplate"),
            new ResourceLocation("items/empty_armor_slot_leggings"),
            new ResourceLocation("items/empty_armor_slot_boots"));
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, String> BUILT_IN_MODELS = new HashMap<>();
    private static final Joiner JOINER = Joiner.on(" -> ");
    private static final ModelBlock MODEL_GENERATED = ModelBlock.deserialize(
            "{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private static final ModelBlock MODEL_COMPASS = ModelBlock.deserialize(
            "{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private static final ModelBlock MODEL_CLOCK = ModelBlock.deserialize(
            "{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private static final ModelBlock MODEL_ENTITY = ModelBlock.deserialize(
            "{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private static final Map<Item, Set<String>> customVariantNames = new HashMap<>();

    static {
        BUILT_IN_MODELS.put("missing",
                "{ \"textures\": {   \"particle\": \"missingno\",   \"missingno\": \"missingno\"}, \"elements\": [ {     \"from\": [ 0, 0, 0 ],     \"to\": [ 16, 16, 16 ],     \"faces\": {         \"down\":  { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"down\", \"texture\": \"#missingno\" },         \"up\":    { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"up\", \"texture\": \"#missingno\" },         \"north\": { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"north\", \"texture\": \"#missingno\" },         \"south\": { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"south\", \"texture\": \"#missingno\" },         \"west\":  { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"west\", \"texture\": \"#missingno\" },         \"east\":  { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"east\", \"texture\": \"#missingno\" }    }}]}");
        MODEL_GENERATED.name = "generation marker";
        MODEL_COMPASS.name = "compass generation marker";
        MODEL_CLOCK.name = "class generation marker";
        MODEL_ENTITY.name = "block entity marker";
    }

    private final IResourceManager resourceManager;
    private final Map<ResourceLocation, TextureAtlasSprite> sprites = Maps
            .newHashMap();
    private final Map<ResourceLocation, ModelBlock> models = Maps.newLinkedHashMap();
    private final Map<ModelResourceLocation, ModelBlockDefinition.Variants> variants = Maps.newLinkedHashMap();
    private final TextureMap textureMap;
    private final BlockModelShapes blockModelShapes;
    private final FaceBakery faceBakery = new FaceBakery();
    private final ItemModelGenerator itemModelGenerator = new ItemModelGenerator();
    private final Map<ResourceLocation, ModelBlockDefinition> blockDefinitions = Maps
            .newHashMap();
    private final RegistrySimple<ModelResourceLocation, IBakedModel> bakedRegistry = new RegistrySimple<>();
    private final Map<String, ResourceLocation> itemLocations = Maps.newLinkedHashMap();
    private final Map<Item, List<String>> variantNames = Maps.newIdentityHashMap();

    public ModelBakery(IResourceManager p_i46085_1_, TextureMap p_i46085_2_, BlockModelShapes p_i46085_3_) {
        resourceManager = p_i46085_1_;
        textureMap = p_i46085_2_;
        blockModelShapes = p_i46085_3_;
    }

    public static void fixModelLocations(ModelBlock p_fixModelLocations_0_, String p_fixModelLocations_1_) {
        ResourceLocation resourcelocation = fixModelLocation(p_fixModelLocations_0_.getParentLocation(),
                p_fixModelLocations_1_);

        if (resourcelocation != p_fixModelLocations_0_.parentLocation) {
            p_fixModelLocations_0_.parentLocation = resourcelocation;
        }

        Map<String, String> map = p_fixModelLocations_0_.textures;

        if (map != null) {
            for (Entry<String, String> entry : map.entrySet()) {
                String s = entry.getValue();
                String s1 = fixResourcePath(s, p_fixModelLocations_1_);

                if (s1 != s) {
                    entry.setValue(s1);
                }
            }
        }
    }

    public static ResourceLocation fixModelLocation(ResourceLocation p_fixModelLocation_0_,
                                                    String p_fixModelLocation_1_) {
        if (p_fixModelLocation_0_ != null && p_fixModelLocation_1_ != null) {
            if (!p_fixModelLocation_0_.getResourceDomain().equals("minecraft")) {
                return p_fixModelLocation_0_;
            } else {
                String s = p_fixModelLocation_0_.getResourcePath();
                String s1 = fixResourcePath(s, p_fixModelLocation_1_);

                if (s1 != s) {
                    p_fixModelLocation_0_ = new ResourceLocation(p_fixModelLocation_0_.getResourceDomain(), s1);
                }

                return p_fixModelLocation_0_;
            }
        } else {
            return p_fixModelLocation_0_;
        }
    }

    private static String fixResourcePath(String p_fixResourcePath_0_, String p_fixResourcePath_1_) {
        p_fixResourcePath_0_ = TextureUtils.fixResourcePath(p_fixResourcePath_0_, p_fixResourcePath_1_);
        p_fixResourcePath_0_ = StrUtils.removeSuffix(p_fixResourcePath_0_, ".json");
        p_fixResourcePath_0_ = StrUtils.removeSuffix(p_fixResourcePath_0_, ".png");
        return p_fixResourcePath_0_;
    }

    public IRegistry<ModelResourceLocation, IBakedModel> setupModelRegistry() {
        loadVariantItemModels();
        loadModelsCheck();
        loadSprites();
        bakeItemModels();
        bakeBlockModels();
        return bakedRegistry;
    }

    private void loadVariantItemModels() {
        loadVariants(blockModelShapes.getBlockStateMapper().putAllStateModelLocations().values());
        variants.put(MODEL_MISSING,
                new ModelBlockDefinition.Variants(MODEL_MISSING.getVariant(),
                        Lists.newArrayList(new ModelBlockDefinition.Variant(new ResourceLocation(MODEL_MISSING.getResourcePath()),
                                ModelRotation.X0_Y0, false, 1))));
        ResourceLocation resourcelocation = new ResourceLocation("item_frame");
        ModelBlockDefinition modelblockdefinition = getModelBlockDefinition(resourcelocation);
        registerVariant(modelblockdefinition, new ModelResourceLocation(resourcelocation, "normal"));
        registerVariant(modelblockdefinition, new ModelResourceLocation(resourcelocation, "map"));
        loadVariantModels();
        loadItemModels();
    }

    private void loadVariants(Collection<ModelResourceLocation> p_177591_1_) {
        for (ModelResourceLocation modelresourcelocation : p_177591_1_) {
            try {
                ModelBlockDefinition modelblockdefinition = getModelBlockDefinition(modelresourcelocation);

                try {
                    registerVariant(modelblockdefinition, modelresourcelocation);
                } catch (Exception exception) {
                    LOGGER.warn("Unable to load variant: {} from {}", modelresourcelocation.getVariant(), modelresourcelocation, exception);
                }
            } catch (Exception exception1) {
                LOGGER.warn("Unable to load definition {}", modelresourcelocation, exception1);
            }
        }
    }

    private void registerVariant(ModelBlockDefinition p_177569_1_, ModelResourceLocation p_177569_2_) {
        variants.put(p_177569_2_, p_177569_1_.getVariants(p_177569_2_.getVariant()));
    }

    private ModelBlockDefinition getModelBlockDefinition(ResourceLocation p_177586_1_) {
        ResourceLocation resourcelocation = getBlockStateLocation(p_177586_1_);
        ModelBlockDefinition modelblockdefinition = blockDefinitions.get(resourcelocation);

        if (modelblockdefinition == null) {
            List<ModelBlockDefinition> list = new ArrayList<>();

            try {
                for (IResource iresource : resourceManager.getAllResources(resourcelocation)) {
                    InputStream inputstream = null;

                    try {
                        inputstream = iresource.getInputStream();
                        ModelBlockDefinition modelblockdefinition1 = ModelBlockDefinition
                                .parseFromReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                        list.add(modelblockdefinition1);
                    } catch (Exception exception) {
                        throw new RuntimeException("Encountered an exception when loading model definition of '"
                                + p_177586_1_ + "' from: '" + iresource.getResourceLocation()
                                + "' in resourcepack: '" + iresource.getResourcePackName() + "'", exception);
                    } finally {
                        IOUtils.closeQuietly(inputstream);
                    }
                }
            } catch (IOException ioexception) {
                throw new RuntimeException("Encountered an exception when loading model definition of model "
                        + resourcelocation, ioexception);
            }

            modelblockdefinition = new ModelBlockDefinition(list);
            blockDefinitions.put(resourcelocation, modelblockdefinition);
        }

        return modelblockdefinition;
    }

    private ResourceLocation getBlockStateLocation(ResourceLocation p_177584_1_) {
        return new ResourceLocation(p_177584_1_.getResourceDomain(),
                "blockstates/" + p_177584_1_.getResourcePath() + ".json");
    }

    private void loadVariantModels() {
        for (Entry<ModelResourceLocation, ModelBlockDefinition.Variants> entry : variants.entrySet()) {
            for (ModelBlockDefinition.Variant modelblockdefinition$variant : entry.getValue().getVariants()) {
                ResourceLocation resourcelocation = modelblockdefinition$variant.getModelLocation();

                if (models.get(resourcelocation) == null) {
                    try {
                        ModelBlock modelblock = loadModel(resourcelocation);
                        models.put(resourcelocation, modelblock);
                    } catch (Exception exception) {
                        LOGGER.warn("Unable to load block model: '{}' for variant: '{}'", resourcelocation, entry.getKey(), exception);
                    }
                }
            }
        }
    }

    private ModelBlock loadModel(ResourceLocation p_177594_1_) throws IOException {
        String s = p_177594_1_.getResourcePath();

        return switch (s) {
            case "builtin/generated" -> MODEL_GENERATED;
            case "builtin/compass" -> MODEL_COMPASS;
            case "builtin/clock" -> MODEL_CLOCK;
            case "builtin/entity" -> MODEL_ENTITY;
            default -> {
                Reader reader;

                if (s.startsWith("builtin/")) {
                    String s1 = s.substring("builtin/".length());
                    String s2 = BUILT_IN_MODELS.get(s1);

                    if (s2 == null) {
                        throw new FileNotFoundException(p_177594_1_.toString());
                    }

                    reader = new StringReader(s2);
                } else {
                    p_177594_1_ = getModelLocation(p_177594_1_);
                    IResource iresource = resourceManager.getResource(p_177594_1_);
                    reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8);
                }

                ModelBlock modelblock;

                try {
                    ModelBlock modelblock1 = ModelBlock.deserialize(reader);
                    modelblock1.name = p_177594_1_.toString();
                    modelblock = modelblock1;
                    String s3 = TextureUtils.getBasePath(p_177594_1_.getResourcePath());
                    fixModelLocations(modelblock1, s3);
                } finally {
                    reader.close();
                }

                yield modelblock;
            }
        };
    }

    private ResourceLocation getModelLocation(ResourceLocation p_177580_1_) {
        ResourceLocation resourcelocation = p_177580_1_;
        String s = p_177580_1_.getResourcePath();

        if (!s.startsWith("mcpatcher") && !s.startsWith("optifine")) {
            return new ResourceLocation(p_177580_1_.getResourceDomain(),
                    "models/" + p_177580_1_.getResourcePath() + ".json");
        } else {
            if (!s.endsWith(".json")) {
                resourcelocation = new ResourceLocation(p_177580_1_.getResourceDomain(), s + ".json");
            }

            return resourcelocation;
        }
    }

    private void loadItemModels() {
        registerVariantNames();

        for (Item item : Item.itemRegistry) {
            for (String s : getVariantNames(item)) {
                ResourceLocation resourcelocation = getItemLocation(s);
                itemLocations.put(s, resourcelocation);

                if (models.get(resourcelocation) == null) {
                    try {
                        ModelBlock modelblock = loadModel(resourcelocation);
                        models.put(resourcelocation, modelblock);
                    } catch (Exception exception) {
                        LOGGER.warn("Unable to load item model: '{}' for item: '{}'", resourcelocation, Item.itemRegistry.getNameForObject(item), exception);
                    }
                }
            }
        }
    }

    public void loadItemModel(String p_loadItemModel_1_, ResourceLocation p_loadItemModel_2_,
                              ResourceLocation p_loadItemModel_3_) {
        itemLocations.put(p_loadItemModel_1_, p_loadItemModel_2_);

        if (models.get(p_loadItemModel_2_) == null) {
            try {
                ModelBlock modelblock = loadModel(p_loadItemModel_2_);
                models.put(p_loadItemModel_2_, modelblock);
            } catch (Exception exception) {
                LOGGER.warn("Unable to load item model: '{}' for item: '{}'",
                        new Object[]{p_loadItemModel_2_, p_loadItemModel_3_});
                LOGGER.warn("{}: {}", exception.getClass().getName(), exception.getMessage());
            }
        }
    }

    private void registerVariantNames() {
        variantNames.clear();
        variantNames.put(Item.getItemFromBlock(Blocks.stone), Lists.newArrayList("stone", "granite",
                "granite_smooth", "diorite", "diorite_smooth", "andesite", "andesite_smooth"));
        variantNames.put(Item.getItemFromBlock(Blocks.dirt),
                Lists.newArrayList("dirt", "coarse_dirt", "podzol"));
        variantNames.put(Item.getItemFromBlock(Blocks.planks), Lists.newArrayList("oak_planks",
                "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks"));
        variantNames.put(Item.getItemFromBlock(Blocks.sapling), Lists.newArrayList("oak_sapling",
                "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling"));
        variantNames.put(Item.getItemFromBlock(Blocks.sand),
                Lists.newArrayList("sand", "red_sand"));
        variantNames.put(Item.getItemFromBlock(Blocks.log),
                Lists.newArrayList("oak_log", "spruce_log", "birch_log", "jungle_log"));
        variantNames.put(Item.getItemFromBlock(Blocks.leaves),
                Lists.newArrayList("oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves"));
        variantNames.put(Item.getItemFromBlock(Blocks.sponge),
                Lists.newArrayList("sponge", "sponge_wet"));
        variantNames.put(Item.getItemFromBlock(Blocks.sandstone),
                Lists.newArrayList("sandstone", "chiseled_sandstone", "smooth_sandstone"));
        variantNames.put(Item.getItemFromBlock(Blocks.red_sandstone),
                Lists.newArrayList("red_sandstone", "chiseled_red_sandstone", "smooth_red_sandstone"));
        variantNames.put(Item.getItemFromBlock(Blocks.tallgrass),
                Lists.newArrayList("dead_bush", "tall_grass", "fern"));
        variantNames.put(Item.getItemFromBlock(Blocks.deadbush), Lists.newArrayList("dead_bush"));
        variantNames.put(Item.getItemFromBlock(Blocks.wool),
                Lists.newArrayList("black_wool", "red_wool", "green_wool", "brown_wool", "blue_wool",
                        "purple_wool", "cyan_wool", "silver_wool", "gray_wool", "pink_wool", "lime_wool", "yellow_wool",
                        "light_blue_wool", "magenta_wool", "orange_wool", "white_wool"));
        variantNames.put(Item.getItemFromBlock(Blocks.yellow_flower),
                Lists.newArrayList("dandelion"));
        variantNames.put(Item.getItemFromBlock(Blocks.red_flower),
                Lists.newArrayList("poppy", "blue_orchid", "allium", "houstonia", "red_tulip",
                        "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"));
        variantNames.put(Item.getItemFromBlock(Blocks.stone_slab),
                Lists.newArrayList("stone_slab", "sandstone_slab", "cobblestone_slab", "brick_slab",
                        "stone_brick_slab", "nether_brick_slab", "quartz_slab"));
        variantNames.put(Item.getItemFromBlock(Blocks.stone_slab2),
                Lists.newArrayList("red_sandstone_slab"));
        variantNames.put(Item.getItemFromBlock(Blocks.stained_glass),
                Lists.newArrayList("black_stained_glass", "red_stained_glass", "green_stained_glass",
                        "brown_stained_glass", "blue_stained_glass", "purple_stained_glass", "cyan_stained_glass",
                        "silver_stained_glass", "gray_stained_glass", "pink_stained_glass", "lime_stained_glass",
                        "yellow_stained_glass", "light_blue_stained_glass", "magenta_stained_glass",
                        "orange_stained_glass", "white_stained_glass"));
        variantNames.put(Item.getItemFromBlock(Blocks.monster_egg),
                Lists.newArrayList("stone_monster_egg", "cobblestone_monster_egg",
                        "stone_brick_monster_egg", "mossy_brick_monster_egg", "cracked_brick_monster_egg",
                        "chiseled_brick_monster_egg"));
        variantNames.put(Item.getItemFromBlock(Blocks.stonebrick), Lists.newArrayList(
                "stonebrick", "mossy_stonebrick", "cracked_stonebrick", "chiseled_stonebrick"));
        variantNames.put(Item.getItemFromBlock(Blocks.wooden_slab), Lists.newArrayList("oak_slab",
                "spruce_slab", "birch_slab", "jungle_slab", "acacia_slab", "dark_oak_slab"));
        variantNames.put(Item.getItemFromBlock(Blocks.cobblestone_wall),
                Lists.newArrayList("cobblestone_wall", "mossy_cobblestone_wall"));
        variantNames.put(Item.getItemFromBlock(Blocks.anvil),
                Lists.newArrayList("anvil_intact", "anvil_slightly_damaged", "anvil_very_damaged"));
        variantNames.put(Item.getItemFromBlock(Blocks.quartz_block),
                Lists.newArrayList("quartz_block", "chiseled_quartz_block", "quartz_column"));
        variantNames.put(Item.getItemFromBlock(Blocks.stained_hardened_clay),
                Lists.newArrayList("black_stained_hardened_clay", "red_stained_hardened_clay",
                        "green_stained_hardened_clay", "brown_stained_hardened_clay", "blue_stained_hardened_clay",
                        "purple_stained_hardened_clay", "cyan_stained_hardened_clay", "silver_stained_hardened_clay",
                        "gray_stained_hardened_clay", "pink_stained_hardened_clay", "lime_stained_hardened_clay",
                        "yellow_stained_hardened_clay", "light_blue_stained_hardened_clay",
                        "magenta_stained_hardened_clay", "orange_stained_hardened_clay",
                        "white_stained_hardened_clay"));
        variantNames.put(Item.getItemFromBlock(Blocks.stained_glass_pane),
                Lists.newArrayList("black_stained_glass_pane", "red_stained_glass_pane",
                        "green_stained_glass_pane", "brown_stained_glass_pane", "blue_stained_glass_pane",
                        "purple_stained_glass_pane", "cyan_stained_glass_pane", "silver_stained_glass_pane",
                        "gray_stained_glass_pane", "pink_stained_glass_pane", "lime_stained_glass_pane",
                        "yellow_stained_glass_pane", "light_blue_stained_glass_pane", "magenta_stained_glass_pane",
                        "orange_stained_glass_pane", "white_stained_glass_pane"));
        variantNames.put(Item.getItemFromBlock(Blocks.leaves2),
                Lists.newArrayList("acacia_leaves", "dark_oak_leaves"));
        variantNames.put(Item.getItemFromBlock(Blocks.log2),
                Lists.newArrayList("acacia_log", "dark_oak_log"));
        variantNames.put(Item.getItemFromBlock(Blocks.prismarine),
                Lists.newArrayList("prismarine", "prismarine_bricks", "dark_prismarine"));
        variantNames.put(Item.getItemFromBlock(Blocks.carpet),
                Lists.newArrayList("black_carpet", "red_carpet", "green_carpet", "brown_carpet",
                        "blue_carpet", "purple_carpet", "cyan_carpet", "silver_carpet", "gray_carpet", "pink_carpet",
                        "lime_carpet", "yellow_carpet", "light_blue_carpet", "magenta_carpet", "orange_carpet",
                        "white_carpet"));
        variantNames.put(Item.getItemFromBlock(Blocks.double_plant), Lists.newArrayList(
                "sunflower", "syringa", "double_grass", "double_fern", "double_rose", "paeonia"));
        variantNames.put(Items.bow,
                Lists.newArrayList("bow", "bow_pulling_0", "bow_pulling_1", "bow_pulling_2"));
        variantNames.put(Items.coal, Lists.newArrayList("coal", "charcoal"));
        variantNames.put(Items.fishing_rod,
                Lists.newArrayList("fishing_rod", "fishing_rod_cast"));
        variantNames.put(Items.fish,
                Lists.newArrayList("cod", "salmon", "clownfish", "pufferfish"));
        variantNames.put(Items.cooked_fish, Lists.newArrayList("cooked_cod", "cooked_salmon"));
        variantNames.put(Items.dye,
                Lists.newArrayList("dye_black", "dye_red", "dye_green", "dye_brown", "dye_blue",
                        "dye_purple", "dye_cyan", "dye_silver", "dye_gray", "dye_pink", "dye_lime", "dye_yellow",
                        "dye_light_blue", "dye_magenta", "dye_orange", "dye_white"));
        variantNames.put(Items.potionitem,
                Lists.newArrayList("bottle_drinkable", "bottle_splash"));
        variantNames.put(Items.skull, Lists.newArrayList(
                "skull_skeleton", "skull_wither", "skull_zombie", "skull_char", "skull_creeper"));
        variantNames.put(Item.getItemFromBlock(Blocks.oak_fence_gate),
                Lists.newArrayList("oak_fence_gate"));
        variantNames.put(Item.getItemFromBlock(Blocks.oak_fence),
                Lists.newArrayList("oak_fence"));
        variantNames.put(Items.oak_door, Lists.newArrayList("oak_door"));

        for (Entry<Item, Set<String>> entry : customVariantNames.entrySet()) {
            variantNames.put(entry.getKey(), Lists.newArrayList(((Set) entry.getValue()).iterator()));
        }

        CustomItems.update();
        CustomItems.loadModels(this);
    }

    private List<String> getVariantNames(Item p_177596_1_) {
        List<String> list = variantNames.get(p_177596_1_);

        if (list == null) {
            list = Collections.singletonList(
                    Item.itemRegistry.getNameForObject(p_177596_1_).toString());
        }

        return list;
    }

    private ResourceLocation getItemLocation(String p_177583_1_) {
        ResourceLocation resourcelocation = new ResourceLocation(p_177583_1_);

        // Forge hook removed

        return new ResourceLocation(resourcelocation.getResourceDomain(), "item/" + resourcelocation.getResourcePath());
    }

    private void bakeBlockModels() {
        for (Entry<ModelResourceLocation, ModelBlockDefinition.Variants> entry : variants.entrySet()) {
            ModelResourceLocation modelresourcelocation = entry.getKey();
            WeightedBakedModel.Builder weightedbakedmodel$builder = new WeightedBakedModel.Builder();
            int i = 0;

            for (ModelBlockDefinition.Variant modelblockdefinition$variant : entry.getValue().getVariants()) {
                ModelBlock modelblock = models.get(modelblockdefinition$variant.getModelLocation());

                if (modelblock != null && modelblock.isResolved()) {
                    ++i;
                    weightedbakedmodel$builder.add(
                            bakeModel(modelblock, modelblockdefinition$variant.getRotation(),
                                    modelblockdefinition$variant.isUvLocked()),
                            modelblockdefinition$variant.getWeight());
                } else {
                    LOGGER.warn("Missing model for: {}", modelresourcelocation);
                }
            }

            if (i == 0) {
                LOGGER.warn("No weighted models for: {}", modelresourcelocation);
            } else if (i == 1) {
                bakedRegistry.putObject(modelresourcelocation, weightedbakedmodel$builder.first());
            } else {
                bakedRegistry.putObject(modelresourcelocation, weightedbakedmodel$builder.build());
            }
        }

        for (Entry<String, ResourceLocation> entry : itemLocations.entrySet()) {
            ResourceLocation resourcelocation = entry.getValue();
            ModelResourceLocation modelresourcelocation1 = new ModelResourceLocation(entry.getKey(),
                    "inventory");

            // Forge hook removed

            ModelBlock modelblock1 = models.get(resourcelocation);

            if (modelblock1 != null && modelblock1.isResolved()) {
                if (isCustomRenderer(modelblock1)) {
                    bakedRegistry.putObject(modelresourcelocation1,
                            new BuiltInModel(modelblock1.getAllTransforms()));
                } else {
                    bakedRegistry.putObject(modelresourcelocation1,
                            bakeModel(modelblock1, ModelRotation.X0_Y0, false));
                }
            } else {
                LOGGER.warn("Missing model for: {}", resourcelocation);
            }
        }
    }

    private Set<ResourceLocation> getVariantsTextureLocations() {
        Set<ResourceLocation> set = new HashSet<>();
        List<ModelResourceLocation> list = Lists.newArrayList(variants.keySet());
        list.sort(Comparator.comparing(ModelResourceLocation::toString));

        for (ModelResourceLocation modelresourcelocation : list) {
            ModelBlockDefinition.Variants modelblockdefinition$variants = variants
                    .get(modelresourcelocation);

            for (ModelBlockDefinition.Variant modelblockdefinition$variant : modelblockdefinition$variants
                    .getVariants()) {
                ModelBlock modelblock = models.get(modelblockdefinition$variant.getModelLocation());

                if (modelblock == null) {
                    LOGGER.warn("Missing model for: {}", modelresourcelocation);
                } else {
                    set.addAll(getTextureLocations(modelblock));
                }
            }
        }

        set.addAll(LOCATIONS_BUILTIN_TEXTURES);
        return set;
    }

    protected IBakedModel bakeModel(ModelBlock p_bakeModel_1_, ModelRotation p_bakeModel_2_, boolean p_bakeModel_3_) {
        TextureAtlasSprite textureatlassprite = sprites
                .get(new ResourceLocation(p_bakeModel_1_.resolveTextureName("particle")));
        SimpleBakedModel.Builder simplebakedmodel$builder = (new SimpleBakedModel.Builder(p_bakeModel_1_))
                .setTexture(textureatlassprite);

        for (BlockPart blockpart : p_bakeModel_1_.getElements()) {
            for (EnumFacing enumfacing : blockpart.mapFaces().keySet()) {
                BlockPartFace blockpartface = blockpart.mapFaces().get(enumfacing);
                TextureAtlasSprite textureatlassprite1 = sprites
                        .get(new ResourceLocation(p_bakeModel_1_.resolveTextureName(blockpartface.texture())));
                boolean flag = true;

                if (blockpartface.cullFace() != null) {
                    simplebakedmodel$builder.addFaceQuad(p_bakeModel_2_.rotate(blockpartface.cullFace()),
                            makeBakedQuad(blockpart, blockpartface, textureatlassprite1, enumfacing,
                                    p_bakeModel_2_, p_bakeModel_3_));
                } else {
                    simplebakedmodel$builder.addGeneralQuad(makeBakedQuad(blockpart, blockpartface,
                            textureatlassprite1, enumfacing, p_bakeModel_2_, p_bakeModel_3_));
                }
            }
        }

        return simplebakedmodel$builder.makeBakedModel();
    }

    private BakedQuad makeBakedQuad(BlockPart p_177589_1_, BlockPartFace p_177589_2_, TextureAtlasSprite p_177589_3_,
                                    EnumFacing p_177589_4_, ModelRotation p_177589_5_, boolean p_177589_6_) {
        return faceBakery.makeBakedQuad(p_177589_1_.positionFrom(), p_177589_1_.positionTo(), p_177589_2_, p_177589_3_,
                p_177589_4_, p_177589_5_, p_177589_1_.partRotation(), p_177589_6_, p_177589_1_.shade());
    }

    private void loadModelsCheck() {
        loadModels();

        for (ModelBlock modelblock : models.values()) {
            modelblock.getParentFromMap(models);
        }

        ModelBlock.checkModelHierarchy(models);
    }

    private void loadModels() {
        Deque<ResourceLocation> deque = Queues.newArrayDeque();
        Set<ResourceLocation> set = new HashSet<>();

        for (Entry<ResourceLocation, ModelBlock> entry : models.entrySet()) {
            set.add(entry.getKey());
            ResourceLocation resourcelocation1 = entry.getValue().getParentLocation();

            if (resourcelocation1 != null) {
                deque.add(resourcelocation1);
            }
        }

        while (!deque.isEmpty()) {
            ResourceLocation resourcelocation2 = deque.pop();

            try {
                if (models.get(resourcelocation2) != null) {
                    continue;
                }

                ModelBlock modelblock = loadModel(resourcelocation2);
                models.put(resourcelocation2, modelblock);
                ResourceLocation resourcelocation3 = modelblock.getParentLocation();

                if (resourcelocation3 != null && !set.contains(resourcelocation3)) {
                    deque.add(resourcelocation3);
                }
            } catch (Exception var6) {
                LOGGER.warn("In parent chain: {}; unable to load model: '{}'", JOINER.join(getParentPath(resourcelocation2)), resourcelocation2);
            }

            set.add(resourcelocation2);
        }
    }

    private List<ResourceLocation> getParentPath(ResourceLocation p_177573_1_) {
        List<ResourceLocation> list = Lists.newArrayList(p_177573_1_);
        ResourceLocation resourcelocation = p_177573_1_;

        while ((resourcelocation = getParentLocation(resourcelocation)) != null) {
            list.addFirst(resourcelocation);
        }

        return list;
    }

    private ResourceLocation getParentLocation(ResourceLocation p_177576_1_) {
        for (Entry<ResourceLocation, ModelBlock> entry : models.entrySet()) {
            ModelBlock modelblock = entry.getValue();

            if (modelblock != null && p_177576_1_.equals(modelblock.getParentLocation())) {
                return entry.getKey();
            }
        }

        return null;
    }

    private Set<ResourceLocation> getTextureLocations(ModelBlock p_177585_1_) {
        Set<ResourceLocation> set = new HashSet<>();

        for (BlockPart blockpart : p_177585_1_.getElements()) {
            for (BlockPartFace blockpartface : blockpart.mapFaces().values()) {
                ResourceLocation resourcelocation = new ResourceLocation(
                        p_177585_1_.resolveTextureName(blockpartface.texture()));
                set.add(resourcelocation);
            }
        }

        set.add(new ResourceLocation(p_177585_1_.resolveTextureName("particle")));
        return set;
    }

    private void loadSprites() {
        final Set<ResourceLocation> set = getVariantsTextureLocations();
        set.addAll(getItemsTextureLocations());
        set.remove(TextureMap.LOCATION_MISSING_TEXTURE);
        IIconCreator iiconcreator = iconRegistry -> {
            for (ResourceLocation resourcelocation : set) {
                TextureAtlasSprite textureatlassprite = iconRegistry.registerSprite(resourcelocation);
                sprites.put(resourcelocation, textureatlassprite);
            }
        };
        textureMap.loadSprites(resourceManager, iiconcreator);
        sprites.put(new ResourceLocation("missingno"), textureMap.getMissingSprite());
    }

    private Set<ResourceLocation> getItemsTextureLocations() {
        Set<ResourceLocation> set = new HashSet<>();

        for (ResourceLocation resourcelocation : itemLocations.values()) {
            ModelBlock modelblock = models.get(resourcelocation);

            if (modelblock != null) {
                set.add(new ResourceLocation(modelblock.resolveTextureName("particle")));

                if (hasItemModel(modelblock)) {
                    for (String s : ItemModelGenerator.LAYERS) {
                        ResourceLocation resourcelocation2 = new ResourceLocation(modelblock.resolveTextureName(s));

                        if (modelblock.getRootModel() == MODEL_COMPASS
                                && !TextureMap.LOCATION_MISSING_TEXTURE.equals(resourcelocation2)) {
                            TextureAtlasSprite.setLocationNameCompass(resourcelocation2.toString());
                        } else if (modelblock.getRootModel() == MODEL_CLOCK
                                && !TextureMap.LOCATION_MISSING_TEXTURE.equals(resourcelocation2)) {
                            TextureAtlasSprite.setLocationNameClock(resourcelocation2.toString());
                        }

                        set.add(resourcelocation2);
                    }
                } else if (!isCustomRenderer(modelblock)) {
                    for (BlockPart blockpart : modelblock.getElements()) {
                        for (BlockPartFace blockpartface : blockpart.mapFaces().values()) {
                            ResourceLocation resourcelocation1 = new ResourceLocation(
                                    modelblock.resolveTextureName(blockpartface.texture()));
                            set.add(resourcelocation1);
                        }
                    }
                }
            }
        }

        return set;
    }

    private boolean hasItemModel(ModelBlock p_177581_1_) {
        if (p_177581_1_ == null) {
            return false;
        } else {
            ModelBlock modelblock = p_177581_1_.getRootModel();
            return modelblock == MODEL_GENERATED || modelblock == MODEL_COMPASS || modelblock == MODEL_CLOCK;
        }
    }

    private boolean isCustomRenderer(ModelBlock p_177587_1_) {
        if (p_177587_1_ == null) {
            return false;
        } else {
            ModelBlock modelblock = p_177587_1_.getRootModel();
            return modelblock == MODEL_ENTITY;
        }
    }

    private void bakeItemModels() {
        for (ResourceLocation resourcelocation : itemLocations.values()) {
            ModelBlock modelblock = models.get(resourcelocation);

            if (hasItemModel(modelblock)) {
                ModelBlock modelblock1 = makeItemModel(modelblock);

                if (modelblock1 != null) {
                    modelblock1.name = resourcelocation.toString();
                }

                models.put(resourcelocation, modelblock1);
            } else if (isCustomRenderer(modelblock)) {
                models.put(resourcelocation, modelblock);
            }
        }

        for (TextureAtlasSprite textureatlassprite : sprites.values()) {
            if (!textureatlassprite.hasAnimationMetadata()) {
                textureatlassprite.clearFramesTextureData();
            }
        }
    }

    private ModelBlock makeItemModel(ModelBlock p_177582_1_) {
        return itemModelGenerator.makeItemModel(textureMap, p_177582_1_);
    }

    public ModelBlock getModelBlock(ResourceLocation p_getModelBlock_1_) {
        return models.get(p_getModelBlock_1_);
    }
}
