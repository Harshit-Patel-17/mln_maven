grammar Database;

@header {

}

database
	: databaseItems EOF
	;
	
databaseItems
	:
	| databaseItem databaseItems
	| COMMENT databaseItems
	;
	
databaseItem
	: predicateName=SYMBOL LPAREN vals=symblist RPAREN EQ val=(SYMBOL|INTEGER) #databaseItem1
	;
	
symblist
	: SYMBOL 
	| INTEGER
	| '*'
	| SYMBOL COMMA symblist
	| INTEGER COMMA symblist
	| '*' COMMA symblist
	;
	
COMMENT
	: '//' (.)*? '\n'
	| '//' (.)*? EOF
	;

INTEGER
	: MINUS? DIGIT+
	;

SYMBOL
	: CAP (UNDSCORE | CAP | SMALL | DIGIT)*
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
	