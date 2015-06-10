package jp.tsur.booksearch.data.api;

import com.squareup.okhttp.MediaType;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * A {@link retrofit.converter.Converter} which uses SimpleXML for reading and writing entities.
 *
 * @author Fabien Ric (fabien.ric@gmail.com)
 */
public class SimpleXMLConverter implements Converter {
    private static final boolean DEFAULT_STRICT = true;
    private static final String CHARSET = "UTF-8";
    private static final MediaType MEDIA_TYPE =
            MediaType.parse("application/xml; charset=" + CHARSET);

    private final Serializer serializer;

    private final boolean strict;

    public SimpleXMLConverter() {
        this(DEFAULT_STRICT);
    }

    public SimpleXMLConverter(boolean strict) {
        this(new Persister(), strict);
    }

    public SimpleXMLConverter(Serializer serializer) {
        this(serializer, DEFAULT_STRICT);
    }

    public SimpleXMLConverter(Serializer serializer, boolean strict) {
        this.serializer = serializer;
        this.strict = strict;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        String charset = "UTF-8";
        if (body.mimeType() != null) {
            charset = MimeUtil.parseCharset(body.mimeType());
        }

        try {
            InputStreamReader isr = new InputStreamReader(body.in(), charset);
            return serializer.read((Class<?>) type, isr);
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        return null;
    }


}