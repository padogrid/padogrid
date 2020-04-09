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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hazelcast.addon.hql.IPageResults;
import org.hazelcast.addon.hql.ResultType;

import com.hazelcast.map.IMap;
import com.hazelcast.query.PagingPredicate;

/**
 * PageResults contains a partial collection from executing an HQL query.
 * @author dpark
 *
 * @param <T> Object type in the result collection
 */
@SuppressWarnings("rawtypes")
public class PageResultsImpl<T>  implements IPageResults {

	private final IMap map;
	private Collection<T> results;
	private PagingPredicate pagingPredicate;
	private ResultType type = ResultType.VALUES;
	private int largestPageVisted = 0;
	private int largestPageVisitedSize = 0;

	/**
	 * Constructs a PageSet object containing the specified result set.
	 * 
	 * @param map             IMap object
	 * @param results         Page result set
	 * @param pagingPredicate Paging predicate
	 * @param type            Result set type
	 */
	public PageResultsImpl(IMap map, Collection<T> results, PagingPredicate pagingPredicate, ResultType type) {
		this.map = map;
		this.results = results;
		this.pagingPredicate = pagingPredicate;
		this.type = type;
	}
	
	/**
	 * Returns the result type.
	 */
	public ResultType getResultType()
	{
		return type;
	}

	/**
	 * Returns the page result collection.
	 */
	public Collection<T> getResults() {
		return results;
	}

	/**
	 * Returns PagingPredicate
	 */
	public PagingPredicate getPagingPredicate() {
		return pagingPredicate;
	}

	/**
	 * Returns the total size of largest page number visited so far.
	 * @return
	 */
	public int getVisitedTotalSize() {
		if (results == null) {
			return 0;
		}
		return largestPageVisitedSize + getLargestPageVisted() * getFetchSize();
	}

	/**
	 * Returns the results in the form of {@link List}.
	 */
	public List<T> toList() {
		if (pagingPredicate == null) {
			return new ArrayList();
		}
		return new ArrayList(results);
	}

	/**
	 * Returns the page results size. Note that the returned value may be less
	 * than the fetch size if the current page is the last page and its size is less
	 * than the fetch size.
	 */
	public int getSize() {
		if (results == null) {
			return 0;
		}
		return results.size();
	}

	/**
	 * Returns the page fetch size.
	 */
	public int getFetchSize() {
		if (pagingPredicate == null) {
			return 0;
		}
		return pagingPredicate.getPageSize();
	}

	/**
	 * Executes pagingPredicate and returns the result set.
	 */
	@SuppressWarnings("unchecked")
	private Collection<T> __getResults() {
		switch (type) {
		case KEYS:
			results = map.keySet(pagingPredicate);
			break;
		case VALUES:
			results = map.values(pagingPredicate);
			break;
		case KEYS_VALUES:
			results = map.entrySet(pagingPredicate);
			break;
		}
		return results;
	}

	/**
	 * Advances to the next page of results.
	 * 
	 * @return Returns false if the current page is the last page.
	 */
	public boolean nextPage() {
		pagingPredicate.nextPage();
		results = __getResults();
		boolean pageExists = results.size() > 0;
		if (pageExists) {
			if (largestPageVisted < getPage()) {
				largestPageVisted = getPage();
				largestPageVisitedSize = getSize();
			}
		}
		return pageExists;
	}

	/**
	 * Advances to the previous page of results.
	 * 
	 * @return Returns false if the current page is the first page.
	 */
	public boolean previousPage() {
		pagingPredicate.previousPage();
		results = __getResults();
		boolean pageExists = results.size() > 0;
		if (pageExists) {
			if (largestPageVisted < getPage()) {
				largestPageVisted = getPage();
				largestPageVisitedSize = getSize();
			}
		}
		return pageExists;
	}

	/**
	 * Moves to the specified page.
	 * 
	 * @param pageNumber Page number. Page number begins from 0.
	 * @return true if the page exists
	 */
	public boolean setPage(int pageNumber) {
		pagingPredicate.setPage(pageNumber);
		results = __getResults();
		boolean pageExists = results.size() > 0;
		if (pageExists) {
			if (largestPageVisted < getPage()) {
				largestPageVisted = getPage();
				largestPageVisitedSize = getSize();
			}
		}
		return pageExists;
	}

	/**
	 * Returns the page number. The page number begins from 0.
	 */
	public int getPage() {
		if (pagingPredicate == null) {
			return 0;
		}
		return pagingPredicate.getPage();
	}

	/**
	 * Returns the entire set start index number. For example, if the current page
	 * number is 2, the fetch size is 100, and the current page size is greater than
	 * 0, then the start index is (2*200) or 200. Index number begins from 0.
	 * 
	 * @return Overall start index number. -1 if there are no overall results.
	 */
	public int getStartIndex() {
		if (results == null || results.size() == 0) {
			return -1;
		}
		if (pagingPredicate == null) {
			return 0;
		}
		return pagingPredicate.getPage() * getFetchSize();
	}

	/**
	 * Returns the entire set end index number of the current page. For example, if the
	 * current page number is 2, the fetch size 100, and the result set size is 15,
	 * then the end index is (2*100+20-1) or 219. Index number begins from 0.
	 * 
	 * @return Entire set end index number. -1 if there no results.
	 */
	public int getEndIndex() {
		int startIndex = getStartIndex();
		if (startIndex == -1) {
			return -1;
		}
		return startIndex + getSize() - 1;
	}
	
	/**
	 * Returns the largest page number it has visited so far.
	 */
	public int getLargestPageVisted() {
		return largestPageVisted;
	}

	/**
	 * Returns true if the current page is the last page.
	 */
	public boolean isLastPage() {
		return results.size() < getFetchSize();
	}
	
	/**
	 * Returns the size of the largest page visited.
	 */
	public int getLargestPageVisitedSize() {
		return largestPageVisitedSize;
	}
	
	/**
	 * Dumps the page results to sysout.
	 */
	public void dump() {
		int index = getStartIndex();
		switch (type) {
		case KEYS_VALUES:
			for (T obj: results) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
				System.out.println(index++ + ". key=" + entry.getKey() + ", value=" + entry.getValue());
			}
			break;
		default:
			for (T obj : results) {
				System.out.println(index++ + ". " + obj);
			}
			break;
		}	
	}
}
