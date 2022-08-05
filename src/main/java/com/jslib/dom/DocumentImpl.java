package com.jslib.dom;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.html.dom.HTMLDocumentImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.EList;
import com.jslib.api.dom.Element;
import com.jslib.api.dom.NamespaceContext;
import com.jslib.lang.BugError;
import com.jslib.util.Params;

/**
 * Master document implementation.
 * 
 * @author Iulian Rotaru
 */
public final class DocumentImpl implements Document
{
  /** Back reference key is used to store element instance to W3C node, as user defined data. */
  private static final String BACK_REF = "__js_element__";

  /** Wrapped W3C DOM document object. */
  private final org.w3c.dom.Document doc;

  /**
   * Construct document object wrapping native W3C DOM document.
   * 
   * @param doc native DOM document.
   */
  public DocumentImpl(org.w3c.dom.Document doc)
  {
    this.doc = doc;
  }

  @Override
  public boolean isXML()
  {
    return !(doc instanceof HTMLDocumentImpl);
  }

  /**
   * Get the element associated to node. Returns the element bound to given node. If no element instance found, create a
   * new {@link Element} instance, bound it to node then returns it. Returns null is given node is undefined or null.
   * <p>
   * Element instance is saved on node using {@link Node#setUserData(String, Object, org.w3c.dom.UserDataHandler)} and
   * reused. See {@link #BACK_REF} for key used to store element instance.
   * 
   * @param node native W3C DOM Node.
   * @return element wrapping the given node or null.
   */
  Element getElement(Node node)
  {
    if(node == null) {
      return null;
    }
    // element instance is cached as node user defined data and reused
    Object value = node.getUserData(BACK_REF);
    if(value != null) {
      return (Element)value;
    }
    Element el = new ElementImpl(this, node);
    node.setUserData(BACK_REF, el, null);
    return el;
  }

  /**
   * Overload of the {@link #getElement(Node)} method using first node from given W3C DOM nodes list. Returns null if
   * <code>nodeList</code> parameter is empty.
   * 
   * @param nodeList native DOM nodes list, possible empty.
   * @return element instance or null.
   * @throws IllegalArgumentException if nodes list parameter is null.
   */
  Element getElement(NodeList nodeList)
  {
    Params.notNull(nodeList, "Nodes list");
    if(nodeList.getLength() == 0) {
      return null;
    }
    return getElement(nodeList.item(0));
  }

  /**
   * Elements list factory. Create a new list of elements wrapping native W3C DOM nodes. If <code>nodeList</code>
   * parameter has no items returned elements list is empty.
   * 
   * @param nodeList native DOM nodes list.
   * @return newly created elements list, possible empty.
   * @throws IllegalArgumentException if nodes list parameter is null.
   */
  EList createEList(NodeList nodeList)
  {
    Params.notNull(nodeList, "Nodes list");
    return new EListImpl(this, nodeList);
  }

  /**
   * Low level ;-) access to W3C DOM Document interface.
   * 
   * @return wrapped W3C DOM document.
   */
  public org.w3c.dom.Document getDocument()
  {
    return doc;
  }

  @Override
  public Element createElement(String tagName, String... attrNameValues)
  {
    Params.notNullOrEmpty(tagName, "Tag name");
    Params.isTrue(attrNameValues.length % 2 == 0, "Missing value for last attribute.");

    Element el = getElement(doc.createElement(tagName));
    if(attrNameValues.length > 0) {
      el.setAttrs(attrNameValues);
    }
    return el;
  }

  @Override
  public Element createElementNS(String namespaceURI, String tagName, String... attrNameValues)
  {
    if(namespaceURI == null) {
      return createElement(tagName, attrNameValues);
    }
    Params.notNullOrEmpty(tagName, "Tag name");
    Element el = getElement(doc.createElementNS(namespaceURI, tagName));
    if(attrNameValues.length > 0) {
      el.setAttrsNS(namespaceURI, attrNameValues);
    }
    return el;
  }

  @Override
  public Element importElement(Element el)
  {
    Params.notNull(el, "Element");
    Params.isTrue(el.getDocument() != this, "Element already belongs to this document.");
    return getElement(doc.importNode(((ElementImpl)el).getNode(), true));
  }

  @Override
  public Element getRoot()
  {
    return getElement(doc.getDocumentElement());
  }

  @Override
  public Element getById(String id)
  {
    Params.notNullOrEmpty(id, "ID");
    return getElement(doc.getElementById(id));
  }

  @Override
  public Element getByTag(String tagName)
  {
    Params.notNullOrEmpty(tagName, "Tag name");
    return getElement(doc.getElementsByTagName(tagName));
  }

  @Override
  public Element getByTagNS(String namespaceURI, String tagName)
  {
    if(namespaceURI == null) {
      return getByTag(tagName);
    }
    Params.notNullOrEmpty(tagName, "Tag name");
    return getElement(doc.getElementsByTagNameNS(namespaceURI, tagName));
  }

  @Override
  public EList findByCssClass(String cssClass)
  {
    Params.notNull(cssClass, "CSS class");
    if(cssClass.isEmpty()) {
      return createEList(new NodeListImpl());
    }
    try {
      return findByXPath(XPATH.getElementsByClassName(cssClass));
    }
    catch(XPathExpressionException e) {
      // XPath expression is build internally and cannot fail
      throw new BugError(e);
    }
  }

  @Override
  public EList findByTag(String tagName)
  {
    Params.notNullOrEmpty(tagName, "Tag name");
    return createEList(doc.getElementsByTagName(tagName));
  }

  @Override
  public EList findByTagNS(String namespaceURI, String tagName)
  {
    if(namespaceURI == null) {
      return findByTag(tagName);
    }
    Params.notNullOrEmpty(tagName, "Tag name");
    return createEList(doc.getElementsByTagNameNS(namespaceURI, tagName));
  }

  @Override
  public EList findByXPath(String xpath, Object... args) throws XPathExpressionException
  {
    Params.notNullOrEmpty(xpath, "XPath");
    return createEList(XPATH.evaluateXPathNodeList(doc, xpath, args));
  }

  @Override
  public EList findByXPathNS(NamespaceContext namespaceContext, String xpath, Object... args) throws XPathExpressionException
  {
    Params.notNull(namespaceContext, "Namespace context");
    Params.notNullOrEmpty(xpath, "XPath");
    return createEList(XPATH.evaluateXPathNodeListNS(doc, namespaceContext, xpath, args));
  }

  @Override
  public EList findByXPathNS(String namespaceURI, String xpath, Object... args) throws XPathExpressionException
  {
    Params.notNullOrEmpty(namespaceURI, "Namespace URI");
    Params.notNullOrEmpty(xpath, "XPath");
    return createEList(XPATH.evaluateXPathNodeListNS(doc, new NamespaceContext()
    {
      @Override
      public String getNamespaceURI(String prefix)
      {
        // it is expected to be used on documents with a single namespace
        return namespaceURI;
      }
    }, xpath, args));
  }

  @Override
  public Element getByCssClass(String cssClass)
  {
    Params.notNullOrEmpty(cssClass, "CSS class");
    try {
      return getByXPath(XPATH.getElementsByClassName(cssClass));
    }
    catch(XPathExpressionException e) {
      // XPath expression is build internally and cannot fail
      throw new BugError(e);
    }
  }

  @Override
  public Element getByXPath(String xpath, Object... args) throws XPathExpressionException
  {
    Params.notNullOrEmpty(xpath, "XPath");
    return getElement(XPATH.evaluateXPathNode(doc, xpath, args));
  }

  @Override
  public Element getByXPathNS(NamespaceContext namespaceContext, String xpath, Object... args) throws XPathExpressionException
  {
    Params.notNull(namespaceContext, "Namespace context");
    Params.notNullOrEmpty(xpath, "XPath");
    return getElement(XPATH.evaluateXPathNodeNS(doc, namespaceContext, xpath, args));
  }

  @Override
  public Element getByXPathNS(String namespaceURI, String xpath, Object... args) throws XPathExpressionException
  {
    Params.notNullOrEmpty(namespaceURI, "Namespace URI");
    Params.notNullOrEmpty(xpath, "XPath");
    return getElement(XPATH.evaluateXPathNodeNS(doc, new NamespaceContext()
    {
      @Override
      public String getNamespaceURI(String prefix)
      {
        // it is expected to be used on documents with a single namespace
        return namespaceURI;
      }
    }, xpath, args));
  }

  @Override
  public Element getByAttr(String name, String... value)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    try {
      return getByXPath(XPATH.getElementsByAttrNameValue(name, value));
    }
    catch(XPathExpressionException e) {
      // XPath expression is build internally and cannot fail
      throw new BugError(e);
    }
  }

  @Override
  public Element getByAttrNS(String namespaceURI, String name, String... value)
  {
    if(namespaceURI == null) {
      return getByAttr(name, value);
    }
    Params.notNullOrEmpty(name, "Attribute name");

    String namespacePrefix = "ns";
    try {
      return getByXPathNS(new NamespaceContext()
      {
        @Override
        public String getNamespaceURI(String prefix)
        {
          return prefix.equals(namespacePrefix) ? namespaceURI : null;
        }
      }, XPATH.getElementsByAttrNameValueNS(namespacePrefix, name, value));
    }
    catch(XPathExpressionException e) {
      // XPath expression is build internally and cannot fail
      throw new BugError(e);
    }
  }

  @Override
  public EList findByAttr(String name, String... value)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    try {
      return findByXPath(XPATH.getElementsByAttrNameValue(name, value));
    }
    catch(XPathExpressionException e) {
      // XPath expression is build internally and cannot fail
      throw new BugError(e);
    }
  }

  @Override
  public EList findByAttrNS(String namespaceURI, String name, String... value)
  {
    if(namespaceURI == null) {
      return findByAttr(name, value);
    }
    Params.notNullOrEmpty(name, "Attribute name");

    String namespacePrefix = "ns";
    try {
      return findByXPathNS(new NamespaceContext()
      {
        @Override
        public String getNamespaceURI(String prefix)
        {
          return prefix.equals(namespacePrefix) ? namespaceURI : null;
        }
      }, XPATH.getElementsByAttrNameValueNS(namespacePrefix, name, value));
    }
    catch(XPathExpressionException e) {
      // XPath expression is build internally and cannot fail
      throw new BugError(e);
    }
  }

  @Override
  public void dump()
  {
    try {
      Serializer serializer = new Serializer(new OutputStreamWriter(System.out, "UTF-8"));
      serializer.serialize(this);
    }
    catch(Exception e) {
      // hard to believe standard out will fail to write
      throw new BugError(e);
    }
  }

  @Override
  public void serialize(Writer writer, Object... flags) throws IOException
  {
    boolean closeWriter = flags.length > 0 ? (boolean)flags[0] : false;
    boolean xmlDeclaration = flags.length > 1 ? (boolean)flags[1] : true;

    Serializer serializer = new Serializer(writer);
    serializer.setXmlDeclaration(xmlDeclaration);

    if(closeWriter) {
      try {
        serializer.serialize(this);
      }
      finally {
        writer.close();
      }
    }
    else {
      serializer.serialize(this);
    }
  }

  @Override
  public void removeNamespaceDeclaration(String namespaceURI)
  {
    Params.notNullOrEmpty(namespaceURI, "Namespace URI");
    removeNamespaceDeclarations(doc.getDocumentElement(), namespaceURI);
  }

  /**
   * Recursively search for namespace declaration on requested URI and remove it. Iterate all element attributes for one
   * with name beginning with <code>xmlns:</code> and value equal to requested namespace URI. If found remove the
   * attribute and break iteration loop since an element can have a single namespace declaration for a given URI.
   * <p>
   * After processing current element attributes continue recursively with child elements.
   * 
   * @param element current element.
   * @param namespaceURI namespace URI used by namespace declaration.
   */
  private static void removeNamespaceDeclarations(org.w3c.dom.Element element, String namespaceURI)
  {
    NamedNodeMap attributes = element.getAttributes();
    for(int i = 0; i < attributes.getLength(); ++i) {
      final Node attribute = attributes.item(i);
      final String name = attribute.getNodeName();
      if(name != null && namespaceURI.equals(attribute.getNodeValue()) && name.startsWith("xmlns:")) {
        // an element can have only one declaration for specific namespace URI
        element.removeAttribute(name);
        break;
      }
    }

    NodeList children = element.getChildNodes();
    for(int i = 0; i < children.getLength(); ++i) {
      final Node child = children.item(i);
      if(child instanceof org.w3c.dom.Element) {
        removeNamespaceDeclarations((org.w3c.dom.Element)child, namespaceURI);
      }
    }
  }
}
