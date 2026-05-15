package io.telicent.smart.cache.distribution.lifecycle.store.apps;

import java.io.File;
import java.util.Objects;

public class AppDistributionLifecycleStoreFile extends AbstractAppDistributionLifecycleStore implements AutoCloseable {
    private final File stateDir;

    protected AppDistributionLifecycleStoreFile(String app, File stateDir) {
        super(app);
        this.stateDir = Objects.requireNonNull(stateDir, "State store directory cannot be null");
        if (this.stateDir.isFile()) {
            throw new IllegalArgumentException(
                    "State store given as a file (" + this.stateDir.getAbsolutePath() + ") when a directory was expected");
        } else if (!this.stateDir.exists() && !this.stateDir.mkdirs()) {
            throw new IllegalArgumentException(
                    "Failed to create configured state store directory " + this.stateDir.getAbsolutePath());
        }

        this.load();
    }

    private void load() {

    }

    private void save() {

    }

    @Override
    public void close() {
        this.save();
    }
}
