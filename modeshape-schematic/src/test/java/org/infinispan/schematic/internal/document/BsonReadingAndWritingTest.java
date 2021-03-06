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
package org.infinispan.schematic.internal.document;

import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.bson.BSONObject;
import org.bson.BasicBSONCallback;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.BasicBSONList;
import org.codehaus.jackson.JsonToken;
import org.infinispan.schematic.FixFor;
import org.infinispan.schematic.TestUtil;
import org.infinispan.schematic.document.Binary;
import org.infinispan.schematic.document.Code;
import org.infinispan.schematic.document.CodeWithScope;
import org.infinispan.schematic.document.Document;
import org.infinispan.schematic.document.Json;
import org.infinispan.schematic.document.MaxKey;
import org.infinispan.schematic.document.MinKey;
import org.infinispan.schematic.document.ObjectId;
import org.infinispan.schematic.document.Symbol;
import org.infinispan.schematic.document.Timestamp;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BsonReadingAndWritingTest {

    protected BsonReader reader;
    protected BsonWriter writer;
    protected Document input;
    protected Document output;
    protected boolean print;

    @Before
    public void beforeTest() {
        reader = new BsonReader();
        writer = new BsonWriter();
        print = false;
    }

    @After
    public void afterTest() {
        reader = null;
        writer = null;
    }

    @Test
    public void shouldReadExampleBsonStream() throws IOException {
        // "\x16\x00\x00\x00\x02hello\x00\x06\x00\x00\x00world\x00\x00"
        byte[] bytes = new byte[] {0x16, 0x00, 0x00, 0x00, 0x02, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x00, 0x06, 0x00, 0x00, 0x00,
            0x77, 0x6f, 0x72, 0x6c, 0x64, 0x00, 0x00};
        output = reader.read(new ByteArrayInputStream(bytes));
        String json = Json.write(output);
        String expected = "{ \"hello\" : \"world\" }";
        if (print) {
            System.out.println(json);
            System.out.flush();
        }
        assert expected.equals(json);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithStringValue() {
        input = new BasicDocument("name", "Joe");
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithBooleanValue() {
        input = new BasicDocument("foo", 3L);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithIntValue() {
        input = new BasicDocument("foo", 3);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithLongValue() {
        input = new BasicDocument("foo", 3L);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithFloatValue() {
        input = new BasicDocument("foo", 3.0f);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithDoubleValue() {
        input = new BasicDocument("foo", 3.0d);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithDateValue() {
        input = new BasicDocument("foo", new Date());
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithTimestampValue() {
        input = new BasicDocument("foo", new Timestamp(new Date()));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithObjectId() {
        // print = true;
        int time = Math.abs((int) new Date().getTime());
        if (print) System.out.println("time value: " + time);
        input = new BasicDocument("foo", new ObjectId(time, 1, 2, 3));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithCode() {
        input = new BasicDocument("foo", new Code("bar"));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithCodeWithScope() {
        Document scope = new BasicDocument("baz", "bam", "bak", "bat");
        input = new BasicDocument("foo", new CodeWithScope("bar", scope));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithMaxKey() {
        input = new BasicDocument("foo", MaxKey.getInstance());
        assertRoundtrip(input, false);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithMinKey() {
        input = new BasicDocument("foo", MinKey.getInstance());
        assertRoundtrip(input, false);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithSymbol() {
        input = new BasicDocument("foo", new Symbol("bar"));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithNull() {
        input = new BasicDocument("foo", null);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithBinary1() {
        byte[] data = new byte[] {0x16, 0x00, 0x00, 0x00, 0x02, 0x68, 0x65, 0x6c};
        input = new BasicDocument("foo", new Binary(data));
        assertRoundtrip(input);
    }

    @Test
    //Fix-For MODE-1575
    public void shouldRoundTripSimpleBsonObjectWithBinary2() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = getClass().getClassLoader().getResourceAsStream("binary");
        assertNotNull(is);
        try {
            byte[] buff = new byte[1024];
            int read;
            while ((read = is.read(buff)) != -1) {
                bos.write(buff, 0, read);
            }
        } finally {
            bos.close();
            is.close();
        }

        input = new BasicDocument("foo", new Binary(bos.toByteArray()));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithUuid() {
        input = new BasicDocument("foo", UUID.randomUUID());
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithPattern() {
        // print = true;
        input = new BasicDocument("foo", Pattern.compile("[CH]at\\s+"));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithPatternAndFlags() {
        // print = true;
        input = new BasicDocument("foo", Pattern.compile("[CH]at\\s+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripSimpleBsonObjectWithArray() {
        BasicArray array = new BasicArray();
        array.addValue("value1");
        array.addValue(new Symbol("value2"));
        array.addValue(30);
        array.addValue(40L);
        array.addValue(4.33d);
        array.addValue(false);
        array.addValue(null);
        array.addValue("value2");
        input = new BasicDocument("foo", array);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripBsonObjectWithTwoFields() {
        input = new BasicDocument("name", "Joe", "age", 35);
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripBsonObjectWithThreeFields() {
        input = new BasicDocument("name", "Joe", "age", 35, "nick", "joey");
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripBsonObjectWithNestedDocument() {
        BasicDocument address = new BasicDocument("street", "100 Main", "city", "Springfield", "zip", 12345);
        input = new BasicDocument("name", "Joe", "age", 35, "address", address, "nick", "joey");
        assertRoundtrip(input);
    }

    @Test
    public void shouldRoundTripLargeModeShapeDocument() throws Exception {
        Document doc = Json.read(TestUtil.resource("json/sample-large-modeshape-doc.json"));
        // OutputStream os = new FileOutputStream("src/test/resources/json/sample-large-modeshape-doc2.json");
        // Json.writePretty(doc, os);
        // os.flush();
        // os.close();
        assertRoundtrip(doc);
    }

    @Test
    @FixFor( "MODE-2074" )
    public void shouldRoundTripBsonWithLargeStringField() throws Exception {
        //use a string which overflows the default buffer BufferCache.MINIMUM_SIZE
        final String largeString = readFile("json/sample-large-modeshape-doc3.json");
        Document document = new BasicDocument("largeString", largeString);
        assertRoundtrip(document);
    }

    @Test
    @FixFor( "MODE-2074" )
    public void shouldRoundTripBsonWithLargeStringFieldFromMultipleThreads() throws Exception {
        final String largeString = readFile("json/sample-large-modeshape-doc3.json");
        int threadCount = 10;
        List<Future<Void>> results = new ArrayList<Future<Void>>();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            results.add(executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Document document = new BasicDocument("largeString", largeString);
                    assertRoundtrip(document);
                    return null;
                }
            }));
        }

        for (Future<Void> result : results) {
            result.get(1, TimeUnit.SECONDS);
        }
    }
    
    @Test
    @FixFor( "MODE-2430" )
    public void shouldRoundTripLargeStringsSuccessively() throws Exception {
        int limit = 1048 * 8; // the default buffer size
        int iterations = 20;
        char letter = 'a';
        for (int i = 0; i < iterations; i++) {
            int size = limit + i;
            char[] chars = new char[size];
            Arrays.fill(chars, letter);
            letter = (char)((byte) letter + 1);
            String str = new String(chars);
            Document document = new BasicDocument("largeString", str);
            assertRoundtrip(document, false);
        }
    }

    protected String readFile(String filePath) throws IOException {
        InputStreamReader reader = new InputStreamReader(TestUtil.resource(filePath));
        StringBuilder stringBuilder = new StringBuilder();
        boolean error = false;
        try {
            int numRead = 0;
            char[] buffer = new char[1024];
            while ((numRead = reader.read(buffer)) > -1) {
                stringBuilder.append(buffer, 0, numRead);
            }
        } catch (IOException e) {
            error = true; // this error should be thrown, even if there is an error closing reader
            throw e;
        } catch (RuntimeException e) {
            error = true; // this error should be thrown, even if there is an error closing reader
            throw e;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                if (!error) throw e;
            }
        }
        return stringBuilder.toString();
    }

    protected void assertRoundtrip( Document input ) {
        assertRoundtrip(input, true);
    }

    protected void assertRoundtrip( Document input,
                                    boolean compareToOtherImpls ) {
        assert input != null;
        Document output = writeThenRead(input, compareToOtherImpls);
        if (print) {
            System.out.println("********************************************************************************");
            System.out.println("INPUT :  " + input);
            System.out.println();
            System.out.println("OUTPUT:  " + output);
            System.out.println("********************************************************************************");
            System.out.flush();
        }
        Assert.assertEquals("Round trip failed", input, output);
    }

    protected Document writeThenRead( Document object,
                                      boolean compareToOtherImpls ) {
        try {
            long start = System.nanoTime();
            byte[] bytes = writer.write(object);
            long writeTime = System.nanoTime() - start;

            start = System.nanoTime();
            Document result = reader.read(new ByteArrayInputStream(bytes));
            long readTime = System.nanoTime() - start;

            if (compareToOtherImpls) {
                // Convert to MongoDB, write to bytes, and compare ...
                BSONObject mongoData = createMongoData(object);
                start = System.nanoTime();
                byte[] mongoBytes = new BasicBSONEncoder().encode(mongoData);
                long mongoWriteTime = System.nanoTime() - start;
                assertSame(bytes, mongoBytes, "BSON   ", "Mongo  ");

                // FYI: The Jackson BSON library writes several of the types incorrectly,
                // whereas the MongoDB library seems to write things per the spec.

                // // Convert to Jackson BSON, write to bytes, and compare ...
                // ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                // ObjectMapper om = new ObjectMapper(new BsonFactory());
                // Map<String, Object> jacksonData = createJacksonData(object);
                // om.writeValue(stream2, jacksonData);
                // byte[] jacksonBytes = stream2.toByteArray();
                // assertSame(bytes, jacksonBytes, "BSON   ", "Jackson");

                start = System.nanoTime();
                new BasicBSONDecoder().decode(bytes, new BasicBSONCallback());
                long mongoReadTime = System.nanoTime() - start;

                Document fromMongo = reader.read(new ByteArrayInputStream(mongoBytes));
                if (!fromMongo.equals(result)) {
                    System.out.println("from Schematic: " + result);
                    System.out.println("from Mongo:     " + fromMongo);
                    assert false : "Document read from bytes written by Mongo did not match expected document: " + result;
                }

                if (print) {
                    System.out.println("Reading with Schematic:  " + percent(readTime, mongoReadTime) + " than Mongo");
                    System.out.println("Writing with Schematic:  " + percent(writeTime, mongoWriteTime) + " than Mongo");
                }
            }

            return result;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    protected String time( long nanos ) {
        return "" + TimeUnit.NANOSECONDS.convert(nanos, TimeUnit.NANOSECONDS) + "ns";
    }

    protected String percent( long nanos1,
                              long nanos2 ) {
        float percent = 100.0f * (float)(((double)nanos2 - (double)nanos1) / nanos1);
        if (percent < 0.0d) {
            return "" + -percent + "% slower";
        }
        return "" + percent + "% faster";
    }

    protected BSONObject createMongoData( Document document ) {
        BSONObject obj = new BasicBSONObject();
        for (Document.Field field : document.fields()) {
            Object value = field.getValue();
            obj.put(field.getName(), createMongoData(value));
        }
        return obj;
    }

    protected Object createMongoData( Object value ) {
        if (value instanceof MinKey) {
            value = "MinKey";
        } else if (value instanceof MaxKey) {
            value = "MaxKey";
        } else if (value instanceof Symbol) {
            Symbol symbol = (Symbol)value;
            value = new org.bson.types.Symbol(symbol.getSymbol());
        } else if (value instanceof ObjectId) {
            ObjectId id = (ObjectId)value;
            value = new org.bson.types.ObjectId(id.getBytes());
        } else if (value instanceof Timestamp) {
            Timestamp ts = (Timestamp)value;
            value = new BSONTimestamp(ts.getTime(), ts.getInc());
        } else if (value instanceof CodeWithScope) {
            CodeWithScope code = (CodeWithScope)value;
            value = new org.bson.types.CodeWScope(code.getCode(), createMongoData(code.getScope()));
        } else if (value instanceof Code) {
            Code code = (Code)value;
            value = new org.bson.types.Code(code.getCode());
        } else if (value instanceof Binary) {
            Binary binary = (Binary)value;
            value = new org.bson.types.Binary(binary.getBytes());
        } else if (value instanceof List) {
            List<?> values = (List<?>)value;
            BasicBSONList newValues = new BasicBSONList();
            for (Object v : values) {
                newValues.add(createMongoData(v));
            }
            value = newValues;
        } else if (value instanceof Document) {
            value = createMongoData((Document)value);
        }
        return value;
    }

    protected Map<String, Object> createJacksonData( Document document ) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        for (Document.Field field : document.fields()) {
            Object value = field.getValue();
            data.put(field.getName(), createJacksonData(value));
        }
        return data;
    }

    protected Object createJacksonData( Object value ) {
        if (value instanceof MinKey) {
            value = JsonToken.VALUE_STRING;
        } else if (value instanceof MaxKey) {
            value = JsonToken.VALUE_STRING;
        } else if (value instanceof Symbol) {
            value = new de.undercouch.bson4jackson.types.Symbol(((Symbol)value).getSymbol());
        } else if (value instanceof ObjectId) {
            ObjectId id = (ObjectId)value;
            value = new de.undercouch.bson4jackson.types.ObjectId(id.getTime(), id.getMachine(), id.getInc());
        } else if (value instanceof Timestamp) {
            Timestamp ts = (Timestamp)value;
            value = new de.undercouch.bson4jackson.types.Timestamp(ts.getTime(), ts.getInc());
        } else if (value instanceof CodeWithScope) {
            CodeWithScope code = (CodeWithScope)value;
            value = new de.undercouch.bson4jackson.types.JavaScript(code.getCode(), createJacksonData(code.getScope()));
        } else if (value instanceof Code) {
            Code code = (Code)value;
            value = new de.undercouch.bson4jackson.types.JavaScript(code.getCode(), null);
        } else if (value instanceof List) {
            List<?> values = (List<?>)value;
            List<Object> newValues = new ArrayList<Object>(values.size());
            for (Object v : values) {
                newValues.add(createJacksonData(v));
            }
            value = newValues;
        } else if (value instanceof Document) {
            value = createJacksonData((Document)value);
        }
        return value;
    }

    protected void assertSame( byte[] b1,
                               byte[] b2,
                               String name1,
                               String name2 ) {
        if (b1.equals(b2)) return;
        int s1 = b1.length;
        int s2 = b2.length;
        String sb1 = toString(b1);
        String sb2 = toString(b2);
        if (!sb1.equals(sb2)) {
            System.out.println(name1 + " size: " + padLeft(s1, 3) + " content: " + sb1);
            System.out.println(name2 + " size: " + padLeft(s2, 3) + " content: " + sb2);
            assert false;
        }
    }

    protected String padLeft( Object value,
                              int width ) {
        String result = value != null ? value.toString() : "null";
        while (result.length() < width) {
            result = " " + result;
        }
        return result;
    }

    protected String toString( byte[] bytes ) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(padLeft((int)b, 4)).append(' ');
        }
        return sb.toString();
    }

}
