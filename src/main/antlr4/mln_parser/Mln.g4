grammar Mln;

mln
	: domainblock EOF
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
	: domainName=IDENTIFIER EQ set
	;
	
set
	: LCURLY list RCURLY
	;
	
list
	: symblist
	| intrange
	;
	
symblist
	: val=SYMBOL 
	| val=INTEGER
	| val=SYMBOL COMMA symblist
	| val=INTEGER COMMA symblist
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
	
	