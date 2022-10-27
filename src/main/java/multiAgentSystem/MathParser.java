package multiAgentSystem;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * Convert MathML XML tree to string format.
 *
 * Content Markup MathML:
 * <math display = 'block'>
 *   <apply>
 *     <eq/>
 *     <ci>z</ci>
 *     <apply>
 *       <plus/>
 *       <apply>
 *         <times/>
 *         <cn>2</cn>
 *         <ci>e</ci>
 *       </apply>
 *       <apply>
 *         <times/>
 *         <cn>1</cn>
 *         <ci>p</ci>
 *       </apply>
 *       <apply>
 *         <times/>
 *         <cn>1</cn>
 *         <ci>d</ci>
 *       </apply>
 *     </apply>
 *   </apply>
 * </math>
 *
 * String form:
 * z=((2*e)+(1*p)+(1*d))
 *
 * @author Haochi Zhang 
 *
 */

public class MathParser {
	private static final List<String> ARRAY_PROPERTY = Arrays.asList(new String[]{"math","apply"});
	
	public String parserXml(String xml) throws Exception {
        StringReader reader = new StringReader(xml);
        SAXReader sax = new SAXReader(false);
        sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        String result = null;
        try {
    		Document doc = sax.read(new InputSource(new StringReader(xml)));
    		//get root element
            Element root = doc.getRootElement();
            List children = root.elements();
            //parse xml to String
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
				if (i == 0) {
					operator = getOperator(node.getName());
				} else if (isMathOrApply(node.getName())) {
					sb.append("(");
					toFormula(sb,node.elements());
					sb.append(")");
				} else {
					String value = node.getTextTrim();
					if (!value.isEmpty()) {
						sb.append(value);						
					}					
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
			case "minus":
				op = "-"; break;
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
