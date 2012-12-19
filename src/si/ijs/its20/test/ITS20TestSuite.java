package si.ijs.its20.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({TestIO.class, 
	TestDisambiguationXML.class, 
	TestDisambiguationHTML.class,
	TestDomainXML.class,
	TestDomainHTML.class})
public class ITS20TestSuite {

}
