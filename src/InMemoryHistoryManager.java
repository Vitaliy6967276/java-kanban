import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager{
    public class Node {
        public Task task;
        public Node prev;
        public Node next;
        public Node(Task task) {
            this.task = task;
        }
    }
    private final Map<Integer, Node> taskMap = new HashMap<>();
    private Node head;
    private Node tail;

    public void linkLast(Task task) {
        Node newNode = new Node(task);
        if(head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }
    public void removeNode(Node node) {
        if(node == null) {
            return;
        }
        if(node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if(node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        taskMap.remove(node.task.getId());
    }
    @Override
    public void add(Task task) {
        int taskId = task.getId();
        Node existingNode = taskMap.get(taskId);

        if (existingNode != null) {
            removeNode(existingNode);
        }

        Node newNode = new Node(task);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }

        taskMap.put(taskId, newNode);
    }
    public List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }
    @Override
    public List<Task> getHistory() {
        return getTasks();

    }
    @Override
    public void remove(int id) {
        Node node = taskMap.remove(id);
        if(node != null) {
            removeNode(node);
        }

    }
}
