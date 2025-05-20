package Agent;

public class StringStack {
    private class Node {
        String data;
        Node next;
        Node(String data) { this.data = data; }
    }
    private Node top;
    private int size;

    public StringStack() {
        top = null;
        size = 0;
    }

    public void push(String item) {
        Node newNode = new Node(item);
        newNode.next = top;
        top = newNode;
        size++;
    }

    public String pop() {
        if (isEmpty()) throw new IllegalStateException("Stack is empty");
        String item = top.data;
        top = top.next;
        size--;
        return item;
    }

    public String peek() {
        if (isEmpty()) throw new IllegalStateException("Stack is empty");
        return top.data;
    }

    public boolean isEmpty() { return top == null; }
    public int size() { return size; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node current = top;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) sb.append(", ");
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}