grammar Mln;

@header {
import iitd.data_analytics.mln.*;
import iitd.data_analytics.mln.mln.Predicate;
import iitd.data_analytics.mln.logic.*;
}

mln
	: domainblock predicateDefBlock formulaBlock EOF
	;
	
domainblock
	: '#domains' domainbody
	;
	
domainbody
	:
	| domainbody domain
	| domainbody COMMENT
	;
	
domain
	: domainName1=SYMBOL EQ LCURLY vals1=symblist RCURLY #domain1
	| domainName2=SYMBOL EQ LCURLY vals2=intrange RCURLY #domain2
	;
	
predicateDefBlock
	: '#predicates' predicateDefBody
	;
	
predicateDefBody
	:
	| predicateDefBody predicateDef
	| predicateDefBody COMMENT
	;
	
predicateDef
	: predicateName1=SYMBOL LPAREN doms1=symblist RPAREN EQ LCURLY vals1=symblist RCURLY #predicateDef1
	| predicateName2=SYMBOL LPAREN doms2=symblist RPAREN EQ LCURLY vals2=intrange RCURLY #predicateDef2
	| predicateName3=SYMBOL LPAREN doms3=symblist RPAREN EQ vals3=SYMBOL #predicateDef3
	;
	
formulaBlock
	: '#formulas' formulaBody
	;
	
formulaBody
	: #formulaBody1
	| formulaBody w=(INTEGER|REAL)'::'formula #formulaBody2
	| formulaBody COMMENT #formulaBody3
	;
	
formula locals[FirstOrderFormula<Predicate> foFormula]
	: p=predicate #formula1
	| '!' p=formula #formula2
	| p=formula '^' q=formula #formula3
	| p=formula 'v' q=formula #formula4
	| p=formula '=>' q=formula #formula5
	| p=formula '<=>' q=formula #formula6
	| LPAREN p=formula RPAREN #formula7
	;
	
predicate locals[Predicate pred]
	: predicateName1=SYMBOL LPAREN terms1=symbvarlist RPAREN EQ val1=(SYMBOL|INTEGER)
	;	
	
symblist
	:
	| SYMBOL 
	| INTEGER
	| SYMBOL COMMA symblist
	| INTEGER COMMA symblist
	;

symbvarlist
	:
	| SYMBOL
	| INTEGER
	| IDENTIFIER
	| SYMBOL COMMA symbvarlist
	| INTEGER COMMA symbvarlist
	| IDENTIFIER COMMA symbvarlist
	;

intrange
	: valStart=INTEGER COMMA ELLIPSIS COMMA valEnd=INTEGER
	;

COMMENT
	: '//' (.)*? '\n'
	| '//' (.)*? EOF
	;

INTEGER
	: MINUS? DIGIT+
	;

REAL
	: MINUS? DIGIT+ (POINT DIGIT+)? (('e'|'E') MINUS? DIGIT+ (POINT DIGIT+)?)?
	;

SYMBOL
	: CAP (UNDSCORE | CAP | SMALL | DIGIT)*
	;

IDENTIFIER
	: (UNDSCORE | SMALL) (UNDSCORE | CAP | SMALL | DIGIT)*
	;

LPAREN
	: '('
	;
	
RPAREN
	: ')'
	;
	
LCURLY
	: '{'
	;
	
RCURLY
	: '}'
	;
	
EQ
	: '='
	;
	
COL
	: ':'
	;
	
UNDSCORE
	: '_'
	;
	
POINT
	: '.'
	;
	
MINUS
	: '-'
	;
	
COMMA
	: ','
	;
	
ELLIPSIS
	: '...'
	;
	
CAP 
	: [A-Z]
	;

SMALL
	: [a-z]
	;
	
DIGIT
	: [0-9]
	;

WS
	: [ \r\n\t]+ -> channel (HIDDEN)
	;
	
	