/*******************************************************************************
 * Copyright (c) 2012 Eclipse RDF4J contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *******************************************************************************/
package org.eclipse.rdf4j.rio.rdfjson;

import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.helpers.BooleanRioSetting;

/**
 * A selection of parser settings specific to RDF/JSON parsers.
 * <p>
 * Several of these settings can be overridden by means of a system property, but only if specified at JVM startup time.
 *
 * @author Peter Ansell
 *
 * @since 4.3.0
 */
public class RDFJSONParserSettings {

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should fail if it finds multiple values for a
	 * single object in a single statement.
	 * <p>
	 * Defaults to true.
	 * <p>
	 * Can be overridden by setting system property
	 * {@code org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_values}.
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_VALUES = new BooleanRioSetting(
			"org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_values", "Fail on multiple object values",
			Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should fail if it finds multiple types for a
	 * single object in a single statement.
	 * <p>
	 * Defaults to true.
	 * <p>
	 * Can be overridden by setting system property {@code org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_types}.
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_TYPES = new BooleanRioSetting(
			"org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_types", "Fail on multiple object types",
			Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should fail if it finds multiple languages for
	 * a single object in a single statement.
	 * <p>
	 * Defaults to true.
	 * <p>
	 * Can be overridden by setting system property
	 * {@code org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_languages}.
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_LANGUAGES = new BooleanRioSetting(
			"org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_languages", "Fail on multiple object languages",
			Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should fail if it finds multiple datatypes for
	 * a single object in a single statement.
	 * <p>
	 * Defaults to true.
	 * <p>
	 * Can be overridden by setting system property
	 * {@code org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_datatypes}.
	 */
	public static final RioSetting<Boolean> FAIL_ON_MULTIPLE_OBJECT_DATATYPES = new BooleanRioSetting(
			"org.eclipse.rdf4j.rio.rdfjson.fail_on_multiple_object_datatypes", "Fail on multiple object datatypes",
			Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should fail if it finds multiple properties
	 * that it does not recognize in the JSON document.
	 * <p>
	 * Defaults to true.
	 * <p>
	 * Can be overridden by setting system property {@code org.eclipse.rdf4j.rio.rdfjson.fail_on_unknown_property}.
	 */
	public static final RioSetting<Boolean> FAIL_ON_UNKNOWN_PROPERTY = new BooleanRioSetting(
			"org.eclipse.rdf4j.rio.rdfjson.fail_on_unknown_property", "Fail on unknown property", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether an RDF/JSON parser should support the graphs extension to make it
	 * a quads format.
	 * <p>
	 * Defaults to true.
	 * <p>
	 * Can be overridden by setting system property {@code org.eclipse.rdf4j.rio.rdfjson.support_graphs_extension}.
	 */
	public static final RioSetting<Boolean> SUPPORT_GRAPHS_EXTENSION = new BooleanRioSetting(
			"org.eclipse.rdf4j.rio.rdfjson.support_graphs_extension", "SUPPORT_GRAPHS_EXTENSION", Boolean.TRUE);

	/**
	 * Private default constructor.
	 */
	private RDFJSONParserSettings() {
	}

	static {
		assert FAIL_ON_MULTIPLE_OBJECT_VALUES
				.equals(org.eclipse.rdf4j.rio.helpers.RDFJSONParserSettings.FAIL_ON_MULTIPLE_OBJECT_VALUES);
		assert FAIL_ON_MULTIPLE_OBJECT_TYPES
				.equals(org.eclipse.rdf4j.rio.helpers.RDFJSONParserSettings.FAIL_ON_MULTIPLE_OBJECT_TYPES);
		assert FAIL_ON_MULTIPLE_OBJECT_LANGUAGES
				.equals(org.eclipse.rdf4j.rio.helpers.RDFJSONParserSettings.FAIL_ON_MULTIPLE_OBJECT_LANGUAGES);
		assert FAIL_ON_MULTIPLE_OBJECT_DATATYPES
				.equals(org.eclipse.rdf4j.rio.helpers.RDFJSONParserSettings.FAIL_ON_MULTIPLE_OBJECT_DATATYPES);
		assert FAIL_ON_UNKNOWN_PROPERTY
				.equals(org.eclipse.rdf4j.rio.helpers.RDFJSONParserSettings.FAIL_ON_UNKNOWN_PROPERTY);
		assert SUPPORT_GRAPHS_EXTENSION
				.equals(org.eclipse.rdf4j.rio.helpers.RDFJSONParserSettings.SUPPORT_GRAPHS_EXTENSION);
	}

}
