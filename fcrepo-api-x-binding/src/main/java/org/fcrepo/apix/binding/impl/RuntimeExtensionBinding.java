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

package org.fcrepo.apix.binding.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.fcrepo.apix.model.Extension;
import org.fcrepo.apix.model.WebResource;
import org.fcrepo.apix.model.components.ExtensionBinding;
import org.fcrepo.apix.model.components.ExtensionRegistry;
import org.fcrepo.apix.model.components.OntologyService;
import org.fcrepo.apix.model.components.Registry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple extension binding based on runtime lookup and reasoning.
 * <p>
 * Polls the extension registry for all known extensions at time of transaction, and performs reasoning at runtime in
 * order to bind a repository resource to the extensions that match it.
 * </p>
 *
 * @author apb@jhu.edu
 */
@Component(configurationPolicy = ConfigurationPolicy.REQUIRE)
public class RuntimeExtensionBinding implements ExtensionBinding {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeExtensionBinding.class);

    private ExtensionRegistry extensionRegistry;

    private OntologyService ontologySvc;

    private Registry registry;

    /**
     * Set the underlying registry containing extensions that may be bound.
     *
     * @param exr Extension registry instance.
     */
    @Reference
    public void setExtensionRegistry(final ExtensionRegistry exr) {
        extensionRegistry = exr;
    }

    /**
     * Set the underlying ontology service for performing reasoning for binding.
     *
     * @param os Ontology service instance.
     */
    @Reference
    public void setOntologyService(final OntologyService os) {
        ontologySvc = os;
    }

    /**
     * Set the underlying delegate registry for retrieving arbitrary web resources from the repository.
     * <p>
     * This registry is consulted in support of {@link #getExtensionsFor(URI)}
     * <p/>
     *
     * @param registry Registry impl.
     */
    @Reference(target = "(org.fcrepo.apix.registry.role=default)")
    public void setDelegateRegistry(final Registry registry) {
        this.registry = registry;
    }

    /**
     * Simple/naive binding algorithm, there may be opportunities for optimization when the time is right
     * <p>
     * <ol>
     * <li>Determine the set of known extensions</li>
     * <li>for each extension, get its ontology closure</li>
     * <li>for each ontology closure, infer classes of the instance</li>
     * <li>For each extension, see if its binding class is in that list of inferred classes.</li>
     * <li>Return all extensions that match</li>
     * </ol>
     * <p>
     */
    @Override
    public Collection<Extension> getExtensionsFor(final WebResource resource) {

        final Collection<Extension> extensions = extensionRegistry.getExtensions();

        final Set<URI> rdfTypes = extensions.stream()
                .map(Extension::getResource)
                .peek(extURI -> LOG.debug("Examinining the ontology closure of extension {}", extURI))
                .map(ontologySvc::parseOntology)
                .flatMap(o -> ontologySvc.inferClasses(resource.uri(), resource, o).stream())
                .peek(rdfType -> LOG.debug("Instance {} is of class {}", resource.uri(), rdfType))
                .collect(Collectors.toSet());

        return extensions.stream()
                .peek(e -> LOG.debug("Extension {} binds to instances of {}", e.uri(), e.bindingClass()))
                .filter(e -> rdfTypes.contains(e.bindingClass()))
                .peek(e -> LOG.debug("Extension {} bound to instance {} via {}", e.uri(), resource.uri(), e
                        .bindingClass()))
                .collect(Collectors.toList());
    }

    /** Just does a dumb dereference and lookup */
    @Override
    public Collection<Extension> getExtensionsFor(final URI resourceURI) {
        try (WebResource resource = registry.get(resourceURI)) {
            return getExtensionsFor(resource);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
