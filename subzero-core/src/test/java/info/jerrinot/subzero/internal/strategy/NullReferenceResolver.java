package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ReferenceResolver;

public class NullReferenceResolver implements ReferenceResolver {
    @Override
    public void setKryo(Kryo kryo) {
        // do nothing
    }

    @Override
    public int getWrittenId(Object object) {
        return 0;
    }

    @Override
    public int addWrittenObject(Object object) {
        return 0;
    }

    @Override
    public int nextReadId(Class type) {
        return 0;
    }

    @Override
    public void setReadObject(int id, Object object) {
        // do nothing
    }

    @Override
    public Object getReadObject(Class type, int id) {
        return null;
    }

    @Override
    public void reset() {
        // do nothing
    }

    @Override
    public boolean useReferences(Class type) {
        return false;
    }
}
