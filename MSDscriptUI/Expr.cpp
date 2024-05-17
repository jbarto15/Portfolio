//
// Created by Josh Barton on 1/16/24.
//

#include "Expr.h"
#include "Val.h"
#include "Env.h"

/**
 * \file Expr.cpp
 * \brief contains the implementations of the expression class and its child classes
 *
 * This file contains the implementations for all the methods in the Num, Add, Mult, Variable, Let, Bool, Equals, If, and Function classes
 *
 * \author Josh Barton
 */


/**
 * \brief a constructor for a Num object
 * \param val, an integer value that is stored inside the object
 * \return a num object with the value inside
 */
NumExpr::NumExpr(int val) {
    this->val = val;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, or Variable object
 * \return a boolean value based on if the object is equal to the other object
 */
bool NumExpr::equals(PTR(Expr) e) {
    PTR(NumExpr) num = CAST (NumExpr)(e);
    if ( num == nullptr ) {
        return false;
    }
    return this->val == num->val;
}


/**
 * \brief Interprets the value contained in the number object
 * \return an integer that gives the value of the number
 */
PTR(Val) NumExpr::interp(PTR(Env) env) {
    return NEW (NumVal)(val);
}


/**
 * \brief Checks if the number expression has a variable object in it
 * \return returns false
 */
bool NumExpr::has_variable() {
    return false;
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire number expression object with the substitution
 */
PTR(Expr) NumExpr::subst(std::string s, PTR(Expr) e) {
    return NEW (NumExpr)(this->val);
}


/**
 * \brief Function that prints the contents of the Num object
 * \param ot, a an output stream
 */
void NumExpr::print(std::ostream &ot) {
    ot << std::to_string(val);
}


/**
 * \brief Function that prints the contents of the Num object in a prettier format
 * \param ot, a an output stream
 */
void NumExpr::pretty_print(std::ostream &ot) {
    ot << std::to_string(val);
}


/**
 * \brief Helper function that prints the contents of the Num object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void NumExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {
    ot << std::to_string(val);
}


/**
 * \brief Constructor that takes in two expressions and assigns them as the left hand side and the right hand side
 * \param lhs, left hand side of the expression
 * \param rhs, right hand side of the expression
 */
AddExpr::AddExpr(PTR(Expr) lhs, PTR(Expr) rhs) {
    this->lhs = lhs;
    this->rhs = rhs;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, or Variable object
 * \return a boolean value based on if the object is equal to the other object
 */
bool AddExpr::equals(PTR(Expr) e) {
    PTR(AddExpr) add = CAST (AddExpr)(e);
    if ( add == nullptr ) {
        return false;
    }
    return this->lhs->equals(add->lhs) && this->rhs->equals(add->rhs);
}


/**
 * \brief Interprets the left hand side and right hand side of the expression
 * \return an integer that gives the value of the objects in the left hand side and right hand side
 */
PTR(Val) AddExpr::interp(PTR(Env) env) {
    if ( env == nullptr ) {
        env = Env::empty;
    }

    return lhs->interp(env)
            ->add_to(rhs->interp(env));
}


/**
 * \brief Checks if the Add expression has a variable object in it
 * \return returns true or false depending on if there is a variable object in either the left hand side or right hand side of the expression
 */
bool AddExpr::has_variable() {
    return lhs->has_variable() || rhs->has_variable();
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire add expression object with the substitution
 */
PTR(Expr) AddExpr::subst(std::string s, PTR(Expr) e) {
    return NEW (AddExpr)(this->lhs->subst(s, e), this->rhs->subst(s, e));
}


/**
 * \brief Function that prints the contents of the Add object with parentheses around each expression
 * \param ot, a an output stream
 */
void AddExpr::print(std::ostream &ot) {
    ot << "(";
    this->lhs->print(ot);
    ot << "+";
    this->rhs->print(ot);
    ot << ")";
}


/**
 * \brief Function that prints the contents of the Add object in a prettier format
 * \param ot, a an output stream
 */
void AddExpr::pretty_print(std::ostream &ot) {
    std::streampos position = ot.tellp();
    pretty_print_at(ot, prec_add, position, false);
}


/**
 * \brief Helper function that prints the contents of the Add object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void AddExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {

    if ( precedence > prec_add ) {
        ot << "(";
    }

    this->lhs->pretty_print_at(ot, static_cast<precedence_t>(prec_add + 1), pos, true);
    ot << " + ";
    this->rhs->pretty_print_at(ot, prec_add, pos, false);

    if ( precedence > prec_add ) {
        ot << ")";
    }
}


/**
 * \brief Constructor that takes in two expressions and assigns them as the left hand side and the right hand side
 * \param lhs, left hand side of the expression
 * \param rhs, right hand side of the expression
 */
MultExpr::MultExpr(PTR(Expr) lhs, PTR(Expr) rhs) {
    this->lhs = lhs;
    this->rhs = rhs;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, or Variable object
 * \return a boolean value based on if the object is equal to the other object
 */
bool MultExpr::equals(PTR(Expr) e) {
    PTR(MultExpr) mult = CAST (MultExpr)(e);
    if ( mult == nullptr ) {
        return false;
    }
    return this->lhs->equals(mult->lhs) && this->rhs->equals(mult->rhs);
}


/**
 * \brief Interprets the left hand side and right hand side of the expression
 * \return an integer that gives the value of the objects in the left hand side and right hand side
 */
PTR(Val) MultExpr::interp(PTR(Env) env) {
    if ( env == nullptr ) {
        env = Env::empty;
    }

    return lhs->interp(env)
            ->mult_with(rhs->interp(env));
}


/**
 * \brief Checks if the Mult expression has a variable object in it
 * \return returns true or false depending on if there is a variable object in either the left hand side or right hand side of the expression
 */
bool MultExpr::has_variable() {
    return lhs->has_variable() || rhs->has_variable();
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire Mult expression object with the substitution
 */
PTR(Expr) MultExpr::subst(std::string s, PTR(Expr) e) {
    return NEW (MultExpr)(this->lhs->subst(s, e), this->rhs->subst(s, e));
}


/**
 * \brief Function that prints the contents of the Mult object in a prettier format
 * \param ot, a an output stream
 */
void MultExpr::print(std::ostream &ot) {
    ot << "(";
    this->lhs->print(ot);
    ot << "*";
    this->rhs->print(ot);
    ot << ")";
}


/**
 * \brief Function that prints the contents of the Mult object in a prettier format
 * \param ot, a an output stream
 */
void MultExpr::pretty_print(std::ostream &ot) {
    std::streampos position = ot.tellp();
    pretty_print_at(ot, prec_mult, position, false);
}


/**
 * \brief Helper function that prints the contents of the Mult object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void MultExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {

    if ( precedence > prec_mult ) {
        ot << "(";
    }

    this->lhs->pretty_print_at(ot, static_cast<precedence_t>(prec_mult + 1), pos, true);
    ot << " * ";
    this->rhs->pretty_print_at(ot, prec_mult, pos, false);

    if ( precedence > prec_mult ) {
        ot << ")";
    }
}


/**
 * \brief a constructor for a Variable object
 * \param val, a string value that is stored inside the object
 * \return a Variable object with the value inside
 */
VarExpr::VarExpr(std::string value) {
    this->value = value;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, or Variable object
 * \return a boolean value based on if the object is equal to the other object
 */
bool VarExpr::equals(PTR(Expr) e) {
    PTR(VarExpr) var = CAST (VarExpr)(e);
    if ( var == nullptr ) {
        return false;
    }
    return this->value == var->value;
}


/**
 * \brief Throws a runtime error when trying to interpret a Variable object because it doesn't have a value in it
 * \return an integer that notifies the system of an error
 */
PTR(Val) VarExpr::interp(PTR(Env) env) {
    if ( env == nullptr ) {
        env = Env::empty;
    }

    return env->lookup(this->value);
}


/**
 * \brief Checks if the Variable expression has a variable object in it
 * \return returns true
 */
bool VarExpr::has_variable() {
    return true;
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire Variable expression object with the substitution
 */
PTR(Expr) VarExpr::subst(std::string s, PTR(Expr) e) {
    //check if the s exists
    if ( s == this->value ) {
        return e;
    }

    return NEW (VarExpr)(this->value);
}


/**
 * \brief Function that prints the contents of the Variable object
 * \param ot, a an output stream
 */
void VarExpr::print(std::ostream &ot) {
    ot << this->value;
}


/**
 * \brief Function that prints the contents of the Variable object in a prettier format
 * \param ot, a an output stream
 */
void VarExpr::pretty_print(std::ostream &ot) {
    ot << this->value;
}


/**
 * \brief Helper function that prints the contents of the Variable object in a prettier format
 * \param ot, a an output stream
 */
void VarExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {
    ot << this->value;
}


/**
 * \brief A constructor for a LetExpr object
 * \param variable object is passed in as a parameter
 */
LetExpr::LetExpr(std::string val, PTR(Expr) sub, PTR(Expr) body) {
    this->value = val;
    this->rhs = sub;
    this->body = body;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, Variable, or LetExpr object
 * \return a boolean value based on if the object is equal to the other object
 */
bool LetExpr::equals(PTR(Expr) e) {
    PTR(LetExpr) let = CAST (LetExpr)(e);
    if ( let == nullptr ) {
        return false;
    }

    return this->value == let->value && this->rhs->equals(let->rhs) && this->body->equals(let->body);
}


/**
 * \brief Interprets the value of the body expression by substituting the variable in the body expression with the value in the LetExpr object and then
 * performing the calculation
 * \return the result of the body expression
 */
PTR(Val) LetExpr::interp(PTR(Env) env) {
    if ( env == nullptr ) {
        env = Env::empty;
    }

    PTR(Val) rhs_val = rhs->interp(env);
    PTR(Env) new_env = NEW (ExtendedEnv) (this->value, rhs_val, env);
    return body->interp(new_env);
}


/**
 * \brief Checks if the LetExpr expression has a variable object in it on either the right hand side expression or the body expression.
 * It will not have a variable expression on the left hand side because the left hand side is just a name of the variable. Not a variable object itself.
 * \return returns false
 */
bool LetExpr::has_variable() {

    if ( this->rhs->has_variable() || this->body->has_variable()) {
        return true;
    } else {
        return false;
    }
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire number expression object with the substitution
 */
PTR(Expr) LetExpr::subst(std::string s, PTR(Expr) e) {
    //check if the string given is equal to the string we already have in our object, if so subst the rhs
    if ( s == this->value ) {
        PTR(Expr) new_rhs = this->rhs->subst(s, e);

        return NEW (LetExpr)(this->value, new_rhs, this->body); //was s in first slot
    } else {

        PTR(Expr) new_rhs = this->rhs->subst(s, e);
        PTR(Expr) new_body = this->body->subst(s, e);

        return NEW (LetExpr)(this->value, new_rhs, new_body);
    }
}


/**
 * \brief Function that prints the contents of the LetExpr object
 * \param ot, a an output stream
 */
void LetExpr::print(std::ostream &ot) {
    ot << "(_let ";
    ot << this->value;
    ot << "=";
    this->rhs->print(ot);
    ot << " _in ";
    this->body->print(ot);
    ot << ")";
}


/**
 * \brief Function that prints the contents of the LetExpr object in a prettier format
 * \param ot, a an output stream
 */
void LetExpr::pretty_print(std::ostream &ot) {
    std::streampos position = ot.tellp();
    pretty_print_at(ot, prec_none, position, false);
}


/**
 * \brief Helper function that prints the contents of the LetExpr object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void LetExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {

    if ( needParentheses ) {
        ot << "(";
    }

    std::streampos firstPosition = ot.tellp();

    ot << "_let " << this->value << " = ";
    this->rhs->pretty_print_at(ot, prec_none, pos, false);
    ot << '\n';

    std::streampos recordPosition = ot.tellp();
    for ( int i = 0; i < firstPosition - pos; i++ ) {
        ot << " ";
    }

    ot << "_in  ";
    this->body->pretty_print_at(ot, prec_none, recordPosition, true);

    if ( needParentheses ) {
        ot << ")";
    }

}


/**
 * \brief A constructor for a BoolExpr object
 * \param a boolean
 */
BoolExpr::BoolExpr(bool boolean) {
    this->boolean = boolean;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, Variable, LetExpr, BoolExpr, EqExpr, or IfExpr object
 * \return a boolean value based on if the object is equal to the other object
 */
bool BoolExpr::equals(PTR(Expr) e) {
    PTR(BoolExpr) boolExpression = CAST (BoolExpr)(e);
    if ( boolExpression == nullptr ) {
        return false;
    }

    return this->boolean == boolExpression->boolean;
}


/**
 * \brief Interprets the boolean value of the BoolExpr object.
 *
 * \return true or false depending on if the BoolVal is set to true or false
 */
PTR(Val) BoolExpr::interp(PTR(Env) env) {
    return NEW (BoolVal)(this->boolean);
}


/**
 * \brief Checks if the BoolExpr expression has a variable object in it
 * It will never have a variable in it so just return false
 * \return returns false
 */
bool BoolExpr::has_variable() {
    return false;
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire boolean expression object with the substitution
 */
PTR(Expr) BoolExpr::subst(std::string s, PTR(Expr) e) {
    //can just return the object
    return NEW(BoolExpr)(this->boolean);
}


/**
 * \brief Function that prints the contents of the BoolExpr object
 * \param ot, a an output stream
 */
void BoolExpr::print(std::ostream &ot) {

    if ( this->boolean ) {
        ot << "_true";
    } else {
        ot << "_false";
    }
}


/**
 * \brief Function that prints the contents of the BoolExpr object in a prettier format (not relevant to this subclass)
 * \param ot, a an output stream
 */
void BoolExpr::pretty_print(std::ostream &ot) {
    ot << this->boolean;
}


/**
 * \brief Helper function that prints the contents of the BoolExpr object in a prettier format (not relevant to this subclass)
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void BoolExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {
    ot << this->boolean;
}


/**
 * \brief Constructor that takes in two expressions and assigns them as the left hand side and the right hand side
 * \param lhs, left hand side of the expression
 * \param rhs, right hand side of the expression
 */
EqExpr::EqExpr(PTR(Expr) lhs, PTR(Expr) rhs) {
    this->lhs = lhs;
    this->rhs = rhs;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, Variable, LetExpr, BoolExpr, EqExpr, or IfExpr object
 * \return a boolean value based on if the object is equal to the other object
 */
bool EqExpr::equals(PTR(Expr) e) {
    PTR(EqExpr) eqExpression = CAST (EqExpr)(e);
    if ( eqExpression == nullptr ) {
        return false;
    }
    return this->lhs->equals(eqExpression->lhs) && this->rhs->equals(eqExpression->rhs);
}


/**
 * \brief Interprets the result of an equals expression
 *
 * \return a BoolVal object that is either true or false depending on the result of the interp and equals operation
 */
PTR(Val) EqExpr::interp(PTR(Env) env) {

    if ( env == nullptr ) {
        env = Env::empty;
    }

    //if the lhs is equal to the rhs then return a BoolVal object that is set to true
    if ( this->lhs->interp(env)->equals(this->rhs->interp(env))) {
        return NEW (BoolVal)(true);
    } else {
        return NEW (BoolVal)(false);
    }
}


/**
 * \brief Checks if the EqExpr expression has a variable object in it
 *
 * \return returns true or false depending on if the lhs or rhs contain a variable object
 */
bool EqExpr::has_variable() {
    return this->lhs->has_variable() || this->rhs->has_variable();
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire boolean expression object with the substitution
 */
PTR(Expr) EqExpr::subst(std::string s, PTR(Expr) e) {
    return NEW (EqExpr)(this->lhs->subst(s, e), this->rhs->subst(s, e));
}


/**
 * \brief Function that prints the contents of the EqExpr object
 * \param ot, a an output stream
 */
void EqExpr::print(std::ostream &ot) {
    ot << "(";
    this->lhs->print(ot);
    ot << "==";
    this->rhs->print(ot);
    ot << ")";
}


/**
 * \brief Function that prints the contents of the EqExpr object in a prettier format
 * \param ot, a an output stream
 */
void EqExpr::pretty_print(std::ostream &ot) {
    std::streampos position = ot.tellp();
    pretty_print_at(ot, prec_none, position, false);
}


/**
 * \brief Helper function that prints the contents of the EqExpr object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void EqExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {

    if ( precedence > prec_none ) {
        ot << "(";
    }

    this->lhs->pretty_print_at(ot, static_cast<precedence_t>(prec_none + 1), pos, needParentheses);
    ot << " == ";
    this->rhs->pretty_print_at(ot, prec_none, pos, needParentheses);

    if ( precedence > prec_none ) {
        ot << ")";
    }
}


/**
 * \brief A constructor for a IfExpr object
 * \param Three expression objects are passed in as parameters
 */
IfExpr::IfExpr(PTR(Expr) ifExpr, PTR(Expr) thenExpr, PTR(Expr) elseExpr) {
    this->ifExpr = ifExpr;
    this->thenExpr = thenExpr;
    this->elseExpr = elseExpr;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, Variable, LetExpr, BoolExpr, EqExpr, or IfExpr object
 * \return a boolean value based on if the object is equal to the other object
 */
bool IfExpr::equals(PTR(Expr) e) {
    PTR(IfExpr) let = CAST (IfExpr)(e);
    if ( let == nullptr ) {
        return false;
    }

    return this->ifExpr->equals(let->ifExpr) && this->thenExpr->equals(let->thenExpr) &&
           this->elseExpr->equals(let->elseExpr);
}


/**
 * \brief Interprets the result of the if expression
 * \return - returns a Val object
 */
PTR(Val) IfExpr::interp(PTR(Env) env) {

    if ( env == nullptr ) {
        env = Env::empty;
    }

    //the first expression must evaluate to a boolean. If it's not a boolean than an exception needs to
    //be raised.
    if ( this->ifExpr->interp(env)->equals(NEW (BoolVal)(true)) || this->ifExpr->interp(env)->equals(NEW (BoolVal)(false))) {

        //if the first expression evaluates to a boolean then we need to make it so that if its true we
        //evaluate the then expression
        if ( this->ifExpr->interp(env)->equals(NEW (BoolVal)(true))) {
            return this->thenExpr->interp(env);
        }
            //else we need to evaluate the else expression
        else {
            return this->elseExpr->interp(env);
        }
    }

    //throw an exception because the ifExpr does not evaluate to a boolean
    throw std::runtime_error("if statement doesn't evaluate to a boolean, must evaluate to a boolean");
}


/**
 * \brief Checks if the IfExpr expression has a variable object in it on either the right hand side expression or the body expression.
 * It will not have a variable expression on the left hand side because the left hand side is just a name of the variable. Not a variable object itself.
 * \return returns false
 */
bool IfExpr::has_variable() {
    return this->ifExpr->has_variable() || this->thenExpr->has_variable() || this->elseExpr->has_variable();
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire number expression object with the substitution
 */
PTR(Expr) IfExpr::subst(std::string s, PTR(Expr) e) {
    return NEW (IfExpr)(this->ifExpr->subst(s, e), this->thenExpr->subst(s, e), this->elseExpr->subst(s, e));
}


/**
 * \brief Function that prints the contents of the IfExpr object
 * \param ot, a an output stream
 */
void IfExpr::print(std::ostream &ot) {
    ot << "(_if ";
    this->ifExpr->print(ot);
    ot << " _then ";
    this->thenExpr->print(ot);
    ot << " _else ";
    this->elseExpr->print(ot);
    ot << ")";
}


/**
 * \brief Function that prints the contents of the IfExpr object in a prettier format
 * \param ot, a an output stream
 */
void IfExpr::pretty_print(std::ostream &ot) {
    std::streampos position = ot.tellp();
    pretty_print_at(ot, prec_none, position, false);
}


/**
 * \brief Helper function that prints the contents of the IfExpr object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void IfExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {

    if ( precedence > prec_none ) {
        ot << "(";
    }

    std::streampos firstPosition = ot.tellp();
    std::string kw_pos = std::string( firstPosition - pos, ' ' );

    ot << "_if ";
    this->ifExpr->pretty_print_at(ot, prec_none, pos, needParentheses);
    ot << '\n';

    pos = ot.tellp();

    ot << kw_pos << "_then ";
    this->thenExpr->pretty_print_at(ot, prec_none, pos, needParentheses);
    ot << '\n';

    pos = ot.tellp();

    ot << kw_pos << "_else ";
    this->elseExpr->pretty_print_at(ot, prec_none, pos, needParentheses);

    if ( precedence > prec_none ) {
        ot << ")";
    }


}


/**
 * \brief A constructor for a FunExpr object
 * \param A string that is the formal argument to the function expression. An expression object that is the body of the function
 */
FunExpr::FunExpr(std::string formal_arg, PTR (Expr) body) {
    this->formal_arg = formal_arg;
    this->body = body;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, Variable, LetExpr, BoolExpr, EqExpr, IfExpr or FunExpr object
 * \return a boolean value based on if the object is equal to the other object
 */
bool FunExpr::equals(PTR (Expr) e) {
    PTR(FunExpr) func = CAST (FunExpr)(e);
    if ( func == nullptr ) {
        return false;
    }

    return this->formal_arg == func->formal_arg && this->body->equals(func->body);
}


/**
 * \brief Interprets the result of the func expression
 * \return - returns a Val object
 */
PTR(Val) FunExpr::interp(PTR(Env) env) {
    if ( env == nullptr ) {
        env = Env::empty;
    }
    return NEW (FunVal)(this->formal_arg, this->body, env);
}


/**
 * \brief Checks if the FunExpr expression has a variable object in it
 * A function expression will always have a variable in it
 * \return returns true
 */
bool FunExpr::has_variable() {
    return true;
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire number expression object with the substitution
 */
PTR(Expr)FunExpr::subst(std::string s, PTR(Expr) e) {
    //check if the string given is equal to the string we already have in our object, if so subst the rhs
    if ( s == this->formal_arg ) {
        return NEW(FunExpr)(this->formal_arg, this->body); //THIS;
    } else {
        return NEW (FunExpr)(this->formal_arg, this->body->subst(s, e));
    }
}


/**
 * \brief Function that prints the contents of the FunExpr object
 * \param ot, a an output stream
 */
void FunExpr::print(std::ostream &ot) {
    ot << "(_fun ";
    ot << "(";
    ot << this->formal_arg;
    ot << ") ";
    this->body->print(ot);
    ot << ")";
}


/**
 * \brief Function that prints the contents of the FunExpr object in a prettier format
 * \param ot, a an output stream
 */
void FunExpr::pretty_print(std::ostream &ot) {

    std::streampos position = ot.tellp();
    pretty_print_at(ot, prec_none, position, false);
}


/**
 * \brief Helper function that prints the contents of the FunExpr object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void FunExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {

    if ( precedence > prec_none ) {
        ot << "(";
    }

    std::streampos first_position = ot.tellp();
    std::string kw_pos = std::string( first_position - pos, ' ' );


    ot << "_fun (" << this->formal_arg << ")";
    ot << "\n";

    pos = ot.tellp();
    ot << kw_pos << "  ";

    this->body->pretty_print_at(ot, prec_none, pos, needParentheses );


    if ( precedence > prec_none ) {
        ot << ")";
    }

}


/**
 * \brief A constructor for a CallExpr object
 * \param An expression object that is the formal argument to the function expression. An expression object which is the actual argument
 */
CallExpr::CallExpr(PTR(Expr) to_be_called, PTR(Expr) actual_arg) {
    this->to_be_called = to_be_called;
    this->actual_arg = actual_arg;
}


/**
 * \brief takes an expression and compares other expressions of the same type and determines if they are equal expressions
 * \param e, an expression which can be either a Num, Add, Mult, Variable, LetExpr, BoolExpr, EqExpr, IfExpr, FunExpr or CallExpr object
 * \return a boolean value based on if the object is equal to the other object
 */
bool CallExpr::equals(PTR(Expr) e) {
    PTR(CallExpr) call = CAST (CallExpr)(e);
    if ( call == nullptr ) {
        return false;
    }

    return this->to_be_called->equals(call->to_be_called) && this->actual_arg->equals(call->actual_arg);
}


/**
 * \brief Interprets the result of the call expression
 * \return - returns a Val object
 */
PTR(Val) CallExpr::interp(PTR(Env) env) {
    if ( env == nullptr ) {
        env = Env::empty;
    }

    return this->to_be_called->interp(env)->call(this->actual_arg->interp(env));
}


/**
 * \brief Checks if the CallExpr expression has a variable object in it
 *
 * \return returns true or false
 */
bool CallExpr::has_variable() {
    return this->to_be_called->has_variable() || this->actual_arg->has_variable();
}


/**
 * \brief Substitutes a string with an expression
 * \param s, a string that can be substituted with an expression
 * \param e, an expression that will be substituted with the string value
 * \return the entire number expression object with the substitution
 */
PTR(Expr) CallExpr::subst(std::string s, PTR(Expr) e) {
    return NEW (CallExpr)(this->to_be_called->subst(s, e), this->actual_arg->subst(s, e));
}


/**
 * \brief Function that prints the contents of the FunExpr object
 * \param ot, a an output stream
 */
void CallExpr::print(std::ostream &ot) {
    this->to_be_called->print(ot);
    //ot << "(";
    ot << " ";
    this->actual_arg->print(ot);
    //ot << ")";
}


/**
 * \brief Function that prints the contents of the CallExpr object in a prettier format
 * \param ot, a an output stream
 */
void CallExpr::pretty_print(std::ostream &ot) {

    std::streampos position = ot.tellp();
    pretty_print_at(ot, prec_none, position, false);

}


/**
 * \brief Helper function that prints the contents of the CallExpr object in a prettier format
 * \param ot, a an output stream
 * \param precedence, a precedence level which will determine when a parentheses will be added when printing
 */
void CallExpr::pretty_print_at(std::ostream &ot, precedence_t precedence, std::streampos &pos, bool needParentheses) {

    this->to_be_called->pretty_print_at(ot, prec_none, pos, needParentheses);
    ot << "(";
    this->actual_arg->pretty_print_at(ot, prec_none, pos, needParentheses);
    ot << ")";
}





