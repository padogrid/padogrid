// Generated from /Users/dpark/Work/git/Hazelcast/hazelcast-addon/src/main/resources/Hql.g4 by ANTLR 4.7.2
package org.hazelcast.addon.hql.internal.antlr4.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link HqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface HqlVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link HqlParser#hql_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHql_file(HqlParser.Hql_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#hql_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHql_clause(HqlParser.Hql_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#dml_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDml_clause(HqlParser.Dml_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#select_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_statement(HqlParser.Select_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#end_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnd_statement(HqlParser.End_statementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binary_operator_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinary_operator_expression(HqlParser.Binary_operator_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitive_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive_expression(HqlParser.Primitive_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bracket_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracket_expression(HqlParser.Bracket_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unary_operator_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_operator_expression(HqlParser.Unary_operator_expressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code column_ref_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_ref_expression(HqlParser.Column_ref_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#constant_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant_expression(HqlParser.Constant_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(HqlParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#search_condition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_list(HqlParser.Search_condition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#search_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition(HqlParser.Search_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#search_condition_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_and(HqlParser.Search_condition_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#or_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr_condition(HqlParser.Or_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#and_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd_condition(HqlParser.And_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#search_condition_not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition_not(HqlParser.Search_condition_notContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(HqlParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#lhs_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLhs_expression(HqlParser.Lhs_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#rhs_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRhs_expression(HqlParser.Rhs_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#query_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery_expression(HqlParser.Query_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#query_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery_specification(HqlParser.Query_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#select_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_clause(HqlParser.Select_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_clause(HqlParser.Order_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#order_by_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_expression(HqlParser.Order_by_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#select_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_list(HqlParser.Select_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#select_list_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_list_elem(HqlParser.Select_list_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#from_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_clause(HqlParser.From_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(HqlParser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#path_source}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_source(HqlParser.Path_sourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#path_source_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_source_item(HqlParser.Path_source_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#set_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_type(HqlParser.Set_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#as_path_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_path_alias(HqlParser.As_path_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#path_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_alias(HqlParser.Path_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#path_hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_hint(HqlParser.Path_hintContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#column_alias_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alias_list(HqlParser.Column_alias_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#column_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alias(HqlParser.Column_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(HqlParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#path_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_name(HqlParser.Path_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath(HqlParser.PathContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#full_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_column_name(HqlParser.Full_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#column_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name_list(HqlParser.Column_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(HqlParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#simple_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_name(HqlParser.Simple_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#null_notnull}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNull_notnull(HqlParser.Null_notnullContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#default_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_value(HqlParser.Default_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(HqlParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(HqlParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(HqlParser.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(HqlParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#simple_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_id(HqlParser.Simple_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordsCanBeId(HqlParser.KeywordsCanBeIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link HqlParser#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(HqlParser.Comparison_operatorContext ctx);
}