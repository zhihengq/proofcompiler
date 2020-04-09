lexer grammar ProofLexer;

// Default mode: declarations
EOL : '\n' ;
WS : [ \t\r]+ -> skip ;

COMMENT_SINGLE : ('//' | '#') -> skip, pushMode(comment_singleline) ;
COMMENT_MULTI  : '/*'         -> skip, pushMode(comment_multiline)  ;

START : 'proof.' -> mode(proof_body) ;
STARTEQUIV : 'equivalence.' -> mode(proof_body) ;

GIVEN         : 'given' | 'Given' ;
LET           : 'let'   | 'Let'   ;
BE            : 'be'              ;
A             : 'a'               ;
PROPOSITION   : 'proposition'     ;

ID       : [a-zA-Z_'] [a-zA-Z0-9_']* ;
LPAREN   : '(' ;
RPAREN   : ')' ;

NEG     : '\\neg'                    | '¬' ;
AND     : '\\land'       | '\\wedge' | '∧' ;
OR      : '\\lor'        | '\\vee'   | '∨' ;
XOR     : '\\oplus'                  | '⊕' ;
IMPLIES : '\\rightarrow' | '\\to'    | '→' ;
IFF     : '\\leftrightarrow'         | '↔' ;
EQUIV   : '\\equiv'      | '='       | '≡' ;


// Proof body
mode proof_body;

PB_EOL            : EOL            -> type(EOL)                          ;
PB_WS             : WS             -> skip                               ;
PB_COMMENT_SINGLE : COMMENT_SINGLE -> skip, pushMode(comment_singleline) ;
PB_COMMENT_MULTI  : COMMENT_MULTI  -> skip, pushMode(comment_multiline)  ;
PB_ID             : ID             -> type(ID)                           ;
PB_LPAREN         : LPAREN         -> type(LPAREN)                       ;
PB_RPAREN         : RPAREN         -> type(RPAREN)                       ;
PB_NEG            : NEG            -> type(NEG)                          ;
PB_AND            : AND            -> type(AND)                          ;
PB_OR             : OR             -> type(OR)                           ;
PB_XOR            : XOR            -> type(XOR)                          ;
PB_IMPLIES        : IMPLIES        -> type(IMPLIES)                      ;
PB_IFF            : IFF            -> type(IFF)                          ;
PB_EQUIV          : EQUIV          -> type(EQUIV)                        ;

DOT      : '.' ;
COMMA    : ',' ;
COLON    : ':' ;
POSINT   : [1-9] [0-9]* ;
LBRACKET : '[' ;
RBRACKET : ']' ;


// Single-line comments
mode comment_singleline;
SLC_EOL : EOL -> skip, popMode ;
SLC_ANY : .+? -> skip ;


// Multi-line comments
mode comment_multiline;
MLC_END : '*/' -> skip, popMode ;
MLC_ANY : .+? -> skip ;
