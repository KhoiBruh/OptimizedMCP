package net.minecraft.client.renderer.block.model;

import com.google.gson.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

public record BlockPartFace(EnumFacing cullFace, int tintIndex, String texture, BlockFaceUV blockFaceUV) {
    public static final EnumFacing FACING_DEFAULT = null;

    static class Deserializer implements JsonDeserializer<BlockPartFace> {
        public BlockPartFace deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            EnumFacing enumfacing = parseCullFace(jsonobject);
            int i = parseTintIndex(jsonobject);
            String s = parseTexture(jsonobject);
            BlockFaceUV blockfaceuv = p_deserialize_3_.deserialize(jsonobject, BlockFaceUV.class);
            return new BlockPartFace(enumfacing, i, s, blockfaceuv);
        }

        protected int parseTintIndex(JsonObject p_178337_1_) {
            return JsonUtils.getInt(p_178337_1_, "tintindex", -1);
        }

        private String parseTexture(JsonObject p_178340_1_) {
            return JsonUtils.getString(p_178340_1_, "texture");
        }

        private EnumFacing parseCullFace(JsonObject p_178339_1_) {
            String s = JsonUtils.getString(p_178339_1_, "cullface", "");
            return EnumFacing.byName(s);
        }
    }
}
