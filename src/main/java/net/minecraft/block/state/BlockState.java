package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.Cartesian;
import net.minecraft.util.MapPopulator;

import java.util.*;

public class BlockState {
    private static final Joiner COMMA_JOINER = Joiner.on(", ");
    private static final Function<IProperty, String> GET_NAME_FUNC = p_apply_1_ -> p_apply_1_ == null ? "<NULL>" : p_apply_1_.getName();
    private final Block block;
    private final ImmutableList<IProperty> properties;
    private final ImmutableList<IBlockState> validStates;

    public BlockState(Block blockIn, IProperty... properties) {
        block = blockIn;
        Arrays.sort(properties, (p_compare_1_, p_compare_2_) -> p_compare_1_.getName().compareTo(p_compare_2_.getName()));
        this.properties = ImmutableList.copyOf(properties);
        Map<Map<IProperty, Comparable>, BlockState.StateImplementation> map = Maps.newLinkedHashMap();
        List<BlockState.StateImplementation> list = Lists.newArrayList();

        for (List<Comparable> list1 : Cartesian.cartesianProduct(getAllowedValues())) {
            Map<IProperty, Comparable> map1 = MapPopulator.createMap(this.properties, list1);
            BlockState.StateImplementation blockstate$stateimplementation = new BlockState.StateImplementation(blockIn, ImmutableMap.copyOf(map1));
            map.put(map1, blockstate$stateimplementation);
            list.add(blockstate$stateimplementation);
        }

        for (BlockState.StateImplementation blockstate$stateimplementation1 : list) {
            blockstate$stateimplementation1.buildPropertyValueTable(map);
        }

        validStates = ImmutableList.copyOf(list);
    }

    public ImmutableList<IBlockState> getValidStates() {
        return validStates;
    }

    private List<Iterable<Comparable>> getAllowedValues() {
        List<Iterable<Comparable>> list = Lists.newArrayList();

        for (int i = 0; i < properties.size(); ++i) {
            list.add(properties.get(i).getAllowedValues());
        }

        return list;
    }

    public IBlockState getBaseState() {
        return validStates.get(0);
    }

    public Block getBlock() {
        return block;
    }

    public Collection<IProperty> getProperties() {
        return properties;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("block", Block.blockRegistry.getNameForObject(block)).add("properties", Iterables.transform(properties, GET_NAME_FUNC)).toString();
    }

    static class StateImplementation extends BlockStateBase {
        private final Block block;
        private final ImmutableMap<IProperty, Comparable> properties;
        private ImmutableTable<IProperty, Comparable, IBlockState> propertyValueTable;

        private StateImplementation(Block blockIn, ImmutableMap<IProperty, Comparable> propertiesIn) {
            block = blockIn;
            properties = propertiesIn;
        }

        public Collection<IProperty> getPropertyNames() {
            return Collections.unmodifiableCollection(properties.keySet());
        }

        public <T extends Comparable<T>> T getValue(IProperty<T> property) {
            if (!properties.containsKey(property)) {
                throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + block.getBlockState());
            } else {
                return property.getValueClass().cast(properties.get(property));
            }
        }

        public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
            if (!properties.containsKey(property)) {
                throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + block.getBlockState());
            } else if (!property.getAllowedValues().contains(value)) {
                throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.blockRegistry.getNameForObject(block) + ", it is not an allowed value");
            } else {
                return properties.get(property) == value ? this : propertyValueTable.get(property, value);
            }
        }

        public ImmutableMap<IProperty, Comparable> getProperties() {
            return properties;
        }

        public Block getBlock() {
            return block;
        }

        public boolean equals(Object p_equals_1_) {
            return this == p_equals_1_;
        }

        public int hashCode() {
            return properties.hashCode();
        }

        public void buildPropertyValueTable(Map<Map<IProperty, Comparable>, BlockState.StateImplementation> map) {
            if (propertyValueTable != null) {
                throw new IllegalStateException();
            } else {
                Table<IProperty, Comparable, IBlockState> table = HashBasedTable.create();

                for (IProperty<? extends Comparable> iproperty : properties.keySet()) {
                    for (Comparable comparable : iproperty.getAllowedValues()) {
                        if (comparable != properties.get(iproperty)) {
                            table.put(iproperty, comparable, map.get(getPropertiesWithValue(iproperty, comparable)));
                        }
                    }
                }

                propertyValueTable = ImmutableTable.copyOf(table);
            }
        }

        private Map<IProperty, Comparable> getPropertiesWithValue(IProperty property, Comparable value) {
            Map<IProperty, Comparable> map = Maps.newHashMap(properties);
            map.put(property, value);
            return map;
        }
    }
}
