//
// Created by Josh Barton on 4/2/24.
//
#pragma once

#include "pointer.h"
#include "string"
#include <stdio.h>

class Val;

CLASS (Env) {

public:
    static PTR(Env) empty;

    virtual PTR(Val) lookup(std::string find_name) = 0;

};


class EmptyEnv : public Env {
public:

    EmptyEnv() = default;

    PTR(Val) lookup(std::string find_name);

};


class ExtendedEnv : public Env {
private:
    std::string name;
    PTR(Val) val;
    PTR(Env) rest;

public:
    ExtendedEnv(std::string name, PTR(Val) val, PTR(Env) rest);

    PTR(Val) lookup(std::string find_name);

};