//
//  HomeViewModel.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation

/// A SwiftUI ObservableObject ViewModel for managing the home screen logic, including logout.
class HomeViewModel: ObservableObject {
    private let securePreferences: SecurePreferences

    /// Initializes the ViewModel with required dependencies.
    init(securePreferences: SecurePreferences) {
        self.securePreferences = securePreferences
    }

    /// Logs the user out by clearing the stored token.
    func logout() {
        securePreferences.clearToken()
    }
}
