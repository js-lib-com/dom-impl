package js.dom.w3c;

import org.xml.sax.SAXException;

import js.dom.Document;
import js.dom.EList;
import junit.framework.TestCase;

public class DomUtilUnitTest extends TestCase {
	public void testFindByNonEmptyText() throws SAXException {
		String fragment = "<body>" + //
				"<a>about</a>" + //
				"<a>terms</a>" + //
				"<p>" + //
				"  <span>first-name</span>, <span>surname</span>" + //
				"</p>" + //
				"</body>";
		Document doc = new DocumentBuilderImpl().parseXML(fragment);
		EList elist = DomUtil.findByNonEmptyText(doc);
		assertEquals(4, elist.size());
		assertEquals("about", elist.item(0).getText());
		assertEquals("terms", elist.item(1).getText());
		assertEquals("first-name", elist.item(2).getText());
		assertEquals("surname", elist.item(3).getText());
	}
}
