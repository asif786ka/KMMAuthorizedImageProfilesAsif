//
//  LoginViewModel.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import shared
import Combine

/// A SwiftUI ObservableObject ViewModel for managing login and registration logic.
class LoginViewModel: ObservableObject {
    @Published var loginState: LoginState = .idle // The current state of the login process

    private let repository: UserRepository
    private let securePreferences: SecurePreferences

    /// Initializes the ViewModel with required dependencies.
    init(repository: UserRepository, securePreferences: SecurePreferences) {
        self.repository = repository
        self.securePreferences = securePreferences
    }

    /// Calls the login method and updates the state accordingly.
    /// - Parameters:
    ///   - email: The user's email.
    ///   - password: The user's password.
    func login(email: String, password: String) {
        loginState = .loading
        repository.login(email: email, password: password) { result, error in
            if let result = result {
                self.securePreferences.saveToken(token: result.token)
                self.loginState = .success(tokenResponse: result)
            } else if let error = error {
                self.loginState = .error(message: error.localizedDescription)
            } else {
                self.loginState = .error(message: "Unknown error occurred")
            }
        }
    }

    /// Calls the register method and updates the state accordingly.
    /// - Parameters:
    ///   - email: The user's email.
    ///   - password: The user's password.
    func register(email: String, password: String) {
        loginState = .loading
        repository.register(email: email, password: password) { result, error in
            if let result = result {
                self.securePreferences.saveToken(token: result.token)
                self.securePreferences.saveUserId(userId: result.userid)
                self.loginState = .success(tokenResponse: result)
            } else if let error = error {
                self.loginState = .error(message: error.localizedDescription)
            } else {
                self.loginState = .error(message: "Unknown error occurred")
            }
        }
    }

    /// Resets the login state to idle.
    func resetState() {
        loginState = .idle
    }
}

/// Enum representing different states of the login process.
enum LoginState {
    case idle
    case loading
    case success(tokenResponse: TokenResponse)
    case error(message: String)
}

