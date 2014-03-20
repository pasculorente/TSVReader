/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tsvreader;

/**
 * A Header represents a column and stores its name, type, description, parent name and original
 * name in file.
 *
 * @author Pascual Lorente Arencibia
 */
public class Header {

    /**
     * The name of the column.
     */
    private final String name;
    /**
     * The description of the column.
     */
    private final String description;
    /**
     * The type of the column: Numeric or Text, any cASe.
     */
    private final String type;
    /**
     * The name of the parent group column.
     */
    private final String parent;
    /**
     * The name of the column in the file.
     */
    private final String origin;

    /**
     * Creates a new header. nulls not allowed in parameters. Remember to use the same parent in
     * consecutive columns.
     *
     * @param origin The name of the column in the file.
     * @param parent A name for the grouping column. Use "" for non-grouped columns.
     * @param name The name of the column.
     * @param type The type of the column (numeric or text).
     * @param desc The description of the column.
     */
    Header(String origin, String parent, String name, String type, String desc) {
        this.origin = origin;
        this.name = name;
        this.description = desc;
        this.parent = parent;
        this.type = type;
    }

    /**
     * Gets the description.
     *
     * @return a String with the description of the header.
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the name of the header.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Parent name of the header.
     *
     * @return the parent.
     */
    public String getParent() {
        return parent;
    }

    /**
     * Gets the type of the header.
     *
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the name of the column in the file.
     *
     * @return the name of the origin.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * A representation of the Header as <code>origin:parent:name:type:description</code>
     *
     * @return the string representation of the Header.
     */
    @Override
    public String toString() {
        return getOrigin() + ":" + getParent() + ":" + getName() + ":" + getType() + ":"
                + getDescription();
    }

}
