package net.minecraft.client.renderer.block.model;

import net.minecraft.util.EnumFacing;
import org.joml.Vector3f;

public record BlockPartRotation(Vector3f origin, EnumFacing.Axis axis, float angle, boolean rescale) {
}
