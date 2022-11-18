package com.smartsparrow.plugin.semver;

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.expr.Expression;
import com.github.zafarkhaja.semver.expr.ExpressionParser;
import com.github.zafarkhaja.semver.expr.LexerException;
import com.github.zafarkhaja.semver.expr.UnexpectedTokenException;
import com.smartsparrow.plugin.lang.VersionParserFault;

/**
 * This class represents expression which is used to search for version.
 * If expression contains pre-release and build labels the original expression can not be parsed to jsemver {@link Expression}
 * and in this case {@link #getExprString()} should be used for search, otherwise {@link #getParsedExpr()}
 *
 * @see Expression
 * @see <a href="https://semver.org/">Semantic Versioning</a>
 * @see <a href="https://github.com/zafarkhaja/jsemver">Java SemVer library</a>
 */
public class SemVerExpression {

    private String exprString;
    private Expression parsedExpr;
    private boolean searchByEquals;

    private SemVerExpression(String exprString, Expression parsedExpr, boolean searchByEquals) {
        this.exprString = exprString;
        this.parsedExpr = parsedExpr;
        this.searchByEquals = searchByEquals;
    }

    /**
     * Parses the expression and instantiates SemVerExpression
     *
     * @param exprString string representation of expression (ex. "1.2.*")
     * @return SemVerExpression
     * @throws VersionParserFault if expression can not be parsed
     */
    public static SemVerExpression from(String exprString) throws VersionParserFault {
        Expression expression;
        boolean searchByEquals;
        try {
            Parser<Expression> parser = ExpressionParser.newInstance();
            expression = parser.parse(exprString);
            searchByEquals = false;
        } catch (LexerException e) {
            expression = null; //happens if versionExpr contains pre-release or build part, ex '1.2.3-alpha'.
            searchByEquals = true;
        } catch (UnexpectedTokenException e) {
            throw new VersionParserFault(String.format("Can not parse version '%s'", exprString));
        }

        return new SemVerExpression(exprString, expression, searchByEquals);
    }

    /**
     * Defines how this expression should be used for searching
     *
     * @return {@code true} if search by full match should be used, otherwise {@code false}
     */
    public boolean isSearchByEquals() {
        return searchByEquals;
    }

    Expression getParsedExpr() {
        return parsedExpr;
    }

    String getExprString() {
        return exprString;
    }

}
