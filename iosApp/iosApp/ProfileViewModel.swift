//
//  ProfileViewModel.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import SwiftUI

/// A SwiftUI ObservableObject ViewModel for managing the user's profile data and updating the avatar.
class ProfileViewModel: ObservableObject {
    @Published var profileState: ProfileState = .loading // The current state of the profile

    private let repository: UserRepository
    private let securePreferences: SecurePreferences

    /// Initializes the ViewModel with required dependencies.
    init(repository: UserRepository, securePreferences: SecurePreferences) {
        self.repository = repository
        self.securePreferences = securePreferences
        loadProfile() // Load profile data when the ViewModel is initialized
    }

    /// Loads the user's profile data and updates the state.
    func loadProfile() {
        guard let userId = securePreferences.getUserId(),
              let token = securePreferences.getToken() else {
            profileState = .error(message: "User ID or Token not found")
            return
        }

        profileState = .loading
        repository.getProfile(userId: userId, token: token, completionHandler: { (result, error) in
            if let result = result {
                self.profileState = .success(profile: result)
            } else if let error = error {
                self.profileState = .error(message: error.localizedDescription)
            } else {
                self.profileState = .error(message: "Unknown error occurred")
            }
        })
    }

    /// Updates the user's avatar and refreshes the profile data.
      /// - Parameter image: The new avatar image as UIImage.
      func updateAvatar(image: UIImage) {
          guard let userId = securePreferences.getUserId(),
                let token = securePreferences.getToken() else {
              profileState = .error(message: "User ID or Token not found")
              return
          }

          // Convert UIImage to a Base64 encoded string, making it optional in case of failure
          let base64String: String? = image.jpegData(compressionQuality: 0.8)?.base64EncodedString()

          // Check if the base64String was successfully generated
          guard let encodedString = base64String else {
              profileState = .error(message: "Failed to encode image to Base64")
              return
          }

          profileState = .loading
          repository.updateAvatar(userId: userId, avatar: encodedString, token: token, completionHandler: { (result, error) in
              if let _ = result {
                  // Assuming we want to reload the profile after updating the avatar
                  self.loadProfile()
              } else if let error = error {
                  self.profileState = .error(message: error.localizedDescription)
              } else {
                  self.profileState = .error(message: "Unknown error occurred")
              }
          })
      }
}

/// Enum representing different states of the profile view.
enum ProfileState {
    case loading
    case success(profile: ProfileResponse)
    case error(message: String)
}


