//
// Created by Josh Barton on 3/7/24.
//

#ifndef MSDSCRIPT_VAL_H
#define MSDSCRIPT_VAL_H

#include <string>
#include <stdexcept>
#include <sstream>
#include "pointer.h"
#include <memory>

/**
 * \file Val.h
 * \brief value class
 *
 * Contains all the functions declarations for the value class and its child classes
 */

class Expr;

class Env;

/**
 * \brief Value class that has many methods to alter, compare, and print the contents of the value object
 */
CLASS (Val) {
public:
    virtual bool equals(PTR(Val) e) = 0;

    virtual void print(std::ostream &ot) = 0;

    virtual PTR(Val) add_to(PTR(Val) other_val) = 0;

    virtual PTR(Val) mult_with(PTR(Val) other_val) = 0;

    virtual PTR(Val) call(PTR(Val) actual_arg) = 0;

    virtual PTR (Expr) to_expr() = 0;

    virtual bool is_true() = 0;


    std::string to_string() {
        std::stringstream st("");
        this->print(st);
        return st.str();
    }

};


/**
 * \brief Number class that has many methods to alter, compare, and print the contents of the Number object
 */
class NumVal : public Val {
public:
    int val; ///< integer that is the value of the number object
    NumVal(int val);

    bool equals(PTR(Val) e);

    void print(std::ostream &ot);

    PTR(Val) add_to(PTR(Val) other_val);

    PTR(Val) mult_with(PTR(Val) other_val);

    PTR(Val) call(PTR(Val) actual_arg);

    PTR (Expr) to_expr();

    bool is_true();

};


/**
 * \brief Bool value class that has many methods to alter, compare, and print the contents of the bool object
 */
class BoolVal : public Val {
public:
    bool boolean; ///< boolean value of the BoolVal object
    std::string booleanValue;

    BoolVal(bool boolean);

    bool equals(PTR(Val) e);

    void print(std::ostream &ot);

    PTR(Val) add_to(PTR(Val) other_bool);

    PTR(Val) mult_with(PTR(Val) other_bool);

    PTR(Val) call(PTR(Val) actual_arg);

    PTR (Expr) to_expr();

    bool is_true();

};


/**
 * \brief Function value class that has many methods to alter, compare, and print the contents of the FunVal object
 */
class FunVal : public Val {
public:
    std::string formal_arg;

    PTR(Expr) body;

    PTR(Env) env;

    FunVal(std::string formal_arg, PTR(Expr) body, PTR(Env) env = nullptr);

    bool equals(PTR(Val) e);

    void print(std::ostream &ot);

    PTR(Val) add_to(PTR(Val) other_bool);

    PTR(Val) mult_with(PTR(Val) other_bool);

    PTR(Val) call(PTR(Val) actual_arg);

    PTR (Expr) to_expr();

    bool is_true();

};


#endif //MSDSCRIPT_VAL_H
