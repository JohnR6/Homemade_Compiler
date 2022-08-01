package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.ErrorReporter;

public class Parser {
	
	private Scanner scanner;
	private ErrorReporter reporter;
	private Token token;
	private boolean trace = true;
	
	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.scanner = scanner;
		this.reporter = reporter;
	}
	
	/**
	 * SyntaxError is used to unwind parse stack when parse fails
	 *
	 */
	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;	
	}
	
	public void parse() {
		token = scanner.scan();
		try {
			parseProgram();
		}
		catch (SyntaxError e) { }
	}
	
	//Program ::= (ClassDeclaration)* eot
	private void parseProgram() throws SyntaxError {
		while (token.kind != TokenKind.EOT) {
				parseCD();
		}
		accept(TokenKind.EOT);
	}
	
	//ClassDeclaration ::= class id { ( FieldDeclaration | MethodDeclaration )* }     
	private void parseCD() throws SyntaxError {
		accept(TokenKind.CLASS);
		accept(TokenKind.ID);
		accept(TokenKind.LCURLY);
		while(token.kind != TokenKind.RCURLY) {
			parseFMD();
		}
		accept(TokenKind.RCURLY);
	}
	
	//FieldDeclaration ::= Visibility Access Type id ;
	//MethodDeclaration ::= Visibility Access ( Type | void ) id ( ParameterList? ) {Statement*}
	private void parseFMD() throws SyntaxError {
		parseVis();
		parseAccess();
		if(token.kind == TokenKind.VOID) {
			acceptIt();
		}
		else {
			parseType();
		}
		accept(TokenKind.ID);
		if(token.kind == TokenKind.SEMIC) {
			acceptIt();
		}
		else {
			accept(TokenKind.LPAREN);
			if(token.kind != TokenKind.RPAREN) {
				parsePL();
			}
			accept(TokenKind.RPAREN);
			accept(TokenKind.LCURLY);
			while(token.kind != TokenKind.RCURLY) {
				parseStatement();
			}
			accept(TokenKind.RCURLY);
		}
	}
	
	//Visibility ::= ( public | private )?
	private void parseVis() throws SyntaxError {
		switch(token.kind) {
		
		case PUBLIC:
			acceptIt();
			return;
		
		case PRIVATE:
			acceptIt();
			return;
		
		default:
			return;
		}
	}
	
	//Access ::= static ?
	private void parseAccess() throws SyntaxError {
		if(token.kind == TokenKind.STATIC) {
			acceptIt();
		}
		return;
	}
	
	//Type ::= int | boolean | id | ( int | id ) []
	private void parseType() throws SyntaxError {
		switch (token.kind) {
		
		case INT:
			acceptIt();
			arrayCheck();
			return;
		
		case BOOL:
			acceptIt();
			return;
		
		case ID:
			acceptIt();
			arrayCheck();
			return;
		
		default:
			parseError("Invalid Type - " + token.kind);
		}
	}
	
	private void arrayCheck() {
		if(token.kind == TokenKind.LSQUARE) {
			accept(TokenKind.LSQUARE);
			accept(TokenKind.RSQUARE);
		}
	}
	
	//ParameterList ::= Type id ( , Type id )*
	private void parsePL() throws SyntaxError {
		parseType();
		accept(TokenKind.ID);
		while(token.kind == TokenKind.COMMA) {
			acceptIt();
			parseType();
			accept(TokenKind.ID);
		}
	}
	
	//ArgumentList ::= Expression ( , Expression )*
	private void parseAL() throws SyntaxError {
		parseExp();
		while(token.kind == TokenKind.COMMA) {
			acceptIt();
			parseExp();
		}
	}
	
	//Reference ::= id | this | Reference . id 
	//Reference ::= (id | this) (. id)*  
	private void parseRef() throws SyntaxError {
		switch (token.kind) {
		
		case ID:
			acceptIt();
			return;
		
		case THIS:
			acceptIt();
			return;
		
		default:
			parseRef();
			accept(TokenKind.PERIOD);
			accept(TokenKind.ID);
		}
	}
	
	/**
	 * Statement ::=
	 * { Statement* }
	 * | Type id = Expression ;
	 * | Reference = Expression ;
	 * | Reference [ Expression ] = Expression ;
	 * | Reference ( ArgumentList? ) ;
	 * | return Expression? ;
	 * | if ( Expression ) Statement (else Statement)?
	 * | while ( Expression ) Statement
	 * 
	 * LL(1):
	 * S ::= E ;
	 * 
	 */
	private void parseStatement() {
		if(token.kind == TokenKind.LCURLY) {
			acceptIt();
			while(token.kind != TokenKind.RCURLY) {
				parseStatement();
			}
			accept(TokenKind.RCURLY);
		}
		else if(token.kind == TokenKind.INT || token.kind == TokenKind.BOOL) {
			parseType();
			accept(TokenKind.ID);
			accept(TokenKind.EQUALS);
			parseExp();
			accept(TokenKind.SEMIC);
		}
		else if(token.kind == TokenKind.THIS ) {
			parseRef();
		}
		else if(token.kind == TokenKind.ID) {
			acceptIt();
			if(token.kind == TokenKind.ID) {
				acceptIt();
				accept(TokenKind.EQUALS);
				parseExp();
				accept(TokenKind.SEMIC);
			}
			else {
				if(token.kind == TokenKind.PERIOD) {
					while(token.kind == TokenKind.PERIOD) {
						acceptIt();
						accept(TokenKind.ID);
					}
				}
				if (token.kind == TokenKind.EQUALS) {
					acceptIt();
					parseExp();
					accept(TokenKind.SEMIC);
				}
				else if(token.kind == TokenKind.LSQUARE) {
					acceptIt();
					parseExp();
					accept(TokenKind.RSQUARE);
					accept(TokenKind.EQUALS);
					parseExp();
					accept(TokenKind.SEMIC);
				}
				else if(token.kind == TokenKind.LPAREN) {
					acceptIt();
					if(token.kind != TokenKind.RPAREN) {
						parseAL();
					}
					accept(TokenKind.RPAREN);
					accept(TokenKind.SEMIC);
				}
			}
		}
		else if(token.kind == TokenKind.RETURN) {
			acceptIt();
			if(token.kind != TokenKind.SEMIC) {
				parseExp();
			}
			accept(TokenKind.SEMIC);
		}
		else if(token.kind == TokenKind.IF) {
			acceptIt();
			accept(TokenKind.LPAREN);
			parseExp();
			accept(TokenKind.RPAREN);
			parseStatement();
			if(token.kind == TokenKind.ELSE) {
				acceptIt();
				parseStatement();
			}
		}
		else if(token.kind == TokenKind.WHILE) {
			acceptIt();
			accept(TokenKind.LPAREN);
			parseExp();
			accept(TokenKind.RPAREN);
			parseStatement();
		}
	}
	
	/**
	 * Expression ::=
	 * (unop Expression
	 * | ( Expression )
	 * | num | true | false
	 * | new ( id () | int [ Expression ] | id [ Expression ] )
 	 * | Reference
 	 * | Reference [ Expression ]
	 * | Reference ( ArgumentList? ))
	 * 		(binop Expression)*
	 */
	private void parseExp() throws SyntaxError {
		if(token.kind == TokenKind.UNOP || token.kind == TokenKind.MINUS) {
			acceptIt();
			parseExp();
		}
		else if(token.kind == TokenKind.LPAREN) {
			acceptIt();
			parseExp();
			accept(TokenKind.RPAREN);
		}
		else if(token.kind == TokenKind.NUM || token.kind == TokenKind.TRUE || token.kind == TokenKind.FALSE) {
			acceptIt();
		}
		else if(token.kind == TokenKind.NEW) {
			acceptIt();
			if(token.kind == TokenKind.INT) {
				acceptIt();
				accept(TokenKind.LSQUARE);
				parseExp();
				accept(TokenKind.RSQUARE);
			}
			else {
				accept(TokenKind.ID);
				if(token.kind == TokenKind.LPAREN) {
					acceptIt();
					accept(TokenKind.RPAREN);
				}
				else {
					accept(TokenKind.LSQUARE);
					parseExp();
					accept(TokenKind.RSQUARE);
				}
			}
		}
		else {
			parseRef();
			if(token.kind == TokenKind.LSQUARE) {
				acceptIt();
				parseExp();
				accept(TokenKind.RSQUARE);
			}
			else if(token.kind == TokenKind.LPAREN) {
				acceptIt();
				if(token.kind != TokenKind.RPAREN) {
					parseAL();
				}
				accept(TokenKind.RPAREN);
			}
		}
		while (token.kind == TokenKind.BINOP || token.kind == TokenKind.MINUS) {
			acceptIt();
			parseExp();
		}
	}
	
	/**
	 * accept current token and advance to next token
	 */
	private void acceptIt() throws SyntaxError {
		accept(token.kind);
	}
	
	/**
	 * verify that current token in input matches expected token and advance to next token
	 * @param expectedToken
	 * @throws SyntaxError  if match fails
	 */
	private void accept(TokenKind expectedTokenKind) throws SyntaxError {
		if (token.kind == expectedTokenKind) {
			if (trace)
				pTrace();
			token = scanner.scan();
		}
		else
			parseError("expecting '" + expectedTokenKind +
					"' but found '" + token.kind + "'");
	}
	
	/**
	 * report parse error and unwind call stack to start of parse
	 * @param e  string with error detail
	 * @throws SyntaxError
	 */
	private void parseError(String e) throws SyntaxError {
		reporter.reportError("Parse error: " + e);
		throw new SyntaxError();
	}
	
	// show parse stack whenever terminal is  accepted
	private void pTrace() {
		StackTraceElement [] stl = Thread.currentThread().getStackTrace();
		for (int i = stl.length - 1; i > 0 ; i--) {
			if(stl[i].toString().contains("parse"))
				System.out.println(stl[i]);
		}
		System.out.println("accepting: " + token.kind + " (\"" + token.spelling + "\")");
		System.out.println();
	}
}
