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
package org.modeshape.jcr.mimetype;

/**
 * Use for testing only.
 */
public final class MimeTypeConstants {

    private MimeTypeConstants() {
    }

    public static final String TEXT_PLAIN = "text/plain";
    public static final String RTF = "text/rtf";
    public static final String HTML = "text/html";
    public static final String WSDL = "application/wsdl+xml";
    public static final String APPLICATION_XML = "application/xml";
    public static final String TEXT_XML = "text/xml";
    public static final String HTML_XML = "application/xhtml+xml";
    public static final String XOP_XML = "application/xop+xml";
    public static final String XSLT = "application/xslt+xml";
    public static final String XSFP = "application/xsfp+xml";
    public static final String MXML = "application/xv+xml";

    /** This is not a valid MIME type, but we're using it in case other people use it */
    @Deprecated
    public static final String XSD = "application/xsd";
    @Deprecated
    public static final String XSD_XML = "application/xsd+xml";
    public static final String WSDL_XML = "application/wsdl+xml";

    public static final String MP3 = "audio/mpeg";
    public static final String WAV = "audio/x-wav";
    public static final String AU = "audio/basic";
    public static final String OGG = "application/x-ogg";

    public static final String FLI = "video/x-fli";
    public static final String BMP = "image/bmp";
    public static final String GIF = "image/gif";
    public static final String ICON = "image/x-icon";
    public static final String JPEG = "image/jpeg";
    public static final String PBM = "image/x-portable-bitmap";
    public static final String PGM = "image/x-portable-graymap";
    public static final String PPM = "image/x-portable-pixmap";
    public static final String PORTABLE_PIXMAP = "image/x-portable-pixmap";
    public static final String PNG = "image/png";
    public static final String RAS = "image/x-cmu-raster";
    public static final String TIFF = "image/tiff";
    public static final String TGA = "image/x-tga";
    public static final String WMF = "image/wmf";
    public static final String XCF = "image/xcf";
    public static final String XPM = "image/xpm";
    public static final String PHOTOSHOP = "image/vnd.adobe.photoshop";
    public static final String PCX = "image/x-pcx";
    public static final String PICT = "image/x-pict";

    public static final String JAVA = "text/java";
    public static final String JAVA_CLASS = "application/x-java-class";
    public static final String JAR = "application/java-archive";
    public static final String JAR_MANIFEST = "application/x-java-manifest";

    public static final String PDF = "application/pdf";
    public static final String POSTSCRIPT = "application/postscript";

    public static final String GZIP = "application/gzip";
    public static final String ZIP = "application/zip";
    public static final String LZW = "application/x-compress";
    public static final String TAR = "application/x-tar";
    public static final String GTAR = "application/x-gtar";

    public static final String BASH = "application/x-bash";
    public static final String SH = "application/x-sh";

    public static final String OPEN_DOC_FORMULA = "application/vnd.oasis.opendocument.formula";
    public static final String OPEN_DOC_GRAPHICS = "application/vnd.oasis.opendocument.graphics";
    public static final String OPEN_DOC_GRAPHICS_TEMPLATE = "application/vnd.oasis.opendocument.graphics-template";
    public static final String OPEN_DOC_PRESENTATION = "application/vnd.oasis.opendocument.presentation";
    public static final String OPEN_DOC_PRESENTATION_TEMPLATE = "application/vnd.oasis.opendocument.presentation-template";
    public static final String OPEN_DOC_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";
    public static final String OPEN_DOC_SPREADSHEET_TEMPLATE = "application/vnd.oasis.opendocument.spreadsheet-template";
    public static final String OPEN_DOC_TEXT = "application/vnd.oasis.opendocument.text";
    public static final String OPEN_DOC_TEXT_TEMPLATE = "application/vnd.oasis.opendocument.text-template";

    public static final String OPEN_OFFICE_CALC = "application/vnd.sun.xml.calc";
    public static final String OPEN_OFFICE_CALC_TEMPLATE = "application/vnd.sun.xml.calc.template";
    public static final String OPEN_OFFICE_DRAW = "application/vnd.sun.xml.draw";
    public static final String OPEN_OFFICE_DRAW_TEMPLATE = "application/vnd.sun.xml.draw.template";
    public static final String OPEN_OFFICE_IMPRESS = "application/vnd.sun.xml.impress";
    public static final String OPEN_OFFICE_IMPRESS_TEMPLATE = "application/vnd.sun.xml.impress.template";
    public static final String OPEN_OFFICE_WRITER = "application/vnd.sun.xml.writer";
    public static final String OPEN_OFFICE_WRITER_TEMPLATE = "application/vnd.sun.xml.writer.template";

    public static final String STAR_OFFICE_CALC = "application/vnd.stardivision.calc";
    public static final String STAR_OFFICE_DRAW = "application/vnd.stardivision.draw";
    public static final String STAR_OFFICE_IMPRESS = "application/vnd.stardivision.impress";
    public static final String STAR_OFFICE_WRITER = "application/vnd.stardivision.writer";

    public static final String MICROSOFT_APPLICATION_MS_WORD = "application/msword";
    public static final String MICROSOFT_OFFICE = "application/vnd.ms-office";
    public static final String MICROSOFT_WORD = "application/vnd.ms-word";
    public static final String MICROSOFT_WORD_OPEN_XML = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MICROSOFT_OFFICE_DOCUMENT_OPENXML = "application/vnd.openxmlformats-officedocument.wordprocessingml";
    public static final String MICROSOFT_WORKS = "application/vnd.ms-works";
    public static final String MICROSOFT_EXCEL = "application/vnd.ms-excel";
    public static final String MICROSOFT_EXCEL_OPENXML = "application/vnd.openxmlformats-officedocument.spreadsheetml";
    public static final String MICROSOFT_POWERPOINT_OPENXML = "application/vnd.openxmlformats-officedocument.presentationml";
    public static final String MICROSOFT_POWERPOINT = "application/vnd.ms-powerpoint";
    public static final String MICROSOFT_PUBLISHER = "application/x-mspublisher";
    public static final String MICROSOFT_VISIO = "application/vnd.visio";
    public static final String MICROSOFT_OUTLOOK = "application/vnd.ms-outlook";

    public static final String COREL_PRESENTATION = "application/presentations";
    public static final String COREL_QUATTRO_SPREADSHEET = "application/wb2";
    public static final String COREL_QUATTRO_PRO = "application/x-quattropro";
    public static final String COREL_WORD_PERFECT = "application/vnd.wordperfect";

    public static final String MESSAGE_RFC = "message/rfc822";
    public static final String MOZILLA_ADDRESS_BOOK = "application/x-mozilla-addressbook";
    public static final String VCARD = "text/x-vcard";
    public static final String CALENDAR = "text/calendar";

    public static final String OCTET_STREAM = "application/octet-stream";

}
