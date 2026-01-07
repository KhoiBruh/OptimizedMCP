package net.optifine;

import java.util.Comparator;
import java.util.Objects;

public class CustomItemsComparator implements Comparator<CustomItemProperties> {
    @Override
    public int compare(CustomItemProperties properties, CustomItemProperties other) {
        return properties.weight != other.weight ? other.weight - properties.weight : (
                !Objects.equals(properties.basePath, other.basePath) ? properties.basePath.compareTo(other.basePath) : properties.name.compareTo(other.name)
        );
    }
}
