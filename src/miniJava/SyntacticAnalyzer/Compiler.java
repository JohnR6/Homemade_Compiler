package miniJava.SyntacticAnalyzer;

import java.io.*;
import miniJava.SyntacticAnalyzer.*;
import miniJava.*;

public class Compiler {

	public static void main(String args[]) {
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(args[0]);
		}
		catch (FileNotFoundException e) {
			System.out.println("Input file " + args[0] + " not found");
			System.exit(1);
		}
		
		ErrorReporter errorReporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream, errorReporter);
		Parser parser = new Parser(scanner, errorReporter);
		
		parser.parse();
		
		if (errorReporter.hasErrors()) {
			System.out.println("Failure");
			System.exit(4);
		}
		
		System.out.println("Success");
		System.exit(0);
	}
}
