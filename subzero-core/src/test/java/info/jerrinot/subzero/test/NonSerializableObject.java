package info.jerrinot.subzero.test;

public final class NonSerializableObject {
    private String name;

    public NonSerializableObject(String name) {
        this.name = name;
    }

    public NonSerializableObject() { }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NonSerializableObject)) return false;

        NonSerializableObject nonSerializableObject = (NonSerializableObject) o;

        return name.equals(nonSerializableObject.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "NonSerializableObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
