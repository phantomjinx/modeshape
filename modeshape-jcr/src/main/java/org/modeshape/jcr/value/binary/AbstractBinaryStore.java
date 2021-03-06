/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.jcr.value.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.jcr.RepositoryException;
import org.modeshape.common.annotation.ThreadSafe;
import org.modeshape.common.logging.Logger;
import org.modeshape.common.util.CheckArg;
import org.modeshape.common.util.StringUtil;
import org.modeshape.jcr.TextExtractors;
import org.modeshape.jcr.mimetype.MimeTypeDetector;
import org.modeshape.jcr.mimetype.NullMimeTypeDetector;
import org.modeshape.jcr.text.TextExtractorContext;
import org.modeshape.jcr.value.BinaryKey;
import org.modeshape.jcr.value.BinaryValue;

/**
 * An abstract class for a {@link BinaryStore}, with common functionality needed by implementation classes.
 */
@ThreadSafe
public abstract class AbstractBinaryStore implements BinaryStore {

    /**
     * The default minimum size (in bytes) of binary values that are persisted in the binary store is 4096 bytes, or 4KB.
     */
    static final long DEFAULT_MINIMUM_BINARY_SIZE_IN_BYTES = 1024 * 4;

    private static final long DEFAULT_LATCH_WAIT_IN_SECONDS = 10L;

    private static final long LARGE_SIZE = 1 << 25; // 32MB
    private static final long MEDIUM_FILE_SIZE = 1 << 20; // 1MB
    private static final long SMALL_FILE_SIZE = 1 << 15; // 32K
    private static final long TINY_FILE_SIZE = 1 << 10; // 1K

    private static final int LARGE_BUFFER_SIZE = 1 << 20; // 1MB
    protected static final int MEDIUM_BUFFER_SIZE = 1 << 16; // 64K
    private static final int SMALL_BUFFER_SIZE = 1 << 12; // 4K
    private static final int TINY_BUFFER_SIZE = 1 << 11; // 2K

    protected final Logger logger = Logger.getLogger(getClass());
    private final AtomicLong minBinarySizeInBytes = new AtomicLong(DEFAULT_MINIMUM_BINARY_SIZE_IN_BYTES);

    private volatile TextExtractors extractors;
    private volatile MimeTypeDetector detector = NullMimeTypeDetector.INSTANCE;

    /**
     * Given a number of bytes representing the length of a file, returns the optimum size for a buffer that should be used
     * when reading/working with that file
     *
     * @param fileSize the size of a file, in bytes
     * @return the size of a read/write buffer
     */
    public static int bestBufferSize( long fileSize ) {
        assert fileSize >= 0;
        if (fileSize < TINY_FILE_SIZE) {
            return (int)fileSize + 2;
        }
        if (fileSize < SMALL_FILE_SIZE) {
            return TINY_BUFFER_SIZE;
        }
        if (fileSize < MEDIUM_FILE_SIZE) {
            return SMALL_BUFFER_SIZE;
        }
        if (fileSize < LARGE_SIZE) {
            return MEDIUM_BUFFER_SIZE;
        }
        return LARGE_BUFFER_SIZE;
    }


    @Override
    public long getMinimumBinarySizeInBytes() {
        return minBinarySizeInBytes.get();
    }

    @Override
    public void setMinimumBinarySizeInBytes( long minSizeInBytes ) {
        CheckArg.isNonNegative(minSizeInBytes, "minSizeInBytes");
        minBinarySizeInBytes.set(minSizeInBytes);
    }

    @Override
    public void setTextExtractors( TextExtractors textExtractors ) {
        CheckArg.isNotNull(textExtractors, "textExtractors");
        this.extractors = textExtractors;
    }

    @Override
    public void setMimeTypeDetector( MimeTypeDetector mimeTypeDetector ) {
        this.detector = mimeTypeDetector != null ? mimeTypeDetector : NullMimeTypeDetector.INSTANCE;
    }

    @Override
    public final String getText( BinaryValue binary ) throws BinaryStoreException {
        // try and locate an already extracted text from the store
        if (binary instanceof StoredBinaryValue) {
            String extractedText = getExtractedText(binary);
            if (extractedText != null) {
                return extractedText;
            }
        }

        // there isn't extracted text stored, so try to extract text if extraction is enabled
        if (extractors == null || !extractors.extractionEnabled()) {
            return null;
        }

        if (binary instanceof InMemoryBinaryValue) {
            // The extracted text will never be stored, so try directly using the text extractors ...
            return extractors.extract((InMemoryBinaryValue)binary, new TextExtractorContext(detector()));
        }

        // there isn't any text available, so wait for a job to finish and then return the result
        try {
            CountDownLatch latch = extractors.getWorkerLatch(binary.getKey(), false);
            if (latch == null) {
                // There is no latch, so just compute the text here ...
                latch = extractors.extract(this, binary, new TextExtractorContext(detector()));
            }
            // There was a latch, so wait till the work is done ...
            if (latch != null && latch.await(DEFAULT_LATCH_WAIT_IN_SECONDS, TimeUnit.SECONDS)) {
                return getExtractedText(binary);
            }
            // Stopped waiting ...
            return null;
        } catch (InterruptedException e) {
            throw new BinaryStoreException(e);
        }
    }

    @Override
    public String getMimeType( BinaryValue binary,
                               String name ) throws IOException, RepositoryException {
        if (binary instanceof StoredBinaryValue) {
            String storedMimeType = getStoredMimeType(binary);
            if (!StringUtil.isBlank(storedMimeType)) {
                return storedMimeType;
            }
        }

        if (detector == null) {
            return null;
        }

        String detectedMimeType = detector().mimeTypeOf(name, binary);
        if (binary instanceof InMemoryBinaryValue) {
            return detectedMimeType;
        } else if (!StringUtil.isBlank(detectedMimeType)) {
            storeMimeType(binary, detectedMimeType);
        }
        return detectedMimeType;
    }

    @Override
    public boolean hasBinary( BinaryKey key ) {
        try {
            InputStream is = getInputStream(key);
            is.close();
        } catch (BinaryStoreException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns the stored mime-type of a binary value.
     *
     * @param binaryValue a {@code non-null} {@link BinaryValue}
     * @return either a non-empty {@code String} if a stored mimetype exists, or {@code null} if such a value doesn't exist yet.
     * @throws BinaryStoreException if there's a problem accessing the binary store or if the binary value cannot be found in the
     * store
     */
    protected abstract String getStoredMimeType( BinaryValue binaryValue ) throws BinaryStoreException;

    /**
     * Stores the given mime-type for a binary value.
     *
     * @param binaryValue a {@code non-null} {@link BinaryValue}
     * @param mimeType a non-empty {@code String}
     * @throws BinaryStoreException if there's a problem accessing the binary store
     */
    protected abstract void storeMimeType( BinaryValue binaryValue,
                                           String mimeType ) throws BinaryStoreException;

    /**
     * Stores the extracted text of a binary value into this store.
     *
     * @param source a {@code non-null} {@link BinaryValue} instance from which the text was extracted
     * @param extractedText a {@code non-null} and {@code non-blank} string representing the extracted text
     * @throws BinaryStoreException if the operation fails or if the extracted text cannot be stored for the given binary value
     * (regardless of the reason)
     */
    public abstract void storeExtractedText( BinaryValue source,
                                             String extractedText ) throws BinaryStoreException;

    /**
     * Returns the extracted text of a binary value, or {@code null} if such text hasn't been stored previously (but the binary
     * value can be found in the store)
     *
     * @param source a {@code non-null} {@link BinaryValue} instance from which the text was extracted
     * @return a {@code String} representing the extracted text, or {@code null} if such text hasn't been stored in this store
     *         previously.
     * @throws BinaryStoreException if the binary value cannot be found in the store.
     */
    public abstract String getExtractedText( BinaryValue source ) throws BinaryStoreException;

    /**
     * Get the text extractor that can be used to extract text by this store.
     *
     * @return the text extractor; never null
     */
    protected final TextExtractors extractors() {
        return this.extractors;
    }

    /**
     * Get the MIME type detector that can be used to find the MIME type for binary content
     *
     * @return the detector; never null
     */
    protected final MimeTypeDetector detector() {
        return detector;
    }

    @Override
    public BinaryValue storeValue( InputStream stream, String hint, boolean markAsUnused ) throws BinaryStoreException {
        return storeValue(stream, markAsUnused);
    }

    @Override
    public void start() {
        //does nothing by default
    }

    @Override
    public void shutdown() {
        //does nothing by default
    }
}
