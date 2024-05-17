//
// Created by Josh Barton on 3/7/24.
//

#include "Val.h"
#include "Expr.h"
#include "Env.h"
#include <memory>

/**
 * \file Val.cpp
 * \brief value class
 *
 * Contains all the function implementations for the value class and its child classes
 */



/**
 * \brief a constructor for a NumVal object
 * \param val, an integer value that is stored inside the object
 * \return a NumVal object with the value inside
 */
NumVal::NumVal(int val) {
    this->val = val;
}


/**
 * \brief takes a Val object and compares other Val objects of the same type and determines if they are equal Val objects
 * \param e, an expression which can be either a NumVal or BoolVal object
 * \return a boolean value based on if the object is equal to the other object
 */
bool NumVal::equals(PTR(Val) e) {
    PTR(NumVal) num = CAST (NumVal)(e);
    if ( num == nullptr ) {
        return false;
    }
    return this->val == num->val;
}


/**
 * \brief Function that prints the contents of the NumVal object
 * \param ot, a an output stream
 */
void NumVal::print(std::ostream &ot) {
    ot << std::to_string(val);
}


/**
 * \brief this method makes it possible to interp the actual value of an Add expression
 * @param other_val
 * @return a Val object
 */
PTR(Val) NumVal::add_to(PTR(Val) other_val) {
    PTR(NumVal) other_num = CAST (NumVal)(other_val);
    if ( other_num == NULL ) throw std::runtime_error("add of non-number");
    return NEW (NumVal)((unsigned) val + (unsigned) other_num->val);
}


/**
 * \brief this method makes it possible to interp the actual value of a Mult expression
 * @param other_val
 * @return a Val object
 */
PTR(Val) NumVal::mult_with(PTR(Val) other_val) {
    PTR(NumVal) other_num = CAST (NumVal)(other_val);
    if ( other_num == NULL ) throw std::runtime_error("mult of non-number");
    return NEW (NumVal)((unsigned) val * (unsigned) other_num->val);
}


/**
 * \brief this method throws an error because you can't call add_to or mult_with on a NumVal or BoolVal
 * @param other_val
 * @return a Val object
 */
PTR(Val) NumVal::call(PTR(Val) actual_arg) {
    throw std::runtime_error("NumVal cannot call");
}


/**
 * \brief this method converts a NumVal object into a NumExpr object
 * @return expression object with this number value objects value field
 */
PTR(Expr) NumVal::to_expr() {
    return NEW (NumExpr)(this->val);
}


/**
 * \brief this method checks if the value of the object is true or false
 * @return - throws an exception since the value of a number expression is not true or false
 */
bool NumVal::is_true() {
    throw std::runtime_error("No value true or false for type NumVal");
}


/**
 * \brief a constructor for a BoolVal object
 * \param boolean value that is stored inside the object
 * \return a BoolVal object with the value inside
 */
BoolVal::BoolVal(bool boolean) {
    this->boolean = boolean;
}


/**
 * \brief takes a Val object and compares other Val objects of the same type and determines if they are equal Val objects
 * \param e, an expression which can be either a NumVal or BoolVal object
 * \return a boolean value based on if the object is equal to the other object
 */
bool BoolVal::equals(PTR(Val) e) {
    PTR(BoolVal) booleanVal = CAST (BoolVal)(e);
    if ( booleanVal == nullptr ) {
        return false;
    }

    return this->boolean == booleanVal->boolean;

}


/**
 * \brief Function that prints the contents of the NumVal object
 * \param ot, a an output stream
 */
void BoolVal::print(std::ostream &ot) {

    if ( this->boolean ) {
        ot << "_true";
    } else {
        ot << "_false";
    }

}


/**
 * \brief this method throws an error because you can't add booleans together
 * @param other_val
 * @return throws a runtime error
 */
PTR(Val) BoolVal::add_to(PTR(Val) other_bool) {
    throw std::runtime_error("cannot add booleans together");
}


/**
 * \brief this method throws an error because you can't multiply booleans together
 * @param other_val
 * @return a Val object
 */
PTR(Val) BoolVal::mult_with(PTR(Val) other_bool) {
    throw std::runtime_error("cannot multiply booleans together");
}


/**
 * \brief this method throws an error because you can't call add_to or mult_with on a NumVal or BoolVal
 * @param other_val
 * @return a Val object
 */
PTR(Val) BoolVal::call(PTR(Val) actual_arg) {
    throw std::runtime_error("BoolVal cannot call");
}


/**
 * \brief this method converts a NumVal object into a NumExpr object
 * @return expression object with this number value objects value field
 */
PTR(Expr) BoolVal::to_expr() {
    return NEW (BoolExpr)(this->boolean);
}


/**
 * \brief this method checks if the value of the object is true or false
 * @return - returns true if the boolean member variable is true, if not, false
 */
bool BoolVal::is_true() {
    return this->boolean;
}


/**
 * \brief a constructor for a FunVal object
 * \param
 * \return a FunVal object with the formal argument and body inside
 */
FunVal::FunVal(std::string formal_arg, PTR(Expr) body, PTR(Env) env) {
    this->formal_arg = formal_arg;
    this->body = body;
    this->env = env;
}


/**
 * \brief takes a Val object and compares other Val objects of the same type and determines if they are equal Val objects
 * \param e, an expression which can be either a NumVal, BoolVal or FunVal object
 * \return a boolean value based on if the object is equal to the other object
 */
bool FunVal::equals(PTR(Val) e) {
    PTR(FunVal) funVal = CAST (FunVal)(e);
    if ( funVal == nullptr ) {
        return false;
    }

    return this->formal_arg == funVal->formal_arg && this->body->equals(funVal->body);

}


/**
 * \brief Function that prints the contents of the FunVal object
 * \param ot, a an output stream
 */
void FunVal::print(std::ostream &ot) {
    ot << "(";
    ot << "_fun ";
    ot << "(";
    ot << this->formal_arg;
    ot << ") ";
    this->body->print(ot);
    ot << ")";

}


/**
 * \brief this method throws an error because you can't add functions together
 * @param other_val
 * @return throws a runtime error
 */
PTR(Val) FunVal::add_to(PTR(Val) other_function) {
    throw std::runtime_error("cannot add function together");
}


/**
 * \brief this method throws an error because you can't multiply functions together
 * @param other_val
 * @return a Val object
 */
PTR(Val) FunVal::mult_with(PTR(Val) other_function) {
    throw std::runtime_error("cannot multiply functions together");
}


/**
 * \brief this method puts two FunVal objects together
 * @param other_val
 * @return a Val object
 */
PTR(Val) FunVal::call(PTR(Val) actual_arg) {
    return this->body->interp(NEW(ExtendedEnv)(this->formal_arg, actual_arg, this->env));
}


/**
 * \brief this method converts a FunVal object into a FunExpr object
 * @return expression object with the appropriate fields
 */
PTR(Expr) FunVal::to_expr() {
    return NEW (FunExpr)(this->formal_arg, this->body);
}


/**
 * \brief this method checks if the value of the object is true or false
 * @return -
 */
bool FunVal::is_true() {
    throw std::runtime_error("FunVal is not of type boolean");
}

