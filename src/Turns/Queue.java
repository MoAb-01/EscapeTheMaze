package Turns;

public class Queue<T> {
    private class Node {
        T data;
        Node next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public Queue() {
        head = null;
        tail = null;
        size = 0;
    }

    public void enqueue(T item) {
        Node newNode = new Node(item);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T item = head.data;
        head = head.next;
        size--;
        if (isEmpty()) {
            tail = null;
        }
        return item;
    }
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
    public CustomIterator iterator() {
        return new CustomIterator();
    }

    public class CustomIterator {
        private Node current;
        CustomIterator() {
            current = head;
        }
        public boolean hasNext() {
            return current != null;
        }
        public T next() {
            if (!hasNext()) {
                return null;
            }
            T item = current.data;
            current = current.next;
            return item;
        }
    }
    @Override
    public String toString() {
        String result = "[";
        Node current = head;
        while (current != null) {
            result += current.data;
            if (current.next != null) {
                result += ", ";
            }
            current = current.next;
        }
        result += "]";
        return result;
    }
}