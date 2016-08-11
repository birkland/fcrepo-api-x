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

package org.fcrepo.apix.model;

import java.io.InputStream;
import java.net.URI;

/**
 * Contains a representation of a web resource.
 */
public interface WebResource extends AutoCloseable {

    public String contentType();

    public URI uri();

    public long length();

    public InputStream representation();

    public static WebResource of(final InputStream stream, final String contentType) {
        return of(stream, contentType, null, null);
    }

    public static WebResource of(final InputStream stream, final String contentType, final URI uri,
            final Long length) {
        return new WebResource() {

            @Override
            public void close() throws Exception {
                stream.close();
            }

            @Override
            public URI uri() {
                return uri;
            }

            @Override
            public InputStream representation() {
                return stream;
            }

            @Override
            public long length() {
                return length;
            }

            @Override
            public String contentType() {
                return contentType;
            }
        };
    }
}
