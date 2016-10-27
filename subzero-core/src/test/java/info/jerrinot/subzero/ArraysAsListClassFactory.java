package info.jerrinot.subzero;

import java.util.Arrays;

public class ArraysAsListClassFactory implements ClassFactory {

    @Override
    public Class createClass() {
        return Arrays.asList( "" ).getClass();
    }
}
