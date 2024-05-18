//
// Created by Josh Barton on 2/23/24.
//

#ifndef CRYPTOGRAPHYBLOCKANDSTREAMS_BLOCKCIPHER_H
#define CRYPTOGRAPHYBLOCKANDSTREAMS_BLOCKCIPHER_H

#include <string>
#include <array>

using Block = std::array<uint8_t, 8>;
using namespace std;

class BlockCipher {

private:
    Block key;
    Block plaintext;
    array<array<uint8_t, 256>, 8> substitutionTables;
    array<array<uint8_t, 256>, 8> reverseSubstitutionTables;
    Block encryption;
    Block decryption;
    string password;


public:

    BlockCipher(string& password);

    //destructor
    //~BlockCipher();

    Block generateEncryptionKey(const std::string &password);

    void generateSubstitutionTables();

    void encrypt( std::array<uint8_t,8>& state, std::array<uint8_t,8>& key );

    void decrypt( std::array<uint8_t,8>& state, std::array<uint8_t,8>& key );

    void printMessage(const Block& message);

};




















#endif //CRYPTOGRAPHYBLOCKANDSTREAMS_BLOCKCIPHER_H
