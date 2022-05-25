grammar Hql;     

hql_file
    : ( hql_clause )* EOF
    ;

hql_clause
    : dml_clause
    ;

dml_clause
    : select_statement
    ;
  
// DML
select_statement
    : query_expression end_statement?
    ;

end_statement
    : SEMI
    ;
    
// Expression
expression
    : NULL                                                     #primitive_expression
    | constant                                                 #primitive_expression
    | full_column_name                                         #column_ref_expression
    | '(' expression ')'                                       #bracket_expression
    | expression op=('*' | '/' | '%') expression               #binary_operator_expression
    | op=('+' | '-') expression                                #unary_operator_expression
    | expression op=('+' | '-' | '&' | '^' | '|') expression   #binary_operator_expression
    | expression comparison_operator expression                #binary_operator_expression
    ;

constant_expression
    : NULL
    | constant
    | '(' constant_expression ')'
    ;
    
subquery
    : select_statement
    ;

// https://msdn.microsoft.com/en-us/library/ms173545.aspx
search_condition_list
    : search_condition (',' search_condition)*
    ;

search_condition
    : search_condition_and (or_condition)*
    ;

search_condition_and
    : search_condition_not (and_condition)*
    ;

or_condition
    : OR search_condition_and
    ;
    
and_condition
    : AND search_condition_not
    ;
    
search_condition_not
    : NOT? predicate
    ;
      
predicate
    : lhs_expression rhs_expression
    | lhs_expression rhs_expression
    | lhs_expression rhs_expression
    | lhs_expression rhs_expression
    | '(' search_condition ')'
    ;
 
// Left-hand side expression. 
lhs_expression
    : expression
    ;
    
// Right-hand side expression which also includes the operator
rhs_expression
    : comparison_operator expression
    | NOT? BETWEEN expression AND expression
    | NOT? IN '(' expression_list ')'
    | NOT? (LIKE | ILIKE | REGEX) expression
    ;
         
query_expression
    : (query_specification | '(' query_expression ')')
    ;

query_specification
    : select_clause
      from_clause
      where_clause?
      order_by_clause?
    ;
 
select_clause
    : SELECT select_list
    ;
            
order_by_clause
    : ORDER BY order_by_expression (',' order_by_expression)*
    ;
    
order_by_expression
    : expression (ASC | DESC)?
    ;
   
// https://msdn.microsoft.com/en-us/library/ms176104.aspx
select_list
    : select_list_elem
    ;

// select projection not supported    
select_list_elem
    : '*'
    ;

from_clause
    : (FROM path_source)?
    ;
    
where_clause
    : WHERE where=search_condition?
    ;
    
path_source
    : path_source_item
    | '(' path_source_item ')'
    ;
    
path_source_item
    : path ('.'set_type)? as_path_alias?
    ;

set_type
	: ENTRIES
	| KEYS
	| VALUES
	;
	
as_path_alias
    : AS? path_alias
    ;

path_alias
    : id column_alias?
    | path_hint (',' path_hint)*
    ;

path_hint
    : ID
    ;
   
column_alias_list
    : '(' column_alias (',' column_alias)* ')'
    ;

column_alias
    : id
    | STRING
    ;
    
expression_list
    : expression (',' expression)*
    ;
    
path_name
    : simple_id
    ;
    
path
    : '/'? path_name ('/' path_name)*
    ;
    
full_column_name
    : (path '.')? (KEY | VALUE)
    | (path '.')? (KEY | VALUE)? '.' column_name
    | column_name
    ;

column_name_list
    : column_name (',' column_name)*
    ;

column_name
    : ( id DOT )? id ( column_alias )? | ( path_alias )? id 
    ;

simple_name
    : (schema=id '.')? name=id
    ;
  
fragment FullWidthLetter
    : '\u00c0'..'\u00d6'
    | '\u00d8'..'\u00f6'
    | '\u00f8'..'\u00ff'
    | '\u0100'..'\u1fff'
    | '\u2c00'..'\u2fff'
    | '\u3040'..'\u318f'
    | '\u3300'..'\u337f'
    | '\u3400'..'\u3fff'
    | '\u4e00'..'\u9fff'
    | '\ua000'..'\ud7ff'
    | '\uf900'..'\ufaff'
    | '\uff00'..'\ufff0'
    // | '\u10000'..'\u1F9FF'  //not support four bytes chars
    // | '\u20000'..'\u2FA1F'
    ;
   
 
null_notnull
    : NOT? NULL
    ;
    
default_value
    : NULL
    | constant
    ;

constant
    : STRING // string, datetime or uniqueidentifier
    | BINARY
    | number
    | sign? (REAL | FLOAT)  // float or decimal
    | sign? '$' (DECIMAL | FLOAT)       // money
    ;

number
    : sign? DECIMAL
    ;

sign
    : PLUS
    | MINUS
    ;

id
    : simple_id
    | DOUBLE_QUOTE_ID
    | SQUARE_BRACKET_ID
    ;   
    
simple_id
    : ID
    | ABSOLUTE
    | COUNT
    | COUNT_BIG
    | MAX
    | MIN
    | NUMBER
    | PARTITION
    | PATH
    | STDEV
    | SUM
    | TYPE
    | keywordsCanBeId
    ;
 
// ASC and DESC are left out in the keywordsCanBeId list.
// These keys are not allowed in simple_id.    
keywordsCanBeId
    : AND | AS | BETWEEN | BY
    | DELETE | DISTINCT | DOUBLE
    | ENTRIES | FROM | ILIKE | IN
    | KEY | KEYS | LIKE | NOT | NULL | NVL
    | OR | ORDER | REGEX
    | SET | SELECT
    | VALUE | VALUES | WHERE
    | ABSOLUTE | COUNT | COUNT_BIG | LTRIM 
    | MAX | MIN | NUMBER
    | PARTITION | PATH
    | RTRIM
    | STDEV | SUM
    | TRIM | TYPE
    ;
    
comparison_operator
    : '=' | '>' | '<' | '<' '=' | '>' '=' | '!' '='
    ;

// Lexer

// Basic keywords
AND:                             A N D;
AS:                              A S;
ASC:                             A S C;
BETWEEN:                         B E T W E E N;
BY:                              B Y;
DELETE:                          D E L E T E;
DESC:                            D E S C;
DISTINCT:                        D I S T I N C T;
DOUBLE:                          D O U B L E;
ENTRIES:                         E N T R I E S;
FROM:                            F R O M;
ILIKE:                           I L I K E;
IN:                              I N;
KEY:                             K E Y;
KEYS:                            K E Y S;
LIKE:                            L I K E;
NOT:                             N O T;
NULL:                            N U L L;
NVL:                             N V L;
OR:                              O R;
ORDER:                           O R D E R;
REGEX:                           R E G E X;
SET:                             S E T;
SELECT:                          S E L E C T;
VALUE:                           V A L U E;
VALUES:                          V A L U E S;
WHERE:                           W H E R E;


// Additional keywords (they can be id).
ABSOLUTE:                        A B S O L U T E;
COUNT:                           C O U N T;
COUNT_BIG:                       C O U N T '_' B I G;
LTRIM:                           L T R I M;
MAX:                             M A X;
MIN:                             M I N;
MINUTE:                          M I N U T E;
NUMBER:                          N U M B E R;
PARTITION:                       P A R T I T I O N;
PATH:                            P A T H;
RTRIM:                           R T R I M;
STDEV:                           S T D E V;
SUM:                             S U M;
TRIM:                            T R I M;
TYPE:                            T Y P E;

SPACE:              [ \t\r\n]+    -> skip;
COMMENT:            '/*' .*? '*/' -> channel(HIDDEN);
LINE_COMMENT:       '--' ~[\r\n]* -> channel(HIDDEN);

DOUBLE_QUOTE_ID:    '"' ~'"'+ '"';
SQUARE_BRACKET_ID:  '[' ~']'+ ']';
DECIMAL:             DEC_DIGIT+;
fragment
ALPHA 
   : [a-zA-Z_] ;

fragment
DIGIT 
   : [0-9] ;

ID
   : ALPHA (ALPHA|DIGIT)*
   ;
   
STRING:              N? '\'' (~'\'' | '\'\'')* '\'';
BINARY:              '0' X HEX_DIGIT*;
FLOAT:               DEC_DOT_DEC;
REAL:                DEC_DOT_DEC (E [+-]? DEC_DIGIT+)?;

EQUAL:               '=';

GREATER:             '>';
LESS:                '<';
EXCLAMATION:         '!';

PLUS_ASSIGN:         '+=';
MINUS_ASSIGN:        '-=';
MULT_ASSIGN:         '*=';
DIV_ASSIGN:          '/=';
MOD_ASSIGN:          '%=';
AND_ASSIGN:          '&=';
XOR_ASSIGN:          '^=';
OR_ASSIGN:           '|=';

DOT:                 '.';
UNDERLINE:           '_';
AT:                  '@';
SHARP:               '#';
DOLLAR:              '$';
LR_BRACKET:          '(';
RR_BRACKET:          ')';
COMMA:               ',';
SEMI:                ';';
COLON:               ':';
STAR:                '*';
DIVIDE:              '/';
MODULE:              '%';
PLUS:                '+';
MINUS:               '-';
BIT_NOT:             '~';
BIT_OR:              '|';
BIT_AND:             '&';
BIT_XOR:             '^';

fragment LETTER:       [a-zA-Z_];
fragment DEC_DOT_DEC:  (DEC_DIGIT+ '.' DEC_DIGIT+ |  DEC_DIGIT+ '.' | '.' DEC_DIGIT+);
fragment HEX_DIGIT:    [0-9A-Fa-f];
fragment DEC_DIGIT:    [0-9];

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];