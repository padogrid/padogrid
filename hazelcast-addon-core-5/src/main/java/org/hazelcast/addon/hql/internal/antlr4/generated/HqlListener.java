// Generated from /Users/dpark/Work/git/Hazelcast/hazelcast-addon/src/main/resources/Hql.g4 by ANTLR 4.7.2
package org.hazelcast.addon.hql.internal.antlr4.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link HqlParser}.
 */
public interface HqlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link HqlParser#hql_file}.
	 * @param ctx the parse tree
	 */
	void enterHql_file(HqlParser.Hql_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#hql_file}.
	 * @param ctx the parse tree
	 */
	void exitHql_file(HqlParser.Hql_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#hql_clause}.
	 * @param ctx the parse tree
	 */
	void enterHql_clause(HqlParser.Hql_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#hql_clause}.
	 * @param ctx the parse tree
	 */
	void exitHql_clause(HqlParser.Hql_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#dml_clause}.
	 * @param ctx the parse tree
	 */
	void enterDml_clause(HqlParser.Dml_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#dml_clause}.
	 * @param ctx the parse tree
	 */
	void exitDml_clause(HqlParser.Dml_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void enterSelect_statement(HqlParser.Select_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void exitSelect_statement(HqlParser.Select_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#end_statement}.
	 * @param ctx the parse tree
	 */
	void enterEnd_statement(HqlParser.End_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#end_statement}.
	 * @param ctx the parse tree
	 */
	void exitEnd_statement(HqlParser.End_statementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binary_operator_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinary_operator_expression(HqlParser.Binary_operator_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binary_operator_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinary_operator_expression(HqlParser.Binary_operator_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitive_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimitive_expression(HqlParser.Primitive_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitive_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimitive_expression(HqlParser.Primitive_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bracket_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBracket_expression(HqlParser.Bracket_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bracket_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBracket_expression(HqlParser.Bracket_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unary_operator_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnary_operator_expression(HqlParser.Unary_operator_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unary_operator_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnary_operator_expression(HqlParser.Unary_operator_expressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code column_ref_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterColumn_ref_expression(HqlParser.Column_ref_expressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code column_ref_expression}
	 * labeled alternative in {@link HqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitColumn_ref_expression(HqlParser.Column_ref_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#constant_expression}.
	 * @param ctx the parse tree
	 */
	void enterConstant_expression(HqlParser.Constant_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#constant_expression}.
	 * @param ctx the parse tree
	 */
	void exitConstant_expression(HqlParser.Constant_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(HqlParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(HqlParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#search_condition_list}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_list(HqlParser.Search_condition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#search_condition_list}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_list(HqlParser.Search_condition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#search_condition}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition(HqlParser.Search_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#search_condition}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition(HqlParser.Search_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_and(HqlParser.Search_condition_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#search_condition_and}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_and(HqlParser.Search_condition_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#or_condition}.
	 * @param ctx the parse tree
	 */
	void enterOr_condition(HqlParser.Or_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#or_condition}.
	 * @param ctx the parse tree
	 */
	void exitOr_condition(HqlParser.Or_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#and_condition}.
	 * @param ctx the parse tree
	 */
	void enterAnd_condition(HqlParser.And_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#and_condition}.
	 * @param ctx the parse tree
	 */
	void exitAnd_condition(HqlParser.And_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition_not(HqlParser.Search_condition_notContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#search_condition_not}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition_not(HqlParser.Search_condition_notContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(HqlParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(HqlParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#lhs_expression}.
	 * @param ctx the parse tree
	 */
	void enterLhs_expression(HqlParser.Lhs_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#lhs_expression}.
	 * @param ctx the parse tree
	 */
	void exitLhs_expression(HqlParser.Lhs_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#rhs_expression}.
	 * @param ctx the parse tree
	 */
	void enterRhs_expression(HqlParser.Rhs_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#rhs_expression}.
	 * @param ctx the parse tree
	 */
	void exitRhs_expression(HqlParser.Rhs_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#query_expression}.
	 * @param ctx the parse tree
	 */
	void enterQuery_expression(HqlParser.Query_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#query_expression}.
	 * @param ctx the parse tree
	 */
	void exitQuery_expression(HqlParser.Query_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#query_specification}.
	 * @param ctx the parse tree
	 */
	void enterQuery_specification(HqlParser.Query_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#query_specification}.
	 * @param ctx the parse tree
	 */
	void exitQuery_specification(HqlParser.Query_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void enterSelect_clause(HqlParser.Select_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void exitSelect_clause(HqlParser.Select_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause(HqlParser.Order_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause(HqlParser.Order_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#order_by_expression}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_expression(HqlParser.Order_by_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#order_by_expression}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_expression(HqlParser.Order_by_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#select_list}.
	 * @param ctx the parse tree
	 */
	void enterSelect_list(HqlParser.Select_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#select_list}.
	 * @param ctx the parse tree
	 */
	void exitSelect_list(HqlParser.Select_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#select_list_elem}.
	 * @param ctx the parse tree
	 */
	void enterSelect_list_elem(HqlParser.Select_list_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#select_list_elem}.
	 * @param ctx the parse tree
	 */
	void exitSelect_list_elem(HqlParser.Select_list_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(HqlParser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(HqlParser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(HqlParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(HqlParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#path_source}.
	 * @param ctx the parse tree
	 */
	void enterPath_source(HqlParser.Path_sourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#path_source}.
	 * @param ctx the parse tree
	 */
	void exitPath_source(HqlParser.Path_sourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#path_source_item}.
	 * @param ctx the parse tree
	 */
	void enterPath_source_item(HqlParser.Path_source_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#path_source_item}.
	 * @param ctx the parse tree
	 */
	void exitPath_source_item(HqlParser.Path_source_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#set_type}.
	 * @param ctx the parse tree
	 */
	void enterSet_type(HqlParser.Set_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#set_type}.
	 * @param ctx the parse tree
	 */
	void exitSet_type(HqlParser.Set_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#as_path_alias}.
	 * @param ctx the parse tree
	 */
	void enterAs_path_alias(HqlParser.As_path_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#as_path_alias}.
	 * @param ctx the parse tree
	 */
	void exitAs_path_alias(HqlParser.As_path_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#path_alias}.
	 * @param ctx the parse tree
	 */
	void enterPath_alias(HqlParser.Path_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#path_alias}.
	 * @param ctx the parse tree
	 */
	void exitPath_alias(HqlParser.Path_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#path_hint}.
	 * @param ctx the parse tree
	 */
	void enterPath_hint(HqlParser.Path_hintContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#path_hint}.
	 * @param ctx the parse tree
	 */
	void exitPath_hint(HqlParser.Path_hintContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#column_alias_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias_list(HqlParser.Column_alias_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#column_alias_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias_list(HqlParser.Column_alias_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(HqlParser.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(HqlParser.Column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(HqlParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(HqlParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#path_name}.
	 * @param ctx the parse tree
	 */
	void enterPath_name(HqlParser.Path_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#path_name}.
	 * @param ctx the parse tree
	 */
	void exitPath_name(HqlParser.Path_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#path}.
	 * @param ctx the parse tree
	 */
	void enterPath(HqlParser.PathContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#path}.
	 * @param ctx the parse tree
	 */
	void exitPath(HqlParser.PathContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#full_column_name}.
	 * @param ctx the parse tree
	 */
	void enterFull_column_name(HqlParser.Full_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#full_column_name}.
	 * @param ctx the parse tree
	 */
	void exitFull_column_name(HqlParser.Full_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_list(HqlParser.Column_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_list(HqlParser.Column_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(HqlParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(HqlParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#simple_name}.
	 * @param ctx the parse tree
	 */
	void enterSimple_name(HqlParser.Simple_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#simple_name}.
	 * @param ctx the parse tree
	 */
	void exitSimple_name(HqlParser.Simple_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#null_notnull}.
	 * @param ctx the parse tree
	 */
	void enterNull_notnull(HqlParser.Null_notnullContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#null_notnull}.
	 * @param ctx the parse tree
	 */
	void exitNull_notnull(HqlParser.Null_notnullContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#default_value}.
	 * @param ctx the parse tree
	 */
	void enterDefault_value(HqlParser.Default_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#default_value}.
	 * @param ctx the parse tree
	 */
	void exitDefault_value(HqlParser.Default_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(HqlParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(HqlParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(HqlParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(HqlParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(HqlParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(HqlParser.SignContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(HqlParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(HqlParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#simple_id}.
	 * @param ctx the parse tree
	 */
	void enterSimple_id(HqlParser.Simple_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#simple_id}.
	 * @param ctx the parse tree
	 */
	void exitSimple_id(HqlParser.Simple_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 */
	void enterKeywordsCanBeId(HqlParser.KeywordsCanBeIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 */
	void exitKeywordsCanBeId(HqlParser.KeywordsCanBeIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link HqlParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(HqlParser.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link HqlParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(HqlParser.Comparison_operatorContext ctx);
}