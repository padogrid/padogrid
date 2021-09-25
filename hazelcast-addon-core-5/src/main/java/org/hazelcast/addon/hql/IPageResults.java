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

package org.hazelcast.addon.hql;

import java.util.Collection;
import java.util.List;

import com.hazelcast.query.PagingPredicate;

/**
 * PageResults contains a partial collection from executing an HQL query.
 * @author dpark
 *
 * @param <T> Object type in the result collection
 */
@SuppressWarnings("rawtypes")
public interface IPageResults<T> {
	
	/**
	 * Returns the result type.
	 */
	public ResultType getResultType();
	

	/**
	 * Returns the page result collection.
	 */
	public Collection<T> getResults();

	/**
	 * Returns PagingPredicate
	 */
	public PagingPredicate getPagingPredicate();

	/**
	 * Returns the total size of largest page number visited so far.
	 * @return
	 */
	public int getVisitedTotalSize();

	/**
	 * Returns the results in the form of {@link List}.
	 */
	public List<T> toList();

	/**
	 * Returns the page results size. Note that the returned value may be less
	 * than the fetch size if the current page is the last page and its size is less
	 * than the fetch size.
	 */
	public int getSize();

	/**
	 * Returns the page fetch size.
	 */
	public int getFetchSize();

	/**
	 * Advances to the next page of results.
	 * 
	 * @return Returns false if the current page is the last page.
	 */
	public boolean nextPage();

	/**
	 * Advances to the previous page of results.
	 * 
	 * @return Returns false if the current page is the first page.
	 */
	public boolean previousPage();

	/**
	 * Moves to the specified page.
	 * 
	 * @param pageNumber Page number. Page number begins from 0.
	 * @return true if the page exists
	 */
	public boolean setPage(int pageNumber);

	/**
	 * Returns the page number. The page number begins from 0.
	 */
	public int getPage();

	/**
	 * Returns the entire set start index number. For example, if the current page
	 * number is 2, the fetch size is 100, and the current page size is greater than
	 * 0, then the start index is (2*200) or 200. Index number begins from 0.
	 * 
	 * @return Overall start index number. -1 if there are no overall results.
	 */
	public int getStartIndex();

	/**
	 * Returns the entire set end index number of the current page. For example, if the
	 * current page number is 2, the fetch size 100, and the result set size is 15,
	 * then the end index is (2*100+20-1) or 219. Index number begins from 0.
	 * 
	 * @return Entire set end index number. -1 if there no results.
	 */
	public int getEndIndex();
	
	/**
	 * Returns the largest page number it has visited so far.
	 */
	public int getLargestPageVisted();

	/**
	 * Returns true if the current page is the last page.
	 */
	public boolean isLastPage();
	
	/**
	 * Returns the size of the largest page visited.
	 */
	public int getLargestPageVisitedSize();
}
