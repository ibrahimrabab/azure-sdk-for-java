// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.StructuredMessageDecoder;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobMessageDecoderTests extends BlobTestBase {

    private BlobClient bc;
    private BlockBlobClient bbc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = cc.getBlobClient(blobName);
        bbc = bc.getBlockBlobClient();
    }

    @Test
    @LiveOnly
    public void isBodyAccepted() {
        byte[] randomData = "hello world".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        //Response<BlockBlobItem> response = bc.uploadWithResponse(new BlobParallelUploadOptions(input), null, null);
        Response<BlockBlobItem> response
            = bbc.uploadWithResponse(new BlockBlobSimpleUploadOptions(input, randomData.length), null, null);
        assertResponseStatusCode(response, 201);
    }

    @Test
    public void isBodyDecodedCorrectly() {
        byte[] randomData = "hello world".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);
        Response<BlockBlobItem> uploadResponse
            = bbc.uploadWithResponse(new BlockBlobSimpleUploadOptions(input, randomData.length), null, null);
        assertResponseStatusCode(uploadResponse, 201);
        OutputStream outputStream = new ByteArrayOutputStream();
        bc.downloadStreamWithResponse(outputStream, new BlobRange(0), null, null, false, null, Context.NONE);

        // assert that the downloadedData == original data
        assertEquals(new String(randomData, StandardCharsets.UTF_8), outputStream.toString());

    }

    @Test
    public void isLargeBodyDecodedCorrectly() {
        // Generate large data (greater than 4MB)
        byte[] largeData = new byte[5 * 1024 * 1024]; // 5MB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        ByteArrayInputStream input = new ByteArrayInputStream(largeData);
        Response<BlockBlobItem> uploadResponse
            = bbc.uploadWithResponse(new BlockBlobSimpleUploadOptions(input, largeData.length), null, null);
        assertResponseStatusCode(uploadResponse, 201);

        // Download the data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bc.downloadStreamWithResponse(outputStream, new BlobRange(0), null, null, false, null, Context.NONE);

        // Verify the downloaded data matches the original data
        byte[] downloadedData = outputStream.toByteArray();
        assertEquals(largeData.length, downloadedData.length);
        for (int i = 0; i < largeData.length; i++) {
            assertEquals(largeData[i], downloadedData[i]);
        }
    }
}
