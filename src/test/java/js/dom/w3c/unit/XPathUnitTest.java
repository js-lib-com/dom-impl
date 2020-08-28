package js.dom.w3c.unit;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.EList;
import js.dom.Element;
import js.dom.NamespaceContext;
import js.dom.w3c.DocumentBuilderImpl;
import junit.framework.TestCase;

public class XPathUnitTest extends TestCase
{
  private Document doc;

  public void testElement()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<root id='1'>" + //
        "   <child id='2'>" + //
        "       <nephew id='3'>" + //
        "           <child id='4'></child>" + //
        "       </nephew>" + //
        "   </child>" + //
        "</root>";
    doc = builder().parseXML(xml);

    assertElement("root", 1);
    assertElement("/root", 1);
    assertElement("//root", 1);

    // assertGetChild(2, "child");
    assertElement("/root/child", 2);

    assertEquals("3", doc.getByXPath("/root/child/nephew").getAttr("id"));
    assertNull(doc.getByXPath("/child"));
    assertNull(doc.getByXPath("/child/nephew"));

    Element root = doc.getRoot();
    assertNotNull(root);

    Element child = root.getByXPath("child::%s", "child");
    assertNotNull(child);

    Element nephew = child.getByXPath("child::%s", "nephew");
    assertNotNull(nephew);
  }

  private void assertElement(String xpath, int expectedId)
  {
    assertEquals(expectedId, Integer.parseInt(doc.getByXPath(xpath).getAttr("id")));
  }

  public void testEList()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<bloodline id='1'>" + //
        "   <child id='2'>" + //
        "       <nephew id='3'>" + //
        "           <child id='4'></child>" + //
        "       </nephew>" + //
        "   </child>" + //
        "</bloodline>";
    Document doc = builder().parseXML(xml);
    Element root = doc.getRoot();
    Element nephew = doc.getByTag("nephew");

    // ------------------------------------------------
    // DIRECT ELEMENTS BY TAG

    // child:: axe is default so child::tag === tag
    // select direct elements with 'bloodline' tag name
    assertEList(doc, "bloodline", 1);
    assertEList(doc, "child::bloodline", 1);
    // there is no direct element with tag 'child' on document element, that is, root
    assertEList(doc, "child");
    assertEList(doc, "child::child");
    // select direct 'child' element on root element
    assertEList(root, "child", 2);
    assertEList(root, "child::child", 2);
    // select direct element with tag 'child' on nephew element
    assertEList(nephew, "child", 4);
    assertEList(nephew, "child::child", 4);

    // ------------------------------------------------
    // ABSOLUTE XPATH

    // select the 'bloodline' element since path is absolute, that is, starts with slash
    assertEList(doc, "/bloodline", 1);
    // absolute xpath is always evaluated from root no matter context element, i.e. document or element
    assertEList(root, "/bloodline", 1);
    assertEList(nephew, "/bloodline", 1);
    // root element tag is 'bloodline' not 'child'
    assertEList(doc, "/child");
    assertEList(root, "/child");
    assertEList(nephew, "/child");

    // ------------------------------------------------
    // ELEMENTS BY TAG ANYWHERE

    // select element with 'bloodline' tag name in all document because xpath starts with double slash
    assertEList(doc, "//bloodline", 1);
    // there are two elements with 'child' tag into document
    assertEList(doc, "//child", 2, 4);
    // double slash xpath is evaluated on entire document no matter context, i.e. document or element
    assertEList(root, "//child", 2, 4);
    assertEList(nephew, "//child", 2, 4);

    // ------------------------------------------------
    // DESCENDANTS

    assertEList(root, "child::child", 2);
    assertEList(root, "descendant::child", 2, 4);
  }

  private static void assertEList(Element el, String xpath, int... expectedIds)
  {
    assertEList(el.findByXPath(xpath), expectedIds);
  }

  private static void assertEList(Document doc, String xpath, int... expectedIds)
  {
    assertEList(doc.findByXPath(xpath), expectedIds);
  }

  private static void assertEList(EList elist, int... expectedIds)
  {
    assertEquals("Elements counts ", expectedIds.length, elist.size());
    for(int i = 0; i < expectedIds.length; ++i) {
      assertEquals("Element id ", expectedIds[i], Integer.parseInt(elist.item(i).getAttr("id")));
    }
  }

  public void testGetChildNS()
  {
    final String NS1 = "js-lib.com/ns1";
    final String NS2 = "js-lib.com/ns2";

    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<root xmlns:ns1='js-lib.com/ns1' xmlns:ns2='js-lib.com/ns2'>" + //
        "   <ns1:child attr='attr' ns1:attr='ns1.attr'>" + //
        "       <nephew>" + //
        "           <ns2:child attr='attr' ns2:attr='ns2.attr'></ns2:child>" + //
        "       </nephew>" + //
        "   </ns1:child>" + //
        "</root>";
    Document doc = builder().parseXMLNS(xml);

    // Element el = doc.getByXPathNS(NS1, "//ns1:child");
    // assertNotNull(el);
    // assertEquals("attr", el.getAttr("attr"));
    // assertEquals("ns1.attr", el.getAttrNS(NS1, "attr"));

    // Element el = doc.getByXPathNS(NS1, "/root/ns1:child/nephew/ns2:child");

    Element el = doc.getByXPathNS(new NamespaceContext()
    {
      @Override
      public String getNamespaceURI(String prefix)
      {
        if(prefix.equals("ns1")) {
          return NS1;
        }
        if(prefix.equals("ns2")) {
          return NS2;
        }
        return super.getNamespaceURI(prefix);
      }
    }, "/root/ns1:child/nephew");
    assertNotNull(el);
  }

  public void testChildWithAttribute()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<root>" + //
        "   <heroes>" + //
        "       <child name='Paris'>paris</child>" + //
        "       <child name='Heracle'>heracle</child>" + //
        "   </heroes>" + //
        "</root>";
    Document doc = builder().parseXML(xml);
    Element heroes = doc.getByTag("heroes");

    assertEquals(2, doc.findByXPath("//child[@name]").size());
    assertEquals(1, doc.findByXPath("//child[@name='Paris']").size());
    assertEquals(1, doc.findByXPath("//child[@name='Heracle']").size());

    assertEquals(2, heroes.findByXPath("child[@name]").size());
    assertEquals(1, heroes.findByXPath("child[@name='Paris']").size());
    assertEquals(1, heroes.findByXPath("child[@name='Heracle']").size());

    assertEquals("paris", doc.getByXPath("//child[@name='Paris']").getText());
    assertEquals("paris", heroes.getByXPath("child[@name='Paris']").getText());
    assertEquals("heracle", doc.getByXPath("//child[@name='Heracle']").getText());
    assertEquals("heracle", heroes.getByXPath("child[@name='Heracle']").getText());

    // letter case matters
    assertNull(doc.getByXPath("//CHILD[@name='Paris']"));
    assertNull(doc.getByXPath("//child[@name='paris']"));
  }

  public void testChildWithAttributeNS()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<root xmlns:ns='js-lib.com/ns'>" + //
        "   <heroes>" + //
        "       <child ns:name='Paris'>paris</child>" + //
        "       <child ns:name='Heracle'>heracle</child>" + //
        "       <child name='Achile'>achile</child>" + //
        "   </heroes>" + //
        "</root>";
    Document doc = builder().parseXMLNS(xml);

    EList elist = doc.findByXPathNS(new NamespaceContext()
    {
      @Override
      public String getNamespaceURI(String prefix)
      {
        if(prefix.equals("ns")) {
          return "js-lib.com/ns";
        }
        return super.getNamespaceURI(prefix);
      }
    }, "//*[@ns:name]");

    assertNotNull(elist);
    assertEquals(2, elist.size());
    assertEquals("paris", elist.item(0).getText());
    assertEquals("heracle", elist.item(1).getText());
  }

  public void testXpathCaseSensitiveOnHTML()
  {
    String html = "" + //
        "<BODY>" + //
        "<H1 title='title'>test</H1>" + //
        "</BODY>";
    Document doc = builder().parseXML(html);

    assertEquals("test", doc.getByXPath("//H1").getText());
    assertEquals("test", doc.getByXPath("//H1[@title]").getText());
    assertEquals("test", doc.getByXPath("//H1[@title='title']").getText());

    // xpath is case sensitive on tag name
    assertNotNull(doc.getByXPath("//H1[@title='title']"));
    assertNull(doc.getByXPath("//h1[@title='title']"));
  }

  public void testDescendantWithAttribute()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<root>" + //
        "   <gods>" + //
        "       <child name='Zeus'>zeus</child>" + //
        "       <child name='Hades'>hades</child>" + //
        "   </gods>" + //
        "   <heroes>" + //
        "       <child name='Paris'>paris</child>" + //
        "       <child name='Heracle'>heracle</child>" + //
        "   </heroes>" + //
        "</root>";
    Document doc = builder().parseXML(xml);
    String ATTR_NAME = "name";

    Element heroes = doc.getByTag("heroes");
    EList elist = heroes.findByXPath("descendant::*[@%s]", ATTR_NAME);
    assertEquals(2, elist.size());
    assertEquals("paris", elist.item(0).getText());
    assertEquals("heracle", elist.item(1).getText());
  }

  public void testChildPosition()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<parent>" + //
        "   <child>x</child>" + //
        "   <child>y</child>" + //
        "   <child>z</child>" + //
        "</parent>";
    Document doc = builder().parseXML(xml);

    assertEquals("x", doc.getByXPath("/parent/*[position()=1]").getText());
    assertEquals("y", doc.getByXPath("/parent/*[position()=2]").getText());
    assertEquals("z", doc.getByXPath("/parent/*[position()=3]").getText());
    assertEquals("z", doc.getByXPath("/parent/*[last()]").getText());

    assertEquals("x", doc.getByXPath("//child[position()=1]").getText());
    assertEquals("z", doc.getByXPath("//child[last()]").getText());

    Element parent = doc.getByXPath("parent");
    assertEquals("x", parent.getByXPath("child[position()=1]").getText());
    assertEquals("z", parent.getByXPath("child[last()]").getText());
  }

  public void testImgOrInputWithSrc()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<parent>" + //
        "   <img src='image.png' />" + //
        "   <img />" + //
        "   <input type='button' src='button.png' />" + //
        "   <input type='text' />" + //
        "</parent>";
    Document doc = builder().parseXML(xml);

    EList elist = doc.findByXPath("//img[@src]|//input[@src]");
    assertEquals(2, elist.size());
    assertEquals("image.png", elist.item(0).getAttr("src"));
    assertEquals("button.png", elist.item(1).getAttr("src"));
  }

  public void testAttributeContainingEditable()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<parent>" + //
        "   <div contenteditable='true'></div>" + //
        "</parent>";
    Document doc = builder().parseXML(xml);

    EList elist = doc.findByXPath("//*[@editable]");
    assertEquals(0, elist.size());
  }

  public void testAttributeContainingEditableNS()
  {
    String xml = "<?xml version='1.0' encoding='UTF-8'?>" + //
        "<parent xmlns:ns='js-lib.com/ns'>" + //
        "   <div ns:contenteditable='true'></div>" + //
        "</parent>";
    Document doc = builder().parseXMLNS(xml);

    EList elist = doc.findByXPathNS(new NamespaceContext()
    {
      @Override
      public String getNamespaceURI(String prefix)
      {
        if(prefix.equals("ns")) {
          return "js-lib.com/ns";
        }
        return super.getNamespaceURI(prefix);
      }
    }, "//*[@ns:editable]");
    
    assertEquals(0, elist.size());
  }

  private static DocumentBuilder builder()
  {
    return new DocumentBuilderImpl();
  }
}
