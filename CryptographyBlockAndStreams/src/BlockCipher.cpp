//
// Created by Josh Barton on 2/23/24.
//

#include "BlockCipher.h"
#include <algorithm>
#include <iostream>
#include <array>

using Block = std::array<uint8_t, 8>;
using namespace std;


/**
 * Constructor
 * @param password - takes in a string that contains the password
 */
BlockCipher::BlockCipher(string &password) {
    this->password = password;
}


/**
 * GenerateEncryptionKey method generates an encryption key
 * @param password - takes in a string which is the password
 * @return - returns a Block object that contains the encryption key
 */
Block BlockCipher::generateEncryptionKey(const std::string &password) {

    this->key = {0, 0, 0, 0, 0, 0, 0, 0};

    for ( size_t i = 0; i < password.length() - 1; i++ ) {
        key[i % 8] = key[i % 8] ^ password[i];
    }

    cout << "Key Generated" << endl;

    return key;
}


/**
 * GenerateSubstitutionTables method creates substitution tables
 */
void BlockCipher::generateSubstitutionTables() {

    srand(time(NULL));

    for ( array<uint8_t, 256> &table: substitutionTables ) {
        for ( size_t i = 0; i < 256; i++ ) {
            table[i] = static_cast<uint8_t>(i);
        }

        // Shuffle the array using the fisher-yates shuffle
        for ( size_t i = 255; i > 0; i-- ) {
            // Generate a random index between 0 and i
            int j = rand() % (i + 1);

            // Swap the current element with the randomly chosen element
            std::swap(table[i], table[j]);
        }

    }

    for ( array<uint8_t, 256> &table: reverseSubstitutionTables ) {
        for ( size_t i = 0; i < 256; i++ ) {
            table[i] = static_cast<uint8_t>(i);
        }
    }

    for ( size_t i = 0; i < 8; i++ ) {
        for ( size_t j = 0; j < 256; j++ ) {
            reverseSubstitutionTables[i][substitutionTables[i][j]] = j;
        }
    }
}


/**
 * Encrypt method encrypts the plaintext
 * @param state - a Block object that contains the state
 * @param key - a Block object that contains the key
 */
void BlockCipher::encrypt(Block &state, Block &key) {
    // For 16 rounds
    for ( size_t i = 0; i < 16; i++ ) {
        // Xor the current state with the key
        for ( size_t j = 0; j < 8; j++ ) {
            state[j] ^= key[j];
        }
        // Substitute the byte using the appropriate substitution table
        for ( size_t j = 0; j < 8; j++ ) {
            state[j] = substitutionTables[j][state[j]];
        }

        // Shift 1 bit to the left
        uint8_t leftBit = state[0];
        for ( size_t j = 0; j < 7; j++ ) {
            state[j] = state[j] << 1 | state[j + 1] >> 7;
        }

        state[7] = state[7] << 1 | leftBit >> 7;
    }

    cout << "Finished encrypting" << endl;
}


/**
 * Decrypt method which decrypts the ciphertext
 * @param state - a Block object that contains the state
 * @param key - a Block object that contains the key
 */
void BlockCipher::decrypt(Block &state, Block &key) {

    // For 16 rounds
    for ( size_t i = 0; i < 16; i++ ) {

        // Shift 1 bit to the right
        uint8_t rightBit = state[7];
        for ( size_t j = 7; j > 0; j-- ) {
            state[j] = state[j] >> 1 | state[j - 1] << 7;
        }

        // State[0] = state[state.size() - 1] >> 7 | rightBit << 1;
        state[0] = state[0] >> 1 | rightBit << 7;

        // Substitute the byte using the appropriate substitution table
        for ( size_t j = 0; j < 8; j++ ) {
            state[j] = reverseSubstitutionTables[j][state[j]];

        }

        // Xor the current state with the key
        for ( size_t j = 0; j < 8; j++ ) {
            state[j] ^= key[j];
        }

    }

    cout << "finished decrypt" << endl;


}


/**
 * A print method that prints the message
 * @param message - a Block object containing the message
 */
void BlockCipher::printMessage(const Block &message) {
    for ( unsigned char i: message ) {
        std::cout << static_cast<int>(i) << " ";
    }

    cout << endl;
}
