/*******************************************************************************
 * Copyright (c) 2022 Eclipse RDF4J contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *******************************************************************************/
package org.eclipse.rdf4j.testsuite.sparql.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.testsuite.sparql.AbstractComplianceTest;
import org.eclipse.rdf4j.testsuite.sparql.vocabulary.EX;
import org.junit.jupiter.api.DynamicTest;

/**
 * Tests on handling default graph identification (DEFAULT keyword, rf4j:nil).
 *
 * @author Jeen Broekstra
 *
 */
public class DefaultGraphTest extends AbstractComplianceTest {

	public DefaultGraphTest(Supplier<Repository> repo) {
		super(repo);
	}

	private void testNullContext1() throws Exception {
		Repository repo = openRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			loadTestData("/testdata-query/dataset-query.trig", conn);
			String query = " SELECT * " + " FROM DEFAULT " + " WHERE { ?s ?p ?o } ";

			TupleQuery tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

			try (TupleQueryResult result = tq.evaluate()) {
				assertNotNull(result);

				while (result.hasNext()) {
					BindingSet bs = result.next();
					assertNotNull(bs);

					Resource s = (Resource) bs.getValue("s");

					assertNotNull(s);
					assertNotEquals(EX.BOB, s); // should not be present in default
					// graph
					assertNotEquals(EX.ALICE, s); // should not be present in
					// default
					// graph
				}
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		closeRepository(repo);
	}

	private void testNullContext2() throws Exception {
		Repository repo = openRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			loadTestData("/testdata-query/dataset-query.trig", conn);
			String query = " SELECT * " + " FROM rdf4j:nil " + " WHERE { ?s ?p ?o } ";

			TupleQuery tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

			try (TupleQueryResult result = tq.evaluate()) {
				assertNotNull(result);

				while (result.hasNext()) {
					BindingSet bs = result.next();
					assertNotNull(bs);

					Resource s = (Resource) bs.getValue("s");

					assertNotNull(s);
					assertNotEquals(EX.BOB, s); // should not be present in default
					// graph
					assertNotEquals(EX.ALICE, s); // should not be present in
					// default
					// graph
				}
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		} finally {
			closeRepository(repo);
		}
	}

	private void testSesameNilAsGraph() throws Exception {
		Repository repo = openRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			loadTestData("/testdata-query/dataset-query.trig", conn);
			String query = " SELECT * " + " WHERE { GRAPH rdf4j:nil { ?s ?p ?o } } ";
//		query.append(" WHERE { ?s ?p ?o } ");

			TupleQuery tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);

			try {
				List<BindingSet> result = QueryResults.asList(tq.evaluate());

				// nil graph should not be empty
				assertThat(result.size()).isGreaterThan(1);

				for (BindingSet bs : result) {
					Resource s = (Resource) bs.getValue("s");

					assertNotNull(s);
					assertThat(s).withFailMessage("%s should not be present in nil graph", EX.BOB).isNotEqualTo(EX.BOB);
					assertThat(s).withFailMessage("%s should not be present in nil graph", EX.ALICE)
							.isNotEqualTo(EX.ALICE);
				}
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		} finally {
			closeRepository(repo);
		}
	}

	public Stream<DynamicTest> tests() {
		return Stream.of(makeTest("SesameNilAsGraph", this::testSesameNilAsGraph),
				makeTest("NullContext2", this::testNullContext2), makeTest("NullContext1", this::testNullContext1));
	}
}
