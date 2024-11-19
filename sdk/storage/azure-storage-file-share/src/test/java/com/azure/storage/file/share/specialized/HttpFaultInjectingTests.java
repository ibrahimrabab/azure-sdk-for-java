// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.FileShareTestBase;
import com.azure.storage.file.share.FileShareTestHelper;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.storage.common.test.shared.StorageCommonTestUtils.ENVIRONMENT;

/**
 * Set of tests that use <a href="">HTTP fault injecting</a> to simulate scenarios where the network has random errors.
 */
@EnabledIf("shouldRun")
public class HttpFaultInjectingTests {
    private static final ClientLogger LOGGER = new ClientLogger(HttpFaultInjectingTests.class);
    private static final HttpHeaderName UPSTREAM_URI_HEADER = HttpHeaderName.fromString("X-Upstream-Base-Uri");
    private static final HttpHeaderName HTTP_FAULT_INJECTOR_RESPONSE_HEADER
        = HttpHeaderName.fromString("x-ms-faultinjector-response-option");

    private ShareClient shareClient;

    @BeforeEach
    public void setup() {
        String testName = ("httpFaultInjectingTests" + CoreUtils.randomUuid().toString().replace("-", ""))
            .toLowerCase();
        System.out.println(testName);
        shareClient = new ShareServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .httpClient(FileShareTestBase.getHttpClient(() -> {
                throw new RuntimeException("Test should not run during playback.");
            }))
            .buildClient()
            .createShare(testName);
    }

    @AfterEach
    public void teardown() {
        if (shareClient != null) {
            shareClient.delete();
        }
    }

    // Method to compute checksum using MD5
    private String calculateChecksum(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data);
        return Base64.getEncoder().encodeToString(digest); // Encodes checksum for easy comparison
    }

    /**
     * Tests downloading to file with fault injection.
     * <p>
     * This test will upload a single blob of about 9MB and then download it in parallel 500 times. Each download will
     * have its file contents compared to the original blob data. The test only cares about files that were properly
     * downloaded, if a download fails with a network error it will be ignored. A requirement of 90% of files being
     * successfully downloaded is also a requirement to prevent a case where most files failed to download and passing,
     * hiding a true issue.
     */
    @Test
    public void downloadToFileWithFaultInjection() throws InterruptedException, NoSuchAlgorithmException, IOException, ExecutionException, TimeoutException {
        int outerLoopRuns = 2;         // Outer loop for 5 iterations
        int testRuns = 100;            // Inner loop for 100 concurrent runs
        int length = 30 * Constants.MB - 1;
        Path originalFilePath = setupReadableFile(length);
        byte[] realFileBytes = Files.readAllBytes(originalFilePath);
        String originalChecksum = calculateChecksum(realFileBytes);

        System.out.println("original checksum: " + originalChecksum);

        ShareFileClient fileClient = shareClient.getFileClient(shareClient.getShareName());
        fileClient.create(length);
        //fileClient.uploadWithResponse(new ShareFileUploadOptions(BinaryData.fromBytes(realFileBytes).toStream()), null, Context.NONE);
        fileClient.uploadFromFile(originalFilePath.toString());

        ShareFileClient downloadClient = new ShareFileClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getFileEndpoint())
            .shareName(shareClient.getShareName())
            .resourcePath(shareClient.getShareName())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .httpClient(new HttpFaultInjectingHttpClient(getFaultInjectingWrappedHttpClientWithNetty()))
            .buildFileClient();

        AtomicInteger successCount = new AtomicInteger();
        Map<String, Path> allFailedDownloads = new ConcurrentHashMap<>();
        Map<String, String> allExceptionOccurrences = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int outerRun = 1; outerRun <= outerLoopRuns; outerRun++) {
            System.out.println("Starting outer loop iteration: " + outerRun);

            List<File> files = new ArrayList<>(testRuns);
            URL testFolder = getClass().getClassLoader().getResource("testfiles");
            for (int i = 0; i < testRuns; i++) {
                File file = new File(String.format("%s/%s_%d_debug2.txt", testFolder.getPath(), outerRun, i));
                file.deleteOnExit();
                files.add(file);
            }

            // Use a list of Futures to keep track of submitted tasks
            List<Future<?>> futures = new ArrayList<>();

            for (File it : files) {
                Future<?> future = executor.submit(() -> {
                    try {
                        System.out.println("Starting run for file: " + it.getAbsolutePath());
                        Context context = new Context("filePath", it.getAbsolutePath());
                        downloadClient.downloadToFileWithResponse(it.getAbsolutePath(), null, null, context);
                        byte[] actualFileBytes = Files.readAllBytes(it.toPath());

                        String downloadedChecksum = calculateChecksum(actualFileBytes);
                        System.out.println("downloaded checksum: " + downloadedChecksum);
                        if (!originalChecksum.equals(downloadedChecksum)) {
                            // Save the mismatched file for examination
                            Path mismatchFilePath = Paths.get("C:/azure-sdk-for-java/contentmismatchrepro", it.getName());
                            Files.write(mismatchFilePath, actualFileBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            allFailedDownloads.put(it.getAbsolutePath(), mismatchFilePath);
                            System.out.println("Checksum mismatch for file: " + it.getAbsolutePath() + " saved to: " + mismatchFilePath);
                            LOGGER.atWarning()
                                .addKeyValue("downloadFile", it.getAbsolutePath())
                                .log("File content did not match expected checksum.");
                        } else {
                            LOGGER.atVerbose()
                                .addKeyValue("successCount", successCount.incrementAndGet())
                                .log("Download completed successfully.");
                            System.out.println("Download complete successfully, count: " + successCount);
                        }

                        if (Files.exists(it.toPath())) {
                            FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), it.getName());
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        allExceptionOccurrences.put(it.getAbsolutePath(), ex.getMessage());
                        System.out.println("Error has occurred for file " + it.getAbsolutePath() + ": " + ex.getMessage());
                        LOGGER.atWarning()
                            .addKeyValue("downloadFile", it.getAbsolutePath())
                            .log("Failed to complete download.", ex);
                    }
                });
                futures.add(future);
            }
            // Wait for all tasks in this iteration to complete
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.MINUTES); // Await completion of each task, with timeout
            }
        }

        executor.shutdown(); // Shut down the executor after all tasks are complete

        System.out.println("Total successful downloads: " + successCount.get());
        System.out.println("Expected successful downloads: " + (outerLoopRuns * testRuns));

        // Print accumulated failed downloads
        if (!allFailedDownloads.isEmpty()) {
            System.out.println("Failed Downloads: " + allFailedDownloads.size());
            allFailedDownloads.forEach((filePath, mismatchFilePath) -> {
                System.out.println("File: " + filePath + " saved at: " + mismatchFilePath);
            });
        }

        // Print accumulated exceptions
        if (!allExceptionOccurrences.isEmpty()) {
            System.out.println("Exception Occurrences: " + allExceptionOccurrences.size());
            allExceptionOccurrences.forEach((filePath, message) -> {
                System.out.println("File: " + filePath + " and Exception: " + message);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private HttpClient getFaultInjectingWrappedHttpClientWithNetty() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
            .maxConnections(500)                       // Adjust max connections as needed
            .pendingAcquireTimeout(Duration.ofSeconds(60)) // Increase timeout for acquiring a connection
            .maxIdleTime(Duration.ofSeconds(30))        // Set max idle time for connections
            .maxLifeTime(Duration.ofMinutes(5))         // Set max lifetime for connections
            .build();

        return new NettyAsyncHttpClientBuilder()
            .connectionProvider(connectionProvider)
            .readTimeout(Duration.ofSeconds(60))        // Increase read timeout as needed
            .writeTimeout(Duration.ofSeconds(60))       // Increase write timeout as needed
            .responseTimeout(Duration.ofSeconds(90))    // Increase response timeout as needed
            .build();
    }

    @SuppressWarnings("unchecked")
    private HttpClient getFaultInjectingWrappedHttpClient() {
        switch (ENVIRONMENT.getHttpClientType()) {
            case NETTY:
                System.out.println("Using netty");
                return HttpClient.createDefault(new HttpClientOptions()
                    .readTimeout(Duration.ofSeconds(2))
                    //.responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(NettyAsyncHttpClientProvider.class));
            case OK_HTTP:
                System.out.println("Using ok_http");
                return HttpClient.createDefault(new HttpClientOptions()
                    .readTimeout(Duration.ofSeconds(2))
                    //.responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(OkHttpAsyncClientProvider.class));
            case VERTX:
                System.out.println("using vertx");
                return HttpClient.createDefault(new HttpClientOptions()
                    .readTimeout(Duration.ofSeconds(2))
                    //.responseTimeout(Duration.ofSeconds(2))
                    .setHttpClientProvider(getVertxClientProviderReflectivelyUntilNameChangeReleases()));
            case JDK_HTTP:
                try {
                    System.out.println("using jdk_http");
                    return HttpClient.createDefault(new HttpClientOptions()
                        .readTimeout(Duration.ofSeconds(2))
                        //.responseTimeout(Duration.ofSeconds(2))
                        .setHttpClientProvider((Class<? extends HttpClientProvider>) Class.forName(
                            "com.azure.core.http.jdk.httpclient.JdkHttpClientProvider")));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }

            default:
                throw new IllegalArgumentException("Unknown http client type: " + ENVIRONMENT.getHttpClientType());
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends HttpClientProvider> getVertxClientProviderReflectivelyUntilNameChangeReleases() {
        Class<?> clazz;
        try {
            clazz = Class.forName("com.azure.core.http.vertx.VertxHttpClientProvider");
        } catch (ClassNotFoundException ex) {
            try {
                clazz = Class.forName("com.azure.core.http.vertx.VertxAsyncHttpClientProvider");
            } catch (ClassNotFoundException ex2) {
                ex2.addSuppressed(ex);
                throw new RuntimeException(ex2);
            }
        }

        return (Class<? extends HttpClientProvider>) clazz;
    }

    /**
     * Creates a readable file of the specified size if it doesn't already exist.
     *
     * @return Path to the generated file.
     * @throws IOException if an I/O error occurs during file creation.
     */
    public static Path setupReadableFile(int fileSize) throws IOException {
        Path originalDataPath = Paths.get("C:/azure-sdk-for-java/contentmismatchrepro/original_data.txt");
        if (Files.exists(originalDataPath) && Files.size(originalDataPath) == fileSize) {
            System.out.println("File already exists at: " + originalDataPath.toAbsolutePath());
            return originalDataPath; // Return existing file if it matches the required size
        }

        System.out.println("Creating new file at: " + originalDataPath.toAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(originalDataPath.toFile()))) {
            Random random = new Random();
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

            // Write characters in chunks to manage memory usage
            int chunkSize = 1024; // Write in 1 KB chunks
            int written = 0;

            while (written < fileSize) {
                StringBuilder chunk = new StringBuilder(chunkSize);
                for (int i = 0; i < chunkSize && written + i < fileSize; i++) {
                    chunk.append(characters.charAt(random.nextInt(characters.length())));
                }
                writer.write(chunk.toString());
                written += chunkSize;
            }
        }

        System.out.println("File created at: " + originalDataPath.toAbsolutePath() + " with size: " + Files.size(originalDataPath));
        return originalDataPath;
    }

    // For now a local implementation is here in azure-storage-blob until this is released in azure-core-test.
    // Since this is a local definition with a clear set of configurations everything is simplified.
    private static final class HttpFaultInjectingHttpClient implements HttpClient {
        private final HttpClient wrappedHttpClient;

        HttpFaultInjectingHttpClient(HttpClient wrappedHttpClient) {
            this.wrappedHttpClient = wrappedHttpClient;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return send(request, Context.NONE);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl();
            String filePath = context.getData("filePath").orElse("unknown file path").toString();
            //String fileName = extractFileNameFromUrl(originalUrl); // Extract the file name from the URL
            String faultType = faultInjectorHandling();
            String range = request.getHeaders().getValue("x-ms-range");
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);
            logRequestDetails(request, filePath, faultType, range);
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));

// add range header to see the requested bytes


            return wrappedHttpClient.send(request, context)
                .map(response -> {
                    HttpRequest request1 = response.getRequest();
                    request1.getHeaders().remove(UPSTREAM_URI_HEADER);
                    request1.setUrl(originalUrl);
                    logResponseDetails(response, filePath);

                    return response;
                });
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            URL originalUrl = request.getUrl();
            //String fileName = extractFileNameFromUrl(originalUrl); // Extract the file name from the URL
            String filePath = context.getData("filePath").orElse("unknown file path").toString();
            String faultType = faultInjectorHandling();
            String range = request.getHeaders().getValue("x-ms-range");
            request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);
            logRequestDetails(request, filePath, faultType, range);
            request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));

            HttpResponse response = wrappedHttpClient.sendSync(request, context);
            response.getRequest().setUrl(originalUrl);
            response.getRequest().getHeaders().remove(UPSTREAM_URI_HEADER);
            logResponseDetails(response, filePath);

            return response;
        }

        private static String extractFileNameFromUrl(URL url) {
            String path = url.getPath();
            String[] pathSegments = path.split("/");
            return pathSegments[pathSegments.length - 1]; // Assume the last segment is the file name
        }

        private static void logRequestDetails(HttpRequest request, String fileName, String faultType, String range) {
            LOGGER.atInfo()
                .addKeyValue("FileName", fileName)
                .addKeyValue("Request URL", request.getUrl())
                .addKeyValue("Fault Type", faultType)
                .addKeyValue("x-ms-range", range)
                .log("Sending request associated with file.");
        }

        private static void logResponseDetails(HttpResponse response, String fileName) {
            LOGGER.atInfo()
                .addKeyValue("FileName", fileName)
                .addKeyValue("Status Code", response.getStatusCode())
                .log("Received response for file.");
        }

        private static URL rewriteUrl(URL originalUrl) {
            try {
                return UrlBuilder.parse(originalUrl)
                    .setScheme("http")
                    .setHost("localhost")
                    .setPort(7777)
                    .toUrl();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        private static List<Tuple2<Double, String>> addResponseFaultedProbabilities() {
            // f: Full response
            // p: Partial Response (full headers, 50% of body), then wait indefinitely
            // pc: Partial Response (full headers, 50% of body), then close (TCP FIN)
            // pa: Partial Response (full headers, 50% of body), then abort (TCP RST)
            // pn: Partial Response (full headers, 50% of body), then finish normally
            // n: No response, then wait indefinitely
            // nc: No response, then close (TCP FIN)
            // na: No response, then abort (TCP RST)
            List<Tuple2<Double, String>> probabilities = new ArrayList<>();
            probabilities.add(Tuples.of(0.1, "p"));
            probabilities.add(Tuples.of(0.1, "pc"));
            probabilities.add(Tuples.of(0.1, "pa"));
            probabilities.add(Tuples.of(0.1, "pn"));
            probabilities.add(Tuples.of(0.003, "n"));
            probabilities.add(Tuples.of(0.004, "nc"));
            probabilities.add(Tuples.of(0.003, "na"));
            return probabilities;
        }

        private static String faultInjectorHandling() {
            List<Tuple2<Double, String>> probabilities = addResponseFaultedProbabilities();
            double random = Math.random();
            double sum = 0d;

            for (Tuple2<Double, String> tup : probabilities) {
                if (random < sum + tup.getT1()) {
                    return tup.getT2();
                }
                sum += tup.getT1();
            }
            return "f";
        }
    }

    private static boolean shouldRun() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        // macOS has known issues running HTTP fault injector, change this once
        // https://github.com/Azure/azure-sdk-tools/pull/6216 is resolved
        return ENVIRONMENT.getTestMode() == TestMode.LIVE
            && !osName.contains("mac os")
            && !osName.contains("darwin");
    }
}
