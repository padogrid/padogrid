// Generated from /Users/dpark/Work/git/Hazelcast/hazelcast-addon/src/main/resources/Hql.g4 by ANTLR 4.7.2
package org.hazelcast.addon.hql.internal.antlr4.generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class HqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, AS=2, ASC=3, BETWEEN=4, BY=5, DELETE=6, DESC=7, DISTINCT=8, DOUBLE=9, 
		ENTRIES=10, FROM=11, ILIKE=12, IN=13, KEY=14, KEYS=15, LIKE=16, NOT=17, 
		NULL=18, NVL=19, OR=20, ORDER=21, REGEX=22, SET=23, SELECT=24, VALUE=25, 
		VALUES=26, WHERE=27, ABSOLUTE=28, COUNT=29, COUNT_BIG=30, LTRIM=31, MAX=32, 
		MIN=33, MINUTE=34, NUMBER=35, PARTITION=36, PATH=37, RTRIM=38, STDEV=39, 
		SUM=40, TRIM=41, TYPE=42, SPACE=43, COMMENT=44, LINE_COMMENT=45, DOUBLE_QUOTE_ID=46, 
		SQUARE_BRACKET_ID=47, DECIMAL=48, ID=49, STRING=50, BINARY=51, FLOAT=52, 
		REAL=53, EQUAL=54, GREATER=55, LESS=56, EXCLAMATION=57, PLUS_ASSIGN=58, 
		MINUS_ASSIGN=59, MULT_ASSIGN=60, DIV_ASSIGN=61, MOD_ASSIGN=62, AND_ASSIGN=63, 
		XOR_ASSIGN=64, OR_ASSIGN=65, DOT=66, UNDERLINE=67, AT=68, SHARP=69, DOLLAR=70, 
		LR_BRACKET=71, RR_BRACKET=72, COMMA=73, SEMI=74, COLON=75, STAR=76, DIVIDE=77, 
		MODULE=78, PLUS=79, MINUS=80, BIT_NOT=81, BIT_OR=82, BIT_AND=83, BIT_XOR=84;
	public static final int
		RULE_hql_file = 0, RULE_hql_clause = 1, RULE_dml_clause = 2, RULE_select_statement = 3, 
		RULE_end_statement = 4, RULE_expression = 5, RULE_constant_expression = 6, 
		RULE_subquery = 7, RULE_search_condition_list = 8, RULE_search_condition = 9, 
		RULE_search_condition_and = 10, RULE_or_condition = 11, RULE_and_condition = 12, 
		RULE_search_condition_not = 13, RULE_predicate = 14, RULE_lhs_expression = 15, 
		RULE_rhs_expression = 16, RULE_query_expression = 17, RULE_query_specification = 18, 
		RULE_select_clause = 19, RULE_order_by_clause = 20, RULE_order_by_expression = 21, 
		RULE_select_list = 22, RULE_select_list_elem = 23, RULE_from_clause = 24, 
		RULE_where_clause = 25, RULE_path_source = 26, RULE_path_source_item = 27, 
		RULE_set_type = 28, RULE_as_path_alias = 29, RULE_path_alias = 30, RULE_path_hint = 31, 
		RULE_column_alias_list = 32, RULE_column_alias = 33, RULE_expression_list = 34, 
		RULE_path_name = 35, RULE_path = 36, RULE_full_column_name = 37, RULE_column_name_list = 38, 
		RULE_column_name = 39, RULE_simple_name = 40, RULE_null_notnull = 41, 
		RULE_default_value = 42, RULE_constant = 43, RULE_number = 44, RULE_sign = 45, 
		RULE_id = 46, RULE_simple_id = 47, RULE_keywordsCanBeId = 48, RULE_comparison_operator = 49;
	private static String[] makeRuleNames() {
		return new String[] {
			"hql_file", "hql_clause", "dml_clause", "select_statement", "end_statement", 
			"expression", "constant_expression", "subquery", "search_condition_list", 
			"search_condition", "search_condition_and", "or_condition", "and_condition", 
			"search_condition_not", "predicate", "lhs_expression", "rhs_expression", 
			"query_expression", "query_specification", "select_clause", "order_by_clause", 
			"order_by_expression", "select_list", "select_list_elem", "from_clause", 
			"where_clause", "path_source", "path_source_item", "set_type", "as_path_alias", 
			"path_alias", "path_hint", "column_alias_list", "column_alias", "expression_list", 
			"path_name", "path", "full_column_name", "column_name_list", "column_name", 
			"simple_name", "null_notnull", "default_value", "constant", "number", 
			"sign", "id", "simple_id", "keywordsCanBeId", "comparison_operator"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "'='", "'>'", "'<'", "'!'", "'+='", 
			"'-='", "'*='", "'/='", "'%='", "'&='", "'^='", "'|='", "'.'", "'_'", 
			"'@'", "'#'", "'$'", "'('", "')'", "','", "';'", "':'", "'*'", "'/'", 
			"'%'", "'+'", "'-'", "'~'", "'|'", "'&'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "AND", "AS", "ASC", "BETWEEN", "BY", "DELETE", "DESC", "DISTINCT", 
			"DOUBLE", "ENTRIES", "FROM", "ILIKE", "IN", "KEY", "KEYS", "LIKE", "NOT", 
			"NULL", "NVL", "OR", "ORDER", "REGEX", "SET", "SELECT", "VALUE", "VALUES", 
			"WHERE", "ABSOLUTE", "COUNT", "COUNT_BIG", "LTRIM", "MAX", "MIN", "MINUTE", 
			"NUMBER", "PARTITION", "PATH", "RTRIM", "STDEV", "SUM", "TRIM", "TYPE", 
			"SPACE", "COMMENT", "LINE_COMMENT", "DOUBLE_QUOTE_ID", "SQUARE_BRACKET_ID", 
			"DECIMAL", "ID", "STRING", "BINARY", "FLOAT", "REAL", "EQUAL", "GREATER", 
			"LESS", "EXCLAMATION", "PLUS_ASSIGN", "MINUS_ASSIGN", "MULT_ASSIGN", 
			"DIV_ASSIGN", "MOD_ASSIGN", "AND_ASSIGN", "XOR_ASSIGN", "OR_ASSIGN", 
			"DOT", "UNDERLINE", "AT", "SHARP", "DOLLAR", "LR_BRACKET", "RR_BRACKET", 
			"COMMA", "SEMI", "COLON", "STAR", "DIVIDE", "MODULE", "PLUS", "MINUS", 
			"BIT_NOT", "BIT_OR", "BIT_AND", "BIT_XOR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Hql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public HqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Hql_fileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(HqlParser.EOF, 0); }
		public List<Hql_clauseContext> hql_clause() {
			return getRuleContexts(Hql_clauseContext.class);
		}
		public Hql_clauseContext hql_clause(int i) {
			return getRuleContext(Hql_clauseContext.class,i);
		}
		public Hql_fileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hql_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterHql_file(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitHql_file(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitHql_file(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Hql_fileContext hql_file() throws RecognitionException {
		Hql_fileContext _localctx = new Hql_fileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_hql_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SELECT || _la==LR_BRACKET) {
				{
				{
				setState(100);
				hql_clause();
				}
				}
				setState(105);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(106);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Hql_clauseContext extends ParserRuleContext {
		public Dml_clauseContext dml_clause() {
			return getRuleContext(Dml_clauseContext.class,0);
		}
		public Hql_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hql_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterHql_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitHql_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitHql_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Hql_clauseContext hql_clause() throws RecognitionException {
		Hql_clauseContext _localctx = new Hql_clauseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_hql_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			dml_clause();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Dml_clauseContext extends ParserRuleContext {
		public Select_statementContext select_statement() {
			return getRuleContext(Select_statementContext.class,0);
		}
		public Dml_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dml_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDml_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDml_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitDml_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dml_clauseContext dml_clause() throws RecognitionException {
		Dml_clauseContext _localctx = new Dml_clauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_dml_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			select_statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_statementContext extends ParserRuleContext {
		public Query_expressionContext query_expression() {
			return getRuleContext(Query_expressionContext.class,0);
		}
		public End_statementContext end_statement() {
			return getRuleContext(End_statementContext.class,0);
		}
		public Select_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelect_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelect_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSelect_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_statementContext select_statement() throws RecognitionException {
		Select_statementContext _localctx = new Select_statementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_select_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			query_expression();
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(113);
				end_statement();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class End_statementContext extends ParserRuleContext {
		public TerminalNode SEMI() { return getToken(HqlParser.SEMI, 0); }
		public End_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_end_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterEnd_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitEnd_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitEnd_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final End_statementContext end_statement() throws RecognitionException {
		End_statementContext _localctx = new End_statementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_end_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Binary_operator_expressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode STAR() { return getToken(HqlParser.STAR, 0); }
		public TerminalNode DIVIDE() { return getToken(HqlParser.DIVIDE, 0); }
		public TerminalNode MODULE() { return getToken(HqlParser.MODULE, 0); }
		public TerminalNode PLUS() { return getToken(HqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(HqlParser.MINUS, 0); }
		public TerminalNode BIT_AND() { return getToken(HqlParser.BIT_AND, 0); }
		public TerminalNode BIT_XOR() { return getToken(HqlParser.BIT_XOR, 0); }
		public TerminalNode BIT_OR() { return getToken(HqlParser.BIT_OR, 0); }
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public Binary_operator_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterBinary_operator_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitBinary_operator_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitBinary_operator_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Primitive_expressionContext extends ExpressionContext {
		public TerminalNode NULL() { return getToken(HqlParser.NULL, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public Primitive_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPrimitive_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPrimitive_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPrimitive_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Bracket_expressionContext extends ExpressionContext {
		public TerminalNode LR_BRACKET() { return getToken(HqlParser.LR_BRACKET, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(HqlParser.RR_BRACKET, 0); }
		public Bracket_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterBracket_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitBracket_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitBracket_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Unary_operator_expressionContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode PLUS() { return getToken(HqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(HqlParser.MINUS, 0); }
		public Unary_operator_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterUnary_operator_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitUnary_operator_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitUnary_operator_expression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Column_ref_expressionContext extends ExpressionContext {
		public Full_column_nameContext full_column_name() {
			return getRuleContext(Full_column_nameContext.class,0);
		}
		public Column_ref_expressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterColumn_ref_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitColumn_ref_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitColumn_ref_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				_localctx = new Primitive_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(119);
				match(NULL);
				}
				break;
			case 2:
				{
				_localctx = new Primitive_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(120);
				constant();
				}
				break;
			case 3:
				{
				_localctx = new Column_ref_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(121);
				full_column_name();
				}
				break;
			case 4:
				{
				_localctx = new Bracket_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(122);
				match(LR_BRACKET);
				setState(123);
				expression(0);
				setState(124);
				match(RR_BRACKET);
				}
				break;
			case 5:
				{
				_localctx = new Unary_operator_expressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(126);
				((Unary_operator_expressionContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
					((Unary_operator_expressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(127);
				expression(3);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(142);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(140);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
					case 1:
						{
						_localctx = new Binary_operator_expressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(130);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(131);
						((Binary_operator_expressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76)) & ((1L << (STAR - 76)) | (1L << (DIVIDE - 76)) | (1L << (MODULE - 76)))) != 0)) ) {
							((Binary_operator_expressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(132);
						expression(5);
						}
						break;
					case 2:
						{
						_localctx = new Binary_operator_expressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(133);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(134);
						((Binary_operator_expressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & ((1L << (PLUS - 79)) | (1L << (MINUS - 79)) | (1L << (BIT_OR - 79)) | (1L << (BIT_AND - 79)) | (1L << (BIT_XOR - 79)))) != 0)) ) {
							((Binary_operator_expressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(135);
						expression(3);
						}
						break;
					case 3:
						{
						_localctx = new Binary_operator_expressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(136);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(137);
						comparison_operator();
						setState(138);
						expression(2);
						}
						break;
					}
					} 
				}
				setState(144);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Constant_expressionContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(HqlParser.NULL, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(HqlParser.LR_BRACKET, 0); }
		public Constant_expressionContext constant_expression() {
			return getRuleContext(Constant_expressionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(HqlParser.RR_BRACKET, 0); }
		public Constant_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterConstant_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitConstant_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitConstant_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constant_expressionContext constant_expression() throws RecognitionException {
		Constant_expressionContext _localctx = new Constant_expressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_constant_expression);
		try {
			setState(151);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				match(NULL);
				}
				break;
			case DECIMAL:
			case STRING:
			case BINARY:
			case FLOAT:
			case REAL:
			case DOLLAR:
			case PLUS:
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
				constant();
				}
				break;
			case LR_BRACKET:
				enterOuterAlt(_localctx, 3);
				{
				setState(147);
				match(LR_BRACKET);
				setState(148);
				constant_expression();
				setState(149);
				match(RR_BRACKET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubqueryContext extends ParserRuleContext {
		public Select_statementContext select_statement() {
			return getRuleContext(Select_statementContext.class,0);
		}
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSubquery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			select_statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_condition_listContext extends ParserRuleContext {
		public List<Search_conditionContext> search_condition() {
			return getRuleContexts(Search_conditionContext.class);
		}
		public Search_conditionContext search_condition(int i) {
			return getRuleContext(Search_conditionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HqlParser.COMMA, i);
		}
		public Search_condition_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearch_condition_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearch_condition_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSearch_condition_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_condition_listContext search_condition_list() throws RecognitionException {
		Search_condition_listContext _localctx = new Search_condition_listContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_search_condition_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			search_condition();
			setState(160);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(156);
				match(COMMA);
				setState(157);
				search_condition();
				}
				}
				setState(162);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_conditionContext extends ParserRuleContext {
		public Search_condition_andContext search_condition_and() {
			return getRuleContext(Search_condition_andContext.class,0);
		}
		public List<Or_conditionContext> or_condition() {
			return getRuleContexts(Or_conditionContext.class);
		}
		public Or_conditionContext or_condition(int i) {
			return getRuleContext(Or_conditionContext.class,i);
		}
		public Search_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearch_condition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearch_condition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSearch_condition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_conditionContext search_condition() throws RecognitionException {
		Search_conditionContext _localctx = new Search_conditionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_search_condition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			search_condition_and();
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(164);
				or_condition();
				}
				}
				setState(169);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_condition_andContext extends ParserRuleContext {
		public Search_condition_notContext search_condition_not() {
			return getRuleContext(Search_condition_notContext.class,0);
		}
		public List<And_conditionContext> and_condition() {
			return getRuleContexts(And_conditionContext.class);
		}
		public And_conditionContext and_condition(int i) {
			return getRuleContext(And_conditionContext.class,i);
		}
		public Search_condition_andContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearch_condition_and(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearch_condition_and(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSearch_condition_and(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_condition_andContext search_condition_and() throws RecognitionException {
		Search_condition_andContext _localctx = new Search_condition_andContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_search_condition_and);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			search_condition_not();
			setState(174);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(171);
				and_condition();
				}
				}
				setState(176);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Or_conditionContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(HqlParser.OR, 0); }
		public Search_condition_andContext search_condition_and() {
			return getRuleContext(Search_condition_andContext.class,0);
		}
		public Or_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOr_condition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOr_condition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitOr_condition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Or_conditionContext or_condition() throws RecognitionException {
		Or_conditionContext _localctx = new Or_conditionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_or_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(177);
			match(OR);
			setState(178);
			search_condition_and();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class And_conditionContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(HqlParser.AND, 0); }
		public Search_condition_notContext search_condition_not() {
			return getRuleContext(Search_condition_notContext.class,0);
		}
		public And_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAnd_condition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAnd_condition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitAnd_condition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final And_conditionContext and_condition() throws RecognitionException {
		And_conditionContext _localctx = new And_conditionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_and_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(180);
			match(AND);
			setState(181);
			search_condition_not();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Search_condition_notContext extends ParserRuleContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public Search_condition_notContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearch_condition_not(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearch_condition_not(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSearch_condition_not(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_condition_notContext search_condition_not() throws RecognitionException {
		Search_condition_notContext _localctx = new Search_condition_notContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_search_condition_not);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(183);
				match(NOT);
				}
				break;
			}
			setState(186);
			predicate();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public Lhs_expressionContext lhs_expression() {
			return getRuleContext(Lhs_expressionContext.class,0);
		}
		public Rhs_expressionContext rhs_expression() {
			return getRuleContext(Rhs_expressionContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(HqlParser.LR_BRACKET, 0); }
		public Search_conditionContext search_condition() {
			return getRuleContext(Search_conditionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(HqlParser.RR_BRACKET, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_predicate);
		try {
			setState(204);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(188);
				lhs_expression();
				setState(189);
				rhs_expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(191);
				lhs_expression();
				setState(192);
				rhs_expression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(194);
				lhs_expression();
				setState(195);
				rhs_expression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(197);
				lhs_expression();
				setState(198);
				rhs_expression();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(200);
				match(LR_BRACKET);
				setState(201);
				search_condition();
				setState(202);
				match(RR_BRACKET);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Lhs_expressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Lhs_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lhs_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterLhs_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitLhs_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitLhs_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Lhs_expressionContext lhs_expression() throws RecognitionException {
		Lhs_expressionContext _localctx = new Lhs_expressionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_lhs_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Rhs_expressionContext extends ParserRuleContext {
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(HqlParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(HqlParser.AND, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public TerminalNode IN() { return getToken(HqlParser.IN, 0); }
		public TerminalNode LR_BRACKET() { return getToken(HqlParser.LR_BRACKET, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(HqlParser.RR_BRACKET, 0); }
		public TerminalNode LIKE() { return getToken(HqlParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(HqlParser.ILIKE, 0); }
		public TerminalNode REGEX() { return getToken(HqlParser.REGEX, 0); }
		public Rhs_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rhs_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterRhs_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitRhs_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitRhs_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Rhs_expressionContext rhs_expression() throws RecognitionException {
		Rhs_expressionContext _localctx = new Rhs_expressionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_rhs_expression);
		int _la;
		try {
			setState(232);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(208);
				comparison_operator();
				setState(209);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(212);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(211);
					match(NOT);
					}
				}

				setState(214);
				match(BETWEEN);
				setState(215);
				expression(0);
				setState(216);
				match(AND);
				setState(217);
				expression(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(219);
					match(NOT);
					}
				}

				setState(222);
				match(IN);
				setState(223);
				match(LR_BRACKET);
				setState(224);
				expression_list();
				setState(225);
				match(RR_BRACKET);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(228);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(227);
					match(NOT);
					}
				}

				setState(230);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ILIKE) | (1L << LIKE) | (1L << REGEX))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(231);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Query_expressionContext extends ParserRuleContext {
		public Query_specificationContext query_specification() {
			return getRuleContext(Query_specificationContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(HqlParser.LR_BRACKET, 0); }
		public Query_expressionContext query_expression() {
			return getRuleContext(Query_expressionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(HqlParser.RR_BRACKET, 0); }
		public Query_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterQuery_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitQuery_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitQuery_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Query_expressionContext query_expression() throws RecognitionException {
		Query_expressionContext _localctx = new Query_expressionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_query_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				{
				setState(234);
				query_specification();
				}
				break;
			case LR_BRACKET:
				{
				setState(235);
				match(LR_BRACKET);
				setState(236);
				query_expression();
				setState(237);
				match(RR_BRACKET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Query_specificationContext extends ParserRuleContext {
		public Select_clauseContext select_clause() {
			return getRuleContext(Select_clauseContext.class,0);
		}
		public From_clauseContext from_clause() {
			return getRuleContext(From_clauseContext.class,0);
		}
		public Where_clauseContext where_clause() {
			return getRuleContext(Where_clauseContext.class,0);
		}
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Query_specificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query_specification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterQuery_specification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitQuery_specification(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitQuery_specification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Query_specificationContext query_specification() throws RecognitionException {
		Query_specificationContext _localctx = new Query_specificationContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_query_specification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(241);
			select_clause();
			setState(242);
			from_clause();
			setState(244);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(243);
				where_clause();
				}
			}

			setState(247);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(246);
				order_by_clause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_clauseContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(HqlParser.SELECT, 0); }
		public Select_listContext select_list() {
			return getRuleContext(Select_listContext.class,0);
		}
		public Select_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelect_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelect_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSelect_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_clauseContext select_clause() throws RecognitionException {
		Select_clauseContext _localctx = new Select_clauseContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_select_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(249);
			match(SELECT);
			setState(250);
			select_list();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Order_by_clauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(HqlParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public List<Order_by_expressionContext> order_by_expression() {
			return getRuleContexts(Order_by_expressionContext.class);
		}
		public Order_by_expressionContext order_by_expression(int i) {
			return getRuleContext(Order_by_expressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HqlParser.COMMA, i);
		}
		public Order_by_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order_by_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOrder_by_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOrder_by_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitOrder_by_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Order_by_clauseContext order_by_clause() throws RecognitionException {
		Order_by_clauseContext _localctx = new Order_by_clauseContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_order_by_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			match(ORDER);
			setState(253);
			match(BY);
			setState(254);
			order_by_expression();
			setState(259);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(255);
				match(COMMA);
				setState(256);
				order_by_expression();
				}
				}
				setState(261);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Order_by_expressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ASC() { return getToken(HqlParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(HqlParser.DESC, 0); }
		public Order_by_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order_by_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOrder_by_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOrder_by_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitOrder_by_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Order_by_expressionContext order_by_expression() throws RecognitionException {
		Order_by_expressionContext _localctx = new Order_by_expressionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_order_by_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(262);
			expression(0);
			setState(264);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(263);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_listContext extends ParserRuleContext {
		public Select_list_elemContext select_list_elem() {
			return getRuleContext(Select_list_elemContext.class,0);
		}
		public Select_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelect_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelect_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSelect_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_listContext select_list() throws RecognitionException {
		Select_listContext _localctx = new Select_listContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_select_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			select_list_elem();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Select_list_elemContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(HqlParser.STAR, 0); }
		public Select_list_elemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_list_elem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelect_list_elem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelect_list_elem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSelect_list_elem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_list_elemContext select_list_elem() throws RecognitionException {
		Select_list_elemContext _localctx = new Select_list_elemContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_select_list_elem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
			match(STAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class From_clauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public Path_sourceContext path_source() {
			return getRuleContext(Path_sourceContext.class,0);
		}
		public From_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_from_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFrom_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFrom_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitFrom_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final From_clauseContext from_clause() throws RecognitionException {
		From_clauseContext _localctx = new From_clauseContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_from_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(272);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(270);
				match(FROM);
				setState(271);
				path_source();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Where_clauseContext extends ParserRuleContext {
		public Search_conditionContext where;
		public TerminalNode WHERE() { return getToken(HqlParser.WHERE, 0); }
		public Search_conditionContext search_condition() {
			return getRuleContext(Search_conditionContext.class,0);
		}
		public Where_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterWhere_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitWhere_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitWhere_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Where_clauseContext where_clause() throws RecognitionException {
		Where_clauseContext _localctx = new Where_clauseContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_where_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			match(WHERE);
			setState(276);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(275);
				((Where_clauseContext)_localctx).where = search_condition();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_sourceContext extends ParserRuleContext {
		public Path_source_itemContext path_source_item() {
			return getRuleContext(Path_source_itemContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(HqlParser.LR_BRACKET, 0); }
		public TerminalNode RR_BRACKET() { return getToken(HqlParser.RR_BRACKET, 0); }
		public Path_sourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_source; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPath_source(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPath_source(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPath_source(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_sourceContext path_source() throws RecognitionException {
		Path_sourceContext _localctx = new Path_sourceContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_path_source);
		try {
			setState(283);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AND:
			case AS:
			case BETWEEN:
			case BY:
			case DELETE:
			case DISTINCT:
			case DOUBLE:
			case ENTRIES:
			case FROM:
			case ILIKE:
			case IN:
			case KEY:
			case KEYS:
			case LIKE:
			case NOT:
			case NULL:
			case NVL:
			case OR:
			case ORDER:
			case REGEX:
			case SET:
			case SELECT:
			case VALUE:
			case VALUES:
			case WHERE:
			case ABSOLUTE:
			case COUNT:
			case COUNT_BIG:
			case LTRIM:
			case MAX:
			case MIN:
			case NUMBER:
			case PARTITION:
			case PATH:
			case RTRIM:
			case STDEV:
			case SUM:
			case TRIM:
			case TYPE:
			case ID:
			case DIVIDE:
				enterOuterAlt(_localctx, 1);
				{
				setState(278);
				path_source_item();
				}
				break;
			case LR_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(279);
				match(LR_BRACKET);
				setState(280);
				path_source_item();
				setState(281);
				match(RR_BRACKET);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_source_itemContext extends ParserRuleContext {
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public TerminalNode DOT() { return getToken(HqlParser.DOT, 0); }
		public Set_typeContext set_type() {
			return getRuleContext(Set_typeContext.class,0);
		}
		public As_path_aliasContext as_path_alias() {
			return getRuleContext(As_path_aliasContext.class,0);
		}
		public Path_source_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_source_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPath_source_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPath_source_item(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPath_source_item(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_source_itemContext path_source_item() throws RecognitionException {
		Path_source_itemContext _localctx = new Path_source_itemContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_path_source_item);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(285);
			path();
			setState(288);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT) {
				{
				setState(286);
				match(DOT);
				setState(287);
				set_type();
				}
			}

			setState(291);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				{
				setState(290);
				as_path_alias();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Set_typeContext extends ParserRuleContext {
		public TerminalNode ENTRIES() { return getToken(HqlParser.ENTRIES, 0); }
		public TerminalNode KEYS() { return getToken(HqlParser.KEYS, 0); }
		public TerminalNode VALUES() { return getToken(HqlParser.VALUES, 0); }
		public Set_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSet_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSet_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSet_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Set_typeContext set_type() throws RecognitionException {
		Set_typeContext _localctx = new Set_typeContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_set_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ENTRIES) | (1L << KEYS) | (1L << VALUES))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class As_path_aliasContext extends ParserRuleContext {
		public Path_aliasContext path_alias() {
			return getRuleContext(Path_aliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public As_path_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_as_path_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAs_path_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAs_path_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitAs_path_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final As_path_aliasContext as_path_alias() throws RecognitionException {
		As_path_aliasContext _localctx = new As_path_aliasContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_as_path_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(296);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				setState(295);
				match(AS);
				}
				break;
			}
			setState(298);
			path_alias();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_aliasContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Column_aliasContext column_alias() {
			return getRuleContext(Column_aliasContext.class,0);
		}
		public List<Path_hintContext> path_hint() {
			return getRuleContexts(Path_hintContext.class);
		}
		public Path_hintContext path_hint(int i) {
			return getRuleContext(Path_hintContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HqlParser.COMMA, i);
		}
		public Path_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPath_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPath_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPath_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_aliasContext path_alias() throws RecognitionException {
		Path_aliasContext _localctx = new Path_aliasContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_path_alias);
		int _la;
		try {
			setState(312);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(300);
				id();
				setState(302);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(301);
					column_alias();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(304);
				path_hint();
				setState(309);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(305);
					match(COMMA);
					setState(306);
					path_hint();
					}
					}
					setState(311);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_hintContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(HqlParser.ID, 0); }
		public Path_hintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPath_hint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPath_hint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPath_hint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_hintContext path_hint() throws RecognitionException {
		Path_hintContext _localctx = new Path_hintContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_path_hint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(314);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_alias_listContext extends ParserRuleContext {
		public TerminalNode LR_BRACKET() { return getToken(HqlParser.LR_BRACKET, 0); }
		public List<Column_aliasContext> column_alias() {
			return getRuleContexts(Column_aliasContext.class);
		}
		public Column_aliasContext column_alias(int i) {
			return getRuleContext(Column_aliasContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(HqlParser.RR_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(HqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HqlParser.COMMA, i);
		}
		public Column_alias_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_alias_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterColumn_alias_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitColumn_alias_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitColumn_alias_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_alias_listContext column_alias_list() throws RecognitionException {
		Column_alias_listContext _localctx = new Column_alias_listContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_column_alias_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(316);
			match(LR_BRACKET);
			setState(317);
			column_alias();
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(318);
				match(COMMA);
				setState(319);
				column_alias();
				}
				}
				setState(324);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(325);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_aliasContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode STRING() { return getToken(HqlParser.STRING, 0); }
		public Column_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterColumn_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitColumn_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitColumn_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_aliasContext column_alias() throws RecognitionException {
		Column_aliasContext _localctx = new Column_aliasContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_column_alias);
		try {
			setState(329);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AND:
			case AS:
			case BETWEEN:
			case BY:
			case DELETE:
			case DISTINCT:
			case DOUBLE:
			case ENTRIES:
			case FROM:
			case ILIKE:
			case IN:
			case KEY:
			case KEYS:
			case LIKE:
			case NOT:
			case NULL:
			case NVL:
			case OR:
			case ORDER:
			case REGEX:
			case SET:
			case SELECT:
			case VALUE:
			case VALUES:
			case WHERE:
			case ABSOLUTE:
			case COUNT:
			case COUNT_BIG:
			case LTRIM:
			case MAX:
			case MIN:
			case NUMBER:
			case PARTITION:
			case PATH:
			case RTRIM:
			case STDEV:
			case SUM:
			case TRIM:
			case TYPE:
			case DOUBLE_QUOTE_ID:
			case SQUARE_BRACKET_ID:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(327);
				id();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(328);
				match(STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Expression_listContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HqlParser.COMMA, i);
		}
		public Expression_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExpression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExpression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitExpression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_listContext expression_list() throws RecognitionException {
		Expression_listContext _localctx = new Expression_listContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(331);
			expression(0);
			setState(336);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(332);
				match(COMMA);
				setState(333);
				expression(0);
				}
				}
				setState(338);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_nameContext extends ParserRuleContext {
		public Simple_idContext simple_id() {
			return getRuleContext(Simple_idContext.class,0);
		}
		public Path_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPath_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPath_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPath_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Path_nameContext path_name() throws RecognitionException {
		Path_nameContext _localctx = new Path_nameContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_path_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(339);
			simple_id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathContext extends ParserRuleContext {
		public List<Path_nameContext> path_name() {
			return getRuleContexts(Path_nameContext.class);
		}
		public Path_nameContext path_name(int i) {
			return getRuleContext(Path_nameContext.class,i);
		}
		public List<TerminalNode> DIVIDE() { return getTokens(HqlParser.DIVIDE); }
		public TerminalNode DIVIDE(int i) {
			return getToken(HqlParser.DIVIDE, i);
		}
		public PathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPath(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathContext path() throws RecognitionException {
		PathContext _localctx = new PathContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DIVIDE) {
				{
				setState(341);
				match(DIVIDE);
				}
			}

			setState(344);
			path_name();
			setState(349);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DIVIDE) {
				{
				{
				setState(345);
				match(DIVIDE);
				setState(346);
				path_name();
				}
				}
				setState(351);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Full_column_nameContext extends ParserRuleContext {
		public TerminalNode KEY() { return getToken(HqlParser.KEY, 0); }
		public TerminalNode VALUE() { return getToken(HqlParser.VALUE, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(HqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(HqlParser.DOT, i);
		}
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public Full_column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_full_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFull_column_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFull_column_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitFull_column_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Full_column_nameContext full_column_name() throws RecognitionException {
		Full_column_nameContext _localctx = new Full_column_nameContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_full_column_name);
		int _la;
		try {
			setState(369);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(355);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
				case 1:
					{
					setState(352);
					path();
					setState(353);
					match(DOT);
					}
					break;
				}
				setState(357);
				_la = _input.LA(1);
				if ( !(_la==KEY || _la==VALUE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(361);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
				case 1:
					{
					setState(358);
					path();
					setState(359);
					match(DOT);
					}
					break;
				}
				setState(364);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KEY || _la==VALUE) {
					{
					setState(363);
					_la = _input.LA(1);
					if ( !(_la==KEY || _la==VALUE) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(366);
				match(DOT);
				setState(367);
				column_name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(368);
				column_name();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_name_listContext extends ParserRuleContext {
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HqlParser.COMMA, i);
		}
		public Column_name_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterColumn_name_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitColumn_name_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitColumn_name_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_name_listContext column_name_list() throws RecognitionException {
		Column_name_listContext _localctx = new Column_name_listContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_column_name_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			column_name();
			setState(376);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(372);
				match(COMMA);
				setState(373);
				column_name();
				}
				}
				setState(378);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Column_nameContext extends ParserRuleContext {
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public TerminalNode DOT() { return getToken(HqlParser.DOT, 0); }
		public Column_aliasContext column_alias() {
			return getRuleContext(Column_aliasContext.class,0);
		}
		public Path_aliasContext path_alias() {
			return getRuleContext(Path_aliasContext.class,0);
		}
		public Column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterColumn_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitColumn_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitColumn_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_nameContext column_name() throws RecognitionException {
		Column_nameContext _localctx = new Column_nameContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_column_name);
		try {
			setState(392);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(382);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
				case 1:
					{
					setState(379);
					id();
					setState(380);
					match(DOT);
					}
					break;
				}
				setState(384);
				id();
				setState(386);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
				case 1:
					{
					setState(385);
					column_alias();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(389);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
				case 1:
					{
					setState(388);
					path_alias();
					}
					break;
				}
				setState(391);
				id();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Simple_nameContext extends ParserRuleContext {
		public IdContext schema;
		public IdContext name;
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public TerminalNode DOT() { return getToken(HqlParser.DOT, 0); }
		public Simple_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSimple_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSimple_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSimple_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Simple_nameContext simple_name() throws RecognitionException {
		Simple_nameContext _localctx = new Simple_nameContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_simple_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(397);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				{
				setState(394);
				((Simple_nameContext)_localctx).schema = id();
				setState(395);
				match(DOT);
				}
				break;
			}
			setState(399);
			((Simple_nameContext)_localctx).name = id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Null_notnullContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(HqlParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public Null_notnullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_null_notnull; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterNull_notnull(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitNull_notnull(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitNull_notnull(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Null_notnullContext null_notnull() throws RecognitionException {
		Null_notnullContext _localctx = new Null_notnullContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_null_notnull);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(402);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(401);
				match(NOT);
				}
			}

			setState(404);
			match(NULL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Default_valueContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(HqlParser.NULL, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public Default_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_default_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDefault_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDefault_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitDefault_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Default_valueContext default_value() throws RecognitionException {
		Default_valueContext _localctx = new Default_valueContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_default_value);
		try {
			setState(408);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(406);
				match(NULL);
				}
				break;
			case DECIMAL:
			case STRING:
			case BINARY:
			case FLOAT:
			case REAL:
			case DOLLAR:
			case PLUS:
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(407);
				constant();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstantContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(HqlParser.STRING, 0); }
		public TerminalNode BINARY() { return getToken(HqlParser.BINARY, 0); }
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode REAL() { return getToken(HqlParser.REAL, 0); }
		public TerminalNode FLOAT() { return getToken(HqlParser.FLOAT, 0); }
		public SignContext sign() {
			return getRuleContext(SignContext.class,0);
		}
		public TerminalNode DOLLAR() { return getToken(HqlParser.DOLLAR, 0); }
		public TerminalNode DECIMAL() { return getToken(HqlParser.DECIMAL, 0); }
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_constant);
		int _la;
		try {
			setState(422);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(410);
				match(STRING);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(411);
				match(BINARY);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(412);
				number();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(414);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PLUS || _la==MINUS) {
					{
					setState(413);
					sign();
					}
				}

				setState(416);
				_la = _input.LA(1);
				if ( !(_la==FLOAT || _la==REAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(418);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PLUS || _la==MINUS) {
					{
					setState(417);
					sign();
					}
				}

				setState(420);
				match(DOLLAR);
				setState(421);
				_la = _input.LA(1);
				if ( !(_la==DECIMAL || _la==FLOAT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode DECIMAL() { return getToken(HqlParser.DECIMAL, 0); }
		public SignContext sign() {
			return getRuleContext(SignContext.class,0);
		}
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitNumber(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(425);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(424);
				sign();
				}
			}

			setState(427);
			match(DECIMAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SignContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(HqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(HqlParser.MINUS, 0); }
		public SignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignContext sign() throws RecognitionException {
		SignContext _localctx = new SignContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_sign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(429);
			_la = _input.LA(1);
			if ( !(_la==PLUS || _la==MINUS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdContext extends ParserRuleContext {
		public Simple_idContext simple_id() {
			return getRuleContext(Simple_idContext.class,0);
		}
		public TerminalNode DOUBLE_QUOTE_ID() { return getToken(HqlParser.DOUBLE_QUOTE_ID, 0); }
		public TerminalNode SQUARE_BRACKET_ID() { return getToken(HqlParser.SQUARE_BRACKET_ID, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_id);
		try {
			setState(434);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AND:
			case AS:
			case BETWEEN:
			case BY:
			case DELETE:
			case DISTINCT:
			case DOUBLE:
			case ENTRIES:
			case FROM:
			case ILIKE:
			case IN:
			case KEY:
			case KEYS:
			case LIKE:
			case NOT:
			case NULL:
			case NVL:
			case OR:
			case ORDER:
			case REGEX:
			case SET:
			case SELECT:
			case VALUE:
			case VALUES:
			case WHERE:
			case ABSOLUTE:
			case COUNT:
			case COUNT_BIG:
			case LTRIM:
			case MAX:
			case MIN:
			case NUMBER:
			case PARTITION:
			case PATH:
			case RTRIM:
			case STDEV:
			case SUM:
			case TRIM:
			case TYPE:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(431);
				simple_id();
				}
				break;
			case DOUBLE_QUOTE_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(432);
				match(DOUBLE_QUOTE_ID);
				}
				break;
			case SQUARE_BRACKET_ID:
				enterOuterAlt(_localctx, 3);
				{
				setState(433);
				match(SQUARE_BRACKET_ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Simple_idContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(HqlParser.ID, 0); }
		public TerminalNode ABSOLUTE() { return getToken(HqlParser.ABSOLUTE, 0); }
		public TerminalNode COUNT() { return getToken(HqlParser.COUNT, 0); }
		public TerminalNode COUNT_BIG() { return getToken(HqlParser.COUNT_BIG, 0); }
		public TerminalNode MAX() { return getToken(HqlParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(HqlParser.MIN, 0); }
		public TerminalNode NUMBER() { return getToken(HqlParser.NUMBER, 0); }
		public TerminalNode PARTITION() { return getToken(HqlParser.PARTITION, 0); }
		public TerminalNode PATH() { return getToken(HqlParser.PATH, 0); }
		public TerminalNode STDEV() { return getToken(HqlParser.STDEV, 0); }
		public TerminalNode SUM() { return getToken(HqlParser.SUM, 0); }
		public TerminalNode TYPE() { return getToken(HqlParser.TYPE, 0); }
		public KeywordsCanBeIdContext keywordsCanBeId() {
			return getRuleContext(KeywordsCanBeIdContext.class,0);
		}
		public Simple_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSimple_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSimple_id(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitSimple_id(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Simple_idContext simple_id() throws RecognitionException {
		Simple_idContext _localctx = new Simple_idContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_simple_id);
		try {
			setState(449);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(436);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(437);
				match(ABSOLUTE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(438);
				match(COUNT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(439);
				match(COUNT_BIG);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(440);
				match(MAX);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(441);
				match(MIN);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(442);
				match(NUMBER);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(443);
				match(PARTITION);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(444);
				match(PATH);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(445);
				match(STDEV);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(446);
				match(SUM);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(447);
				match(TYPE);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(448);
				keywordsCanBeId();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeywordsCanBeIdContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(HqlParser.AND, 0); }
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public TerminalNode BETWEEN() { return getToken(HqlParser.BETWEEN, 0); }
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public TerminalNode DELETE() { return getToken(HqlParser.DELETE, 0); }
		public TerminalNode DISTINCT() { return getToken(HqlParser.DISTINCT, 0); }
		public TerminalNode DOUBLE() { return getToken(HqlParser.DOUBLE, 0); }
		public TerminalNode ENTRIES() { return getToken(HqlParser.ENTRIES, 0); }
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public TerminalNode ILIKE() { return getToken(HqlParser.ILIKE, 0); }
		public TerminalNode IN() { return getToken(HqlParser.IN, 0); }
		public TerminalNode KEY() { return getToken(HqlParser.KEY, 0); }
		public TerminalNode KEYS() { return getToken(HqlParser.KEYS, 0); }
		public TerminalNode LIKE() { return getToken(HqlParser.LIKE, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(HqlParser.NULL, 0); }
		public TerminalNode NVL() { return getToken(HqlParser.NVL, 0); }
		public TerminalNode OR() { return getToken(HqlParser.OR, 0); }
		public TerminalNode ORDER() { return getToken(HqlParser.ORDER, 0); }
		public TerminalNode REGEX() { return getToken(HqlParser.REGEX, 0); }
		public TerminalNode SET() { return getToken(HqlParser.SET, 0); }
		public TerminalNode SELECT() { return getToken(HqlParser.SELECT, 0); }
		public TerminalNode VALUE() { return getToken(HqlParser.VALUE, 0); }
		public TerminalNode VALUES() { return getToken(HqlParser.VALUES, 0); }
		public TerminalNode WHERE() { return getToken(HqlParser.WHERE, 0); }
		public TerminalNode ABSOLUTE() { return getToken(HqlParser.ABSOLUTE, 0); }
		public TerminalNode COUNT() { return getToken(HqlParser.COUNT, 0); }
		public TerminalNode COUNT_BIG() { return getToken(HqlParser.COUNT_BIG, 0); }
		public TerminalNode LTRIM() { return getToken(HqlParser.LTRIM, 0); }
		public TerminalNode MAX() { return getToken(HqlParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(HqlParser.MIN, 0); }
		public TerminalNode NUMBER() { return getToken(HqlParser.NUMBER, 0); }
		public TerminalNode PARTITION() { return getToken(HqlParser.PARTITION, 0); }
		public TerminalNode PATH() { return getToken(HqlParser.PATH, 0); }
		public TerminalNode RTRIM() { return getToken(HqlParser.RTRIM, 0); }
		public TerminalNode STDEV() { return getToken(HqlParser.STDEV, 0); }
		public TerminalNode SUM() { return getToken(HqlParser.SUM, 0); }
		public TerminalNode TRIM() { return getToken(HqlParser.TRIM, 0); }
		public TerminalNode TYPE() { return getToken(HqlParser.TYPE, 0); }
		public KeywordsCanBeIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keywordsCanBeId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterKeywordsCanBeId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitKeywordsCanBeId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitKeywordsCanBeId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeywordsCanBeIdContext keywordsCanBeId() throws RecognitionException {
		KeywordsCanBeIdContext _localctx = new KeywordsCanBeIdContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_keywordsCanBeId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AND) | (1L << AS) | (1L << BETWEEN) | (1L << BY) | (1L << DELETE) | (1L << DISTINCT) | (1L << DOUBLE) | (1L << ENTRIES) | (1L << FROM) | (1L << ILIKE) | (1L << IN) | (1L << KEY) | (1L << KEYS) | (1L << LIKE) | (1L << NOT) | (1L << NULL) | (1L << NVL) | (1L << OR) | (1L << ORDER) | (1L << REGEX) | (1L << SET) | (1L << SELECT) | (1L << VALUE) | (1L << VALUES) | (1L << WHERE) | (1L << ABSOLUTE) | (1L << COUNT) | (1L << COUNT_BIG) | (1L << LTRIM) | (1L << MAX) | (1L << MIN) | (1L << NUMBER) | (1L << PARTITION) | (1L << PATH) | (1L << RTRIM) | (1L << STDEV) | (1L << SUM) | (1L << TRIM) | (1L << TYPE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Comparison_operatorContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(HqlParser.EQUAL, 0); }
		public TerminalNode GREATER() { return getToken(HqlParser.GREATER, 0); }
		public TerminalNode LESS() { return getToken(HqlParser.LESS, 0); }
		public TerminalNode EXCLAMATION() { return getToken(HqlParser.EXCLAMATION, 0); }
		public Comparison_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterComparison_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitComparison_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HqlVisitor ) return ((HqlVisitor<? extends T>)visitor).visitComparison_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Comparison_operatorContext comparison_operator() throws RecognitionException {
		Comparison_operatorContext _localctx = new Comparison_operatorContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_comparison_operator);
		try {
			setState(462);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(453);
				match(EQUAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(454);
				match(GREATER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(455);
				match(LESS);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(456);
				match(LESS);
				setState(457);
				match(EQUAL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(458);
				match(GREATER);
				setState(459);
				match(EQUAL);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(460);
				match(EXCLAMATION);
				setState(461);
				match(EQUAL);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 5:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 2);
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3V\u01d3\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\3\2\7\2"+
		"h\n\2\f\2\16\2k\13\2\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\5\5u\n\5\3\6\3\6"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u0083\n\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\7\7\u008f\n\7\f\7\16\7\u0092\13\7\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\5\b\u009a\n\b\3\t\3\t\3\n\3\n\3\n\7\n\u00a1\n\n\f\n\16\n"+
		"\u00a4\13\n\3\13\3\13\7\13\u00a8\n\13\f\13\16\13\u00ab\13\13\3\f\3\f\7"+
		"\f\u00af\n\f\f\f\16\f\u00b2\13\f\3\r\3\r\3\r\3\16\3\16\3\16\3\17\5\17"+
		"\u00bb\n\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u00cf\n\20\3\21\3\21\3\22\3\22\3\22"+
		"\3\22\5\22\u00d7\n\22\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u00df\n\22\3"+
		"\22\3\22\3\22\3\22\3\22\3\22\5\22\u00e7\n\22\3\22\3\22\5\22\u00eb\n\22"+
		"\3\23\3\23\3\23\3\23\3\23\5\23\u00f2\n\23\3\24\3\24\3\24\5\24\u00f7\n"+
		"\24\3\24\5\24\u00fa\n\24\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\7\26"+
		"\u0104\n\26\f\26\16\26\u0107\13\26\3\27\3\27\5\27\u010b\n\27\3\30\3\30"+
		"\3\31\3\31\3\32\3\32\5\32\u0113\n\32\3\33\3\33\5\33\u0117\n\33\3\34\3"+
		"\34\3\34\3\34\3\34\5\34\u011e\n\34\3\35\3\35\3\35\5\35\u0123\n\35\3\35"+
		"\5\35\u0126\n\35\3\36\3\36\3\37\5\37\u012b\n\37\3\37\3\37\3 \3 \5 \u0131"+
		"\n \3 \3 \3 \7 \u0136\n \f \16 \u0139\13 \5 \u013b\n \3!\3!\3\"\3\"\3"+
		"\"\3\"\7\"\u0143\n\"\f\"\16\"\u0146\13\"\3\"\3\"\3#\3#\5#\u014c\n#\3$"+
		"\3$\3$\7$\u0151\n$\f$\16$\u0154\13$\3%\3%\3&\5&\u0159\n&\3&\3&\3&\7&\u015e"+
		"\n&\f&\16&\u0161\13&\3\'\3\'\3\'\5\'\u0166\n\'\3\'\3\'\3\'\3\'\5\'\u016c"+
		"\n\'\3\'\5\'\u016f\n\'\3\'\3\'\3\'\5\'\u0174\n\'\3(\3(\3(\7(\u0179\n("+
		"\f(\16(\u017c\13(\3)\3)\3)\5)\u0181\n)\3)\3)\5)\u0185\n)\3)\5)\u0188\n"+
		")\3)\5)\u018b\n)\3*\3*\3*\5*\u0190\n*\3*\3*\3+\5+\u0195\n+\3+\3+\3,\3"+
		",\5,\u019b\n,\3-\3-\3-\3-\5-\u01a1\n-\3-\3-\5-\u01a5\n-\3-\3-\5-\u01a9"+
		"\n-\3.\5.\u01ac\n.\3.\3.\3/\3/\3\60\3\60\3\60\5\60\u01b5\n\60\3\61\3\61"+
		"\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\61\5\61\u01c4\n\61"+
		"\3\62\3\62\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\63\5\63\u01d1\n\63"+
		"\3\63\2\3\f\64\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64"+
		"\668:<>@BDFHJLNPRTVXZ\\^`bd\2\f\3\2QR\3\2NP\4\2QRTV\5\2\16\16\22\22\30"+
		"\30\4\2\5\5\t\t\5\2\f\f\21\21\34\34\4\2\20\20\33\33\3\2\66\67\4\2\62\62"+
		"\66\66\6\2\3\4\6\b\n#%,\2\u01f3\2i\3\2\2\2\4n\3\2\2\2\6p\3\2\2\2\br\3"+
		"\2\2\2\nv\3\2\2\2\f\u0082\3\2\2\2\16\u0099\3\2\2\2\20\u009b\3\2\2\2\22"+
		"\u009d\3\2\2\2\24\u00a5\3\2\2\2\26\u00ac\3\2\2\2\30\u00b3\3\2\2\2\32\u00b6"+
		"\3\2\2\2\34\u00ba\3\2\2\2\36\u00ce\3\2\2\2 \u00d0\3\2\2\2\"\u00ea\3\2"+
		"\2\2$\u00f1\3\2\2\2&\u00f3\3\2\2\2(\u00fb\3\2\2\2*\u00fe\3\2\2\2,\u0108"+
		"\3\2\2\2.\u010c\3\2\2\2\60\u010e\3\2\2\2\62\u0112\3\2\2\2\64\u0114\3\2"+
		"\2\2\66\u011d\3\2\2\28\u011f\3\2\2\2:\u0127\3\2\2\2<\u012a\3\2\2\2>\u013a"+
		"\3\2\2\2@\u013c\3\2\2\2B\u013e\3\2\2\2D\u014b\3\2\2\2F\u014d\3\2\2\2H"+
		"\u0155\3\2\2\2J\u0158\3\2\2\2L\u0173\3\2\2\2N\u0175\3\2\2\2P\u018a\3\2"+
		"\2\2R\u018f\3\2\2\2T\u0194\3\2\2\2V\u019a\3\2\2\2X\u01a8\3\2\2\2Z\u01ab"+
		"\3\2\2\2\\\u01af\3\2\2\2^\u01b4\3\2\2\2`\u01c3\3\2\2\2b\u01c5\3\2\2\2"+
		"d\u01d0\3\2\2\2fh\5\4\3\2gf\3\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2jl\3"+
		"\2\2\2ki\3\2\2\2lm\7\2\2\3m\3\3\2\2\2no\5\6\4\2o\5\3\2\2\2pq\5\b\5\2q"+
		"\7\3\2\2\2rt\5$\23\2su\5\n\6\2ts\3\2\2\2tu\3\2\2\2u\t\3\2\2\2vw\7L\2\2"+
		"w\13\3\2\2\2xy\b\7\1\2y\u0083\7\24\2\2z\u0083\5X-\2{\u0083\5L\'\2|}\7"+
		"I\2\2}~\5\f\7\2~\177\7J\2\2\177\u0083\3\2\2\2\u0080\u0081\t\2\2\2\u0081"+
		"\u0083\5\f\7\5\u0082x\3\2\2\2\u0082z\3\2\2\2\u0082{\3\2\2\2\u0082|\3\2"+
		"\2\2\u0082\u0080\3\2\2\2\u0083\u0090\3\2\2\2\u0084\u0085\f\6\2\2\u0085"+
		"\u0086\t\3\2\2\u0086\u008f\5\f\7\7\u0087\u0088\f\4\2\2\u0088\u0089\t\4"+
		"\2\2\u0089\u008f\5\f\7\5\u008a\u008b\f\3\2\2\u008b\u008c\5d\63\2\u008c"+
		"\u008d\5\f\7\4\u008d\u008f\3\2\2\2\u008e\u0084\3\2\2\2\u008e\u0087\3\2"+
		"\2\2\u008e\u008a\3\2\2\2\u008f\u0092\3\2\2\2\u0090\u008e\3\2\2\2\u0090"+
		"\u0091\3\2\2\2\u0091\r\3\2\2\2\u0092\u0090\3\2\2\2\u0093\u009a\7\24\2"+
		"\2\u0094\u009a\5X-\2\u0095\u0096\7I\2\2\u0096\u0097\5\16\b\2\u0097\u0098"+
		"\7J\2\2\u0098\u009a\3\2\2\2\u0099\u0093\3\2\2\2\u0099\u0094\3\2\2\2\u0099"+
		"\u0095\3\2\2\2\u009a\17\3\2\2\2\u009b\u009c\5\b\5\2\u009c\21\3\2\2\2\u009d"+
		"\u00a2\5\24\13\2\u009e\u009f\7K\2\2\u009f\u00a1\5\24\13\2\u00a0\u009e"+
		"\3\2\2\2\u00a1\u00a4\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3"+
		"\23\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a5\u00a9\5\26\f\2\u00a6\u00a8\5\30"+
		"\r\2\u00a7\u00a6\3\2\2\2\u00a8\u00ab\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9"+
		"\u00aa\3\2\2\2\u00aa\25\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ac\u00b0\5\34\17"+
		"\2\u00ad\u00af\5\32\16\2\u00ae\u00ad\3\2\2\2\u00af\u00b2\3\2\2\2\u00b0"+
		"\u00ae\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1\27\3\2\2\2\u00b2\u00b0\3\2\2"+
		"\2\u00b3\u00b4\7\26\2\2\u00b4\u00b5\5\26\f\2\u00b5\31\3\2\2\2\u00b6\u00b7"+
		"\7\3\2\2\u00b7\u00b8\5\34\17\2\u00b8\33\3\2\2\2\u00b9\u00bb\7\23\2\2\u00ba"+
		"\u00b9\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00bd\5\36"+
		"\20\2\u00bd\35\3\2\2\2\u00be\u00bf\5 \21\2\u00bf\u00c0\5\"\22\2\u00c0"+
		"\u00cf\3\2\2\2\u00c1\u00c2\5 \21\2\u00c2\u00c3\5\"\22\2\u00c3\u00cf\3"+
		"\2\2\2\u00c4\u00c5\5 \21\2\u00c5\u00c6\5\"\22\2\u00c6\u00cf\3\2\2\2\u00c7"+
		"\u00c8\5 \21\2\u00c8\u00c9\5\"\22\2\u00c9\u00cf\3\2\2\2\u00ca\u00cb\7"+
		"I\2\2\u00cb\u00cc\5\24\13\2\u00cc\u00cd\7J\2\2\u00cd\u00cf\3\2\2\2\u00ce"+
		"\u00be\3\2\2\2\u00ce\u00c1\3\2\2\2\u00ce\u00c4\3\2\2\2\u00ce\u00c7\3\2"+
		"\2\2\u00ce\u00ca\3\2\2\2\u00cf\37\3\2\2\2\u00d0\u00d1\5\f\7\2\u00d1!\3"+
		"\2\2\2\u00d2\u00d3\5d\63\2\u00d3\u00d4\5\f\7\2\u00d4\u00eb\3\2\2\2\u00d5"+
		"\u00d7\7\23\2\2\u00d6\u00d5\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00d8\3"+
		"\2\2\2\u00d8\u00d9\7\6\2\2\u00d9\u00da\5\f\7\2\u00da\u00db\7\3\2\2\u00db"+
		"\u00dc\5\f\7\2\u00dc\u00eb\3\2\2\2\u00dd\u00df\7\23\2\2\u00de\u00dd\3"+
		"\2\2\2\u00de\u00df\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e1\7\17\2\2\u00e1"+
		"\u00e2\7I\2\2\u00e2\u00e3\5F$\2\u00e3\u00e4\7J\2\2\u00e4\u00eb\3\2\2\2"+
		"\u00e5\u00e7\7\23\2\2\u00e6\u00e5\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7\u00e8"+
		"\3\2\2\2\u00e8\u00e9\t\5\2\2\u00e9\u00eb\5\f\7\2\u00ea\u00d2\3\2\2\2\u00ea"+
		"\u00d6\3\2\2\2\u00ea\u00de\3\2\2\2\u00ea\u00e6\3\2\2\2\u00eb#\3\2\2\2"+
		"\u00ec\u00f2\5&\24\2\u00ed\u00ee\7I\2\2\u00ee\u00ef\5$\23\2\u00ef\u00f0"+
		"\7J\2\2\u00f0\u00f2\3\2\2\2\u00f1\u00ec\3\2\2\2\u00f1\u00ed\3\2\2\2\u00f2"+
		"%\3\2\2\2\u00f3\u00f4\5(\25\2\u00f4\u00f6\5\62\32\2\u00f5\u00f7\5\64\33"+
		"\2\u00f6\u00f5\3\2\2\2\u00f6\u00f7\3\2\2\2\u00f7\u00f9\3\2\2\2\u00f8\u00fa"+
		"\5*\26\2\u00f9\u00f8\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\'\3\2\2\2\u00fb"+
		"\u00fc\7\32\2\2\u00fc\u00fd\5.\30\2\u00fd)\3\2\2\2\u00fe\u00ff\7\27\2"+
		"\2\u00ff\u0100\7\7\2\2\u0100\u0105\5,\27\2\u0101\u0102\7K\2\2\u0102\u0104"+
		"\5,\27\2\u0103\u0101\3\2\2\2\u0104\u0107\3\2\2\2\u0105\u0103\3\2\2\2\u0105"+
		"\u0106\3\2\2\2\u0106+\3\2\2\2\u0107\u0105\3\2\2\2\u0108\u010a\5\f\7\2"+
		"\u0109\u010b\t\6\2\2\u010a\u0109\3\2\2\2\u010a\u010b\3\2\2\2\u010b-\3"+
		"\2\2\2\u010c\u010d\5\60\31\2\u010d/\3\2\2\2\u010e\u010f\7N\2\2\u010f\61"+
		"\3\2\2\2\u0110\u0111\7\r\2\2\u0111\u0113\5\66\34\2\u0112\u0110\3\2\2\2"+
		"\u0112\u0113\3\2\2\2\u0113\63\3\2\2\2\u0114\u0116\7\35\2\2\u0115\u0117"+
		"\5\24\13\2\u0116\u0115\3\2\2\2\u0116\u0117\3\2\2\2\u0117\65\3\2\2\2\u0118"+
		"\u011e\58\35\2\u0119\u011a\7I\2\2\u011a\u011b\58\35\2\u011b\u011c\7J\2"+
		"\2\u011c\u011e\3\2\2\2\u011d\u0118\3\2\2\2\u011d\u0119\3\2\2\2\u011e\67"+
		"\3\2\2\2\u011f\u0122\5J&\2\u0120\u0121\7D\2\2\u0121\u0123\5:\36\2\u0122"+
		"\u0120\3\2\2\2\u0122\u0123\3\2\2\2\u0123\u0125\3\2\2\2\u0124\u0126\5<"+
		"\37\2\u0125\u0124\3\2\2\2\u0125\u0126\3\2\2\2\u01269\3\2\2\2\u0127\u0128"+
		"\t\7\2\2\u0128;\3\2\2\2\u0129\u012b\7\4\2\2\u012a\u0129\3\2\2\2\u012a"+
		"\u012b\3\2\2\2\u012b\u012c\3\2\2\2\u012c\u012d\5> \2\u012d=\3\2\2\2\u012e"+
		"\u0130\5^\60\2\u012f\u0131\5D#\2\u0130\u012f\3\2\2\2\u0130\u0131\3\2\2"+
		"\2\u0131\u013b\3\2\2\2\u0132\u0137\5@!\2\u0133\u0134\7K\2\2\u0134\u0136"+
		"\5@!\2\u0135\u0133\3\2\2\2\u0136\u0139\3\2\2\2\u0137\u0135\3\2\2\2\u0137"+
		"\u0138\3\2\2\2\u0138\u013b\3\2\2\2\u0139\u0137\3\2\2\2\u013a\u012e\3\2"+
		"\2\2\u013a\u0132\3\2\2\2\u013b?\3\2\2\2\u013c\u013d\7\63\2\2\u013dA\3"+
		"\2\2\2\u013e\u013f\7I\2\2\u013f\u0144\5D#\2\u0140\u0141\7K\2\2\u0141\u0143"+
		"\5D#\2\u0142\u0140\3\2\2\2\u0143\u0146\3\2\2\2\u0144\u0142\3\2\2\2\u0144"+
		"\u0145\3\2\2\2\u0145\u0147\3\2\2\2\u0146\u0144\3\2\2\2\u0147\u0148\7J"+
		"\2\2\u0148C\3\2\2\2\u0149\u014c\5^\60\2\u014a\u014c\7\64\2\2\u014b\u0149"+
		"\3\2\2\2\u014b\u014a\3\2\2\2\u014cE\3\2\2\2\u014d\u0152\5\f\7\2\u014e"+
		"\u014f\7K\2\2\u014f\u0151\5\f\7\2\u0150\u014e\3\2\2\2\u0151\u0154\3\2"+
		"\2\2\u0152\u0150\3\2\2\2\u0152\u0153\3\2\2\2\u0153G\3\2\2\2\u0154\u0152"+
		"\3\2\2\2\u0155\u0156\5`\61\2\u0156I\3\2\2\2\u0157\u0159\7O\2\2\u0158\u0157"+
		"\3\2\2\2\u0158\u0159\3\2\2\2\u0159\u015a\3\2\2\2\u015a\u015f\5H%\2\u015b"+
		"\u015c\7O\2\2\u015c\u015e\5H%\2\u015d\u015b\3\2\2\2\u015e\u0161\3\2\2"+
		"\2\u015f\u015d\3\2\2\2\u015f\u0160\3\2\2\2\u0160K\3\2\2\2\u0161\u015f"+
		"\3\2\2\2\u0162\u0163\5J&\2\u0163\u0164\7D\2\2\u0164\u0166\3\2\2\2\u0165"+
		"\u0162\3\2\2\2\u0165\u0166\3\2\2\2\u0166\u0167\3\2\2\2\u0167\u0174\t\b"+
		"\2\2\u0168\u0169\5J&\2\u0169\u016a\7D\2\2\u016a\u016c\3\2\2\2\u016b\u0168"+
		"\3\2\2\2\u016b\u016c\3\2\2\2\u016c\u016e\3\2\2\2\u016d\u016f\t\b\2\2\u016e"+
		"\u016d\3\2\2\2\u016e\u016f\3\2\2\2\u016f\u0170\3\2\2\2\u0170\u0171\7D"+
		"\2\2\u0171\u0174\5P)\2\u0172\u0174\5P)\2\u0173\u0165\3\2\2\2\u0173\u016b"+
		"\3\2\2\2\u0173\u0172\3\2\2\2\u0174M\3\2\2\2\u0175\u017a\5P)\2\u0176\u0177"+
		"\7K\2\2\u0177\u0179\5P)\2\u0178\u0176\3\2\2\2\u0179\u017c\3\2\2\2\u017a"+
		"\u0178\3\2\2\2\u017a\u017b\3\2\2\2\u017bO\3\2\2\2\u017c\u017a\3\2\2\2"+
		"\u017d\u017e\5^\60\2\u017e\u017f\7D\2\2\u017f\u0181\3\2\2\2\u0180\u017d"+
		"\3\2\2\2\u0180\u0181\3\2\2\2\u0181\u0182\3\2\2\2\u0182\u0184\5^\60\2\u0183"+
		"\u0185\5D#\2\u0184\u0183\3\2\2\2\u0184\u0185\3\2\2\2\u0185\u018b\3\2\2"+
		"\2\u0186\u0188\5> \2\u0187\u0186\3\2\2\2\u0187\u0188\3\2\2\2\u0188\u0189"+
		"\3\2\2\2\u0189\u018b\5^\60\2\u018a\u0180\3\2\2\2\u018a\u0187\3\2\2\2\u018b"+
		"Q\3\2\2\2\u018c\u018d\5^\60\2\u018d\u018e\7D\2\2\u018e\u0190\3\2\2\2\u018f"+
		"\u018c\3\2\2\2\u018f\u0190\3\2\2\2\u0190\u0191\3\2\2\2\u0191\u0192\5^"+
		"\60\2\u0192S\3\2\2\2\u0193\u0195\7\23\2\2\u0194\u0193\3\2\2\2\u0194\u0195"+
		"\3\2\2\2\u0195\u0196\3\2\2\2\u0196\u0197\7\24\2\2\u0197U\3\2\2\2\u0198"+
		"\u019b\7\24\2\2\u0199\u019b\5X-\2\u019a\u0198\3\2\2\2\u019a\u0199\3\2"+
		"\2\2\u019bW\3\2\2\2\u019c\u01a9\7\64\2\2\u019d\u01a9\7\65\2\2\u019e\u01a9"+
		"\5Z.\2\u019f\u01a1\5\\/\2\u01a0\u019f\3\2\2\2\u01a0\u01a1\3\2\2\2\u01a1"+
		"\u01a2\3\2\2\2\u01a2\u01a9\t\t\2\2\u01a3\u01a5\5\\/\2\u01a4\u01a3\3\2"+
		"\2\2\u01a4\u01a5\3\2\2\2\u01a5\u01a6\3\2\2\2\u01a6\u01a7\7H\2\2\u01a7"+
		"\u01a9\t\n\2\2\u01a8\u019c\3\2\2\2\u01a8\u019d\3\2\2\2\u01a8\u019e\3\2"+
		"\2\2\u01a8\u01a0\3\2\2\2\u01a8\u01a4\3\2\2\2\u01a9Y\3\2\2\2\u01aa\u01ac"+
		"\5\\/\2\u01ab\u01aa\3\2\2\2\u01ab\u01ac\3\2\2\2\u01ac\u01ad\3\2\2\2\u01ad"+
		"\u01ae\7\62\2\2\u01ae[\3\2\2\2\u01af\u01b0\t\2\2\2\u01b0]\3\2\2\2\u01b1"+
		"\u01b5\5`\61\2\u01b2\u01b5\7\60\2\2\u01b3\u01b5\7\61\2\2\u01b4\u01b1\3"+
		"\2\2\2\u01b4\u01b2\3\2\2\2\u01b4\u01b3\3\2\2\2\u01b5_\3\2\2\2\u01b6\u01c4"+
		"\7\63\2\2\u01b7\u01c4\7\36\2\2\u01b8\u01c4\7\37\2\2\u01b9\u01c4\7 \2\2"+
		"\u01ba\u01c4\7\"\2\2\u01bb\u01c4\7#\2\2\u01bc\u01c4\7%\2\2\u01bd\u01c4"+
		"\7&\2\2\u01be\u01c4\7\'\2\2\u01bf\u01c4\7)\2\2\u01c0\u01c4\7*\2\2\u01c1"+
		"\u01c4\7,\2\2\u01c2\u01c4\5b\62\2\u01c3\u01b6\3\2\2\2\u01c3\u01b7\3\2"+
		"\2\2\u01c3\u01b8\3\2\2\2\u01c3\u01b9\3\2\2\2\u01c3\u01ba\3\2\2\2\u01c3"+
		"\u01bb\3\2\2\2\u01c3\u01bc\3\2\2\2\u01c3\u01bd\3\2\2\2\u01c3\u01be\3\2"+
		"\2\2\u01c3\u01bf\3\2\2\2\u01c3\u01c0\3\2\2\2\u01c3\u01c1\3\2\2\2\u01c3"+
		"\u01c2\3\2\2\2\u01c4a\3\2\2\2\u01c5\u01c6\t\13\2\2\u01c6c\3\2\2\2\u01c7"+
		"\u01d1\78\2\2\u01c8\u01d1\79\2\2\u01c9\u01d1\7:\2\2\u01ca\u01cb\7:\2\2"+
		"\u01cb\u01d1\78\2\2\u01cc\u01cd\79\2\2\u01cd\u01d1\78\2\2\u01ce\u01cf"+
		"\7;\2\2\u01cf\u01d1\78\2\2\u01d0\u01c7\3\2\2\2\u01d0\u01c8\3\2\2\2\u01d0"+
		"\u01c9\3\2\2\2\u01d0\u01ca\3\2\2\2\u01d0\u01cc\3\2\2\2\u01d0\u01ce\3\2"+
		"\2\2\u01d1e\3\2\2\2\67it\u0082\u008e\u0090\u0099\u00a2\u00a9\u00b0\u00ba"+
		"\u00ce\u00d6\u00de\u00e6\u00ea\u00f1\u00f6\u00f9\u0105\u010a\u0112\u0116"+
		"\u011d\u0122\u0125\u012a\u0130\u0137\u013a\u0144\u014b\u0152\u0158\u015f"+
		"\u0165\u016b\u016e\u0173\u017a\u0180\u0184\u0187\u018a\u018f\u0194\u019a"+
		"\u01a0\u01a4\u01a8\u01ab\u01b4\u01c3\u01d0";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}