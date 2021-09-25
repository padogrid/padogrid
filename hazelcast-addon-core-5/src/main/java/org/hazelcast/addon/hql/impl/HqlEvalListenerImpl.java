/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hazelcast.addon.hql.impl;

import org.antlr.v4.runtime.misc.Interval;
import org.hazelcast.addon.exception.HqlException;
import org.hazelcast.addon.hql.ResultType;
import org.hazelcast.addon.hql.internal.antlr4.generated.HqlBaseListener;
import org.hazelcast.addon.hql.internal.antlr4.generated.HqlParser;

import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.SqlPredicate;

/**
 * HqlEvalListenerImpl parses HQL queries and builds {@link HqlContext} that
 * contain context information needed for creating {@link SqlPredicate} and
 * {@link PagingPredicate}.
 * 
 * @author dpark
 *
 */
public class HqlEvalListenerImpl extends HqlBaseListener {

	enum ClauseType {
		UNDEFINED, SELECT, FROM, WHERE, ORDER_BY
	}

	private boolean isDebug = false;
	private HqlContext context;
	private ClauseType clauseType = ClauseType.UNDEFINED;
	private StringBuilder predicateBuilder = new StringBuilder();

	// Temporary fields used to extract out order-by expression information
	SearchType searchType;
	String columnName;

	public HqlEvalListenerImpl() {
		this.context = new HqlContext();
	}

	public HqlContext getHqlContext() {
		return context;
	}

	public void dump() {
		context.dump();
	}

	private void println(Object obj) {
		if (isDebug) {
			System.out.println(obj);
		}
	}

	@Override
	public void enterSelect_clause(HqlParser.Select_clauseContext ctx) {
		clauseType = ClauseType.SELECT;
	}

	@Override
	public void exitSelect_clause(HqlParser.Select_clauseContext ctx) {
		clauseType = ClauseType.UNDEFINED;
	}

	@Override
	public void enterWhere_clause(HqlParser.Where_clauseContext ctx) {
		clauseType = ClauseType.WHERE;
	}

	@Override
	public void exitWhere_clause(HqlParser.Where_clauseContext ctx) {
		clauseType = ClauseType.UNDEFINED;
	}

	@Override
	public void enterFrom_clause(HqlParser.From_clauseContext ctx) {
		clauseType = ClauseType.FROM;
	}

	@Override
	public void exitFrom_clause(HqlParser.From_clauseContext ctx) {
		clauseType = ClauseType.UNDEFINED;
	}

	@Override
	public void enterOrder_by_clause(HqlParser.Order_by_clauseContext ctx) {
		clauseType = ClauseType.ORDER_BY;
	}

	@Override
	public void exitOrder_by_clause(HqlParser.Order_by_clauseContext ctx) {
		clauseType = ClauseType.UNDEFINED;
	}

	@Override
	public void exitPath_source_item(HqlParser.Path_source_itemContext ctx) {
		String path = ctx.path().getText();
		context.setPath(path);
	}

	@Override
	public void exitSet_type(HqlParser.Set_typeContext ctx) {
		if (ctx.KEYS() != null) {
			context.setResultType(ResultType.KEYS);
		} else if (ctx.ENTRIES() != null) {
			context.setResultType(ResultType.KEYS_VALUES);
		} else {
			context.setResultType(ResultType.VALUES);
		}
	}

	@Override
	public void exitPath_alias(HqlParser.Path_aliasContext ctx) {
		String pathAlias = ctx.getText();
		context.setPathAlias(pathAlias);
	}

	@Override
	public void enterFull_column_name(HqlParser.Full_column_nameContext ctx) {

	}

	@Override
	public void exitFull_column_name(HqlParser.Full_column_nameContext ctx) {
		switch (clauseType) {
		case WHERE:
			handleWhereClauseFullColumnName(ctx);
			break;
		case ORDER_BY:
			handleOrderByClauseFullColumnName(ctx);
			break;
		default:
			break;
		}
	}

	private void handleWhereClauseFullColumnName(HqlParser.Full_column_nameContext ctx) {
		if (context.getResultType() == ResultType.KEYS_VALUES) {
			if (context.isPathAlias() && ctx.path() == null) {
				throw new HqlException("Invalid query: No map reference in where clause [" + ctx.getText() + "]");
			}

			if (context.isPathAlias() == false && ctx.path() != null) {
				throw new HqlException(
						"Invalid query: Reference to undefined map alias in where clause [" + ctx.getText() + "]");
			}
			if (ctx.KEY() != null) {
				if (ctx.column_name() == null) {
					buildSpaceAfter("__key");
				} else {
					throw new HqlException(
							"Invalid query: KEY field not allowed in the where clause for the type ENTRIES ["
									+ ctx.getText() + "]");
				}
			} else if (ctx.VALUE() != null) {
				if (ctx.column_name() == null) {
					throw new HqlException("Invalid query: VALUE field missing [" + ctx.getText() + "]");
				} else {
					buildSpaceAfter(ctx.column_name().getText());
				}
			} else {
				if (ctx.DOT() == null) {
					if (context.getPathAlias() == null) {
						buildSpaceAfter(ctx.column_name().getText());
					} else {
						throw new HqlException("Invalid query: KEY or VALUE type missing [" + ctx.getText() + "]");
					}
				} else {
					throw new HqlException("Invalid query: KEY or VALUE type missing [" + ctx.getText() + "]");
				}
			}
		} else {
			if (context.isPathAlias()) {
				if (ctx.KEY() != null) {
					if (ctx.path() == null) {
						throw new HqlException(
								"Invalid query: No map reference in the where clause [" + ctx.getText() + "]");
					}
					if (ctx.column_name() != null) {
						columnName = "key." + ctx.column_name().getText();
					} else {
						columnName = "key";
					}
				} else if (ctx.VALUE() != null) {
					if (ctx.path() == null) {
						throw new HqlException(
								"Invalid query: No map reference in the where clause [" + ctx.getText() + "]");
					}
					if (ctx.column_name() != null) {
						columnName = "value." + ctx.column_name().getText();
					} else {
						columnName = "value";
					}
				} else {
					if (ctx.path() == null) {
						if (ctx.column_name() != null) {
							if (context.getPathAlias().equals(ctx.column_name().getText())) {
								if (context.getResultType() == ResultType.KEYS) {
									columnName = "__key";
								} else {
									throw new HqlException(
											"Invalid query: Key object comparison not allowed [" + ctx.getText() + "]");
								}
							} else if (ctx.column_name().DOT() != null) {
								columnName = ctx.column_name().id(1).getText();
							}
						} else {
							throw new HqlException("Invalid query: Map alias not specified [" + ctx.getText() + "]");
						}
					}
				}
			} else {
				columnName = ctx.getText();
			}
			buildSpaceAfter(columnName);
		}
	}

	private void handleOrderByClauseFullColumnName(HqlParser.Full_column_nameContext ctx) {
		columnName = null;

		if (context.getResultType() == ResultType.KEYS_VALUES) {
			if (context.isPathAlias() && ctx.path() == null) {
				throw new HqlException("Invalid query: No map reference in order by [" + ctx.getText() + "]");
			}

			if (context.isPathAlias() == false && ctx.path() != null) {
				throw new HqlException(
						"Invalid query: Reference to undefined map alias in order by [" + ctx.getText() + "]");
			}
			if (ctx.KEY() != null) {
				if (context.isPathAlias() && ctx.path() == null) {
					throw new HqlException(
							"Invalid query: Map alias not specified in order by [" + ctx.getText() + "]");
				}
			}

			if (columnName == null && ctx.column_name() != null) {
				columnName = ctx.column_name().getText();
			}
			if (columnName == null) {
				if (ctx.KEY() != null) {
					searchType = SearchType.KEY_OBJECT;
				} else {
					searchType = SearchType.VALUE_OBJECT;
				}
			} else {
				if (ctx.column_name().DOT() != null) {
					// parser takes care of incomplete column name, e.g., "v."
					columnName = columnName.substring(columnName.indexOf('.') + 1);
				} else if (columnName.equals(context.getPathAlias())) {
					columnName = null;
					switch (context.getResultType()) {
					case KEYS:
						searchType = SearchType.KEY_OBJECT;
						break;
					default:
						searchType = SearchType.VALUE_OBJECT;
						break;
					}
				} else {
					if (context.getResultType() == ResultType.KEYS) {
						searchType = SearchType.KEY_FIELD;
					} else {
						if (ctx.KEY() != null) {
							searchType = SearchType.KEY_FIELD;
						} else {
							searchType = SearchType.VALUE_FIELD;
						}
					}
				}
			}

		} else {
			if (context.isPathAlias()) {
				if (ctx.KEY() != null) {
					if (ctx.path() == null) {
						throw new HqlException("Invalid query: No map reference in order by [" + ctx.getText() + "]");
					}
					columnName = "key." + ctx.column_name().getText();
					searchType = SearchType.VALUE_FIELD;
				} else if (ctx.VALUE() != null) {
					if (ctx.path() == null) {
						throw new HqlException("Invalid query: No map reference in order by [" + ctx.getText() + "]");
					}
					columnName = "value." + ctx.column_name().getText();
					searchType = SearchType.VALUE_FIELD;
				} else {
					if (ctx.DOT() == null) {
						searchType = SearchType.VALUE_FIELD;
					} else if (ctx.path() == null) {
						if (ctx.column_name().DOT() != null) {
							columnName = ctx.column_name().id(1).getText();
							searchType = SearchType.VALUE_FIELD;
						} else {
							if (ctx.getText().equals(context.getPathAlias())) {
								columnName = null;
								searchType = SearchType.VALUE_OBJECT;
							} else {
								throw new HqlException(
										"Invalid query: No map reference in order by [" + ctx.getText() + "]");
							}
						}
					} else {
						searchType = SearchType.VALUE_FIELD;
					}
				}
			} else {
				columnName = ctx.getText();
				searchType = SearchType.VALUE_FIELD;
			}

			if (context.getResultType() == ResultType.KEYS) {
				if (searchType == SearchType.VALUE_FIELD) {
					searchType = SearchType.KEY_FIELD;
				} else {
					searchType = SearchType.KEY_OBJECT;
				}
			}
		}

	}

	@Override
	public void enterOrder_by_expression(HqlParser.Order_by_expressionContext ctx) {
		searchType = SearchType.VALUE_FIELD;
		columnName = null;
	}

	@Override
	public void exitOrder_by_expression(HqlParser.Order_by_expressionContext ctx) {
		context.addOrderByExpression(columnName, searchType, ctx.DESC() == null);
	}

	private void buildSpaceAfter(String token) {
		predicateBuilder.append(token);
		predicateBuilder.append(" ");
	}

	@Override
	public void enterOr_condition(HqlParser.Or_conditionContext ctx) {
		buildSpaceAfter(ctx.OR().getText());
	}

	@Override
	public void enterAnd_condition(HqlParser.And_conditionContext ctx) {
		buildSpaceAfter(ctx.AND().getText());
	}

	@Override
	public void exitRhs_expression(HqlParser.Rhs_expressionContext ctx) {
		int a = ctx.start.getStartIndex();
		int b = ctx.stop.getStopIndex();
		Interval interval = new Interval(a, b);
		String rhs = ctx.getStart().getInputStream().getText(interval);
		buildSpaceAfter(rhs);
	}

	@Override
	public void exitQuery_specification(HqlParser.Query_specificationContext ctx) {
		context.setWhereClause(predicateBuilder.toString().trim());
		println("exitQuery_specification(): whereClause=" + context.getWhereClause());
	}

}
