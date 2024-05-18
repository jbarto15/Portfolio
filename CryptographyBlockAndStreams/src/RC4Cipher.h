//
// Created by Josh Barton on 2/26/24.
//

#ifndef CRYPTOGRAPHYBLOCKANDSTREAMS_RC4CIPHER_H
#define CRYPTOGRAPHYBLOCKANDSTREAMS_RC4CIPHER_H

#include <string>
#include <array>

using namespace std;

class RC4Cipher {

private:
    int i = 0;
    int j = 0;
    array<uint8_t, 256> S;
    array<uint8_t, 256> K;
    string key;

public:

    RC4Cipher(string key);

    uint8_t nextByte();

    void generateKeyStream();

    string encrypt(const string &plaintext);

    string decrypt(const string &ciphertext);

};




#endif //CRYPTOGRAPHYBLOCKANDSTREAMS_RC4CIPHER_H
