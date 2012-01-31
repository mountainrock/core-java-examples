package tutorial;

import java.util.ArrayList;
import java.util.List;

public class GenericsDemo {

	public static void main(final String[] args) {
		//Without generics
		final List basket = new ArrayList();
		basket.add("jd");
		basket.add(new Pepsi());
		
		final String hotDrink= (String) basket.get(0);
		//the below will not give compile error but throws class cast runtime exception
		final String coolDrink= (String) basket.get(1);
		
		//with generics
		final List<String> basketWithGenerics = new ArrayList<String>();
		basketWithGenerics.add("jd");
		//the below line is not allowed. Compile time error as we are marking this list to only hold strings 
		//basketWithGenerics.add(new Pepsi()); 

		
	}

}

class Pepsi{
	
}