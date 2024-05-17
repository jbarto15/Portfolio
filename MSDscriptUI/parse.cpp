#include <iostream>
#include <cctype>
#include "parse.hpp"


/**
 * \file parse.cpp
 * \brief contains functions that parse input into the correct expression objects
 *
 * \author Josh Barton
 */


/**
 * \brief Function that consumes a character from the input stream
 * @param in - stream of characters
 * @param expect - the character we expect to be consumed
 */
static void consume(std::istream &in, int expect) {
    int c = in.get();

    if ( c != expect ) {
        throw std::runtime_error("consume mismatch");
    }
}


/**
 * \brief Function that consumes a specific keyword which is a string
 * @param in - stream of characters
 * @param expected - the string we are expecting to consume
 */
static void consume_keyword(std::istream &in, std::string expected) {

    for ( char c: expected ) {

        if ( c == in.peek()) {
            consume(in, c);
        } else {
            throw std::runtime_error("consume mismatch");
        }
    }
}


/**
 * \brief Skip_whitespace function skips any space in the input stream
 *
 * Utilizes the consume function to consume the space characters in the input stream
 * @param in - stream of characters
 */
void skip_whitespace(std::istream &in) {
    while ( true ) {
        int c = in.peek();

        if ( !isspace(c)) {
            break;
        }
        consume(in, c);
    }
}


/**
 * \brief This function parses an expression object
 * @param in - stream of characters
 * @return - returns an expression object
 */
PTR (Expr)parse_expr(std::istream &in) {
    PTR (Expr)e = parse_comparg(in);

    skip_whitespace(in);

    int firstEquals = in.peek();
    int secondEquals = in.peek();

    if ( firstEquals == '=' && secondEquals == '=' ) {
        consume_keyword(in, "==");
        skip_whitespace(in);
        PTR (Expr)rhs = parse_expr(in);
        return NEW (EqExpr)(e, rhs);
    } else {
        return e;
    }
}


/**
 * \brief This function parses a comparsion object and checks for addition between expressions
 * @param in - stream of characters
 * @return - returns an expression object
 */
PTR (Expr)parse_comparg(std::istream &in) {
    PTR (Expr)e = parse_addend(in);

    skip_whitespace(in);

    int c = in.peek();
    if ( c == '+' ) {
        consume(in, '+');
        skip_whitespace(in);
        PTR (Expr)rhs = parse_comparg(in);
        e = NEW (AddExpr)(e, rhs);
    }
    return e;
}


/**
 * \brief This function parses an addition object and checks for multiplication between expressions
 * @param in - stream of characters
 * @return - returns an expression object
 */
PTR (Expr)parse_addend(std::istream &in) {
    PTR (Expr)e = parse_multicand(in);

    skip_whitespace(in);
    int c = in.peek();
    if ( c == '*' ) {
        consume(in, '*');
        skip_whitespace(in);
        PTR (Expr)rhs = parse_addend(in);
        return NEW (MultExpr)(e, rhs);
    } else {
        return e;
    }
}


/**
 * \brief This function parses a multiplication object and checks to see if a CallExpr function needs to be created
 * @param in - stream of characters
 * @return - returns an expression object
 */
PTR (Expr)parse_multicand(std::istream &in) {
    PTR (Expr)e = parse_inner(in);

    while ( in.peek() == '(' ) {
        consume(in, '(');
        PTR (Expr)actual_arg = parse_expr(in);
        consume(in, ')');
        e = NEW (CallExpr)(e, actual_arg);
    }

    return e;
}


/**
 * \brief This function parses all the inner expressions that are nested inside the overall starting expression
 * @param in - stream of characters
 * @return - returns an expression object
 */
PTR (Expr)parse_inner(std::istream &in) {
    skip_whitespace(in);

    std::string kw;

    int c = in.peek();
    if ((c == '-') || isdigit(c)) {
        return parse_num(in);
    } else if ( c == '(' ) {
        consume(in, '(');
        PTR (Expr)e = parse_expr(in);
        skip_whitespace(in);
        c = in.peek();
        if ( c != ')' ) {
            throw std::runtime_error("missing close parenthesis");
        } else {
            consume(in, ')');
        }
        return e;

    } else if ( isalpha(c)) {
        return parse_variable(in);
    } else if ( c == '_' ) {
        consume(in, '_');
        kw = peek_keyword(in);

        if ( kw == "_let" ) {
            return parse_let(in);
        } else if ( kw == "_true" || kw == "_false" ) {
            return parse_bool(in, kw);
        } else if ( kw == "_if" ) {
            return parse_if(in);
        } else if ( kw == "_fun" ) {
            return parse_fun(in);
        } else {
            throw std::runtime_error("invalid input");
        }
    } else {
        consume(in, c);
        throw std::runtime_error("invalid input");
    }
}


/**
 * \brief This function parses a number from the input stream into a NumExpr object
 * @param in - stream of characters
 * @return - returns a NumExpr object
 */
PTR (Expr)parse_num(std::istream &in) {

    int n = 0;
    bool negative = false;

    if ( in.peek() == '-' ) {
        negative = true;
        consume(in, '-');

        if ( !isdigit(in.peek())) {
            throw std::runtime_error("invalid input");
        }
    }

    while ( true ) {
        int c = in.peek();

        if ( isdigit(c)) {
            consume(in, c);
            n = n * 10 + (c - '0');
        } else
            break;
    }

    if ( negative ) {
        n = -n;
    }

    return NEW (NumExpr)(n);

}


/**
 * \brief This function parses a variable from a string and creates a VarExpr object of that variable
 * @param in - stream of characters
 * @return - returns a VarExpr object with the desired variable
 */
PTR (Expr)parse_variable(std::istream &in) {
    skip_whitespace(in);

    std::string s;

    while ( true ) {
        char c = in.peek();

        if ( isalpha(c)) {
            consume(in, c);
            s += c;
        } else {
            if ( in.peek() == '_' || in.peek() == '-' ) {
                throw std::runtime_error("invalid input");
            }
            break;
        }
    }

    return NEW (VarExpr)(s);
}


/**
 * \brief This function parses the keyword _let and everything after the let into a LetExpr object
 * @param in - stream of characters
 * @return - returns a LetExpr object
 */
PTR (Expr)parse_let(std::istream &in) {
    skip_whitespace(in);
    std::string kw;

    PTR (Expr)variable;
    PTR (Expr)rhs;
    PTR (Expr)body;

    int c = in.peek();
    if ( c == 'l' ) {
        consume_keyword(in, "let");
        skip_whitespace(in);
        variable = parse_variable(in);
    }

    skip_whitespace(in);

    if ( in.peek() == '=' ) {
        consume(in, '=');
        skip_whitespace(in);
        rhs = parse_expr(in);
    }

    skip_whitespace(in);

    if ( in.peek() == '_' ) {
        consume(in, '_');
        consume_keyword(in, "in");
        skip_whitespace(in);
        body = parse_expr(in);
    }

    return NEW (LetExpr)(CAST (VarExpr)(variable)->value, rhs, body);
}


/**
 * \brief This function parses the keyword _true or _false into a BoolExpr object
 * @param in - stream of characters
 * @param kw - takes the keyword as a parameter
 * @return - returns a BoolExpr object
 */
PTR (Expr)parse_bool(std::istream &in, std::string &kw) {
    skip_whitespace(in);

    if ( kw == "_true" ) {
        consume_keyword(in, "true");
        return NEW (BoolExpr)(true);
    } else if ( kw == "_false" ) {
        consume_keyword(in, "false");
        return NEW (BoolExpr)(false);
    } else {
        throw std::runtime_error("keyword is not a bool");
    }
}


/**
 * \brief This function parses the keyword _if and turns the information after the _if into an IfExpr object
 * @param in - stream of characters
 * @return - returns an IfExpr object
 */
PTR (Expr)parse_if(std::istream &in) {
    skip_whitespace(in);

    PTR (Expr)ifExpr;
    PTR (Expr)thenExpr;
    PTR (Expr)elseExpr;

    if ( in.peek() == 'i' ) {
        consume_keyword(in, "if");
        skip_whitespace(in);
        ifExpr = parse_expr(in);
    }

    skip_whitespace(in);
    consume(in, '_');

    if ( in.peek() == 't' ) {
        consume_keyword(in, "then");
        skip_whitespace(in);
        thenExpr = parse_expr(in);
    }

    skip_whitespace(in);
    consume(in, '_');

    if ( in.peek() == 'e' ) {
        consume_keyword(in, "else");
        skip_whitespace(in);
        elseExpr = parse_expr(in);
    }

    return NEW (IfExpr)(ifExpr, thenExpr, elseExpr);
}


/**
 * \brief This function parses the keyword _fun and take the information following the _fun keyword and creates a FunExpr object
 * @param in - stream of characters
 * @return - returns a FunExpr object
 */
PTR (Expr)parse_fun(std::istream &in) {
    skip_whitespace(in);

    std::string formal_arg;
    PTR (Expr)body;

    consume_keyword(in, "fun");

    skip_whitespace(in);
    consume(in, '(');

    //consume the formal_arg
    while ( true ) {
        char c = in.peek();

        if ( isalpha(c)) {
            consume(in, c);
            formal_arg += c;
        } else {
            //exception
            if ( in.peek() == '_' || in.peek() == '-' ) {
                throw std::runtime_error("invalid input");
            }
            break;
        }
    }

    skip_whitespace(in);
    consume(in, ')');

    body = parse_expr(in);

    return NEW (FunExpr)(formal_arg, body);
}


/**
 * \brief This function checks the input stream to determine what the keyword is next in the input stream
 * @param in - stream of characters
 * @return - returns a string that contains the keyword that is next in the stream of characters
 */
std::string peek_keyword(std::istream &in) {
    skip_whitespace(in);

    std::string keyword;

    int c = in.peek();

    if ( c == 'l' ) {
        keyword = "_let";
    } else if ( c == 'i' ) {
        keyword = "_if";
    } else if ( c == 't' ) {
        keyword = "_true";
    } else if ( c == 'f' ) {
        consume(in, 'f');
        int nextCharacter = in.peek();

        if ( nextCharacter == 'a' ) {
            keyword = "_false";
        } else {
            keyword = "_fun";
        }

        in.putback(c);

    }

    return keyword;
}


/**
 * \brief This function parses a string and returns an Expr object equivalent to the string
 * @param s - a string of the desired Expr objects to be created
 * @return - returns an expression object
 */
PTR (Expr)parse_str(std::string s) {
    std::istringstream string_stream(s);
    return parse_expr(string_stream);
}

