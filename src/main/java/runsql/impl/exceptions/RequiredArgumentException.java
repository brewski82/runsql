/*
 * Copyright 2019 William Bruschi - williambruschi.net
 *
 * This file is part of runsql.
 *
 * runsql is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * runsql is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with runsql.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package runsql.impl.exceptions;

import runsql.impl.arguments.Parameter;

public class RequiredArgumentException extends RuntimeException {
    private final Parameter parameter;

    public RequiredArgumentException(final Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        String longOptionName = parameter.getLongName();
        String optionName = longOptionName == null ? parameter.getName() : longOptionName;
        return "Missing required argument: " + optionName;
    }
}