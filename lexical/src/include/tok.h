#ifndef TOK_H
#define TOK_H

enum {
	AUTO=256,
	BREAK,
	CASE,
	CHAR,
	CONST,
	CONTINUE,
	DEFAULT,
	DO,
	DOUBLE,
	ELSE,
	ABS,
	NEW,
	ASSERT,
	BYTE,
	//IMPORT,
	PUBLIC,
	THROWS,
	INSOF,
	TRANS,
	AS,
	BASE,
	CHECKED,
	DECIMAL,
	DELEGATE,
	EVENT,
	EXPLICIT,
	FALS,
	FIXED,
	FEACH,
	IMPLICIT,
	INTERNAL,
	LOCK,
	NSPA,
	NUL,
	OBJECT,
	OPRT,
	OVRD,
	PARAMS,
	REDY,
	REF,
	SBYTE,
	SEALED,
	STALLC,
	STRING,
	TRU,
	TYPEOF,
	UINT,
	ULONG,
	UCEKD,
	USFE,
	USHORT,
	USING,
	VIR,
	CATCH,
	EXTENDS,
	TRY,
	FINAL,
	INTER,
	CLASS,
	FINALLY,
	STRICT,
	NATIVE,
	SUPER,
	IMP,
	PROT,
	THROW,
	PACK,
	SYN,
	BOOLEAN,
	PRI,
	THIS,
	ENUM,
	EXTERN,
	FLOAT,
	FOR,
	GOTO,
	IF,
	INLINE,
	INT,
	LONG,
	REGISTER,
	RESTRICT,
	RETURN,
	SHORT,
	SIGNED,
	SIZEOF,
	STATIC,
	STRUCT,
	SWITCH,
	TYPEDEF,
	UNION,
	UNSIGNED,
	VOID,
	VOLATILE,
	WHILE,
	ALIGNAS,
	ALIGNOF,
	ATOMIC,
	BOOL,
	COMPLEX,
	GENERIC,
	IMAGINARY,
	NORETURN,
	STATIC_ASSERT,
	THREAD_LOCAL,
	FUNC_NAME,
	I_CONSTANT,	
	F_CONSTANT,
	STRING_LITERAL,
	ELLIPSIS,
	RIGHT_ASSIGN,
	LEFT_ASSIGN,
	ADD_ASSIGN,
	SUB_ASSIGN,
	MUL_ASSIGN,
	DIV_ASSIGN,
	MOD_ASSIGN,
	AND_ASSIGN,
	XOR_ASSIGN,
	OR_ASSIGN,
	RIGHT_OP,
	LEFT_OP,
	INC_OP,
	DEC_OP,
	PTR_OP,
	AND_OP,
	OR_OP,
	LE_OP,
	GE_OP,
	EQ_OP,
	NE_OP,
	TYPEDEF_NAME,
	ENUMERATION_CONSTANT,
	IDENTIFIER
};


//声明语义值如下
extern char* var_val;
extern int yylval;

#endif