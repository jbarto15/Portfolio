#include <iostream>
#include <array>
#include "BlockCipher.h"
#include "RC4Cipher.h"


int main() {
    // Verify that the program can encrypt and decrypt messages.

    // Create a password
    string password = "userpassword";

    // Create a message
    Block msg = {1, 2, 3, 4, 5, 6, 7, 8};

    // Create a block cipher object
    BlockCipher cipher(password);

    // Generate an encryption key with the password
    Block key = cipher.generateEncryptionKey(password);

    // Generate substitution tables
    cipher.generateSubstitutionTables();

    // Encrypt the message
    cipher.encrypt(msg, key);

    // Print out the encrypted message
    cipher.printMessage(msg);

    // Decrypt
    cipher.decrypt(msg, key);

    // Print out the decrypted state
    cipher.printMessage(msg);

    // Demonstate that trying to decrypt a message using the wrong password (and therefore the wrong key) does not recover the plaintext message.
    string password2 = "wrong";
    BlockCipher cipher2(password2);
    cipher2.generateSubstitutionTables();
    cipher2.printMessage(msg);
    Block key2 = cipher2.generateEncryptionKey(password2);

    cipher.encrypt(msg, key);
    cipher2.decrypt(msg, key2);
    cipher2.printMessage(msg);


    // Try modifying 1 bit of the ciphertext and then decrypting with the correct passwords. What do you see?
    // Toggle the 5th bit (index 4)
    string password3 = "wrongpassword";
    Block msg3 = {1, 2, 3, 4, 5, 6, 7, 8};
    BlockCipher cipher3(password3);
    cipher3.generateSubstitutionTables();
    Block key3 = cipher3.generateEncryptionKey(password3);
    cipher3.encrypt(msg3, key3);
    msg3[0] = msg3[0] ^ (1 << 4);
    cipher3.decrypt(msg3, key3);
    cout << "After modifying 1 bit: ";
    cipher3.printMessage(msg3);



    // RC4 CIPHER

    std::string RC4Key = "secret";
    RC4Cipher rc4Cipher1 = RC4Cipher(RC4Key);
    std::string plaintext = "I love CS6014";
    std::cout << "Plaintext is: " << plaintext << std::endl;
    std::string cipherText = rc4Cipher1.encrypt(plaintext);
    std::cout << "CipherText is: " << cipherText << std::endl;

    RC4Cipher rc4Cipher2 = RC4Cipher(RC4Key);
    std::string originalPlainText = rc4Cipher2.decrypt(cipherText);
    std::cout << "Decrypted CipherText is: " << originalPlainText << std::endl;
    std::cout << "\n";

    // Verify that decrypting a message with a different key than the encryption key does not reveal the plaintext.
    string wrongPassword = "wrongpassword";
    RC4Cipher rc4CipherWrongKey(wrongPassword);
    string decryptedWrongKey = rc4CipherWrongKey.decrypt(cipherText);
    cout << "Decrypted CipherText with wrong key: " << decryptedWrongKey << endl;

    // Verify that encrypting 2 messages using the same keystream is insecure.
    // What do you expect to see if you XOR the two encrypted messages?
    string secondPlaintext = "CS6014 is interesting";
    string secondCipherText = rc4Cipher1.encrypt(secondPlaintext);

    // XOR the two encrypted messages
    string xoredMessages;
    for ( size_t i = 0; i < cipherText.size(); ++i ) {
        xoredMessages += cipherText[i] ^ secondCipherText[i];
    }

    cout << "XORed Messages: " << xoredMessages << endl;

    // Modify part of a message using a bit-flipping attack.
    // For example, try sending the message "Your salary is $1000" encrypted with RC4.
    // Modify the ciphertext so that when decrypted, it says that your salary is $9999, instead.
    string salaryMessage = "Your salary is $1000";
    RC4Cipher rc4CipherSalary(RC4Key);
    string encryptedSalary = rc4CipherSalary.encrypt(salaryMessage);

    // Modify the ciphertext using XOR for a bit-flipping attack
    encryptedSalary[17] = encryptedSalary[17] ^ ('1' ^ '9');

    string decryptedModifiedSalary = rc4CipherSalary.decrypt(encryptedSalary);
    cout << "Decrypted Modified Salary: " << decryptedModifiedSalary << endl;

    return 0;
}
