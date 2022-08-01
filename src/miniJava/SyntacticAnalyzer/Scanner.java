package miniJava.SyntacticAnalyzer;

import java.io.*;

import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.ErrorReporter;
import java.lang.Character;

public class Scanner {

	private InputStream inputStream;
	private ErrorReporter reporter;
	
	private char currentChar;
	private StringBuilder currentSpelling;
	private String temp;
	
	private boolean eot = false;
	
	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;
		
		readChar();
	}
	
	public Token scan() {
		
		//skip whitespace
		while(!eot && (currentChar == ' ' || currentChar == '\t'))
			skipIt();
		
		if(currentChar == '/') {
			inputStream.mark(1);
			try {
				int c = inputStream.read();
				char x = (char) c;
				if(x == '/') {
					inputStream.reset();
					skipIt();
					skipIt();
					while(!eot)
						skipIt();
				}
				else if(x == '*') {
					inputStream.reset();
					skipIt();
					skipIt();
					char lastChar = 'x';
					while(!(lastChar == '*' && x == '/')) {
						lastChar = x;
						skipIt();
					}
					skipIt();
				}
				else {
					inputStream.reset();
				}
			}
			catch (IOException e) {
				scanError("I/O Exception!");
				eot = true;
			}
		}
		
		//start of a token: collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind = scanToken();
		String spelling = currentSpelling.toString();
		
		
		//return new token
		return new Token(kind, spelling);
	}
	
	public TokenKind scanToken() {
		
		if (eot)
			return(TokenKind.EOT); 

		// scan Token
		if(currentChar == ';') {
			takeIt();
			return(TokenKind.SEMIC);
		}
		else if(currentChar == ',') {
			takeIt();
			return(TokenKind.COMMA);
		}
		else if(currentChar == '.') {
			takeIt();
			return(TokenKind.PERIOD);
		}
		else if(currentChar == '(') {
			takeIt();
			return(TokenKind.LPAREN);
		}
		else if(currentChar == ')') {
			takeIt();
			return(TokenKind.RPAREN);
		}	
		else if(currentChar == '{') {
			takeIt();
			return(TokenKind.LCURLY);
		}
		else if(currentChar == '}') {
			takeIt();
			return(TokenKind.RCURLY);
		}
		else if(currentChar == '[') {
			takeIt();
			return(TokenKind.LSQUARE);
		}
		else if(currentChar == ']') {
			takeIt();
			return(TokenKind.RSQUARE);
		}
		else if(currentChar == '+' || currentChar == '*') {
			takeIt();
			return(TokenKind.BINOP);
		}
		else if(currentChar == '&') {
			takeIt();
			if(currentChar == '&') {
				takeIt();
				return(TokenKind.BINOP);
			}
			return(TokenKind.ERROR);
		}
		else if(currentChar == '|') {
			takeIt();
			if(currentChar == '|') {
				takeIt();
				return(TokenKind.BINOP);
			}
			return(TokenKind.ERROR);
		}
		else if(currentChar == '=') {
			takeIt();
			if(currentChar == '=') {
				takeIt();
				return(TokenKind.BINOP);
			}
			return(TokenKind.EQUALS);
		}
		else if(currentChar == '>' || currentChar == '<') {
			takeIt();
			if(currentChar == '=') {
				takeIt();
			}
			return(TokenKind.BINOP);
		}
		else if(currentChar == '!') {
			takeIt();
			if(currentChar == '=') {
				takeIt();
				return(TokenKind.BINOP);
			}
			return(TokenKind.UNOP);
		}
		else if(currentChar == '-') {
			takeIt();
			return(TokenKind.MINUS);
		}
		else if(currentChar == '0' || currentChar == '1' || currentChar == '2'
				|| currentChar == '3' || currentChar == '4' || currentChar == '5' 
				|| currentChar == '6' || currentChar == '7' || currentChar == '8' 
				|| currentChar == '9') {
			while (isDigit(currentChar))
				takeIt();
			return(TokenKind.NUM);
		}
		else if(isLetter(currentChar)) {
			temp = "";
			while(isLetter(currentChar)) {
				temp = temp + currentChar;
				takeIt();
			}
			if(isDigit(currentChar) || currentChar == '_') {
				while(isLetter(currentChar) || isDigit(currentChar) || currentChar == '_') {
					takeIt();
				}
				return(TokenKind.ID);
			}
			else {
				switch (temp) {
				
				case "class":
					return(TokenKind.CLASS);
				
				case "void":
					return(TokenKind.VOID);
				
				case "new":
					return(TokenKind.NEW);
					
				case "public":
					return(TokenKind.PUBLIC);
					
				case "private":
					return(TokenKind.PRIVATE);
				
				case "static":
					return(TokenKind.STATIC);
					
				case "int":
					return(TokenKind.INT);
					
				case "boolean":
					return(TokenKind.BOOL);
					
				case "this":
					return(TokenKind.THIS);
					
				case "return":
					return(TokenKind.RETURN);
					
				case "if":
					return(TokenKind.IF);
					
				case "else":
					return(TokenKind.ELSE);
				
				case "while":
		 			return(TokenKind.WHILE);
					
				case "true":
					return(TokenKind.TRUE);
					
				case "false":
					return(TokenKind.FALSE);
					
				default:
					return(TokenKind.ID);
				}
			}
		}

		else {
			scanError("Unrecognized character '" + currentChar + "' in input");
			return(TokenKind.ERROR);
		}
	}
	private void takeIt() {
		currentSpelling.append(currentChar);
		nextChar();
	}

	private void skipIt() {
		nextChar();
	}

	private boolean isDigit(char c) {
		return (c >= '0') && (c <= '9');
	}
	
	private boolean isLetter(char c) {
		return Character.isLetter(c);
	}

	private void scanError(String m) {
		reporter.reportError("Scan Error:  " + m);
	}


	private final static char eolUnix = '\n';
	private final static char eolWindows = '\r';

	private void nextChar() {
		if (!eot)
			readChar();
	}

	private void readChar() {
		try {
			int c = inputStream.read();
			currentChar = (char) c;
			if (c == -1 || currentChar == eolUnix || currentChar == eolWindows) {
				eot = true;
			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}
}
