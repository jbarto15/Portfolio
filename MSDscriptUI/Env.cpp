//
// Created by Josh Barton on 4/2/24.
//

#include "Env.h"


PTR(Env) Env::empty = NEW(EmptyEnv)();


ExtendedEnv::ExtendedEnv(std::string name, PTR(Val) val, PTR(Env) rest) {
    this->name = name;
    this->val = val;
    this->rest = rest;
}


PTR(Val) EmptyEnv::lookup(std::string find_name) {
    throw std::runtime_error("free variable: " + find_name);
}


PTR(Val) ExtendedEnv::lookup(std::string find_name) {
    if ( find_name == name ) {
        return val;
    } else {
        return rest->lookup(find_name);
    }
}

