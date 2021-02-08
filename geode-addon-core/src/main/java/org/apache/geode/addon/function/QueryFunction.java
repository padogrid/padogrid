package org.apache.geode.addon.function;

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;

/**
 * QueryFunction executes the specified OQL query and returns the results as a list.
 * It takes a single query string as an argument.
 * 
 * <p>
 * <b>Arguments:</b>
 * <ul><li><b>queryStatement</b> String OQL query string</li></ul>
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryFunction implements Function, Declarable {
	private static final long serialVersionUID = 1L;

	public final static String ID = "addon.QueryFunction";

	private Cache cache;
	
	@Override
	public void execute(FunctionContext context) {
		QueryService queryService = cache.getQueryService();
		String qstr = (String)context.getArguments();
		try {
			Query query = queryService.newQuery(qstr);
			SelectResults result = (SelectResults) query.execute((RegionFunctionContext) context);
			context.getResultSender().lastResult(result.asList());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void initialize(Cache cache, Properties properties) {
		this.cache = cache;
	}
	
	@Override
	public String getId() {
		return ID;
	}
}