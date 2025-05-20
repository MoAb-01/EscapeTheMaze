package Agent;

public class AgentList {
    private class Node {
        Agent data;
        Node next;
        Node(Agent data) { this.data = data; }
    }
    private Node head;
    private int size;

    public AgentList() {
        head = null;
        size = 0;
    }

    public void add(Agent item) {
        Node newNode = new Node(item);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    public Agent get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    public int size() { return size; }
    public boolean isEmpty() { return head == null; }
}