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
package org.openremote.container.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;

import java.util.HashMap;
import java.util.Map;

public interface Database {

    Map<String, Object> open(String connectionUrl, String username, String password, int minIdle, int maxPoolSize);

    void close();

    enum Product implements Database {

        H2 {

            protected HikariConfig hikariConfig;
            protected HikariDataSource hikariDataSource;

            @Override
            public Map<String, Object> open(String connectionUrl, String username, String password, int minIdle, int maxPoolSize) {

                // Don't trace log values larger than X bytes (especially useful for debugging LOBs, which are accessed in toString()!)
                System.setProperty("h2.maxTraceDataLength", "256"); // 256 bytes, default is 64 kilobytes

                hikariConfig = new HikariConfig();
                hikariConfig.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
                hikariConfig.addDataSourceProperty("URL", connectionUrl);
                hikariConfig.setUsername(username);
                hikariConfig.setPassword(password);
                hikariConfig.setMinimumIdle(minIdle);
                hikariConfig.setMaximumPoolSize(maxPoolSize);

                hikariDataSource = new HikariDataSource(hikariConfig);

                Map<String, Object> properties = new HashMap<>();
                properties.put(AvailableSettings.DIALECT, H2Dialect.class.getName());
                properties.put(AvailableSettings.DATASOURCE, hikariDataSource);

                return properties;
            }

            @Override
            public void close() {
                if (hikariDataSource != null)
                    hikariDataSource.close();
                hikariConfig = null;
                hikariDataSource = null;
            }
        };
    }
}