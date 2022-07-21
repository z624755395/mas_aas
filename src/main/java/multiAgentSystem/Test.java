package multiAgentSystem;

import java.util.ArrayList;

public class Test {

	public static void main(String[] args) {
		String a = "3;  4;12";
		String s = a.replaceAll(" ","");
		System.out.println(s);
		String b[] = s.split(";");
		ArrayList<Double> ar = new ArrayList<Double>();
		
		for (int i = 0; i < b.length; i++)
			ar.add(Double.parseDouble(b[i]));
		
		System.out.println(ar);
		
		double result = 0;
		double[] parameter = {1,2,3}; 
		try {
			for(int i = 0; i < ar.size(); i++) {				
			result = result + ar.get(i) * parameter[i];
			}
		} catch (Exception e) {
			System.out.println("cannot calculate the total coast, wrong dimension");
		}
		
		System.out.println(result);
		
	}

}
