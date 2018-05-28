package js.dom.w3c;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import js.dom.Document;
import js.dom.EList;
import js.lang.BugError;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM utility class.
 * 
 * @author Iulian Rotaru
 */
public final class DomUtil {
	public static EList findByNonEmptyText(Document doc) {
		// I did not find a XPath expression to return text nodes only
		// the only option I have is brute force: get elements and text and remove nodes with child elements

		String expression = "//*[child::text()[normalize-space()][string-length()>0]]";
		XPath xpath = XPathFactory.newInstance().newXPath();
		List<Node> textNodes = new ArrayList<Node>();
		try {
			NodeList nodes = (NodeList) xpath.evaluate(expression, ((DocumentImpl)doc).getDocument(), XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NodeList children = node.getChildNodes();
				if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
					// accept only nodes with a single child of type text node
					textNodes.add(node);
				}
			}
		} catch (XPathExpressionException e) {
			throw new BugError("Invalid XPath expression |%s|.", expression);
		}

		class TextNodeList implements NodeList {
			List<Node> textNodes;

			TextNodeList(List<Node> textNodes) {
				this.textNodes = textNodes;
			}

			@Override
			public Node item(int index) {
				return this.textNodes.get(index);
			}

			@Override
			public int getLength() {
				return this.textNodes.size();
			}
		}
		return new EListImpl(doc, new TextNodeList(textNodes));
	}

	private DomUtil() {
	}
}
