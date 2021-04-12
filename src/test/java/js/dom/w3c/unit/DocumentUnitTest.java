package js.dom.w3c.unit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.StringWriter;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.EList;
import js.dom.Element;
import js.dom.NamespaceContext;
import js.dom.w3c.DocumentBuilderImpl;
import js.io.WriterOutputStream;
import js.util.Classes;
import junit.framework.TestCase;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DocumentUnitTest extends TestCase {
	private static final String NS1 = "js-lib.com/ns1";
	private static final String NS2 = "js-lib.com/ns2";
	private static final String FAKE_NS = "js-lib.com/fake-ns";

	public void testCreateElement() {
		Document doc = builder().createXML("root");
		org.w3c.dom.Document document = Classes.getFieldValue(doc, "doc");
		org.w3c.dom.Element root = document.getDocumentElement();

		Element p = doc.createElement("p");
		root.appendChild(node(p));

		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName("p").item(0);
		assertNotNull(element);
		assertEquals("p", element.getTagName());
	}

	public void testCreateElementWithAttributes() {
		Document doc = builder().createXML("root");
		org.w3c.dom.Document document = Classes.getFieldValue(doc, "doc");
		org.w3c.dom.Element root = document.getDocumentElement();

		Element p = doc.createElement("p", "id", "123", "title", "paragraph description");
		root.appendChild(node(p));

		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName("p").item(0);
		assertNotNull(element);
		assertEquals("p", element.getTagName());
		assertEquals("123", element.getAttribute("id"));
		assertEquals("paragraph description", element.getAttribute("title"));
	}

	public void testCreateElementWithInvalidAttributes() {
		Document doc = builder().createXML("root");
		try {
			doc.createElement("p", "id", "123", "title");
		} catch (IllegalArgumentException expected) {
			return;
		}
		fail("Creating element with invalid argument number should rise assertion.");
	}

	/**
	 * For documents without schema {@link Document#getById(String)} always returns null. In order to use IDs one should enable
	 * default schema or to provide a schema that properly declare ID attribute.
	 */
	public void _testGetById() throws FileNotFoundException, SAXException {
		File file = getFile("document-utf.xml");
		Document doc = builder().loadXML(file);
		assertNull(doc.getById("id1"));

		doc = builder().loadXML(file);
		assertNull(doc.getById("id1"));

		doc = builder().loadXMLNS(file);
		assertNotNull(doc.getById("id1"));
		assertEquals("ηεαδερ 1", doc.getById("id1").getText());
	}

	public void testGetByTag() throws FileNotFoundException {
		Document doc = builder().loadXML(getFile("document-ns.xml"));

		Element el = doc.getByTag("el");
		assertNotNull(el);
		assertEquals("element", el.getText());
	}

	public void testGetByTagNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(getFile("document-ns.xml"));

		Element el = doc.getByTag("el");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = doc.getByTagNS(NS1, "el");
		assertNotNull(el);
		assertEquals("ns1.element", el.getText());

		el = doc.getByTagNS(NS2, "el");
		assertNotNull(el);
		assertEquals("ns2.element", el.getText());

		el = doc.getByTagNS(FAKE_NS, "el");
		assertNull(el);
	}

	public void testGetByTagOnDocumentWithoutNamespace() throws FileNotFoundException {
		Document doc = builder().loadXML(getFile("document-ns.xml"));

		Element el = doc.getByTag("ns1:el");
		assertNotNull(el);
		assertEquals("ns1.element", el.getText());

		EList elist = doc.findByTag("ns1:el");
		assertNotNull(elist);
		assertFalse(elist.isEmpty());
		assertEquals("ns1.element", elist.item(0).getText());
	}

	/**
	 * If document is parsed without name space support element 'NS' getters always returns null and 'NS' finders always return
	 * empty list.
	 */
	public void testGetByTagNSOnDocumentWithoutNamespace() throws FileNotFoundException {
		Document doc = builder().loadXML(getFile("document-ns.xml"));

		Element el = doc.getByTag("el");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = doc.getByTag("ns1:el");
		assertNotNull(el);
		assertEquals("ns1.element", el.getText());

		el = doc.getByTagNS(NS1, "el");
		assertNull(el);

		assertFalse(doc.findByTag("el").isEmpty());
		assertTrue(doc.findByTagNS(NS1, "el").isEmpty());
	}

	public void testFindByTag() throws FileNotFoundException {
		Document doc = builder().loadXML(getFile("document-ns.xml"));

		// document is not parsed with name space and prefix is part of tag name; colon is valid for tag name
		// so that root has three child elements: 'el', 'ns1:el' and 'ns2:el'
		// when search for tag 'el' we found only one

		EList elist = doc.findByTag("el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("element", elist.item(0).getText());

		elist = doc.findByTagNS(null, "el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("element", elist.item(0).getText());

		// 'ns1:el' is the tag name for second child element
		// it is not name space prefix since document is not parsed with name space support
		elist = doc.findByTag("ns1:el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("ns1.element", elist.item(0).getText());

		elist = doc.findByTagNS(NS1, "el");
		assertNotNull(elist);
		assertTrue(elist.isEmpty());
	}

	public void testFindByTagNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(getFile("document-ns.xml"));

		EList elist = doc.findByTag("el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("element", elist.item(0).getText());

		elist = doc.findByTagNS(NS1, "el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("ns1.element", elist.item(0).getText());

		elist = doc.findByTagNS("*", "el");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		// null name space is global scope
		elist = doc.findByTagNS(null, "el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("element", elist.item(0).getText());

		boolean exception = false;
		try {
			doc.findByTagNS(NS1, null);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}

	public void testGetByCssClass() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));
		assertEquals("ηεαδερ 1", doc.getByCssClass("header").getText());
		assertEquals("ηεαδερ 1", doc.getByCssClass("title").getText());
		assertEquals("ηεαδερ 2", doc.getByCssClass("chapter").getText());
		assertEquals("ηεαδερ 3", doc.getByCssClass("inner").getText());
	}

	public void testFindByCssClass() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));
		EList elist = doc.findByCssClass("header");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("ηεαδερ 1", elist.item(0).getText());
		assertEquals("ηεαδερ 2", elist.item(1).getText());
		assertEquals("ηεαδερ 3", elist.item(2).getText());
	}

	public void testGetByEmptyCssClass() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));
		try {
			doc.getByCssClass("");
		} catch (IllegalArgumentException e) {
			return;
		}
		fail("Attempting to retrieve element by empty CSS class should rise assertion.");
	}

	public void testGetByNullCssClass() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));
		try {
			doc.getByCssClass(null);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail("Attempting to retrieve element by null CSS class should rise assertion.");
	}

	public void testGetByCss() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));
		try {
			doc.getByCss(".header");
		} catch (UnsupportedOperationException expected) {
			return;
		}
		fail("Get by CSS should throw unsupported operation.");
	}

	public void testGetByXPath() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));

		Element el = doc.getByXPath("/HTML/BODY/H1");
		assertNotNull(el);
		assertEquals("ηεαδερ 1", el.getText());

		el = doc.getByXPath("/HTML/BODY/H11");
		assertNull(el);
	}

    public void testGetByXPathNS() throws FileNotFoundException {
        Document doc = builder().loadXMLNS(getFile("document-ns.xml"));

        Element el = doc.getByXPath("//el");
        assertNotNull(el);
        assertEquals("element", el.getText());

        el = doc.getByXPathNS(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix.equals("ns1")) {
                    return NS1;
                }
                return null;
            }
        }, "//ns1:el");
        assertNotNull(el);
        assertEquals("ns1.element", el.getText());
    }

    public void testGetByXPathNS_NamespaceURI() throws FileNotFoundException {
        Document doc = builder().loadXMLNS(getFile("document-ns.xml"));

        Element el = doc.getByXPath("//el");
        assertNotNull(el);
        assertEquals("element", el.getText());

        el = doc.getByXPathNS(NS1, "//ns1:el");
        assertNotNull(el);
        assertEquals("ns1.element", el.getText());
    }

    public void testGetByXPathNS_NamespaceURI_Attr() throws FileNotFoundException {
        Document doc = builder().loadXMLNS(getFile("document-ns.xml"));

        Element el = doc.getByXPath("//*[@attr]");
        assertNotNull(el);
        assertEquals("element", el.getText());

        el = doc.getByXPathNS(NS1, "//*[@ns1:attr]");
        assertNotNull(el);
        assertEquals("ns1.element", el.getText());

        el = doc.getByXPathNS(NS1, "//*[@fake:attr]");
        assertNotNull(el);
        assertEquals("ns1.element", el.getText());
    }

	public void testFindByXPath() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));

		EList elist = doc.findByXPath("//*[@class]");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("ηεαδερ 1", elist.item(0).getText());
		assertEquals("ηεαδερ 2", elist.item(1).getText());
		assertEquals("ηεαδερ 3", elist.item(2).getText());

		elist = doc.findByXPath("//*[@fake-attr]");
		assertNotNull(elist);
		assertTrue(elist.isEmpty());
	}

	public void testFindByXPathNS() throws FileNotFoundException {
		Document doc = builder().loadXMLNS(getFile("document-ns.xml"));

		EList elist = doc.findByXPath("//el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("element", elist.item(0).getText());

		elist = doc.findByXPathNS(new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				if (prefix.equals("ns1")) {
					return NS1;
				}
				return null;
			}
		}, "//ns1:el");
		assertNotNull(elist);
		assertEquals(1, elist.size());
		assertEquals("ns1.element", elist.item(0).getText());
	}

	public void testGetByXPathCaseSensitive() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-simple.html"));

		// HTML tags are upper case and XPath expression is case sensitive
		Element el = doc.getByXPath("/html/body/h1");
		assertNull(el);
		el = doc.getByXPath("/HTML/BODY/H1");
		assertNotNull(el);
	}

	public void testGetByAttr() throws FileNotFoundException {
		Document doc = builder().loadXML(getFile("document-ns.xml"));

		Element el = doc.getByAttr("attr");
		assertNotNull(el);
		assertEquals("element", el.getText());

		el = doc.getByAttr("attr", "value");
		assertNotNull(el);
		assertEquals("element", el.getText());

		assertNull(doc.getByAttr("fake-attr"));
		assertNull(doc.getByAttr("attr", "fake-value"));
		assertNull(doc.getByAttr("fake-attr", "fake-value"));
	}

	public void testFindByAttr() throws FileNotFoundException {
		Document doc = builder().loadXML(getFile("document-ns.xml"));

		EList elist = doc.findByAttr("attr");
		assertNotNull(elist);
		assertEquals(3, elist.size());
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		elist = doc.findByAttr("attr", "value");
		assertNotNull(elist);
		assertEquals("element", elist.item(0).getText());
		assertEquals("ns1.element", elist.item(1).getText());
		assertEquals("ns2.element", elist.item(2).getText());

		assertTrue(doc.findByAttr("fake-attr").isEmpty());
		assertTrue(doc.findByAttr("attr", "fake-value").isEmpty());
		assertTrue(doc.findByAttr("fake-attr", "fake-value").isEmpty());
	}

	public void testSerialize() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("document-utf.xml"));
		PrintStream systemOut = System.out;

		StringWriter string = new StringWriter();
		System.setOut(new PrintStream(new WriterOutputStream(string)));
		doc.dump();
		System.setOut(systemOut);

		Document recreatedDoc = builder().parseHTML(string.toString());
		assertEquals(doc.getByTag("h1").getText(), recreatedDoc.getByTag("h1").getText());
		assertEquals(doc.getByTag("h2").getText(), recreatedDoc.getByTag("h2").getText());
		assertEquals(doc.getByTag("h3").getText(), recreatedDoc.getByTag("h3").getText());
	}

	private static DocumentBuilder builder() {
		return new DocumentBuilderImpl();
	}

	private Node node(Element el) {
		return Classes.getFieldValue(el, "node");
	}

	private File getFile(String resource) {
		return new File("fixture/" + resource);
	}
}
