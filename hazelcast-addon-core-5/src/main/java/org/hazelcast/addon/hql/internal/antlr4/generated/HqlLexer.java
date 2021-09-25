// Generated from /Users/dpark/Work/git/Hazelcast/hazelcast-addon/src/main/resources/Hql.g4 by ANTLR 4.7.2
package org.hazelcast.addon.hql.internal.antlr4.generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class HqlLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"FullWidthLetter", "AND", "AS", "ASC", "BETWEEN", "BY", "DELETE", "DESC", 
			"DISTINCT", "DOUBLE", "ENTRIES", "FROM", "ILIKE", "IN", "KEY", "KEYS", 
			"LIKE", "NOT", "NULL", "NVL", "OR", "ORDER", "REGEX", "SET", "SELECT", 
			"VALUE", "VALUES", "WHERE", "ABSOLUTE", "COUNT", "COUNT_BIG", "LTRIM", 
			"MAX", "MIN", "MINUTE", "NUMBER", "PARTITION", "PATH", "RTRIM", "STDEV", 
			"SUM", "TRIM", "TYPE", "SPACE", "COMMENT", "LINE_COMMENT", "DOUBLE_QUOTE_ID", 
			"SQUARE_BRACKET_ID", "DECIMAL", "ALPHA", "DIGIT", "ID", "STRING", "BINARY", 
			"FLOAT", "REAL", "EQUAL", "GREATER", "LESS", "EXCLAMATION", "PLUS_ASSIGN", 
			"MINUS_ASSIGN", "MULT_ASSIGN", "DIV_ASSIGN", "MOD_ASSIGN", "AND_ASSIGN", 
			"XOR_ASSIGN", "OR_ASSIGN", "DOT", "UNDERLINE", "AT", "SHARP", "DOLLAR", 
			"LR_BRACKET", "RR_BRACKET", "COMMA", "SEMI", "COLON", "STAR", "DIVIDE", 
			"MODULE", "PLUS", "MINUS", "BIT_NOT", "BIT_OR", "BIT_AND", "BIT_XOR", 
			"LETTER", "DEC_DOT_DEC", "HEX_DIGIT", "DEC_DIGIT", "A", "B", "C", "D", 
			"E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", 
			"S", "T", "U", "V", "W", "X", "Y", "Z"
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


	public HqlLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Hql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2V\u02dc\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\4k\t"+
		"k\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\tr\4s\ts\4t\tt\4u\tu\4v\tv\3"+
		"\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21"+
		"\3\21\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24"+
		"\3\24\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\35\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\36\3\36"+
		"\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3 \3 \3 \3 "+
		"\3 \3 \3!\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3$\3$\3$\3$\3$\3"+
		"$\3$\3%\3%\3%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3&\3&\3&\3&\3&\3\'\3\'\3\'\3"+
		"\'\3\'\3(\3(\3(\3(\3(\3(\3)\3)\3)\3)\3)\3)\3*\3*\3*\3*\3+\3+\3+\3+\3+"+
		"\3,\3,\3,\3,\3,\3-\6-\u01df\n-\r-\16-\u01e0\3-\3-\3.\3.\3.\3.\7.\u01e9"+
		"\n.\f.\16.\u01ec\13.\3.\3.\3.\3.\3.\3/\3/\3/\3/\7/\u01f7\n/\f/\16/\u01fa"+
		"\13/\3/\3/\3\60\3\60\6\60\u0200\n\60\r\60\16\60\u0201\3\60\3\60\3\61\3"+
		"\61\6\61\u0208\n\61\r\61\16\61\u0209\3\61\3\61\3\62\6\62\u020f\n\62\r"+
		"\62\16\62\u0210\3\63\3\63\3\64\3\64\3\65\3\65\3\65\7\65\u021a\n\65\f\65"+
		"\16\65\u021d\13\65\3\66\5\66\u0220\n\66\3\66\3\66\3\66\3\66\7\66\u0226"+
		"\n\66\f\66\16\66\u0229\13\66\3\66\3\66\3\67\3\67\3\67\7\67\u0230\n\67"+
		"\f\67\16\67\u0233\13\67\38\38\39\39\39\59\u023a\n9\39\69\u023d\n9\r9\16"+
		"9\u023e\59\u0241\n9\3:\3:\3;\3;\3<\3<\3=\3=\3>\3>\3>\3?\3?\3?\3@\3@\3"+
		"@\3A\3A\3A\3B\3B\3B\3C\3C\3C\3D\3D\3D\3E\3E\3E\3F\3F\3G\3G\3H\3H\3I\3"+
		"I\3J\3J\3K\3K\3L\3L\3M\3M\3N\3N\3O\3O\3P\3P\3Q\3Q\3R\3R\3S\3S\3T\3T\3"+
		"U\3U\3V\3V\3W\3W\3X\3X\3Y\3Y\3Z\6Z\u028c\nZ\rZ\16Z\u028d\3Z\3Z\6Z\u0292"+
		"\nZ\rZ\16Z\u0293\3Z\6Z\u0297\nZ\rZ\16Z\u0298\3Z\3Z\3Z\3Z\6Z\u029f\nZ\r"+
		"Z\16Z\u02a0\5Z\u02a3\nZ\3[\3[\3\\\3\\\3]\3]\3^\3^\3_\3_\3`\3`\3a\3a\3"+
		"b\3b\3c\3c\3d\3d\3e\3e\3f\3f\3g\3g\3h\3h\3i\3i\3j\3j\3k\3k\3l\3l\3m\3"+
		"m\3n\3n\3o\3o\3p\3p\3q\3q\3r\3r\3s\3s\3t\3t\3u\3u\3v\3v\3\u01ea\2w\3\2"+
		"\5\3\7\4\t\5\13\6\r\7\17\b\21\t\23\n\25\13\27\f\31\r\33\16\35\17\37\20"+
		"!\21#\22%\23\'\24)\25+\26-\27/\30\61\31\63\32\65\33\67\349\35;\36=\37"+
		"? A!C\"E#G$I%K&M\'O(Q)S*U+W,Y-[.]/_\60a\61c\62e\2g\2i\63k\64m\65o\66q"+
		"\67s8u9w:y;{<}=\177>\u0081?\u0083@\u0085A\u0087B\u0089C\u008bD\u008dE"+
		"\u008fF\u0091G\u0093H\u0095I\u0097J\u0099K\u009bL\u009dM\u009fN\u00a1"+
		"O\u00a3P\u00a5Q\u00a7R\u00a9S\u00abT\u00adU\u00afV\u00b1\2\u00b3\2\u00b5"+
		"\2\u00b7\2\u00b9\2\u00bb\2\u00bd\2\u00bf\2\u00c1\2\u00c3\2\u00c5\2\u00c7"+
		"\2\u00c9\2\u00cb\2\u00cd\2\u00cf\2\u00d1\2\u00d3\2\u00d5\2\u00d7\2\u00d9"+
		"\2\u00db\2\u00dd\2\u00df\2\u00e1\2\u00e3\2\u00e5\2\u00e7\2\u00e9\2\u00eb"+
		"\2\3\2&\f\2\u00c2\u00d8\u00da\u00f8\u00fa\u2001\u2c02\u3001\u3042\u3191"+
		"\u3302\u3381\u3402\u4001\u4e02\ud801\uf902\ufb01\uff02\ufff2\5\2\13\f"+
		"\17\17\"\"\4\2\f\f\17\17\3\2$$\3\2__\5\2C\\aac|\3\2\62;\3\2))\4\2--//"+
		"\5\2\62;CHch\4\2CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4"+
		"\2JJjj\4\2KKkk\4\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRr"+
		"r\4\2SSss\4\2TTtt\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2"+
		"[[{{\4\2\\\\||\2\u02cf\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2"+
		"\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S"+
		"\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2"+
		"\2\2\2a\3\2\2\2\2c\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2"+
		"\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3\2\2\2\2}"+
		"\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085\3\2\2\2"+
		"\2\u0087\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f"+
		"\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2"+
		"\2\2\u0099\3\2\2\2\2\u009b\3\2\2\2\2\u009d\3\2\2\2\2\u009f\3\2\2\2\2\u00a1"+
		"\3\2\2\2\2\u00a3\3\2\2\2\2\u00a5\3\2\2\2\2\u00a7\3\2\2\2\2\u00a9\3\2\2"+
		"\2\2\u00ab\3\2\2\2\2\u00ad\3\2\2\2\2\u00af\3\2\2\2\3\u00ed\3\2\2\2\5\u00ef"+
		"\3\2\2\2\7\u00f3\3\2\2\2\t\u00f6\3\2\2\2\13\u00fa\3\2\2\2\r\u0102\3\2"+
		"\2\2\17\u0105\3\2\2\2\21\u010c\3\2\2\2\23\u0111\3\2\2\2\25\u011a\3\2\2"+
		"\2\27\u0121\3\2\2\2\31\u0129\3\2\2\2\33\u012e\3\2\2\2\35\u0134\3\2\2\2"+
		"\37\u0137\3\2\2\2!\u013b\3\2\2\2#\u0140\3\2\2\2%\u0145\3\2\2\2\'\u0149"+
		"\3\2\2\2)\u014e\3\2\2\2+\u0152\3\2\2\2-\u0155\3\2\2\2/\u015b\3\2\2\2\61"+
		"\u0161\3\2\2\2\63\u0165\3\2\2\2\65\u016c\3\2\2\2\67\u0172\3\2\2\29\u0179"+
		"\3\2\2\2;\u017f\3\2\2\2=\u0188\3\2\2\2?\u018e\3\2\2\2A\u0198\3\2\2\2C"+
		"\u019e\3\2\2\2E\u01a2\3\2\2\2G\u01a6\3\2\2\2I\u01ad\3\2\2\2K\u01b4\3\2"+
		"\2\2M\u01be\3\2\2\2O\u01c3\3\2\2\2Q\u01c9\3\2\2\2S\u01cf\3\2\2\2U\u01d3"+
		"\3\2\2\2W\u01d8\3\2\2\2Y\u01de\3\2\2\2[\u01e4\3\2\2\2]\u01f2\3\2\2\2_"+
		"\u01fd\3\2\2\2a\u0205\3\2\2\2c\u020e\3\2\2\2e\u0212\3\2\2\2g\u0214\3\2"+
		"\2\2i\u0216\3\2\2\2k\u021f\3\2\2\2m\u022c\3\2\2\2o\u0234\3\2\2\2q\u0236"+
		"\3\2\2\2s\u0242\3\2\2\2u\u0244\3\2\2\2w\u0246\3\2\2\2y\u0248\3\2\2\2{"+
		"\u024a\3\2\2\2}\u024d\3\2\2\2\177\u0250\3\2\2\2\u0081\u0253\3\2\2\2\u0083"+
		"\u0256\3\2\2\2\u0085\u0259\3\2\2\2\u0087\u025c\3\2\2\2\u0089\u025f\3\2"+
		"\2\2\u008b\u0262\3\2\2\2\u008d\u0264\3\2\2\2\u008f\u0266\3\2\2\2\u0091"+
		"\u0268\3\2\2\2\u0093\u026a\3\2\2\2\u0095\u026c\3\2\2\2\u0097\u026e\3\2"+
		"\2\2\u0099\u0270\3\2\2\2\u009b\u0272\3\2\2\2\u009d\u0274\3\2\2\2\u009f"+
		"\u0276\3\2\2\2\u00a1\u0278\3\2\2\2\u00a3\u027a\3\2\2\2\u00a5\u027c\3\2"+
		"\2\2\u00a7\u027e\3\2\2\2\u00a9\u0280\3\2\2\2\u00ab\u0282\3\2\2\2\u00ad"+
		"\u0284\3\2\2\2\u00af\u0286\3\2\2\2\u00b1\u0288\3\2\2\2\u00b3\u02a2\3\2"+
		"\2\2\u00b5\u02a4\3\2\2\2\u00b7\u02a6\3\2\2\2\u00b9\u02a8\3\2\2\2\u00bb"+
		"\u02aa\3\2\2\2\u00bd\u02ac\3\2\2\2\u00bf\u02ae\3\2\2\2\u00c1\u02b0\3\2"+
		"\2\2\u00c3\u02b2\3\2\2\2\u00c5\u02b4\3\2\2\2\u00c7\u02b6\3\2\2\2\u00c9"+
		"\u02b8\3\2\2\2\u00cb\u02ba\3\2\2\2\u00cd\u02bc\3\2\2\2\u00cf\u02be\3\2"+
		"\2\2\u00d1\u02c0\3\2\2\2\u00d3\u02c2\3\2\2\2\u00d5\u02c4\3\2\2\2\u00d7"+
		"\u02c6\3\2\2\2\u00d9\u02c8\3\2\2\2\u00db\u02ca\3\2\2\2\u00dd\u02cc\3\2"+
		"\2\2\u00df\u02ce\3\2\2\2\u00e1\u02d0\3\2\2\2\u00e3\u02d2\3\2\2\2\u00e5"+
		"\u02d4\3\2\2\2\u00e7\u02d6\3\2\2\2\u00e9\u02d8\3\2\2\2\u00eb\u02da\3\2"+
		"\2\2\u00ed\u00ee\t\2\2\2\u00ee\4\3\2\2\2\u00ef\u00f0\5\u00b9]\2\u00f0"+
		"\u00f1\5\u00d3j\2\u00f1\u00f2\5\u00bf`\2\u00f2\6\3\2\2\2\u00f3\u00f4\5"+
		"\u00b9]\2\u00f4\u00f5\5\u00ddo\2\u00f5\b\3\2\2\2\u00f6\u00f7\5\u00b9]"+
		"\2\u00f7\u00f8\5\u00ddo\2\u00f8\u00f9\5\u00bd_\2\u00f9\n\3\2\2\2\u00fa"+
		"\u00fb\5\u00bb^\2\u00fb\u00fc\5\u00c1a\2\u00fc\u00fd\5\u00dfp\2\u00fd"+
		"\u00fe\5\u00e5s\2\u00fe\u00ff\5\u00c1a\2\u00ff\u0100\5\u00c1a\2\u0100"+
		"\u0101\5\u00d3j\2\u0101\f\3\2\2\2\u0102\u0103\5\u00bb^\2\u0103\u0104\5"+
		"\u00e9u\2\u0104\16\3\2\2\2\u0105\u0106\5\u00bf`\2\u0106\u0107\5\u00c1"+
		"a\2\u0107\u0108\5\u00cfh\2\u0108\u0109\5\u00c1a\2\u0109\u010a\5\u00df"+
		"p\2\u010a\u010b\5\u00c1a\2\u010b\20\3\2\2\2\u010c\u010d\5\u00bf`\2\u010d"+
		"\u010e\5\u00c1a\2\u010e\u010f\5\u00ddo\2\u010f\u0110\5\u00bd_\2\u0110"+
		"\22\3\2\2\2\u0111\u0112\5\u00bf`\2\u0112\u0113\5\u00c9e\2\u0113\u0114"+
		"\5\u00ddo\2\u0114\u0115\5\u00dfp\2\u0115\u0116\5\u00c9e\2\u0116\u0117"+
		"\5\u00d3j\2\u0117\u0118\5\u00bd_\2\u0118\u0119\5\u00dfp\2\u0119\24\3\2"+
		"\2\2\u011a\u011b\5\u00bf`\2\u011b\u011c\5\u00d5k\2\u011c\u011d\5\u00e1"+
		"q\2\u011d\u011e\5\u00bb^\2\u011e\u011f\5\u00cfh\2\u011f\u0120\5\u00c1"+
		"a\2\u0120\26\3\2\2\2\u0121\u0122\5\u00c1a\2\u0122\u0123\5\u00d3j\2\u0123"+
		"\u0124\5\u00dfp\2\u0124\u0125\5\u00dbn\2\u0125\u0126\5\u00c9e\2\u0126"+
		"\u0127\5\u00c1a\2\u0127\u0128\5\u00ddo\2\u0128\30\3\2\2\2\u0129\u012a"+
		"\5\u00c3b\2\u012a\u012b\5\u00dbn\2\u012b\u012c\5\u00d5k\2\u012c\u012d"+
		"\5\u00d1i\2\u012d\32\3\2\2\2\u012e\u012f\5\u00c9e\2\u012f\u0130\5\u00cf"+
		"h\2\u0130\u0131\5\u00c9e\2\u0131\u0132\5\u00cdg\2\u0132\u0133\5\u00c1"+
		"a\2\u0133\34\3\2\2\2\u0134\u0135\5\u00c9e\2\u0135\u0136\5\u00d3j\2\u0136"+
		"\36\3\2\2\2\u0137\u0138\5\u00cdg\2\u0138\u0139\5\u00c1a\2\u0139\u013a"+
		"\5\u00e9u\2\u013a \3\2\2\2\u013b\u013c\5\u00cdg\2\u013c\u013d\5\u00c1"+
		"a\2\u013d\u013e\5\u00e9u\2\u013e\u013f\5\u00ddo\2\u013f\"\3\2\2\2\u0140"+
		"\u0141\5\u00cfh\2\u0141\u0142\5\u00c9e\2\u0142\u0143\5\u00cdg\2\u0143"+
		"\u0144\5\u00c1a\2\u0144$\3\2\2\2\u0145\u0146\5\u00d3j\2\u0146\u0147\5"+
		"\u00d5k\2\u0147\u0148\5\u00dfp\2\u0148&\3\2\2\2\u0149\u014a\5\u00d3j\2"+
		"\u014a\u014b\5\u00e1q\2\u014b\u014c\5\u00cfh\2\u014c\u014d\5\u00cfh\2"+
		"\u014d(\3\2\2\2\u014e\u014f\5\u00d3j\2\u014f\u0150\5\u00e3r\2\u0150\u0151"+
		"\5\u00cfh\2\u0151*\3\2\2\2\u0152\u0153\5\u00d5k\2\u0153\u0154\5\u00db"+
		"n\2\u0154,\3\2\2\2\u0155\u0156\5\u00d5k\2\u0156\u0157\5\u00dbn\2\u0157"+
		"\u0158\5\u00bf`\2\u0158\u0159\5\u00c1a\2\u0159\u015a\5\u00dbn\2\u015a"+
		".\3\2\2\2\u015b\u015c\5\u00dbn\2\u015c\u015d\5\u00c1a\2\u015d\u015e\5"+
		"\u00c5c\2\u015e\u015f\5\u00c1a\2\u015f\u0160\5\u00e7t\2\u0160\60\3\2\2"+
		"\2\u0161\u0162\5\u00ddo\2\u0162\u0163\5\u00c1a\2\u0163\u0164\5\u00dfp"+
		"\2\u0164\62\3\2\2\2\u0165\u0166\5\u00ddo\2\u0166\u0167\5\u00c1a\2\u0167"+
		"\u0168\5\u00cfh\2\u0168\u0169\5\u00c1a\2\u0169\u016a\5\u00bd_\2\u016a"+
		"\u016b\5\u00dfp\2\u016b\64\3\2\2\2\u016c\u016d\5\u00e3r\2\u016d\u016e"+
		"\5\u00b9]\2\u016e\u016f\5\u00cfh\2\u016f\u0170\5\u00e1q\2\u0170\u0171"+
		"\5\u00c1a\2\u0171\66\3\2\2\2\u0172\u0173\5\u00e3r\2\u0173\u0174\5\u00b9"+
		"]\2\u0174\u0175\5\u00cfh\2\u0175\u0176\5\u00e1q\2\u0176\u0177\5\u00c1"+
		"a\2\u0177\u0178\5\u00ddo\2\u01788\3\2\2\2\u0179\u017a\5\u00e5s\2\u017a"+
		"\u017b\5\u00c7d\2\u017b\u017c\5\u00c1a\2\u017c\u017d\5\u00dbn\2\u017d"+
		"\u017e\5\u00c1a\2\u017e:\3\2\2\2\u017f\u0180\5\u00b9]\2\u0180\u0181\5"+
		"\u00bb^\2\u0181\u0182\5\u00ddo\2\u0182\u0183\5\u00d5k\2\u0183\u0184\5"+
		"\u00cfh\2\u0184\u0185\5\u00e1q\2\u0185\u0186\5\u00dfp\2\u0186\u0187\5"+
		"\u00c1a\2\u0187<\3\2\2\2\u0188\u0189\5\u00bd_\2\u0189\u018a\5\u00d5k\2"+
		"\u018a\u018b\5\u00e1q\2\u018b\u018c\5\u00d3j\2\u018c\u018d\5\u00dfp\2"+
		"\u018d>\3\2\2\2\u018e\u018f\5\u00bd_\2\u018f\u0190\5\u00d5k\2\u0190\u0191"+
		"\5\u00e1q\2\u0191\u0192\5\u00d3j\2\u0192\u0193\5\u00dfp\2\u0193\u0194"+
		"\7a\2\2\u0194\u0195\5\u00bb^\2\u0195\u0196\5\u00c9e\2\u0196\u0197\5\u00c5"+
		"c\2\u0197@\3\2\2\2\u0198\u0199\5\u00cfh\2\u0199\u019a\5\u00dfp\2\u019a"+
		"\u019b\5\u00dbn\2\u019b\u019c\5\u00c9e\2\u019c\u019d\5\u00d1i\2\u019d"+
		"B\3\2\2\2\u019e\u019f\5\u00d1i\2\u019f\u01a0\5\u00b9]\2\u01a0\u01a1\5"+
		"\u00e7t\2\u01a1D\3\2\2\2\u01a2\u01a3\5\u00d1i\2\u01a3\u01a4\5\u00c9e\2"+
		"\u01a4\u01a5\5\u00d3j\2\u01a5F\3\2\2\2\u01a6\u01a7\5\u00d1i\2\u01a7\u01a8"+
		"\5\u00c9e\2\u01a8\u01a9\5\u00d3j\2\u01a9\u01aa\5\u00e1q\2\u01aa\u01ab"+
		"\5\u00dfp\2\u01ab\u01ac\5\u00c1a\2\u01acH\3\2\2\2\u01ad\u01ae\5\u00d3"+
		"j\2\u01ae\u01af\5\u00e1q\2\u01af\u01b0\5\u00d1i\2\u01b0\u01b1\5\u00bb"+
		"^\2\u01b1\u01b2\5\u00c1a\2\u01b2\u01b3\5\u00dbn\2\u01b3J\3\2\2\2\u01b4"+
		"\u01b5\5\u00d7l\2\u01b5\u01b6\5\u00b9]\2\u01b6\u01b7\5\u00dbn\2\u01b7"+
		"\u01b8\5\u00dfp\2\u01b8\u01b9\5\u00c9e\2\u01b9\u01ba\5\u00dfp\2\u01ba"+
		"\u01bb\5\u00c9e\2\u01bb\u01bc\5\u00d5k\2\u01bc\u01bd\5\u00d3j\2\u01bd"+
		"L\3\2\2\2\u01be\u01bf\5\u00d7l\2\u01bf\u01c0\5\u00b9]\2\u01c0\u01c1\5"+
		"\u00dfp\2\u01c1\u01c2\5\u00c7d\2\u01c2N\3\2\2\2\u01c3\u01c4\5\u00dbn\2"+
		"\u01c4\u01c5\5\u00dfp\2\u01c5\u01c6\5\u00dbn\2\u01c6\u01c7\5\u00c9e\2"+
		"\u01c7\u01c8\5\u00d1i\2\u01c8P\3\2\2\2\u01c9\u01ca\5\u00ddo\2\u01ca\u01cb"+
		"\5\u00dfp\2\u01cb\u01cc\5\u00bf`\2\u01cc\u01cd\5\u00c1a\2\u01cd\u01ce"+
		"\5\u00e3r\2\u01ceR\3\2\2\2\u01cf\u01d0\5\u00ddo\2\u01d0\u01d1\5\u00e1"+
		"q\2\u01d1\u01d2\5\u00d1i\2\u01d2T\3\2\2\2\u01d3\u01d4\5\u00dfp\2\u01d4"+
		"\u01d5\5\u00dbn\2\u01d5\u01d6\5\u00c9e\2\u01d6\u01d7\5\u00d1i\2\u01d7"+
		"V\3\2\2\2\u01d8\u01d9\5\u00dfp\2\u01d9\u01da\5\u00e9u\2\u01da\u01db\5"+
		"\u00d7l\2\u01db\u01dc\5\u00c1a\2\u01dcX\3\2\2\2\u01dd\u01df\t\3\2\2\u01de"+
		"\u01dd\3\2\2\2\u01df\u01e0\3\2\2\2\u01e0\u01de\3\2\2\2\u01e0\u01e1\3\2"+
		"\2\2\u01e1\u01e2\3\2\2\2\u01e2\u01e3\b-\2\2\u01e3Z\3\2\2\2\u01e4\u01e5"+
		"\7\61\2\2\u01e5\u01e6\7,\2\2\u01e6\u01ea\3\2\2\2\u01e7\u01e9\13\2\2\2"+
		"\u01e8\u01e7\3\2\2\2\u01e9\u01ec\3\2\2\2\u01ea\u01eb\3\2\2\2\u01ea\u01e8"+
		"\3\2\2\2\u01eb\u01ed\3\2\2\2\u01ec\u01ea\3\2\2\2\u01ed\u01ee\7,\2\2\u01ee"+
		"\u01ef\7\61\2\2\u01ef\u01f0\3\2\2\2\u01f0\u01f1\b.\3\2\u01f1\\\3\2\2\2"+
		"\u01f2\u01f3\7/\2\2\u01f3\u01f4\7/\2\2\u01f4\u01f8\3\2\2\2\u01f5\u01f7"+
		"\n\4\2\2\u01f6\u01f5\3\2\2\2\u01f7\u01fa\3\2\2\2\u01f8\u01f6\3\2\2\2\u01f8"+
		"\u01f9\3\2\2\2\u01f9\u01fb\3\2\2\2\u01fa\u01f8\3\2\2\2\u01fb\u01fc\b/"+
		"\3\2\u01fc^\3\2\2\2\u01fd\u01ff\7$\2\2\u01fe\u0200\n\5\2\2\u01ff\u01fe"+
		"\3\2\2\2\u0200\u0201\3\2\2\2\u0201\u01ff\3\2\2\2\u0201\u0202\3\2\2\2\u0202"+
		"\u0203\3\2\2\2\u0203\u0204\7$\2\2\u0204`\3\2\2\2\u0205\u0207\7]\2\2\u0206"+
		"\u0208\n\6\2\2\u0207\u0206\3\2\2\2\u0208\u0209\3\2\2\2\u0209\u0207\3\2"+
		"\2\2\u0209\u020a\3\2\2\2\u020a\u020b\3\2\2\2\u020b\u020c\7_\2\2\u020c"+
		"b\3\2\2\2\u020d\u020f\5\u00b7\\\2\u020e\u020d\3\2\2\2\u020f\u0210\3\2"+
		"\2\2\u0210\u020e\3\2\2\2\u0210\u0211\3\2\2\2\u0211d\3\2\2\2\u0212\u0213"+
		"\t\7\2\2\u0213f\3\2\2\2\u0214\u0215\t\b\2\2\u0215h\3\2\2\2\u0216\u021b"+
		"\5e\63\2\u0217\u021a\5e\63\2\u0218\u021a\5g\64\2\u0219\u0217\3\2\2\2\u0219"+
		"\u0218\3\2\2\2\u021a\u021d\3\2\2\2\u021b\u0219\3\2\2\2\u021b\u021c\3\2"+
		"\2\2\u021cj\3\2\2\2\u021d\u021b\3\2\2\2\u021e\u0220\5\u00d3j\2\u021f\u021e"+
		"\3\2\2\2\u021f\u0220\3\2\2\2\u0220\u0221\3\2\2\2\u0221\u0227\7)\2\2\u0222"+
		"\u0226\n\t\2\2\u0223\u0224\7)\2\2\u0224\u0226\7)\2\2\u0225\u0222\3\2\2"+
		"\2\u0225\u0223\3\2\2\2\u0226\u0229\3\2\2\2\u0227\u0225\3\2\2\2\u0227\u0228"+
		"\3\2\2\2\u0228\u022a\3\2\2\2\u0229\u0227\3\2\2\2\u022a\u022b\7)\2\2\u022b"+
		"l\3\2\2\2\u022c\u022d\7\62\2\2\u022d\u0231\5\u00e7t\2\u022e\u0230\5\u00b5"+
		"[\2\u022f\u022e\3\2\2\2\u0230\u0233\3\2\2\2\u0231\u022f\3\2\2\2\u0231"+
		"\u0232\3\2\2\2\u0232n\3\2\2\2\u0233\u0231\3\2\2\2\u0234\u0235\5\u00b3"+
		"Z\2\u0235p\3\2\2\2\u0236\u0240\5\u00b3Z\2\u0237\u0239\5\u00c1a\2\u0238"+
		"\u023a\t\n\2\2\u0239\u0238\3\2\2\2\u0239\u023a\3\2\2\2\u023a\u023c\3\2"+
		"\2\2\u023b\u023d\5\u00b7\\\2\u023c\u023b\3\2\2\2\u023d\u023e\3\2\2\2\u023e"+
		"\u023c\3\2\2\2\u023e\u023f\3\2\2\2\u023f\u0241\3\2\2\2\u0240\u0237\3\2"+
		"\2\2\u0240\u0241\3\2\2\2\u0241r\3\2\2\2\u0242\u0243\7?\2\2\u0243t\3\2"+
		"\2\2\u0244\u0245\7@\2\2\u0245v\3\2\2\2\u0246\u0247\7>\2\2\u0247x\3\2\2"+
		"\2\u0248\u0249\7#\2\2\u0249z\3\2\2\2\u024a\u024b\7-\2\2\u024b\u024c\7"+
		"?\2\2\u024c|\3\2\2\2\u024d\u024e\7/\2\2\u024e\u024f\7?\2\2\u024f~\3\2"+
		"\2\2\u0250\u0251\7,\2\2\u0251\u0252\7?\2\2\u0252\u0080\3\2\2\2\u0253\u0254"+
		"\7\61\2\2\u0254\u0255\7?\2\2\u0255\u0082\3\2\2\2\u0256\u0257\7\'\2\2\u0257"+
		"\u0258\7?\2\2\u0258\u0084\3\2\2\2\u0259\u025a\7(\2\2\u025a\u025b\7?\2"+
		"\2\u025b\u0086\3\2\2\2\u025c\u025d\7`\2\2\u025d\u025e\7?\2\2\u025e\u0088"+
		"\3\2\2\2\u025f\u0260\7~\2\2\u0260\u0261\7?\2\2\u0261\u008a\3\2\2\2\u0262"+
		"\u0263\7\60\2\2\u0263\u008c\3\2\2\2\u0264\u0265\7a\2\2\u0265\u008e\3\2"+
		"\2\2\u0266\u0267\7B\2\2\u0267\u0090\3\2\2\2\u0268\u0269\7%\2\2\u0269\u0092"+
		"\3\2\2\2\u026a\u026b\7&\2\2\u026b\u0094\3\2\2\2\u026c\u026d\7*\2\2\u026d"+
		"\u0096\3\2\2\2\u026e\u026f\7+\2\2\u026f\u0098\3\2\2\2\u0270\u0271\7.\2"+
		"\2\u0271\u009a\3\2\2\2\u0272\u0273\7=\2\2\u0273\u009c\3\2\2\2\u0274\u0275"+
		"\7<\2\2\u0275\u009e\3\2\2\2\u0276\u0277\7,\2\2\u0277\u00a0\3\2\2\2\u0278"+
		"\u0279\7\61\2\2\u0279\u00a2\3\2\2\2\u027a\u027b\7\'\2\2\u027b\u00a4\3"+
		"\2\2\2\u027c\u027d\7-\2\2\u027d\u00a6\3\2\2\2\u027e\u027f\7/\2\2\u027f"+
		"\u00a8\3\2\2\2\u0280\u0281\7\u0080\2\2\u0281\u00aa\3\2\2\2\u0282\u0283"+
		"\7~\2\2\u0283\u00ac\3\2\2\2\u0284\u0285\7(\2\2\u0285\u00ae\3\2\2\2\u0286"+
		"\u0287\7`\2\2\u0287\u00b0\3\2\2\2\u0288\u0289\t\7\2\2\u0289\u00b2\3\2"+
		"\2\2\u028a\u028c\5\u00b7\\\2\u028b\u028a\3\2\2\2\u028c\u028d\3\2\2\2\u028d"+
		"\u028b\3\2\2\2\u028d\u028e\3\2\2\2\u028e\u028f\3\2\2\2\u028f\u0291\7\60"+
		"\2\2\u0290\u0292\5\u00b7\\\2\u0291\u0290\3\2\2\2\u0292\u0293\3\2\2\2\u0293"+
		"\u0291\3\2\2\2\u0293\u0294\3\2\2\2\u0294\u02a3\3\2\2\2\u0295\u0297\5\u00b7"+
		"\\\2\u0296\u0295\3\2\2\2\u0297\u0298\3\2\2\2\u0298\u0296\3\2\2\2\u0298"+
		"\u0299\3\2\2\2\u0299\u029a\3\2\2\2\u029a\u029b\7\60\2\2\u029b\u02a3\3"+
		"\2\2\2\u029c\u029e\7\60\2\2\u029d\u029f\5\u00b7\\\2\u029e\u029d\3\2\2"+
		"\2\u029f\u02a0\3\2\2\2\u02a0\u029e\3\2\2\2\u02a0\u02a1\3\2\2\2\u02a1\u02a3"+
		"\3\2\2\2\u02a2\u028b\3\2\2\2\u02a2\u0296\3\2\2\2\u02a2\u029c\3\2\2\2\u02a3"+
		"\u00b4\3\2\2\2\u02a4\u02a5\t\13\2\2\u02a5\u00b6\3\2\2\2\u02a6\u02a7\t"+
		"\b\2\2\u02a7\u00b8\3\2\2\2\u02a8\u02a9\t\f\2\2\u02a9\u00ba\3\2\2\2\u02aa"+
		"\u02ab\t\r\2\2\u02ab\u00bc\3\2\2\2\u02ac\u02ad\t\16\2\2\u02ad\u00be\3"+
		"\2\2\2\u02ae\u02af\t\17\2\2\u02af\u00c0\3\2\2\2\u02b0\u02b1\t\20\2\2\u02b1"+
		"\u00c2\3\2\2\2\u02b2\u02b3\t\21\2\2\u02b3\u00c4\3\2\2\2\u02b4\u02b5\t"+
		"\22\2\2\u02b5\u00c6\3\2\2\2\u02b6\u02b7\t\23\2\2\u02b7\u00c8\3\2\2\2\u02b8"+
		"\u02b9\t\24\2\2\u02b9\u00ca\3\2\2\2\u02ba\u02bb\t\25\2\2\u02bb\u00cc\3"+
		"\2\2\2\u02bc\u02bd\t\26\2\2\u02bd\u00ce\3\2\2\2\u02be\u02bf\t\27\2\2\u02bf"+
		"\u00d0\3\2\2\2\u02c0\u02c1\t\30\2\2\u02c1\u00d2\3\2\2\2\u02c2\u02c3\t"+
		"\31\2\2\u02c3\u00d4\3\2\2\2\u02c4\u02c5\t\32\2\2\u02c5\u00d6\3\2\2\2\u02c6"+
		"\u02c7\t\33\2\2\u02c7\u00d8\3\2\2\2\u02c8\u02c9\t\34\2\2\u02c9\u00da\3"+
		"\2\2\2\u02ca\u02cb\t\35\2\2\u02cb\u00dc\3\2\2\2\u02cc\u02cd\t\36\2\2\u02cd"+
		"\u00de\3\2\2\2\u02ce\u02cf\t\37\2\2\u02cf\u00e0\3\2\2\2\u02d0\u02d1\t"+
		" \2\2\u02d1\u00e2\3\2\2\2\u02d2\u02d3\t!\2\2\u02d3\u00e4\3\2\2\2\u02d4"+
		"\u02d5\t\"\2\2\u02d5\u00e6\3\2\2\2\u02d6\u02d7\t#\2\2\u02d7\u00e8\3\2"+
		"\2\2\u02d8\u02d9\t$\2\2\u02d9\u00ea\3\2\2\2\u02da\u02db\t%\2\2\u02db\u00ec"+
		"\3\2\2\2\27\2\u01e0\u01ea\u01f8\u0201\u0209\u0210\u0219\u021b\u021f\u0225"+
		"\u0227\u0231\u0239\u023e\u0240\u028d\u0293\u0298\u02a0\u02a2\4\b\2\2\2"+
		"\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}