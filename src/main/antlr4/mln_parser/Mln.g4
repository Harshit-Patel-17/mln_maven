grammar Mln;

mln
	: domainblock predicateDefBlock EOF
	;
	
domainblock
	: '#domains' domainbody
	;
	
domainbody
	:
	| domain domainbody
	| COMMENT domainbody
	;
	
domain
	: domainName1=SYMBOL EQ LCURLY vals1=symblist RCURLY
	| domainName2=SYMBOL EQ LCURLY vals2=intrange RCURLY
	;
	
predicateDefBlock
	: '#predicates' predicateDefBody
	;
	
predicateDefBody
	:
	| predicateDef predicateDefBody
	| COMMENT predicateDefBody
	;
	
predicateDef
	: predicateName1=SYMBOL LPAREN doms1=symblist RPAREN EQ LCURLY vals1=symblist RCURLY
	| predicateName2=SYMBOL LPAREN doms2=symblist RPAREN EQ LCURLY vals2=intrange RCURLY
	| predicateName3=SYMBOL LPAREN doms3=symblist RPAREN EQ vals3=SYMBOL
	;
	
symblist
	: SYMBOL 
	| INTEGER
	| SYMBOL COMMA symblist
	| INTEGER COMMA symblist
	;

intrange
	: valStart=INTEGER COMMA ELLIPSIS COMMA valEnd=INTEGER
	;

COMMENT
	: COMMENTSYM (.)*? NEWLINE
	| COMMENTSYM (.)*? EOF
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
	
COMMENTSYM
	: '//'
	;
	
NEWLINE
	: '\n'
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
	
	