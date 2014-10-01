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

package com.github.avarabyeu.restendpoint.http;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Base Rest request representation
 *
 * @param <RQ> - type of request
 * @param <RS> - type of response
 * @author Andrei Varabyeu
 */
public class RestCommand<RQ, RS> {

    private HttpMethod httpMethod;
    private RQ request;
    private String uri;
    private TypeToken<RS> responseType;

    public RestCommand(String uri, HttpMethod method, RQ request) {
        this(uri, method, request, new TypeToken<RS>() {
        });
    }

    public RestCommand(String uri, HttpMethod method, RQ request, TypeToken<RS> responseType) {
        this.httpMethod = method;
        this.request = request;
        this.uri = uri;
        this.responseType = responseType;
        validate();
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public RQ getRequest() {
        return request;
    }

    public String getUri() {
        return uri;
    }

    public Type getResponseType() {
        return responseType.getType();
    }

    private void validate() {
        if (HttpMethod.GET.equals(this.httpMethod) && null != this.request) {
            throw new RuntimeException("'GET' request cannot contain body");
        }
    }
}
