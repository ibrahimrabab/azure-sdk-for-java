package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;


public class BlobClientUpload extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(BlobClientUpload.class);
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(BlobClientUpload.class);
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public BlobClientUpload(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(options.getBlobName());
        this.syncClient = getSyncContainerClient().getBlobClient(options.getBlobName());
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(options.getBlobName());
    }

    @Override
    protected boolean runInternal(Context span) {
        syncClient.upload(ORIGINAL_CONTENT.getBlobContent());
        return true;
        //return ORIGINAL_CONTENT.checkMatch(asyncClient, span).block();
    }

    @Override
    protected Mono<Boolean> runInternalAsync(Context span) {
        return asyncClient.upload(ORIGINAL_CONTENT.uploadData).then(ORIGINAL_CONTENT.checkMatch(asyncClient, span));
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            //.then(ORIGINAL_CONTENT.setupBlob(asyncClient, options.getSize()));
            .then(ORIGINAL_CONTENT.setupBlobWithoutUpload(options.getSize()));
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(super.globalCleanupAsync());
    }
}
