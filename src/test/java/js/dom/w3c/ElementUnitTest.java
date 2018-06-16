package js.dom.w3c;

import java.io.File;
import java.io.FileNotFoundException;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.EList;
import js.dom.Element;
import js.dom.NamespaceContext;
import js.util.Classes;
import junit.framework.TestCase;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementUnitTest extends TestCase {
	private static final String NS1 = "js-lib.com/ns1";
	private static final String NS2 = "js-lib.com/ns2";
	private static final String FAKE_NS = "js-lib.com/fake-ns";

	private Document doc;
	private org.w3c.dom.Document document;
	private org.w3c.dom.Element element;

	@Override
	protected void setUp() throws Exception {
		doc = builder().loadHTML(new File("fixture/page-simple.html"));
		document = Classes.getFieldValue(doc, "doc");
	}

	public void testAddChild() {
		Element child = doc.createElement("p", "id", "123", "title", "paragraph description");
		Element body = doc.getByTag("body");
		body.addChild(child);

		element = document.getElementById("123");
		assertNotNull(element);
		assertEquals("P", element.getTagName());
		assertEquals("paragraph description", element.getAttribute("title"));
	}

	/**
	 * Add as child an element already part of the document tree. Newly added child will become last child and it will be
	 * removed from old position.
	 */
	public void testAddExistingChild() {
		Element body = doc.getByTag("body");

		Element child = doc.getByTag("h1");
		body.addChild(child);

		// before addChild : h1 h2 h3
		// after addChild(h1) : h2 h3 h1

		EList elist = body.getChildren();
		assertEquals("h2", elist.item(0).getTag());
		assertEquals("h3", elist.item(1).getTag());
		assertEquals("h1", elist.item(2).getTag());
	}

	public void testAddChildTree() {
		Element ul = doc.createElement("ul");
		ul.addChild(doc.createElement("li", "id", "id1").setText("apple"));
		ul.addChild(doc.createElement("li", "id", "id2").setText("grapes"));
		ul.addChild(doc.createElement("li", "id", "id3").setText("melon"));

		Element parent = doc.getByTag("body");
		parent.addChild(ul);
		assertEquals("apple", document.getElementById("id1").getTextContent());
		assertEquals("grapes", document.getElementById("id2").getTextContent());
		assertEquals("melon", document.getElementById("id3").getTextContent());
	}

	public void testAddForeignChild() throws FileNotFoundException {
		Document foreignDoc = builder().createXML("root");
		Element foreignChild = foreignDoc.createElement("div", "id", "321", "title", "division description");

		Element parent = doc.getByTag("body");
		parent.addChild(foreignChild);

		element = document.getElementById("321");
		assertNotNull(element);
		assertEquals("DIV", element.getTagName());
		assertEquals("division description", element.getAttribute("title"));

		foreignDoc = builder().loadXMLNS(new File("fixture/document-utf.xml"));
		Element foreignH1 = foreignDoc.getByTag("h1");
		Element foreignH2 = foreignDoc.getByTag("h2");
		Element foreignH3 = foreignDoc.getByTag("h3");

		parent.addChild(foreignH1, foreignH2, foreignH3);
		assertEquals("ηεαδερ 1", document.getElementById("id1").getTextContent());
		assertEquals("ηεαδερ 2", document.getElementById("id2").getTextContent());
		assertEquals("ηεαδερ 3", document.getElementById("id3").getTextContent());
	}

	public void testSetText() {
		Document doc = builder().createXML("root");
		doc.getRoot().setText("\"'&<>");
		org.w3c.dom.Document document = Classes.getFieldValue(doc, "doc");
		assertEquals("\"'&<>", document.getDocumentElement().getTextContent());
	}

	public void testRemoveText() {
		String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
				"<root><element0 />text0<element1 />text1<element2 />text2</root>";
		Document doc = builder().parseXML(xml);
		Element root = doc.getRoot();
		Node rootNode = node(root);
		for (int i = 0; i < 3; ++i) {
			Node element = node(root.getByTag("element" + i));
			Node text = document(doc).createTextNode("text");
			rootNode.insertBefore(text, element);
		}

		root.removeText();
		NodeList children = rootNode.getChildNodes();
		assertEquals(3, children.getLength());
		assertEquals("element0", children.item(0).getNodeName());
		assertEquals("element1", children.item(1).getNodeName());
		assertEquals("element2", children.item(2).getNodeName());
	}

	public void testEscapeAttribute() {
		Document doc = builder().createXML("root");
		doc.getRoot().setAttr("name", "\"'&<>");
		org.w3c.dom.Document document = Classes.getFieldValue(doc, "doc");
		assertEquals("\"'&<>", document.getDocumentElement().getAttribute("name"));
	}

	public void testSetAttribute() {
		Document doc = builder().createXML("root");
		Element el = doc.getRoot();
		org.w3c.dom.Document document = Classes.getFieldValue(doc, "doc");
		org.w3c.dom.Element node = document.getDocumentElement();

		el.setAttr("attr1", "value1").setAttr("attr2", "value2").setAttr("attr3", "value3");
		assertEquals("value1", node.getAttribute("attr1"));
		assertEquals("value2", node.getAttribute("attr2"));
		assertEquals("value3", node.getAttribute("attr3"));
	}

	public void _testSetAttributeNS() {
		Document doc = builder().createXMLNS("root");
		Element el = doc.getRoot();
		org.w3c.dom.Document document = Classes.getFieldValue(doc, "doc");
		org.w3c.dom.Element node = document.getDocumentElement();

		el.setAttrNS(NS1, "attr", "ns1.value").setAttrNS(NS2, "attr", "ns2.value").setAttr("attr", "value");

		assertEquals("ns1.value", node.getAttributeNS(NS1, "attr"));
		assertEquals("ns2.value", node.getAttributeNS(NS2, "attr"));
		assertEquals("value", node.getAttribute("attr"));
	}

	public void testSetMultipleAttribute() {
		Document doc = builder().createXML("root");
		Element el = doc.getRoot();
		org.w3c.dom.Document document = Classes.getFieldValue(doc, "doc");
		org.w3c.dom.Element node = document.getDocumentElement();

		el.setAttrs("attr1", "value1", "attr2", "value2", "attr3", "value3");
		assertEquals("value1", node.getAttribute("attr1"));
		assertEquals("value2", node.getAttribute("attr2"));
		assertEquals("value3", node.getAttribute("attr3"));
	}

	public void testSetInvalidMultipleAttribute() {
		Document doc = builder().createXML("root");
		Element el = doc.getRoot();

		try {
			el.setAttrs("attr1", "value1", "attr2", "value2", "attr3");
		} catch (IllegalArgumentException expected) {
			return;
		}
		fail("Missing attribute value should rise illegal argument exception.");
	}

	public void testGetByTag() throws FileNotFoundException {
		Document doc = builder().loadXML(new File("fixture/document-ns.xml"));

		Element el = doc.getRoot().getByTag("el");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = doc.getRoot().getByTag("*");
		assertNotNull(el);
		assertEquals("element", el.getText());

		// document is not parsed with name space and prefix is part of tag name
		el = doc.getRoot().getByTag("ns1:el");
		assertNotNull(el);
		assertEquals("ns1.element", el.getText());

		el = doc.getRoot().getByTag("fake-ns:el");
		assertNull(el);
	}

	public void testGetByTagNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(new File("fixture/document-ns.xml"));
		Element root = doc.getRoot();

		Element el = root.getByTag("el");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = root.getByTagNS(null, "el");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = root.getByTagNS("*", "el");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = root.getByTagNS(NS1, "el");
		assertNotNull(el);
		assertEquals("ns1.element", el.getText());

		el = root.getByTagNS(NS2, "el");
		assertNotNull(el);
		assertEquals("ns2.element", el.getText());

		assertNull(root.getByTagNS(FAKE_NS, "el"));
	}

	/**
	 * XML source file has name spaces defined but parser is configured without name space support. Element without prefix is
	 * found but elements with name spaces are not.
	 */
	public void testGetByTagNoNS() throws FileNotFoundException {
		Document doc = builder().loadXML(new File("fixture/document-ns.xml"));
		Element root = doc.getRoot();

		Element el = root.getByTag("el");
		assertNotNull(el);
		assertEquals("element", el.getText());

		assertNull(root.getByTagNS(NS1, "el"));
		assertNull(root.getByTagNS(NS2, "el"));
		assertNull(root.getByTagNS(FAKE_NS, "el"));
	}

	public void testFindByTag() throws FileNotFoundException {
		Document doc = builder().loadXML(new File("fixture/document-ns.xml"));
		Element root = doc.getRoot();

		EList elist = root.findByTag("el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("element", elist.item(0).getText());

		elist = root.findByTag("*");
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		elist = root.findByTagNS(NS1, "el");
		assertNotNull(elist);
		assertEquals(0, elist.size());

		elist = root.findByTagNS(FAKE_NS, "el");
		assertNotNull(elist);
		assertEquals(0, elist.size());
	}

	public void testFindByTagNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(new File("fixture/document-ns.xml"));
		Element root = doc.getRoot();

		EList elist = root.findByTag("el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("element", elist.item(0).getText());

		elist = root.findByTagNS(NS1, "el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("ns1.element", elist.item(0).getText());

		elist = root.findByTagNS(NS2, "el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("ns2.element", elist.item(0).getText());

		elist = root.findByTagNS(FAKE_NS, "el");
		assertNotNull(elist);
		assertEquals(0, elist.size());

		elist = root.findByTagNS("*", "el");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());
	}

	public void testGetByCssClass() {
		Element body = doc.getByTag("body");
		assertEquals("ηεαδερ 1", body.getByCssClass("header").getText());
		assertEquals("ηεαδερ 1", body.getByCssClass("title").getText());
		assertEquals("ηεαδερ 2", body.getByCssClass("chapter").getText());
		assertEquals("ηεαδερ 3", body.getByCssClass("inner").getText());
	}

	public void testFindByCssClass() {
		Element body = doc.getByTag("body");
		EList elist = body.findByCssClass("header");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("ηεαδερ 1", elist.item(0).getText());
		assertEquals("ηεαδερ 2", elist.item(1).getText());
		assertEquals("ηεαδερ 3", elist.item(2).getText());
	}

	public void testGetByAttr() throws FileNotFoundException {
		Document doc = builder().loadXML(new File("fixture/document-ns.xml"));
		Element root = doc.getRoot();

		Element el = root.getByAttr("attr");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = root.getByAttr("attr", "value");
		assertNotNull(el);
		assertEquals("element", el.getText());

		assertNull(root.getByAttr("fake-attr"));
		assertNull(root.getByAttr("attr", "fake-value"));
		assertNull(root.getByAttr("fake-attr", "fake-value"));
	}

	public void testFindByAttr() throws FileNotFoundException {
		Document doc = builder().loadXML(new File("fixture/document-ns.xml"));
		Element root = doc.getRoot();

		EList elist = root.findByAttr("attr");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		elist = root.findByAttr("attr", "value");
		assertNotNull(elist);
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		assertTrue(root.findByAttr("fake-attr").isEmpty());
		assertTrue(root.findByAttr("attr", "fake-value").isEmpty());
		assertTrue(root.findByAttr("fake-attr", "fake-value").isEmpty());
	}

	public void _testFindByAttrNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(new File("fixture/dom/document-ns.xml"));
		doc.dump();
		Element root = doc.getRoot();

		EList elist = root.findByAttr("attr");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		elist = root.findByAttr("attr", "value");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		elist = root.findByXPathNS(new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				return "js-lib.com/ns1";
			}
		}, "/*[@ns1:attr='ns1.value']");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		assertTrue(root.findByAttr("fake-attr").isEmpty());
		assertTrue(root.findByAttr("attr", "fake-value").isEmpty());
		assertTrue(root.findByAttr("fake-attr", "fake-value").isEmpty());
	}

	public void testGetByXPath() {
		Element h2 = doc.getByTag("h2");
		h2.addChild(doc.createElement("h4"));

		Element h4 = h2.getByXPath("H4");
		assertNotNull(h4);
		assertEquals("h4", h4.getTag());

		assertNull(h2.getByXPath("H1"));
		assertNotNull(h2.getByXPath("//H1"));
		assertNotNull(h2.getByXPath("/HTML/BODY/H1"));
	}

	public void testGetByXPathNS() {
		String html = "<!DOCTYPE html>" + //
				"<html xmlns:ns='js-lib.com/ns'>" + //
				"   <head></head>" + //
				"   <body>" + //
				"       <ns:div></ns:div>" + //
				"       <section>" + //
				"           <ns:p></ns:p>" + //
				"       </section>" + //
				"   </body>" + //
				"</html>";
		Document doc = builder().parseHTMLNS(html);

		Element section = doc.getByTag("section");
		assertNotNull(section);

		Element el = section.getByXPathNS(new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				return "js-lib.com/ns";
			}
		}, "ns:P");
		assertNotNull(el);
		assertEquals("ns:p", el.getTag());

		el = section.getByXPathNS(new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				return "js-lib.com/ns";
			}
		}, "//ns:DIV");
		assertNotNull(el);
		assertEquals("ns:div", el.getTag());
	}

	public void testFindByXPath() {
		Element body = doc.getByTag("body");
		EList elist = body.findByXPath("child::*");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("h1", elist.item(0).getTag());
		assertEquals("h2", elist.item(1).getTag());
		assertEquals("h3", elist.item(2).getTag());

		assertFalse(body.findByXPath("//H1").isEmpty());
		assertFalse(body.findByXPath("//HEAD").isEmpty());
		assertFalse(body.findByXPath("/HTML/HEAD").isEmpty());
		assertTrue(body.findByXPath("H4").isEmpty());
	}

	public void testFindByXPathNS() {
		String html = "<!DOCTYPE html>" + //
				"<html xmlns:ns='js-lib.com/ns'>" + //
				"   <head></head>" + //
				"   <body>" + //
				"       <ns:p>paragraph 1</ns:p>" + //
				"       <section>" + //
				"           <ns:p>paragraph 2</ns:p>" + //
				"       </section>" + //
				"		<p></p>" + //
				"   </body>" + //
				"</html>";
		Document doc = builder().parseHTMLNS(html);

		Element section = doc.getByTag("section");
		assertNotNull(section);

		EList elist = section.findByXPathNS(new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				return "js-lib.com/ns";
			}
		}, "ns:P");
		assertFalse(elist.isEmpty());
		assertEquals(1, elist.size());
		assertEquals("paragraph 2", elist.item(0).getText());

		elist = section.findByXPathNS(new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				return "js-lib.com/ns";
			}
		}, "//ns:P");
		assertFalse(elist.isEmpty());
		assertEquals(2, elist.size());
		assertEquals("paragraph 1", elist.item(0).getText());
		assertEquals("paragraph 2", elist.item(1).getText());
	}

	public void testGetAttr() throws FileNotFoundException {
		Document doc = builder().loadXML(new File("fixture/document-ns.xml"));
		Element el = doc.getRoot().getByTag("el");
		assertEquals("value", el.getAttr("attr"));
		assertNull(el.getAttr("fake-attr"));
	}

	public void testGetAttrNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(new File("fixture/document-ns.xml"));
		Element el = doc.getRoot().getByTagNS(NS1, "el");
		assertNotNull("Missing element with namespace.", el);

		assertEquals("value", el.getAttr("attr"));
		assertEquals("ns1.value", el.getAttrNS(NS1, "attr"));
		assertEquals("ns2.value", el.getAttrNS(NS2, "attr"));
		assertEquals("value", el.getAttrNS(null, "attr"));

		assertNull(el.getAttrNS(FAKE_NS, "attr"));
		assertNull(el.getAttrNS("*", "attr"));

		assertNull(el.getAttr("fake-attr"));
		assertNull(el.getAttrNS(NS1, "fake-attr"));
		assertNull(el.getAttrNS(FAKE_NS, "fake-attr"));
	}

	public void testHasCssClass() {
		Element h1 = doc.getByTag("h1");
		assertTrue(h1.hasCssClass("header"));
		assertTrue(h1.hasCssClass("title"));
		assertFalse(h1.hasCssClass("fake"));

		Element h2 = doc.getByTag("h2");
		assertTrue(h2.hasCssClass("header"));
		assertTrue(h2.hasCssClass("chapter"));
		assertFalse(h2.hasCssClass("fake"));

		Element h3 = doc.getByTag("h3");
		assertTrue(h3.hasCssClass("header"));
		assertFalse(h3.hasCssClass("heade"));
		assertTrue(h3.hasCssClass("inner"));
		assertFalse(h3.hasCssClass("inne"));
		assertTrue(h3.hasCssClass("chapter"));
		assertFalse(h3.hasCssClass("chapte"));
		assertFalse(h3.hasCssClass("fake"));
	}

	public void testRemove() {
		assertNotNull(doc.getByTag("h2"));
		doc.getByTag("h2").remove();
		assertNull(doc.getByTag("h2"));
	}

	public void testRemoveChildren() {
		Element body = doc.getByTag("body");
		assertTrue(body.hasChildren());
		body.removeChildren();
		assertFalse(body.hasChildren());
	}

	public void testRemoveAttr() throws FileNotFoundException {
		Document doc = builder().loadXML(new File("fixture/document-ns.xml"));
		Element el = doc.getByTag("el");
		assertTrue(el.hasAttr("attr"));
		el.removeAttr("attr");
		assertFalse(el.hasAttr("attr"));
	}

	public void testRemoveAttrNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(new File("fixture/document-ns.xml"));
		Element el = doc.getByTagNS(NS1, "el");

		assertTrue(el.hasAttr("attr"));
		el.removeAttr("attr");
		assertFalse(el.hasAttr("attr"));

		assertTrue(el.hasAttrNS(NS1, "attr"));
		el.removeAttrNS(NS1, "attr");
		assertFalse(el.hasAttrNS(NS1, "attr"));

		assertTrue(el.hasAttrNS(NS2, "attr"));
		el.removeAttrNS(NS2, "attr");
		assertFalse(el.hasAttrNS(NS2, "attr"));
	}

	public void testRemoveCssClass() throws FileNotFoundException {
		Element h3 = doc.getByTag("h3");
		assertTrue(h3.hasCssClass("inner"));
		h3.removeCssClass("inner");
		assertEquals("header chapter", h3.getAttr("class"));

		doc = builder().loadHTML(new File("fixture/page-simple.html"));
		h3 = doc.getByTag("h3");
		assertTrue(h3.hasCssClass("header"));
		h3.removeCssClass("header");
		assertEquals("inner chapter", h3.getAttr("class"));

		doc = builder().loadHTML(new File("fixture/page-simple.html"));
		h3 = doc.getByTag("h3");
		assertTrue(h3.hasCssClass("chapter"));
		h3.removeCssClass("chapter");
		assertEquals("header inner", h3.getAttr("class"));
	}

	public void testTrace() {
		String html = "<!DOCTYPE html>" + //
				"<html>" + //
				"   <head></head>" + //
				"   <body>" + //
				"       <div></div>" + //
				"       <div>" + //
				"           <p></p>" + //
				"       </div>" + //
				"   </body>" + //
				"</html>";
		Document doc = builder().parseHTML(html);

		EList elist = doc.findByTag("div");
		assertEquals("/html/body/div[0]", elist.item(0).trace());
		assertEquals("/html/body/div[1]", elist.item(1).trace());
		assertEquals("/html/body/div[1]/p", doc.getByTag("p").trace());
	}

	/**
	 * Replace an element that is part of the document tree with a newly create element. After replacement newly element becomes
	 * part of the document tree but replaced element is not longer.
	 */
	public void testReplace() {
		String html = "<!DOCTYPE html>" + //
				"<html>" + //
				"   <head></head>" + //
				"   <body><h1></h1></body>" + //
				"</html>";
		Document doc = builder().parseHTML(html);

		Element replacement = doc.createElement("h2");
		Element existing = doc.getByTag("h1");
		existing.replace(replacement);

		assertNull(doc.getByTag("h1"));
		assertNotNull(doc.getByTag("h2"));
	}

	public void testReplaceExistingChild() {
		Element h1 = doc.getByTag("h1");
		Element h3 = doc.getByTag("h3");

		// before replace: h1 h2 h3
		h1.replace(h3);
		// after replace: h3 h2 and h1 not longer part of the document tree

		EList elist = doc.getByTag("body").getChildren();
		assertEquals(2, elist.size());
		assertEquals("h3", elist.item(0).getTag());
		assertEquals("h2", elist.item(1).getTag());
	}

	public void testReplaceChild() {
		String string = "<!DOCTYPE html>" + //
				"<html>" + //
				"   <head></head>" + //
				"   <body></body>" + //
				"</html>";
		Document doc = builder().parseHTML(string);

		Element replacement = doc.createElement("body", "data-page", "js.test.Page");
		Element html = doc.getByTag("html");
		Element body = doc.getByTag("body");
		assertNull(body.getAttr("data-page"));
		html.replaceChild(replacement, body);
		assertEquals("js.test.Page", doc.getByTag("body").getAttr("data-page"));
	}

	public void testInsertChild() {
		Element body = doc.getByTag("body");

		Element child = doc.createElement("h4");
		child.setText("ηεαδερ 4");

		body.insertChild(child);
		assertEquals("ηεαδερ 4", doc.getByTag("h4").getText());
	}

	public void testInsertExistingChild() {
		Element body = doc.getByTag("body");
		Element child = doc.getByTag("h3");

		// before insert: h1 h2 h3
		body.insertChild(child);
		// after insert: h3 h1 h2

		EList elist = body.getChildren();
		assertEquals(3, elist.size());
		assertEquals("h3", elist.item(0).getTag());
		assertEquals("h1", elist.item(1).getTag());
		assertEquals("h2", elist.item(2).getTag());
	}

	public void testInsertChildren() {
		Element body = doc.getByTag("body");

		// chapter elements: h2 h3
		EList children = doc.findByCssClass("chapter");

		// before insert: h1 h2 h3
		body.insertChildren(children);
		// after insert: h2 h3 h1

		EList elist = body.getChildren();
		assertEquals(3, elist.size());
		assertEquals("h2", elist.item(0).getTag());
		assertEquals("h3", elist.item(1).getTag());
		assertEquals("h1", elist.item(2).getTag());
	}

	public void testInsertBefore() {
		Element h2 = doc.getByTag("h2");
		Element h4 = doc.createElement("h4");

		// before insert: h1 h2 h3
		h2.insertBefore(h4);
		// after insert: h1 h4 h2 h3

		EList elist = doc.getByTag("body").getChildren();
		assertEquals(4, elist.size());
		assertEquals("h1", elist.item(0).getTag());
		assertEquals("h4", elist.item(1).getTag());
		assertEquals("h2", elist.item(2).getTag());
		assertEquals("h3", elist.item(3).getTag());
	}

	public void testInsertExistingBefore() {
		Element h2 = doc.getByTag("h2");
		Element h3 = doc.getByTag("h3");

		// before insert: h1 h2 h3
		h2.insertBefore(h3);
		// after insert: h1 h3 h2

		EList elist = doc.getByTag("body").getChildren();
		assertEquals(3, elist.size());
		assertEquals("h1", elist.item(0).getTag());
		assertEquals("h3", elist.item(1).getTag());
		assertEquals("h2", elist.item(2).getTag());
	}

	public void testClone() {
		Element body = doc.getByTag("body");

		Element h2 = doc.getByTag("h2");
		Element clone = h2.clone(false);

		assertNotNull(clone);
		// returned clone is not part of document tree but belongs to the same document
		assertEquals(doc, clone.getDocument());
		EList elist = body.getChildren();
		assertEquals(3, elist.size());

		body.addChild(clone);
		elist = body.getChildren();
		assertEquals(4, elist.size());
		assertEquals("h1", elist.item(0).getTag());
		assertEquals("h2", elist.item(1).getTag());
		assertEquals("h3", elist.item(2).getTag());
		assertEquals("h2", elist.item(3).getTag());
	}

	public void testSetRichText() {
		Element body = doc.getByTag("body");
		body.setRichText("<p>Some <b>bold</b> and <i>italic</i> text.</p>");
		assertEquals(1, document.getElementsByTagName("p").getLength());
		assertEquals(1, document.getElementsByTagName("b").getLength());
		assertEquals(1, document.getElementsByTagName("i").getLength());
	}

	public void testGetRichText() {
		Element body = doc.getByTag("body");
		String richText = body.getRichText();
		assertTrue(richText.contains("<H1>ηεαδερ 1</H1>"));
		assertTrue(richText.contains("<H2>ηεαδερ 2</H2>"));
		assertTrue(richText.contains("<H3>ηεαδερ 3</H3>"));
	}

	private static DocumentBuilder builder() {
		return new DocumentBuilderImpl();
	}

	private static org.w3c.dom.Document document(Document doc) {
		return Classes.getFieldValue(doc, "doc");
	}

	private static Node node(Element el) {
		return Classes.getFieldValue(el, "node");
	}
}
