package aas;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

public class Math {
	private static final List<String> ARRAY_PROPERTY = Arrays.asList(new String[]{"math","apply"});
	
	public String parserXml(String xml) throws Exception {
        StringReader reader = new StringReader(xml);
        SAXReader sax = new SAXReader(false);
        sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        String result = null;
        try {
            // 通过输入源构造一个Document  
    		Document doc = sax.read(new InputSource(new StringReader(xml)));
            // 取的根元素
            Element root = doc.getRootElement();
            List children = root.elements();
            //解析xml
            StringBuilder stb = new StringBuilder();
            toFormula(stb, children);
            result = stb.toString();
        } catch (Exception e) {
            System.err.print(e.getMessage());
            throw e;
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
        return result;
    }		
    
	public void toFormula(StringBuilder sb, List<Element> elements) {
		Element el = null;
        String name = "";
        
        Element node = elements.get(0);
        if (isMathOrApply(node.getName()) && elements.size() <= 1) {
			if (elements.size() == 1) {
				toFormula(sb, node.elements());
			}
		} 		
		if (elements.size() >= 3) {		
			String operator = "op";
			for (int i = 0; i < elements.size(); i++) {			
				ArrayList<String> arrlist = new ArrayList<String>();
				node = elements.get(i);
				//System.out.println(node.getName());	
				if (i == 0) {
					operator = getOperator(node.getName());
				} else if (isMathOrApply(node.getName())) {
					//System.out.println("found apply");
					sb.append("(");
					toFormula(sb,node.elements());
					sb.append(")");
				} else {
					//System.out.println("node value: " + node.getTextTrim());
					String value = node.getTextTrim();
					if (!value.isEmpty()) {
						sb.append(value);
						//sb.append("(").append(value).append(")");
					}
					/*
					if(arrlist.isEmpty()) {
						arrlist.add(node.getTextTrim());
						System.out.println("added :" + node.getTextTrim());
					} else {
						arrlist.add(operator);
						System.out.println("added :" + operator);
						arrlist.add(node.getTextTrim());
						System.out.println("added :" + node.getTextTrim());
					}*/
				}
				if (i != 0 && i+1 < elements.size() && !operator.isEmpty()) {
					sb.append(operator);
				}
			}		
		}	
	}
	
	private static boolean isMathOrApply(String nodeName) {
		return nodeName != null && (nodeName.equals("math") || nodeName.equals("apply"));
	}
	
	private String getOperator(String s) {
		String op = null;
		switch (s) {
			case "plus": 
				op = "+"; break;
			case "times": 
				op = "*"; break;
			case "eq": 
				op = "="; break;
			default:
		}
		
		
		return op;
	}
	
	private void printList(ArrayList<String> list) {
		Iterator i = list.iterator();
		while (i.hasNext()) {
			System.out.println(i.next());
		}
	}
	
	

  

}
