package info.jerrinot.subzero;

import com.esotericsoftware.kryo.Kryo;

/**
 * Created by brunomendoncalopes on 11/11/16.
 *
 * @author Bruno Lopes
 */
public interface DelegateKryo {

    void accept(Kryo kryo);
}
