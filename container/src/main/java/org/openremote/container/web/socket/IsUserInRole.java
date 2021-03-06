/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.container.web.socket;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

public class IsUserInRole implements Predicate {

    final protected String[] roles;

    public IsUserInRole(String... roles) {
        this.roles = roles;
    }

    @Override
    public boolean matches(Exchange exchange) {
        if (roles == null)
            return true;
        WebsocketAuth auth = exchange.getIn().getHeader(WebsocketConstants.AUTH, WebsocketAuth.class);
        for (String role : roles) {
            if (!auth.isUserInRole(role))
                return false;
        }
        return true;
    }
}
