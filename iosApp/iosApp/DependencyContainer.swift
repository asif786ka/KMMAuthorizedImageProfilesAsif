//
//  DependencyContainer.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import shared

class DependencyContainer {
    static let shared = DependencyContainer()

    // Create a single instance of ApiService
    private let apiService = ApiService()

    // Create and hold a single instance of UserRepository
    private lazy var userRepository: UserRepository = {
        return UserRepository(api: apiService)
    }()

    // Provide a UserRepository instance
    func provideUserRepository() -> UserRepository {
        return userRepository
    }

    // Provide a SecurePreferences instance
    func provideSecurePreferences() -> SecurePreferences {
        return SecurePreferences()
    }
}
