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

package org.fcrepo.apix.routing.impl;

import java.net.URI;

import org.fcrepo.apix.model.Extension.ServiceExposureSpec;
import org.fcrepo.apix.model.components.Routing;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * Stub/placeholder Routing implementation that does nothing.
 *
 * @author apb@jhu.edu
 */
@Component(configurationPolicy = ConfigurationPolicy.REQUIRE)
public class RoutingStub implements Routing {

    @Override
    public URI endpointFor(final ServiceExposureSpec spec, final URI onResource) {
        return URI.create("test:/endpoint" + onResource.getPath());
    }

    @Override
    public URI serviceDocFor(final URI resource) {
        return URI.create("test:/serviceDoc" + resource.getPath());
    }

}
