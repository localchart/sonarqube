/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.user;

import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.Startable;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.ws.Request;
import org.sonar.api.utils.log.Loggers;

@ServerSide
public class SystemPasscodeImpl implements SystemPasscode, Startable {

  public static final String PASSCODE_HTTP_HEADER = "X-Sonar-Passcode";
  public static final String PASSCODE_CONF_PROPERTY = "sonar.web.systemPasscode";

  private final Configuration configuration;

  public SystemPasscodeImpl(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public boolean isConfigured() {
    return getConfiguredValue().isPresent();
  }

  @Override
  public boolean isValid(Request request) {
    Optional<String> configuredValue = getConfiguredValue();
    if (!configuredValue.isPresent()) {
      return false;
    }
    return request.header(PASSCODE_HTTP_HEADER)
      .map(s -> configuredValue.get().equals(s))
      .orElse(false);
  }

  private Optional<String> getConfiguredValue() {
    // if present, result is never empty string
    return configuration.get(PASSCODE_CONF_PROPERTY)
      .map(StringUtils::trimToNull);
  }

  @Override
  public void start() {
    String status = isConfigured() ? "enabled" : "disabled";
    Loggers.get(getClass()).info("System authentication by passcode is {}", status);
  }

  @Override
  public void stop() {
    // nothing to do
  }
}
