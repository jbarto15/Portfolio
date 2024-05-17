//
// Created by Josh Barton on 1/16/24.
//

#ifndef MSDSCRIPT_EXPR_H
#define MSDSCRIPT_EXPR_H

/**
 * \file Expr.h
 * \brief expression class
 *
 * Contains all the functions declarations for the expression class and its child classes
 */

#include <string>
#include <stdexcept>
#include <sstream>
#include "pointer.h"
#include <memory>

class Val;

class Env;

/**
 * \brief A new type that is used to determine which expressions have precedence over another
 */
typedef enum {
    prec_none = 0, ///< type when there is no precedence
    prec_eq = 1,   ///< type when there is an equals expression
    prec_add = 2, ///< type when there is an add expression
    prec_mult = 3, ///< type when there is a mult expression
    prec_call = 4   ///< type when there is a let expression
} precedence_t;


/**
 * \brief Expression class that has many methods to alter, compare, and print the contents of the expression object
 */
CLASS (Expr) {
public:
    virtual bool equals(PTR (Expr) e) = 0;

    virtual PTR(Val) interp(PTR(Env) env = nullptr) = 0;

    virtual bool has_variable() = 0;

    virtual PTR(Expr) subst(std::string s, PTR(Expr) e) = 0;

    virtual void print(std::ostream &ot) = 0;

    virtual void pretty_print(std::ostream &ot) = 0;

    virtual void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) = 0;


    std::string to_string() {
        std::stringstream st("");
        this->print(st);
        return st.str();
    }


    std::string to_string_pretty() {
        std::stringstream st("");
        this->pretty_print(st);
        return st.str();
    }

};


/**
 * \brief Number class that has many methods to alter, compare, and print the contents of the Number object
 */
class NumExpr : public Expr {
public:
    int val; ///< integer that is the value of the number expression object
    NumExpr(int val);

    bool equals(PTR(Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR(Expr) subst(std::string s, PTR(Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};

/**
 * \brief Add class that has many methods to alter, compare, and print the contents of the Add object
 */
class AddExpr : public Expr {
public:
    PTR(Expr) lhs; ///< An expression that represents the left hand side of the expression
    PTR(Expr) rhs; ///< An expression that represents the right hand side of the expression
    AddExpr(PTR(Expr) lhs, PTR(Expr) rhs);

    bool equals(PTR(Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR(Expr) subst(std::string s, PTR(Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};

/**
 * \brief Multiplication class that has many methods to alter, compare, and print the contents of the Mult object
 */
class MultExpr : public Expr {
public:
    PTR(Expr) lhs; ///< An expression that represents the left hand side of the expression
    PTR(Expr) rhs; ///< An expression that represents the right hand side of the expression
    MultExpr(PTR(Expr) lhs, PTR(Expr) rhs);

    bool equals(PTR(Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR(Expr) subst(std::string s, PTR(Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};

/**
 * \brief Variable class that has many methods to alter, compare, and print the contents of the Variable object
 */
class VarExpr : public Expr {
public:
    std::string value; ///< string that is the value of the Variable object
    VarExpr(std::string value);

    bool equals(PTR (Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR(Expr) subst(std::string s, PTR(Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};


/**
 * \brief LetExpr class which makes it possible to give a variable a value
 */
class LetExpr : public Expr {
public:
    std::string value; ///< string that is the value of the Variable
    PTR(Expr) rhs; ///< an expression that represents the expression to the right hand side of the equals sign
    PTR(Expr) body; ///< an expression that represents ...
    LetExpr(std::string val, PTR(Expr) substitute, PTR(Expr) body);

    bool equals(PTR (Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR(Expr) subst(std::string s, PTR (Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);

};


/**
 * \brief BoolExpr class which helps us implement conditionals
 */
class BoolExpr : public Expr {

public:
    bool boolean;

    std::string booleanValue;

    BoolExpr(bool boolean);

    bool equals(PTR (Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR (Expr) subst(std::string s, PTR (Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);

};


/**
 * \brief EqExpr class which helps us implement conditionals
 */
class EqExpr : public Expr {

public:
    PTR (Expr) rhs;

    PTR (Expr) lhs;

    EqExpr(PTR (Expr) lhs, PTR (Expr) rhs);

    bool equals(PTR (Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR (Expr) subst(std::string s, PTR (Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};


/**
 * \brief IfExpr class which helps us implement conditionals
 */
class IfExpr : public Expr {

public:
    PTR (Expr) ifExpr;

    PTR (Expr) thenExpr;

    PTR (Expr) elseExpr;

    IfExpr(PTR (Expr) ifExpr, PTR (Expr) thenExpr, PTR (Expr) elseExpr);

    bool equals(PTR (Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR (Expr) subst(std::string, PTR (Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};


/**
 * \brief FunExpr class which helps us implement functions
 */
class FunExpr : public Expr {

public:
    std::string formal_arg;

    PTR (Expr) body;

    FunExpr(std::string variable, PTR (Expr) body);

    bool equals(PTR (Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR (Expr) subst(std::string, PTR (Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};


/**
 * \brief CallExpr class which helps us call functions
 */
class CallExpr : public Expr {

public:
    PTR (Expr) to_be_called;

    PTR (Expr) actual_arg;

    CallExpr(PTR (Expr) to_be_called, PTR (Expr) actual_arg);

    bool equals(PTR (Expr) e);

    PTR(Val) interp(PTR(Env) env = nullptr);

    bool has_variable();

    PTR (Expr) subst(std::string, PTR (Expr) e);

    void print(std::ostream &ot);

    void pretty_print(std::ostream &ot);

    void pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses);
};

#endif //MSDSCRIPT_EXPR_H
