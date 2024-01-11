// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class UploadFromFile extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(UploadFromFile.class);
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(UploadFromFile.class);
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final File uploadFile;
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public UploadFromFile(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.uploadFile = getRandomFile((int) options.getSize());
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(options.getBlobName());
        this.syncClient = getSyncContainerClient().getBlobClient(options.getBlobName());
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(options.getBlobName());
    }

    @Override
    protected boolean runInternal(Context span) {
        BlobUploadFromFileOptions blobOptions = new BlobUploadFromFileOptions(uploadFile.getAbsolutePath());

        try {
            syncClient.uploadFromFileWithResponse(blobOptions, Duration.ofSeconds(options.getDuration()), span);
            return ORIGINAL_CONTENT.checkMatch(BinaryData.fromFile(uploadFile.toPath()), span).block();
        } finally {
            deleteFile(uploadFile);
        }
    }

    @Override
    protected Mono<Boolean> runInternalAsync(Context span) {
        return Mono.using(
            () -> uploadFile,
            file -> asyncClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(file.getAbsolutePath()))
                .flatMap(ignored -> ORIGINAL_CONTENT.checkMatch(BinaryData.fromFile(uploadFile.toPath()), span)),
            UploadFromFile::deleteFile);
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

    private static void deleteFile(File file) {
        try {
            file.delete();
        } catch (Throwable e) {
            LOGGER.atInfo()
                .addKeyValue("path", file.toPath())
                .log("failed to delete file", e);
        }
    }

    /**
     * Gets a random file of the given size.
     *
     * @param size The size of the file.
     * @return The random file.
     */
    private static File getRandomFile(int size) {
        try {
            File file = File.createTempFile(CoreUtils.randomUuid().toString(), ".txt");
            file.deleteOnExit();

            if (size > Constants.MB) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] data = getRandomByteArray(Constants.MB);
                    int mbChunks = size / Constants.MB;
                    int remaining = size % Constants.MB;
                    for (int i = 0; i < mbChunks; i++) {
                        fos.write(data);
                    }

                    if (remaining > 0) {
                        fos.write(data, 0, remaining);
                    }
                }
            } else {
                Files.write(file.toPath(), getRandomByteArray(size));
            }

            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets a random byte array of the given size.
     *
     * @param size The size of the byte array.
     * @return The random byte array.
     */
    public static byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(CoreUtils.randomUuid().toString()).getMostSignificantBits() & Long.MAX_VALUE;
        Random rand = new Random(seed);
        byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }
}
