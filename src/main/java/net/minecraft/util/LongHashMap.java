package net.minecraft.util;

import java.util.Objects;

public class LongHashMap<V> {
    private final float percentUseable = 0.75F;
    private transient LongHashMap.Entry<V>[] hashArray = new LongHashMap.Entry[4096];
    private transient int numHashElements;
    private int mask;
    private int capacity = 3072;
    private transient volatile int modCount;

    public LongHashMap() {
        mask = hashArray.length - 1;
    }

    private static int getHashedKey(long originalKey) {
        return (int) (originalKey ^ originalKey >>> 27);
    }

    private static int hash(int integer) {
        integer = integer ^ integer >>> 20 ^ integer >>> 12;
        return integer ^ integer >>> 7 ^ integer >>> 4;
    }

    private static int getHashIndex(int p_76158_0_, int p_76158_1_) {
        return p_76158_0_ & p_76158_1_;
    }

    public int getNumHashElements() {
        return numHashElements;
    }

    public V getValueByKey(long p_76164_1_) {
        int i = getHashedKey(p_76164_1_);

        for (LongHashMap.Entry<V> entry = hashArray[getHashIndex(i, mask)]; entry != null; entry = entry.nextEntry) {
            if (entry.key == p_76164_1_) {
                return entry.value;
            }
        }

        return null;
    }

    public boolean containsItem(long p_76161_1_) {
        return getEntry(p_76161_1_) != null;
    }

    final LongHashMap.Entry<V> getEntry(long p_76160_1_) {
        int i = getHashedKey(p_76160_1_);

        for (LongHashMap.Entry<V> entry = hashArray[getHashIndex(i, mask)]; entry != null; entry = entry.nextEntry) {
            if (entry.key == p_76160_1_) {
                return entry;
            }
        }

        return null;
    }

    public void add(long p_76163_1_, V p_76163_3_) {
        int i = getHashedKey(p_76163_1_);
        int j = getHashIndex(i, mask);

        for (LongHashMap.Entry<V> entry = hashArray[j]; entry != null; entry = entry.nextEntry) {
            if (entry.key == p_76163_1_) {
                entry.value = p_76163_3_;
                return;
            }
        }

        ++modCount;
        createKey(i, p_76163_1_, p_76163_3_, j);
    }

    private void resizeTable(int p_76153_1_) {
        LongHashMap.Entry<V>[] entry = hashArray;
        int i = entry.length;

        if (i == 1073741824) {
            capacity = Integer.MAX_VALUE;
        } else {
            LongHashMap.Entry<V>[] entry1 = new LongHashMap.Entry[p_76153_1_];
            copyHashTableTo(entry1);
            hashArray = entry1;
            mask = hashArray.length - 1;
            float f = (float) p_76153_1_;
            getClass();
            capacity = (int) (f * 0.75F);
        }
    }

    private void copyHashTableTo(LongHashMap.Entry<V>[] p_76154_1_) {
        LongHashMap.Entry<V>[] entry = hashArray;
        int i = p_76154_1_.length;

        for (int j = 0; j < entry.length; ++j) {
            LongHashMap.Entry<V> entry1 = entry[j];

            if (entry1 != null) {
                entry[j] = null;

                while (true) {
                    LongHashMap.Entry<V> entry2 = entry1.nextEntry;
                    int k = getHashIndex(entry1.hash, i - 1);
                    entry1.nextEntry = p_76154_1_[k];
                    p_76154_1_[k] = entry1;
                    entry1 = entry2;

                    if (entry2 == null) {
                        break;
                    }
                }
            }
        }
    }

    public V remove(long p_76159_1_) {
        LongHashMap.Entry<V> entry = removeKey(p_76159_1_);
        return entry == null ? null : entry.value;
    }

    final LongHashMap.Entry<V> removeKey(long p_76152_1_) {
        int i = getHashedKey(p_76152_1_);
        int j = getHashIndex(i, mask);
        LongHashMap.Entry<V> entry = hashArray[j];
        LongHashMap.Entry<V> entry1;
        LongHashMap.Entry<V> entry2;

        for (entry1 = entry; entry1 != null; entry1 = entry2) {
            entry2 = entry1.nextEntry;

            if (entry1.key == p_76152_1_) {
                ++modCount;
                --numHashElements;

                if (entry == entry1) {
                    hashArray[j] = entry2;
                } else {
                    entry.nextEntry = entry2;
                }

                return entry1;
            }

            entry = entry1;
        }

        return entry1;
    }

    private void createKey(int p_76156_1_, long p_76156_2_, V p_76156_4_, int p_76156_5_) {
        LongHashMap.Entry<V> entry = hashArray[p_76156_5_];
        hashArray[p_76156_5_] = new LongHashMap.Entry<>(p_76156_1_, p_76156_2_, p_76156_4_, entry);

        if (numHashElements++ >= capacity) {
            resizeTable(2 * hashArray.length);
        }
    }

    public double getKeyDistribution() {
        int i = 0;

        for (Entry<V> vEntry : hashArray) {
            if (vEntry != null) {
                ++i;
            }
        }

        return (double) i / (double) numHashElements;
    }

    static class Entry<V> {
        final long key;
        final int hash;
        V value;
        LongHashMap.Entry<V> nextEntry;

        Entry(int p_i1553_1_, long p_i1553_2_, V p_i1553_4_, LongHashMap.Entry<V> p_i1553_5_) {
            value = p_i1553_4_;
            nextEntry = p_i1553_5_;
            key = p_i1553_2_;
            hash = p_i1553_1_;
        }

        public final long getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final boolean equals(Object p_equals_1_) {
            if (!(p_equals_1_ instanceof LongHashMap.Entry)) {
                return false;
            } else {
                LongHashMap.Entry<V> entry = (LongHashMap.Entry) p_equals_1_;
                Object object = key;
                Object object1 = entry.key;

                if (Objects.equals(object, object1)) {
                    Object object2 = value;
                    Object object3 = entry.value;

                    return Objects.equals(object2, object3);
                }

                return false;
            }
        }

        public final int hashCode() {
            return LongHashMap.getHashedKey(key);
        }

        public final String toString() {
            return key + "=" + value;
        }
    }
}
