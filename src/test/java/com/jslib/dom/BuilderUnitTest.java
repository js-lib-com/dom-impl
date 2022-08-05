package com.jslib.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.DocumentBuilder;
import com.jslib.util.Classes;
import com.jslib.util.Strings;

import junit.framework.TestCase;

public class BuilderUnitTest
{
  @Test
  public void documentValidation() throws SAXException, IOException
  {
    File file = file("document-utf.xml");

    Validator validator = getSchema().newValidator();
    Source source = new StreamSource(file);
    try {
      validator.validate(source);
    }
    catch(SAXParseException e) {
      fail("Validation with good schema should past.");
    }

    validator = getBadSchema().newValidator();
    source = new StreamSource(file);
    try {
      validator.validate(source);
      TestCase.fail("Validation with bad schema should throw SAX parse exception.");
    }
    catch(SAXParseException e) {}
  }

  @Test
  public void createXML() throws IOException
  {
    Document document = builder().createXML("root");
    assertNotNull(document);
    org.w3c.dom.Document doc = Classes.getFieldValue(document, "doc");
    assertNotNull(doc);
    assertEquals("root", doc.getDocumentElement().getTagName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createXML_NullRoot()
  {
    builder().createXML(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createXML_EmptyRoot()
  {
    builder().createXML(null);
  }

  @Test
  public void parseXml_UTF8() throws SAXException
  {
    String string = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //
        "<body>" + //
        "    <h1 id=\"id1\">ηεαδερ 1</h1>" + //
        "    <h2 id=\"id2\">ηεαδερ 2</h2>" + //
        "    <h3 id=\"id3\">ηεαδερ 3</h3>" + //
        "</body>";

    assertUtfDocument(builder().parseXML(string));
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseXML_EmptyString() throws SAXException
  {
    builder().parseXML("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseXML_NullString() throws SAXException
  {
    builder().parseXML(null);
  }

  @Test
  public void loadXML_UtfFile() throws SAXException, IOException
  {
    File file = file("document-utf.xml");
    assertUtfDocument(builder().loadXML(file));
  }

  @Test
  public void loadXML_IsoFile() throws SAXException, IOException
  {
    File file = file("document-iso.xml");
    assertIsoDocument(builder().loadXML(file));
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadXML_NullFile() throws IOException, SAXException
  {
    builder().loadXML((File)null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadXML_NotFile() throws IOException, SAXException
  {
    builder().loadXML(new File("."));
  }

  @Test
  public void loadXML_UtfStream() throws IOException, SAXException
  {
    assertUtfDocument(builder().loadXML(stream("document-utf.xml")));
  }

  @Test
  public void loadXML_IsoStream() throws IOException, SAXException
  {
    assertIsoDocument(builder().loadXML(stream("document-iso.xml")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadXML_NullStream() throws IOException, SAXException
  {
    builder().loadXML((InputStream)null);
  }

  @Test
  public void loadXML_UtfSource() throws IOException, SAXException
  {
    assertUtfDocument(builder().loadXML(file("document-utf.xml")));
  }

  @Test
  public void loadXML_IsoSource() throws IOException, SAXException
  {
    assertIsoDocument(builder().loadXML(file("document-iso.xml")));
  }

  public void loadXML_UtfFromURL() throws IOException, SAXException
  {
    URL url = new URL("http://test.bbnet.ro/utf.xml");
    assertUtfDocument(builder().loadXML(url));
  }

  public void parseXML_IsoURL() throws IOException, SAXException
  {
    URL url = new URL("http://test.bbnet.ro/iso.xml");
    assertIsoDocument(builder().loadXML(url));
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseXML_NullURL() throws IOException, SAXException
  {
    builder().loadXML((URL)null);
  }

  @Test
  public void loadHTML_UtfFile() throws IOException, SAXException
  {
    assertUtfDocument(builder().loadHTML(file("page-utf.html")));
  }

  @Test
  public void loadHTML_IsoFile() throws IOException, SAXException
  {
    assertIsoDocument(builder().loadHTML(file("page-iso.html")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadHTML_NullFile() throws IOException, SAXException
  {
    builder().loadHTML((File)null);
  }

  @Test
  public void parserDetection() throws IOException, SAXException
  {
    final int READ_AHEAD_SIZE = 20;

    InputStream inputStream = new BufferedInputStream(stream("page-index.html"));
    assert inputStream.markSupported();
    inputStream.mark(READ_AHEAD_SIZE);

    byte[] bytes = new byte[READ_AHEAD_SIZE];
    for(int i = 0; i < READ_AHEAD_SIZE; ++i) {
      bytes[i] = (byte)inputStream.read();
    }
    String prolog = new String(bytes, "UTF-8");
    boolean isXML = prolog.startsWith("<?xml");

    inputStream.reset();
    Document doc = isXML ? builder().loadXML(inputStream) : builder().loadHTML(inputStream);
    assertNotNull(doc);
  }

  @Test
  public void loadHTML_UtfStream() throws IOException, SAXException
  {
    assertUtfDocument(builder().loadHTML(stream("page-utf.html")));
  }

  @Test
  public void loadHTML_IsoStream() throws IOException, SAXException
  {
    assertIsoDocument(builder().loadHTML(stream("page-iso.html")));
  }

  @Test
  public void loadHTML_TextWithApostrophe() throws IOException, SAXException
  {
    assertUtfDocument(builder().loadHTML(stream("page-apostrophe.html")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadHTML_NullStream() throws IOException, SAXException
  {
    builder().loadHTML((InputStream)null);
  }

  @Test
  public void loadHTML_UtfSource() throws IOException, SAXException
  {
    assertUtfDocument(builder().loadHTML(file("page-utf.html")));
  }

  @Test
  public void loadHTML_IsoSource() throws IOException, SAXException
  {
    assertIsoDocument(builder().loadHTML(file("page-iso.html")));
  }

  @Test
  public void loadHTML5_File() throws IOException, SAXException
  {
    Document doc = builder().loadHTML(file("page-html5.html"));
    assertEquals("Current Value in €", doc.getByTag("h1").getText());
    assertEquals("current value in €.", doc.getByTag("p").getAttr("title"));
    assertEquals("copyright © j(s)-lib tools ® 2013", doc.getByTag("footer").getText());
  }

  @Test
  public void parseHTML5_String() throws IOException, SAXException
  {
    String html5 = Strings.load(file("page-html5.html"));
    Document doc = builder().parseHTML(html5);
    assertEquals("Current Value in €", doc.getByTag("h1").getText());
    assertEquals("current value in €.", doc.getByTag("p").getAttr("title"));
    assertEquals("copyright © j(s)-lib tools ® 2013", doc.getByTag("footer").getText());
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHTML5_NullString() throws SAXException
  {
    builder().parseHTML((String)null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHTML5_EmptyString() throws SAXException
  {
    builder().parseHTML("");
  }

  public void loadHTML_UtfURL() throws IOException, SAXException
  {
    assertUtfDocument(builder().loadHTML(new URL("http://test.bbnet.ro/utf.html")));
  }

  public void loadHTML_IsoURL() throws IOException, SAXException
  {
    assertIsoDocument(builder().loadHTML(new URL("http://test.bbnet.ro/iso.html")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadHTML_NullURL() throws IOException, SAXException
  {
    assertIsoDocument(builder().loadHTML((URL)null));
  }

  @Test
  public void localLoadedDTD() throws IOException, SAXException
  {
    builder().loadXML(stream("web.xml"));
  }

  // ----------------------------------------------------------------------------------------------

  private static DocumentBuilder builder()
  {
    return new DocumentBuilderImpl();
  }

  private void assertUtfDocument(Document document)
  {
    TestCase.assertNotNull(document);
    org.w3c.dom.Document doc = Classes.getFieldValue(document, "doc");
    TestCase.assertNotNull(doc);
    TestCase.assertEquals("ηεαδερ 1", doc.getElementsByTagName("h1").item(0).getTextContent());
    TestCase.assertEquals("ηεαδερ 2", doc.getElementsByTagName("h2").item(0).getTextContent());
    TestCase.assertEquals("ηεαδερ 3", doc.getElementsByTagName("h3").item(0).getTextContent());
  }

  private void assertIsoDocument(Document document)
  {
    TestCase.assertNotNull(document);
    org.w3c.dom.Document doc = Classes.getFieldValue(document, "doc");
    TestCase.assertNotNull(doc);
    TestCase.assertEquals("hedîr 1", doc.getElementsByTagName("h1").item(0).getTextContent());
    TestCase.assertEquals("hedîr 2", doc.getElementsByTagName("h2").item(0).getTextContent());
    TestCase.assertEquals("hedîr 3", doc.getElementsByTagName("h3").item(0).getTextContent());
  }

  private InputStream stream(String resource) throws FileNotFoundException
  {
    return new FileInputStream(new File("src/test/resources/" + resource));
  }

  private File file(String resource)
  {
    return new File("src/test/resources/" + resource);
  }

  private Schema getSchema() throws SAXException
  {
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    return sf.newSchema(file("schema.xsd"));
  }

  private Schema getBadSchema() throws SAXException
  {
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    return sf.newSchema(file("schema-bad.xsd"));
  }
}
