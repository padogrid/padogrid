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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.xml.transform.TransformerException;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hazelcast.addon.exception.HqlException;
import org.hazelcast.addon.hql.internal.antlr4.generated.HqlLexer;
import org.hazelcast.addon.hql.internal.antlr4.generated.HqlParser;

/**
 * HqlEvalDriver evaluates the specified HQL statement(s).
 * 
 * @author dpark
 *
 */
public class HqlEvalDriver {
	private CharStream cs;
	private HqlEvalListenerImpl evalProp;

	public HqlEvalDriver(String queryString) {
		cs = CharStreams.fromString(queryString);
	}

	public HqlEvalDriver(Path path) throws IOException {
		cs = CharStreams.fromPath(path);
	}

	public HqlEvalDriver(InputStream is) throws IOException {
		cs = CharStreams.fromStream(is);
	}

	public HqlContext getHqlContext() {
		return evalProp.getHqlContext();
	}

	public void dump() throws TransformerException {
		evalProp.dump();
	}

	public void execute() {
		HqlLexer lexer = new HqlLexer(cs);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		HqlParser parser = new HqlParser(tokens);
		parser.setBuildParseTree(true); // tell ANTLR to build a parse tree
		parser.setErrorHandler(new BailErrorStrategy());
		ParseTree tree;
		try {
			tree = parser.hql_file(); // parse
		} catch (Throwable th) {
			throw new HqlException(th);
		}
		// show tree in text form
//		System.out.println(tree.toStringTree(parser));

		// Listener
		ParseTreeWalker walker = new ParseTreeWalker();
		evalProp = new HqlEvalListenerImpl();
		walker.walk(evalProp, tree);
	}
}