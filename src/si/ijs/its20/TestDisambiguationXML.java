package si.ijs.its20;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestDisambiguationXML {

	protected static final String disambigInputRoot = "ITS-2.0-Testsuite/its2.0/inputdata/disambiguation";
	protected static final String disambigOutputRoot = "ITS-2.0-Testsuite/its2.0/expected/disambiguation";
	
	protected void testXMLDisambig(String testName) {
		System.out.println("====" +testName);
		DocumentBuilder builder = DOMSnapshot.getXMLParser();
		try {
			Document doc = builder.parse(new File(disambigInputRoot + "/xml/" + testName + ".xml"));
			
			DOMSnapshot snap =new DOMSnapshot(doc, disambigInputRoot + "/xml/", new XMLDialect());
			
			List<String> lst = snap.getLines();
			for (String line : lst) {
				System.out.println(line);
			}
			
			List<String> expected = new ArrayList<String>();
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(disambigOutputRoot + "/xml/" + testName + "output.txt")));
			String line = null;
			while (null != (line = rdr.readLine())) {
				expected.add(line);
			}
			rdr.close();
					
			Assert.assertArrayEquals(expected.toArray(), lst.toArray());
			
			
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void test1XML() {
		testXMLDisambig("disambiguation1xml");
	}
	
	@Test
	public void test2XML() {
		testXMLDisambig("disambiguation2xml");
	}
	
	@Test
	public void test3XML() {
		testXMLDisambig("disambiguation3xml");
	}
	

	@Test
	public void test4XML() {
		testXMLDisambig("disambiguation4xml");
	}
	

	@Test
	public void test5XML() {
		testXMLDisambig("disambiguation5xml");
	}
	
	@Test
	public void test6XML() {
		testXMLDisambig("disambiguation6xml");
	}
	

	@Test
	public void test7XML() {
		testXMLDisambig("disambiguation7xml");
	}
	
	@Test
	public void test8XML() {
		testXMLDisambig("disambiguation8xml");
	}
}
