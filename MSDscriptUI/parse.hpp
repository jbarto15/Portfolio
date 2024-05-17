
#ifndef parse_hpp
#define parse_hpp

#include <stdio.h>
#include "Expr.h"
#include "pointer.h"

/**
 * \file parse.hpp
 * \brief contains the functions in the parse.cpp file
 *
 */

static void consume(std::istream &in, int expect);

static void consume_keyword(std::istream &in, std::string expected);

void skip_whitespace(std::istream &in);

PTR (Expr)parse_expr(std::istream &in);

PTR (Expr)parse_comparg(std::istream &in);

PTR (Expr)parse_addend(std::istream &in);

PTR (Expr)parse_inner(std::istream &in);

PTR (Expr)parse_multicand(std::istream &in);

PTR (Expr)parse_num(std::istream &in);

PTR (Expr)parse_variable(std::istream &in);

PTR (Expr)parse_let(std::istream &in);

PTR (Expr)parse_bool(std::istream &in, std::string &kw);

PTR (Expr)parse_if(std::istream &in);

PTR (Expr)parse_fun(std::istream &in);

std::string peek_keyword(std::istream &in);

PTR (Expr)parse_str(std::string s);


#endif


