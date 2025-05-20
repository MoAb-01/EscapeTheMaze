package Maze;

public class CircularLinkedList {
    private Node head;
    private Node tail;

    public static class Node {
        MazeTile tile;
        Node next;
        Node(MazeTile tile) { this.tile = tile; }
    }


    public CircularLinkedList() {
        head = null;
        tail = null;
    }
    public Node getHead() { return head; }

    public void add(MazeTile tile) {
        Node newNode = new Node(tile);
        if (head == null) {
            head = newNode;
            tail = newNode;
            newNode.next = head;
        } else {
            tail.next = newNode;
            tail = newNode;
            tail.next = head;
        }
    }

    public void rotate() {
        if (head != null) {
            head = head.next;
            tail = tail.next;
        }
    }
    public String toString() {
        if (head == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Node current = head;
        do {
            sb.append(current.tile.getType()).append(",");
            current = current.next;
        } while (current != head);
        sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

}