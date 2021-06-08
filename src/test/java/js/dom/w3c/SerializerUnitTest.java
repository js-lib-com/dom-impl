package js.dom.w3c;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.util.Strings;

public class SerializerUnitTest
{
  /**
   * X(HT)ML reserved characters. There are 5 reserved characters that can't be present into text content because
   * X(HT)ML processors could mistake them for markup. These are: quotation mark, apostrophe, ampersand, less-than and
   * greater-than.
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
  private Serializer serializer;

  @Before
  public void beforeTest() throws Exception
  {
    writer = new StringWriter();
    serializer = new Serializer(writer);
  }

  @Test
  public void testSerializeReservedChars() throws Exception
  {
    DocumentImpl doc = getDocument(RESERVED_CHARS);
    serializer.serialize(doc);
    // serialize escape all reserved characters
    assertEquals(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%1$s<tag attr=\"&lt;&gt;&amp;&quot;&apos;\">&lt;&gt;&amp;&quot;&apos;</tag>%1$s", System.lineSeparator()));
  }

  @Test
  public void testNiceSuspectChars() throws Exception
  {
    DocumentImpl doc = getDocument(SUSPECT_CHARS);
    serializer.serialize(doc);
    assertThat(getAttr(), equalTo(SUSPECT_CHARS));
    assertThat(getText(), equalTo(SUSPECT_CHARS));
  }

  @Test
  public void testSerializeUnicodeChars() throws Exception
  {
    DocumentImpl doc = getDocument(UNICODE_CHARS);
    serializer.serialize(doc);
    assertThat(getAttr(), equalTo(UNICODE_CHARS));
    assertThat(getText(), equalTo(UNICODE_CHARS));
  }

  @Test
  public void testSerializeHtml() throws Exception
  {
    DocumentImpl doc = (DocumentImpl)builder().loadHTML(file("page-simple.html"));
    serializer.serialize(doc);
    assertTrue(writer.toString().startsWith("<!DOCTYPE html>"));
  }

  @Test
  public void testSerializerXmlEntity() throws Exception
  {
    File file = file("strings.xml");
    DocumentImpl doc = (DocumentImpl)builder().loadXML(file);
    serializer.serialize(doc);
    assertTrue(writer.toString().contains("copyright © j(s)-lib tools ® 2013"));
  }

  @Test
  public void testSerializerXmlEntityToFile() throws Exception
  {
    File xml = file("strings.xml");
    DocumentImpl doc = (DocumentImpl)builder().loadXML(xml);

    File xmlser = file("strings.ser");
    FileWriter writer = new FileWriter(xmlser);
    Serializer serializer = new Serializer(writer);
    serializer.serialize(doc);
    writer.close();

    assertTrue(Strings.load(xmlser).contains("copyright © j(s)-lib tools ® 2013"));
  }

  @Test
  public void testInlineScript() throws Exception
  {
    File file = file("inline-script.js");
    DocumentImpl doc = (DocumentImpl)builder().loadXML(file);
    serializer.serialize(doc);
    assertTrue(writer.toString().contains("(window,document,'script','//www.google-analytics.com/analytics.js','ga');"));
  }

  @Test
  public void GivenWebXmlFile_ThenAsExpected() throws Exception
  {
    // given
    File file = file("web.xml");
    DocumentImpl doc = (DocumentImpl)builder().loadXML(file);
    
    // when
    serializer.serialize(doc);

    // then
    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%1$s" + //
        "<web-app>%1$s" + //
        "\t<display-name>j(s)-lib Management</display-name>%1$s" + //
        "\t<description></description>%1$s" + //
        "\t<listener>%1$s" + //
        "\t\t<listener-class>js.web.Listener</listener-class>%1$s" + //
        "\t</listener>%1$s" + //
        "\t<servlet>%1$s" + //
        "\t\t<servlet-name>dispatcher</servlet-name>%1$s" + //
        "\t\t<servlet-class>js.web.DispatcherServlet</servlet-class>%1$s" + //
        "\t\t<load-on-startup>1</load-on-startup>%1$s" + //
        "\t</servlet>%1$s" + //
        "\t<servlet-mapping>%1$s" + //
        "\t\t<servlet-name>dispatcher</servlet-name>%1$s" + //
        "\t\t<url-pattern>*.rmi</url-pattern>%1$s" + //
        "\t</servlet-mapping>%1$s" + //
        "</web-app>%1$s";
    assertThat(writer.toString(), equalTo(format(expected, System.lineSeparator())));
  }

  @Test
  public void GivenComplexHTML5_ThenNoException() throws Exception
  {
    // given
    File file = file("page-html5.html");
    DocumentImpl doc = (DocumentImpl)builder().loadXML(file);
    
    // when
    serializer.serialize(doc);
    
    // then
  }

  @Test
  public void GivenElementWithText_ThenAsExpected() throws Exception
  {
    // given
    File file = file("strings.xml");
    DocumentImpl doc = (DocumentImpl)builder().loadXML(file);

    // when
    serializer.serialize(doc);

    // then
    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%1$s" + //
        "<string>%1$s" + //
        "\t<copyright>copyright © j(s)-lib tools ® 2013</copyright>%1$s" + //
        "</string>%1$s";
    assertThat(writer.toString(), equalTo(format(expected, System.lineSeparator())));
  }

  @Test
  public void GivenNoXmlDeclaration_ThenNotStartsWith() throws Exception
  {
    // given
    File file = file("strings.xml");
    DocumentImpl doc = (DocumentImpl)builder().loadXML(file);
    serializer.setXmlDeclaration(false);

    // when
    serializer.serialize(doc);

    // then
    assertThat(writer.toString(), not(startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")));
  }

  // --------------------------------------------------------------------------------------------

  private void assertEquals(String expected)
  {
    assertThat(this.writer.getBuffer().toString(), equalTo(expected));
  }

  private static DocumentImpl getDocument(String charset)
  {
    Document doc = builder().createXML("tag");
    doc.getRoot().setText(charset).setAttr("attr", charset);
    return (DocumentImpl)doc;
  }

  private static DocumentBuilder builder()
  {
    return new DocumentBuilderImpl();
  }

  private String getAttr()
  {
    String s = this.writer.getBuffer().toString();
    int i = s.indexOf("attr=\"") + 6;
    int j = s.indexOf('"', i + 1);
    return s.substring(i, j);
  }

  private String getText()
  {
    String s = this.writer.getBuffer().toString();
    int i = s.indexOf("\">") + 2;
    int j = s.indexOf('<', i + 1);
    return s.substring(i, j);
  }

  private static File file(String path)
  {
    return new File("src/test/resources/" + path);
  }
}
