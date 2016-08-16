/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.apix.registry.impl;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;

import org.fcrepo.apix.model.WebResource;
import org.fcrepo.apix.model.components.Registry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTTP-based registry that performs GET for lookups on a given URI.
 */
@Component(configurationPolicy = REQUIRE)
public class HttpRegistry implements Registry {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRegistry.class);

    private CloseableHttpClient client;

    static final String RDF_MEDIA_TYPES = "application/rdf+xml, text/turtle";

    @Reference
    public void setHttpClient(CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public WebResource get(URI id) {

        final HttpGet get = new HttpGet(id);
        get.setHeader(ACCEPT, RDF_MEDIA_TYPES);

        final CloseableHttpResponse response = execute(get);

        return new WebResource() {

            @Override
            public void close() throws Exception {
                response.close();
            }

            @Override
            public URI uri() {
                return id;
            }

            @Override
            public InputStream representation() {
                try {
                    return response.getEntity().getContent();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Long length() {
                return response.getEntity().getContentLength();
            }

            @Override
            public String contentType() {
                return response.getFirstHeader(CONTENT_TYPE).getValue();
            }
        };
    }

    private CloseableHttpResponse execute(HttpUriRequest request) {
        CloseableHttpResponse response = null;

        try {
            response = client.execute(request);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        if (response.getStatusLine().getStatusCode() != SC_OK) {
            try {
                LOG.warn(IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF_8")));
            } catch (final Exception e) {
                LOG.warn(Integer.toString(response.getStatusLine().getStatusCode()));
            } finally {
                try {
                    response.close();
                } catch (final Exception x) {

                }

            }
            throw new RuntimeException(String.format("Error performing %s on %s: %s; %s", request.getMethod(), request
                    .getURI(), response.getStatusLine(), body(response)));
        }

        return response;
    }

    private String body(HttpResponse response) {
        if (response.getEntity() != null) {
            try {
                return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            } catch (final Exception e) {
                return "";
            }
        }

        return "";
    }

    @Override
    public boolean contains(URI uri) {
        try (CloseableHttpResponse response = client.execute(new HttpHead(uri))) {
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI put(WebResource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public Collection<URI> list() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(URI uri) {
        throw new UnsupportedOperationException();
    }
}
