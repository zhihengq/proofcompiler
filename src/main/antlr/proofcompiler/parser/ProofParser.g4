parser grammar ProofParser;

@header {
    import java.util.List;
    import proofcompiler.ast.Proof;
    import proofcompiler.ast.Declarations;
    import proofcompiler.ast.Line;
    import proofcompiler.ast.Number;
    import proofcompiler.ast.logic.Proposition;
    import proofcompiler.ast.Rule;
}

options { tokenVocab=ProofLexer; }

proof returns [Proof value]
    : decls START proofBody EOF ;


decls returns [Declarations value]
    : (declLine? EOL)* ;

declLine returns [Declarations value]
    : LET ID BE A PROPOSITION   # declLet
    | GIVEN proposition         # declGiven
    ;

proofBody returns [List<Line> value]
    : (proofLine? EOL)* ;

proofLine returns [Line value]
    : number proposition ruleRef ;

number returns [Number value]
    : POSINT (DOT POSINT)* DOT? ;

ruleRef returns [Rule value]
    : LBRACKET ruleName (COLON number (COMMA number)*)? RBRACKET
    ;

ruleName returns [String value]
    : ID+ operator?
    ;

operator : NEG | AND | OR | XOR | IMPLIES | IFF ;

proposition returns [Proposition value]
    : propSum                       # propositionSum
    | propSum IMPLIES propSum       # propositionImpl
    | propSum IFF propSum           # propositionIff
    ;
propSum  returns [Proposition value] : propXor  (OR  propXor )* ;
propXor  returns [Proposition value] : propProd (XOR propProd)* ;
propProd returns [Proposition value] : propAtom (AND propAtom)* ;
propAtom returns [Proposition value]
    : LPAREN proposition RPAREN     # propAtomParen
    | NEG propAtom                  # propAtomNeg
    | ID                            # propAtomID
    ;
