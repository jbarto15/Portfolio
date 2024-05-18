//
// Created by Josh Barton on 2/26/24.
//

#include "RC4Cipher.h"


/**
 * Constructor
 * @param key - takes a string that represents a key
 */
RC4Cipher::RC4Cipher(string key) {
    this->key = key;
    generateKeyStream();
}


/**
 * Generate key stream method generates a key stream
 */
void RC4Cipher::generateKeyStream() {

    // Fill the K array
    for ( int index = 0; index < K.size() - 1; index++ ) {
        K[index] = static_cast<uint8_t>(key[index % key.length()]);
    }

    // Fill the S array
    for ( int index = 0; index < 256; index++ ) {
        S[index] = index;
    }

    int j = 0;
    for ( int i = 0; i < 256; i++ ) {
        j = (j + S[i] + key[i % key.length()]) % 256;
        std::swap(S[i], S[j]);
    }
    this->i = 0;
    this->j = 0;
}


/**
 * NextByte method gets the next byte in the key stream
 * @return - returns an uint8_t with the next byte in the stream
 */
uint8_t RC4Cipher::nextByte() {

    i = (i + 1) % 256;
    j = (j + S[i]) % 256;
    std::swap(S[i], S[j]);

    return S[(S[i] + S[j]) % 256];

}


/**
 * Encrypt method encrypts the plaintext
 * @param plaintext - takes in a string which contains the plaintext
 * @return - returns a string which contains the ciphertext
 */
string RC4Cipher::encrypt(const string &plaintext) {

    string ciphertext;
    for ( uint8_t c: plaintext ) {
        uint8_t keyStreamByte = nextByte();
        uint8_t encryptedByte = c ^ keyStreamByte;
        ciphertext += encryptedByte;
    }

    return ciphertext;
}


/**
 * Decrypt method decrypts the ciphertext
 * @param ciphertext - a string that contains the ciphertext to be decrypted
 * @return - returns a string that contains the plaintext
 */
string RC4Cipher::decrypt(const string &ciphertext) {

    string plaintext;
    for ( uint8_t c: ciphertext ) {
        uint8_t keyStreamByte = nextByte();
        uint8_t decryptedByte = c ^ keyStreamByte;
        plaintext += decryptedByte;
    }

    return plaintext;
}





