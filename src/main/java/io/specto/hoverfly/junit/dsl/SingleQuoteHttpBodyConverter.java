package io.specto.hoverfly.junit.dsl;

public class SingleQuoteHttpBodyConverter implements HttpBodyConverter {

    private String convertedBody;

    private SingleQuoteHttpBodyConverter(String json) {
        this.convertedBody = convert(json);
    }

    public static SingleQuoteHttpBodyConverter jsonWithSingleQuotes(String body) {
        return new SingleQuoteHttpBodyConverter(body);
    }

    @Override
    public String body() {
        return convertedBody;
    }

    @Override
    public String contentType() {
        return APPLICATION_JSON;
    }

    /**
     * Reads the input text with possible single quotes as delimiters
     * and returns a String correctly formatted.
     * <p>For convenience, single quotes as well as double quotes
     * are allowed to delimit strings. If single quotes are
     * used, any quotes, single or double, in the string must be
     * escaped (prepend with a '\').
     *
     * @param text the input data
     * @return String without single quotes
     */
    private static String convert(String text) {
        StringBuilder builder = new StringBuilder();
        boolean single_context = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\\') {
                i = i + 1;
                if (i < text.length()) {
                    ch = text.charAt(i);
                    if (!(single_context && ch == '\'')) {
                        // unescape ' inside single quotes
                        builder.append('\\');
                    }
                }
            } else if (ch == '\'') {
                // Turn ' into ", for proper string
                ch = '"';
                single_context = ! single_context;
            }
            builder.append(ch);
        }

        return builder.toString();
    }
}
