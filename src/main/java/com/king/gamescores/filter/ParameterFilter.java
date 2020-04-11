package com.king.gamescores.filter;

import com.king.gamescores.server.HttpMethod;
import com.king.gamescores.util.Strings;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ParameterFilter extends Filter {

    public static final String PARAMETERS = "parameters";
    public static final String SCORE_PARAM = "score";

    private static final String REQUEST_PARAM_SEPARATOR = "[&]";
    private static final String EQUAL_SEPARATOR = "[=]";
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    @Override
    public String description() {
        return "Parses the requested URI for parameters";
    }

    @Override
    public void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
        parseGetParameters(exchange);
        parsePostParameters(exchange);
        chain.doFilter(exchange);
    }

    private void parseGetParameters(HttpExchange exchange) throws UnsupportedEncodingException {
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        exchange.setAttribute(PARAMETERS, parameters);
    }

    private void parsePostParameters(HttpExchange exchange) throws IOException {
        if (!HttpMethod.POST.toString().equalsIgnoreCase(exchange.getRequestMethod())) {
            return;
        }
        Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute(PARAMETERS);
        String query;
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), ENCODING)) {
            BufferedReader br = new BufferedReader(isr);
            query = br.readLine();
        }
        parseQuery(query, parameters);
    }

    private void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
        if (!Strings.isNotEmpty(query)) {
            return;
        }
        for (String pair : query.split(REQUEST_PARAM_SEPARATOR)) {

            String[] param = pair.split(EQUAL_SEPARATOR);

            String key = null;
            String value = null;
            if (param.length == 1) {
                key = URLDecoder.decode(SCORE_PARAM, ENCODING.name());
                value = URLDecoder.decode(param[0], ENCODING.name());
            } else if (param.length > 1) {
                key = URLDecoder.decode(param[0], ENCODING.name());
                value = URLDecoder.decode(param[1], ENCODING.name());
            }

            if (Strings.isNotEmpty(key) && Strings.isNotEmpty(value) && !parameters.containsKey(key)) {
                parameters.put(key, value);
            }
        }
    }
}
