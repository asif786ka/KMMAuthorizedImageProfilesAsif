//
//  SecurePreferences.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import Security

/// A class responsible for securely saving and retrieving user data, such as tokens and user IDs, using Keychain.
class SecurePreferences {

    /// Saves a token in Keychain.
    /// - Parameter token: The token string to be saved.
    func saveToken(token: String) {
        save(key: "TOKEN", value: token)
    }

    /// Retrieves the saved token from Keychain.
    /// - Returns: The saved token string, or nil if not found.
    func getToken() -> String? {
        return read(key: "TOKEN")
    }

    /// Clears the saved token from Keychain.
    func clearToken() {
        delete(key: "TOKEN")
    }

    /// Saves the user ID in Keychain.
    /// - Parameter userId: The user ID string to be saved.
    func saveUserId(userId: String) {
        save(key: "USER_ID", value: userId)
    }

    /// Retrieves the saved user ID from Keychain.
    /// - Returns: The saved user ID string, or nil if not found.
    func getUserId() -> String? {
        return read(key: "USER_ID")
    }

    /// Clears the saved user ID from Keychain.
    func clearUserId() {
        delete(key: "USER_ID")
    }

    /// Saves a string value in Keychain.
    /// - Parameters:
    ///   - key: The key under which to save the value.
    ///   - value: The value to be saved.
    private func save(key: String, value: String) {
        let data = Data(value.utf8)
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]
        SecItemAdd(query as CFDictionary, nil)
    }

    /// Reads a string value from Keychain.
    /// - Parameter key: The key associated with the value.
    /// - Returns: The retrieved string value, or nil if not found.
    private func read(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var item: CFTypeRef?
        SecItemCopyMatching(query as CFDictionary, &item)
        guard let data = item as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    /// Deletes a value from Keychain.
    /// - Parameter key: The key associated with the value to be deleted.
    private func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}

