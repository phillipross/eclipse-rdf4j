/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra.evaluation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.BooleanLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.algebra.And;
import org.eclipse.rdf4j.query.algebra.FunctionCall;
import org.eclipse.rdf4j.query.algebra.GroupElem;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryOptimizerTest;
import org.eclipse.rdf4j.query.algebra.evaluation.optimizer.BindingAssignerOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.optimizer.ConstantOptimizer;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class ConstantOptimizerTest extends QueryOptimizerTest {

	@Test
	public void testAndOptimization() throws RDF4JException {
		String query = "prefix ex: <ex:>" + "select ?a ?b ?c\n" + "where {\n" + " bind((?a && ?b) as ?c) \n" + "}";

		ParsedQuery pq = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);

		TupleExpr original = pq.getTupleExpr();

		final AlgebraFinder finder = new AlgebraFinder();
		original.visit(finder);
		assertTrue(finder.logicalAndfound);

		// reset for re-use on optimized query
		finder.reset();

		QueryBindingSet constants = new QueryBindingSet();
		constants.addBinding("a", BooleanLiteral.TRUE);
		constants.addBinding("b", BooleanLiteral.FALSE);

		EvaluationStrategy strategy = new StrictEvaluationStrategy(new EmptyTripleSource(), null);
		TupleExpr optimized = optimize(pq.getTupleExpr().clone(), constants, strategy);

		optimized.visit(finder);
		assertThat(finder.logicalAndfound).isFalse();

		CloseableIteration<BindingSet> result = strategy.precompile(optimized).evaluate(new EmptyBindingSet());
		assertNotNull(result);
		assertTrue(result.hasNext());
		BindingSet bindings = result.next();
		assertTrue(bindings.hasBinding("a"));
		assertTrue(bindings.hasBinding("b"));
		assertTrue(bindings.hasBinding("c"));
	}

	@Test
	public void testFunctionOptimization() throws RDF4JException {
		String query = "prefix ex: <ex:>" + "select ?a ?b ?c \n " + "where {\n" + " bind(concat(?a, ?b) as ?c) \n"
				+ "}";

		ParsedQuery pq = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
		EvaluationStrategy strategy = new StrictEvaluationStrategy(new EmptyTripleSource(), null);
		TupleExpr original = pq.getTupleExpr();

		final AlgebraFinder finder = new AlgebraFinder();
		original.visit(finder);
		assertTrue(finder.functionCallFound);

		// reset for re-use on optimized query
		finder.reset();

		QueryBindingSet constants = new QueryBindingSet();
		constants.addBinding("a", SimpleValueFactory.getInstance().createLiteral("foo"));
		constants.addBinding("b", SimpleValueFactory.getInstance().createLiteral("bar"));

		TupleExpr optimized = optimize(pq.getTupleExpr().clone(), constants, strategy);

		optimized.visit(finder);
		assertThat(finder.functionCallFound).isFalse();

		CloseableIteration<BindingSet> result = strategy.precompile(optimized)
				.evaluate(
						new EmptyBindingSet());
		assertNotNull(result);
		assertTrue(result.hasNext());
		BindingSet bindings = result.next();
		assertTrue(bindings.hasBinding("a"));
		assertTrue(bindings.hasBinding("b"));
		assertTrue(bindings.hasBinding("c"));

	}

	@Test
	public void testAggregateOptimization() throws RDF4JException {
		String query = "prefix ex: <ex:>" + "select (max(1) AS ?a) \n " + "where {\n" + "?x a ?z \n"
				+ "}";

		ParsedQuery pq = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
		EvaluationStrategy strategy = new DefaultEvaluationStrategy(new EmptyTripleSource(), null);
		TupleExpr original = pq.getTupleExpr();

		final AlgebraFinder finder = new AlgebraFinder();
		original.visit(finder);
		assertTrue(finder.groupElemFound);

		// reset for re-use on optimized query
		finder.reset();

		QueryBindingSet constants = new QueryBindingSet();
		constants.addBinding("x", SimpleValueFactory.getInstance().createLiteral("foo"));
		constants.addBinding("z", SimpleValueFactory.getInstance().createLiteral("bar"));

		TupleExpr optimized = optimize(pq.getTupleExpr().clone(), constants, strategy);

		optimized.visit(finder);
		assertThat(finder.functionCallFound).isFalse();

		CloseableIteration<BindingSet> result = strategy.precompile(optimized)
				.evaluate(
						new EmptyBindingSet());
		assertNotNull(result);
		assertTrue(result.hasNext());
		BindingSet bindings = result.next();
		assertTrue(bindings.hasBinding("a"));
		assertEquals(1, ((Literal) bindings.getBinding("a").getValue()).intValue());
	}

	@Test
	public void testAllAggregateOptimizations() throws RDF4JException {
		String query = String.join("\n",
				"PREFIX ex: <ex:>",
				"SELECT",
				"  (MAX(1) AS ?a)",
				"  (MIN(1) AS ?b)",
				"  (AVG(1) AS ?c)",
				"  (COUNT(1) AS ?d)",
				"  (COUNT(DISTINCT 1) AS ?e)",
				"  (COUNT(*) AS ?f)",
				"WHERE {",
				"  ?x a ?z ;",
				"  ex:someProperty ?val .",
				"}"
		);

		ParsedQuery pq = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
		EvaluationStrategy strategy = new DefaultEvaluationStrategy(new EmptyTripleSource(), null);
		TupleExpr original = pq.getTupleExpr();

		final AlgebraFinder finder = new AlgebraFinder();
		original.visit(finder);
		assertTrue(finder.groupElemFound);

		// reset for re-use on optimized query
		finder.reset();

		QueryBindingSet constants = new QueryBindingSet();

		TupleExpr optimized = optimize(pq.getTupleExpr().clone(), constants, strategy);

		optimized.visit(finder);
		assertThat(finder.functionCallFound).isFalse();

		CloseableIteration<BindingSet> result = strategy.precompile(optimized)
				.evaluate(
						new EmptyBindingSet());
		assertNotNull(result);
		assertTrue(result.hasNext());

		BindingSet bindings = result.next();
		assertTrue(bindings.hasBinding("a"));
		assertTrue(bindings.hasBinding("b"));
		assertTrue(bindings.hasBinding("c"));
		assertTrue(bindings.hasBinding("d"));
		assertTrue(bindings.hasBinding("e"));
		assertTrue(bindings.hasBinding("f"));
		assertEquals(1, ((Literal) bindings.getBinding("a").getValue()).intValue());
		assertEquals(1, ((Literal) bindings.getBinding("b").getValue()).intValue());
		assertEquals(1, ((Literal) bindings.getBinding("c").getValue()).intValue());
		assertEquals(0, ((Literal) bindings.getBinding("d").getValue()).intValue());
		assertEquals(0, ((Literal) bindings.getBinding("e").getValue()).intValue());
		assertEquals(0, ((Literal) bindings.getBinding("f").getValue()).intValue());
	}

	private class AlgebraFinder extends AbstractQueryModelVisitor<RuntimeException> {

		public boolean logicalAndfound = false;

		public boolean functionCallFound = false;

		public boolean groupElemFound = false;

		@Override
		public void meet(And and) {
			logicalAndfound = true;
			super.meet(and);

		}

		@Override
		public void meet(FunctionCall arg) {
			functionCallFound = true;
			super.meet(arg);
		}

		public void reset() {
			logicalAndfound = false;
			functionCallFound = false;
			groupElemFound = false;
		}

		public void meet(GroupElem ge) {
			groupElemFound = true;
			super.meet(ge);
		}
	}

	private TupleExpr optimize(TupleExpr expr, BindingSet bs, EvaluationStrategy strategy) {
		QueryRoot optRoot = new QueryRoot(expr);
		new BindingAssignerOptimizer().optimize(optRoot, null, bs);
		new ConstantOptimizer(strategy).optimize(optRoot, null, bs);
		return optRoot;
	}

	@Override
	public ConstantOptimizer getOptimizer() {
		return new ConstantOptimizer(mock(StrictEvaluationStrategy.class));
	}
}
