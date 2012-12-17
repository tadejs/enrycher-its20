package si.ijs.its20;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestDisambiguationHTML {

	protected static final String disambigInputRoot = "ITS-2.0-Testsuite/its2.0/inputdata/disambiguation";
	protected static final String disambigOutputRoot = "ITS-2.0-Testsuite/its2.0/expected/disambiguation";
	
	protected void testHTMLDisambig(String testName) {
		System.out.println("====" +testName);
		DocumentBuilder builder = DOMSnapshot.getHtmlParser();
		try {
			InputStream is = new FileInputStream(new File(disambigInputRoot + "/html/" + testName + ".html"));
			Document doc = builder.parse(is);
			is.close();
			DOMSnapshot snap =new DOMSnapshot(doc, disambigInputRoot + "/html/", new HTMLDialect());
			
			List<String> lst = snap.getLines();
			for (String line : lst) {
				System.out.println(line);
			}
			
			List<String> expected = new ArrayList<String>();
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(disambigOutputRoot + "/html/" + testName + "output.txt")));
			String line = null;
			while (null != (line = rdr.readLine())) {
				expected.add(line);
			}
			rdr.close();
					
			Assert.assertArrayEquals(expected.toArray(), lst.toArray());
			
			
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SAXException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testHTML1() {
		testHTMLDisambig("disambiguation1html");
	}

	@Test
	public void testHTML2() {
		testHTMLDisambig("disambiguation1html");
	}

	
	@Test
	public void testHTML3() {
		testHTMLDisambig("disambiguation3html");
	}

	
	@Test
	public void testHTML4() {
		testHTMLDisambig("disambiguation4html");
	}

}
