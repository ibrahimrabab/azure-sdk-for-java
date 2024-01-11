// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AppendBlock extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(AppendBlock.class);
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(AppendBlock.class);
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public AppendBlock(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(options.getBlobName());
        this.syncClient = getSyncContainerClient().getBlobClient(options.getBlobName());
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(options.getBlobName());
    }

    @Override
    protected boolean runInternal(Context span) throws IOException {
        try (InputStream inputStream = ORIGINAL_CONTENT.getBlobContent()) {
            AppendBlobClient appendBlobClient = syncClient.getAppendBlobClient();
            appendBlobClient.appendBlock(inputStream, options.getSize());
            return ORIGINAL_CONTENT.checkMatch(asyncClient, span).block();
        }
    }

    @Override
    protected Mono<Boolean> runInternalAsync(Context span) {
        AppendBlobAsyncClient appendBlobAsyncClient = asyncClient.getAppendBlobAsyncClient();
        ByteBuffer byteBuffer = ByteBuffer.wrap(ORIGINAL_CONTENT.uploadData.toBytes());
        return appendBlobAsyncClient.appendBlock(Flux.just(byteBuffer), options.getSize())
            .then(ORIGINAL_CONTENT.checkMatch(asyncClient, span));
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
