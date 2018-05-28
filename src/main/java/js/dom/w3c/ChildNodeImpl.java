package js.dom.w3c;

import js.dom.ChildNode;
import js.dom.Element;

import org.w3c.dom.Node;

public class ChildNodeImpl implements ChildNode {
	private final DocumentImpl doc;
	private final Node node;

	public ChildNodeImpl(DocumentImpl doc, Node node) {
		this.doc = doc;
		this.node = node;
	}

	@Override
	public Element asElement() {
		return doc.getElement(node);
	}

	@Override
	public String asText() {
		return node.getTextContent();
	}

	@Override
	public boolean isElement() {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	@Override
	public boolean isText() {
		return node.getNodeType() == Node.TEXT_NODE;
	}
}
