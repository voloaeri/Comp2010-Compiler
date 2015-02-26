import java_cup.runtime.*;

%%

%public
%class Lexer

%line
%column

%cup

 /*  Declarations */

%{
  /* Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions. */

  /* To create a new java_cup.runtime.Symbol with information about
       the current token, the token will have no value in this
       case. */
  private Symbol symbol(int type) {
    //System.out.print(type+" ");
        return new Symbol(type, yyline, yycolumn);
    }
    
    /* Also creates a new java_cup.runtime.Symbol with information
       about the current token, but this object has a value. */
    private Symbol symbol(int type, Object value) {
      //System.out.print(type+"("+value+") ");
        return new Symbol(type, yyline, yycolumn, value);
    }

%}

/* -- Macro Declarations -- */

/* Character Types */

digit = [0-9]
nonzero = [1-9]
letter = [a-zA-Z]
/* String types */
alphanum = ({digit}|{letter}|_)

ident = {letter} {alphanum}*

char = '.'
string = \"[^\"]*\"
bool = (T|F)
int = (0|{nonzero} {digit}*)
neg_int = -{nonzero} {digit}*
float = (0|{nonzero} {digit}*)\.{digit}+
neg_float = -{float}
rational_num = {int}_{int}\/{int}
neg_rational_num = -{rational_num}

LineTerminator = \r|\n|\r\n
		       
comment = (#.* {LineTerminator}|\/#(.|{LineTerminator})*#\/)

type = "char"|"bool"|"int"|"float"|"top"|"rat"

WhiteSpace     = {LineTerminator} | [ \t\f]


%%

/* Lexical rules */
<YYINITIAL> {
  {bool} {return symbol(sym.BOOL_LITERAL, new Boolean(yytext()=="T"?true:false));} 

  {comment} { }

  ";" {return symbol(sym.SEMI);}
  ":" {return symbol(sym.COLON);}
  "," {return symbol(sym.COMMA);}
  "::" {return symbol(sym.CONCAT_OP);}

  ":=" {return symbol(sym.ASSIGN_OP);}
  "=" {return symbol(sym.EQ_OP);}
  "!=" {return symbol(sym.NEQ_OP);}

  "+" {return symbol(sym.PLUS_OP);}
  "-" {return symbol(sym.MINUS_OP);}
  "*" {return symbol(sym.MULT_OP);}
  "/" {return symbol(sym.DIV_OP);}
  "^" {return symbol(sym.POW_OP);}

  "&&" {return symbol(sym.AND_OP);}
  "||" {return symbol(sym.OR_OP);}
  "!" {return symbol(sym.OR_OP);}
  "<" {return symbol(sym.LT_OP);}
  "<=" {return symbol(sym.LTE_OP);}
  "=>" {return symbol(sym.IMPL_OP);}
  "." {return symbol(sym.DOT_OP);}
 
  "in" {return symbol(sym.IN_OP);}

  "(" {return symbol(sym.LPAREN);}
  ")" {return symbol(sym.RPAREN);}
  "{" {return symbol(sym.LBRACE);}
  "}" {return symbol(sym.RBRACE);}
  "[" {return symbol(sym.LBRACK);}
  "]" {return symbol(sym.RBRACK);}
  ">" {return symbol(sym.RANGLE);}
  
  "seq" {return symbol(sym.SEQ);}
  "dict" {return symbol(sym.DICT);}

  {type} {return symbol(sym.TYPE);}

  "return" {return symbol(sym.RETURN);}
  "print" {return symbol(sym.PRINT);}
  "read" {return symbol(sym.READ);}

  "if" {return symbol(sym.IF);}
  "then" {return symbol(sym.THEN);}
  "else" {return symbol(sym.ELSE);}
  "fi" {return symbol(sym.FI);}

  "while" {return symbol(sym.WHILE);}
  "forall" {return symbol(sym.FORALL);}
  "do" {return symbol(sym.DO);}
  "od" {return symbol(sym.OD);}

  "tdef"  {return symbol(sym.TDEF);}
  "fdef"  {return symbol(sym.FDEF);}
  "alias"  {return symbol(sym.ALIAS);}
  "len" {return symbol(sym.LEN);}

  "main"  {return symbol(sym.MAIN);}

  {char} {return symbol(sym.CHAR_LITERAL, new Character(yytext().charAt(0)));} 
  {string} {return symbol(sym.STRING_LITERAL, new String(yytext()));}
  {int} {return symbol(sym.INT_LITERAL, new Integer(yytext()));}  
  {neg_int} {return symbol(sym.NEG_INT_LITERAL, new Integer(yytext()));}  
  {float} {return symbol(sym.FLOAT_LITERAL, new Float(yytext()));}
  {neg_float} {return symbol(sym.NEG_FLOAT_LITERAL, new Float(yytext()));} 
  {rational_num} {return symbol(sym.RATIONAL_LITERAL, new String(yytext()));}
  {neg_rational_num} {return symbol(sym.NEG_RATIONAL_LITERAL, new String(yytext()));}
  {ident} {return symbol(sym.IDENT, new String(yytext()));}

  {LineTerminator} {}
  {WhiteSpace} {}

}

/* No token was found for the input so throw an error.  Print out an
   Illegal character message with the illegal character that was found. */
[^]   { throw new Error("Illegal character <"+yytext()+">"); }

