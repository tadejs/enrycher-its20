package si.ijs.its20.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import si.ijs.its20.DOMSnapshot;
import si.ijs.its20.XMLDialect;

public class TestIO {

	@Test
	public void testXpathHtml() {
		String dir = "ITS-2.0-Testsuite/its2.0/inputdata/disambiguation/html/";
		String file = "disambiguation1html.html";
		CleanerProperties cp = new CleanerProperties();
		//cp.setNamespacesAware(true);
		//cp.set
		try {

			TagNode root = DOMSnapshot.getHtmlParser().clean(new File(dir + file));
			Object[] nl = root.evaluateXPath("//body/p/*[@id='dublin']");
			Assert.assertEquals(1, nl.length);
			
			Document doc = new DomSerializer(cp).createDOM(root);
			
			
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xpath = xpf.newXPath();
			
			XPathExpression xpe = xpath.compile("//body/p/*[@id='dublin']");
			NodeList nl2 = (NodeList) xpe.evaluate(doc, XPathConstants.NODESET);
			Assert.assertEquals(xpe.toString(), 1, nl2.getLength());
			
			/*xpe = xpath.compile("//h:body/h:p/h:*[@id='dublin']");
			nl = (NodeList) xpe.evaluate(doc, XPathConstants.NODESET);
			Assert.assertEquals(xpe.toString(), 1, nl.getLength());*/
			
			
			
			
		} catch (ParserConfigurationException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		} catch (XPatherException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
		
		
	}
	

	@Test
	public void testWhoAmI() throws SAXException, IOException {
		String dir = "ITS-2.0-Testsuite/its2.0/inputdata/disambiguation/xml/";
		String file = "disambiguation1xml.xml";
		Document doc = DOMSnapshot.getXMLParser().parse(new File(dir + file));
		
		DOMSnapshot snap = new DOMSnapshot(doc, dir, new XMLDialect());
		
		for (DOMSnapshot.ITSState state : snap.iterNodes()) {
			Assert.assertEquals(state.nodePath, DOMSnapshot.whoAmI(state.node));
		}
		
	}

	

}
