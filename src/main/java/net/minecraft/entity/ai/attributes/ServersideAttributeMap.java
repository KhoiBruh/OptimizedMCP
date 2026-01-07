package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Sets;
import net.minecraft.server.management.LowerStringMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ServersideAttributeMap extends BaseAttributeMap {
    protected final Map<String, IAttributeInstance> descriptionToAttributeInstanceMap = new LowerStringMap<>();
    private final Set<IAttributeInstance> attributeInstanceSet = Sets.newHashSet();

    public ModifiableAttributeInstance getAttributeInstance(IAttribute attribute) {
        return (ModifiableAttributeInstance) super.getAttributeInstance(attribute);
    }

    public ModifiableAttributeInstance getAttributeInstanceByName(String attributeName) {
        IAttributeInstance iattributeinstance = super.getAttributeInstanceByName(attributeName);

        if (iattributeinstance == null) {
            iattributeinstance = descriptionToAttributeInstanceMap.get(attributeName);
        }

        return (ModifiableAttributeInstance) iattributeinstance;
    }

    public IAttributeInstance registerAttribute(IAttribute attribute) {
        IAttributeInstance iattributeinstance = super.registerAttribute(attribute);

        if (attribute instanceof RangedAttribute && ((RangedAttribute) attribute).getDescription() != null) {
            descriptionToAttributeInstanceMap.put(((RangedAttribute) attribute).getDescription(), iattributeinstance);
        }

        return iattributeinstance;
    }

    protected IAttributeInstance func_180376_c(IAttribute attribute) {
        return new ModifiableAttributeInstance(this, attribute);
    }

    public void func_180794_a(IAttributeInstance instance) {
        if (instance.getAttribute().getShouldWatch()) {
            attributeInstanceSet.add(instance);
        }

        for (IAttribute iattribute : field_180377_c.get(instance.getAttribute())) {
            ModifiableAttributeInstance modifiableattributeinstance = getAttributeInstance(iattribute);

            if (modifiableattributeinstance != null) {
                modifiableattributeinstance.flagForUpdate();
            }
        }
    }

    public Set<IAttributeInstance> getAttributeInstanceSet() {
        return attributeInstanceSet;
    }

    public Collection<IAttributeInstance> getWatchedAttributes() {
        Set<IAttributeInstance> set = Sets.newHashSet();

        for (IAttributeInstance iattributeinstance : getAllAttributes()) {
            if (iattributeinstance.getAttribute().getShouldWatch()) {
                set.add(iattributeinstance);
            }
        }

        return set;
    }
}
