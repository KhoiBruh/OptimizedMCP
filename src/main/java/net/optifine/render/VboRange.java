package net.optifine.render;

import net.optifine.util.LinkedList;

public class VboRange {
    private int position = -1;
    private int size = 0;
    private final LinkedList.Node<VboRange> node = new LinkedList.Node<>(this);

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPositionNext() {
        return position + size;
    }

    public LinkedList.Node<VboRange> getNode() {
        return node;
    }

    public VboRange getPrev() {
        LinkedList.Node<VboRange> node = this.node.getPrev();
        return node == null ? null : node.getItem();
    }

    public VboRange getNext() {
        LinkedList.Node<VboRange> node = this.node.getNext();
        return node == null ? null : node.getItem();
    }

    public String toString() {
        return position + "/" + size + "/" + (position + size);
    }
}
