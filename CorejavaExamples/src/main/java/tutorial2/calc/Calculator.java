package tutorial2.calc;
import java.util.Scanner;

public class Calculator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
    
		Scanner iname = new Scanner(System.in);
		double fnum, snum, total;
		System.out.println("Enter first number: ");
		fnum = iname.nextDouble();
		System.out.println("Enter second number: ");
		snum = iname.nextDouble();
		total = fnum + snum;
		System.out.println("Total: " + total);
		System.out.println(total);
	}

}
