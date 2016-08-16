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

import java.net.URI;

/**
 * Abstract notion of an API-X extension.
 * <p>
 * TODO: This is mostly a stub
 * </p>
 * 
 * @author apb@jhu.edu
 */
public interface Extension {

    /**
     * RDF type this extension binds to.
     *
     * @return URI of the <code>rdf:type</code> this extension binds to.
     */
    public URI bindingClass();

    /**
     * The URI (location) of the extension.
     * <p>
     * This is a potentially more lightweight operation than <code>getResource().uri()</code>
     * </p>
     *
     * @return its resolvable URI.
     */
    public URI uri();

    /**
     * Underlying RDF representation of an extension.
     *
     * @return A serialization of the extension resource.
     */
    public WebResource getResource();
}
