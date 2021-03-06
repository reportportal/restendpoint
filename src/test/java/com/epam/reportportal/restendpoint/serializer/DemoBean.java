/*
 * Copyright (C) 2014 Andrei Varabyeu
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

package com.epam.reportportal.restendpoint.serializer;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean for unit tests
 *
 * @author Andrey Vorobyov
 */
@XmlRootElement
public class DemoBean {

	private String someField;

	public DemoBean() {
	}

	public DemoBean(String someField) {
		this.someField = someField;
	}

	public String getSomeField() {
		return someField;
	}

	public void setSomeField(String someField) {
		this.someField = someField;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DemoBean demoBean = (DemoBean) o;
		return com.google.common.base.Objects.equal(someField, demoBean.someField);
	}

	@Override
	public int hashCode() {
		return com.google.common.base.Objects.hashCode(someField);
	}
}
