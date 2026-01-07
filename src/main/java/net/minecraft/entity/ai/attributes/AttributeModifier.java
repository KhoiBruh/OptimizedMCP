package net.minecraft.entity.ai.attributes;

import org.apache.commons.lang3.Validate;

import java.util.UUID;

public class AttributeModifier {
    private final double amount;
    private final int operation;
    private final String name;
    private final UUID id;
    private boolean isSaved;

    public AttributeModifier(String nameIn, double amountIn, int operationIn) {
        this(UUID.randomUUID(), nameIn, amountIn, operationIn);
    }

    public AttributeModifier(UUID idIn, String nameIn, double amountIn, int operationIn) {
        isSaved = true;
        id = idIn;
        name = nameIn;
        amount = amountIn;
        operation = operationIn;
        Validate.notEmpty(nameIn, "Modifier name cannot be empty");
        Validate.inclusiveBetween(0L, 2L, operationIn, "Invalid operation");
    }

    public UUID getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOperation() {
        return operation;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public AttributeModifier setSaved(boolean saved) {
        isSaved = saved;
        return this;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && getClass() == p_equals_1_.getClass()) {
            AttributeModifier attributemodifier = (AttributeModifier) p_equals_1_;

            if (id != null) {
                return id.equals(attributemodifier.id);
            } else return attributemodifier.id == null;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String toString() {
        return "AttributeModifier{amount=" + amount + ", operation=" + operation + ", name='" + name + '\'' + ", id=" + id + ", serialize=" + isSaved + '}';
    }
}
