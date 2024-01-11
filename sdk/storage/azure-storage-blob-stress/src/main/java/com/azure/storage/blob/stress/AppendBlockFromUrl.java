package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

public class AppendBlockFromUrl extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(AppendBlockFromUrl.class);
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(AppendBlockFromUrl.class);
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public AppendBlockFromUrl(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(options.getBlobName());
        this.syncClient = getSyncContainerClient().getBlobClient(options.getBlobName());
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(options.getBlobName());
    }

    @Override
    protected boolean runInternal(Context span) {
        return false;
    }

    @Override
    protected Mono<Boolean> runInternalAsync(Context span) {
        return null;
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(ORIGINAL_CONTENT.setupBlobForUpload(options.getSize()));
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(super.globalCleanupAsync());
    }
}
