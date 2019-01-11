package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.PropertyUserSerializer;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SubzeroConfigRule implements TestRule {
    private static final String CONFIG_FILE_PROP = "subzero.custom.serializers.config.filename";

    private final String filename;

    private SubzeroConfigRule(String filename) {
        this.filename = filename;
    }

    public static SubzeroConfigRule useConfig(String filename) {
        return new SubzeroConfigRule(filename);
    }

    public SubzeroConfigRule reconfigure(String filename) {
        System.setProperty(CONFIG_FILE_PROP, filename);
        PropertyUserSerializer.reinitialize();
        return this;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String origValue = System.getProperty(CONFIG_FILE_PROP);
                System.setProperty(CONFIG_FILE_PROP, filename);
                PropertyUserSerializer.reinitialize();
                try {
                    base.evaluate();
                } finally {
                    if (origValue == null) {
                        System.clearProperty(CONFIG_FILE_PROP);
                    } else {
                        System.setProperty(CONFIG_FILE_PROP, origValue);
                    }
                }
            }
        };
    }
}
