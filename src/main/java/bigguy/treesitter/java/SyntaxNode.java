package bigguy.treesitter.java;

import org.treesitter.TSNode;
import org.treesitter.TSPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A read-only, Java-idiomatic view over a tree-sitter {@link TSNode}.
 *
 * <p>Wraps the native node with null-safe accessors, built-in span
 * conversion, and convenience traversal methods. Unlike {@code TSNode},
 * this class never throws on null/missing children — it returns {@code null}
 * instead, letting callers use standard null-checks.</p>
 */
public final class SyntaxNode {

    private final TSNode node;
    private final String sourceText;

    SyntaxNode(TSNode node, String sourceText) {
        this.node = node;
        this.sourceText = sourceText;
    }

    public boolean isNull() {
        return node == null || node.isNull();
    }

    public String getType() {
        return isNull() ? null : node.getType();
    }

    public boolean isNamed() {
        return !isNull() && node.isNamed();
    }

    public boolean hasError() {
        return !isNull() && node.hasError();
    }

    public boolean isError() {
        return !isNull() && node.isError();
    }

    public boolean isMissing() {
        return !isNull() && node.isMissing();
    }

    public TextSpan getSpan() {
        if (isNull()) return null;
        TSPoint start = node.getStartPoint();
        TSPoint end = node.getEndPoint();
        return new TextSpan(
                start.getRow(), start.getColumn(),
                end.getRow(), end.getColumn(),
                node.getStartByte(), node.getEndByte()
        );
    }

    /**
     * Extracts the source text covered by this node.
     *
     * @return the text, or {@code null} if no source is available
     */
    public String getText() {
        if (isNull() || sourceText == null) return null;
        int start = node.getStartByte();
        int end = Math.min(node.getEndByte(), sourceText.length());
        if (start < 0 || start >= end) return "";
        return sourceText.substring(start, end);
    }

    public int getChildCount() {
        return isNull() ? 0 : node.getChildCount();
    }

    public SyntaxNode getChild(int index) {
        if (isNull()) return null;
        TSNode child = node.getChild(index);
        return (child == null || child.isNull()) ? null : new SyntaxNode(child, sourceText);
    }

    public SyntaxNode getNamedChild(int index) {
        if (isNull()) return null;
        TSNode child = node.getNamedChild(index);
        return (child == null || child.isNull()) ? null : new SyntaxNode(child, sourceText);
    }

    public SyntaxNode getChildByFieldName(String fieldName) {
        if (isNull() || fieldName == null) return null;
        TSNode child = node.getChildByFieldName(fieldName);
        return (child == null || child.isNull()) ? null : new SyntaxNode(child, sourceText);
    }

    public SyntaxNode getParent() {
        if (isNull()) return null;
        TSNode parent = node.getParent();
        return (parent == null || parent.isNull()) ? null : new SyntaxNode(parent, sourceText);
    }

    public SyntaxNode getNextSibling() {
        if (isNull()) return null;
        TSNode sib = node.getNextSibling();
        return (sib == null || sib.isNull()) ? null : new SyntaxNode(sib, sourceText);
    }

    public SyntaxNode getNextNamedSibling() {
        if (isNull()) return null;
        TSNode sib = node.getNextNamedSibling();
        return (sib == null || sib.isNull()) ? null : new SyntaxNode(sib, sourceText);
    }

    public SyntaxNode getPrevSibling() {
        if (isNull()) return null;
        TSNode sib = node.getPrevSibling();
        return (sib == null || sib.isNull()) ? null : new SyntaxNode(sib, sourceText);
    }

    public int getNamedChildCount() {
        return isNull() ? 0 : node.getNamedChildCount();
    }

    /**
     * Collects all named children into a list.
     *
     * @return an unmodifiable list of named children, never {@code null}
     */
    public List<SyntaxNode> getNamedChildren() {
        if (isNull()) return Collections.emptyList();
        int count = node.getNamedChildCount();
        List<SyntaxNode> children = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            TSNode child = node.getNamedChild(i);
            if (child != null && !child.isNull()) {
                children.add(new SyntaxNode(child, sourceText));
            }
        }
        return Collections.unmodifiableList(children);
    }

    /**
     * Finds the first descendant matching the given node type.
     *
     * @param nodeType the tree-sitter node type string
     * @return the first matching descendant, or {@code null}
     */
    public SyntaxNode findFirst(String nodeType) {
        if (isNull() || nodeType == null) return null;
        return findFirstRecursive(this, nodeType);
    }

    private static SyntaxNode findFirstRecursive(SyntaxNode current, String nodeType) {
        if (nodeType.equals(current.getType())) return current;
        int count = current.getChildCount();
        for (int i = 0; i < count; i++) {
            SyntaxNode child = current.getChild(i);
            if (child == null) continue;
            SyntaxNode found = findFirstRecursive(child, nodeType);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Collects all descendants matching the given node type.
     *
     * @param nodeType the tree-sitter node type string
     * @return an unmodifiable list of matching descendants
     */
    public List<SyntaxNode> findAll(String nodeType) {
        if (isNull() || nodeType == null) return Collections.emptyList();
        List<SyntaxNode> result = new ArrayList<>();
        findAllRecursive(this, nodeType, result);
        return Collections.unmodifiableList(result);
    }

    private static void findAllRecursive(SyntaxNode current, String nodeType, List<SyntaxNode> out) {
        if (nodeType.equals(current.getType())) out.add(current);
        int count = current.getChildCount();
        for (int i = 0; i < count; i++) {
            SyntaxNode child = current.getChild(i);
            if (child != null) findAllRecursive(child, nodeType, out);
        }
    }

    /**
     * @return the S-expression representation of this node's subtree
     */
    public String toSExpression() {
        return isNull() ? "(null)" : node.toString();
    }

    TSNode unwrap() {
        return node;
    }

    @Override
    public String toString() {
        if (isNull()) return "SyntaxNode{null}";
        TextSpan span = getSpan();
        return String.format("SyntaxNode{%s %s}", getType(), span);
    }
}
