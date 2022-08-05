package com.jslib.dom;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.EList;
import com.jslib.api.dom.Element;
import com.jslib.lang.BugError;
import com.jslib.util.Classes;
import com.jslib.util.Params;

/**
 * List of elements implementation.
 * 
 * @author Iulian Rotaru
 */
final class EListImpl implements EList {
	/** Owner document. */
	private Document ownerDoc;
	/** Wrapped W3C DOM NodeList interface. */
	private NodeList nodeList;

	/**
	 * Construct elements list instance.
	 * 
	 * @param ownerDoc owner document,
	 * @param nodeList nodes list.
	 * @throws IllegalArgumentException if any argument is null.
	 */
	public EListImpl(Document ownerDoc, NodeList nodeList) throws IllegalArgumentException {
		Params.notNull(ownerDoc, "Owner document");
		Params.notNull(nodeList, "Node list");
		this.ownerDoc = ownerDoc;
		this.nodeList = nodeList;
	}

	@Override
	public EList call(String elementMethodName, Object... args) {
		for (int i = 0; i < size(); ++i) {
			Element element = item(i);
			try {
				Classes.invoke(element, elementMethodName, args);
			} catch (Throwable e) {
				throw new BugError(e);
			}
		}
		return this;
	}

	@Override
	public boolean isEmpty() {
		return nodeList.getLength() == 0;
	}

	@Override
	public Element item(int index) {
		return new ElementImpl(ownerDoc, nodeList.item(index));
	}

	@Override
	public void remove() {
		while (nodeList.getLength() > 0) {
			nodeList.item(0).getParentNode().removeChild(nodeList.item(0));
		}
	}

	@Override
	public int size() {
		return nodeList.getLength();
	}

	@Override
	public Iterator<Element> iterator() {
		return new NodeListIterator();
	}

	/**
	 * Nodes list iterator used by elements list.
	 * 
	 * @author Iulian Rotaru
	 */
	private class NodeListIterator implements Iterator<Element> {
		/** Internal nodes index. */
		private int index = 0;
		/** Current node. */
		private Node node;

		/**
		 * Test if this iterator has more nodes.
		 */
		public boolean hasNext() {
			if (nodeList == null) {
				return false;
			}
			if (index == nodeList.getLength()) {
				return false;
			}
			node = nodeList.item(index++);
			// there are obscure conditions when node can be null at this point
			// discovered when running on real life documents but not understood
			// is like node list can hold null nodes...
			if (node == null) {
				return false;
			}
			return true;
		}

		/**
		 * Get next node wrapped into document element.
		 */
		public Element next() {
			return ((DocumentImpl) ownerDoc).getElement(node);
		}

		/**
		 * Remove operation is not supported.
		 */
		public void remove() {
			throw new UnsupportedOperationException("Nodes list iterator does not support node removing.");
		}
	}
}
