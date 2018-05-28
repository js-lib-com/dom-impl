package js.dom.w3c;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.util.Classes;
import js.util.Strings;
import junit.framework.TestCase;

public class SerializerUnitTest extends TestCase {
	/**
	 * X(HT)ML reserved characters. There are 5 reserved characters that can't be present into text content because X(HT)ML
	 * processors could mistake them for markup. These are: quotation mark, apostrophe, ampersand, less-than and greater-than.
	 */
	private static final String RESERVED_CHARS = "<>&\"'";

	/**
	 * Suspect ;-) characters that should be passed as they are.
	 */
	private static final String SUSPECT_CHARS = "!@#$%^*(),.;:{[]}\\|";

	/**
	 * Unicode characters that should be passed as they are.
	 */
	private static final String UNICODE_CHARS = "αβψδεφγηιξκλμνοπ�?στθωχζςΑΒΨΔΕΦΓΗΙΞΚΛΜ�?ΟΠΡΣΤΘΩΧΖςăîâşţĂÎÂŞŢ";

	private StringWriter writer;
	private Object serializer;

	@Override
	protected void setUp() throws Exception {
		writer = new StringWriter();
		serializer = Classes.newInstance("js.dom.w3c.Serializer", writer);
	}

	public void testSerializeReservedChars() throws Exception {
		Document doc = getDocument(RESERVED_CHARS);
		Classes.invoke(serializer, "serialize", doc);
		// serialize escape all reserved characters
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<tag attr=\"&lt;&gt;&amp;&quot;&apos;\">&lt;&gt;&amp;&quot;&apos;</tag>");
	}

	public void testNiceSuspectChars() throws Exception {
		Document doc = getDocument(SUSPECT_CHARS);
		Classes.invoke(serializer, "serialize", doc);
		assertEquals(SUSPECT_CHARS, getAttr());
		assertEquals(SUSPECT_CHARS, getText());
	}

	public void testSerializeUnicodeChars() throws Exception {
		Document doc = getDocument(UNICODE_CHARS);
		Classes.invoke(serializer, "serialize", doc);
		TestCase.assertEquals(UNICODE_CHARS, getAttr());
		TestCase.assertEquals(UNICODE_CHARS, getText());
	}

	public void testSerializeHtml() throws Exception {
		Document doc = builder().loadHTML(file("page-simple.html"));
		Classes.invoke(this.serializer, "serialize", doc);
		assertTrue(writer.toString().startsWith("<!DOCTYPE html>"));
	}

	public void testSerializerXmlEntity() throws Exception {
		File file = file("strings.xml");
		Document doc = builder().loadXML(file);
		Classes.invoke(this.serializer, "serialize", doc);
		assertTrue(writer.toString().contains("copyright © j(s)-lib tools ® 2013"));
	}

	public void testSerializerXmlEntityToFile() throws Exception {
		File xml = file("strings.xml");
		Document doc = builder().loadXML(xml);

		File xmlser = file("strings.ser");
		FileWriter writer = new FileWriter(xmlser);
		Object serializer = Classes.newInstance("js.dom.w3c.Serializer", writer);
		Classes.invoke(serializer, "serialize", doc);
		writer.close();

		assertTrue(Strings.load(xmlser).contains("copyright © j(s)-lib tools ® 2013"));
	}

	public void testInlineScript() throws Exception {
		File file = file("inline-script.js");
		Document doc = builder().loadXML(file);
		Classes.invoke(serializer, "serialize", doc);
		assertTrue(writer.toString().contains("(window,document,'script','//www.google-analytics.com/analytics.js','ga');"));
	}

	private void assertEquals(String expected) {
		assertEquals(expected, this.writer.getBuffer().toString());
	}

	private static Document getDocument(String charset) {
		Document doc = builder().createXML("tag");
		doc.getRoot().setText(charset).setAttr("attr", charset);
		return doc;
	}

	private static DocumentBuilder builder() {
		return new DocumentBuilderImpl();
	}

	private String getAttr() {
		String s = this.writer.getBuffer().toString();
		int i = s.indexOf("attr=\"") + 6;
		int j = s.indexOf('"', i + 1);
		return s.substring(i, j);
	}

	private String getText() {
		String s = this.writer.getBuffer().toString();
		int i = s.indexOf("\">") + 2;
		int j = s.indexOf('<', i + 1);
		return s.substring(i, j);
	}

	private static File file(String path) {
		return new File("fixture/dom/" + path);
	}
}
