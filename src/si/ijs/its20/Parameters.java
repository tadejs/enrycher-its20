package si.ijs.its20;

import java.util.HashMap;
import java.util.Map;

public class Parameters {

	protected Map<String, String> paramMap;
	
	public Parameters() {

		paramMap = new HashMap<String, String>();
		
	}
	
	public void add(String key, String val) {
		paramMap.put(key, val);
	}
	

	public String expandParams(String val) {
		if (val.indexOf('$') == -1 || val == null)		 
			return val;
		
		for (Map.Entry<String, String> param : paramMap.entrySet()) {
			val = val.replace("$" + param.getKey(), "'" + param.getValue() + "'");
		}
		return val;
		
	}
}
