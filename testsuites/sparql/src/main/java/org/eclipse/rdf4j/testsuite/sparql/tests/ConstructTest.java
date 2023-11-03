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
import static org.eclipse.rdf4j.model.util.Statements.statement;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.testsuite.sparql.AbstractComplianceTest;
import org.junit.jupiter.api.DynamicTest;

/**
 * Tests on SPARQL CONSTRUCT queries.
 *
 * @author Jeen Broekstra
 *
 */
public class ConstructTest extends AbstractComplianceTest {

	public ConstructTest(Supplier<Repository> repo) {
		super(repo);
	}

	private void testConstructModifiers() throws Exception {
		Repository repo = openRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			loadTestData("/testdata-query/dataset-construct-modifiers.ttl", conn);
			String qry = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" + "PREFIX site: <http://example.org/stats#> \n"
					+ "CONSTRUCT { \n" + "  ?iri foaf:name ?name . \n" + "  ?iri foaf:nick ?nick . \n" + "} \n"
					+ "WHERE { \n" + "  ?iri foaf:name ?name ; \n" + "    site:hits ?hits ; \n"
					+ "    foaf:nick ?nick . \n" + "} \n" + "ORDER BY desc(?hits) \n" + "LIMIT 3";
			Statement[] correctResult = {
					statement(iri("urn:1"), iri("http://xmlns.com/foaf/0.1/name"), literal("Alice"), null),
					statement(iri("urn:1"), iri("http://xmlns.com/foaf/0.1/nick"), literal("Al"), null),

					statement(iri("urn:3"), iri("http://xmlns.com/foaf/0.1/name"), literal("Eve"), null),
					statement(iri("urn:3"), iri("http://xmlns.com/foaf/0.1/nick"), literal("Ev"), null),

					statement(iri("urn:2"), iri("http://xmlns.com/foaf/0.1/name"), literal("Bob"), null),
					statement(iri("urn:2"), iri("http://xmlns.com/foaf/0.1/nick"), literal("Bo"), null), };
			GraphQuery gq = conn.prepareGraphQuery(qry);
			try (GraphQueryResult result = gq.evaluate()) {
				assertNotNull(result);
				assertTrue(result.hasNext());
				int resultNo = 0;
				while (result.hasNext()) {
					Statement st = result.next();
					assertThat(resultNo).isLessThan(correctResult.length);
					assertEquals(correctResult[resultNo], st);
					resultNo++;
				}
				assertEquals(correctResult.length, resultNo);
			}
		}
		closeRepository(repo);
	}

	/**
	 * See https://github.com/eclipse/rdf4j/issues/3011
	 */

	private void testConstruct_CyclicPathWithJoin() {
		Repository repo = openRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			IRI test = iri("urn:test"), a = iri("urn:a"), b = iri("urn:b"), c = iri("urn:c");
			conn.add(test, RDF.TYPE, DCAT.CATALOG);

			String query = "PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" + "\n" + "CONSTRUCT {\n"
					+ "<urn:a> <urn:b> ?x .\n" + "  ?x <urn:c> ?x .\n" + "}\n" + "WHERE {\n" + "  ?x a dcat:Catalog .\n"
					+ "}";

			Model result = QueryResults.asModel(conn.prepareGraphQuery(query).evaluate());

			assertThat(result.contains(a, b, test)).isTrue();
			assertThat(result.contains(test, c, test)).isTrue();
		}
		closeRepository(repo);
	}

	private void testSES2104ConstructBGPSameURI() throws Exception {
		Repository repo = openRepository();
		try (RepositoryConnection conn = repo.getConnection()) {
			final String queryStr = "PREFIX : <urn:> CONSTRUCT {:x :p :x } WHERE {} ";

			conn.add(new StringReader("@prefix : <urn:> . :a :p :b . "), "", RDFFormat.TURTLE);

			final IRI x = conn.getValueFactory().createIRI("urn:x");
			final IRI p = conn.getValueFactory().createIRI("urn:p");

			GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryStr);
			try (GraphQueryResult evaluate = query.evaluate()) {
				Model result = QueryResults.asModel(evaluate);

				assertNotNull(result);
				assertFalse(result.isEmpty());
				assertTrue(result.contains(x, p, x));
			}
		}
		closeRepository(repo);
	}

	public Stream<DynamicTest> tests() {
		return Stream.of(makeTest("ConstructModifiers", this::testConstructModifiers),
				makeTest("SES2104ConstructBGPSameURI", this::testSES2104ConstructBGPSameURI),
				makeTest("Construct_CyclicPathWithJoin", this::testConstruct_CyclicPathWithJoin));
	}
}
