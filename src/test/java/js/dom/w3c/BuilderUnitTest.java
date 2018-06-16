package js.dom.w3c;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.util.Classes;
import js.util.Strings;
import junit.framework.TestCase;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class BuilderUnitTest extends TestCase {
	public void testDocumentValidation() throws SAXException, IOException {
		File file = getFile("document-utf.xml");

		Validator validator = getSchema().newValidator();
		Source source = new StreamSource(file);
		try {
			validator.validate(source);
		} catch (SAXParseException e) {
			TestCase.fail("Validation with good schema should past.");
		}

		validator = getBadSchema().newValidator();
		source = new StreamSource(file);
		try {
			validator.validate(source);
			TestCase.fail("Validation with bad schema should throw SAX parse exception.");
		} catch (SAXParseException e) {
		}
	}

	public void testCreateDocument() throws IOException {
		Document document = builder().createXML("root");
		TestCase.assertNotNull(document);
		org.w3c.dom.Document doc = Classes.getFieldValue(document, "doc");
		TestCase.assertNotNull(doc);
		TestCase.assertEquals("root", doc.getDocumentElement().getTagName());
	}

	public void testParseUtfXml() {
		String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //
				"<body>" + //
				"    <h1 id=\"id1\">ηεαδερ 1</h1>" + //
				"    <h2 id=\"id2\">ηεαδερ 2</h2>" + //
				"    <h3 id=\"id3\">ηεαδερ 3</h3>" + //
				"</body>";

		assertUtfDocument(builder().parseXML(string));
	}

	public void testLoadXmlUtfFile() throws SAXException, IOException {
		File file = getFile("document-utf.xml");
		assertUtfDocument(builder().loadXML(file));
	}

	public void testLoadXmlIsoFile() throws FileNotFoundException, SAXException {
		File file = getFile("document-iso.xml");
		assertIsoDocument(builder().loadXML(file));
	}

	public void testLoadXmlUtfStream() throws FileNotFoundException, SAXException {
		assertUtfDocument(builder().loadXML(getStream("document-utf.xml")));
	}

	public void testLoadXmlIsoStream() throws FileNotFoundException, SAXException {
		assertIsoDocument(builder().loadXML(getStream("document-iso.xml")));
	}

	public void testLoadXmlUtfSource() throws FileNotFoundException, SAXException {
		assertUtfDocument(builder().loadXML(getFile("document-utf.xml")));
	}

	public void testLoadXmlIsoSource() throws FileNotFoundException, SAXException {
		assertIsoDocument(builder().loadXML(getFile("document-iso.xml")));
	}

	public void _testLoadXmlUtfFromURL() throws FileNotFoundException, SAXException, MalformedURLException {
		URL url = new URL("http://test.bbnet.ro/utf.xml");
		assertUtfDocument(builder().loadXML(url));
	}

	public void _testParseXmlIsoURI() throws FileNotFoundException, SAXException, MalformedURLException {
		URL url = new URL("http://test.bbnet.ro/iso.xml");
		assertIsoDocument(builder().loadXML(url));
	}

	public void testParseHtmlUtfFile() throws FileNotFoundException {
		assertUtfDocument(builder().loadHTML(getFile("page-utf.html")));
	}

	public void testParseHtmlIsoFile() throws FileNotFoundException {
		assertIsoDocument(builder().loadHTML(getFile("page-iso.html")));
	}

	public void testParseHtmlUtfStream() throws FileNotFoundException {
		assertUtfDocument(builder().loadHTML(getStream("page-utf.html")));
	}

	public void testParserDetection() throws IOException {
		final int READ_AHEAD_SIZE = 20;

		InputStream inputStream = new BufferedInputStream(getStream("page-index.html"));
		assert inputStream.markSupported();
		inputStream.mark(READ_AHEAD_SIZE);

		byte[] bytes = new byte[READ_AHEAD_SIZE];
		for (int i = 0; i < READ_AHEAD_SIZE; ++i) {
			bytes[i] = (byte) inputStream.read();
		}
		String prolog = new String(bytes, "UTF-8");
		boolean isXML = prolog.startsWith("<?xml");

		inputStream.reset();
		Document doc = isXML ? builder().loadXML(inputStream) : builder().loadHTML(inputStream);
		assertNotNull(doc);
	}

	public void testParseHtmlIsoStream() throws FileNotFoundException {
		assertIsoDocument(builder().loadHTML(getStream("page-iso.html")));
	}

	public void testParseHtmlUtfSource() throws FileNotFoundException {
		assertUtfDocument(builder().loadHTML(getFile("page-utf.html")));
	}

	public void testParseHtmlIsoSource() throws FileNotFoundException {
		assertIsoDocument(builder().loadHTML(getFile("page-iso.html")));
	}

	public void testLoadHtml5File() throws FileNotFoundException {
		Document doc = builder().loadHTML(getFile("page-html5.html"));
		assertEquals("Current Value in €", doc.getByTag("h1").getText());
		assertEquals("current value in €.", doc.getByTag("p").getAttr("title"));
		assertEquals("copyright © j(s)-lib tools ® 2013", doc.getByTag("footer").getText());
	}

	public void testParseHtml5String() throws IOException {
		String html5 = Strings.load(getFile("page-html5.html"));
		Document doc = builder().parseHTML(html5);
		assertEquals("Current Value in €", doc.getByTag("h1").getText());
		assertEquals("current value in €.", doc.getByTag("p").getAttr("title"));
		assertEquals("copyright © j(s)-lib tools ® 2013", doc.getByTag("footer").getText());
	}

	public void _testParseHtmlUtfURL() throws MalformedURLException {
		assertUtfDocument(builder().loadHTML(new URL("http://test.bbnet.ro/utf.html")));
	}

	public void _testParseHtmlIsoURI() throws MalformedURLException {
		assertIsoDocument(builder().loadHTML(new URL("http://test.bbnet.ro/iso.html")));
	}

	public void testLocalLoadedDTD() throws FileNotFoundException {
		builder().loadXML(getStream("web.xml"));
	}

	private static DocumentBuilder builder() {
		return new DocumentBuilderImpl();
	}

	private void assertUtfDocument(Document document) {
		TestCase.assertNotNull(document);
		org.w3c.dom.Document doc = Classes.getFieldValue(document, "doc");
		TestCase.assertNotNull(doc);
		TestCase.assertEquals("ηεαδερ 1", doc.getElementsByTagName("h1").item(0).getTextContent());
		TestCase.assertEquals("ηεαδερ 2", doc.getElementsByTagName("h2").item(0).getTextContent());
		TestCase.assertEquals("ηεαδερ 3", doc.getElementsByTagName("h3").item(0).getTextContent());
	}

	private void assertIsoDocument(Document document) {
		TestCase.assertNotNull(document);
		org.w3c.dom.Document doc = Classes.getFieldValue(document, "doc");
		TestCase.assertNotNull(doc);
		TestCase.assertEquals("hedîr 1", doc.getElementsByTagName("h1").item(0).getTextContent());
		TestCase.assertEquals("hedîr 2", doc.getElementsByTagName("h2").item(0).getTextContent());
		TestCase.assertEquals("hedîr 3", doc.getElementsByTagName("h3").item(0).getTextContent());
	}

	private InputStream getStream(String resource) throws FileNotFoundException {
		return new FileInputStream(new File("fixture/" + resource));
	}

	private File getFile(String resource) {
		return new File("fixture/" + resource);
	}

	private Schema getSchema() throws SAXException {
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		return sf.newSchema(getFile("schema.xsd"));
	}

	private Schema getBadSchema() throws SAXException {
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		return sf.newSchema(getFile("schema-bad.xsd"));
	}
}
