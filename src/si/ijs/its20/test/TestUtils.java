package si.ijs.its20.test;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import si.ijs.its20.DOMSnapshot;
import si.ijs.its20.HTMLDialect;
import si.ijs.its20.XMLDialect;

public class TestUtils {

	public static void runTestXML(String inputRoot, String outputRoot, String actualOutputRoot, String testName) {
		DocumentBuilder builder = DOMSnapshot.getXMLParser();
		try {
			Document doc = builder.parse(new File(inputRoot + "/xml/" + testName + ".xml"));
			
			DOMSnapshot snap =new DOMSnapshot(doc, inputRoot + "/xml/", new XMLDialect());
			
			
			List<String> errors = snap.validate();
			Assert.assertTrue(join(errors), errors.size() == 0);
			
			
			List<String> lst = snap.getLines();
			/*for (String line : lst) {
				System.out.println(line);
			}*/
			
			List<String> expected = new ArrayList<String>();
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(outputRoot + "/xml/" + testName + "output.txt")));
			String line = null;
			while (null != (line = rdr.readLine())) {
				expected.add(line);
			}
			rdr.close();
			
			/*for (int i = 0; i < lst.size(); i++) {
				System.out.println(lst.get(i) + " === " + expected.get(i));
			}*/
			
			
			FileOutputStream fos = new FileOutputStream(actualOutputRoot + "/xml/" + testName + "output.txt");
			OutputStreamWriter wri = new OutputStreamWriter(fos);
			for (String string : lst) {
				wri.write(string);
				wri.write("\n");
			}
			wri.flush();
			wri.close();
			

			Assert.assertArrayEquals(expected.toArray(), lst.toArray());
			
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	
	public static String join(List<String> lst) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String string : lst) {
			if (first) {
				first = false;
			} else {
				sb.append("\n");
			}
			sb.append(string);
		}
		return sb.toString();
	}
	
	public static void runTestHTML(String inputRoot, String outputRoot, String actualOutputRoot, String testName) {
		HtmlCleaner builder = DOMSnapshot.getHtmlParser();
		try {
			InputStream is = new FileInputStream(new File(inputRoot + "/html/" + testName + ".html"));
			TagNode tag = builder.clean(is);
			
			CleanerProperties cp = new CleanerProperties();
			
			Document doc = new DomSerializer(cp).createDOM(tag);
			
					//builder.parse(is);
			is.close();
			DOMSnapshot snap =new DOMSnapshot(doc, inputRoot + "/html/", new HTMLDialect());
			
			
			List<String> errors = snap.validate();
			Assert.assertTrue(join(errors), errors.size() == 0);
			
			
			List<String> lst = snap.getLines();
			
			
			List<String> expected = new ArrayList<String>();
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(outputRoot + "/html/" + testName + "output.txt")));
			String line = null;
			while (null != (line = rdr.readLine())) {
				expected.add(line);
			}
			rdr.close();
					
			/*for (int i = 0; i < lst.size(); i++) {
				System.out.println(lst.get(i) + " === " + expected.get(i));
			}*/
			
			FileOutputStream fos = new FileOutputStream(actualOutputRoot + "/html/" + testName + "output.txt");
			OutputStreamWriter wri = new OutputStreamWriter(fos);
			for (String string : lst) {
				wri.write(string);
				wri.write("\n");
			}
			wri.flush();
			wri.close();
			

			Assert.assertArrayEquals(expected.toArray(), lst.toArray());
			
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} /*catch (SAXException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} */catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
