package si.ijs.its20.test;

import org.junit.Test;

public class TestDomainXML {
		
	protected void testXMLDomain(String testName) {
		//System.out.println("====" +testName);
		TestUtils.runTestXML( "ITS-2.0-Testsuite/its2.0/inputdata/domain", "ITS-2.0-Testsuite/its2.0/expected/domain", "output/domain", testName);
	}
	
	
	@Test
	public void testDomain1() {
		testXMLDomain("domain1xml");
	}
	@Test
	public void testDomain2() {
		testXMLDomain("domain2xml");
	}
	@Test
	public void testDomain4() {
		testXMLDomain("domain4xml");
	}
	
	@Test
	public void testDomain5() {
		testXMLDomain("domain5xml");
	}
	@Test
	public void testDomain6() {
		testXMLDomain("domain6xml");
	}
	@Test
	public void testDomain7() {
		testXMLDomain("domain7xml");
	}
}
