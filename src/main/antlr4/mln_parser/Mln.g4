grammar Mln;

mln
	: domainblock SCIENTIFIC EOF
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
	: IDENTIFIER EQ set
	;
	
set
	: LCURLY list RCURLY
	;
	
list
	: symblist
	| intlist
	| intrange
	;
	
symblist
	: SYMBOL 
	| SYMBOL COMMA symblist
	;
	
intlist
	: INTEGER
	| INTEGER COMMA intlist
	;

intrange
	: INTEGER COMMA ELLIPSIS COMMA INTEGER
	;

COMMENT
	: COMMENTSYM .* NEWLINE
	| COMMENTSYM .* EOF
	;

INTEGER
	: MINUS? DIGIT+
	;

SCIENTIFIC
	: DECIMAL (('e'|'E') DECIMAL)?
	;

DECIMAL
	: MINUS? DIGIT+ (POINT DIGIT+)?
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

AND
	: '^'
	;
	
OR
	: 'v'
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
	
	