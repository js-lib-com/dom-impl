package js.dom.w3c;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import js.dom.Attr;
import js.dom.ChildNode;
import js.dom.Document;
import js.dom.EList;
import js.dom.Element;
import js.dom.NamespaceContext;
import js.lang.BugError;
import js.util.Params;
import js.util.Strings;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Document element implementation.
 * 
 * @author Iulian Rotaru
 */
final class ElementImpl implements Element {
	/** Owner document. */
	private DocumentImpl ownerDoc;
	/** Wrapped W3C DOM Node interface. */
	private org.w3c.dom.Element node;

	/**
	 * Construct element for W3C DOM Node.
	 * 
	 * @param ownerDoc owner document.
	 * @param node wrapped W3C DOM Node interface.
	 */
	public ElementImpl(Document ownerDoc, Node node) {
		this.ownerDoc = (DocumentImpl) ownerDoc;
		this.node = (org.w3c.dom.Element) node;
	}

	/**
	 * Low level ;-) access to W3C DOM Node interface.
	 * 
	 * @return this element wrapped node.
	 */
	public Node getNode() {
		return node;
	}

	@Override
	public Element addChild(Element... child) {
		for (Element el : child) {
			Params.notNull(el, "Element");
			if (el.getDocument() != ownerDoc) {
				el = ownerDoc.importElement(el);
			}
			node.appendChild(node(el));
		}
		return this;
	}

	/** Attribute name for CSS class. */
	private static final String ATTR_CLASS = "class";

	@Override
	public Element addCssClass(String cssClass) {
		cssClass = cssClass.trim();
		if (!hasCssClass(cssClass)) {
			String existingCssClass = node.getAttribute(ATTR_CLASS);
			if (!existingCssClass.isEmpty()) {
				StringBuilder sb = new StringBuilder(existingCssClass);
				sb.append(' ');
				sb.append(cssClass);
				cssClass = sb.toString();
			}
			node.setAttribute(ATTR_CLASS, cssClass);
		}
		return this;
	}

	@Override
	public Element toggleCssClass(String cssClass) {
		if (hasCssClass(cssClass)) {
			removeCssClass(cssClass);
		} else {
			addCssClass(cssClass);
		}
		return this;
	}

	@Override
	public Element clone(boolean deep) {
		return ownerDoc.getElement(node.cloneNode(deep));
	}

	@Override
	public EList findByCss(String selector, Object... args) {
		Params.notNullOrEmpty(selector, "CSS selector");
		throw new UnsupportedOperationException("CSS selectors not supported.");
	}

	@Override
	public EList findByCssClass(String cssClass) {
		Params.notNullOrEmpty(cssClass, "CSS class");
		return findByXPath(XPATH.CSS_CLASS, cssClass);
	}

	@Override
	public EList findByTag(String tagName) {
		Params.notNullOrEmpty(tagName, "Tag name");
		return ownerDoc.createEList(node.getElementsByTagName(tagName));
	}

	@Override
	public EList findByTagNS(String namespaceURI, String tagName) {
		if (namespaceURI == null) {
			return findByTag(tagName);
		}
		Params.notNullOrEmpty(tagName, "Tag name");
		return ownerDoc.createEList(node.getElementsByTagNameNS(namespaceURI, tagName));
	}

	@Override
	public EList findByXPath(String xpath, Object... args) {
		Params.notNullOrEmpty(xpath, "XPath");
		return ownerDoc.evaluateXPathNodeList(node, xpath, args);
	}

	@Override
	public EList findByXPathNS(NamespaceContext namespaceContext, String xpath, Object... args) {
		Params.notNull(namespaceContext, "Name space context");
		Params.notNullOrEmpty(xpath, "XPath");
		return ownerDoc.evaluateXPathNodeListNS(node, namespaceContext, xpath, args);
	}

	@Override
	public Element getByAttr(String name, String... value) {
		return getByXPath(ownerDoc.buildAttrXPath(name, value));
	}

	@Override
	public EList findByAttr(String name, String... value) {
		return findByXPath(ownerDoc.buildAttrXPath(name, value));
	}

	// @Override
	// public EList findByAttrNS(final String namespaceURI, String name, String... value) {
	// return findByXPathNS(new NamespaceContext() {
	// @Override
	// public String getNamespaceURI(String prefix) {
	// return namespaceURI;
	// }
	// }, ownerDoc.buildAttrXPathNS(name, value));
	// }

	@Override
	public Iterable<Attr> getAttrs() {
		// TODO: decide if better to create custom iterator
		List<Attr> attrs = new ArrayList<Attr>();
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0, l = attributes.getLength(); i < l; i++) {
			org.w3c.dom.Attr a = (org.w3c.dom.Attr) attributes.item(i);
			attrs.add(new AttrImpl(a.getNodeName(), a.getNodeValue().trim()));
		}
		return Collections.unmodifiableList(attrs);
	}

	@Override
	public String getAttr(String name) {
		Params.notNullOrEmpty(name, "Attribute name");
		String s = node.getAttribute(name);
		return s.isEmpty() ? null : s;
	}

	@Override
	public String getAttrNS(String namespaceURI, String name) {
		if (namespaceURI == null) {
			return getAttr(name);
		}
		Params.notNullOrEmpty(name, "Attribute name");
		String s = node.getAttributeNS(namespaceURI, name);
		return s.isEmpty() ? null : s;
	}

	@Override
	public Element getByCss(String selector, Object... args) {
		Params.notNullOrEmpty(selector, "CSS selector");
		throw new UnsupportedOperationException("CSS selectors not supported.");
	}

	@Override
	public Element getByCssClass(String cssClass) {
		Params.notNullOrEmpty(cssClass, "CSS class");
		return ownerDoc.evaluateXPathNode(node, XPATH.CSS_CLASS, cssClass);
	}

	@Override
	public Element getByTag(String tagName) {
		Params.notNullOrEmpty(tagName, "Tag name");
		return ownerDoc.getElement(node.getElementsByTagName(tagName));
	}

	@Override
	public Element getByTagNS(String namespaceURI, String tagName) {
		if (namespaceURI == null) {
			return getByTag(tagName);
		}
		Params.notNullOrEmpty(tagName, "Tag name");
		return ownerDoc.getElement(node.getElementsByTagNameNS(namespaceURI, tagName));
	}

	@Override
	public Element getByXPath(String xpath, Object... args) {
		Params.notNullOrEmpty(xpath, "XPath");
		return ownerDoc.evaluateXPathNode(node, xpath, args);
	}

	@Override
	public Element getByXPathNS(NamespaceContext namespaceContext, String xpath, Object... args) {
		Params.notNull(namespaceContext, "Name space context");
		Params.notNullOrEmpty(xpath, "XPath");
		return ownerDoc.evaluateXPathNodeNS(node, namespaceContext, xpath, args);
	}

	@Override
	public EList getChildren() {
		NodeListImpl nodeList = new NodeListImpl();
		Node n = node.getFirstChild();
		while (n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				nodeList.add(n);
			}
			n = n.getNextSibling();
		}
		return new EListImpl(ownerDoc, nodeList);
	}

	@Override
	public Iterable<ChildNode> getChildNodes() {
		List<ChildNode> childNodes = new ArrayList<ChildNode>();

		Node n = node.getFirstChild();
		while (n != null) {
			if (n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.TEXT_NODE) {
				childNodes.add(new ChildNodeImpl(ownerDoc, n));
			}
			n = n.getNextSibling();
		}

		return childNodes;
	}

	@Override
	public Document getDocument() {
		return ownerDoc;
	}

	@Override
	public Element getFirstChild() {
		Node n = node.getFirstChild();
		if (n == null) {
			return null;
		}
		while (n.getNodeType() != Node.ELEMENT_NODE) {
			n = n.getNextSibling();
			if (n == null) {
				return null;
			}
		}
		return ownerDoc.getElement(n);
	}

	@Override
	public Element getLastChild() {
		Node n = node.getLastChild();
		if (n == null) {
			return null;
		}
		while (n.getNodeType() != Node.ELEMENT_NODE) {
			n = n.getPreviousSibling();
			if (n == null) {
				return null;
			}
		}
		return ownerDoc.getElement(n);
	}

	@Override
	public Element getNextSibling() {
		Node n = node.getNextSibling();
		if (n == null) {
			return null;
		}
		while (n.getNodeType() != Node.ELEMENT_NODE) {
			n = n.getNextSibling();
			if (n == null) {
				return null;
			}
		}
		return ownerDoc.getElement(n);
	}

	@Override
	public Element getParent() {
		Node n = node.getParentNode();
		// parent can be null if this node is not part of a document tree
		if (n == null) {
			return null;
		}
		// parent can be document if this element is html root
		return n.getNodeType() == Node.ELEMENT_NODE ? ownerDoc.getElement(n) : null;
	}

	@Override
	public Element getPreviousSibling() {
		Node n = node.getPreviousSibling();
		if (n == null) {
			return null;
		}
		while (n.getNodeType() != Node.ELEMENT_NODE) {
			n = n.getPreviousSibling();
			if (n == null) {
				return null;
			}
		}
		return ownerDoc.getElement(n);
	}

	@Override
	public String getTag() {
		return node.getNodeName().toLowerCase();
	}

	@Override
	public String getCaseSensitiveTag() {
		return node.getNodeName();
	}

	@Override
	public String getText() {
		return node.getTextContent();
	}

	@Override
	public boolean hasAttr(String name) {
		return node.getAttribute(name).length() > 0;
	}

	@Override
	public boolean hasAttrNS(String namespaceURI, String name) {
		return node.getAttributeNS(namespaceURI, name).length() > 0;
	}

	@Override
	public boolean hasChildren() {
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		return node.getChildNodes().getLength() == 0;
	}

	/** Regular expression for leading white spaces. */
	private static final String LEADING_SPACE_REX = "(?:^|\\s+)";
	/** Regular expression for trailing white spaces. */
	private static final String TRAILING_SPACE_REX = "(?:\\s+|$)";

	@Override
	public boolean hasCssClass(String classToMatch) {
		String classes = node.getAttribute(ATTR_CLASS);
		if (classes.isEmpty()) {
			return false;
		}
		Pattern pattern = Pattern.compile(Strings.concat(LEADING_SPACE_REX, Strings.escapeRegExp(classToMatch), TRAILING_SPACE_REX));
		Matcher matcher = pattern.matcher(classes);
		return matcher.find();
	}

	@Override
	public Element removeCssClass(String classToRemove) {
		String classes = node.getAttribute(ATTR_CLASS);
		if (classes.isEmpty()) {
			return this;
		}
		Pattern pattern = Pattern.compile(Strings.concat(LEADING_SPACE_REX, Strings.escapeRegExp(classToRemove), TRAILING_SPACE_REX));
		Matcher matcher = pattern.matcher(classes);
		node.setAttribute(ATTR_CLASS, matcher.replaceFirst(" ").trim());
		return this;
	}

	@Override
	public Element insertChild(Element child) {
		Params.notNull(child, "Child element");
		Element firstChild = getFirstChild();
		if (firstChild != null) {
			firstChild.insertBefore(child);
		} else {
			addChild(child);
		}
		return this;
	}

	@Override
	public Element insertChildren(EList children) {
		Params.notNull(children, "Child elements");
		Element firstChild = getFirstChild();
		if (firstChild != null) {
			for (Element child : children) {
				firstChild.insertBefore(child);
			}
		} else {
			for (Element child : children) {
				addChild(child);
			}
		}
		return this;
	}

	@Override
	public Element insertBefore(Element sibling) {
		Params.notNull(sibling, "Sibling element");
		if (sibling.getDocument() != ownerDoc) {
			sibling = ownerDoc.importElement(sibling);
		}
		Node parent = node.getParentNode();
		if (parent == null) {
			throw new IllegalStateException("Missing parent node.");
		}
		parent.insertBefore(node(sibling), node);
		return this;
	}

	@Override
	public void remove() {
		Node parentNode = node.getParentNode();
		if (parentNode != null) {
			node.getParentNode().removeChild(node);
		}
		node = null;
	}

	@Override
	public Element removeAttr(String name) {
		Params.notNullOrEmpty(name, "Attribute name");
		node.removeAttribute(name);
		return this;
	}

	@Override
	public Element removeAttrNS(String namespaceURI, String name) {
		if (namespaceURI == null) {
			return removeAttr(name);
		}
		Params.notNullOrEmpty(name, "Attribute name");
		node.removeAttributeNS(namespaceURI, name);
		return this;
	}

	@Override
	public Element removeChildren() {
		while (node.hasChildNodes()) {
			node.removeChild(node.getFirstChild());
		}
		return this;
	}

	@Override
	public Element removeText() {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				child.getParentNode().removeChild(child);
				--i;
			}
		}
		return this;
	}

	@Override
	public void replace(Element replacement) {
		Params.notNull(replacement, "Replacement element");
		if (replacement.getDocument() != ownerDoc) {
			replacement = ownerDoc.importElement(replacement);
		}
		node.getParentNode().replaceChild(node(replacement), node);
		node = (org.w3c.dom.Element) node(replacement);
	}

	@Override
	public Element replaceChild(Element replacement, Element existing) {
		Params.notNull(replacement, "Replacement element");
		Params.notNull(existing, "Exiting element");
		if (replacement.getDocument() != ownerDoc) {
			replacement = ownerDoc.importElement(replacement);
		}
		node.replaceChild(node(replacement), node(existing));
		return this;
	}

	@Override
	public Element setAttr(String name, String value) {
		Params.notNullOrEmpty(name, "Attribute name");
		Params.notNull(value, "Attribute value");
		node.setAttribute(name, value);
		return this;
	}

	@Override
	public Element setAttrNS(String namespaceURI, String name, String value) {
		if (namespaceURI == null) {
			return setAttr(name, value);
		}
		Params.notNullOrEmpty(name, "Attribute name");
		Params.notNull(value, "Attribute value");
		node.setAttributeNS(namespaceURI, name, value);
		return this;
	}

	@Override
	public Element setAttrs(String... nameValuePairs) {
		Params.isTrue(nameValuePairs.length % 2 == 0, "Missing value for last attribute.");
		for (int i = 0, l = nameValuePairs.length - 1; i < l; i += 2) {
			Params.notNull(nameValuePairs[i + 1], "Attribute value");
			node.setAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
		}
		return this;
	}

	@Override
	public Element addText(String text) {
		node.appendChild(ownerDoc.getDocument().createTextNode(text));
		return this;
	}

	@Override
	public Element setText(String text) {
		node.setTextContent(text);
		return this;
	}

	/** XML document prolog. */
	private static final String XML_DECL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	/** XML DOCTYPE for rich text entities. */
	private static final String XML_DOCTYPE = "<!DOCTYPE fragment [" + //
			"<!ENTITY nbsp \"&#160;\">" + //
			"<!ENTITY copy \"&#169;\">" + //
			"<!ENTITY reg \"&#174;\">" + //
			"]>";
	/** Start tag for rich text fragment. */
	private static final String XML_ROOT_START = "<fragment>";
	/** End tag for rich text fragment. */
	private static final String XML_ROOT_END = "</fragment>";

	@Override
	public Element setRichText(String richText) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new EntityResolverImpl());
			org.w3c.dom.Document doc = db.parse(new InputSource(new StringReader(Strings.concat(XML_DECL, XML_DOCTYPE, XML_ROOT_START, richText, XML_ROOT_END))));
			Node richTextNode = ownerDoc.getDocument().importNode(doc.getDocumentElement(), true);
			DocumentFragment richTextFragment = ownerDoc.getDocument().createDocumentFragment();
			while (richTextNode.hasChildNodes()) {
				richTextFragment.appendChild(richTextNode.removeChild(richTextNode.getFirstChild()));
			}
			removeChildren();
			node.appendChild(richTextFragment);
		} catch (ParserConfigurationException e) {
			throw new BugError(e);
		} catch (SAXException e) {
			throw new DomException("Invalid fragment source.");
		} catch (IOException e) {
			throw new BugError(e);
		}
		return this;
	}

	@Override
	public String getRichText() {
		StringBuilder sb = new StringBuilder();
		getRichText(node, sb);
		return sb.toString();
	}

	/**
	 * Extract rich text content from given node. It is not expected that rich text HTML formatting tags to have attributes and
	 * this builder just ignore them. Also ignores all other node types beside text and elements.
	 * 
	 * @param node source node,
	 * @param builder rich text target builder.
	 */
	private static void getRichText(Node node, StringBuilder builder) {
		Node n = node.getFirstChild();
		while (n != null) {
			if (n.getNodeType() == Node.TEXT_NODE) {
				builder.append(n.getNodeValue());
			} else if (n.getNodeType() == Node.ELEMENT_NODE) {
				builder.append('<');
				builder.append(n.getNodeName());
				builder.append('>');
				getRichText(n, builder);
				builder.append('<');
				builder.append('/');
				builder.append(n.getNodeName());
				builder.append('>');
			}
			n = n.getNextSibling();
		}
	}

	@Override
	public String trace() {
		StringBuilder sb = new StringBuilder();
		Element el = this;
		while (el != null) {
			int index = ((ElementImpl) el).index();
			if (index != -1) {
				sb.insert(0, ']');
				sb.insert(0, index);
				sb.insert(0, '[');
			}
			sb.insert(0, el.getTag());
			sb.insert(0, '/');
			el = el.getParent();
		}
		return sb.toString();
	}

	/**
	 * Return the index this element has in its parent children list. When determine the index only elements of the same kind
	 * are counted; returns -1 if this element is the only child of its kind. This helper method is used by {@link #trace()}.
	 * 
	 * @return this element index or -1 if only of its kind.
	 */
	private int index() {
		ElementImpl parent = (ElementImpl) getParent();
		if (parent == null) {
			return -1;
		}
		Node n = parent.node.getFirstChild();
		int index = 0;
		int twinsCount = 0;
		boolean indexFound = false;
		while (n != null) {
			if (n == node) {
				indexFound = true;
			}
			if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(node.getNodeName())) {
				++twinsCount;
				if (!indexFound) {
					++index;
				}
			}
			n = n.getNextSibling();
		}
		return twinsCount > 1 ? index : -1;
	}

	/**
	 * Element string representation.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(node.getNodeName());
		NamedNodeMap attrs = node.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			sb.append(' ');
			sb.append(attrs.item(i).getNodeName());
			sb.append('=');
			sb.append('\'');
			sb.append(attrs.item(i).getTextContent());
			sb.append('\'');
		}
		return sb.toString();
	}

	private static Node node(Element el) {
		return ((ElementImpl) el).node;
	}
}
