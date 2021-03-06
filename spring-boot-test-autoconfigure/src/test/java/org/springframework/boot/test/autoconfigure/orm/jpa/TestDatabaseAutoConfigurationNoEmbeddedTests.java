/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.autoconfigure.orm.jpa;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.context.ContextLoader;
import org.springframework.boot.testsupport.runner.classpath.ClassPathExclusions;
import org.springframework.boot.testsupport.runner.classpath.ModifiedClassPathRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Specific tests for {@link TestDatabaseAutoConfiguration} when no embedded database is
 * available.
 *
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 */
@RunWith(ModifiedClassPathRunner.class)
@ClassPathExclusions({ "h2-*.jar", "hsqldb-*.jar", "derby-*.jar" })
public class TestDatabaseAutoConfigurationNoEmbeddedTests {

	private final ContextLoader contextLoader = ContextLoader.standard()
			.config(ExistingDataSourceConfiguration.class)
			.autoConfig(TestDatabaseAutoConfiguration.class);

	@Test
	public void applyAnyReplace() {
		this.contextLoader.loadAndFail(BeanCreationException.class, ex -> {
			String message = ex.getMessage();
			assertThat(message).contains(
					"Failed to replace DataSource with an embedded database for tests.");
			assertThat(message).contains(
					"If you want an embedded database please put a supported one on the "
							+ "classpath");
			assertThat(message).contains(
					"or tune the replace attribute of @AutoconfigureTestDatabase.");
		});
	}

	@Test
	public void applyNoReplace() {
		this.contextLoader.env("spring.test.database.replace=NONE").load(context -> {
			assertThat(context.getBeansOfType(DataSource.class)).hasSize(1);
			assertThat(context.getBean(DataSource.class))
					.isSameAs(context.getBean("myCustomDataSource"));
		});
	}

	@Configuration
	static class ExistingDataSourceConfiguration {

		@Bean
		public DataSource myCustomDataSource() {
			return mock(DataSource.class);
		}

	}

}
