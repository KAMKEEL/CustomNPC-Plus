package noppes.npcs.controllers.data.action;

import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.*;

public class ActionLogger {
    // private final Map<Object, List<?>> snapshot = new HashMap<>();
    private final ActionManager manager;
    private final Deque<TreeNode> stack = new ArrayDeque<>();
    private TreeNode root, currentNode;

    static class TreeNode {
        public final Object component; //ActionManager, or ActionQueue, or Action, etc...
        public final List<TreeNode> children = new ArrayList<>();
        public TreeNode parent;

        public boolean isLastChild() {
            if (parent == null)
                return true;
            List<TreeNode> siblings = parent.children;
            return siblings.get(siblings.size() - 1) == this;
        }

        public TreeNode(Object component) {
            this.component = component;
        }

        public TreeNode addChild(Object component) {
            TreeNode child = new TreeNode(component);
            children.add(child);
            child.parent = this;
            return child;
        }
    }

    public ActionLogger(ActionManager manager) {
        this.manager = manager;
    }

    public ActionLogger beginTick(ActionManager manager) {
        stack.clear();

        root = currentNode = new TreeNode(manager);
        for (IActionQueue queue : manager.getAllQueues()) {
            if (!queue.hasActiveTasks())
                continue;
            TreeNode queueNode = root.addChild(queue);

            for (IAction action : queue.getQueue())
                queueNode.addChild(action);
        }

        return this;
    }

    public ActionLogger push(Object component) {
        for (TreeNode child : currentNode.children) {
            if (child.component.equals(component)) {
                currentNode = child;
                break;
            }
        }

        stack.push(currentNode);
        return this;
    }

    public ActionLogger pop() {
        if (!stack.isEmpty())
            currentNode = stack.pop();

        return this;
    }

    public ActionLogger finish(String finished, Object component) {
        log(finished, component);
        log(StringUtils.repeat("-", finished.length()), component, true);
        return this;
    }

    public ActionLogger log(String message, Object source) {
        return log(message, source, false);
    }

    public ActionLogger log(String message, Object source, boolean isLast) {
        StringBuilder prefix = new StringBuilder();

        ListIterator<TreeNode> it = new ArrayList<>(stack).listIterator(stack.size());

        while (it.hasPrevious()) {
            TreeNode node = it.previous();
            if (it.hasPrevious())
                prefix.append("│    ");
            else
                prefix.append(isLast ? "└──── " : "├──── ");
        }

        String header = getLogHeader(source);
        println(prefix + header + message);
        return this;
    }

    public void error(String err, Throwable t) {
        if (manager.reportTo != null)
            manager.reportTo.appendConsole(err + (t != null ? "\n" + ExceptionUtils.getStackTrace(t) : ""));

        LogWriter.error(err);
        if (t != null)
            t.printStackTrace();
    }

    private String getLogHeader(Object obj) {
        String type = obj.getClass().getSimpleName();
        String name = "";

        if (obj instanceof ActionManager) {
            name = ((ActionManager) obj).getName();
        } else if (obj instanceof IActionQueue) {
            name = ((IActionQueue) obj).getName();
        } else if (obj instanceof IAction) {
            name = ((IAction) obj).getName();
        } else if (obj instanceof ActionThread) {
            name = ((ActionThread) obj).getAction().getName();
        }

        return String.format("[%s/'%s'] ", type, name);
    }

    private static final String ANSI_CYAN = "\u001B[96m";
    private static final String ANSI_GOLD = "\u001B[93m";
    private static final String ANSI_RESET = "\u001B[0m";

    public void println(String str) {
        System.out.printf("[%tT] %s[%s]:%s %s%s%n", new Date(), ANSI_CYAN, Thread.currentThread().getName(), ANSI_GOLD, str, ANSI_RESET);
    }

    //    public ActionLogger beginTick1(ActionManager manager) {
    //        snapshot.clear();
    //        stack.clear();
    //
    //        List<IActionQueue> queues = Arrays.stream(manager.getAllQueues()).filter(IActionQueue::hasActiveTasks).collect(Collectors.toList());
    //        snapshot.put(manager, queues);
    //
    //        for (IActionQueue queue : queues) {
    //            LinkedList<Object> contents = new LinkedList<>();
    //            contents.add(queue);
    //
    //            for (IAction a : queue.getQueue())
    //                contents.add(a);
    //
    //            // Actions only (not the queue itself)
    //            snapshot.put(queue, contents.subList(1, contents.size()));
    //        }
    //
    //        return this;
    //    }

    //
    //    private Object getParent() {
    //        return stack.isEmpty() ? null : stack.peek().component;
    //    }

    //    public ActionLogger push1(Object component) {
    //        List<?> siblings = snapshot.get(getParent());
    //        boolean isLast = siblings != null && !siblings.isEmpty() && siblings.get(siblings.size() - 1).equals(component);
    //
    //        stack.push(new TreeNode(component));
    //        return this;
    //    }

    //
    //    public void log(String message, Object source, int in) {
    //        StringBuilder prefix = new StringBuilder();
    //        Deque<Boolean> branches = branchStack.get();
    //        int depth = branches.size();
    //
    //        Iterator<Boolean> iter = branches.descendingIterator();
    //        for (int i = 0; i < depth - 1; i++) {
    //            prefix.append(iter.next() ? "    " : "│   ");
    //        }
    //
    //        if (depth > 0) {
    //            prefix.append(branches.peek() ? "└── " : "├── ");
    //        }
    //
    //        String header = getHeader(source);
    //        int pad = Math.max(0, maxHeaderLength - header.length());
    //        String formatted = prefix + header + StringUtils.repeat(" ", pad) + message;
    //
    //        output.accept(formatted);
    //    }
}
