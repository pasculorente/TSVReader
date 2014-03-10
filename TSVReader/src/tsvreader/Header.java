package tsvreader;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Header {
    private final String name, description, type, parent, origin;

    Header(String origin, String parent, String name, String type, String desc) {
        this.origin = origin;
        this.name = name;
        this.description = desc;
        this.parent = parent;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public String getType() {
        return type;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return origin + ":" + parent + ":" + name + ":" + type + ":" + description;
    }

}
