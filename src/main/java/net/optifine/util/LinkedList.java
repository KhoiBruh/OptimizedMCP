package net.optifine.util;

import java.util.Iterator;

public class LinkedList<T> {
    private LinkedList.Node<T> first;
    private LinkedList.Node<T> last;
    private int size;

    public void addFirst(LinkedList.Node<T> tNode) {
        checkNoParent(tNode);

        if (isEmpty()) {
            first = tNode;
            last = tNode;
        } else {
            LinkedList.Node<T> node = first;
            tNode.setNext(node);
            node.setPrev(tNode);
            first = tNode;
        }

        tNode.setParent(this);
        ++size;
    }

    public void addLast(LinkedList.Node<T> tNode) {
        checkNoParent(tNode);

        if (isEmpty()) {
            first = tNode;
            last = tNode;
        } else {
            LinkedList.Node<T> node = last;
            tNode.setPrev(node);
            node.setNext(tNode);
            last = tNode;
        }

        tNode.setParent(this);
        ++size;
    }

    public void addAfter(LinkedList.Node<T> nodePrev, LinkedList.Node<T> tNode) {
        if (nodePrev == null) {
            addFirst(tNode);
        } else if (nodePrev == last) {
            addLast(tNode);
        } else {
            checkParent(nodePrev);
            checkNoParent(tNode);
            LinkedList.Node<T> nodeNext = nodePrev.getNext();
            nodePrev.setNext(tNode);
            tNode.setPrev(nodePrev);
            nodeNext.setPrev(tNode);
            tNode.setNext(nodeNext);
            tNode.setParent(this);
            ++size;
        }
    }

    public void remove(Node<T> tNode) {
        checkParent(tNode);
        LinkedList.Node<T> prev = tNode.getPrev();
        LinkedList.Node<T> next = tNode.getNext();

        if (prev != null) {
            prev.setNext(next);
        } else {
            first = next;
        }

        if (next != null) {
            next.setPrev(prev);
        } else {
            last = prev;
        }

        tNode.setPrev(null);
        tNode.setNext(null);
        tNode.setParent(null);
        --size;
    }

    private void checkParent(LinkedList.Node<T> node) {
        if (node.parent != this) {
            throw new IllegalArgumentException("Node has different parent, node: " + node + ", parent: " + node.parent + ", this: " + this);
        }
    }

    private void checkNoParent(LinkedList.Node<T> node) {
        if (node.parent != null) {
            throw new IllegalArgumentException("Node has different parent, node: " + node + ", parent: " + node.parent + ", this: " + this);
        }
    }

    public boolean contains(LinkedList.Node<T> node) {
        return node.parent == this;
    }

    public Iterator<LinkedList.Node<T>> iterator() {
        return new Iterator<>() {
            Node<T> node = getFirst();

            public boolean hasNext() {
                return node != null;
            }

            public Node<T> next() {
                Node<T> node = this.node;

                if (this.node != null) {
                    this.node = this.node.next;
                }

                return node;
            }

        };
    }

    public LinkedList.Node<T> getFirst() {
        return first;
    }

    public LinkedList.Node<T> getLast() {
        return last;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size <= 0;
    }

    public String toString() {
        StringBuilder stringbuffer = new StringBuilder();

        for (Iterator<Node<T>> it = iterator(); it.hasNext(); ) {
            Node<T> node = it.next();
            if (!stringbuffer.isEmpty()) {
                stringbuffer.append(", ");
            }

            stringbuffer.append(node.getItem());
        }

        return size + " [" + stringbuffer + "]";
    }

    public static class Node<T> {
        private final T item;
        private LinkedList.Node<T> prev;
        private LinkedList.Node<T> next;
        private LinkedList<T> parent;

        public Node(T item) {
            this.item = item;
        }

        public T getItem() {
            return item;
        }

        public LinkedList.Node<T> getPrev() {
            return prev;
        }

        private void setPrev(LinkedList.Node<T> prev) {
            this.prev = prev;
        }

        public LinkedList.Node<T> getNext() {
            return next;
        }

        private void setNext(LinkedList.Node<T> next) {
            this.next = next;
        }

        private void setParent(LinkedList<T> parent) {
            this.parent = parent;
        }

        public String toString() {
            return "" + item;
        }
    }
}
