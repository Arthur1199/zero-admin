package com.zero.utils.sql;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/19 23:53
 * 说明: 将 SQL 按常见子句换行和缩进，供日志和人工排查使用。
 *     按词元排版，保留参数占位符。每次调用独立保存排版状态
 */
public class SQLFormatter {

	/** 由多个词组成的子句起始关键字。 */
	private static final Set<String> BEGIN_CLAUSES = new HashSet<String>();
	/** 结束子句标题并开始排版其内容的关键字。 */
	private static final Set<String> END_CLAUSES = new HashSet<String>();
	/** 用于拆分条件或 CASE 分支的关键字。 */
	private static final Set<String> LOGICAL = new HashSet<String>();
	/** 排除在函数名识别之外的量词关键字。 */
	private static final Set<String> QUANTIFIERS = new HashSet<String>();
	/** 需要特殊排版的写操作关键字。 */
	private static final Set<String> DML = new HashSet<String>();
	/** 由独立分支处理且不视作函数名的关键字。 */
	private static final Set<String> MISC = new HashSet<String>();
	/** 分词和排版识别的空白字符。 */
	public static final String WHITESPACE = " \n\r\f\t";
	static {
		BEGIN_CLAUSES.add( "left" );
		BEGIN_CLAUSES.add( "right" );
		BEGIN_CLAUSES.add( "inner" );
		BEGIN_CLAUSES.add( "outer" );
		BEGIN_CLAUSES.add( "group" );
		BEGIN_CLAUSES.add( "order" );

		END_CLAUSES.add( "where" );
		END_CLAUSES.add( "set" );
		END_CLAUSES.add( "having" );
		END_CLAUSES.add( "join" );
		END_CLAUSES.add( "from" );
		END_CLAUSES.add( "by" );
		END_CLAUSES.add( "join" );
		END_CLAUSES.add( "into" );
		END_CLAUSES.add( "union" );

		LOGICAL.add( "and" );
		LOGICAL.add( "or" );
		LOGICAL.add( "when" );
		LOGICAL.add( "else" );
		LOGICAL.add( "end" );

		QUANTIFIERS.add( "in" );
		QUANTIFIERS.add( "all" );
		QUANTIFIERS.add( "exists" );
		QUANTIFIERS.add( "some" );
		QUANTIFIERS.add( "any" );

		DML.add( "insert" );
		DML.add( "update" );
		DML.add( "delete" );

		MISC.add( "select" );
		MISC.add( "on" );
	}

	/** 每一级缩进使用四个空格。 */
	static final String indentString = "    ";
	/** 格式化结果的首行缩进。 */
	static final String initial = "\n    ";
	/** 格式化结果的结束换行。 */
	static final String endial = "\n";

	/**
	 *
	 * 作者: Jack
	 * 时间: 2017/8/26 16:46
	 * 说明: 按常见 SQL 子句重新排版，调用方可在词元无法解析时保留原文。
	 * @param source 非 null 的 SQL 文本，可含 JDBC 参数占位符
	 * @return 换行和缩进后的 SQL 文本
	 * 返回值: String
	 * 返回值说明: 供日志展示，保留占位符
	 */
	public static String format(String source) {
		return new FormatProcess( source ).perform();
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2017/8/26 16:46
	 * 说明: 保存单次格式化所需的分词游标、子句状态和缩进层级。
	 */
	@SuppressWarnings("unused")
	private static class FormatProcess {
		boolean beginLine = true;
		boolean afterBeginBeforeEnd = false;
		boolean afterByOrSetOrFromOrSelect = false;
		boolean afterValues = false;
		boolean afterOn = false;
		boolean afterBetween = false;
		boolean afterInsert = false;
		int inFunction = 0;
		int parensSinceSelect = 0;
		private LinkedList<Integer> parenCounts = new LinkedList<Integer>();
		private LinkedList<Boolean> afterByOrFromOrSelects = new LinkedList<Boolean>();

		int indent = 1;

		StringBuilder result = new StringBuilder();
		StringTokenizer tokens;
		String lastToken;
		String token;
		String lcToken;

		/**
		 *
		 * 作者: Jack
		 * 时间: 2017/8/26 16:46
		 * 说明: 初始化 SQL 分词器，保留标点和空白以判断排版边界。
		 * @param sql 非 null 的原始 SQL 文本
		 */
		public FormatProcess(String sql) {
			tokens = new StringTokenizer(
					sql,
					"()+*/-=<>'`\"[]," + WHITESPACE,
					true
			);
		}

		/**
		 *
		 * 作者: Jack
		 * 时间: 2017/8/26 16:46
		 * 说明: 按词元遍历 SQL，根据子句和括号层级生成排版文本。
		 * @return 本次生成的 SQL 文本
		 * 返回值: String
		 * 返回值说明: 包含排版后的换行和缩进
		 */
		public String perform() {

			result.append( initial );

			while ( tokens.hasMoreTokens() ) {
				token = tokens.nextToken();
				lcToken = token.toLowerCase();

				if ( "'".equals( token ) ) {
					String t;
					do {
						t = tokens.nextToken();
						token += t;
					}
					while ( !"'".equals( t ) && tokens.hasMoreTokens() ); // cannot handle single quotes
				}
				else if ( "\"".equals( token ) ) {
					String t;
					do {
						t = tokens.nextToken();
						token += t;
					}
					while ( !"\"".equals( t ) );
				}

				if ( afterByOrSetOrFromOrSelect && ",".equals( token ) ) {
					commaAfterByOrFromOrSelect();
				}
				else if ( afterOn && ",".equals( token ) ) {
					commaAfterOn();
				}

				else if ( "(".equals( token ) ) {
					openParen();
				}
				else if ( ")".equals( token ) ) {
					closeParen();
				}

				else if ( BEGIN_CLAUSES.contains( lcToken ) ) {
					beginNewClause();
				}

				else if ( END_CLAUSES.contains( lcToken ) ) {
					endNewClause();
				}

				else if ( "select".equals( lcToken ) ) {
					select();
				}

				else if ( DML.contains( lcToken ) ) {
					updateOrInsertOrDelete();
				}

				else if ( "values".equals( lcToken ) ) {
					values();
				}

				else if ( "on".equals( lcToken ) ) {
					on();
				}

				else if ( afterBetween && lcToken.equals( "and" ) ) {
					misc();
					afterBetween = false;
				}

				else if ( LOGICAL.contains( lcToken ) ) {
					logical();
				}

				else if ( isWhitespace( token ) ) {
					white();
				}

				else {
					misc();
				}

				if ( !isWhitespace( token ) ) {
					lastToken = lcToken;
				}

			}
			result.append(endial);
			return result.toString();
		}

		private void commaAfterOn() {
			out();
			indent--;
			newline();
			afterOn = false;
			afterByOrSetOrFromOrSelect = true;
		}

		private void commaAfterByOrFromOrSelect() {
			out();
			newline();
		}

		private void logical() {
			if ( "end".equals( lcToken ) ) {
				indent--;
			}
			newline();
			out();
			beginLine = false;
		}

		private void on() {
			indent++;
			afterOn = true;
			newline();
			out();
			beginLine = false;
		}

		private void misc() {
			out();
			if ( "between".equals( lcToken ) ) {
				afterBetween = true;
			}
			if ( afterInsert ) {
				newline();
				afterInsert = false;
			}
			else {
				beginLine = false;
				if ( "case".equals( lcToken ) ) {
					indent++;
				}
			}
		}

		private void white() {
			if ( !beginLine ) {
				result.append( " " );
			}
		}

		private void updateOrInsertOrDelete() {
			out();
			indent++;
			beginLine = false;
			if ( "update".equals( lcToken ) ) {
				newline();
			}
			if ( "insert".equals( lcToken ) ) {
				afterInsert = true;
			}
		}

		private void select() {
			out();
			indent++;
			newline();
			parenCounts.addLast( Integer.valueOf( parensSinceSelect ) );
			afterByOrFromOrSelects.addLast( Boolean.valueOf( afterByOrSetOrFromOrSelect ) );
			parensSinceSelect = 0;
			afterByOrSetOrFromOrSelect = true;
		}

		private void out() {
			result.append( token );
		}

		private void endNewClause() {
			if ( !afterBeginBeforeEnd ) {
				indent--;
				if ( afterOn ) {
					indent--;
					afterOn = false;
				}
				newline();
			}
			out();
			if ( !"union".equals( lcToken ) ) {
				indent++;
			}
			newline();
			afterBeginBeforeEnd = false;
			afterByOrSetOrFromOrSelect = "by".equals( lcToken )
					|| "set".equals( lcToken )
					|| "from".equals( lcToken );
		}

		private void beginNewClause() {
			if ( !afterBeginBeforeEnd ) {
				if ( afterOn ) {
					indent--;
					afterOn = false;
				}
				indent--;
				newline();
			}
			out();
			beginLine = false;
			afterBeginBeforeEnd = true;
		}

		private void values() {
			indent--;
			newline();
			out();
			indent++;
			newline();
			afterValues = true;
		}

		private void closeParen() {
			parensSinceSelect--;
			if ( parensSinceSelect < 0 ) {
				indent--;
				parensSinceSelect = parenCounts.removeLast().intValue();
				afterByOrSetOrFromOrSelect = afterByOrFromOrSelects.removeLast().booleanValue();
			}
			if ( inFunction > 0 ) {
				inFunction--;
				out();
			}
			else {
				if ( !afterByOrSetOrFromOrSelect ) {
					indent--;
					newline();
				}
				out();
			}
			beginLine = false;
		}

		private void openParen() {
			if ( isFunctionName( lastToken ) || inFunction > 0 ) {
				inFunction++;
			}
			beginLine = false;
			if ( inFunction > 0 ) {
				out();
			}
			else {
				out();
				if ( !afterByOrSetOrFromOrSelect ) {
					indent++;
					newline();
					beginLine = true;
				}
			}
			parensSinceSelect++;
		}

		private static boolean isFunctionName(String tok) {
			final char begin = tok.charAt( 0 );
			final boolean isIdentifier = Character.isJavaIdentifierStart( begin ) || '"' == begin;
			return isIdentifier &&
					!LOGICAL.contains( tok ) &&
					!END_CLAUSES.contains( tok ) &&
					!QUANTIFIERS.contains( tok ) &&
					!DML.contains( tok ) &&
					!MISC.contains( tok );
		}

		private static boolean isWhitespace(String token) {
			return WHITESPACE.indexOf( token ) >= 0;
		}

		private void newline() {
			result.append( "\n" );
			for ( int i = 0; i < indent; i++ ) {
				result.append( indentString );
			}
			beginLine = true;
		}
	}

	/**
	 *
	 * 作者: Jack
	 * 时间: 2017/8/26 16:46
	 * 说明: 在控制台展示简单查询的格式化效果。
	 * @param args 命令行参数，示例不读取
	 */
	public static void main(String[] args) {
		String sql = SQLFormatter.format("SELECT * From t_sss");
		System.out.println(sql);
	}


}
