package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.OutputChunked;

final class KryoContext {
    private final Kryo kryo;
    private final InputChunked inputChunked;
    private final OutputChunked outputChunked;

    KryoContext(Kryo kryo, InputChunked inputChunked, OutputChunked outputChunked) {
        this.kryo = kryo;
        this.inputChunked = inputChunked;
        this.outputChunked = outputChunked;
    }

    public InputChunked getInputChunked() {
        return inputChunked;
    }

    public OutputChunked getOutputChunked() {
        return outputChunked;
    }

    public Kryo getKryo() {
        return kryo;
    }
}
