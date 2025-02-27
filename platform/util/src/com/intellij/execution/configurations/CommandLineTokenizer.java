// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.execution.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Splits input String to tokens being aware of quoted tokens ("foo bar") and escaped spaces & quotes (\"foo\ bar\"),
 * usually used for splitting command line to separate arguments that may contain space symbols.
 * Space and quote are the only symbols that can be escaped
 */
public final class CommandLineTokenizer extends StringTokenizer {

    private static final String DEFAULT_DELIMITERS = " \t\n\r\f";
    private final List<String> myTokens = new ArrayList<>();
    private int myCurrentToken = 0;
    private final boolean myHandleEscapedWhitespaces;

    public CommandLineTokenizer(String str) {
      this(str, false);
    }

    public CommandLineTokenizer(String str, boolean handleEscapedWhitespaces) {
        super(str, DEFAULT_DELIMITERS, true);
        myHandleEscapedWhitespaces = handleEscapedWhitespaces;
        parseTokens();
    }

    @Override
    public boolean hasMoreTokens() {
        return myCurrentToken < myTokens.size();
    }

    @Override
    public String nextToken() {
        return myTokens.get(myCurrentToken++);
    }

    public String peekNextToken() {
        return myTokens.get(myCurrentToken);
    }

    @Override
    public int countTokens() {
        return myTokens.size() - myCurrentToken;
    }


    @Override
    public String nextToken(String delim) {
        throw new UnsupportedOperationException();
    }

    private void parseTokens() {
        String token;
        while ((token = nextTokenInternal()) != null) {
            myTokens.add(token);
        }
    }

    private String nextTokenInternal() {
        String nextToken;
        do {
            if (super.hasMoreTokens()) {
                nextToken = super.nextToken();
            } else {
                nextToken = null;
            }
        } while (nextToken != null && nextToken.length() == 1 && DEFAULT_DELIMITERS.indexOf(nextToken.charAt(0)) >= 0);

        if (nextToken == null) {
            return null;
        }

        int i;
        int quotationMarks = 0;
        final StringBuilder buffer = new StringBuilder();

        do {
            while ((i = nextToken.indexOf('"')) >= 0) {
                boolean isEscapedQuote = isEscapedAtPos(nextToken, i);
                if (!isEscapedQuote) quotationMarks++;
                buffer.append(nextToken, 0, isEscapedQuote ? i - 1 : i);
                if (isEscapedQuote) buffer.append('"');
                nextToken = nextToken.substring(i + 1);
            }

            boolean isEscapedWhitespace = false;
            if (myHandleEscapedWhitespaces && quotationMarks == 0 && nextToken.endsWith("\\") && super.hasMoreTokens()) {
              isEscapedWhitespace = true;
              buffer.append(nextToken, 0, nextToken.length() - 1);
              buffer.append(super.nextToken());
            }
            else {
              buffer.append(nextToken);
            }

            if ((isEscapedWhitespace || (quotationMarks & 1) == 1) && super.hasMoreTokens()) {
                nextToken = super.nextToken();
            } else {
                nextToken = null;
            }
        } while (nextToken != null);

        return buffer.toString();
    }

    private static boolean isEscapedAtPos(String token, int pos) {
        int escapeCount = 0;
        --pos;
        while (pos >= 0 && token.charAt(pos) == '\\') {
            ++escapeCount;
            --pos;
        }
        return (escapeCount & 1) == 1;
    }
}
